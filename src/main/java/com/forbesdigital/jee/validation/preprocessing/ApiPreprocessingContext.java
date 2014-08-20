package com.forbesdigital.jee.validation.preprocessing;

import com.forbesdigital.jee.validation.AbstractPatchTransferObject;
import com.forbesdigital.jee.validation.AbstractValidator;
import com.forbesdigital.jee.validation.ApiErrorResponseTO;
import com.forbesdigital.jee.validation.ApiErrorsException;
import com.forbesdigital.jee.validation.IPatchObject;
import com.forbesdigital.jee.validation.IValidationContext;
import com.forbesdigital.jee.validation.IValidator;
import com.forbesdigital.jee.validation.JsonValidationContext;
import java.util.ArrayList;
import java.util.List;
import javax.validation.groups.Default;

/**
 * API preprocessing configuration builder. Use this object to incrementally configure preprocessing
 * parameters and then run the preprocessor. The preprocessor can be a chain (see
 * {@link PreprocessingChain}).
 *
 * <p>The <tt>process</tt> method will throw an {@link ApiErrorsException} if any errors were added
 * to the internal {@link ApiErrorResponseTO} used as an error collector. This exception will be
 * mapped to an HTTP 422 unprocessable entity.</p>
 *
 * <p><pre>
 *	PreprocessingChain chain = ...;
 *	PreprocessingContext context = new PreprocessingContext(chain);
 *	context.validateOnly(Default.class, ValidationGroups.Create.class);
 *	context.process(objectToProcess);
 * </p></pre>
 *
 * <p>Since the context is a builder, these calls can also be chained.</p>
 *
 * <p><pre>
 *	PreprocessingChain chain = ...;
 *	new PreprocessingContext(chain).validateOnly(Default.class, ValidationGroups.Create.class).process(objectToProcess);
 * </p></pre>
 *
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
public class ApiPreprocessingContext implements IPreprocessingConfig {
	public static final int UNPROCESSABLE_ENTITY = 422;
	
	private IPreprocessor preprocessor;
	private ApiErrorResponseTO apiErrorResponse;
	private IValidationContext validationContext;
	private Class[] validationGroups;
	private List<IValidator> validators;
	private boolean failOnErrors;
	private boolean patchValidation;
	private Boolean result;

	public ApiPreprocessingContext(IPreprocessor preprocessor) {
		this.preprocessor = preprocessor;
		this.apiErrorResponse = new ApiErrorResponseTO(UNPROCESSABLE_ENTITY);
		this.validationContext = new JsonValidationContext(apiErrorResponse);
		this.validationGroups = new Class[]{};
		this.validators = new ArrayList<>();
		this.failOnErrors = true;
		this.patchValidation = false;
	}

	/**
	 * Runs preprocessing on the specified object.
	 *
	 * @param object the object to preprocess
	 * @return a result object indicating whether the preprocessing chain completed successfully and
	 * containing the errors collected during preprocessing (note that a successful chain may
	 * produce errors such as validation errors)
	 * @throws ApiErrorsException if any of the preprocessors adds errors to the error collector
	 */
	public ApiPreprocessingContext process(Object object) throws ApiErrorsException {
		if (result != null) {
			throw new IllegalStateException("This preprocessing context has already been used; create another one.");
		}

		result = preprocessor.process(object, this);
		
		if (failOnErrors && apiErrorResponse.hasErrors()) {
			throw new ApiErrorsException(apiErrorResponse);
		}

		return this;
	}

	/**
	 * Indicates whether preprocessing was successful after calling <tt>process</tt>.
	 *
	 * @return true if preprocessing was successful, false otherwise
	 * @throws IllegalStateException if <tt>process</tt> was not called
	 */
	public boolean isSuccessful() {
		if (result == null) {
			throw new IllegalStateException("This preprocessing context has not yet been used; call #process.");
		}
		return result;
	}

	/**
	 * Run only constraint validations belonging to the specified groups. If a constraint validation
	 * has no explicit group, it is part of the {@link Default} group.
	 *
	 * <p><pre>
	 *	// run only create validations
	 *	validateOnly(ValidationGroups.Create.class);
	 *
	 *	// run both default and update validations
	 *	validateOnly(Default.class, ValidationGroups.Update.class);
	 * </p></pre>
	 *
	 * @param groups the group (or list of groups) targeted for validation
	 * @return this updated context
	 */
	public ApiPreprocessingContext validateOnly(Class... groups) {
		this.validationGroups = groups;
		return this;
	}

	/**
	 * Adds the specified validators to be run on the preprocessed object after bean validations.
	 *
	 * @param apiValidators the validators to run
	 * @return this updated context
	 * @see AbstractValidator
	 */
	public ApiPreprocessingContext validateWith(IValidator... apiValidators) {
		for (IValidator apiValidator : apiValidators) {
			if (apiValidator == null) {
				throw new IllegalArgumentException("Validator cannot be null");
			}
			this.validators.add(apiValidator);
		}
		return this;
	}

	/**
	 * Enable patch validation. With patch validation, the validated object must be an
	 * {@link IPatchObject} that defines which of its properties were explicitly set. Only those
	 * properties will be validated.
	 *
	 * <p>Processing anything other than an {@link IPatchObject} after calling this method will
	 * throw an {@link IllegalArgumentException}.</p>
	 *
	 * @return this updated context
	 * @see IPatchObject
	 * @see AbstractPatchTransferObject
	 * @see BeanValidationPreprocessor
	 */
	public ApiPreprocessingContext validatePatch() {
		patchValidation = true;
		return this;
	}

	@Override
	public boolean isPatchValidationEnabled() {
		return patchValidation;
	}

	/**
	 * Sets whether the processing will throw an exception when errors are added to the error
	 * collector. Set to false to disable the exception, then you can retrieve the
	 * {@link ApiErrorResponseTO} and its errors with {@link #getApiErrorResponse()}.
	 *
	 * @param failOnErrors if true, an {@link ApiErrorsException} will be thrown if any errors are
	 * added to the error collector
	 * @return this updated context
	 */
	public ApiPreprocessingContext failOnErrors(boolean failOnErrors) {
		this.failOnErrors = failOnErrors;
		return this;
	}

	/**
	 * Indicates whether errors were collected during preprocessing.
	 *
	 * @return true if at least one error was added during preprocessing
	 */
	public boolean hasErrors() {
		return apiErrorResponse.hasErrors();
	}

	@Override
	public IValidationContext getValidationContext() {
		return validationContext;
	}

	@Override
	public List<IValidator> getValidators() {
		return validators;
	}

	@Override
	public Class[] getValidationGroups() {
		return validationGroups;
	}

	//<editor-fold defaultstate="collapsed" desc="Getters & Setters">
	public ApiErrorResponseTO getApiErrorResponse() {
		return apiErrorResponse;
	}
	//</editor-fold>
}
