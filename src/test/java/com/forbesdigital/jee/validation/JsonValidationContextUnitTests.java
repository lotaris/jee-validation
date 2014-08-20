package com.forbesdigital.jee.validation;

import com.lotaris.rox.annotations.RoxableTest;
import com.lotaris.rox.annotations.RoxableTestClass;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @see JsonValidationContext
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
@RoxableTestClass(tags = {"validation", "jsonValidationContext"})
public class JsonValidationContextUnitTests {

	private static final Random RANDOM = new Random();
	@Mock
	private IErrorCollector collector;
	@InjectMocks
	private JsonValidationContext context;
	private Integer lastCode;

	@Before
	public void setUp() {

		lastCode = null;
		MockitoAnnotations.initMocks(this);

		doReturn(collector).when(collector).addError(any(IError.class));
	}

	@Test
	@RoxableTest(key = "172a761588ae")
	public void validationContextShouldForwardAddErrorsToItsErrorCollector() {

		assertSame(context, context.addError("/foo", code(), "foo"));
		verify(collector).addError(argThat(isAnErrorWith("foo", "/foo", lastCode)));

		assertSame(context, context.addError("/bar", code(), "bar"));
		verify(collector).addError(argThat(isAnErrorWith("bar", "/bar", lastCode)));
	}

	@Test
	@RoxableTest(key = "081129fccdcd")
	public void validationContextShouldForwardAddErrorsAtTheCurrentLocationToItsErrorCollector() {

		assertSame(context, context.addErrorAtCurrentLocation(code(), "foo"));
		verify(collector).addError(argThat(isAnErrorWith("foo", "", lastCode)));
	}

	@Test
	@RoxableTest(key = "32dea4c8942d")
	public void validationContextShouldFormatErrorMessagesWithArguments() {

		assertSame(context, context.addError("/foo", code(), "This message has arguments %s, %s and %s.", "a", "b", "c"));
		verify(collector).addError(argThat(isAnErrorWith("This message has arguments a, b and c.", "/foo", lastCode)));
	}

	@Test
	@RoxableTest(key = "e679dc0b2128")
	public void validationContextShouldFormatErrorMessagesWithArgumentsAtCurrentLocation() {

		assertSame(context, context.addErrorAtCurrentLocation(code(), "This message has arguments %s, %s and %s.", "a", "b", "c"));
		verify(collector).addError(argThat(isAnErrorWith("This message has arguments a, b and c.", "", lastCode)));
	}

	@Test
	@RoxableTest(key = "26ba7f038af7")
	public void validationContextShouldForwardHasErrorsToItsErrorCollector() {

		when(collector.hasErrors()).thenReturn(true);
		assertTrue(context.hasErrors());

		when(collector.hasErrors()).thenReturn(false);
		assertFalse(context.hasErrors());

		verify(collector, times(2)).hasErrors();
	}

	@Test
	@RoxableTest(key = "d78aa68b4eca")
	public void validationContextShouldForwardHasErrorsWithLocationToItsErrorCollector() {

		when(collector.hasErrors("/foo")).thenReturn(true);
		assertTrue(context.hasErrors("/foo"));

		when(collector.hasErrors("/foo")).thenReturn(false);
		assertFalse(context.hasErrors("/foo"));

		verify(collector, times(2)).hasErrors("/foo");
	}

	@Test
	@RoxableTest(key = "53833e3a0fc4")
	public void validationContextShouldForwardHasErrorsWithCodeToItsErrorCollector() {

		when(collector.hasErrors(argThat(isCode(42)))).thenReturn(true);
		assertTrue(context.hasErrors(code(42)));

		when(collector.hasErrors(argThat(isCode(42)))).thenReturn(false);
		assertFalse(context.hasErrors(code(42)));

		verify(collector, times(2)).hasErrors(argThat(isCode(42)));
	}

	@Test
	@RoxableTest(key = "07979806fe1e")
	public void validationContextShouldBuildNoLocation() {
		assertNull(context.location(null));
	}

