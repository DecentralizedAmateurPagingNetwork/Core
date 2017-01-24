package org.dapnet.core.transmission;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.model.Transmitter;
import org.jgroups.stack.IpAddress;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.ScheduledFuture;

class ServerHandler extends SimpleChannelInboundHandler<String> {

	private enum ConnectionState {
		AUTH_PENDING, SYNC_SYS_TIME, SYNC_SYS_TIME_ACK, ONLINE
	}

	private static final Logger logger = LogManager.getLogger(ServerHandler.class);

	private static final int HANDSHAKE_TIMEOUT_SEC = 30;
	private final TransmitterManager manager;
	private TransmitterClient client;
	private ChannelPromise handshakePromise;

	public ServerHandler(TransmitterManager manager) {
		this.manager = manager;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		if (client != null) {
			client.onReceive(msg);
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		logger.info("Accepted new connection from {}", ctx.channel().remoteAddress());

		client = new TransmitterClient(ctx.channel());
		// Do not add the client to the transmitter manager yet. This is done
		// once the handshake is finished.

		handshakePromise = ctx.newPromise();
		initHandshakeTimeout(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.info("Connection closed.");

		if (client != null) {
			int count = client.getPendingAckCount();
			if (count > 0) {
				logger.warn("Client has {} pending acks.", count);
			}

			manager.onDisconnect(client);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("Exception in server handler.", cause);
	}

	private void handleMessage(String msg) throws Exception {

	}

	private void handleAuth(ChannelHandlerContext ctx, String msg) throws Exception {
		Matcher authMatcher = authPattern.matcher(msg);
		if (!authMatcher.matches()) {
			throw new TransmitterDeviceException("Invalid welcome message format.");
		}

		String type = authMatcher.group(1);
		String version = authMatcher.group(2);
		String key = authMatcher.group(3);

		Transmitter t = manager.get(key);
		if (t == null) {
			throw new TransmitterDeviceException("The received auth key is not registered.");
		}

		t.setDeviceType(type);
		t.setDeviceVersion(version);
		t.setAddress(new IpAddress((InetSocketAddress) ctx.channel().remoteAddress()));

		client.setTransmitter(t);

		state = ConnectionState.SYNC_SYS_TIME;
	}

	private void handleSyncSysTime(ChannelHandlerContext ctx, String msg) {
		// TODO Impl

		// Handshake is finished
		handshakePromise.trySuccess();
		// Now it is time to inform the transmitter manager of the new client
		manager.onConnect(client);
	}

	private void handleSyncSysTimeAck(String msg) {

	}

	private void initHandshakeTimeout(final ChannelHandlerContext ctx) {
		final ChannelPromise p = handshakePromise;

		final ScheduledFuture<?> timeoutFuture = ctx.executor().schedule(() -> {
			if (!p.isDone()) {
				logger.warn("Handshake timed out.");
				ctx.flush();
				ctx.close();
			}
		}, HANDSHAKE_TIMEOUT_SEC, TimeUnit.SECONDS);

		p.addListener((f) -> {
			timeoutFuture.cancel(false);
		});
	}
}
