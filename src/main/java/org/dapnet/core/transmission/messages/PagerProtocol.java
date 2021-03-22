package org.dapnet.core.transmission.messages;

import java.time.ZonedDateTime;

import org.dapnet.core.model.Activation;
import org.dapnet.core.model.Call;
import org.dapnet.core.model.News;
import org.dapnet.core.model.Pager.Type;
import org.dapnet.core.model.Rubric;

/**
 * Interface for pager protocol implementation. If a feature is not supported by
 * the implementation, the factory must be {@code null}.
 * 
 * @author Philipp Thiel
 */
public interface PagerProtocol {

	/**
	 * Gets the pager type this protocol is implementing.
	 * 
	 * @return Pager type
	 */
	Type getPagerType();

	/**
	 * Gets the call message factory.
	 * 
	 * @return Call message factory or {@code null} if not supported
	 */
	PagerMessageFactory<Call> getCallFactory();

	/**
	 * Gets the activation message factory.
	 * 
	 * @return Activation message factory or {@code null} if not supported
	 */
	PagerMessageFactory<Activation> getActivationFactory();

	/**
	 * Gets the rubric message factory.
	 * 
	 * @return Rubric message factory or {@code null} if not supported
	 */
	PagerMessageFactory<Rubric> getRubricFactory();

	/**
	 * Gets the news message factory.
	 * 
	 * @return News message factory or {@code null} if not supported
	 */
	PagerMessageFactory<News> getNewsFactory();

	/**
	 * Gets the time message factory.
	 * 
	 * @return Time message factory or {@code null} if not supported
	 */
	PagerMessageFactory<ZonedDateTime> getTimeFactory();

	/**
	 * Gets the transmitter identification message factory.
	 * 
	 * @return Transmitter identification message factory or {@code null} if not
	 *         supported
	 */
	PagerMessageFactory<TransmitterIdentification> getTransmitterIdentificationFactory();

}
