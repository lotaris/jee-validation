package com.lotaris.jee.validation;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Detailed error message concerning a JSON document submitted by an API client.
 *
 * <p>The location should be a JSON Pointer (see
 * http://tools.ietf.org/html/rfc6901) indicating which value of the JSON
 * document is invalid.</p>
 *
 * <p>The code identifies the error type (e.g. invalid string length). It can be
 * used to look up a translation.</p>
 *
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
public class ApiError implements IError {

	private String message;
	@JsonIgnore
	private IErrorCode code;
	@JsonIgnore
	private IErrorLocationType locationType;
	private String location;

	//<editor-fold defaultstate="collapsed" desc="Constructors">
	public ApiError(String message, IErrorCode code) {
		this.message = message;
		this.code = code;
	}

	public ApiError(String message, IErrorCode code, IErrorLocationType locationType, String location) {
		this.message = message;
		this.code = code;
		this.locationType = locationType;
		this.location = location;
	}
	//</editor-fold>

	@JsonProperty("code")
	public Integer getNumericCode() {
		return code != null ? code.getCode() : null;
	}

	@JsonProperty("locationType")
	public String getLocationTypeAsString() {
		return locationType != null ? locationType.getLocationType(): null;
	}

	//<editor-fold defaultstate="collapsed" desc="Getters & Setters">
	@Override
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public IErrorCode getCode() {
		return code;
	}

	public void setCode(IErrorCode code) {
		this.code = code;
	}

	@Override
	public IErrorLocationType getLocationType() {
		return locationType;
	}

	public void setLocationType(IErrorLocationType locationType) {
		this.locationType = locationType;
	}

	@Override
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	//</editor-fold>
}
