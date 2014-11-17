package com.lotaris.jee.validation.preprocessing;

import com.lotaris.jee.validation.IPatchObject;
import com.lotaris.jee.validation.IValidationContext;
import com.lotaris.jee.validation.IValidator;
import java.util.List;
import javax.validation.groups.Default;

/**
 * Shared configuration for preprocessors.
 *
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
public interface IPreprocessingConfig {

	/**
	 * Returns the context used for validation.
	 *
	 * @return a validation context
	 */
	IValidationContext getValidationContext();

	/**
	 * Returns the group or list of groups targeted for validation. An empty list is equivalent to
	 * the {@link Default} group. This is used by the validation preprocessor.
	 *
	 * @return a list of validation groups (must not be null)
	 * @see BeanValidationPreprocessor
	 */
	Class[] getValidationGroups();

	/**
	 * Returns a list of additional validators to apply to the preprocessed object.
	 *
	 * @return a list of validators
	 * @see ValidationPreprocessor
	 */
	List<IValidator> getValidators();

	/**
	 * Whether patch validation should be used. Patch validation will only accept an
	 * {@link IPatchObject} that defines which of its properties were explicitly set. Only those
	 * properties will be validated.
	 *
	 * @return true if patch validation is enabled
	 * @see BeanValidationPreprocessor
	 */
	boolean isPatchValidationEnabled();
}