	@Test
	@RoxableTest(key = "5a67b405c7c1")
	public void validationContextShouldBuildTheCurrentLocationString() {
		assertEquals("", context.location(""));
	}

	@Test
	@RoxableTest(key = "6436993f1bcc")
	public void validationContextShouldBuildRelativeLocationStrings() {
		assertEquals("/foo", context.location("/foo"));
		assertEquals("/foo/bar", context.location("/foo/bar"));
		assertEquals("/bar", context.location("/bar"));
	}

	@Test
	@RoxableTest(key = "1d650d095174")
	public void validationContextShouldValidateNestedObjects() {

		final IValidator<String> validator = new IValidator<String>() {
			@Override
			public void collectErrors(String object, IValidationContext context) {

				assertEquals("yeehaw", object);
				assertEquals("/foo", context.location(""));
				assertEquals("/foo/bar", context.location("/bar"));

				context.addError("/bar", code(24), "broken");
				context.addError("/bar/baz", code(42), "also broken");
			}
		};

		context.validateObject("yeehaw", "/foo", validator);

		final InOrder inOrder = inOrder(collector);
		inOrder.verify(collector).addError(argThat(isAnErrorWith("broken", "/foo/bar", 24)));
		inOrder.verify(collector).addError(argThat(isAnErrorWith("also broken", "/foo/bar/baz", 42)));
	}

	@Test
	@RoxableTest(key = "436cb220727e")
	public void validationContextShouldValidateNestedObjectLists() {

		final IValidator<String> validator = new IValidator<String>() {
			@Override
			public void collectErrors(String object, IValidationContext context) {

				assertThat(object, matches("^bar\\d$"));
				assertThat(context.location(""), matches("^\\/foo\\/\\d$"));
				assertThat(context.location("/sub"), matches("^\\/foo\\/\\d\\/sub$"));

				context.addError("/name", code(22), "broken");
			}
		};

		final List<String> strings = Arrays.asList("bar1", "bar2", "bar3");
		context.validateObjects(strings, "foo", validator);

		final InOrder inOrder = inOrder(collector);
		inOrder.verify(collector).addError(argThat(isAnErrorWith("broken", "/foo/0/name", 22)));
		inOrder.verify(collector).addError(argThat(isAnErrorWith("broken", "/foo/1/name", 22)));
		inOrder.verify(collector).addError(argThat(isAnErrorWith("broken", "/foo/2/name", 22)));
		verify(collector, times(3)).addError(any(IError.class));
	}

	@Test
	@RoxableTest(key = "28234124dc55")
	public void validationContextShouldValidateDeeplyNestedObjects() {

		final IValidator<String> thirdLevelValidator = new IValidator<String>() {
			@Override
			public void collectErrors(String object, IValidationContext context) {

				assertThat(object, matches("^baz\\d$"));
				assertThat(context.location(""), matches("^\\/foo\\/bar\\/baz\\/\\d$"));
				assertThat(context.location("/sub"), matches("^\\/foo\\/bar\\/baz\\/\\d\\/sub$"));

				context.addError("/last", code(22), "definitely broken");
			}
		};

		final IValidator<String> secondLevelValidator = new IValidator<String>() {
			@Override
			public void collectErrors(String object, IValidationContext context) {

				assertEquals("bar", object);
				assertEquals("/foo/bar", context.location(""));
				assertEquals("/foo/bar/sub", context.location("/sub"));

				context.addError("/name", null, "also broken");
				context.addErrorAtCurrentLocation(null, "still broken");

				final List<String> objects = Arrays.asList("baz1", "baz2", "baz3");
				context.validateObjects(objects, "/baz", thirdLevelValidator);
			}
		};

		final IValidator<String> firstLevelValidator = new IValidator<String>() {
			@Override
			public void collectErrors(String object, IValidationContext context) {

				assertEquals("foo", object);
				assertEquals("/foo", context.location(""));
				assertEquals("/foo/sub", context.location("/sub"));

				context.addError(null, code(66), "broken");
				context.validateObject("bar", "/bar", secondLevelValidator);
			}
		};

		context.addError("", null, "fubar");
		context.validateObject("foo", "/foo", firstLevelValidator);

		final InOrder inOrder = inOrder(collector);
		inOrder.verify(collector).addError(argThat(isAnErrorWith("fubar", "", null)));
		inOrder.verify(collector).addError(argThat(isAnErrorWith("broken", null, 66)));
		inOrder.verify(collector).addError(argThat(isAnErrorWith("also broken", "/foo/bar/name", null)));
		inOrder.verify(collector).addError(argThat(isAnErrorWith("still broken", "/foo/bar", null)));
		inOrder.verify(collector).addError(argThat(isAnErrorWith("definitely broken", "/foo/bar/baz/0/last", 22)));
		inOrder.verify(collector).addError(argThat(isAnErrorWith("definitely broken", "/foo/bar/baz/1/last", 22)));
		inOrder.verify(collector).addError(argThat(isAnErrorWith("definitely broken", "/foo/bar/baz/2/last", 22)));
		verify(collector, times(7)).addError(any(IError.class));
	}

