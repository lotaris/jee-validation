package com.forbesdigital.jee.validation;

/**
 * An error message with an optional location and code. The location indicates what part of the
 * validated object is invalid. The code identifies the type of error (meant to be used for
 * internationalization purposes).
 *
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
public interface IError {

	String getMessage();

	String getLocation();

	IErrorCode getCode();
}
