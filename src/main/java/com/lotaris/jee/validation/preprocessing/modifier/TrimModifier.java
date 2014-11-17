package com.lotaris.jee.validation.preprocessing.modifier;

import com.lotaris.jee.validation.AbstractModifier;
import com.lotaris.jee.validation.IModifier;
import java.lang.reflect.Field;
import org.slf4j.LoggerFactory;

/**
 * Modifier implementation for {@link Trim}. It trims the value of the specified object field.
 * Nothing is done if the value is null. Whitespace inside the string is collapsed only if the
 * corresponding switch is set on the annotation (true by default, see {@link Trim#collapseWhitespace()}).
 *
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
public class TrimModifier extends AbstractModifier<Trim> implements IModifier<Trim> {
	public TrimModifier() {
		super(Trim.class);
	}

	@Override
	public void process(Object object, Field field, Trim annotation) {
		try {
			trim(object, field, annotation);
			LoggerFactory.getLogger(TrimModifier.class).trace("Trimmed field {}", field.getName());
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			LoggerFactory.getLogger(TrimModifier.class).warn("Could not trim field " + field.getName(), ex);
		}
	}

	private void trim(Object object, Field field, Trim annotation) throws IllegalArgumentException, IllegalAccessException {
		final Object value = field.get(object);
		if (value != null) {
			field.set(object, trimValue(value.toString(), annotation));
		}
	}

	private String trimValue(String value, Trim annotation) {

		// trim left and right
		value = value.trim();

		// replace all whitespace by one space
		if (annotation.collapseWhitespace()) {
			value = value.replaceAll("\\s+", " ");
		}

		return value;
	}
}
