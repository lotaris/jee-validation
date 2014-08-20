package com.forbesdigital.jee.validation.preprocessing.modifier;

import static org.junit.Assert.*;

import com.lotaris.rox.annotations.RoxableTest;
import com.lotaris.rox.annotations.RoxableTestClass;
import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
@RoxableTestClass(tags = "modifiers")
public class TrimModifierUnitTest {

	private TrimModifier trim;
	private TestObject testObject;
	private Field valueField;

	@Before
	public void setUp() throws NoSuchFieldException {
		trim = new TrimModifier();
		testObject = new TestObject();
		valueField = testObject.getClass().getDeclaredField("value");
		valueField.setAccessible(true);
	}

	@Test
	@RoxableTest(key = "ac05669354d2")
	public void trimModifierShouldTrimLeadingAndTrailingWhitespace() {
		testObject.value = " \n  foo		";
		trim.process(testObject, valueField, mockAnnotation(false));
		assertEquals(testObject.value, "foo");
	}

	@Test
	@RoxableTest(key = "e8eeabdc0a60")
	public void trimModifierShouldCollapseWhitespace() {
		testObject.value = "foo		\n bar";
		trim.process(testObject, valueField, mockAnnotation(true));
		assertEquals(testObject.value, "foo bar");
	}

	@Test
	@RoxableTest(key = "8397e7b4b1ea")
	public void trimModifierShouldNotCollapseWhitespaceIfDisabled() {
		testObject.value = "foo		\n bar";
		trim.process(testObject, valueField, mockAnnotation(false));
		assertEquals(testObject.value, "foo		\n bar");
	}

	@Test
	@RoxableTest(key = "92537c9b3100")
	public void trimModifierShouldIgnoreNullValue() {
		testObject.value = null;
		trim.process(testObject, valueField, mockAnnotation(false));
		assertEquals(testObject.value, null);
	}

	private Trim mockAnnotation(boolean collapseWhitespace) {
		final Trim annotationMock = Mockito.mock(Trim.class);
		Mockito.when(annotationMock.collapseWhitespace()).thenReturn(collapseWhitespace);
		return annotationMock;
	}

	public static class TestObject {

		private String value;
	}
}
