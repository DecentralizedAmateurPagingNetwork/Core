package org.dapnet.core.transmission.messages;

import java.util.Collection;

/**
 * This interface defines a factory for pager messages.
 * 
 * @author Philipp Thiel
 *
 * @param <T> Object type
 */
@FunctionalInterface
public interface PagerMessageFactory<T> {

	/**
	 * Constructs one or more pager messages for the given payload. The result can
	 * be {@code null} or empty if message creation is not supported for the given
	 * object. The method should throw an exception in case of critical errors
	 * though.
	 * 
	 * @param payload Message payload
	 * @return Pager messages
	 */
	Collection<PagerMessage> createMessage(T payload);

}
