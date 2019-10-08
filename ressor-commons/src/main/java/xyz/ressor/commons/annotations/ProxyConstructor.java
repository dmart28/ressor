package xyz.ressor.commons.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.CONSTRUCTOR })
@Retention(RetentionPolicy.RUNTIME)
/**
 * Used for marking a constructor, which will be called by the Ressor proxy class instance (not the actual data reloads,
 * for that purpose {@link ServiceFactory} is used)
 */
public @interface ProxyConstructor {
}
