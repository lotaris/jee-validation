package com.lotaris.jee.validation.preprocessing;

import com.lotaris.jee.validation.preprocessing.IPreprocessingConfig;
import com.lotaris.jee.validation.preprocessing.DefaultPreprocessingChain;
import com.lotaris.jee.validation.preprocessing.ValidationPreprocessor;
import com.lotaris.jee.validation.preprocessing.ModifiersPreprocessor;
import com.lotaris.jee.validation.preprocessing.BeanValidationPreprocessor;
import com.lotaris.rox.annotations.RoxableTest;
import com.lotaris.rox.annotations.RoxableTestClass;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @see DefaultPreprocessingChain
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
@RoxableTestClass(tags = {"preprocessing", "defaultPreprocessingChain"})
public class DefaultPreprocessingChainUnitTest {

	@Mock
	private ModifiersPreprocessor modifiersPreprocessor;
	@Mock
	private BeanValidationPreprocessor beanValidationPreprocessor;
	@Mock
	private ValidationPreprocessor validationPreprocessor;
	@InjectMocks
	private DefaultPreprocessingChain chain;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		chain.buildChain();
	}

	@Test
	@RoxableTest(key = "fc75ba4bd4a8")
	public void defaultPreprocessingChainShouldRunModifiersThenBeanValidationsThenValidations() {

		// make sure all three preprocessors return true so the chain can be completed
		when(modifiersPreprocessor.process(anyObject(), any(IPreprocessingConfig.class))).thenReturn(true);
		when(beanValidationPreprocessor.process(anyObject(), any(IPreprocessingConfig.class))).thenReturn(true);
		when(validationPreprocessor.process(anyObject(), any(IPreprocessingConfig.class))).thenReturn(true);

		final Object object = new Object();
		final IPreprocessingConfig config = mock(IPreprocessingConfig.class);
		assertTrue(chain.process(object, config));

		// check that the three preprocessors were called in the correct order
		final InOrder inOrder = inOrder(modifiersPreprocessor, beanValidationPreprocessor, validationPreprocessor);
		inOrder.verify(modifiersPreprocessor).process(object, config);
		inOrder.verify(beanValidationPreprocessor).process(object, config);
		inOrder.verify(validationPreprocessor).process(object, config);
	}
}
