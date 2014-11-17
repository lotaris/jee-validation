package com.lotaris.jee.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	/**
	 * State objects to share between validators and with the caller.
	 */
	private Map<Class, Object> states;

	/**
	 * Constructs a new context.
	 *
	 * @param collector the object into which to collect errors
	 */
	public JsonValidationContext(IErrorCollector collector) {
		this.collector = collector;
		this.currentLocation = new JsonPointer();
		this.states = new HashMap<>();
	}

	@Override
	public IValidationContext addError(String location, IErrorLocationType type, IErrorCode code, String message, Object... messageArgs) {
		collector.addError(new ApiError(String.format(message, messageArgs), code, type, location(location)));
		return this;
	}

	@Override
	public IValidationContext addErrorAtCurrentLocation(IErrorCode code, String message, Object... messageArgs) {
		return addError("", JSON_LOCATION_TYPE, code, message, messageArgs);
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

	/**
	 * Adds the specified state object to this validation context. It can be retrieved by passing
	 * the identifying class to {@link #getState(java.lang.Class)}.
	 *
	 * @param <T> the type of state
	 * @param state the state object to register
	 * @param stateClass the class identifying the state
	 * @return this context
	 * @throws IllegalArgumentException if a state is already registered for that class
	 */
	public <T> JsonValidationContext addState(T state, Class<? extends T> stateClass) throws IllegalArgumentException {

		// only accept one state of each class
		if (this.states.containsKey(state.getClass())) {
			throw new IllegalArgumentException("A state object is already registered for class " + state.getClass().getName());
		}

		this.states.put(state.getClass(), state);

		return this;
	}

	/**
	 * Adds the specified state objects to this validation context. Each state object will be
	 * identified by its concrete class (retrieved with <tt>getClass</tt>) and can be retrieved with
	 * {@link #getState(java.lang.Class)}.
	 *
	 * @param states the state objects to register
	 * @return this context
	 * @throws IllegalArgumentException if a state is already registered for a class of one of the
	 * specified states (or there are duplicates)
	 */
	public JsonValidationContext addStates(Object... states) throws IllegalArgumentException {

		for (Object state : states) {
			addState(state, state.getClass());
		}

		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getState(Class<? extends T> stateClass) throws IllegalArgumentException {

		final Object state = states.get(stateClass);
		if (state == null) {
			throw new IllegalArgumentException("No state object registered for class " + stateClass.getName());
		}

		return (T) state;
	}
	private static final IErrorLocationType JSON_LOCATION_TYPE = new IErrorLocationType() {
		@Override
		public String getLocationType() {
			return "json";
		}
	};
}
