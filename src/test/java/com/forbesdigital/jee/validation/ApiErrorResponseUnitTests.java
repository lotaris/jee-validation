package com.forbesdigital.jee.validation;

import com.lotaris.rox.annotations.RoxableTest;
import com.lotaris.rox.annotations.RoxableTestClass;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * @see ApiErrorResponseTO
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
@RoxableTestClass(tags = {"api", "apiErrorResponse"})
public class ApiErrorResponseUnitTests {

	@Test
	@RoxableTest(key = "f243618b7701")
	public void apiErrorResponseShouldConstructEmptyResponsesWithTheSpecifiedStatusCode() {
		final ApiErrorResponseTO res = new ApiErrorResponseTO(403);
		assertEquals("API error response should be empty by default", res.getErrors().size(), 0);
		assertEquals(403, res.getHttpStatusCode());
	}

	@Test
	@RoxableTest(key = "4661857863de")
	public void apiErrorResponseShouldNotAcceptSuccessfulStatusCodes() {
		try {
			new ApiErrorResponseTO(200);
			fail("Expected an exception to be thrown with status code 200 (not in the 4xx or 5xx range); nothing was thrown");
		} catch (IllegalArgumentException iae) {
			assertThat(iae.getMessage(), equalTo("HTTP status code for an API error response must be in the 4xx or 5xx range, got 200"));
		}
	}

	@Test
	@RoxableTest(key = "2f5009e03daa")
	public void apiErrorResponseShouldAddErrorsToItsErrorList() {

		final ApiErrorResponseTO res = badRequest();

		ApiErrorTO error = new ApiErrorTO("1", null, code(1));
		res.addError(error);
		assertThat(res.getErrors().size(), equalTo(1));
		assertThat(res.getErrors(), hasItem(isAnApiErrorWith("1", null, null, 1)));

		error = new ApiErrorTO("2", null, code(2));
		res.addError(error);
		assertThat(res.getErrors().size(), equalTo(2));
		assertThat(res.getErrors(), hasItem(isAnApiErrorWith("1", null, null, 1)));
		assertThat(res.getErrors(), hasItem(isAnApiErrorWith("2", null, null, 2)));
	}

	@Test
	@RoxableTest(key = "705523ce682d")
	public void apiErrorResponseShouldCheckWhetherItHasErrors() {

		final ApiErrorResponseTO res = badRequest();
		assertFalse(res.hasErrors());

		res.addError(new ApiErrorTO("1", null, code(1)));
		assertTrue(res.hasErrors());
	}

	@Test
	@RoxableTest(key = "7395802ef85f")
	public void apiErrorResponseShouldCheckWhetherItHasErrorsByLocation() {

		final ApiErrorResponseTO res = badRequest();
		res.addError(new ApiErrorTO("1", null, code(1))).addError(new ApiErrorTO("2", "/bar", locationType("locationType"), code(2)));
		assertFalse(res.hasErrors("/foo"));

		res.addError(new ApiErrorTO("3", "/foo", locationType("locationType"), code(3)));
		assertTrue(res.hasErrors("/foo"));
	}

	@Test
	@RoxableTest(key = "e64c05d6bdb3")
	public void apiErrorResponseShouldCheckWhetherItHasErrorsWithNoLocation() {

		final ApiErrorResponseTO res = badRequest();
		res.addError(new ApiErrorTO("1", "/1", locationType("locationType"), code(1)));
		assertFalse(res.hasErrors((String) null));

		res.addError(new ApiErrorTO("2", null, code(2)));
		assertTrue(res.hasErrors((String) null));
	}

	@Test
	@RoxableTest(key = "4d73535fa505")
	public void apiErrorResponseShouldCheckWhetherItHasErrorsByLocationIncludingSubLocations() {

		final ApiErrorResponseTO res = badRequest();
		res.addError(new ApiErrorTO("foo", "/person/name", locationType("locationType"), code(1))).addError(new ApiErrorTO("bar", "/person/children/0/name", locationType("locationType"), code(2)));

		assertTrue(res.hasErrors("/person"));
		assertTrue(res.hasErrors("/person/name"));
		assertTrue(res.hasErrors("/person/children"));
		assertTrue(res.hasErrors("/person/children/0"));
		assertTrue(res.hasErrors("/person/children/0/name"));
	}

	@Test
	@RoxableTest(key = "bf6669b33829")
	public void apiErrorResponseShouldNotIncludePartialMatchesWhenCheckingWhetherItHasErrorsByLocation() {

		final ApiErrorResponseTO res = badRequest();
		res.addError(new ApiErrorTO("foo", "/person/name", locationType("locationType"), code(1))).addError(new ApiErrorTO("bar", "/person/children/0/name", locationType("locationType"), code(2)));

		assertFalse(res.hasErrors("/pers"));
		assertFalse(res.hasErrors("/person/child"));
		assertFalse(res.hasErrors("/person/children/1"));
		assertFalse(res.hasErrors("/person/children/0/nam"));
	}

	@Test
	@RoxableTest(key = "b811721b482b")
	public void apiErrorResponseShouldCheckWhetherItHasErrorsByCode() {

		final ApiErrorResponseTO res = badRequest();
		res.addError(new ApiErrorTO("1", null, null)).addError(new ApiErrorTO("2", null, code(2))).addError(new ApiErrorTO("3", "/3", locationType("locationType"), code(3)));
		assertFalse(res.hasErrors(code(4)));

		res.addError(new ApiErrorTO("4", null, code(4)));
		assertTrue(res.hasErrors(code(4)));
	}

	@Test
	@RoxableTest(key = "2d996209563e")
	public void apiErrorResponseShouldCheckWhetherItHasErrorsWithNoCode() {

		final ApiErrorResponseTO res = badRequest();
		res.addError(new ApiErrorTO("1", null, code(1)));
		assertFalse(res.hasErrors((IErrorCode) null));

		res.addError(new ApiErrorTO("2", null, null));
		assertTrue(res.hasErrors((IErrorCode) null));
	}

	private static BaseMatcher<ApiErrorTO> isAnApiErrorWith(final String message, final String location, final String locationType, final Integer code) {
		return new BaseMatcher<ApiErrorTO>() {
			@Override
			public boolean matches(Object item) {

				if (!(item instanceof ApiErrorTO)) {
					return false;
				}

				final IError error = (ApiErrorTO) item;

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

	private static ApiErrorResponseTO badRequest() {
		return new ApiErrorResponseTO(400);
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
