package com.forbesdigital.jee.validation;

/**
 * Container to collect and keep track of errors.
 *
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
public interface IErrorCollector {

	/**
	 * Adds an error. Check previously added errors with <tt>hasErrors</tt>.
	 *
	 * @param error the error
	 * @return this collector
	 */
	IErrorCollector addError(IError error);

	/**
	 * Indicates whether this collector has errors.
	 *
	 * @return true if at least one error was added to this collector
	 */
	boolean hasErrors();

	/**
	 * Indicates whether this collector has errors at or under the specified location.
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
	 * @param absoluteLocation a JSON Pointer (see http://tools.ietf.org/html/rfc6901)
	 * @return true if at least one error with the specified location was added to this collector
	 */
	boolean hasErrors(String absoluteLocation);

	/**
	 * Indicates whether this collector has errors with the specified code.
	 *
	 * @param code an error code
	 * @return true if at least one error with the specified code was added to this collector
	 */
	boolean hasErrors(IErrorCode code);
}
