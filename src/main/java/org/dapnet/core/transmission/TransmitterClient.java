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
		synchronized (pendingAcks) {
			msg.setSequenceNumber(sequenceNumber);
			sequenceNumber = (sequenceNumber + 1) % 256;
			// TODO Enable sequence numbers
			// pendingAcks.add(sequenceNumber);
		}

		channel.writeAndFlush(msg);
	}

	/**
	 * Sends all messages to the client.
	 * 
	 * @param messages
	 *            Messages to send.
	 */
	public void sendMessages(Collection<Message> messages) {
		// TODO Sort messages?
		synchronized (pendingAcks) {
			messages.forEach(m -> {
				m.setSequenceNumber(sequenceNumber);

				sequenceNumber = (sequenceNumber + 1) % 256;
				// TODO Enable sequence numbers
				// pendingAcks.add(sequenceNumber);

				channel.write(m);
			});

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

	/**
	 * Closes the connection.
	 */
	public void close() {
		if (channel != null) {
			channel.close();
		}
	}

}
