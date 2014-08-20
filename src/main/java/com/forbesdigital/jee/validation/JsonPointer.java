package com.forbesdigital.jee.validation;

import java.util.LinkedList;

/**
 * JSON Pointer that identifies a value within a JSON document (see
 * http://tools.ietf.org/html/rfc6901).
 *
 * <p>You can add path fragments by calling {@link #path(java.lang.String)}. Remove previously added
 * fragments by calling {@link #pop()}. Call {@link #root()} to return to the root of the document.
 * All these methods can be chained.</p>
 *
 * <p><pre>
 *	JsonPointer pointer = new JsonPointer();
 *
 *	pointer.toString(); // ""
 *
 *	pointer.path("name").toString(); // "/name"
 *	pointer.pop();
 *
 *	pointer.path("children").path(0).toString(); // "/children/0"
 *	pointer.pop().path(1).toString(); // "/children/1"
 *
 *	pointer.root().toString() // ""
 * </pre></p>
 *
 * @author Simon Oulevay (simon.oulevay@lotaris.com)
 * @see http://tools.ietf.org/html/rfc6901
 */
public class JsonPointer {

	/**
	 * The individual path fragments.
	 */
	private LinkedList<String> pathFragments;

	/**
	 * Constructs a pointer (points to the root of the JSON document by default).
	 */
	public JsonPointer() {
		pathFragments = new LinkedList<>();
	}

	/**
	 * Adds the path fragments contained in the specified string. This method performs no escaping
	 * so reserved JSON Pointer characters should already be escaped.
	 *
	 * <p>For example, <tt>add("/foo/bar")</tt> adds two path fragments, <tt>"foo"</tt> and
	 * <tt>"bar"</tt>, and returns 2.</p>
	 *
	 * @param fragments the path fragments to add
	 * @return the number of fragments added
	 */
	public int add(String fragments) {
		if (fragments == null) {
			return 0;
		}

		final String[] splitFragments = fragments.replaceFirst("^\\/", "").split("\\/", -1);

		final int n = splitFragments.length;
		for (int i = 0; i < n; i++) {
			pathFragments.add(splitFragments[i]);
		}

		return n;
	}

	/**
	 * Adds a path fragment.
	 *
	 * @param fragment the path fragment
	 * @return this updated pointer
	 */
	public JsonPointer path(String fragment) {
		pathFragments.addLast(escapeFragment(fragment));
		return this;
	}

	/**
	 * Adds a path fragment representing an array index.
	 *
	 * @param index the path fragment (will be converted to a string)
	 * @return this updated pointer
	 */
	public JsonPointer path(int index) {
		pathFragments.addLast(Integer.toString(index));
		return this;
	}

	/**
	 * Removes the last path fragment of this pointer. Does nothing if it already points to the
	 * root.
	 *
	 * <p>This essentially reverts the effect of the last call to <tt>path</tt>.</p>
	 *
	 * @return this updated pointer
	 */
	public JsonPointer pop() {
		if (!pathFragments.isEmpty()) {
			pathFragments.removeLast();
		}
		return this;
	}

	/**
	 * Removes the n last path fragments of this pointer. Does nothing once it points to the root.
	 *
	 * @param n the number of path fragments to remove
	 * @return this updated pointer
	 */
	public JsonPointer pop(int n) {
		while (n >= 1 && !pathFragments.isEmpty()) {
			pathFragments.removeLast();
			n--;
		}
		return this;
	}

	/**
	 * Removes the first path fragment of this pointer. Does nothing if it already points to the
	 * root.
	 *
	 * @return this updated pointer
	 */
	public JsonPointer shift() {
		if (!pathFragments.isEmpty()) {
			pathFragments.removeFirst();
		}
		return this;
	}

	/**
	 * Removes all path fragments so that the pointer points to the root of the JSON document.
	 *
	 * @return this updated pointer
	 */
	public JsonPointer root() {
		pathFragments.clear();
		return this;
	}

	/**
	 * Indicates whether this pointer points to the root of the JSON document.
	 *
	 * @return true when the pointer is <tt>""</tt>
	 */
	public boolean isRoot() {
		return pathFragments.isEmpty();
	}

	/**
	 * Returns the path fragment at the specified index.
	 *
	 * @param index the index of the fragment to retrieve
	 * @return the path fragment
	 * @throws IndexOutOfBoundsException if the index is out of bounds (zero-based)
	 */
	public String fragmentAt(int index) {
		return pathFragments.get(index);
	}

	@Override
	public String toString() {

		final StringBuilder builder = new StringBuilder();

		for (String fragment : pathFragments) {
			builder.append("/");
			builder.append(fragment);
		}

		return builder.toString();
	}

	/**
	 * Escapes JSON pointer reserved characters.
	 *
	 * @param pathFragment a path fragment which may contain reserved characters
	 * @return the escaped path fragment
	 * @see http://tools.ietf.org/html/rfc6901#section-3
	 */
	private String escapeFragment(String pathFragment) {
		return pathFragment.replaceAll("~", "~0").replaceAll("\\/", "~1");
	}
}
