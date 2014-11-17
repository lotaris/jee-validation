package com.lotaris.jee.validation;

import com.lotaris.jee.validation.IErrorCode;
import com.lotaris.jee.validation.IError;
import com.lotaris.jee.validation.IErrorLocationType;
import com.lotaris.jee.validation.ApiErrorResponse;
import com.lotaris.jee.validation.ApiError;
import com.lotaris.rox.annotations.RoxableTest;
import com.lotaris.rox.annotations.RoxableTestClass;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * @see ApiErrorResponse
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
@RoxableTestClass(tags = {"api", "apiErrorResponse"})
public class ApiErrorResponseUnitTest {

	@Test
	@RoxableTest(key = "f243618b7701")
	public void apiErrorResponseShouldConstructEmptyResponsesWithTheSpecifiedStatusCode() {
		final ApiErrorResponse res = new ApiErrorResponse(403);
		assertEquals("API error response should be empty by default", res.getErrors().size(), 0);
		assertEquals(403, res.getHttpStatusCode());
	}

	@Test
	@RoxableTest(key = "4661857863de")
	public void apiErrorResponseShouldNotAcceptSuccessfulStatusCodes() {
		try {
			new ApiErrorResponse(200);
			fail("Expected an exception to be thrown with status code 200 (not in the 4xx or 5xx range); nothing was thrown");
		} catch (IllegalArgumentException iae) {
			assertThat(iae.getMessage(), equalTo("HTTP status code for an API error response must be in the 4xx or 5xx range, got 200"));
		}
	}

	@Test
	@RoxableTest(key = "2f5009e03daa")
	public void apiErrorResponseShouldAddErrorsToItsErrorList() {

		final ApiErrorResponse res = badRequest();

		ApiError error = new ApiError("1", code(1));
		res.addError(error);
		assertThat(res.getErrors().size(), equalTo(1));
		assertThat(res.getErrors(), hasItem(isAnApiErrorWith("1", null, null, 1)));

		error = new ApiError("2", code(2));
		res.addError(error);
		assertThat(res.getErrors().size(), equalTo(2));
		assertThat(res.getErrors(), hasItem(isAnApiErrorWith("1", null, null, 1)));
		assertThat(res.getErrors(), hasItem(isAnApiErrorWith("2", null, null, 2)));
	}

	@Test
	@RoxableTest(key = "705523ce682d")
	public void apiErrorResponseShouldCheckWhetherItHasErrors() {

		final ApiErrorResponse res = badRequest();
		assertFalse(res.hasErrors());

		res.addError(new ApiError("1", code(1)));
		assertTrue(res.hasErrors());
	}

	@Test
	@RoxableTest(key = "7395802ef85f")
	public void apiErrorResponseShouldCheckWhetherItHasErrorsByLocation() {

		final ApiErrorResponse res = badRequest();
		res.addError(new ApiError("1", code(1)))
				.addError(new ApiError("2", code(2), locationType("locationType"), "/bar"));
		assertFalse(res.hasErrors("/foo"));

		res.addError(new ApiError("3", code(3), locationType("locationType"), "/foo"));
		assertTrue(res.hasErrors("/foo"));
	}

	@Test
	@RoxableTest(key = "e64c05d6bdb3")
	public void apiErrorResponseShouldCheckWhetherItHasErrorsWithNoLocation() {

		final ApiErrorResponse res = badRequest();
		res.addError(new ApiError("1", code(1), locationType("locationType"), "/1"));
		assertFalse(res.hasErrors((String) null));

		res.addError(new ApiError("2", code(2)));
		assertTrue(res.hasErrors((String) null));
	}

	@Test
	@RoxableTest(key = "4d73535fa505")
	public void apiErrorResponseShouldCheckWhetherItHasErrorsByLocationIncludingSubLocations() {

		final ApiErrorResponse res = badRequest();
		res.addError(new ApiError("foo", code(1), locationType("locationType"), "/person/name"))
				.addError(new ApiError("bar", code(2), locationType("locationType"), "/person/children/0/name"));

		assertTrue(res.hasErrors("/person"));
		assertTrue(res.hasErrors("/person/name"));
		assertTrue(res.hasErrors("/person/children"));
		assertTrue(res.hasErrors("/person/children/0"));
		assertTrue(res.hasErrors("/person/children/0/name"));
	}

	@Test
	@RoxableTest(key = "bf6669b33829")
	public void apiErrorResponseShouldNotIncludePartialMatchesWhenCheckingWhetherItHasErrorsByLocation() {

		final ApiErrorResponse res = badRequest();
		res.addError(new ApiError("foo", code(1), locationType("locationType"), "/person/name"))
				.addError(new ApiError("bar", code(2), locationType("locationType"), "/person/children/0/name"));

		assertFalse(res.hasErrors("/pers"));
		assertFalse(res.hasErrors("/person/child"));
		assertFalse(res.hasErrors("/person/children/1"));
		assertFalse(res.hasErrors("/person/children/0/nam"));
	}

	@Test
	@RoxableTest(key = "b811721b482b")
	public void apiErrorResponseShouldCheckWhetherItHasErrorsByCode() {

		final ApiErrorResponse res = badRequest();
		res.addError(new ApiError("1", null))
				.addError(new ApiError("2", code(2)))
				.addError(new ApiError("3", code(3), locationType("locationType"), "/3"));
		assertFalse(res.hasErrors(code(4)));

		res.addError(new ApiError("4", code(4)));
		assertTrue(res.hasErrors(code(4)));
	}

	@Test
	@RoxableTest(key = "2d996209563e")
	public void apiErrorResponseShouldCheckWhetherItHasErrorsWithNoCode() {

		final ApiErrorResponse res = badRequest();
		res.addError(new ApiError("1", code(1)));
		assertFalse(res.hasErrors((IErrorCode) null));

		res.addError(new ApiError("2", null));
		assertTrue(res.hasErrors((IErrorCode) null));
	}

	private static BaseMatcher<ApiError> isAnApiErrorWith(final String message, final String location, final String locationType, final Integer code) {
		return new BaseMatcher<ApiError>() {
			@Override
			public boolean matches(Object item) {

				if (!(item instanceof ApiError)) {
					return false;
				}

				final IError error = (ApiError) item;

				return message.equals(error.getMessage())
						&& (location != null ? location.equals(error.getLocation()) : error.getLocation() == null)
						&& (locationType != null ? locationType.equals(error.getLocationType().getLocationType()) : error.getLocationType() == null)
						&& (code != null ? error.getCode() != null && code.equals(error.getCode().getCode()) : error.getCode() == null);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText(IError.class.getSimpleName() + " with message \"" + message + "\""
						+ " and location \"" + location + "\""
						+ " and locationType \"" + locationType + "\""
						+ " and code " + code);
			}
		};
	}

	private static ApiErrorResponse badRequest() {
		return new ApiErrorResponse(400);
	}

	private static IErrorCode code(final int code) {
		return new IErrorCode() {

			@Override
			public int getCode() {
				return code;
			}

			@Override
			public int getDefaultHttpStatusCode() {
				return 422;
			}
		};
	}

	private static IErrorLocationType locationType(final String locationType) {
		return new IErrorLocationType() {

			@Override
			public String getLocationType() {
				return locationType;
			}

		};
	}

}
