package com.forbesdigital.jee.test.matchers;

import com.forbesdigital.jee.validation.ApiErrorsException;
import java.util.regex.Pattern;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * Ensures that an API errors exception contains an API error response with the expected HTTP status
 * code and errors.
 *
 * @author Simon Oulevay <simon.oulevay@lotaris.com>
 */
public class ApiErrorsExceptionMatcher extends BaseMatcher<ApiErrorsException> {

	//<editor-fold defaultstate="collapsed" desc="Static Imports">
	public static ApiErrorsExceptionMatcher isApiErrorsException(int expectedHttpStatusCode) {
		return new ApiErrorsExceptionMatcher(expectedHttpStatusCode);
	}
	//</editor-fold>
	private boolean hasErrorResponse;
	private ApiErrorResponseObjectMatcher errorResponseMatcher;

	public ApiErrorsExceptionMatcher(int expectedHttpStatusCode) {
		errorResponseMatcher = new ApiErrorResponseObjectMatcher(expectedHttpStatusCode);
	}

	public ApiErrorsExceptionMatcher withError(int code) {
		errorResponseMatcher.withError(code, null, (String) null);
		return this;
	}

	public ApiErrorsExceptionMatcher withError(int code, String location) {
		errorResponseMatcher.withError(code, location, (String) null);
		return this;
	}

	public ApiErrorsExceptionMatcher withError(int code, String location, String message) {
		errorResponseMatcher.withError(code, location, message);
		return this;
	}

	public ApiErrorsExceptionMatcher withError(int code, String location, Pattern messagePattern) {
		errorResponseMatcher.withError(code, location, messagePattern);
		return this;
	}

	@Override
	public boolean matches(Object item) {

		hasErrorResponse = true;

		if (item == null) {
			return false;
		}

		final ApiErrorsException exception = (ApiErrorsException) item;
		if (exception.getErrorResponse() == null) {
			hasErrorResponse = false;
			return false;
		}

		return errorResponseMatcher.matches(exception.getErrorResponse());
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("API errors exception with ");
		errorResponseMatcher.describeTo(description);
	}

	@Override
	public void describeMismatch(Object item, Description description) {
		if (!hasErrorResponse) {
			description.appendText("exception has no error response");
			return;
		}
		errorResponseMatcher.describeMismatch(((ApiErrorsException) item).getErrorResponse(), description);
	}
}
