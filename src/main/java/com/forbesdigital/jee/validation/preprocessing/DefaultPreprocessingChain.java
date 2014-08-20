package com.forbesdigital.jee.validation.preprocessing;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Preprocessing chain that applies all modifiers and validations annotated on an object.
 *
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 * @see ModifiersPreprocessor
 * @see ValidationPreprocessor
 */
public class DefaultPreprocessingChain extends PreprocessingChain {

	@Inject
	private ModifiersPreprocessor modifiersProcessor;
	@Inject
	private BeanValidationPreprocessor beanValidationProcessor;
	@Inject
	private ValidationPreprocessor validationProcessor;

	@PostConstruct
	protected void buildChain() {
		add(modifiersProcessor);
		add(beanValidationProcessor);
		add(validationProcessor);
	}

	public BeanValidationPreprocessor getBeanValidationProcessor() {
		return beanValidationProcessor;
	}

	public ModifiersPreprocessor getModifiersProcessor() {
		return modifiersProcessor;
	}

	public ValidationPreprocessor getValidationProcessor() {
		return validationProcessor;
	}
}
