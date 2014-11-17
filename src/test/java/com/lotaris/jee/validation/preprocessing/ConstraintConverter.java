package com.lotaris.jee.validation.preprocessing;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConstraintConverter {

	String locationType();
	
	int code();
}
