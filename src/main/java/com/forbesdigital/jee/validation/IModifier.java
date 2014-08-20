package com.forbesdigital.jee.validation;

import com.forbesdigital.jee.validation.preprocessing.modifier.Trim;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Modifiers are used when values need to be preprocessed, typically before validation. For example,
 * strings must be trimmed before they are validated. A modifier is the implementation of such an
 * operation.
 *
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 * @see Trim
 */
public interface IModifier<T extends Annotation> {

	/**
	 * Returns the modifier annotation that this modifier implements.
	 *
	 * @return a modifier annotation
	 */
	Class<? extends Annotation> getAnnotationType();

	/**
	 * Modifies the value of the specified field. The full object is given to support use cases
	 * where a modification may depend on the value of other fields.
	 *
	 * @param object the object
	 * @param field the field on which the modification must be applied
	 * @param annotation the annotation that was on the field
	 */
	void process(Object object, Field field, T annotation);
}
