package com.forbesdigital.jee.validation;

import java.util.List;

/**
 * State object to keep track of validation errors.
 *
 * <p>The context is mainly used to add errors with <tt>addError</tt> and keep track of previously
 * added errors with <tt>hasErrors</tt>.</p>
 *
 * <p>It also maintains a current location that is useful to validate nested structures and generate
 * appropriate error locations (e.g. "/person/name", "/person/address/street",
 * "/person/children/0/name", etc). See the <tt>validateObject(s)</tt> methods.</p>
 *
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
public interface IValidationContext {

	/**
	 * Adds an error. Check previously added errors with <tt>hasErrors</tt>.
	 *
	 * <p>Note that the error location you supply is considered to be relative to the current
	 * location. For example, if the current location is "/sub" and you add an error on "/foo", the
	 * final location in that error will be "/sub/foo". The current location changes when you call
	 * other validators with the <tt>validateObject(s)</tt> methods.</p>
	 *
	 * <p>To add an error at the current location, pass the empty string as the location. To add an
	 * error with no location, pass null as the location.</p>
	 *
	 * @param location a JSON Pointer (see http://tools.ietf.org/html/rfc6901) indicating which
	 * value of the JSON document is invalid (relative to the current location)
	 * @param code the code identifying the error type (e.g. invalid string length)
	 * @param message the error message
	 * @param messageArgs optional arguments to be interpolated into the message (see
	 * {@link String#format(java.lang.String, java.lang.Object[])})
	 * @return this context
	 */
	IValidationContext addError(String location, IErrorCode code, String message, Object... messageArgs);

	/**
	 * Adds an error at the current location. Check previously added errors with <tt>hasErrors</tt>.
	 *
	 * <p>The current location changes when you call other validators with the
	 * <tt>validateObject(s)</tt> methods.</p>
	 *
	 * @param code the code identifying the error type (e.g. invalid string length)
	 * @param message the error message
	 * @param messageArgs optional arguments to be interpolated into the message (see
	 * {@link String#format(java.lang.String, java.lang.Object[])})
	 * @return this context
	 */
	IValidationContext addErrorAtCurrentLocation(IErrorCode code, String message, Object... messageArgs);

	/**
	 * Indicates whether this context has errors.
	 *
	 * @return true if at least one error was added to this context
	 */
	boolean hasErrors();

	/**
	 * Indicates whether this context has errors at or under the specified location.
	 *
	 * <p>Note that sub-locations will match. For example, if an error was added at
	 * <tt>/person/children/0/name</tt>, the following locations will be considered to have
	 * errors:</p>
	 * <ul>
	 * <li>/person</li>
	 * <li>/person/children</li>
	 * <li>/person/children/0</li>
	 * <li>/person/children/0/name</li>
	 * </ul>
	 *
	 * <p>Also note that the error location must be absolute (the full location of the error
	 * starting from the root of the JSON document). To obtain an absolute location when validating
	 * inside a nested structure with <tt>validateObject(s)</tt> methods, use the <tt>location</tt>
	 * method.</p>
	 *
	 * <p>Calling this will null returns true if at least one error with no location was added to
	 * this context.</p>
	 *
	 * @param absoluteLocation a JSON Pointer (see http://tools.ietf.org/html/rfc6901)
	 * @return true if at least one error with the specified location was added to this context
	 */
	boolean hasErrors(String absoluteLocation);

	/**
	 * Indicates whether this context has errors with the specified code.
	 *
	 * @param code an error code
	 * @return true if at least one error with the specified code was added to this context
	 */
	boolean hasErrors(IErrorCode code);

	/**
	 * Returns the absolute location of the specified path, relative to the current location.
	 * Calling this with the empty string returns the current (absolute) location. Calling with null
	 * returns null (no location).
	 *
	 * @param path a JSON Pointer (see {@link http://tools.ietf.org/html/rfc6901}) string relative
	 * to the current location
	 * @return the absolute location string for the specified path, or null if given null
	 */
	String location(String path);

	/**
	 * Validates another object in this context, such as a property or sub-object in a nested
	 * structure. You must specify the location of the object to validate relative to the current
	 * location.
	 *
	 * <p><pre>
	 * // validate a "name" property
	 * context.validateObject(parentObject.getName(), "/name", nameValidator);
	 *
	 * // validate an "address" sub-object
	 * context.validateObject(parentObject.getAddress(), "/address", addressValidator);
	 *
	 * // validate the "age" property of a "father" sub-object
	 * context.validateObject(parentObject.getFather().getAge(), "/father/age", ageValidator);
	 * </pre></p>
	 *
	 * @param <T> the type of object to validate
	 * @param object the object to validate
	 * @param relativeLocation the location of the object relative to the current location
	 * @param context the context used to add and keep track of errors during validation
	 * @return this context
	 */
	<T> IValidationContext validateObject(T object, String relativeLocation, IValidator<T> validator);

	/**
	 * Validates a list of objects in this context, such as a list of sub-objects in a nested
	 * structure. You must specify the location of the list relative to the current location. When
	 * the sub-object validator is called, the current location will be the relative location you
	 * have specified plus the index of the sub-object in the list (e.g. "/children/2").
	 *
	 * <p><pre>
	 * // validate a "children" list of sub-objects
	 * // when childValidator is called, the current location will be "/children/0", "/children/1", etc.
	 * context.validateObjects(parentObject.getChildren(), "/children", childValidator);
	 * </pre></p>
	 *
	 * @param <T> the type of object in the list
	 * @param objects the list of objects to validate
	 * @param relativeLocation the location of the list relative to the current location
	 * @param context the context used to add and keep track of errors during validation
	 * @return this context
	 */
	<T> IValidationContext validateObjects(List<T> objects, String relativeLocation, IValidator<T> validator);

	/**
	 * Validates a single object or a list of objects in this context. Delegates to
	 * <tt>validateObject</tt> if {@link SingleObjectOrList#isSingleObject()} returns true, or to
	 * <tt>validateObjects</tt>.
	 *
	 * @param <T> the type of object to validate
	 * @param singleObjectOrList the single object or list
	 * @param relativeLocation the location of the object relative to the current location
	 * @param context the context used to add and keep track of errors during validation
	 * @return this context
	 */
	<T> IValidationContext validateObjectOrList(SingleObjectOrList<T> singleObjectOrList, String relativeLocation, IValidator<T> validator);
}
