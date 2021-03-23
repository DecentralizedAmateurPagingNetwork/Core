package org.dapnet.core.transmission;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.model.CoreRepository;
import org.dapnet.core.model.Transmitter;
import org.dapnet.core.model.Transmitter.Status;
import org.dapnet.core.transmission.TransmitterClient.AckType;
import org.jgroups.stack.IpAddress;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.ScheduledFuture;

class ServerHandler extends SimpleChannelInboundHandler<String> {

	private enum ConnectionState {
		AUTH_PENDING, SYNC_TIME, TIMESLOTS_SENT, ONLINE, OFFLINE, EXCEPTION_CAUGHT
	}

	private static final Logger logger = LogManager.getLogger();
	// Ack message #04 +
	private static final Pattern ACK_PATTERN = Pattern.compile("#(\\p{XDigit}{2}) ([-%\\+])");
	// Welcome string [RasPager v1.0-SCP-#2345678 abcde]
	private static final Pattern AUTH_PATTERN = Pattern
			.compile("\\[([/\\-\\p{Alnum}]+) v(\\d[\\d\\.]+[\\p{Graph}]*) ([\\p{Alnum}_]+) (\\p{Alnum}+)\\]");
	private static final int HANDSHAKE_TIMEOUT_SEC = 30;
	private static final int CLOSE_TASK_DELAY_SEC = 5;
	private final TransmitterManager manager;
	private ConnectionState state = ConnectionState.OFFLINE;
	private TransmitterClient client;
	private ChannelPromise handshakePromise;
	private SyncTimeHandler syncHandler;

	public ServerHandler(TransmitterManager manager) {
		this.manager = Objects.requireNonNull(manager, "Transmitter manager must not be null.");
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
			logger.fatal("Invalid state.");
			ctx.close();
			break;
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		logger.info("Accepted new connection from {}", ctx.channel().remoteAddress());

		// Do not add the client to the transmitter manager yet. This is done
		// once the handshake is finished.
		client = new TransmitterClient(ctx.channel());

		syncHandler = new SyncTimeHandler(
				manager.getSettings().getTransmissionSettings().getPagingProtocolSettings().getNumberOfSyncLoops());

		handshakePromise = ctx.newPromise();
		initHandshakeTimeout(ctx);

		state = ConnectionState.AUTH_PENDING;
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (client != null) {
			logger.info("Connection to {} with callsign {} closed in state {} using {} {}.",
					ctx.channel().remoteAddress(), client.getName(), state, client.getDeviceType(),
					client.getDeviceVersion());
		} else {
			logger.info("Connection to {} closed in state {}.", ctx.channel().remoteAddress(), state);
		}

		if (handshakePromise != null) {
			handshakePromise.trySuccess();
		}

		if (client != null) {
			int count = client.getPendingMessageCount();
			if (count > 0) {
				logger.warn("Client {} has {} pending messages.", client.getName(), count);
			}

			manager.onDisconnect(client);
		}

		state = ConnectionState.OFFLINE;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		state = ConnectionState.EXCEPTION_CAUGHT;

		try {
			String transmitterName = null;
			if (client != null) {
				Transmitter t = client.getTransmitter();
				if (t != null) {
					Lock lock = manager.getRepository().getLock().writeLock();
					lock.lock();

					try {
						t.setStatus(Status.ERROR);
						transmitterName = t.getName();
					} finally {
						lock.unlock();
					}
				}
			}

			if (transmitterName != null && !transmitterName.isEmpty()) {
				if (cause instanceof TransmitterException) {
					logger.error("Closing connection for {}: {}", transmitterName, cause.getMessage());
				} else {
					logger.error("Exception in server handler for {}.", transmitterName, cause);
				}
			} else {
				if (cause instanceof TransmitterException) {
					logger.error("Closing connection: {}", cause.getMessage());
				} else {
					logger.error("Exception in server handler.", cause);
				}
			}
		} catch (Exception ex) {
			logger.error("Exception in exception handler", ex);
		} finally {
			// Delay the channel close
			Runnable closeTask = new SendAndCloseTask(ctx.channel());
			ctx.executor().schedule(closeTask, CLOSE_TASK_DELAY_SEC, TimeUnit.SECONDS);
		}
	}

