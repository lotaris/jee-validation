package com.lotaris.jee.validation;

import com.lotaris.jee.validation.JsonPointer;
import com.lotaris.rox.annotations.RoxableTest;
import com.lotaris.rox.annotations.RoxableTestClass;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * @see JsonPointer
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 */
@RoxableTestClass(tags = {"api", "jsonPointer"})
public class JsonPointerUnitTest {

	private JsonPointer pointer;

	@Before
	public void setUp() {
		pointer = new JsonPointer();
	}

	@Test
	@RoxableTest(key = "73aa88204ef7")
	public void jsonPointerShouldPointToDocumentRootByDefault() {
		assertEquals("", pointer.toString());
	}

	@Test
	@RoxableTest(key = "2d1f65119a63")
	public void jsonPointerShouldIndicateRootByDefault() {
		assertTrue(pointer.isRoot());
	}

	@Test
	@RoxableTest(key = "14a693fbdaae")
	public void jsonPointerShouldAddPathFragments() {
		assertEquals("/one", pointer.path("one").toString());
		assertEquals("/one/two", pointer.path("two").toString());
	}

	@Test
	@RoxableTest(key = "6b1e4d1d75a2")
	public void jsonPointerShouldAddIndexAsPathFragment() {
		assertEquals("/4", pointer.path(4).toString());
		assertEquals("/4/2", pointer.path(2).toString());
	}

	@Test
	@RoxableTest(key = "4432c83334e6")
	public void jsonPointerShouldEscapePathFragments() {
		assertEquals("/one~1two", pointer.path("one/two").toString());
		assertEquals("/one~1two/~1~0~1/three", pointer.path("/~/").path("three").toString());
	}

	@Test
	@RoxableTest(key = "ac1a87258e22")
	public void jsonPointerShouldPopPathFragments() {
		pointer.path("one").path("two").path("three");
		assertEquals("/one/two", pointer.pop().toString());
		assertEquals("/one", pointer.pop().toString());
		assertEquals("", pointer.pop().toString());
	}

	@Test
	@RoxableTest(key = "442e317865cc")
	public void jsonPointerShouldNotThrowWhenPoppingWhileEmpty() {
		pointer.pop();
		assertEquals("", pointer.toString());
	}

	@Test
	@RoxableTest(key = "b731b9f53248")
	public void jsonPointerShouldAddMultiplePathFragments() {

		int n = pointer.add("/one/two");
		assertEquals(2, n);
		assertEquals("/one/two", pointer.toString());

		n = pointer.add("/three/four/five");
		assertEquals(3, n);
		assertEquals("/one/two/three/four/five", pointer.toString());
	}

	@Test
	@RoxableTest(key = "ee872ba43e50")
	public void jsonPointerShouldAddNoPathFragmentsWithoutThrowing() {
		assertEquals(0, pointer.add(null));
		assertEquals("", pointer.toString());
	}

	@Test
	@RoxableTest(key = "26dfbf351452")
	public void jsonPointerShouldPopMultiplePathFragments() {
		pointer.path("one").path("two").path("three").pop(2);
		assertEquals("/one", pointer.toString());
	}

	@Test
	@RoxableTest(key = "1a542fe66dbd")
	public void jsonPointerShouldPopNoPathFragmentsWithoutThrowing() {
		assertEquals("/one/two", pointer.path("one").path("two").pop(0).toString());
	}

	@Test
	@RoxableTest(key = "b0691b10066a")
	public void jsonPointerShouldNotThrowWhenPoppingMorePathFragmentsThanItContains() {
		pointer.path("one").pop(42);
		assertEquals("", pointer.toString());
	}

	@Test
	@RoxableTest(key = "7f78f047c0e1")
	public void jsonPointerShouldGoBackToDocumentRoot() {
		final JsonPointer testPointer = pointer.path("one").path("two").root();
		assertTrue(testPointer.isRoot());
		assertEquals("", testPointer.toString());
	}

	@Test
	@RoxableTest(key = "9df3a8d570f0")
	public void jsonPointerShouldReturnIndividualPathFragments() {
		pointer.path("one").path(2).path("three");
		assertEquals("one", pointer.fragmentAt(0));
		assertEquals("2", pointer.fragmentAt(1));
		assertEquals("three", pointer.fragmentAt(2));
	}

	@Test
	@RoxableTest(key = "472560432a90")
	public void jsonPointerShouldThrowIndexOutOfBoundsExceptionForUnknownFramentIndices() {

		pointer.path("one").path("two");

		try {
			pointer.fragmentAt(-1);
			fail("IndexOutOfBoundsException should have been thrown trying to access path fragment at index -1");
		} catch (IndexOutOfBoundsException ioobe) {
			// success
		}

		try {
			pointer.fragmentAt(2);
			fail("IndexOutOfBoundsException should have been thrown trying to access path fragment at index 2 in a pointer with two fragments");
		} catch (IndexOutOfBoundsException ioobe) {
			// success
		}
	}

	@Test
	@RoxableTest(key = "39a430f615c9")
	public void jsonPointerShouldBeAbleToRebuildItself() {
		final String pointerString = pointer.path("one").path(2).path("three").toString();
		assertEquals(3, pointer.root().add(pointerString));
		assertEquals("/one/2/three", pointer.toString());
	}
}
