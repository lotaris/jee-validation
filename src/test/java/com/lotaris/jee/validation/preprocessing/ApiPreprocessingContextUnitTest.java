package com.lotaris.jee.validation.preprocessing;

import com.lotaris.jee.validation.preprocessing.IPreprocessingConfig;
import com.lotaris.jee.validation.preprocessing.IPreprocessor;
import com.lotaris.jee.validation.preprocessing.ApiPreprocessingContext;
import com.lotaris.jee.test.utils.PreprossessingAnswers;
import com.lotaris.jee.validation.ApiErrorsException;
import com.lotaris.jee.validation.IErrorCode;
import com.lotaris.jee.validation.IValidator;
import com.lotaris.rox.annotations.RoxableTest;
import com.lotaris.rox.annotations.RoxableTestClass;
import java.util.Arrays;
import java.util.List;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static com.lotaris.jee.test.matchers.Matchers.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @see ApiPreprocessingContext
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
@RoxableTestClass(tags = {"preprocessing", "apiPreprocessingContext"})
public class ApiPreprocessingContextUnitTest {

	@Mock
	private IPreprocessor preprocessor;
	private ApiPreprocessingContext context;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		context = createPreprocessingContext();
	}

	@Test
	@RoxableTest(key = "b16890cc8750")
	public void apiPreprocessingContextShouldForwardProcessingToItsPreprocessor() throws ApiErrorsException {

		final Object objectToPreprocess = new Object();

		when(preprocessor.process(anyObject(), any(IPreprocessingConfig.class))).thenReturn(true);
		assertThat(context.process(objectToPreprocess), isSuccessfulPreprocessingResult(true));

		when(preprocessor.process(anyObject(), any(IPreprocessingConfig.class))).thenReturn(false);
		assertThat(createPreprocessingContext().process(objectToPreprocess), isSuccessfulPreprocessingResult(false));
	}

	@Test
	@RoxableTest(key = "4b6d4b545765")
	public void apiPreprocessingContextShouldPassItselfToItsPreprocessorAsThePreprocessingContext() throws ApiErrorsException {

		when(preprocessor.process(anyObject(), any(IPreprocessingConfig.class))).thenReturn(true);
		context.process(new Object());

		verify(preprocessor).process(anyObject(), same(context));
	}

	@Test
	@RoxableTest(key = "4ff56a423d8a")
	public void apiPreprocessingContextShouldRegisterValidationGroups() {

		context.validateOnly(ValidationGroupA.class, ValidationGroupB.class);

		final Class[] validationGroups = context.getValidationGroups();
		assertEquals(ValidationGroupA.class, validationGroups[0]);
		assertEquals(ValidationGroupB.class, validationGroups[1]);
		assertEquals(2, validationGroups.length);
	}

	@Test
	@RoxableTest(key = "3345ed6d8909")
	public void apiPreprocessingContextShouldRegisterValidators() {

		final List<IValidator> validators = Arrays.asList(mock(IValidator.class), mock(IValidator.class));
		context.validateWith(validators.toArray(new IValidator[0]));

		assertEquals(validators, context.getValidators());
	}

	@Test
	@RoxableTest(key = "f20db7f8fe85")
	public void apiPreprocessingContextShouldEnablePatchValidation() {
		assertFalse("Patch validation should not be enabled by default", context.isPatchValidationEnabled());
		assertSame("#validatePatch should return the context itself", context, context.validatePatch());
		assertTrue("Patch validation should be enabled after calling #validatePatch", context.isPatchValidationEnabled());
	}

	@Test
	@RoxableTest(key = "992a5beffc99")
	public void apiPreprocessingContextShouldHaveAnEmptyUnprocessableEntityApiErrorResponseByDefault() {
		assertEquals(ApiPreprocessingContext.UNPROCESSABLE_ENTITY, context.getApiErrorResponse().getHttpStatusCode());
		assertFalse(context.hasErrors());
		assertFalse(context.getApiErrorResponse().hasErrors());
	}

	@Test
	@RoxableTest(key = "50a577358551")
	public void apiPreprocessingContextShouldOnlyBeProcessableOnce() throws ApiErrorsException {

		when(preprocessor.process(anyObject(), any(IPreprocessingConfig.class))).thenReturn(true);
		assertThat(context.process(new Object()), isSuccessfulPreprocessingResult(true));

		try {
			assertThat(context.process(new Object()), isSuccessfulPreprocessingResult(false));
			fail("Expected an illegal state exception when trying to reuse context");
		} catch (IllegalStateException ise) {
			// success
		}
	}

	@Test
	@RoxableTest(key = "b49215985574")
	public void apiPreprocessingContextShouldNotBeSuccessfulWhenUnprocessed() {
		try {
			context.isSuccessful();
			fail("Context should have thrown an illegal state exception when trying to get the result before processing");
		} catch (IllegalStateException ise) {
			// success
		}
	}

	@Test
	@RoxableTest(key = "e3e186c9ca32")
	public void apiPreprocessingContextShouldBuildValidationContextWithItsApiErrorResponseAsTheErrorCollector() {

		// check that the context has an API error response
		assertNotNull(context.getApiErrorResponse());

		// add an error to the validation context
		context.getValidationContext().addError(null, null, errorCode(1), "foo");

		// check that the error was added to the API error response through the validation context
		assertThat(context.getApiErrorResponse(), isApiErrorResponseObject(422).withError(1, null, "foo"));
	}

	@Test
	@RoxableTest(key = "da639176bb96")
	public void apiPreprocessingContextShouldThrowAnApiErrorsExceptionIfErrorsAreAddedToTheValidationContext() {

		// add an error during preprocessing
		doAnswer(new PreprossessingAnswers.PreprossessingWithErrorAnswer(errorCode(2), "foo")).when(preprocessor).process(anyObject(), same(context));

		// make sure the exception is thrown
		try {
			context.process(new Object());
			fail("Expected an API error exception to be thrown when errors are added to the validation context by the preprocessor; nothing was thrown");
		} catch (ApiErrorsException aee) {
			assertThat(aee, isApiErrorsException(422).withError(2, null, "foo"));
			assertSame("Expected the API error response of the exception to be the internal API error response of the context",
					context.getApiErrorResponse(), aee.getErrorResponse());
		}
	}

	@Test
	@RoxableTest(key = "2ed5d7cc5278")
	public void apiPreprocessingContextShouldNotThrowAnApiErrorsExceptionIfDisabled() {

		// add an error during preprocessing
		doAnswer(new PreprossessingAnswers.PreprossessingWithErrorAnswer(new IErrorCode() {
			@Override
			public int getCode() {
				return 10;
			}

			@Override
			public int getDefaultHttpStatusCode() {
				return 422;
			}
		}, "")).when(preprocessor).process(anyObject(), same(context));

		// disable the exception and make sure it is not thrown
		try {
			assertThat(context.failOnErrors(false).process(new Object()), isSuccessfulPreprocessingResult(true));
		} catch (ApiErrorsException aee) {
			fail("Expected API error exception to be disabled; it was thrown when errors are added to the validation context by the preprocessor");
		}
	}

	@Test
	@RoxableTest(key = "6825a51caee9")
	public void apiPreprocessingContextShouldReturnCollectedErrors() throws ApiErrorsException {

		// add two errors during preprocessing
		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				((IPreprocessingConfig) invocation.getArguments()[1]).getValidationContext().addError(null, null, errorCode(1), "foo");
				((IPreprocessingConfig) invocation.getArguments()[1]).getValidationContext().addError(null, null, errorCode(2), "bar");
				return true;
			}
		}).when(preprocessor).process(anyObject(), same(context));

		final ApiPreprocessingContext result = context.failOnErrors(false).process(new Object());
		assertThat(result, isSuccessfulPreprocessingResult(true));
		assertTrue("Preprocessing result should indicate that errors were collected", result.hasErrors());
		assertThat(result.getApiErrorResponse(), isApiErrorResponseObject(422).withError(1, null, "foo").withError(2, null, "bar"));
	}

	@Test
	@RoxableTest(key = "8834b48ef974")
	public void apiPreprocessingContextShouldReturnCollectedErrorsWhenThereAreNone() throws ApiErrorsException {

		when(preprocessor.process(anyObject(), any(IPreprocessingConfig.class))).thenReturn(true);

		final ApiPreprocessingContext result = context.failOnErrors(false).process(new Object());
		assertThat(result, isSuccessfulPreprocessingResult(true));
		assertFalse("Preprocessing result should indicate that no errors were collected", result.hasErrors());
		assertThat(result.getApiErrorResponse(), isApiErrorResponseObject(422));
	}

	@Test
	@RoxableTest(key = "311f9c8c7f61")
	public void apiPreprocessingContextShouldAddStateObjectsToTheValidationContext() {

		try {
			context.getValidationContext().getState(Object.class);
			fail("Expected IllegalArgumentException to be thrown when getting an unregistered state");
		} catch (IllegalArgumentException iae) {
		}

		try {
			context.getValidationContext().getState(ArrayList.class);
			fail("Expected IllegalArgumentException to be thrown when getting an unregistered state");
		} catch (IllegalArgumentException iae) {
		}

		try {
			context.getValidationContext().getState(Map.class);
			fail("Expected IllegalArgumentException to be thrown when getting an unregistered state");
		} catch (IllegalArgumentException iae) {
		}

		final Object state1 = new Object();
		context.withState(state1, Object.class);
		assertSame(state1, context.getValidationContext().getState(Object.class));

		final List state2 = new ArrayList();
		final Map state3 = new HashMap();
		context.withStates(state2, state3);
		assertSame(state2, context.getValidationContext().getState(ArrayList.class));
		assertSame(state3, context.getValidationContext().getState(HashMap.class));
	}

	private IErrorCode errorCode(final int code) {
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

	private ApiPreprocessingContext createPreprocessingContext() {
		return new ApiPreprocessingContext(preprocessor);
	}

	private static Matcher<ApiPreprocessingContext> isSuccessfulPreprocessingResult(final boolean successful) {
		return new BaseMatcher<ApiPreprocessingContext>() {
			@Override
			public boolean matches(Object item) {
				final ApiPreprocessingContext result = (ApiPreprocessingContext) item;
				return result != null && result.isSuccessful() == successful;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText(successful ? "successful preprocessing result" : "failed preprocessing result");
			}

			@Override
			public void describeMismatch(Object item, Description description) {
				if (item == null) {
					description.appendText("preprocessing result is null");
				} else {
					description.appendText(successful ? "preprocessing result is not successful" : "preprocessing result is successful");
				}
			}
		};
	}

	private static interface ValidationGroupA {
	}

	private static interface ValidationGroupB {
	}
}
