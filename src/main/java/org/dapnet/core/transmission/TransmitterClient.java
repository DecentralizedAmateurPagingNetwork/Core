package org.dapnet.core.transmission;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.dapnet.core.model.Transmitter;

import io.netty.channel.Channel;

/**
 * This class holds the client session.
 * 
 * @author Philipp Thiel
 */
class TransmitterClient {

	private final Set<Integer> pendingAcks = new HashSet<>();
	private final Channel channel;
	private int sequenceNumber = 0;
	private Transmitter transmitter;

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
			throw new NullPointerException("Channel cannot be null.");
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
	 * Gets a sequence number and adds it to the list of pending acks.
	 * 
	 * @return Sequence number
	 */
	public int getSequenceNumber() {
		synchronized (pendingAcks) {
			int sn = sequenceNumber;
			sequenceNumber = (sequenceNumber + 1) % 256;

			pendingAcks.add(sn + 1);

			return sn;
		}
	}

	/**
	 * Acknowledges a sequence number and removes it from the list of pending
	 * acks.
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

}
