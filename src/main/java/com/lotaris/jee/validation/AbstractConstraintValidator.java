package com.lotaris.jee.validation;

import java.lang.annotation.Annotation;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Basic bean validation constraint implementation. When using this class, you will implement the
 * <tt>validate</tt> method instead of the <tt>isValid</tt> method from {@link ConstraintValidator}.
 * This allows the abstract validator to hide bean validation implementation details from you,
 * making validations easier to write and test.
 *
 * <h2>What to implement</h2>
 *
 * <p>You must implement the <tt>validate</tt> method. A {@link IConstraintValidationContext} will
 * be passed to your method. This object will allow you to add errors if you determine that the
 * supplied value/object is invalid.</p>
 *
 * <h3>Minimal implementation</h3>
 *
 * <p>This validator implementation will use the default error message defined by your constraint
 * annotation.</p>
 *
 * <p><pre>
 *	public class MyAnnotationValidator extends AbstractConstraintValidator&lt;MyAnnotation, String&gt; {
 *
 *		&#64;Override
 *		public void validate(String value, IConstraintValidationContext context) {
 *			if (isInvalid(value)) {
 *				context.addDefaultError();
 *			}
 *		}
 *	}
 * </pre></p>
 *
 * <h3>Custom errors</h3>
 *
 * <p>Sometimes the static error message defined by your constraint annotation isn't enough. You
 * might need to build the message dynamically based on the validated value. In this case, use the
 * <tt>addError</tt> methods to override the default error message.</p>
 *
 * <p><pre>
 *	public class MyAnnotationValidator extends AbstractConstraintValidator&lt;MyAnnotation, Object&gt; {
 *
 *		&#64;Override
 *		public void validate(Object value, IConstraintValidationContext context) {
 *
 *			if (isInvalid(value)) {
 *
 *				// add the default error message
 *				context.addDefaultError();
 *
 *				// add another error message
 *				context.addErrorAtCurrentLocation("Object is invalid.");
 *
 *				// additional arguments are interpolated
 *				context.addErrorAtCurrentLocation("%s is invalid", value);
 *
 *				// if you are validating a complex object, you can add errors to a specific property
 *				context.addError("name", "Name must be at most 25 characters long, got %d.", value.getName().length());
 *			}
 *		}
 *	}
 * </pre></p>
 *
 * <h2>What to test</h2>
 *
 * <p>When testing, only test the <tt>validate</tt> method and not the <tt>isValid</tt> method. This
 * allows you to mock the validation context and ignore bean validation implementation details.</p>
 *
 * <p><pre>
 *	public class CheckStringLengthValidatorUnitTests {
 *
 *		&#64;Mock
 *		private IConstraintValidationContext context;
 *		private CheckStringLengthValidator validator;
 *
 *		&#64;Before
 *		public void setUp() {
 *			MockitoAnnotations.initMocks(this);
 *			validator = new CheckStringLengthValidator();
 *		}
 *
 *		&#64;Test
 *		public void checkStringLengthValidatorShouldFailIfStringIsTooLong() {
 *			validator.validate("foooooooooooooo", context);
 *			verify(context).addDefaultError();
 *			verifyNoMoreInteractions(context);
 *		}
 *	}
 * </pre></p>
 *
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 * @param <A> the constraint annotation
 * @param <T> the type of value to validate (e.g. String)
 */
public abstract class AbstractConstraintValidator<A extends Annotation, T> implements ConstraintValidator<A, T> {

	@Override
	public void initialize(A constraintAnnotation) {
		// do nothing by default
	}

	/**
	 * Validates the specified object.
	 *
	 * @param object the object to validate
	 * @param context the context used to add and keep track of errors during validation
	 */
	public abstract void validate(T value, IConstraintValidationContext context);

	@Override
	public final boolean isValid(T value, ConstraintValidatorContext context) {

		final IConstraintValidationContext wrapperContext = new ConstraintValidationContext(context);
		validate(value, wrapperContext);
		return !wrapperContext.hasErrors();
	}

	/**
	 * Wrapper around {@link ConstraintValidatorContext} to provide an easier-to-use validation API.
	 */
	private static class ConstraintValidationContext implements IConstraintValidationContext {

		private boolean hasErrors;
		private final ConstraintValidatorContext context;

		public ConstraintValidationContext(ConstraintValidatorContext context) {
			this.hasErrors = false;
			this.context = context;
		}

		private ConstraintValidatorContext addErrors() {
			if (!hasErrors) {
				context.disableDefaultConstraintViolation();
				hasErrors = true;
			}
			return context;
		}

		@Override
		public boolean hasErrors() {
			return hasErrors;
		}

		@Override
		public IConstraintValidationContext addDefaultError() {
			addErrors().buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate()).addConstraintViolation();
			return this;
		}

		@Override
		public IConstraintValidationContext addError(String location, String message, Object... messageArgs) {
			location = validateLocation(location);
			addErrors().buildConstraintViolationWithTemplate(String.format(message, messageArgs)).addPropertyNode(location).addConstraintViolation();
			return this;
		}
		
		@Override
		public IConstraintValidationContext addArrayError(String location, int index, String message, Object... messageArgs) {
			location = validateLocation(location);
			addErrors().buildConstraintViolationWithTemplate(String.format(message, messageArgs)).addPropertyNode(location).addPropertyNode(String.valueOf(index)).addConstraintViolation();
			return this;
		}

		@Override
		public IConstraintValidationContext addErrorAtCurrentLocation(String message, Object... messageArgs) {
			addErrors().buildConstraintViolationWithTemplate(String.format(message, messageArgs)).addConstraintViolation();
			return this;
		}
		
		/**
		 * Validate and sanitize the error location.
		 * @param location The location
		 * @return The sanitized location.
		 * @throws IllegalArgumentException If the location contains dots or slash
		 */
		private String validateLocation(String location) throws IllegalArgumentException {
			
			if (location.contains(".")) {
				throw new IllegalArgumentException("Constraint violation error locations cannot contain dots.");
			}
			location = location.replaceFirst("^\\/", "");
			if (location.contains("/")) {
				throw new IllegalArgumentException("Constraint violation error locations cannot contain a slash except as the first character.");
			}
			return location;
		}
		
	}
}
