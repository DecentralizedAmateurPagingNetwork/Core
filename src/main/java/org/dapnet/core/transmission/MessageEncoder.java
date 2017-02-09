package org.dapnet.core.transmission;

import java.util.List;

import org.dapnet.core.Settings;
import org.dapnet.core.transmission.TransmissionSettings.PagingProtocolSettings;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

/**
 * Encodes a {@link Message} into string.
 * 
 * @author Philipp Thiel
 */
@Sharable
class MessageEncoder extends MessageToMessageEncoder<Message> {

	public enum PagingMessageType {
		SYNCREQUEST(2), SYNCORDER(3), SLOTS(4), NUMERIC(5), ALPHANUM(6);

		private final int value;

		private PagingMessageType(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	private static final PagingProtocolSettings settings = Settings.getTransmissionSettings()
			.getPagingProtocolSettings();

	@Override
	protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
		TransmitterClient client = ctx.channel().attr(ServerHandler.CLIENT_KEY).get();
		if (client == null) {
			return;
		}

		// Mostly adapted from Sven Jung
		// See Diplomarbeit Jansen Page 30
		PagingMessageType type = null;
		switch (msg.getFunctionalBits()) {
		case ACTIVATION:
			type = PagingMessageType.ALPHANUM;
			break;
		case ALPHANUM:
			type = PagingMessageType.ALPHANUM;
			break;
		case NUMERIC:
			type = PagingMessageType.NUMERIC;
			break;
		case TONE:
			type = PagingMessageType.ALPHANUM;
			break;
		}

		int sn = client.getSequenceNumber();
		try {
			String encoded = String.format("#%02X %s:%X:%X:%s:%s\r\n", sn, type.getValue(), settings.getSendSpeed(),
					msg.getAddress(), msg.getFunctionalBits().getValue(), msg.getText());

			out.add(encoded);
		} catch (Exception ex) {
			client.ackSequenceNumber(sn);

			throw ex;
		}
	}

}
