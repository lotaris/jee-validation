package com.forbesdigital.jee.validation;

import java.util.List;

/**
 * Control object responsible for keeping track of errors and their location during validation.
 *
 * <p>The context acts as an error collector which can be used to add errors and check for previous
 * errors as validation progresses. Use the <tt>addError</tt> and <tt>hasErrors</tt> methods.</p>
 *
 * <p>It also keeps track of the current location, allowing to validate nested structures. Calling
 * <tt>validateObject(s)</tt> will perform the supplied validation at a given location relative to
 * the current location. The current location a {@link JsonPointer} initially pointing to the root
 * of the JSON document. Path fragments are added and popped as the object structure is traversed
 * with <tt>validateObject(s)</tt> methods.
 *
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 * @see IValidator
 */
public class JsonValidationContext implements IValidationContext {

	/**
	 * The error response into which errors are collected.
	 */
	private IErrorCollector collector;
	/**
	 * JSON Pointer indicating the current location in the JSON document. The <tt>location</tt>
	 * method can generate absolute location strings relative to this current location.
	 *
	 * @see http://tools.ietf.org/html/rfc6901
	 */
	private JsonPointer currentLocation;

	public JsonValidationContext(IErrorCollector collector) {
		this.collector = collector;
		this.currentLocation = new JsonPointer();
	}

	@Override
	public IValidationContext addError(String location, IErrorLocationType type, IErrorCode code, String message, Object... messageArgs) {
		collector.addError(new ApiError(String.format(message, messageArgs), code, type, location(location)));
		return this;
	}

	@Override
	public IValidationContext addErrorAtCurrentLocation(IErrorLocationType type, IErrorCode code, String message, Object... messageArgs) {
		return addError("", type, code, message, messageArgs);
	}

	@Override
	public boolean hasErrors() {
		return collector.hasErrors();
	}

	@Override
	public boolean hasErrors(String location) {
		return collector.hasErrors(location);
	}

	@Override
	public boolean hasErrors(IErrorCode code) {
		return collector.hasErrors(code);
	}

	@Override
	public String location(String pathFragments) {
		if (pathFragments == null) {
			return null;
		} else if ("".equals(pathFragments)) {
			return currentLocation.toString();
		}

		final int n = currentLocation.add(pathFragments);
		final String location = currentLocation.toString();
		currentLocation.pop(n);
		return location;
	}

	@Override
	public <T> IValidationContext validateObject(T object, String relativeLocation, IValidator<T> validator) {

		final int numberOfPathFragments = "".equals(relativeLocation) ? 0 : currentLocation.add(relativeLocation);
		validator.collectErrors(object, this);
		currentLocation.pop(numberOfPathFragments);

		return this;
	}

	@Override
	public <T> IValidationContext validateObjects(List<T> objects, String relativeLocation, IValidator<T> validator) {
		if (objects == null) {
			return this;
		}

		final int numberOfPathFragments = "".equals(relativeLocation) ? 0 : currentLocation.add(relativeLocation);

		final int n = objects.size();
		for (int i = 0; i < n; i++) {
			currentLocation.path(i);
			validator.collectErrors(objects.get(i), this);
			currentLocation.pop();
		}

		currentLocation.pop(numberOfPathFragments);

		return this;
	}

	@Override
	public <T> IValidationContext validateObjectOrList(SingleObjectOrList<T> singleObjectOrList, String relativeLocation, IValidator<T> validator) {
		if (singleObjectOrList.isSingleObject()) {
			return validateObject(singleObjectOrList.getSingleObject(), relativeLocation, validator);
		} else {
			return validateObjects(singleObjectOrList.getList(), relativeLocation, validator);
		}
	}
}
