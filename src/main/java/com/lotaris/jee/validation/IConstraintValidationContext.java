package com.lotaris.jee.validation;

/**
 * State object to keep track of bean validation errors.
 *
 * <p>You are passed an instance of this interface when implementing the <tt>validate</tt> method of
 * {@link AbstractConstraintValidator} classes.</p>
 *
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 * @see AbstractConstraintValidator
 */
public interface IConstraintValidationContext {

	/**
	 * Adds the default error defined by the constraint annotation.
	 *
	 * @return this context
	 */
	IConstraintValidationContext addDefaultError();

	/**
	 * Adds a custom error to the annotated property.
	 *
	 * <p><pre>
	 *	public void validate(String value, IConstraintValidationContext context) {
	 *		if (isBadValue(value)) {
	 *			context.addErrorAtCurrentLocation("This value is bad.");
	 *		}
	 *	}
	 * </pre></p>
	 *
	 * @param message the error message
	 * @param messageArgs optional arguments to be interpolated into the message (see
	 * {@link String#format(java.lang.String, java.lang.Object[])})
	 * @return this context
	 */
	IConstraintValidationContext addErrorAtCurrentLocation(String message, Object... messageArgs);

	/**
	 * Adds a custom error at the specified location (a property of the annotated object). You can
	 * use this from a class-level validator to add an error message to a property, or from an
	 * object validator to add an error to a sub-object.
	 *
	 * <p><pre>
	 *	public void validate(MyTO object, IConstraintValidationContext context) {
	 *		if (hasBadName(object)) {
	 *			context.addError("name", "Name must not be bad.");
	 *		}
	 *	}
	 * </pre></p>
	 *
	 * @param location the location of the error, either a property name (e.g. "firstName") or a
	 * one-level pointer (e.g. "/lastName")
	 * @param message the error message
	 * @param messageArgs optional arguments to be interpolated into the message (see
	 * {@link String#format(java.lang.String, java.lang.Object[])})
	 * @return this context
	 */
	IConstraintValidationContext addError(String location, String message, Object... messageArgs);
	
	/**
	 * Adds a custom error at the specified array location (a property of the annotated object). You can
	 * use this from a class-level validator to add an error message to a value in an array property, or from an
	 * object validator to add an error to a sub-object.
	 *
	 * <p><pre>
	 *	public void validate(MyTO object, IConstraintValidationContext context) {
	 *		if (hasBadName(object)) {
	 *			context.addArrayError("myArray", 0, "Value at index 0 is wrong");
	 *		}
	 *	}
	 * </pre></p>
	 *
	 * @param location the location of the error, either a property name (e.g. "firstName") or a
	 * one-level pointer (e.g. "/lastName")
	 * @param index index of the error in the array
	 * @param message the error message
	 * @param messageArgs optional arguments to be interpolated into the message (see
	 * {@link String#format(java.lang.String, java.lang.Object[])})
	 * @return this context
	 */
	IConstraintValidationContext addArrayError(String location, int index, String message, Object... messageArgs);

	/**
	 * Indicates whether this context has errors.
	 *
	 * @return true if at least one error was added to this context
	 */
	boolean hasErrors();
}
