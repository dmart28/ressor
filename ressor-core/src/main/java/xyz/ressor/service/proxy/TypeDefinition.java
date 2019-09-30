package xyz.ressor.service.proxy;

import xyz.ressor.commons.annotations.ProxyConstructor;
import xyz.ressor.commons.exceptions.TypeDefinitionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static java.lang.reflect.Modifier.isPrivate;
import static xyz.ressor.commons.utils.ReflectionUtils.findAnnotatedExecutable;
import static xyz.ressor.commons.utils.RessorUtils.defaultInstance;

public class TypeDefinition<T> {
    private final boolean isFinal;
    private final boolean isInterface;
    private final Constructor<T> defaultConstructor;
    private final Object[] defaultArguments;

    private TypeDefinition(boolean isFinal, boolean isInterface, Constructor<T> defaultConstructor,
                           Object[] defaultArguments) {
        this.isFinal = isFinal;
        this.isInterface = isInterface;
        this.defaultConstructor = defaultConstructor;
        this.defaultArguments = defaultArguments;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public Constructor<T> getDefaultConstructor() {
        return defaultConstructor;
    }

    public Object[] getDefaultArguments() {
        return defaultArguments;
    }

    public static <T> TypeDefinition<T> of(Class<? extends T> type) {
        var isFinal = Modifier.isFinal(type.getModifiers());
        var isInterface = type.isInterface();
        if (!isInterface) {
            Constructor<T>[] constructors = (Constructor<T>[]) type.getDeclaredConstructors();
            Constructor<T> defaultConstructor = findAnnotatedExecutable(constructors, ProxyConstructor.class);

            if (defaultConstructor == null) {
                Arrays.sort(constructors, ConstructorComparator.instance());
                if (constructors.length > 0) {
                    defaultConstructor = constructors[0];
                } else {
                    throw new TypeDefinitionException(type, "No constructors were found for class, unable to generate a proxy");
                }
            }
            if (isPrivate(defaultConstructor.getModifiers())) {
                throw new TypeDefinitionException(type, "All available constructors are private, unable to define a proxy class");
            }
            defaultConstructor.setAccessible(true);
            var defaultArguments = generateDefaultArguments(defaultConstructor);
            return new TypeDefinition<>(isFinal, false, defaultConstructor, defaultArguments);
        } else {
            return new TypeDefinition<>(isFinal, true, null, null);
        }
    }

    private static <T> Object[] generateDefaultArguments(Constructor<T> defaultConstructor) {
        var parameterTypes = defaultConstructor.getParameterTypes();
        var defaultArguments = new Object[defaultConstructor.getParameterCount()];
        for (var i = 0; i < parameterTypes.length; i++) {
            defaultArguments[i] = defaultInstance(parameterTypes[i]);
        }
        return defaultArguments;
    }
}
