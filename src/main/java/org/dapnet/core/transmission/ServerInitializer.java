package org.dapnet.core.transmission;

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
	private static final StringEncoder encoder = new StringEncoder();
	private static final StringDecoder decoder = new StringDecoder();
	private static final MessageEncoder msgEncoder = new MessageEncoder();
	private final TransmitterManager manager;

	public ServerInitializer(TransmitterManager manager) {
		this.manager = manager;
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
