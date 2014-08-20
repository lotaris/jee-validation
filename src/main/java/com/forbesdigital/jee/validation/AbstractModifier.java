package com.forbesdigital.jee.validation;

import java.lang.annotation.Annotation;

/**
 * Common functionality for modifiers (see {@link IModifier}).
 *
 * <p>When implementing a modifier, pass the annotation type to the constructor.
 * <p><pre>
 *	public class MyModifier<MyAnnotation> extends AbstractModifier<MyAnnotation> {
 *
 *		public MyModifier() {
 *			super(MyAnnotation.class);
 *		}
 *	}
 * </p></pre>
 *
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
public abstract class AbstractModifier<T extends Annotation> implements IModifier<T> {

	private Class<T> annotationType;

	public AbstractModifier(Class<T> annotationType) {
		this.annotationType = annotationType;
	}

	@Override
	public Class<? extends Annotation> getAnnotationType() {
		return annotationType;
	}
}
