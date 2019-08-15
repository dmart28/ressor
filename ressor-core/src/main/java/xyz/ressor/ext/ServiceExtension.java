package xyz.ressor.ext;

import net.bytebuddy.dynamic.DynamicType;
import xyz.ressor.service.RessorService;

public interface ServiceExtension {

    DynamicType.Builder<? extends RessorService<?>> interceptProxy(DynamicType.Builder<? extends RessorService<?>> builder, Class<?> type);

}
