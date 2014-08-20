package com.forbesdigital.jee.validation.preprocessing;

import com.forbesdigital.jee.validation.preprocessing.modifier.Trim;
import com.forbesdigital.jee.validation.preprocessing.modifier.TrimModifier;
import com.lotaris.rox.annotations.RoxableTest;
import com.lotaris.rox.annotations.RoxableTestClass;
import java.lang.reflect.Field;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

/**
 * @see ModifiersPreprocessor
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
@RoxableTestClass(tags = {"preprocessing", "modifiersPreprocessor"})
public class ModifiersPreprocessorUnitTests {

	@Mock
	private TrimModifier trimModifier;
	@Mock
	private IPreprocessingConfig config;
	@InjectMocks
	private ModifiersPreprocessor preprocessor;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		doReturn(Trim.class).when(trimModifier).getAnnotationType();
		preprocessor.configure();
	}

	@Test
	@RoxableTest(key = "5175a5e42c7d")
	public void modifiersPreprocessorShouldRunTheTrimModifier() {

		final AnnotatedTestClass app = new AnnotatedTestClass("foo");
		preprocessor.process(app, config);

		verify(trimModifier).process(same(app), argThat(isField(AnnotatedTestClass.class, "name", true)), argThat(isTrim()));
	}

	private static class AnnotatedTestClass {

		@Trim
		private String name;

		public AnnotatedTestClass(String name) {
			this.name = name;
		}
	}

	private static BaseMatcher<Trim> isTrim() {
		return new BaseMatcher<Trim>() {
			@Override
			public boolean matches(Object item) {
				return item instanceof Trim;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("trim annotation");
			}
		};
	}

	private static BaseMatcher<Field> isField(final Class declaringClass, final String name, final boolean accessible) {
		return new BaseMatcher<Field>() {
			@Override
			public boolean matches(Object item) {
				if (!(item instanceof Field)) {
					return false;
				}

				final Field field = (Field) item;
				return declaringClass == field.getDeclaringClass() && name.equals(field.getName()) && (accessible == field.isAccessible());
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("field named \"" + name + "\"");
			}
		};
	}
}
