package org.dapnet.core.transmission;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.Settings;
import org.dapnet.core.model.Transmitter;
import org.dapnet.core.model.Transmitter.Status;
import org.dapnet.core.transmission.MessageEncoder.PagingMessageType;
import org.dapnet.core.transmission.TransmissionSettings.PagingProtocolSettings;
import org.jgroups.stack.IpAddress;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.ScheduledFuture;

class ServerHandler extends SimpleChannelInboundHandler<String> {

	private enum ConnectionState {
		AUTH_PENDING, SYNC_TIME, TIMESLOTS_SENT, ONLINE, OFFLINE
	}

	static final AttributeKey<TransmitterClient> CLIENT_KEY = AttributeKey.valueOf("client");
	private static final Logger logger = LogManager.getLogger(ServerHandler.class);
	// Ack message #04 +
	private static final Pattern ackPattern = Pattern.compile("#(\\p{XDigit}{2}) (\\+)");
	// Welcome string [RasPager v1.0-SCP-#2345678 abcde]
	private static final Pattern authPattern = Pattern
			.compile("\\[([/\\p{Alnum}]+) v(\\d+\\.\\d+[-#\\p{Alnum}]*) (\\p{Alnum}+) (\\p{Alnum}+)\\]");
	private static final PagingProtocolSettings settings = Settings.getTransmissionSettings()
			.getPagingProtocolSettings();
	private static final int HANDSHAKE_TIMEOUT_SEC = 30;
	private final TransmitterManager manager;
	private ConnectionState state = ConnectionState.OFFLINE;
	private TransmitterClient client;
	private ChannelPromise handshakePromise;
	private SyncTimeHandler syncHandler;

	public ServerHandler(TransmitterManager manager) {
		this.manager = manager;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		switch (state) {
		case AUTH_PENDING:
			handleAuth(ctx, msg);
			break;
		case SYNC_TIME:
			handleSyncTime(ctx, msg);
			break;
		case TIMESLOTS_SENT:
			handleTimeslotsAck(ctx, msg);
			break;
		case ONLINE:
			handleMessageAck(msg);
			break;
		default:
			break;
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		logger.info("Accepted new connection from {}", ctx.channel().remoteAddress());

		client = new TransmitterClient(ctx.channel());
		// Register channel attribute (used by message encoder)
		ctx.channel().attr(CLIENT_KEY).set(client);
		// Do not add the client to the transmitter manager yet. This is done
		// once the handshake is finished.

		syncHandler = new SyncTimeHandler(settings.getNumberOfSyncLoops());

		handshakePromise = ctx.newPromise();
		initHandshakeTimeout(ctx);

		state = ConnectionState.AUTH_PENDING;
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.info("Connection closed.");

		if (handshakePromise != null) {
			handshakePromise.trySuccess();
		}

		if (client != null) {
			int count = client.getPendingAckCount();
			if (count > 0) {
				logger.warn("Client has {} pending acks.", count);
			}

			manager.onDisconnect(client);
		}

		state = ConnectionState.OFFLINE;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (cause instanceof TransmitterException) {
			logger.error("Closing connection: {}", cause.getMessage());
		} else {
			logger.error("Exception in server handler.", cause);
		}

		if (client != null) {
			Transmitter t = client.getTransmitter();
			if (t != null) {
				t.setStatus(Status.ERROR);
			}
		}

		ctx.close();
	}

	private void handleMessageAck(String msg) throws Exception {
		Matcher ackMatcher = ackPattern.matcher(msg);
		if (!ackMatcher.matches()) {
			throw new TransmitterException("Invalid response received.");
		}

		int seq = Integer.parseInt(ackMatcher.group(1), 16);
		String ack = ackMatcher.group(2);
		if (!ack.equals("+")) {
			throw new TransmitterException("Unexpected response received.");
		} else if (!client.ackSequenceNumber(seq)) {
			throw new TransmitterException("Invalid sequence number received.");
		}
	}

	private void handleAuth(ChannelHandlerContext ctx, String msg) throws Exception {
		Matcher authMatcher = authPattern.matcher(msg);
		if (!authMatcher.matches()) {
			throw new TransmitterException("Invalid welcome message format.");
		}

		String type = authMatcher.group(1);
		String version = authMatcher.group(2);
		String name = authMatcher.group(3);
		String key = authMatcher.group(4);

		Transmitter t = manager.get(name);
		if (t == null) {
			throw new TransmitterException("The transmitter name is not registered.");
		} else if (t.getStatus() == Status.DISABLED) {
			logger.error("Transmitter is disabled and not allowed to connect.");
			ctx.close();
			return;
		} else if (t.getStatus() == Status.ONLINE) {
			// TODO Close existing connection?
			logger.error("Transmitter is already connected.");
			ctx.close();
			return;
		}

		// Test authentication key
		if (!t.getAuthKey().equals(key)) {
			logger.error("Wrong authentication key supplied.");
			ctx.close();
			return;
		}

		t.setDeviceType(type);
		t.setDeviceVersion(version);
		t.setAddress(new IpAddress((InetSocketAddress) ctx.channel().remoteAddress()));

		client.setTransmitter(t);

		// Begin the sync time procedure
		syncHandler.handleMessage(ctx, msg);

		state = ConnectionState.SYNC_TIME;
	}

	private void handleSyncTime(ChannelHandlerContext ctx, String message) throws Exception {
		syncHandler.handleMessage(ctx, message);

		if (syncHandler.isDone()) {
			syncHandler = null;

			// Send timeslots to client
			Transmitter t = client.getTransmitter();
			String msg = String.format("%d:%s\r\n", PagingMessageType.SLOTS.getValue(), t.getTimeSlot());
			ctx.writeAndFlush(msg);

			state = ConnectionState.TIMESLOTS_SENT;
		}
	}

	private void handleTimeslotsAck(ChannelHandlerContext ctx, String msg) throws Exception {
		if (!msg.equals("+")) {
			throw new TransmitterException("Wrong ack received.");
		}

		handshakePromise.trySuccess();
		handshakePromise = null;

		// Now it is time to inform the transmitter manager of the new client
		manager.onConnect(client);

		state = ConnectionState.ONLINE;
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
