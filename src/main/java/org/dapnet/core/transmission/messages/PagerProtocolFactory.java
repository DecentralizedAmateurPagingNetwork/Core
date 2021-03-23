package org.dapnet.core.transmission.messages;

import org.dapnet.core.Settings;
import org.dapnet.core.model.CoreRepository;
import org.dapnet.core.model.Pager.Type;

/**
 * Interface for pager protocol factories.
 * 
 * @author Philipp Thiel
 */
@FunctionalInterface
public interface PagerProtocolFactory {

	/**
	 * Gets a pager protocol implementation for the given pager type.
	 * 
	 * @param type       Pager type
	 * @param settings   Core settings to use
	 * @param repository Core repository to use
	 * @return Pager protocol instance for the given pager type or {@code null} if
	 *         the pager type is not supported
	 */
	PagerProtocol getProtocol(Type type, Settings settings, CoreRepository repository);

}
