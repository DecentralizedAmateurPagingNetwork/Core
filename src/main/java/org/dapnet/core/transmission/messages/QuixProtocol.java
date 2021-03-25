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
 * Quix Ultra pager protocol implementation.
 * 
 * @author Philipp Thiel
 */
class QuixProtocol implements PagerProtocol {

	// TODO Use proper charset
	private static final Charset PAGER_CHARSET = new SkyperCharset();
	private final PagerMessageFactory<Call> callFactory;
	private final PagerMessageFactory<TransmitterIdentification> idFactory;

	/**
	 * Constructs a new Quix Ultra pager protocol instance.
	 * 
	 * @param repository Repository to use
	 */
	public QuixProtocol(CoreRepository repository) {
		callFactory = new AlphanumCallMessageFactory(repository, QuixProtocol::encode, Type.QUIX_ULTRA,
				SubAddress.ADDR_C);
		idFactory = new TransmitterIdentificationMessageFactory();
	}

	@Override
	public Type getPagerType() {
		return Type.QUIX_ULTRA;
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
		return null; // TODO Rubrics not yet supported
	}

	@Override
	public PagerMessageFactory<News> getNewsFactory() {
		return null; // TODO News not yet supported
	}

	@Override
	public PagerMessageFactory<ZonedDateTime> getTimeFactory() {
		return null; // TODO Time not yet supported
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
