package com.lotaris.jee.validation;

import com.lotaris.jee.validation.SkipValidationOnPreviousErrorsAtCurrentLocation;
import com.lotaris.jee.validation.IValidationContext;
import com.lotaris.jee.validation.AbstractValidator;
import com.lotaris.jee.validation.SkipValidationOnPreviousErrors;
import com.lotaris.rox.annotations.RoxableTest;
import com.lotaris.rox.annotations.RoxableTestClass;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @see AbstractValidator
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
@RoxableTestClass(tags = {"validation", "abstractValidator"})
public class AbstractValidatorUnitTest {

	@Mock
	private IValidationContext contextMock;

	@Before
	public void setUp() {

		MockitoAnnotations.initMocks(this);

		// when #location(String) is called, return the specified location without modification
		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				return (String) (invocation.getArguments()[0]);
			}
		}).when(contextMock).location(anyString());
	}

	@Test
	@RoxableTest(key = "b42015689929")
	public void abstractValidatorShouldCallValidateWhenCollectingErrors() {

		final Object objectToValidate = new Object();

		final TestValidator validator = new TestValidator() {
			@Override
			protected void validate(Object object, IValidationContext context) {
				super.validate(object, context);

				// check that the arguments are the same as given to #collectErrors
				assertSame(objectToValidate, object);
				assertSame(contextMock, context);
			}
		};

		validator.collectErrors(objectToValidate, contextMock);

		// check that validate was called once
		assertEquals(1, validator.numberOfCallsToValidate);
	}

	@Test
	@RoxableTest(key = "36f09be4f530")
	public void abstractValidatorShouldSkipValidationDueToPreviousErrorsWithLocationsGivenByAnnotation() {

		// validator with an annotation that skips validation if there are previous errors on /foo or /bar/baz
		final TestValidator validator = new AnnotatedTestValidator();

		// check that validate is not called with a previous error on /foo
		when(contextMock.hasErrors("/foo")).thenReturn(true);
		when(contextMock.hasErrors("/bar/baz")).thenReturn(false);
		validator.collectErrors(new Object(), contextMock);
		assertEquals(0, validator.numberOfCallsToValidate);

		// check that validate is not called with a previous error on /bar/baz
		when(contextMock.hasErrors("/foo")).thenReturn(false);
		when(contextMock.hasErrors("/bar/baz")).thenReturn(true);
		validator.collectErrors(new Object(), contextMock);
		assertEquals(0, validator.numberOfCallsToValidate);
	}

	@Test
	@RoxableTest(key = "43bed67a7ece")
	public void abstractValidatorShouldNotSkipValidationIfNoPreviousErrorsMatchWithLocationsGivenByAnnotation() {

		// validator with an annotation that skips validation if there are previous errors on /foo or /bar/baz
		final TestValidator validator = new AnnotatedTestValidator();

		// check that validate is called with no previous errors
		when(contextMock.hasErrors(anyString())).thenReturn(false);
		validator.collectErrors(new Object(), contextMock);
		assertEquals(1, validator.numberOfCallsToValidate);
	}

	@Test
	@RoxableTest(key = "0234fc272946")
	public void abstractValidatorShouldSkipValidationDueToPreviousErrorsAtCurrentLocationGivenByAnnotation() {

		// validator with an annotation that skips validation if there are previous errors at the current location
		final TestValidator validator = new CurrentLocationAnnotatedTestValidator();

		// check that validate is not called with a previous error on ""
		when(contextMock.hasErrors("")).thenReturn(true);
		validator.collectErrors(new Object(), contextMock);
		assertEquals(0, validator.numberOfCallsToValidate);

		// change the current location to /foo
		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				return "/foo" + invocation.getArguments()[0];
			}
		}).when(contextMock).location(anyString());

		// check that validate is not called with a previous error on /foo when the current location is /foo
		when(contextMock.hasErrors("/foo")).thenReturn(true);
		validator.collectErrors(new Object(), contextMock);
		assertEquals(0, validator.numberOfCallsToValidate);
	}

	@Test
	@RoxableTest(key = "d54a62e7e243")
	public void abstractValidatorShouldNotSkipValidationIfNoPreviousErrorsAtCurrentLocationGivenByAnnotation() {

		// validator with an annotation that skips validation if there are previous errors on /foo or /bar/baz
		final TestValidator validator = new CurrentLocationAnnotatedTestValidator();

		// check that validate is called with no previous errors
		when(contextMock.hasErrors(anyString())).thenReturn(false);
		validator.collectErrors(new Object(), contextMock);
		assertEquals(1, validator.numberOfCallsToValidate);
	}

	@Test
	@RoxableTest(key = "e49aec198ec8")
	public void abstractValidatorShouldSkipValidationDueToPreviousErrorsWithLocationsGivenByManualCall() {

		// validator with a manual call that skips validation if there are previous errors on /foo or /bar/baz
		final TestValidator validator = new TestValidator();
		validator.skipOnPreviousErrors("/foo", "/bar/baz");

		// check that validate is not called with a previous error on /foo
		when(contextMock.hasErrors("/foo")).thenReturn(true);
		when(contextMock.hasErrors("/bar/baz")).thenReturn(false);
		validator.collectErrors(new Object(), contextMock);
		assertEquals(0, validator.numberOfCallsToValidate);

		// check that validate is not called with a previous error on /bar/baz
		when(contextMock.hasErrors("/foo")).thenReturn(false);
		when(contextMock.hasErrors("/bar/baz")).thenReturn(true);
		validator.collectErrors(new Object(), contextMock);
		assertEquals(0, validator.numberOfCallsToValidate);
	}

	@Test
	@RoxableTest(key = "69982bc19c72")
	public void abstractValidatorShouldNotSkipValidationIfNoPreviousErrorsMatchWithLocationsGivenByManualCall() {

		// validator with a manual call that skips validation if there are previous errors on /foo or /bar/baz
		final TestValidator validator = new TestValidator();
		validator.skipOnPreviousErrors("/foo", "/bar/baz");

		// check that validate is called with no previous errors
		when(contextMock.hasErrors(anyString())).thenReturn(false);
		validator.collectErrors(new Object(), contextMock);
		assertEquals(1, validator.numberOfCallsToValidate);
	}

	@Test
	@RoxableTest(key = "24691a01aeb1")
	public void abstractValidatorShouldSkipValidationDueToPreviousErrorsWithLocationsGivenByOverride() {

		// validator with an override that skips validation if there are previous errors on /foo or /bar/baz
		final TestValidator validator = new OverrideTestValidator();

		// check that validate is not called with a previous error on /foo
		when(contextMock.hasErrors("/foo")).thenReturn(true);
		when(contextMock.hasErrors("/bar/baz")).thenReturn(false);
		validator.collectErrors(new Object(), contextMock);
		assertEquals(0, validator.numberOfCallsToValidate);

		// check that validate is not called with a previous error on /bar/baz
		when(contextMock.hasErrors("/foo")).thenReturn(false);
		when(contextMock.hasErrors("/bar/baz")).thenReturn(true);
		validator.collectErrors(new Object(), contextMock);
		assertEquals(0, validator.numberOfCallsToValidate);
	}

	@Test
	@RoxableTest(key = "3438420f737a")
	public void abstractValidatorShouldNotSkipValidationIfNoPreviousErrorsMatchWithLocationsGivenByOverride() {

		// validator with an override that skips validation if there are previous errors on /foo or /bar/baz
		final TestValidator validator = new OverrideTestValidator();

		// check that validate is called with no previous errors
		when(contextMock.hasErrors(anyString())).thenReturn(false);
		validator.collectErrors(new Object(), contextMock);
		assertEquals(1, validator.numberOfCallsToValidate);
	}

	@Test
	@RoxableTest(key = "93c540631816")
	public void abstractValidatorShouldCombinePreviousErrorLocationsFromAnnotationAndManualCalls() {

		// validator with an annotation that skips validation if there are previous errors on /foo or /bar/baz
		final TestValidator validator = new FullyAnnotatedTestValidator();
		assertSetContains(validator.getPreviousErrorLocations(contextMock), "", "/foo", "/bar/baz");

		// add more locations manually; check that old locations are still here and new locations were added
		validator.skipOnPreviousErrors("/bar/baz", "/baz");
		assertSetContains(validator.getPreviousErrorLocations(contextMock), "", "/foo", "/bar/baz", "/baz");

		// add more locations manually; check that old locations are still here and new locations were added
		validator.skipOnPreviousErrors("/baz", "/bar", "/foo", "/baz/foo");
		assertSetContains(validator.getPreviousErrorLocations(contextMock), "", "/foo", "/bar", "/bar/baz", "/baz", "/baz/foo");
	}

	@Test
	@RoxableTest(key = "b095cf68b485")
	public void abstractValidatorShouldPassPreviousErrorLocationsThroughTheValidationContext() {

		// validator with a manual call that skips validation if there are previous errors on /foo or /bar/baz
		final TestValidator validator = new TestValidator();
		validator.skipOnPreviousErrors("/foo", "/bar/baz");

		// mock context to build locations relative to /sub (e.g. /sub/foo for /foo)
		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				return "/sub" + invocation.getArguments()[0];
			}
		}).when(contextMock).location(anyString());

		// mock context to collect the (absolute) previous error locations it receives
		final Set<String> checkedLocations = new HashSet<>(2);
		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				checkedLocations.add((String) (invocation.getArguments()[0]));
				return false;
			}
		}).when(contextMock).hasErrors(anyString());

		validator.collectErrors(new Object(), contextMock);
		assertEquals(1, validator.numberOfCallsToValidate);

		// check that all relative locations were processed by the context before checking for errors
		assertSetContains(checkedLocations, "/sub/foo", "/sub/bar/baz");
	}

	private void assertSetContains(Set<String> set, String... elements) {

		final int n = elements.length;
		assertEquals("Expected set to contain " + n + " elements; got " + set.size(), n, set.size());

		for (int i = 0; i < n; i++) {
			assertTrue("Expected set to contain \"" + elements[i] + "\"", set.contains(elements[i]));
		}
	}

	private static class TestValidator extends AbstractValidator<Object> {

		private int numberOfCallsToValidate = 0;

		@Override
		protected void validate(Object object, IValidationContext context) {
			numberOfCallsToValidate++;
		}
	}

	@SkipValidationOnPreviousErrors(locations = {"/foo", "/bar/baz"})
	private static class AnnotatedTestValidator extends TestValidator {
	}

	@SkipValidationOnPreviousErrorsAtCurrentLocation
	private static class CurrentLocationAnnotatedTestValidator extends TestValidator {
	}

	@SkipValidationOnPreviousErrorsAtCurrentLocation
	@SkipValidationOnPreviousErrors(locations = {"/foo", "/bar/baz"})
	private static class FullyAnnotatedTestValidator extends TestValidator {
	}

	private static class OverrideTestValidator extends TestValidator {

		@Override
		public Set<String> getPreviousErrorLocations(IValidationContext context) {
			return fillSet("/foo", "/bar/baz");
		}
	}
}