	@Test
	@RoxableTest(key = "9f6980e20b74")
	public void validationContextShouldValidateCurrentObjectWithAnotherValidator() {

		final IValidator<String> validator = new IValidator<String>() {
			@Override
			public void collectErrors(String object, IValidationContext context) {

				assertEquals("foo", object);
				assertEquals("", context.location(""));
				assertEquals("/sub", context.location("/sub"));

				context.addError("/name", code(66), "broken");
			}
		};

		context.validateObject("foo", null, validator);
		verify(collector).addError(argThat(isAnErrorWith("broken", "/name", 66)));
	}

	@Test
	@RoxableTest(key = "95e61bc5b4b7")
	public void validationContextShouldValidateCurrentObjectListWithAnotherValidator() {

		final IValidator<String> validator = new IValidator<String>() {
			@Override
			public void collectErrors(String object, IValidationContext context) {

				assertThat(object, matches("^foo\\d$"));
				assertThat(context.location(""), matches("^\\/\\d$"));
				assertThat(context.location("/sub"), matches("^\\/\\d\\/sub$"));

				context.addError("/name", code(66), "broken");
			}
		};

		final List<String> strings = Arrays.asList("foo1", "foo2", "foo3");
		context.validateObjects(strings, null, validator);

		final InOrder inOrder = inOrder(collector);
		inOrder.verify(collector).addError(argThat(isAnErrorWith("broken", "/0/name", 66)));
		inOrder.verify(collector).addError(argThat(isAnErrorWith("broken", "/1/name", 66)));
		inOrder.verify(collector).addError(argThat(isAnErrorWith("broken", "/2/name", 66)));
		verify(collector, times(3)).addError(any(IError.class));
	}

	private IErrorCode code() {
		return code(lastCode = RANDOM.nextInt());
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

	private static BaseMatcher<String> matches(final String pattern) {
		return new BaseMatcher<String>() {
			@Override
			public boolean matches(Object item) {
				return item != null && item.toString().matches(pattern);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("String that matches " + pattern);
			}
		};
	}

	private static BaseMatcher<IErrorCode> isCode(final int code) {
		return new BaseMatcher<IErrorCode>() {
			@Override
			public boolean matches(Object item) {
				return (item instanceof IErrorCode) && ((IErrorCode) item).getCode() == code;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("validation error code with value " + code);
			}
		};
	}

	private static BaseMatcher<IError> isAnErrorWith(final String message, final String location, final Integer code) {
		return new BaseMatcher<IError>() {
			@Override
			public boolean matches(Object item) {

				if (!(item instanceof IError)) {
					return false;
				}

				final IError error = (IError) item;

				return message.equals(error.getMessage())
						&& (location != null ? location.equals(error.getLocation()) : error.getLocation() == null)
						&& (code != null ? error.getCode() != null && code.equals(error.getCode().getCode()) : error.getCode() == null);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText(IError.class.getSimpleName() + " with message \"" + message + "\""
						+ " and location \"" + location + "\""
						+ " and code " + code);
			}
		};
	}
}
