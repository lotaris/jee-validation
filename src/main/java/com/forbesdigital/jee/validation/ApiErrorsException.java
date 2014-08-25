package com.forbesdigital.jee.validation;



/**
 * Thrown to indicate that API-related errors occurred, typically during validation. This exception
 * is automatically mapped to an HTTP 422 Unprocessable Entity API response.
 *
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 * @see ApiErrorsExceptionMapper
 */
public class ApiErrorsException extends Exception {

	private ApiErrorResponse errorResponse;

	public ApiErrorsException(ApiErrorResponse errorResponse) {
		this.errorResponse = errorResponse;
	}

	public ApiErrorResponse getErrorResponse() {
		return errorResponse;
	}
}
