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
	 * Normalizes a string by converting to lower case.
	 * 
	 * @param text Text
	 * @return Normalized string or {@code null} if {@code text == null}
	 */
	static String normalize(String text) {
		if (text != null) {
			text = text.toLowerCase(Locale.ROOT);
		}

		return text;
	}
}
