package com.forbesdigital.jee.validation;

/**
 * Patch document to support partial resource modifications. Unlike JSON or other similar formats,
 * Java cannot differentiate between values that are null and values that were not set, so the patch
 * object must indicate whether a property has been explicitly set. Validation can then be
 * selectively applied only to the properties that were set, ignoring the others.
 *
 * <p>{@link AbstractPatchTransferObject} provides a default implementation.</p>
 *
 * @author Simon Oulevay <simon.oulevay@lotaris.com>
 * @see AbstractPatchTransferObject
 */
public interface IPatchObject {

	/**
	 * Indicates whether the specified property was explicitly set.
	 *
	 * @param property the property to check
	 * @return true if the property was explicitly set (to any value, including null)
	 */
	boolean isPropertySet(String property);
}
