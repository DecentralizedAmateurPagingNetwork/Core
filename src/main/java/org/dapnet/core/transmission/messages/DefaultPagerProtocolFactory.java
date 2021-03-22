package org.dapnet.core.transmission.messages;

import org.dapnet.core.model.CoreRepository;
import org.dapnet.core.model.Pager.Type;

/**
 * Default pager protocol factory implementation.
 * 
 * @author Philipp Thiel
 */
public final class DefaultPagerProtocolFactory implements PagerProtocolFactory {

	@Override
	public PagerProtocol getProtocol(Type type, CoreRepository repository) {
		switch (type) {
		case SKYPER:
			return new SkyperProtocol(repository);
		case ALPHAPOC:
			return new AlphapocProtocol(repository);
		case SWISSPHONE:
			return new SwissphoneProtocol(repository);
		case QUIX_ULTRA:
			return new QuixProtocol(repository);
		default:
			return null;
		}
	}

}
