package org.dapnet.core.transmission.messages;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;

import org.dapnet.core.model.Activation;
import org.dapnet.core.model.Call;
import org.dapnet.core.model.CoreRepository;
import org.dapnet.core.model.News;
import org.dapnet.core.model.Pager.Type;
import org.dapnet.core.model.Rubric;

/**
 * Skyper pager protocol implementation.
 * 
 * @author Philipp Thiel
 */
class SkyperProtocol implements PagerProtocol {

	private static final Charset PAGER_CHARSET = new SkyperCharset();
	private final PagerMessageFactory<Call> callFactory;
	private final PagerMessageFactory<Activation> activationFactory;
	private final PagerMessageFactory<Rubric> rubricFactory;
	private final PagerMessageFactory<News> newsFactory;
	private final PagerMessageFactory<ZonedDateTime> timeFactory;

	/**
	 * Constructs a new Skyper pager protocol instance.
	 * 
	 * @param repository Repository to use
	 */
	public SkyperProtocol(CoreRepository repository, String activationCode) {
		callFactory = new SkyperCallMessageFactory(repository, SkyperProtocol::encode);
		activationFactory = new SkyperActivationMessageFactory(getActivationCode(activationCode));
		rubricFactory = new SkyperRubricMessageFactory(SkyperProtocol::encode);
		newsFactory = new SkyperNewsMessageFactory(repository, SkyperProtocol::encode);
		timeFactory = new SkyperTimeMessageFactory(false);
	}

	@Override
	public Type getPagerType() {
		return Type.SKYPER;
	}

	@Override
	public PagerMessageFactory<Call> getCallFactory() {
		return callFactory;
	}

	@Override
	public PagerMessageFactory<Activation> getActivationFactory() {
		return activationFactory;
	}

	@Override
	public PagerMessageFactory<Rubric> getRubricFactory() {
		return rubricFactory;
	}

	@Override
	public PagerMessageFactory<News> getNewsFactory() {
		return newsFactory;
	}

	@Override
	public PagerMessageFactory<ZonedDateTime> getTimeFactory() {
		return timeFactory;
	}

	@Override
	public PagerMessageFactory<TransmitterIdentification> getTransmitterIdentificationFactory() {
		return null; // Not supported
	}

	private static String encode(String text) {
		if (text != null) {
			byte[] encoded = text.getBytes(PAGER_CHARSET);
			return new String(encoded, StandardCharsets.US_ASCII);
		} else {
			return null;
		}
	}

	private static String[] getActivationCode(String activationCode) {
		if (activationCode != null) {
			return activationCode.split(",");
		} else {
			throw new NullPointerException("Activation code must not be null.");
		}
	}

}
