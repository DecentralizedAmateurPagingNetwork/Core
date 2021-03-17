package org.dapnet.core.model;

import java.util.Locale;

/**
 * Interface for objects with a (unique) name.
 * 
 * @author Philipp Thiel
 */
public interface NamedObject {
	/**
	 * Gets the name of the object.
	 * 
	 * @return Name or {@code null} if not set
	 */
	String getName();

	/**
	 * Gets the normalized name.
	 * 
	 * @return Normalized name or {@code null} if {@code getName() == null}
	 */
	default String getNormalizedName() {
		String name = getName();
		if (name != null) {
			name = name.toLowerCase(Locale.ROOT);
		}

		return name;
	}
}
