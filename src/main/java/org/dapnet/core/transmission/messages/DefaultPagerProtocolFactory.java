package org.dapnet.core.transmission.messages;

import org.dapnet.core.Settings;
import org.dapnet.core.model.CoreRepository;
import org.dapnet.core.model.Pager.Type;

/**
 * Default pager protocol factory implementation.
 * 
 * @author Philipp Thiel
 */
public final class DefaultPagerProtocolFactory implements PagerProtocolFactory {

	@Override
	public PagerProtocol getProtocol(Type type, Settings settings, CoreRepository repository) {
		switch (type) {
		case SKYPER:
			return createSkyperProtocol(settings, repository);
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

	private PagerProtocol createSkyperProtocol(Settings settings, CoreRepository repository) {
		if (settings == null) {
			throw new NullPointerException("Settings must not be null.");
		}

		String activationCode = settings.getTransmissionSettings().getPagingProtocolSettings().getActivationCode();
		return new SkyperProtocol(repository, activationCode);
	}

}
