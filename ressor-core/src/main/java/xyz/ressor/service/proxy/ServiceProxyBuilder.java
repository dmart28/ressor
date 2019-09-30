package xyz.ressor.service.proxy;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.ByteCodeElement;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;
import xyz.ressor.commons.annotations.ServiceFactory;
import xyz.ressor.commons.exceptions.TypeDefinitionException;
import xyz.ressor.commons.utils.Exceptions;
import xyz.ressor.service.RessorService;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Function;

import static net.bytebuddy.dynamic.loading.ClassLoadingStrategy.Default.INJECTION;
import static net.bytebuddy.implementation.MethodCall.call;
import static net.bytebuddy.implementation.MethodCall.invoke;
import static net.bytebuddy.implementation.MethodDelegation.to;
import static net.bytebuddy.implementation.MethodDelegation.toMethodReturnOf;
import static net.bytebuddy.matcher.ElementMatchers.*;
import static xyz.ressor.commons.utils.CollectionUtils.isNotEmpty;
import static xyz.ressor.commons.utils.Exceptions.catchingFunc;
import static xyz.ressor.commons.utils.ReflectionUtils.findAnnotatedExecutables;
import static xyz.ressor.commons.utils.ReflectionUtils.findExecutable;
import static xyz.ressor.commons.utils.RessorUtils.firstNonNull;

public class ServiceProxyBuilder {
    private final ByteBuddy byteBuddy = new ByteBuddy();

    public <T> T buildProxy(ProxyContext<T> context) {
        var serviceProxy = new RessorServiceImpl<>(context.getType(), getFactory(context), context.getTranslator(), context.getInitialInstance());
        var b = byteBuddy.subclass(context.getType());
        if (isNotEmpty(context.getExtensions())) {
            for (var ext : context.getExtensions()) {
                b = (DynamicType.Builder<? extends T>) ext.interceptProxy(b, context.getType());
            }
        }
        var typeDef = TypeDefinition.of(context.getType());

        var i = b.implement(RessorService.class);
        DynamicType.Builder<? extends T> m = i;
        if (!typeDef.isInterface() && typeDef.getDefaultArguments().length > 0) {
            var constructor = invoke(typeDef.getDefaultConstructor())
                    .with(firstNonNull(context.getProxyDefaultArguments(), typeDef.getDefaultArguments()));
            m = i.defineConstructor(Visibility.PUBLIC).intercept(constructor);
        }
        var f = m.defineMethod("getRessorUnderlying", context.getType(), Visibility.PUBLIC)
                .intercept(call(serviceProxy::instance))
                .method(isDeclaredBy(RessorService.class))
                .intercept(to(serviceProxy))
                .method(isDeepDeclaredBy(context.getType()))
                .intercept(toMethodReturnOf("getRessorUnderlying"));

        try {
            var loaded = f.make()
                    .load(firstNonNull(context.getClassLoader(), getClass().getClassLoader()), INJECTION)
                    .getLoaded();
            var targetConstructor = loaded.getDeclaredConstructor();
            targetConstructor.setAccessible(true);
            return targetConstructor.newInstance();
        } catch (Throwable t) {
            throw Exceptions.wrap(t);
        }
    }

    private <T> Function<Object, ? extends T> getFactory(ProxyContext<T> context) {
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
        var is = isDeclaredBy(type);
        var interfaces = type.getInterfaces();
        if (interfaces != null && interfaces.length > 0) {
            for (var ifc : interfaces) {
                is = is.or(isDeclaredBy(ifc)).or((ElementMatcher<? super ByteCodeElement>) isDeepDeclaredBy(ifc));
            }
        }
        if (type.getSuperclass() != null && type.getSuperclass() != Object.class) {
            return is.or(isDeepDeclaredBy(type.getSuperclass()));
        } else {
            return is;
        }
    }

}
