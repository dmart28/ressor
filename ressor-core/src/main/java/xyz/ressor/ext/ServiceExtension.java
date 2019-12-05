package xyz.ressor.ext;

import net.bytebuddy.dynamic.DynamicType;

/**
 * Allows you to extend generated service proxy class with custom bytecode. For example, make it implement interface
 * or dynamically add some methods.
 */
public interface ServiceExtension {

    /**
     * Interceptor of the proxy generation.
     *
     * @param builder ByteBuddy immutable builder
     * @param type the provided class of the service
     * @param <T> erasure type of the service
     * @return extended ByteBuddy builder
     */
    <T> DynamicType.Builder<T> interceptProxy(DynamicType.Builder<T> builder, Class<T> type);

}
