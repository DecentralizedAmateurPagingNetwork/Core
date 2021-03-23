package org.dapnet.core.transmission;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.CoreStartupException;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class TransmitterServer {

	private static final Logger LOGGER = LogManager.getLogger();
	private final int port;
	private final TransmitterManager manager;
	private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
	private final EventLoopGroup workerGroup = new NioEventLoopGroup();

	public TransmitterServer(TransmitterManager manager) {
		this.manager = Objects.requireNonNull(manager, "Transmitter manager must not be null.");
		this.port = manager.getSettings().getTransmissionSettings().getServerSettings().getPort();
	}

	public void start() {
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup);
			b.channel(NioServerSocketChannel.class);
			b.childHandler(new ServerInitializer(manager));
			b.childOption(ChannelOption.SO_KEEPALIVE, true);
			b.bind(port).sync();

			LOGGER.info("Server started on port: {}", port);
		} catch (Exception ex) {
			// LOGGER.fatal("Failed to start the server.", ex);
			throw new CoreStartupException(ex);
		}
	}

	public void stop() {
		try {
			bossGroup.shutdownGracefully().sync();
		} catch (Exception e) {
			LOGGER.warn("Failed to shut down boss group.", e);
		}

		try {
			workerGroup.shutdownGracefully().sync();
		} catch (Exception e) {
			LOGGER.warn("Failed to shut down worker group.", e);
		}

		LOGGER.info("Server stopped.");
	}

}
