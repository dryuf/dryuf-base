package net.dryuf.base.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Write counterpart of {@link io.quarkus.resteasy.reactive.jackson.SecureField}: marks the property to be kept if
 * input is null.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface SecureIgnoreNull
{
	boolean value() default true;
}
