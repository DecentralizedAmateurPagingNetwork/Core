package org.dapnet.core.transmission;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.netty.channel.ChannelHandlerContext;

/**
 * This class implements a simple state machine to handle the initial time
 * synchronization between a client and the server.
 * 
 * @author Philipp Thiel
 */
final class SyncTimeHandler {

	private enum SyncState {
		WRITE_TIME, READ_TIME, READ_TIME_ACK, READ_TIME_ADJUST_ACK, DONE
	}

	// 2:13d3:0026
	private static final Pattern syncAckPattern = Pattern.compile("(\\d):(\\w+):(\\w+)");
	private final int maxLoops;
	private SyncState state = SyncState.WRITE_TIME;
	private int loopCounter = 0;
	private long timeTx = 0;
	private long timeRx = 0;
	private long minRTT = Long.MAX_VALUE;
	private long timeAdjust = 0;
	private String timeTxMsg;
	private String timeRxMsg;

	/**
	 * Creates a new instance.
	 * 
	 * @param maxLoops Number of time synchronizations to perform.
	 */
	public SyncTimeHandler(int maxLoops) {
		this.maxLoops = maxLoops;
	}

	/**
	 * Handles the incoming message depending on the connection state.
	 * 
	 * @param ctx     Channel handler context (used to send responses if required).
	 * @param message Received message.
	 * @throws Exception If an error occurs.
	 */
	public void handleMessage(ChannelHandlerContext ctx, String message) throws Exception {
		switch (state) {
		case WRITE_TIME:
			writeTime(ctx);
			break;
		case READ_TIME:
			readTime(message);
			break;
		case READ_TIME_ACK:
			readTimeAck(ctx, message);
			break;
		case READ_TIME_ADJUST_ACK:
			if (message.equals("+")) {
				state = SyncState.DONE;
			} else {
				throw new TransmitterException("Wrong ack received.");
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Whether the time synchronization process is done.
	 * 
	 * @return True if the time synchronization process is done.
	 */
	public boolean isDone() {
		return state == SyncState.DONE;
	}

	private void writeTime(ChannelHandlerContext ctx) {
		long timemillis = System.currentTimeMillis();
		long seconds = timemillis / 1000;
		// Milliseconds as long of current second
		long deltaTimemillis = timemillis - seconds * 1000;
		// Time since last full minute in 0,1 s, lowest 16 bit
		// after 1 complete minute, counter will continue with 601, 602,...
		// up to 0xffff, than wrap to 0x0000
		timeTx = (seconds * 10 + deltaTimemillis / 100) & 0xffff;

		timeTxMsg = String.format("%04X", timeTx);
		String resp = String.format("%d:%s\n", MessageEncoder.MT_SYNCREQUEST, timeTxMsg);
		ctx.writeAndFlush(resp);

		state = SyncState.READ_TIME;
	}

	private void readTime(String message) {
		long timemillis = System.currentTimeMillis();
		long seconds = timemillis / 1000;
		long deltaTimemillis = timemillis - seconds * 1000;
		timeRx = (seconds * 10 + deltaTimemillis / 100) & 0xffff;

		// Actual message processing is done during readTimeAck()
		timeRxMsg = message;

		state = SyncState.READ_TIME_ACK;
	}

	private void readTimeAck(ChannelHandlerContext ctx, String message) throws Exception {
		if (!message.equals("+")) {
			throw new TransmitterException("Wrong ack received.");
		}

		Matcher syncMatcher = syncAckPattern.matcher(timeRxMsg);
		if (!syncMatcher.matches()) {
			throw new TransmitterException("Wrong sync response received.");
		}

		int id = Integer.parseInt(syncMatcher.group(1));
		String timeStringResp = syncMatcher.group(2);
		String timeStringClient = syncMatcher.group(3);
		long timeLongClient = Long.parseLong(timeStringClient, 16);

		if (id != MessageEncoder.MT_SYNCREQUEST || !timeTxMsg.equalsIgnoreCase(timeStringResp)) {
			throw new TransmitterException("Wrong sync response received.");
		}

		long rtt = timeRx - timeTx;
		if (rtt < minRTT) {
			minRTT = rtt;
			timeAdjust = (timeTx + rtt / 2) - timeLongClient;
		}

		++loopCounter;
		if (loopCounter < maxLoops) {
			writeTime(ctx);
		} else {
			sendTimeAdjust(ctx);
		}
	}

	private void sendTimeAdjust(ChannelHandlerContext ctx) throws Exception {
		long abs = Math.abs(timeAdjust);
		if (abs > 65536) {
			throw new TransmitterException("Time difference too large.");
		}

		String sign = "+";
		if (timeAdjust < 0) {
			sign = "-";
		}

		String msg = String.format("%d:%s%04X\n", MessageEncoder.MT_SYNCORDER, sign, abs);
		ctx.writeAndFlush(msg);

		state = SyncState.READ_TIME_ADJUST_ACK;
	}
}
