package com.lotaris.jee.validation.preprocessing;

import java.util.ArrayList;
import java.util.List;

/**
 * Chain of {@link IPreprocessor} processes to apply to an object. You can add a preprocessor to the
 * chain with <tt>add</tt>. The chain runs all preprocessors in the order they were added. It stops
 * if any of the preprocessors indicates failure.
 *
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 * @see DefaultPreprocessingChain
 */
public class PreprocessingChain implements IPreprocessor {

	private List<IPreprocessor> processors;

	public PreprocessingChain() {
		processors = new ArrayList<>();
	}

	/**
	 * Adds a preprocessor to be run at the end of the chain.
	 *
	 * @param processor the preprocessor to add
	 * @return this updated chain
	 */
	public PreprocessingChain add(IPreprocessor processor) {
		processors.add(processor);
		return this;
	}

	@Override
	public boolean process(Object object, IPreprocessingConfig config) {

		for (IPreprocessor processor : processors) {
			if (!processor.process(object, config)) {
				return false;
			}
		}

		return true;
	}
}
