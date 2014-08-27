package com.forbesdigital.jee.validation;

import java.lang.annotation.Annotation;

/**
 * Defines a constraint converter.
 *
 * @author Laurent Prevost <laurent.prevost@lotaris.com>
 * @author Cristian Calugar <cristian.calugar@fortech.ro>
 */
public interface IConstraintConverter {
	
	IErrorCode getErrorCode(Class<? extends Annotation> annotationType);
	
	IErrorLocationType getErrorLocationType(Class<? extends Annotation> annotationType);
}
