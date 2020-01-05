package xyz.ressor.service.proxy;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;
import xyz.ressor.commons.annotations.ServiceFactory;
import xyz.ressor.commons.exceptions.TypeDefinitionException;
import xyz.ressor.commons.utils.Exceptions;
import xyz.ressor.service.RessorService;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static net.bytebuddy.dynamic.loading.ClassLoadingStrategy.Default.INJECTION;
import static net.bytebuddy.implementation.MethodCall.invoke;
import static net.bytebuddy.implementation.MethodDelegation.toField;
import static net.bytebuddy.implementation.MethodDelegation.toMethodReturnOf;
import static net.bytebuddy.matcher.ElementMatchers.*;
import static xyz.ressor.commons.utils.CollectionUtils.isNotEmpty;
import static xyz.ressor.commons.utils.Exceptions.catchingFunc;
import static xyz.ressor.commons.utils.ReflectionUtils.findAnnotatedExecutables;
import static xyz.ressor.commons.utils.ReflectionUtils.findExecutable;
import static xyz.ressor.commons.utils.RessorUtils.firstNonNull;
import static xyz.ressor.commons.utils.StringUtils.randomString;

public class ServiceProxyBuilder {
    private static final String PROXY_BASE_PACKAGE = "xyz.ressor.service.proxy.";
    private static final String RS_VAR = "__$$rs";
    private static final String RS_METHOD = "__$$grs";
    private static final String RS_METHOD_OBJECT = "__$$grso";
    private final ByteBuddy byteBuddy = new ByteBuddy();
    private final boolean isCacheClasses;
    private final Map<Class<?>, GeneratedClassInfo> classCache = new HashMap<>();

    public ServiceProxyBuilder(boolean isCacheClasses) {
        this.isCacheClasses = isCacheClasses;
    }

    public synchronized <T, D> T buildProxy(ProxyContext<T, D> context) {
        var serviceProxy = new RessorServiceImpl<>(context.getType(), getFactory(context), context.getTranslator(), context.getErrorHandler(),
                context.getInitialInstance(), context.getResource())
                .state(StateVariables.SOURCE, context.getSource());
        Class<? extends T> loadedClass = null;
        if (isCachePossible(context)) {
            var gci = classCache.computeIfAbsent(context.getType(),
                    k -> new GeneratedClassInfo(generateProxyClass(context), context.getProxyDefaultArguments(),
                            context.getClassLoader(), context.isProxyObjectClassMethods()));
            if (gci.isMatches(context)) {
                loadedClass = (Class<? extends T>) gci.loadedClass;
            }
        }
        if (loadedClass == null) {
            loadedClass = generateProxyClass(context);
        }

        try {
            var targetConstructor = loadedClass.getDeclaredConstructor();
            targetConstructor.setAccessible(true);
            var instance = targetConstructor.newInstance();
            var mf = instance.getClass().getDeclaredField(RS_VAR);
            mf.setAccessible(true);
            mf.set(instance, serviceProxy);
            return instance;
        } catch (Throwable t) {
            throw Exceptions.wrap(t);
        }
    }

    private <T, D> boolean isCachePossible(ProxyContext<T, D> context) {
        return isCacheClasses && context.getExtensions().size() == 0;
    }

