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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

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
	private String lastLocationType;

	@Before
	public void setUp() {

		lastCode = null;
		MockitoAnnotations.initMocks(this);

		doReturn(collector).when(collector).addError(any(IError.class));
	}

	@Test
	@RoxableTest(key = "172a761588ae")
	public void validationContextShouldForwardAddErrorsToItsErrorCollector() {

		assertSame(context, context.addError("/foo", locationType(), code(), "foo"));
		verify(collector).addError(argThat(isAnErrorWith("foo", "/foo", lastLocationType, lastCode)));

		assertSame(context, context.addError("/bar", locationType(), code(), "bar"));
		verify(collector).addError(argThat(isAnErrorWith("bar", "/bar", lastLocationType, lastCode)));
	}

	@Test
	@RoxableTest(key = "081129fccdcd")
	public void validationContextShouldForwardAddErrorsAtTheCurrentLocationToItsErrorCollector() {

		assertSame(context, context.addErrorAtCurrentLocation(code(), "foo"));
		verify(collector).addError(argThat(isAnErrorWith("foo", "", "json", lastCode)));
	}

	@Test
	@RoxableTest(key = "32dea4c8942d")
	public void validationContextShouldFormatErrorMessagesWithArguments() {

		assertSame(context, context.addError("/foo", locationType(), code(), "This message has arguments %s, %s and %s.", "a", "b", "c"));
		verify(collector).addError(argThat(isAnErrorWith("This message has arguments a, b and c.", "/foo", lastLocationType, lastCode)));
	}

	@Test
	@RoxableTest(key = "e679dc0b2128")
	public void validationContextShouldFormatErrorMessagesWithArgumentsAtCurrentLocation() {

		assertSame(context, context.addErrorAtCurrentLocation(code(), "This message has arguments %s, %s and %s.", "a", "b", "c"));
		verify(collector).addError(argThat(isAnErrorWith("This message has arguments a, b and c.", "", "json", lastCode)));
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

				context.addError("/bar", locationType("locationType1"), code(24), "broken");
				context.addError("/bar/baz", locationType("locationType2"), code(42), "also broken");
			}
		};

		context.validateObject("yeehaw", "/foo", validator);

		final InOrder inOrder = inOrder(collector);
		inOrder.verify(collector).addError(argThat(isAnErrorWith("broken", "/foo/bar", "locationType1", 24)));
		inOrder.verify(collector).addError(argThat(isAnErrorWith("also broken", "/foo/bar/baz", "locationType2", 42)));
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

				context.addError("/name", locationType("locationType1"), code(22), "broken");
			}
		};

		final List<String> strings = Arrays.asList("bar1", "bar2", "bar3");
		context.validateObjects(strings, "foo", validator);

		final InOrder inOrder = inOrder(collector);
		inOrder.verify(collector).addError(argThat(isAnErrorWith("broken", "/foo/0/name", "locationType1", 22)));
		inOrder.verify(collector).addError(argThat(isAnErrorWith("broken", "/foo/1/name", "locationType1", 22)));
		inOrder.verify(collector).addError(argThat(isAnErrorWith("broken", "/foo/2/name", "locationType1", 22)));
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

				context.addError("/last", locationType("locationType3"), code(22), "definitely broken");
			}
		};

		final IValidator<String> secondLevelValidator = new IValidator<String>() {
			@Override
			public void collectErrors(String object, IValidationContext context) {

				assertEquals("bar", object);
				assertEquals("/foo/bar", context.location(""));
				assertEquals("/foo/bar/sub", context.location("/sub"));

				context.addError("/name", locationType("locationType1"), null, "also broken");
				context.addErrorAtCurrentLocation( null, "still broken");

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

				context.addError(null, null, code(66), "broken");
				context.validateObject("bar", "/bar", secondLevelValidator);
			}
		};

		context.addError("", locationType("locationType0"), null, "fubar");
		context.validateObject("foo", "/foo", firstLevelValidator);

		final InOrder inOrder = inOrder(collector);
		inOrder.verify(collector).addError(argThat(isAnErrorWith("fubar", "", "locationType0",null)));
		inOrder.verify(collector).addError(argThat(isAnErrorWith("broken", null, null, 66)));
		inOrder.verify(collector).addError(argThat(isAnErrorWith("also broken", "/foo/bar/name", "locationType1", null)));
		inOrder.verify(collector).addError(argThat(isAnErrorWith("still broken", "/foo/bar", "json", null)));
		inOrder.verify(collector).addError(argThat(isAnErrorWith("definitely broken", "/foo/bar/baz/0/last", "locationType3", 22)));
		inOrder.verify(collector).addError(argThat(isAnErrorWith("definitely broken", "/foo/bar/baz/1/last", "locationType3", 22)));
		inOrder.verify(collector).addError(argThat(isAnErrorWith("definitely broken", "/foo/bar/baz/2/last", "locationType3", 22)));
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

				context.addError("/name", locationType("locationType1"), code(66), "broken");
			}
		};

		context.validateObject("foo", null, validator);
		verify(collector).addError(argThat(isAnErrorWith("broken", "/name", "locationType1", 66)));
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

				context.addError("/name", locationType("locationType1"), code(66), "broken");
			}
		};

		final List<String> strings = Arrays.asList("foo1", "foo2", "foo3");
		context.validateObjects(strings, null, validator);

		final InOrder inOrder = inOrder(collector);
		inOrder.verify(collector).addError(argThat(isAnErrorWith("broken", "/0/name", "locationType1", 66)));
		inOrder.verify(collector).addError(argThat(isAnErrorWith("broken", "/1/name", "locationType1", 66)));
		inOrder.verify(collector).addError(argThat(isAnErrorWith("broken", "/2/name", "locationType1", 66)));
		verify(collector, times(3)).addError(any(IError.class));
	}

	@Test
	@RoxableTest(key = "956febe33898")
	public void apiPreprocessingContextShouldRegisterOneStateObjectByClass() {

		final Object state = new Object();

		final IValidationContext result = context.addState(state, Object.class);
		assertSame(context, result);
		assertSame(state, context.getState(Object.class));
	}

	@Test
	@RoxableTest(key = "8162ed31c1c8")
	public void apiPreprocessingContextShouldRegisterOneSubclassedStateObjectByClass() {

		final ArrayList state = new ArrayList();

		final IValidationContext result = context.addState(state, Collection.class);
		assertSame(context, result);
		assertSame(state, context.getState(ArrayList.class));
	}

	@Test
	@RoxableTest(key = "5d12f39e8861")
	public void apiPreprocessingContextShouldRegisterManyStateObjects() {

		final ArrayList state1 = new ArrayList();
		final HashMap state2 = new HashMap();

		final IValidationContext result = context.addStates(state1, state2);
		assertSame(context, result);
		assertSame(state1, context.getState(ArrayList.class));
		assertSame(state2, context.getState(HashMap.class));
	}

	@Test
	@RoxableTest(key = "14c0109cd96e")
	public void apiPreprocessingContextShouldThrowIllegalArgumentExceptionForUnregisteredStates() {
		try {
			context.getState(Object.class);
			fail("Expected IllegalArgumentException to be thrown when getting an unregistered state");
		} catch (IllegalArgumentException iae) {
			assertEquals("No state object registered for class " + Object.class.getName(), iae.getMessage());
		}
	}

	@Test
	@RoxableTest(key = "a07e9f77defd")
	public void apiPreprocessingContextShouldThrowIllegalArgumentExceptionForAlreadyRegisteredStates() {

		final Object state = new Object();
		context.addState(state, Object.class);

		try {
			context.addState(new Object(), Object.class);
			fail("Expected IllegalArgumentException to be thrown when registering an already existing state");
		} catch (IllegalArgumentException iae) {
			assertEquals("A state object is already registered for class " + Object.class.getName(), iae.getMessage());
		}

		try {
			context.addStates(new ArrayList(), new Object());
			fail("Expected IllegalArgumentException to be thrown when registering an already existing state");
		} catch (IllegalArgumentException iae) {
			assertEquals("A state object is already registered for class " + Object.class.getName(), iae.getMessage());
		}

		assertSame(state, context.getState(Object.class));
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

	private IErrorLocationType locationType() {
		return locationType(lastLocationType = "locationType" + RANDOM.nextInt());
	}
	
	private static IErrorLocationType locationType(final String locationType) {
		return new IErrorLocationType() {

			@Override
			public String getLocationType() {
				return locationType;
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

	private static BaseMatcher<IError> isAnErrorWith(final String message, final String location, final String locationType, final Integer code) {
		return new BaseMatcher<IError>() {
			@Override
			public boolean matches(Object item) {

				if (!(item instanceof IError)) {
					return false;
				}

				final IError error = (IError) item;

				return message.equals(error.getMessage())
						&& (location != null ? location.equals(error.getLocation()) : error.getLocation() == null)
						&& (locationType != null ? error.getLocationType() != null && locationType.equals(error.getLocationType().getLocationType()) : error.getLocationType() == null)
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
}
