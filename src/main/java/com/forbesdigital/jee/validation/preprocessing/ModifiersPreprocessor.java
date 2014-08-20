package com.forbesdigital.jee.validation.preprocessing;

import com.forbesdigital.jee.validation.preprocessing.modifier.TrimModifier;
import com.forbesdigital.jee.validation.IModifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.slf4j.LoggerFactory;

/**
 * Applies all modifier annotations to the processed object. See {@link IModifier}.
 *
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
public class ModifiersPreprocessor implements IPreprocessor {

	/**
	 * Cache of preprocessing annotations for each class and their fields.
	 */
	private static Map<Class, Map<Field, Set<Class<? extends Annotation>>>> cache;

	static {
		cache = new HashMap<>();
	}

	/**
	 * Fills the annotation cache for the specified class. This scans all fields of the class and
	 * checks if they have modifier annotations. This information is saved in a synchronized static
	 * cache.
	 *
	 * @param objectClass the class to scan for preprocessing annotations
	 * @param processors the map of registered preprocessors (the map key is the annotation type)
	 */
	private synchronized static void fillCache(Class objectClass, Map<Class<? extends Annotation>, IModifier> processors) {

		if (cache.containsKey(objectClass)) {
			return;
		}

		final Map<Field, Set<Class<? extends Annotation>>> classCache = new HashMap<>();
		cache.put(objectClass, classCache);

		// for each field...
		for (Field field : getAllFields(objectClass)) {

			final Set<Class<? extends Annotation>> fieldCache = new HashSet<>();

			// for each annotation on that field...
			for (Annotation annotation : field.getAnnotations()) {

				// cache the annotation type if there is a registered preprocessor for it
				final Class<? extends Annotation> annotationType = annotation.annotationType();
				if (processors.containsKey(annotationType)) {
					fieldCache.add(annotationType);
				}
			}

			if (!fieldCache.isEmpty()) {
				classCache.put(field, fieldCache);
			}
		}
	}

	/**
	 * Returns the declared and inherited fields of the specified class.
	 *
	 * @param type the class whose fields to list
	 * @return a list of fields
	 */
	private static List<Field> getAllFields(Class type) {
		final List<Field> fields = new ArrayList<>();
		for (Class<?> c = type; c != null; c = c.getSuperclass()) {
			fields.addAll(Arrays.asList(c.getDeclaredFields()));
		}
		return fields;
	}
	/**
	 * The map of preprocessors by annotation type.
	 */
	private Map<Class<? extends Annotation>, IModifier> processorsCache;
	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="Manual Injections">
	@Inject
	protected TrimModifier trimProcessor;
	//</editor-fold>

	public ModifiersPreprocessor() {
		processorsCache = new HashMap<>();
	}

	//<editor-fold defaultstate="collapsed" desc="Preprocessor Cache">
	/**
	 * Registers all preprocessors injected into this class.
	 */
	@PostConstruct
	protected void configure() {

		// scan all fields
		for (Field field : getClass().getDeclaredFields()) {

			// if it's a preprocessor, register it
			if (IModifier.class.isAssignableFrom(field.getType())) {

				try {
					field.setAccessible(true);
					final IModifier processor = (IModifier) field.get(this);
					processorsCache.put(processor.getAnnotationType(), processor);
				} catch (IllegalArgumentException | IllegalAccessException ex) {
					LoggerFactory.getLogger(ModifiersPreprocessor.class) .error("Could not register pre-processor for field " + field.getName());
				}
			}
		}
	}
	//</editor-fold>

	@Override
	public boolean process(Object object, IPreprocessingConfig config) {

		fillCache(object.getClass(), processorsCache);

		// for each field and their preprocessing annotations...
		for (Map.Entry<Field, Set<Class<? extends Annotation>>> entry : cache.get(object.getClass()).entrySet()) {

			try {
				// process the field
				processField(object, entry.getKey(), entry.getValue());
			} catch (NoSuchFieldException ex) {
				LoggerFactory.getLogger(ModifiersPreprocessor.class) .warn("Field " + entry.getKey() + " should have been cached", ex);
			}
		}

		return true;
	}

	private void processField(Object object, Field field, Set<Class<? extends Annotation>> annotationTypes) throws NoSuchFieldException {

		field.setAccessible(true);
		for (Class<? extends Annotation> annotationType : annotationTypes) {

			// run the preprocessor for the annotation
			final Annotation annotation = field.getAnnotation(annotationType);
			processorsCache.get(annotationType).process(object, field, annotation);
		}
	}
}
