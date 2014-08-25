package com.forbesdigital.jee.validation.preprocessing;

import com.forbesdigital.jee.validation.AbstractPatchTransferObject;
import com.forbesdigital.jee.validation.IConstraintConverter;
import com.forbesdigital.jee.validation.IErrorCode;
import com.forbesdigital.jee.validation.IErrorLocationType;
import com.forbesdigital.jee.validation.IJsonWrapper;
import com.forbesdigital.jee.validation.IPatchObject;
import com.forbesdigital.jee.validation.IValidationContext;
import com.forbesdigital.jee.validation.JsonPointer;
import java.lang.annotation.Annotation;
import java.util.Set;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;

/**
 * Applies all constraint validation annotations (bean validations) on the processed object. If the
 * object is invalid, errors are collected into the {@link ApiErrorResponse} returned by
 * {@link IPreprocessingConfig#getErrors()}.
 *
 * <h2>JSON Wrappers</h2>
 *
 * <p>If the object is an instance of {@link IJsonWrapper}, the first path fragment in error
 * locations will be removed. For example:</p>
 *
 * <p><pre>
 *	public class Person {
 *
 *		&#64;CheckNotNull
 *		private String name;
 *	}
 *
 *	public class JsonWrapper implements IJsonWrapper {
 *
 *		&#64;Valid
 *		private Person person;
 *	}
 *
 *	// An error on the person name will have its
 *	// location set to "/name" instead of "/person/name".
 * </pre></p>
 *
 * <h2>Patch Validation</h2>
 *
 * <p>Patch validation is enabled if indicated by the preprocessing configuration (see
 * {@link IPreprocessingConfig#isPatchValidationEnabled()}). The object must be an
 * {@link IPatchObject} or an illegal argument exception will be thrown.</p>
 *
 * <p>The patch object indicates which of its properties were explicitly set. Only those properties
 * will be validated. See {@link AbstractPatchTransferObject} for a patch object implementation.</p>
 *
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 * @author Laurent Prevost, laurent.prevost@lotaris.com
 */
public class BeanValidationPreprocessor implements IPreprocessor {

	@Inject
	private ValidatorFactory validatorFactory;

	private IConstraintConverter constraintConverter;
	
	@Override
	public boolean process(Object object, IPreprocessingConfig config) {
		// TODO: Add proper error message for null constraint code
		if (constraintConverter == null) {
			throw new IllegalStateException("No constraint code is configured.");
		}
		
		// Get the patch object if patch validation is enabled.
		IPatchObject patch = null;
		if (config.isPatchValidationEnabled()) {

			// Ensure it is a patch object.
			if (!(object instanceof IPatchObject)) {
				throw new IllegalArgumentException("The preprocessing configuration indicates that "
						+ "patch validation is enabled but the processed object is not an instance "
						+ "of " + IPatchObject.class);
			}

			patch = (IPatchObject) object;
		}

		// Validate the object. This may be a wrapped object (see JsonRootWrapper).
		// In that case, the first fragment of the location path will be removed below.
		final Set<ConstraintViolation<Object>> violations =
				validatorFactory.getValidator().validate(object, config.getValidationGroups());

		// No violations.
		if (violations.isEmpty()) {
			return true;
		}

		final JsonPointer pointer = new JsonPointer();

		for (ConstraintViolation<Object> violation : violations) {

			pointer.root();

			// Build a JSON pointer from the bean validation path.
			for (javax.validation.Path.Node node : violation.getPropertyPath()) {

				if (node.getIndex() != null) {
					pointer.path(node.getIndex());
				}

				if (node.getName() != null) {
					pointer.path(node.getName());
				}
			}

			// If patch validation is enabled and the patch object doesn't indicate the property as
			// set, don't validate it.
			if (patch != null && !patch.isPropertySet(pointer.fragmentAt(0))) {

				/*
				 * Note: this doesn't support deep patch validation. Only the properties of the
				 * top-level object can use it.
				 *
				 * Implementation Note: all validations are run and then errors omitted here. Bean
				 * validations provide a #validateProperty method that could be used instead, but it
				 * doesn't honor the @Valid annotation for sub-objects or sub-lists, so the solution
				 * would be incomplete.
				 */
				continue;
			}

			// Remove the first path fragment of the pointer as the object is wrapped.
			if (object instanceof IJsonWrapper) {
				pointer.shift();
			}

			addError(config.getValidationContext(), violation, pointer);
		}

		return true;
	}

	// TODO: Comment
	public void setConstraintConverter(IConstraintConverter constraintConverter) {
		this.constraintConverter = constraintConverter;
	}
	
	/**
	 * Adds an error message describing a constraint violation.
	 *
	 * @param context the validation context to add errors to
	 * @param violation the constraint violation
	 * @param pointer a JSON pointer to the invalid value in the JSON document
	 * @return an API error message
	 */
	private void addError(IValidationContext context, ConstraintViolation violation, JsonPointer pointer) {

		// extract the error code, if any
		final Class<? extends Annotation> annotationType = violation.getConstraintDescriptor().getAnnotation().annotationType();
		final IErrorCode validationCode = constraintConverter.getErrorCode(annotationType);
		final IErrorLocationType validationType = constraintConverter.getErrorLocationType(annotationType);

		// add the error to the validation context
		context.addError(pointer.toString(), validationType, validationCode, violation.getMessage());
	}
}
