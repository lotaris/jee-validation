package com.forbesdigital.jee.validation;

/**
 * A code identifying a specific type of validation error (such as an invalid string length).
 *
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
public interface IErrorCode {

	/**
	 * Returns the code identifying the error.
	 *
	 * @return an integer code
	 */
	int getCode();
	
	// TODO: Comment
	int getDefaultHttpStatusCode();
}
