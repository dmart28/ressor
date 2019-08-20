package xyz.ressor.ext;

import net.bytebuddy.dynamic.DynamicType;

public interface ServiceExtension {

    <T> DynamicType.Builder<?> interceptProxy(DynamicType.Builder<?> builder, Class<? extends T> type);

}
