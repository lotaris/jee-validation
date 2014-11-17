package com.lotaris.jee.validation;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link IPatchObject} implementation. Setters in subclasses should call the
 * {@link #markPropertyAsSet(java.lang.String, java.lang.Object)} method.
 *
 * <p><pre>
 *	public class PersonTO extends AbstractPatchTransferObject {
 *
 *		public static final String FIRST_NAME = "firstName";
 *		public static final String MIDDLE_NAME = "middleName";
 *		public static final String LAST_NAME = "lastName";
 *
 *		&#64;JsonProperty(FIRST_NAME)
 *		private String firstName;
 *		&#64;JsonProperty(MIDDLE_NAME)
 *		private String middleName;
 *		&#64;JsonProperty(LAST_NAME)
 *		private String lastName;
 *
 *		public void setFirstName(String firstName) {
 *			this.firstName = markPropertyAsSet(FIRST_NAME, firstName);
 *		}
 *
 *		public void setMiddleName(String middleName) {
 *			this.middleName = markPropertyAsSet(MIDDLE_NAME, middleName);
 *		}
 *
 *		public void setFirstName(String lastName) {
 *			this.lastName = markPropertyAsSet(LAST_NAME, lastName);
 *		}
 *	}
 *
 *	PersonTO person = new PersonTO();
 *	person.setFirstName("Bob");
 *	person.setMiddleName(null);
 *
 *	assertTrue(person.isPropertySet(PersonTO.FIRST_NAME));    // first name was set
 *	assertTrue(person.isPropertySet(PersonTO.MIDDLE_NAME));   // middle name was set to null
 *	assertFalse(person.isPropertySet(PersonTO.LAST_NAME));    // last name was not set
 * </pre></p>
 *
 * @author Simon Oulevay <simon.oulevay@lotaris.com>
 */
public abstract class AbstractPatchTransferObject implements IPatchObject {

	private Set<String> setProperties;

	public AbstractPatchTransferObject() {
		setProperties = new HashSet<>();
	}

	/**
	 * Marks the specified property as set and returns the value given as the second argument. This
	 * is meant to be used as a one-liner.
	 *
	 * <p><pre>
	 *	public void setFirstName(String firstName) {
	 *		this.firstName = markPropertyAsSet(FIRST_NAME, firstName);
	 *	}
	 * </pre></p>
	 *
	 * @param <T> the type of value
	 * @param property the property to mark as set
	 * @param value the value to return
	 * @return the value
	 */
	public <T> T markPropertyAsSet(String property, T value) {
		setProperties.add(property);
		return value;
	}

	@Override
	public boolean isPropertySet(String property) {
		return setProperties.contains(property);
	}
}
