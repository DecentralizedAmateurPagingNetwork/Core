package org.dapnet.core.transmission;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.Settings;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Server implements Runnable, AutoCloseable {

	private static final Logger LOGGER = LogManager.getLogger();
	private final int port;
	private final TransmitterManager manager;
	private volatile ChannelFuture serverFuture;

	public Server(TransmitterManager manager) {
		this.port = Settings.getTransmissionSettings().getServerSettings().getPort();
		this.manager = manager;
	}

	@Override
	public void run() {
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup);
			b.channel(NioServerSocketChannel.class);
			b.childHandler(new ServerInitializer(manager));
			b.childOption(ChannelOption.SO_KEEPALIVE, true);
			serverFuture = b.bind(port).sync();

			LOGGER.info("Server started on port: {}", port);

			serverFuture.channel().closeFuture().sync();
		} catch (Exception ex) {
			LOGGER.fatal("Exception in server thread.", ex);
		} finally {
			try {
				bossGroup.shutdownGracefully().sync();
			} catch (Throwable t) {
				LOGGER.fatal("Failed to shut down boss group.", t);
			}

			try {
				workerGroup.shutdownGracefully().sync();
			} catch (Throwable t) {
				LOGGER.fatal("Failed to shut down worker group.", t);
			}

			LOGGER.info("Server stopped.");
		}
	}

	@Override
	public void close() throws Exception {
		try {
			ChannelFuture future = serverFuture;
			if (future != null) {
				future.channel().close().sync();
			}
		} catch (InterruptedException e) {
			LOGGER.warn("Interrupted while closing server channel.", e);
		}
	}

}
