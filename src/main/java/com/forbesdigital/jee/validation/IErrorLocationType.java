package com.forbesdigital.jee.validation;

/**
 * Identifies a specific type of error.
 *
 * @author Cristian Calugar <cristian.calugar@fortech.ro>
 */
public interface IErrorLocationType {

	/**
	 * Returns the location type of the error.
	 *
	 * @return a string type
	 */
	String getLocationType();
}
