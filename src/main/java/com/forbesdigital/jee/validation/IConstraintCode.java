package com.forbesdigital.jee.validation;

import java.lang.annotation.Annotation;

/**
 *
 * @author Laurent Prevost <laurent.prevost@lotaris.com>
 */
public interface IConstraintCode {
	IErrorCode getErrorCode(Class<? extends Annotation> annotationType);
}
