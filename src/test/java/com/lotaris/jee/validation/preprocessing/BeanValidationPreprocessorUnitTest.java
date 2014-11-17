package com.lotaris.jee.validation.preprocessing;

import com.lotaris.jee.validation.preprocessing.IPreprocessingConfig;
import com.lotaris.jee.validation.preprocessing.BeanValidationPreprocessor;
import com.lotaris.jee.validation.AbstractConstraintValidator;
import com.lotaris.jee.validation.IConstraintConverter;
import com.lotaris.jee.validation.IConstraintValidationContext;
import com.lotaris.jee.validation.IErrorCode;
import com.lotaris.jee.validation.IErrorLocationType;
import com.lotaris.jee.validation.IPatchObject;
import com.lotaris.jee.validation.IValidationContext;
import com.lotaris.rox.annotations.RoxableTest;
import com.lotaris.rox.annotations.RoxableTestClass;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @see BeanValidationPreprocessor
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
@RoxableTestClass(tags = {"validation", "beanValidation", "beanValidationPreprocessor"})
public class BeanValidationPreprocessorUnitTest {

	@Mock
	private IPreprocessingConfig config;
	@Mock
	private IValidationContext validationContext;
	@Spy
	protected ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
	@InjectMocks
	private BeanValidationPreprocessor processor;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(config.getValidationContext()).thenReturn(validationContext);
		when(config.getValidationGroups()).thenReturn(new Class[]{});
		when(config.getValidators()).thenReturn(Collections.EMPTY_LIST);
		when(config.isPatchValidationEnabled()).thenReturn(false);

