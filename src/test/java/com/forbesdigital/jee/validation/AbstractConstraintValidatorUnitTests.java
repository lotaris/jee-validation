package com.forbesdigital.jee.validation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.lotaris.rox.annotations.RoxableTest;
import com.lotaris.rox.annotations.RoxableTestClass;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @see AbstractConstraintValidator
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
@RoxableTestClass(tags = {"validation", "beanValidation", "abstractConstraintValidator"})
public class AbstractConstraintValidatorUnitTests {

	private static final String DEFAULT_ERROR_MESSAGE = "default";
	@Mock
	private ConstraintValidatorContext beanValidationContext;
	@Mock
	private ConstraintViolationBuilder constraintViolationBuilder;
	@Mock
	private NodeBuilderCustomizableContext nodeBuilderCustomizableContext;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		doNothing().when(beanValidationContext).disableDefaultConstraintViolation();
		when(beanValidationContext.getDefaultConstraintMessageTemplate()).thenReturn(DEFAULT_ERROR_MESSAGE);
		when(beanValidationContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(constraintViolationBuilder);
		when(constraintViolationBuilder.addConstraintViolation()).thenReturn(beanValidationContext);
		when(constraintViolationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilderCustomizableContext);
		when(nodeBuilderCustomizableContext.addPropertyNode(anyString())).thenReturn(nodeBuilderCustomizableContext);
		when(nodeBuilderCustomizableContext.addConstraintViolation()).thenReturn(beanValidationContext);
	}

	@Test
	@RoxableTest(key = "a595408c248d")
	public void abstractConstraintValidatorShouldCallValidateFromIsValid() {

		final Object objectToValidate = new Object();
		final TestValidator validator = spy(new TestValidator());

		validator.isValid(objectToValidate, beanValidationContext);
		verify(validator).validate(same(objectToValidate), argThat(new BaseMatcher<IConstraintValidationContext>() {
			@Override
			public boolean matches(Object item) {
				return item != null;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("constraint validation context");
			}
		}));
	}

	@Test
	@RoxableTest(key = "870b66b4443c")
	public void abstractConstraintValidatorShouldConsiderValueValidIfNoErrorsWereAddedToTheValidationContext() {
		assertTrue(new TestValidator().isValid(new Object(), beanValidationContext));
		verifyZeroInteractions(beanValidationContext);
	}

	@Test
	@RoxableTest(key = "8a8d270ccc8a")
	public void abstractConstraintValidatorShouldAddTheDefaultErrorThroughTheValidationContext() {

		final TestValidator validator = new TestValidator() {
			@Override
			public void validate(Object value, IConstraintValidationContext context) {
				context.addDefaultError();
			}
		};

		assertFalse(validator.isValid(new Object(), beanValidationContext));
		verifyDefaultErrorAdded();
	}

	@Test
	@RoxableTest(key = "3136e7319101")
	public void abstractConstraintValidatorShouldAddErrorAtCurrentLocationThroughTheValidationContext() {

		final TestValidator validator = new TestValidator() {
			@Override
			public void validate(Object value, IConstraintValidationContext context) {
				context.addErrorAtCurrentLocation("foo");
			}
		};

		assertFalse(validator.isValid(new Object(), beanValidationContext));
		verifyErrorAddedAtCurrentLocation("foo");
	}

	@Test
	@RoxableTest(key = "5747282b3968")
	public void abstractConstraintValidatorShouldAddErrorWithInterpolatedArgumentsAtCurrentLocationThroughTheValidationContext() {

		final TestValidator validator = new TestValidator() {
			@Override
			public void validate(Object value, IConstraintValidationContext context) {
				context.addErrorAtCurrentLocation("%s, %s and %s", "a", "b", "c");
			}
		};

		assertFalse(validator.isValid(new Object(), beanValidationContext));
		verifyErrorAddedAtCurrentLocation("a, b and c");
	}

	@Test
	@RoxableTest(key = "0d230f28fb91")
	public void abstractConstraintValidatorShouldAddErrorThroughTheValidationContext() {

		final TestValidator validator = new TestValidator() {
			@Override
			public void validate(Object value, IConstraintValidationContext context) {
				context.addError("property", "bar");
			}
		};

		assertFalse(validator.isValid(new Object(), beanValidationContext));
		verifyErrorAdded("property", "bar");
	}

	@Test
	@RoxableTest(key = "ae2e6ac46fdc")
	public void abstractConstraintValidatorShouldAddErrorWithInterpolatedArgumentsThroughTheValidationContext() {

		final TestValidator validator = new TestValidator() {
			@Override
			public void validate(Object value, IConstraintValidationContext context) {
				context.addError("property", "%s, %s and %s", "d", "e", "f");
			}
		};

		assertFalse(validator.isValid(new Object(), beanValidationContext));
		verifyErrorAdded("property", "d, e and f");
	}