	private void handleMessageAck(String msg) throws Exception {
		Matcher ackMatcher = ACK_PATTERN.matcher(msg);
		if (!ackMatcher.matches()) {
			throw new TransmitterException("Invalid response received: " + msg);
		}

		int seq = Integer.parseInt(ackMatcher.group(1), 16);
		AckType type = AckType.ERROR;
		switch (ackMatcher.group(2)) {
		case "+":
			type = AckType.OK;
			break;
		case "%":
			type = AckType.RETRY;
			break;
		case "-":
			type = AckType.ERROR;
		}

		if (!client.ackMessage(seq, type)) {
			Transmitter t = client.getTransmitter();
			if (t != null) {
				// TODO Lock required?
				logger.warn("Invalid ack received from {}: {}", t.getName(), msg);
			} else {
				logger.warn("Invalid ack received: {}", msg);
			}
		}
	}

	private void handleAuth(ChannelHandlerContext ctx, String msg) throws Exception {
		Matcher authMatcher = AUTH_PATTERN.matcher(msg);
		if (!authMatcher.matches()) {
			logger.error("Invalid welcome message format: " + msg);
			SendAndCloseTask task = new SendAndCloseTask(ctx.channel(), "7 Invalid welcome message format");
			ctx.executor().schedule(task, CLOSE_TASK_DELAY_SEC, TimeUnit.SECONDS);
			return;
		}

		String type = authMatcher.group(1);
		String version = authMatcher.group(2);
		String name = authMatcher.group(3);
		String key = authMatcher.group(4);

		Transmitter transmitter = null;
		final CoreRepository repo = manager.getRepository();

		Lock lock = repo.getLock().readLock();
		lock.lock();

		try {
			transmitter = repo.getTransmitters().get(name);
			if (transmitter == null) {
				logger.error("The transmitter name is not registered: " + name + " connecting from "
						+ ctx.channel().remoteAddress());
				SendAndCloseTask task = new SendAndCloseTask(ctx.channel(), "7 Transmitter not registered");
				ctx.executor().schedule(task, CLOSE_TASK_DELAY_SEC, TimeUnit.SECONDS);
				return;
			} else if (transmitter.getStatus() == Status.DISABLED) {
				logger.error("Transmitter is disabled and not allowed to connect: " + name + " connecting from "
						+ ctx.channel().remoteAddress());
				SendAndCloseTask task = new SendAndCloseTask(ctx.channel(), "7 Transmitter disabled");
				ctx.executor().schedule(task, CLOSE_TASK_DELAY_SEC, TimeUnit.SECONDS);
				return;
			}

			// Test authentication key
			if (!transmitter.getAuthKey().equals(key)) {
				logger.error("Wrong authentication key supplied for transmitter: " + name + " connecting from "
						+ ctx.channel().remoteAddress());
				SendAndCloseTask task = new SendAndCloseTask(ctx.channel(), "7 Invalid credentials");
				ctx.executor().schedule(task, CLOSE_TASK_DELAY_SEC, TimeUnit.SECONDS);
				return;
			}
		} finally {
			lock.unlock();
		}

		// Close existing connection if necessary. This is a no-op if the transmitter is
		// not connected.
		manager.disconnectFrom(transmitter);

		lock = repo.getLock().writeLock();
		lock.lock();

		try {
			transmitter.setDeviceType(type);
			transmitter.setDeviceVersion(version);
			transmitter.setAddress(new IpAddress((InetSocketAddress) ctx.channel().remoteAddress()));
		} finally {
			lock.unlock();
		}

		client.setTransmitter(transmitter);

		// Begin the sync time procedure
		syncHandler.handleMessage(ctx, msg);

		state = ConnectionState.SYNC_TIME;
	}

	private void handleSyncTime(ChannelHandlerContext ctx, String message) throws Exception {
		syncHandler.handleMessage(ctx, message);

		if (syncHandler.isDone()) {
			syncHandler = null;
			String msg = null;

			// Send timeslots to client
			Transmitter t = client.getTransmitter();

			Lock lock = manager.getRepository().getLock().readLock();
			lock.lock();

			try {
				msg = String.format("%d:%s\n", MessageEncoder.MT_SLOTS, t.getTimeSlot());
			} finally {
				lock.unlock();
			}

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

	private static class SendAndCloseTask implements Runnable {
		private final Channel channel;
		private final String message;

		public SendAndCloseTask(Channel channel) {
			this(channel, null);
		}

		public SendAndCloseTask(Channel channel, String message) {
			this.channel = channel;
			this.message = message;
		}

		@Override
		public void run() {
			if (channel == null) {
				return;
			}

			if (message != null) {
				channel.writeAndFlush(message).addListener(ChannelFutureListener.CLOSE);
			} else {
				channel.close();
			}
		}
	}
}
