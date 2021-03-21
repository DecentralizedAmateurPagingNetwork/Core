package org.dapnet.core.model;

/**
 * This interface provides a method for creating deep copies of an object.
 * 
 * @author Philipp Thiel
 *
 * @param <T> Object type
 */
@FunctionalInterface
public interface DeepCopyable<T> {

	/**
	 * Creates a deep copy of the current object. All mutable state must be copied,
	 * for immutable state using the same reference is acceptable.
	 * 
	 * @return A new copy of the current object
	 */
	T deepCopy();

}
