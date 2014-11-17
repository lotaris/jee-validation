package com.lotaris.jee.validation.preprocessing;

import com.lotaris.jee.validation.ApiErrorsException;

/**
 * Process that can be applied to an object before running data access and business operations.
 * Trimming field values and validations are implemented as preprocessors.
 *
 * <p>Preprocessors can be chained with a {@link PreprocessingChain}.</p>
 *
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
public interface IPreprocessor {

	/**
	 * Processes the specified object.
	 *
	 * @param object the object to process
	 * @param config configuration data for preprocessors
	 * @return true if preprocessing was successful
	 * @throws ApiErrorsException if the object is deemed invalid
	 */
	boolean process(Object object, IPreprocessingConfig config);
}