	@Test
	@RoxableTest(key = "ea1abc92425a")
	public void abstractConstraintValidatorShouldAllowCustomErrorsIfTheDefaultErrorWasAdded() {

		try {
			new TestValidator() {
				@Override
				public void validate(Object value, IConstraintValidationContext context) {
					context.addDefaultError();
					context.addErrorAtCurrentLocation("foo");
					context.addError("property", "foo");
				}
			}.isValid(new Object(), beanValidationContext);
		} catch (IllegalStateException ise) {
			fail("Expected no exception to be thrown");
		}

		verify(constraintViolationBuilder, times(2)).addConstraintViolation();
		verify(nodeBuilderCustomizableContext).addConstraintViolation();
	}

	@Test
	@RoxableTest(key = "c61d92e9682d")
	public void abstractConstraintValidatorShouldAllowTheDefaultErrorIfCustomErrorsWereAdded() {

		try {
			new TestValidator() {
				@Override
				public void validate(Object value, IConstraintValidationContext context) {
					context.addErrorAtCurrentLocation("foo");
					context.addError("property", "foo");
					context.addArrayError("arrayProperty", 42, "foo");
					context.addDefaultError();
				}
			}.isValid(new Object(), beanValidationContext);
		} catch (IllegalStateException ise) {
			fail("Expected no exception to be thrown");
		}

		verify(constraintViolationBuilder, times(2)).addConstraintViolation();
		verify(nodeBuilderCustomizableContext, times(2)).addConstraintViolation();
	}

	@Test
	@RoxableTest(key = "2ae29e32ee81")
	public void abstractConstraintValidatorShouldNotAllowDotsOrMultipleSlashesInTheErrorLocation() {

		try {
			new TestValidator() {
				@Override
				public void validate(Object value, IConstraintValidationContext context) {
					context.addError(".dotted.property", "foo");
					context.addArrayError(".dotted.arrayProperty", 42, "foo");
				}
			}.isValid(new Object(), beanValidationContext);
			fail("Expected an invalid argument exception to be thrown");
		} catch (IllegalArgumentException iae) {
			// successful
		}

		try {
			new TestValidator() {
				@Override
				public void validate(Object value, IConstraintValidationContext context) {
					context.addError("/sub/path", "foo");
					context.addArrayError("/sub/arrayProperty", 42, "foo");
				}
			}.isValid(new Object(), beanValidationContext);
			fail("Expected an invalid argument exception to be thrown");
		} catch (IllegalArgumentException iae) {
			// successful
		}
	}

	@Test
	@RoxableTest(key = "4a78f1cedb44")
	public void abstractConstraintValidatorShouldAllowJsonPointerLocationWithOneLevelAsTheErrorLocation() {

		assertFalse(new TestValidator() {
			@Override
			public void validate(Object value, IConstraintValidationContext context) {
				context.addError("/property", "foo");
			}
		}.isValid(new Object(), beanValidationContext));
		verifyErrorAdded("property", "foo");
	}

	private void verifyErrorAdded(final String location, final String message) {

		verify(beanValidationContext).disableDefaultConstraintViolation();
		verify(beanValidationContext).buildConstraintViolationWithTemplate(message);
		verifyNoMoreInteractions(beanValidationContext);

		verify(constraintViolationBuilder).addPropertyNode(location);
		verifyNoMoreInteractions(constraintViolationBuilder);

		verify(nodeBuilderCustomizableContext).addConstraintViolation();
		verifyNoMoreInteractions(nodeBuilderCustomizableContext);
	}

	private void verifyErrorAddedAtCurrentLocation(final String message) {

		verify(beanValidationContext).disableDefaultConstraintViolation();
		verify(beanValidationContext).buildConstraintViolationWithTemplate(message);
		verifyNoMoreInteractions(beanValidationContext);

		verify(constraintViolationBuilder).addConstraintViolation();
		verifyNoMoreInteractions(constraintViolationBuilder);

		verifyZeroInteractions(nodeBuilderCustomizableContext);
	}

	private void verifyDefaultErrorAdded() {

		verify(beanValidationContext).disableDefaultConstraintViolation();
		verify(beanValidationContext).getDefaultConstraintMessageTemplate();
		verify(beanValidationContext).buildConstraintViolationWithTemplate(DEFAULT_ERROR_MESSAGE);
		verifyNoMoreInteractions(beanValidationContext);

		verify(constraintViolationBuilder).addConstraintViolation();
		verifyNoMoreInteractions(constraintViolationBuilder);

		verifyZeroInteractions(nodeBuilderCustomizableContext);
	}

	private static class TestValidator extends AbstractConstraintValidator<TestAnnotation, Object> {

		@Override
		public void validate(Object value, IConstraintValidationContext context) {
		}
	}

	private static @interface TestAnnotation {
	}
}
