package com.lotaris.jee.validation.preprocessing;

import com.lotaris.jee.validation.IValidationContext;
import com.lotaris.jee.validation.IValidator;

/**
 * Applies all validators returned by {@link IPreprocessingConfig#getValidators()} to the processed
 * object. If the object is invalid, errors are collected into the {@link ApiErrorResponse}
 * returned by {@link IPreprocessingConfig#getErrors()}.
 *
 * <p>Note that the validators are guaranteed to be executed in order.</p>
 *
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
public class ValidationPreprocessor implements IPreprocessor {

	@Override
	@SuppressWarnings("unchecked")
	public boolean process(Object object, IPreprocessingConfig config) {

		// build an initial validation context (its current location is the root of the JSON document)
		final IValidationContext context = config.getValidationContext();

		// collect errors for each validator
		for (IValidator validator : config.getValidators()) {
			validator.collectErrors(object, context);
		}

		return true;
	}
}
