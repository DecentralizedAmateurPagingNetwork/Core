package org.dapnet.core.transmission;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.ScheduledFuture;

class ServerHandler extends SimpleChannelInboundHandler<String> {

	private enum State {
		PENDING_AUTH, SYNC_SYS_TIME, ONLINE, OFFLINE
	}

	private static final Logger logger = LogManager.getLogger(ServerHandler.class);
	private static final int HANDSHAKE_TIMEOUT_SEC = 30;
	// Ack message #04 +
	private final Pattern ackPattern = Pattern.compile("#(\\p{XDigit}+) (\\+)");
	private final TransmitterClient client;
	private State state = State.OFFLINE;
	private ChannelPromise handshakePromise;

	public ServerHandler(TransmitterClient client) {
		this.client = client;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		switch (state) {
		case PENDING_AUTH:
			handleAuth(ctx, msg);
			break;
		case SYNC_SYS_TIME:
			handleSyncSysTime(ctx, msg);
			break;
		case ONLINE:
			handleMessage(ctx, msg);
			break;
		case OFFLINE:
			break;
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		logger.info("Accepted new connection from {}.", ctx.channel().remoteAddress());

		state = State.PENDING_AUTH;

		handshakePromise = ctx.newPromise();
		initHandshakeTimeout(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		state = State.OFFLINE;

		int pending = client.getPendingAckCount();
		if (pending > 0) {
			logger.warn("Pending acks: {}", pending);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("Exception in server handler.", cause);
		ctx.close();
	}

	/**
	 * Initializes the handshake timeout.
	 * 
	 * @param ctx
	 *            Channel handler context
	 */
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

	private void handleMessage(ChannelHandlerContext ctx, String msg) throws Exception {
		// Only acks expected
		Matcher ackMatcher = ackPattern.matcher(msg);
		if (!ackMatcher.matches()) {
			throw new TransmitterDeviceException("Invalid response received.");
		}

		int seq = Integer.parseInt(ackMatcher.group(1), 16);
		String ack = ackMatcher.group(2);
		if (!ack.equals("+") || !client.ackSequenceNumber(seq)) {
			throw new TransmitterDeviceException("Unexpected response received.");
		}
	}

	private void handleAuth(ChannelHandlerContext ctx, String msg) throws Exception {
		// [RasPager v1.0-SCP-#2345678 test1234]
		// TODO Pattern
		Pattern authPattern = Pattern.compile("");
		Matcher authMatcher = authPattern.matcher(msg);

		if (!authMatcher.matches()) {
			throw new TransmitterDeviceException("Invalid auth message format.");
		}

		String type = authMatcher.group(1);
		String version = authMatcher.group(2);
		String key = authMatcher.group(3);

		// TODO Impl
		state = State.SYNC_SYS_TIME;
	}

	private void handleSyncSysTime(ChannelHandlerContext ctx, String msg) throws Exception {
		handshakePromise.trySuccess();
	}
}
