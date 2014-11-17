package com.lotaris.jee.validation.preprocessing;

import com.lotaris.jee.validation.preprocessing.IPreprocessingConfig;
import com.lotaris.jee.validation.preprocessing.IPreprocessor;
import com.lotaris.jee.validation.preprocessing.PreprocessingChain;
import com.lotaris.jee.validation.ApiErrorsException;
import com.lotaris.rox.annotations.RoxableTest;
import com.lotaris.rox.annotations.RoxableTestClass;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @see PreprocessingChain
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
@RoxableTestClass(tags = {"preprocessing", "preprocessingChain"})
public class PreprocessingChainUnitTest {

	private Object object;
	private List<String> calls;
	private PreprocessingChain chain;

	@Before
	public void setUp() {
		object = new Object();
		calls = new ArrayList<>();
		chain = new PreprocessingChain();
	}

	@Test
	@RoxableTest(key = "b21c0944220b")
	public void emptyPreprocessingChainShouldDoNothing() throws ApiErrorsException {
		assertTrue(chain.process(object, mockConfig()));
	}

	@Test
	@RoxableTest(key = "9d6ea8087023")
	public void preprocessingChainShouldCallPreprocessorsInOrder() throws ApiErrorsException {
		chain.add(mockPreprocessor("one", true));
		chain.add(mockPreprocessor("two", true));
		chain.add(mockPreprocessor("three", true));
		assertTrue(chain.process(object, mockConfig()));
		assertArrayEquals(new String[]{"one", "two", "three"}, calls.toArray());
	}

	@Test
	@RoxableTest(key = "f1003410e61c")
	public void preprocessingChainShouldStopAtFirstFailure() throws ApiErrorsException {
		chain.add(mockPreprocessor("one", true));
		chain.add(mockPreprocessor("two", false));
		chain.add(mockPreprocessor("three", true));
		assertFalse(chain.process(object, mockConfig()));
		assertArrayEquals(new String[]{"one", "two"}, calls.toArray());
	}

	@Test
	@RoxableTest(key = "7b79917c552f")
	public void preprocessingChainShouldThrowRuntimeExceptions() throws ApiErrorsException {

		final IPreprocessor preprocessor = mock(IPreprocessor.class);
		final IllegalStateException exception = new IllegalStateException();
		when(preprocessor.process(anyObject(), any(IPreprocessingConfig.class))).thenThrow(exception);
		chain.add(preprocessor);

		try {
			chain.process(object, mockConfig());
			fail("Preprocessing chain should have thrown the exception");
		} catch (IllegalStateException iae) {
			assertSame(exception, iae);
		}
	}

	private IPreprocessor mockPreprocessor(final String name, final boolean successful) throws ApiErrorsException {
		final IPreprocessor preprocessorMock = mock(IPreprocessor.class);
		when(preprocessorMock.process(anyObject(), any(IPreprocessingConfig.class))).then(new Answer<Boolean>() {
			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				calls.add(name);
				return successful;
			}
		});
		return preprocessorMock;
	}

	private IPreprocessingConfig mockConfig() {
		final IPreprocessingConfig configMock = mock(IPreprocessingConfig.class);
		when(configMock.getValidationGroups()).thenReturn(new Class[]{});
		return configMock;
	}
}