    private <T, D> Class<? extends T> generateProxyClass(ProxyContext<T, D> context) {
        var b = byteBuddy.subclass(context.getType()).name(generateName(context.getType()));
        if (isNotEmpty(context.getExtensions())) {
            for (var ext : context.getExtensions()) {
                b = ext.interceptProxy(b, context.getType());
            }
        }
        var typeDef = TypeDefinition.of(context.getType(), context.getProxyDefaultArguments());

        var i = b.implement(RessorService.class);
        DynamicType.Builder<? extends T> m = i;
        if (!typeDef.isInterface() && typeDef.getDefaultArguments().length > 0) {
            var constructor = invoke(typeDef.getDefaultConstructor())
                    .with(typeDef.getDefaultArguments());
            m = i.defineConstructor(Visibility.PUBLIC).intercept(constructor);
        }
        var serviceInstanceMethod = invoke(named("instance")).onField(RS_VAR).withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC);
        var f = m.defineField(RS_VAR, RessorServiceImpl.class, Visibility.PRIVATE)
                .defineMethod(RS_METHOD, context.getType(), Visibility.PRIVATE)
                .intercept(serviceInstanceMethod)
                .method(isDeclaredBy(RessorService.class).and(not(isDefaultMethod())))
                .intercept(toField(RS_VAR))
                .method(isDeepDeclaredBy(context.getType()))
                .intercept(toMethodReturnOf(RS_METHOD));
        if (context.isProxyObjectClassMethods()) {
            f = f.defineMethod(RS_METHOD_OBJECT, Object.class, Visibility.PRIVATE)
                    .intercept(serviceInstanceMethod)
                    .method(definedMethod(mt -> isEquals(mt) || isHashCode(mt) || isToString(mt)))
                    .intercept(toMethodReturnOf(RS_METHOD_OBJECT));
        }
        return f.make()
                .load(firstNonNull(context.getClassLoader(), getClass().getClassLoader()), INJECTION)
                .getLoaded();
    }

    private <T> String generateName(Class<T> type) {
        return PROXY_BASE_PACKAGE + type.getSimpleName() + "$RessorProxy$" + randomString();
    }

    private boolean isHashCode(MethodDescription.InDefinedShape target) {
        return target.getParameters().size() == 0 && target.getName().equals("hashCode") &&
                target.getReturnType().represents(int.class);
    }

    private boolean isEquals(MethodDescription.InDefinedShape t) {
        return t.getParameters().size() == 1 && t.getParameters().get(0).getType().represents(Object.class) &&
                t.getName().equals("equals") && t.getReturnType().represents(boolean.class);
    }

    private boolean isToString(MethodDescription.InDefinedShape target) {
        return target.getParameters().size() == 0 && target.getName().equals("toString") &&
                target.getReturnType().represents(String.class);
    }

    private <T, D> Function<D, ? extends T> getFactory(ProxyContext<T, D> context) {
        if (context.getFactory() != null) {
            return context.getFactory();
        } else {
            var type = context.getType();
            var factoryExecutable = findAnnotatedExecutables(type, ServiceFactory.class).stream()
                    .filter(e -> e.getParameterCount() == 1)
                    .filter(e -> e.getParameterTypes()[0].isAssignableFrom(context.getTranslator().outputType()))
                    .filter(e -> !(e instanceof Method) || Modifier.isStatic(e.getModifiers()))
                    .findFirst()
                    .orElse(findExecutable(type, context.getTranslator().outputType()));
            if (factoryExecutable == null) {
                throw new TypeDefinitionException(type, "Unable to find any @ServiceFactory or any matching constructor for the given source");
            }
            factoryExecutable.setAccessible(true);
            if (factoryExecutable instanceof Method) {
                return catchingFunc(a -> (T) ((Method) factoryExecutable).invoke(null, a));
            } else {
                return catchingFunc(a -> (T) ((Constructor) factoryExecutable).newInstance(a));
            }
        }
    }

    private ElementMatcher<? super MethodDescription> isDeepDeclaredBy(Class<?> type) {
        var is = isDeclaredBy(type).and(not(isDefaultMethod()));
        var interfaces = type.getInterfaces();
        if (interfaces != null && interfaces.length > 0) {
            for (var ifc : interfaces) {
                is = is.or(isDeclaredBy(ifc)).or(isDeepDeclaredBy(ifc));
            }
        }
        if (type.getSuperclass() != null && type.getSuperclass() != Object.class) {
            return is.or(isDeepDeclaredBy(type.getSuperclass()));
        } else {
            return is;
        }
    }

    private static class GeneratedClassInfo {
        private final Class<?> loadedClass;
        private final Object[] defaultArguments;
        private final ClassLoader classLoader;
        private final boolean isProxyObjectClassMethods;

        public GeneratedClassInfo(Class<?> loadedClass, Object[] defaultArguments, ClassLoader classLoader,
                                  boolean isProxyObjectClassMethods) {
            this.loadedClass = loadedClass;
            this.defaultArguments = defaultArguments;
            this.classLoader = classLoader;
            this.isProxyObjectClassMethods = isProxyObjectClassMethods;
        }

        public boolean isMatches(ProxyContext<?, ?> ctx) {
            return isProxyObjectClassMethods == ctx.isProxyObjectClassMethods() &&
                    Arrays.equals(defaultArguments, ctx.getProxyDefaultArguments()) && Objects.equals(classLoader, ctx.getClassLoader());
        }
    }

}
