package xyz.ressor.commons.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used for marking constructors or factory methods of a service class. It will be used by Ressor for new instance
 * creating during application runtime with the data loaded from {@link javax.xml.transform.Source}.
 */
@Target({ ElementType.CONSTRUCTOR, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceFactory {
}