		processor.setConstraintConverter(new IConstraintConverter() {
			@Override
			public IErrorCode getErrorCode(Class<? extends Annotation> annotationType) {
				final ConstraintConverter converter = annotationType.getAnnotation(ConstraintConverter.class);
				
				if (converter != null) {
					return new IErrorCode() {
						@Override
						public int getCode() {
							return converter.code();
						}

						@Override
						public int getDefaultHttpStatusCode() {
							return 200;
						}
					};
				}
				else {
					return null;
				}
			}

			@Override
			public IErrorLocationType getErrorLocationType(Class<? extends Annotation> annotationType) {
				final ConstraintConverter converter = annotationType.getAnnotation(ConstraintConverter.class);
				
				if (converter != null) {
					return new IErrorLocationType() {
						@Override
						public String getLocationType() {
							return converter.locationType();
						}
					};
				}
				else {
					return null;
				}
			}
		});
	}
	
	@Test
	@RoxableTest(key = "69d1921880d8")
	public void beanValidationPreprocessorShouldNotAddAnyErrorsForValidObjects() {

		// valid user
		final UserTO user = new UserTO();
		user.setName("jdoe");

		assertTrue(processor.process(user, config));

		// ensure that no other errors have been added
		verify(validationContext, never()).addError(anyString(), any(IErrorLocationType.class), any(IErrorCode.class), anyString());
		verify(validationContext, never()).addErrorAtCurrentLocation(any(IErrorCode.class), anyString());
	}

	@Test
	@RoxableTest(key = "2129c5f576a7")
	public void beanValidationPreprocessorShouldConvertConstraintViolationsToValidationErrors() {

		// user with null name
		final UserTO user = new UserTO();

		// address with null street
		user.setAddress(new AddressTO());

		// one app with null name
		ApplicationTO app = new ApplicationTO();
		user.getApplications().add(app);

		// another app with an invalid name
		app = new ApplicationTO();
		app.setName("not null name that is too long");
		user.getApplications().add(app);

		final NotNullErrorCode notNullErrorCode = new NotNullErrorCode();
		final LengthErrorCode lnErrorCode = new LengthErrorCode();
		final JsonLocationType errorLocationType = new JsonLocationType(); 

		processor.setConstraintConverter(new IConstraintConverter() {
			@Override
			public IErrorCode getErrorCode(Class<? extends Annotation> annotationType) {
				final ConstraintConverter converter = annotationType.getAnnotation(ConstraintConverter.class);

				if (converter.code() == 10) {
					return notNullErrorCode;
				}
				else {
					return lnErrorCode;
				}
			}

			@Override
			public IErrorLocationType getErrorLocationType(Class<? extends Annotation> annotationType) {
				return errorLocationType;
			}
		});
		
		// process the top-level object
		assertTrue(processor.process(user, config));

		// ensure that all errors have been added at the correct location and with the correct code
		verify(validationContext, times(1)).addError(eq("/name"), eq(errorLocationType), eq(notNullErrorCode), eq("This value must not be null."));
		verify(validationContext, times(1)).addError(eq("/address/street"), eq(errorLocationType), eq(notNullErrorCode), eq("This value must not be null."));
		verify(validationContext, times(1)).addError(eq("/applications/0/name"), eq(errorLocationType), eq(notNullErrorCode), eq("This value must not be null."));
		verify(validationContext, times(1)).addError(eq("/applications/1/name"), eq(errorLocationType), eq(lnErrorCode), eq("This value must be at most 20 characters long."));

		// ensure that no other errors have been added
		verify(validationContext, times(4)).addError(anyString(), any(IErrorLocationType.class), any(IErrorCode.class), anyString());
		verify(validationContext, never()).addErrorAtCurrentLocation(any(IErrorCode.class), anyString());
	}

	@Test
	@RoxableTest(key = "d6dc19eca2e6")
	public void beanValidationPreprocessorShouldIgnoreUnsetFieldsInPatchObjects() {

		final PatchTO patch = new PatchTO();
		patch.setFirstName("Bob"); // first name set and valid
		patch.setMiddleName(null); // middle name set to null; should be invalid
		// last name not set; should not be validated
		patch.setAddress(new AddressTO()); // address is set with no street; street should be invalid

		final NotNullErrorCode notNullErrorCode = new NotNullErrorCode();
		final JsonLocationType errorLocationType = new JsonLocationType(); 

		processor.setConstraintConverter(new IConstraintConverter() {
			@Override
			public IErrorCode getErrorCode(Class<? extends Annotation> annotationType) {
				return notNullErrorCode;
			}

			@Override
			public IErrorLocationType getErrorLocationType(Class<? extends Annotation> annotationType) {
				return errorLocationType;
			}
		});

		when(config.isPatchValidationEnabled()).thenReturn(true);
		assertTrue(processor.process(patch, config));

		// ensure that an error was added for the middle name
		verify(validationContext, times(1)).addError(eq("/middleName"), eq(errorLocationType), eq(notNullErrorCode), eq("This value must not be null."));
		verify(validationContext, times(1)).addError(eq("/address/street"), eq(errorLocationType), eq(notNullErrorCode), eq("This value must not be null."));

		// ensure that no other errors have been added
		verify(validationContext, times(2)).addError(anyString(), any(IErrorLocationType.class), any(IErrorCode.class), anyString());
		verify(validationContext, never()).addErrorAtCurrentLocation(any(IErrorCode.class), anyString());
	}

	@Test
	@RoxableTest(key = "61d8b5bbe987")
	public void beanValidationPreprocessorShouldNotUsePatchValidationIfNotExplicityEnabled() {

		final PatchTO patch = new PatchTO();
		patch.setFirstName("Bob"); // first name set and valid
		patch.setMiddleName(null); // middle name set to null; should be invalid
		// last name not set; should be validated because patch validation is not enabled

		final NotNullErrorCode notNullErrorCode = new NotNullErrorCode();
		final JsonLocationType errorLocationType = new JsonLocationType(); 
		
		processor.setConstraintConverter(new IConstraintConverter() {
			@Override
			public IErrorCode getErrorCode(Class<? extends Annotation> annotationType) {
				return notNullErrorCode;
			}

			@Override
			public IErrorLocationType getErrorLocationType(Class<? extends Annotation> annotationType) {
				return errorLocationType;
			}
		});
		
		assertTrue(processor.process(patch, config));

		// ensure that an error was added for the middle name
		verify(validationContext, times(1)).addError(eq("/middleName"), eq(errorLocationType), eq(notNullErrorCode), eq("This value must not be null."));
		verify(validationContext, times(1)).addError(eq("/lastName"), eq(errorLocationType), eq(notNullErrorCode), eq("This value must not be null."));

		// ensure that no other errors have been added
		verify(validationContext, times(2)).addError(anyString(), any(IErrorLocationType.class), any(IErrorCode.class), anyString());
		verify(validationContext, never()).addErrorAtCurrentLocation(any(IErrorCode.class), anyString());
	}
	
	@Test
	@RoxableTest(key = "703d341e87b8")
	public void beanValidationPreprocessorShouldThrowExceptionWhenPatchValidatingNonPatchObjects() {

		when(config.isPatchValidationEnabled()).thenReturn(true);

		try {
			processor.process(new Object(), config);
			fail("Processing a non-patch object with patch validation enabled should have thrown an illegal argument exception");
		} catch (IllegalArgumentException iae) {
			// success
		}
	}

	//<editor-fold defaultstate="collapsed" desc="@CheckNotNullTest Annotation & Validator">
	@Documented
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = CheckNotNullTestValidator.class)
	@ConstraintConverter(code = 10, locationType = "JSON")
	public @interface CheckNotNullTest {

		String message() default "This value must not be null.";

		Class<?>[] groups() default {};

		Class<? extends Payload>[] payload() default {};
	}

	public static class CheckNotNullTestValidator extends AbstractConstraintValidator<CheckNotNullTest, Object> {

		@Override
		public void validate(Object value, IConstraintValidationContext context) {
			if (value == null) {
				context.addDefaultError();
			}
		}
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="@CheckStringLengthTest Annotation & Validator">
	@Documented
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = CheckStringLengthTestValidator.class)
	@ConstraintConverter(code = 12, locationType = "json")
	public @interface CheckStringLengthTest {

		int max();

		String message() default "This value must be at most {max} characters long.";

		Class<?>[] groups() default {};

		Class<? extends Payload>[] payload() default {};
	}

	public static class CheckStringLengthTestValidator extends AbstractConstraintValidator<CheckStringLengthTest, String> {

		private int max;

		@Override
		public void initialize(CheckStringLengthTest constraintAnnotation) {
			max = constraintAnnotation.max();
		}

		@Override
		public void validate(String value, IConstraintValidationContext context) {
			if (value != null && value.length() > max) {
				context.addDefaultError();
			}
		}
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="Test Transfer Object Classes">
	private static class UserTO {

		@CheckNotNullTest
		private String name;
		@Valid
		private AddressTO address;
		@Valid
		private List<ApplicationTO> applications;

		public UserTO() {
			applications = new ArrayList<>();
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public AddressTO getAddress() {
			return address;
		}

		public void setAddress(AddressTO address) {
			this.address = address;
		}

		public List<ApplicationTO> getApplications() {
			return applications;
		}

		public void setApplications(List<ApplicationTO> applications) {
			this.applications = applications;
		}
	}

	private static class AddressTO {

		@CheckNotNullTest
		private String street;

		public String getStreet() {
			return street;
		}

		public void setStreet(String street) {
			this.street = street;
		}
	}

	private static class ApplicationTO {

		@CheckNotNullTest
		@CheckStringLengthTest(max = 20)
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	private static class PatchTO implements IPatchObject {

		private static final String FIRST_NAME = "firstName";
		private static final String MIDDLE_NAME = "middleName";
		private static final String LAST_NAME = "lastName";
		private static final String ADDRESS = "address";
		@CheckNotNullTest
		private String firstName;
		@CheckNotNullTest
		private String middleName;
		@CheckNotNullTest
		private String lastName;
		@Valid
		private AddressTO address;
		private Set<String> setProperties;

		public PatchTO() {
			this.setProperties = new HashSet<>();
		}

		@Override
		public boolean isPropertySet(String property) {
			return setProperties.contains(property);
		}

		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			setProperties.add(FIRST_NAME);
			this.firstName = firstName;
		}

		public String getMiddleName() {
			return middleName;
		}

		public void setMiddleName(String middleName) {
			setProperties.add(MIDDLE_NAME);
			this.middleName = middleName;
		}

		public String getLastName() {
			return lastName;
		}

		public void setLastName(String lastName) {
			setProperties.add(LAST_NAME);
			this.lastName = lastName;
		}

		public AddressTO getAddress() {
			return address;
		}

		public void setAddress(AddressTO address) {
			setProperties.add(ADDRESS);
			this.address = address;
		}
	}
	
	public static class NotNullErrorCode implements IErrorCode {

		@Override
		public int getCode() {
			return 10;
		}

		@Override
		public int getDefaultHttpStatusCode() {
			return 422;
		}
	}
	
	public static class LengthErrorCode implements IErrorCode {

		@Override
		public int getCode() {
			return 12;
		}

		@Override
		public int getDefaultHttpStatusCode() {
			return 422;
		}
	}
	
	public static class JsonLocationType implements IErrorLocationType {
	
		@Override
		public String getLocationType() {
			return "json"; //To change body of generated methods, choose Tools | Templates.
		}
	}
	//</editor-fold>

}
