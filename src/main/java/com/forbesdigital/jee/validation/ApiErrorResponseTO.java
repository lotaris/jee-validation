package com.forbesdigital.jee.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * An API response indicating that one or multiple errors prevented the request from being
 * completed. The response has a top-level message and an associated HTTP status code that must be
 * in the 4xx or 5xx range. It may also have a list of detailed error messages.
 *
 * <p>This object implements {@link IApiResponseConfig} and can therefore be used with
 * {@link ApiResponse#configure(com.lotaris.dcc.rest.IApiResponseConfig)} to automatically set the
 * HTTP status code and body of the response.</p>
 *
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
public class ApiErrorResponseTO implements IErrorCollector {

	private List<ApiErrorTO> errors;
	@JsonIgnore
	private int httpStatusCode;
	/**
	 * A cache to allow fast error lookup by location.
	 *
	 * @see #hasErrors(java.lang.String)
	 */
	@JsonIgnore
	private Set<String> knownErrorLocations;
	/**
	 * A cache to allow fast error lookup by code.
	 *
	 * @see #hasErrors(int)
	 */
	@JsonIgnore
	private Set<Integer> knownErrorCodes;

	/**
	 * Constructs an empty error response with no error message. Use {@link #add(ApiErrorTO)} to add
	 * error messages.
	 *
	 * @param httpStatusCode the associated HTTP status code
	 * @throws IllegalArgumentException if the status code is null or not in the 4xx or 5xx range
	 */
	public ApiErrorResponseTO(int httpStatusCode) {
		if (httpStatusCode < 400 || httpStatusCode > 599) {
			throw new IllegalArgumentException("HTTP status code for an API error response must be in the 4xx or 5xx range, got " + httpStatusCode);
		}
		this.httpStatusCode = httpStatusCode;
		this.errors = new ArrayList<>();
		this.knownErrorLocations = new HashSet<>();
		this.knownErrorCodes = new HashSet<>();
	}

	/**
	 * Constructs a new error response with one error message.
	 *
	 * @param message the detail message
	 * @param locationType the type of the error
	 * @param errorCode the code identifying the error (must have a default HTTP status code)
	 * @throws IllegalArgumentException if the code's default status code is null or not in the 4xx
	 * or 5xx range
	 */
	// TODO - remove locationType from this constructor after all usages of this class are updated
	public ApiErrorResponseTO(String message, IErrorLocationType locationType, IErrorCode errorCode) {
		this(message, null, locationType, errorCode);
	}
	
	/**
	 * Constructs a new error response with one error message.
	 *
	 * @param message the detail message
	 * @param location the location of the error
	 * @param locationType the location type
	 * @param errorCode the code identifying the error (must have a default HTTP status code)
	 * @throws IllegalArgumentException if the code's default status code is null or not in the 4xx
	 * or 5xx range
	 */
	public ApiErrorResponseTO(String message, String location, IErrorLocationType locationType, IErrorCode errorCode) {
		this(errorCode.getDefaultHttpStatusCode());
		addError(new ApiErrorTO(message, location, locationType, errorCode));
	}

	private void addError(ApiErrorTO error) {

		errors.add(error);

		// store the error code for quick lookup by #hasErrors(EApiErrorCodes)
		knownErrorCodes.add(error.getCode() != null ? error.getCode().getCode() : null);

		// store the error location for quick lookup by #hasErrors(String)
		if (error.getLocation() == null) {
			knownErrorLocations.add(null);
		} else {

			// also store parent paths, e.g. for "/person/children/0/name", the following paths
			// will be stored: "/person", "/person/children", "/person/children/0", "/person/children/0/name"
			String location = error.getLocation();
			while (!location.isEmpty()) {
				knownErrorLocations.add(location);
				if (location.contains("/")) {
					location = location.replaceFirst("\\/[^\\/]*$", "");
				} else {
					break;
				}
			}
		}
	}

	@Override
	public IErrorCollector addError(IError error) {
		addError(error instanceof ApiErrorTO ? ((ApiErrorTO) error) : new ApiErrorTO(error.getMessage(), error.getLocation(), error.getLocationType(), error.getCode()));
		return this;
	}

	@Override
	public boolean hasErrors() {
		return !errors.isEmpty();
	}

	@Override
	public boolean hasErrors(String location) {
		return knownErrorLocations.contains(location);
	}

	@Override
	public boolean hasErrors(IErrorCode code) {
		return knownErrorCodes.contains(code != null ? code.getCode() : null);
	}

	//<editor-fold defaultstate="collapsed" desc="Getters & Setters">
	public int getHttpStatusCode() {
		return httpStatusCode;
	}

	/**
	 * Returns the errors added to this response, if any. If no errors were added, this method
	 * returns null. If errors were added, the returned list is unmodifiable; use <tt>#add</tt> to
	 * add errors to this response.
	 *
	 * @return the list of errors added to this response, or null if there are none
	 */
	public List<ApiErrorTO> getErrors() {
		return Collections.unmodifiableList(errors);
	}
	//</editor-fold>
}
