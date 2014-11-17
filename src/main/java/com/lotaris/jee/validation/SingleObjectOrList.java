package com.lotaris.jee.validation;

import java.util.List;

/**
 * Object that can represent either a single object or a list, used for operations that can handle
 * both.
 *
 * @author Simon Oulevay <simon.oulevay@lotaris.com>
 */
public interface SingleObjectOrList<T> {

	/**
	 * Indicates whether this object represents a single object or a list.
	 *
	 * @return true if this object is a single object
	 */
	boolean isSingleObject();

	/**
	 * Returns this object as a single object.
	 *
	 * @return the single object, or null if this object represents a list
	 */
	T getSingleObject();

	/**
	 * Returns this object as a list.
	 *
	 * @return the list, or null if this object represents a single object
	 */
	List<T> getList();
}
