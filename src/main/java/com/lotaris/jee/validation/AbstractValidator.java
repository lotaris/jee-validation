package com.lotaris.jee.validation;

import java.util.HashSet;
import java.util.Set;

/**
 * Basic {@link IValidator} implementation. When using this class, you will implement the
 * <tt>validate</tt> method instead of the <tt>collectErrors</tt> methods of the {@link IValidator}
 * interface. This allows the abstract validator to perform some common checks and entirely skip
 * your complex validation in some cases.
 *
 * <h2>What to implement</h2>
 *
 * <p>You must implement the <tt>validate</tt> method. A {@link IValidationContext} will be passed
 * to your method. This object will allow you to add errors if you determine that the supplied
 * object is invalid.</p>
 *
 * <h2>Skipping validation when there are previous errors already</h2>
 *
 * <p>Implementations of this class can automatically skip validation if previous errors were
 * already added for given locations. For example, you might want to skip a costly database
 * validation for a field if you know that bean validations have already failed for that field.
 * Locations must be JSON Pointers (see
 * <a href="http://tools.ietf.org/html/rfc6901">ietf.org</a>).</p>
 *
 * <p>To use this mechanism, you can apply the {@link SkipValidationOnPreviousErrors} annotation on
 * your validator class:</p>
 * <p><pre>
 *	&#x40;SkipValidationOnPreviousErrors(locations = {"/foo", "/bar"})
 *	public class MyValidator extends AbstractValidator&lt;MyClass&gt; {
 *	}
 * </pre></p>
 *
 * <p>Or you can call the <tt>skipOnPreviousErrors</tt> method:</p>
 * <p><pre>
 *	// sample call
 *	myValidator.skipOnPreviousErrors("/foo", "/bar");
 *
 *	// you might typically do this when using your validator inside another validator
 *	public class ParentValidator extends AbstractValidator&lt;MyClass&gt; {
 *
 *		&#x40;Override
 *		protected void validate(MyClass object, IValidationContext context) {
 *
 *			// dynamically configure which error locations will cause your validator to be skipped
 *			context.validateObject(object.getValue(), "/subPath", new MyValidator().skipOnPreviousErrors("/baz"));
 *		}
 *	}
 * </pre></p>
 *
 * <p>If you require more complex logic to determine the list of error locations, you can override
 * <tt>getPreviousErrorLocations</tt>:</p>
 * <p><pre>
 *	public class MyValidator extends AbstractValidator&lt;MyClass&gt; {
 *
 *		&#x40;Override
 *		protected Set&lt;String&gt; getPreviousErrorLocations(IValidationContext context) {
 *			return fillSet("/complex" + "/location", "/and" + "/another" + "/one");
 *		}
 *	}
 * </pre></p>
 *
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
public abstract class AbstractValidator<T> implements IValidator<T> {

	private Set<String> previousErrorLocations;

	public AbstractValidator() {
		previousErrorLocations = new HashSet<>(5);
		fillPreviousErrorLocationsFromAnnotations(previousErrorLocations);
	}

	/**
	 * Validates the specified object.
	 *
	 * <p>Note: this will not be called if the validation is skipped due to previous errors (see
	 * class definition).</p>
	 *
	 * @param object the object to validate
	 * @param context the context used to add and keep track of errors during validation
	 */
	protected abstract void validate(T object, IValidationContext context);

	@Override
	public void collectErrors(T object, IValidationContext context) {

		// don't do anything if previous errors are found
		for (final String relativeLocation : getPreviousErrorLocations(context)) {
			if (context.hasErrors(context.location(relativeLocation))) {
				return;
			}
		}

		validate(object, context);
	}

	/**
	 * Returns all locations that should cause validation to be skipped if there are previous errors
	 * at these locations. See class definition. No locations are checked by default.
	 *
	 * @param context the validation context that you can use to build relative locations
	 * @return a set of locations to check for previous errors
	 */
	public Set<String> getPreviousErrorLocations(IValidationContext context) {
		return previousErrorLocations;
	}

	/**
	 * Sets the locations that should cause validation to be skipped if there are previous errors at
	 * these locations. See class definition.
	 *
	 * @param context if given, locations will be relative to that context
	 * @param locations the locations to check for previous errors
	 * @return this validator
	 */
	public AbstractValidator<T> skipOnPreviousErrors(String... locations) {
		fillSet(previousErrorLocations, locations);
		return this;
	}

	protected final void fillPreviousErrorLocationsFromAnnotations(Set<String> locations) {

		final SkipValidationOnPreviousErrors annotation = getClass().getAnnotation(SkipValidationOnPreviousErrors.class);
		if (annotation != null) {
			fillSet(locations, annotation.locations());
		}

		final SkipValidationOnPreviousErrorsAtCurrentLocation currentLocationAnnotation = getClass().getAnnotation(SkipValidationOnPreviousErrorsAtCurrentLocation.class);
		if (currentLocationAnnotation != null) {
			fillSet(locations, "");
		}
	}

	protected Set<String> fillSet(String... strings) {
		return fillSet(new HashSet<String>(), strings);
	}

	protected Set<String> fillSet(Set<String> set, String... strings) {

		final int n = strings.length;
		for (int i = 0; i < n; i++) {
			set.add(strings[i]);
		}

		return set;
	}
}
