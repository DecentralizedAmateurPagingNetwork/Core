package org.dapnet.core.model;

import java.util.Set;
import java.util.TreeSet;

/**
 * This class contains static utility methods related to the models.
 * 
 * @author Philipp Thiel
 */
public final class ModelUtils {

	private ModelUtils() {
	}

	/**
	 * Copies the given set of strings.
	 * 
	 * @param other Set to copy from
	 * @return Copied set or {@code null} if given set is {@code null}
	 */
	public static Set<String> copyStringSet(Set<String> other) {
		Set<String> result = null;

		if (other != null) {
			result = new TreeSet<>(other);
		}

		return result;
	}

}
