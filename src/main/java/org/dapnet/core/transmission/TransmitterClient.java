package org.dapnet.core.transmission;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.dapnet.core.model.Transmitter;
import org.jgroups.stack.IpAddress;

import io.netty.channel.Channel;

/**
 * This class holds the client session.
 * 
 * @author Philipp Thiel
 */
final class TransmitterClient {

	private final Set<Integer> pendingAcks = new HashSet<>();
	private final Channel channel;
	private int sequenceNumber = 0;
	private volatile Transmitter transmitter;

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
	 * Sends a message containing the transmitter name (callsign) to the
	 * connected transmitter.
	 */
	public void sendCallSignMessage() {
		Transmitter theTransmitter = transmitter;
		if (theTransmitter != null) {
			Message msg = theTransmitter.createCallSignMessage();
			sendMessage(msg);
		}
	}

	/**
	 * Sends a message to the client.
	 * 
	 * @param msg
	 *            Message to send.
	 */
	public void sendMessage(Message msg) {
		channel.writeAndFlush(msg);
	}

	/**
	 * Sends all messages to the client.
	 * 
	 * @param messages
	 *            Messages to send.
	 */
	public void sendMessages(Collection<Message> messages) {
		messages.forEach(m -> {
			channel.write(m);
		});

		channel.flush();
	}

	/**
	 * Returns the next sequence number.
	 * 
	 * @return Next sequence number.
	 */
	public int getSequenceNumber() {
		synchronized (pendingAcks) {
			int sn = sequenceNumber;
			sequenceNumber = (sequenceNumber + 1) % 256;
			// Add expected sequence number to pending list
			pendingAcks.add(sequenceNumber);

			return sn;
		}
	}

	/**
	 * Acknowledges a sequence number and removes it from the list of pending
	 * acks if it was valid.
	 * 
	 * @param sequenceNumber
	 *            Sequence number to ack.
	 * @return True if the sequence number was valid.
	 * @see TransmitterClient#freeSequenceNumber(int)
	 */
	public boolean ackSequenceNumber(int sequenceNumber) {
		synchronized (pendingAcks) {
			return pendingAcks.remove(sequenceNumber);
		}
	}

	/**
	 * Frees an allocated sequence number. The number will be incremented as if
	 * a client has sent a correct response. Use this method only to free a
	 * sequence number that will never be ackd due to errors etc.
	 * 
	 * @param sequenceNumber
	 *            Sequence number to free.
	 * @see TransmitterClient#ackSequenceNumber(int)
	 */
	public void freeSequenceNumber(int sequenceNumber) {
		sequenceNumber = (sequenceNumber + 1) % 256;
		ackSequenceNumber(sequenceNumber);
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

	/**
	 * Closes the connection. This call will block until the connection is
	 * closed.
	 */
	public void close() {
		Channel theChannel = channel;
		if (theChannel != null) {
			theChannel.close().syncUninterruptibly();
		}
	}

}
