package org.dapnet.core.transmission;

import java.util.List;
import java.util.Objects;

import org.dapnet.core.transmission.TransmissionSettings.PagingProtocolSettings;
import org.dapnet.core.transmission.TransmitterClient.Message;
import org.dapnet.core.transmission.messages.PagerMessage;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

/**
 * Encodes a {@link PagerMessage} into string.
 * 
 * @author Philipp Thiel
 */
@Sharable
class MessageEncoder extends MessageToMessageEncoder<Message> {

	public static final int MT_SYNCREQUEST = 2;
	public static final int MT_SYNCORDER = 3;
	public static final int MT_SLOTS = 4;
	public static final int MT_NUMERIC = 5;
	public static final int MT_ALPHANUM = 6;

	private final PagingProtocolSettings settings;

	public MessageEncoder(PagingProtocolSettings settings) {
		this.settings = Objects.requireNonNull(settings, "Settings must not be null.");
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
		final PagerMessage pm = msg.getMessage();

		// Mostly adapted from Sven Jung
		// See Diplomarbeit Jansen Page 30
		int type = 0;
		switch (pm.getContentType()) {
		case ALPHANUMERIC:
			type = MT_ALPHANUM;
			break;
		case NUMERIC:
			type = MT_NUMERIC;
			break;
		}

		String encoded = String.format("#%02X %s:%X:%X:%s:%s\n", msg.getSequenceNumber(), type, settings.getSendSpeed(),
				pm.getAddress(), pm.getSubAddress().getValue(), pm.getContent());

		out.add(encoded);
	}

}
