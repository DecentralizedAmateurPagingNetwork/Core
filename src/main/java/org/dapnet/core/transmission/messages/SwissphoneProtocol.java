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
import org.dapnet.core.transmission.messages.PagerMessage.SubAddress;

/**
 * Swissphone pager protocol implementation.
 * 
 * @author Philipp Thiel
 */
class SwissphoneProtocol implements PagerProtocol {

	// TODO Use proper charset
	private static final Charset PAGER_CHARSET = new SkyperCharset();
	private final PagerMessageFactory<Call> callFactory;
	private final PagerMessageFactory<News> newsFactory;
	private final PagerMessageFactory<ZonedDateTime> timeFactory;
	private final PagerMessageFactory<TransmitterIdentification> idFactory;

	/**
	 * Constructs a new Alphapoc pager protocol instance.
	 * 
	 * @param repository Repository to use
	 */
	public SwissphoneProtocol(CoreRepository repository) {
		callFactory = new AlphanumCallMessageFactory(repository, SwissphoneProtocol::encode, Type.SWISSPHONE,
				SubAddress.ADDR_D);
		newsFactory = new RicNewsMessageFactory(repository, SwissphoneProtocol::encode);
		timeFactory = new SwissphoneTimeMessageFactory();
		idFactory = new TransmitterIdentificationMessageFactory();
	}

	@Override
	public Type getPagerType() {
		return Type.SWISSPHONE;
	}

	@Override
	public PagerMessageFactory<Call> getCallFactory() {
		return callFactory;
	}

	@Override
	public PagerMessageFactory<Activation> getActivationFactory() {
		return null; // No activation required
	}

	@Override
	public PagerMessageFactory<Rubric> getRubricFactory() {
		return null; // Rubrics not supported
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
		return idFactory;
	}

	private static String encode(String text) {
		if (text != null) {
			byte[] encoded = text.getBytes(PAGER_CHARSET);
			return new String(encoded, StandardCharsets.US_ASCII);
		} else {
			return null;
		}
	}

}
