package com.forbesdigital.jee.validation;

import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lotaris.rox.annotations.RoxableTest;
import com.lotaris.rox.annotations.RoxableTestClass;
import java.io.IOException;
import org.junit.Test;

/**
 * @see AbstractPatchTransferObject
 * @author Simon Oulevay <simon.oulevay@lotaris.com>
 */
@RoxableTestClass(tags = {"validation", "abstractPatchTransferObject"})
public class AbstractPatchTransferObjectUnitTests {

	@Test
	@RoxableTest(key = "c27716a6f309")
	public void abstractPatchTransferObjectShouldMarkKeysAsSet() {

		final PatchTO transferObject = new PatchTO();
		transferObject.setFirstName("Bill"); // first name set
		transferObject.setMiddleName(null); // middle name set
		// last name not set

		assertTrue("First name should have been marked as set", transferObject.isPropertySet(PatchTO.FIRST_NAME));
		assertTrue("Middle name was set to null; should have been marked as set", transferObject.isPropertySet(PatchTO.MIDDLE_NAME));
		assertFalse("Last name was not set; should not have been marked as set", transferObject.isPropertySet(PatchTO.LAST_NAME));
	}

	@Test
	@RoxableTest(key = "70bfec897a47")
	public void abstractPatchTransferObjectShouldMarkKeysAsSetWhenDeserializedFromJson() throws IOException {

		// firstName and middleName are in JSON document, lastName is not
		final String json = "{\"firstName\": \"Bob\",\"middleName\":null}";
		final PatchTO transferObject = new ObjectMapper().readValue(json, PatchTO.class);

		assertTrue("First name should have been marked as set", transferObject.isPropertySet(PatchTO.FIRST_NAME));
		assertTrue("Middle name was null in JSON document; should have been marked as set", transferObject.isPropertySet(PatchTO.MIDDLE_NAME));
		assertFalse("Last name was not in JSON document; should not have been marked as set", transferObject.isPropertySet(PatchTO.LAST_NAME));
	}

	//<editor-fold defaultstate="collapsed" desc="Test Transfer Object Class">
	private static class PatchTO extends AbstractPatchTransferObject {

		private static final String FIRST_NAME = "firstName";
		private static final String MIDDLE_NAME = "middleName";
		private static final String LAST_NAME = "lastName";
		@JsonProperty(FIRST_NAME)
		private String firstName;
		@JsonProperty(MIDDLE_NAME)
		private String middleName;
		@JsonProperty(LAST_NAME)
		private String lastName;

		public PatchTO() {
		}

		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = markPropertyAsSet(FIRST_NAME, firstName);
		}

		public String getMiddleName() {
			return middleName;
		}

		public void setMiddleName(String middleName) {
			this.middleName = markPropertyAsSet(MIDDLE_NAME, middleName);
		}

		public String getLastName() {
			return lastName;
		}

		public void setLastName(String lastName) {
			this.lastName = markPropertyAsSet(LAST_NAME, lastName);
		}
	}
	//</editor-fold>
}
