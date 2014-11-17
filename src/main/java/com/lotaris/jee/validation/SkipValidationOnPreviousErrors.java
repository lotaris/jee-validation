package com.lotaris.jee.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Apply this annotation to an implementation of {@link AbstractApiValidator} to skip validation
 * when there are previous errors at given locations. See the class definition of
 * {@link AbstractValidator}.
 *
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SkipValidationOnPreviousErrors {

	/**
	 * Locations that will cause validation to be skipped if they have any previous errors.
	 *
	 * @return locations to check for previous errors
	 */
	String[] locations();
}
