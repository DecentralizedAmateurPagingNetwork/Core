package org.dapnet.core.transmission;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * The server initializer initializes the newly created channel pipeline.
 * 
 * @author Philipp Thiel
 */
class ServerInitializer extends ChannelInitializer<SocketChannel> {
	private static final StringEncoder encoder = new StringEncoder(StandardCharsets.US_ASCII);
	private static final StringDecoder decoder = new StringDecoder(StandardCharsets.US_ASCII);
	private final TransmitterManager manager;
	private final MessageEncoder msgEncoder;

	public ServerInitializer(TransmitterManager manager) {
		this.manager = Objects.requireNonNull(manager, "Transmitter manager must not be null.");
		msgEncoder = new MessageEncoder(manager.getSettings().getTransmissionSettings().getPagingProtocolSettings());
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline p = ch.pipeline();
		p.addLast(new DelimiterBasedFrameDecoder(2048, Delimiters.lineDelimiter()));
		p.addLast(decoder);
		p.addLast(encoder);
		p.addLast(msgEncoder);
		p.addLast(new ServerHandler(manager));
	}
}
