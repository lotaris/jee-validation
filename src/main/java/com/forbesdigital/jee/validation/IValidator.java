package com.forbesdigital.jee.validation;

/**
 * A validation to be applied to an object. Errors are not returned directly but collected into the
 * supplied validation context. The context also allows you to check for previously added errors.
 *
 * <p>Subclass {@link AbstractValidator} when creating validators to have access to useful
 * validation utilities.</p>
 *
 * @param <T> the type of object to validate
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
public interface IValidator<T> {

	/**
	 * Validates the specified object.
	 *
	 * @param object the object to validate
	 * @param context the context used to add and keep track of errors during validation
	 */
	void collectErrors(T object, IValidationContext context);
}
