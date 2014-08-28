package com.forbesdigital.jee.validation;

/**
 * An error message with a code and an optional location type and location. 
 * The location type and the location indicates what part of the validated request is invalid. 
 * The code identifies the type of error (meant to be used for internationalization purposes).
 *
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
public interface IError {

	/**
	 * @return the error message.
	 */
	String getMessage();

	/**
	 * @return the error code.
	 */
	IErrorCode getCode();

	/**
	 * @return the error location type.
	 */
	IErrorLocationType getLocationType();

	/**
	 * @return the error location.
	 */
	String getLocation();
}
