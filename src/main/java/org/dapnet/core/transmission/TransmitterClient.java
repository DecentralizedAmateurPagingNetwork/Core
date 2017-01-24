package org.dapnet.core.transmission;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.dapnet.core.model.Transmitter;
import org.jgroups.stack.IpAddress;

import io.netty.channel.Channel;

/**
 * This class holds the client session.
 * 
 * @author Philipp Thiel
 */
class TransmitterClient {

	private final Set<Integer> pendingAcks = new HashSet<>();
	private final Channel channel;
	private ClientStateHandler stateHandler;
	private int sequenceNumber = 0;
	private Transmitter transmitter;
	private AtomicBoolean handshakeDone = new AtomicBoolean(false);

	/**
	 * Creates a new client session.
	 * 
	 * @param channel
	 *            Client connection channel
	 * @throws NullPointerException
	 *             If channel is null.
	 */
	public TransmitterClient(Channel channel) {
		if (channel == null) {
			throw new NullPointerException("channel");
		}

		this.channel = channel;
	}

	/**
	 * Gets the transmitter data.
	 * 
	 * @return Transmitter data
	 */
	public Transmitter getTransmitter() {
		return transmitter;
	}

	/**
	 * Sets the transmitter data.
	 * 
	 * @param transmitter
	 *            Transmitter data
	 */
	public void setTransmitter(Transmitter transmitter) {
		if (transmitter != null) {
			transmitter.setAddress(new IpAddress((InetSocketAddress) channel.remoteAddress()));
		}

		this.transmitter = transmitter;
	}

	/**
	 * Sends a message to the client.
	 * 
	 * @param msg
	 *            Message to send.
	 */
	public void sendMessage(Message msg) {
		synchronized (channel) {
			msg.setSequenceNumber(sequenceNumber);

			pendingAcks.add(sequenceNumber + 1);
			sequenceNumber = (sequenceNumber + 1) % 256;

			channel.writeAndFlush(msg);
		}
	}

	/**
	 * Sends all messages to the client.
	 * 
	 * @param messages
	 *            Messages to send.
	 */
	public void sendMessages(Collection<Message> messages) {
		// TODO Sort messages?
		synchronized (channel) {
			messages.forEach(channel::write);
			channel.flush();
		}
	}

	/**
	 * Acknowledges a sequence number and removes it from the list of pending
	 * acks if it was valid.
	 * 
	 * @param sequenceNumber
	 *            Sequence number to ack.
	 * @return True if the sequence number was valid.
	 */
	public boolean ackSequenceNumber(int sequenceNumber) {
		synchronized (pendingAcks) {
			return pendingAcks.remove(sequenceNumber);
		}
	}

	/**
	 * Returns the number of pending acks.
	 * 
	 * @return Number of pending acks.
	 */
	public int getPendingAckCount() {
		synchronized (pendingAcks) {
			return pendingAcks.size();
		}
	}

	public boolean isHandshakeDone() {
		return handshakeDone.get();
	}

	public void setHandshakeDone(boolean done) {
		this.handshakeDone.set(done);
	}

	public void setStateHandler(ClientStateHandler handler) {
		this.stateHandler = handler;
	}

	public void onReceive(String msg) throws Exception {
		stateHandler.onReceive(this, msg);
	}
}
