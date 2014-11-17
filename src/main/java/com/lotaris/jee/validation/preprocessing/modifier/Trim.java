package com.lotaris.jee.validation.preprocessing.modifier;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Removes leading and trailing whitespace and collapses whitespace inside the string.
 *
 * <p>Whitespace collapse replaces any sequence of whitespace (spaces, tabs, new lines, etc) with
 * one space. It can be disabled.</p>
 *
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Trim {

	/**
	 * Determines whether to collapse duplicate whitespace inside the string.
	 *
	 * @return true to collapse whitespace, false otherwise
	 */
	boolean collapseWhitespace() default true;
}
