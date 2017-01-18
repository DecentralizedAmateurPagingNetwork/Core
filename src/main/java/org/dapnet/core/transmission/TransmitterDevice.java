/*
 * DAPNET CORE PROJECT
 * Copyright (C) 2016
 *
 * Daniel Sialkowski
 *
 * daniel.sialkowski@rwth-aachen.de
 *
 * Institute of High Frequency Technology
 * RWTH AACHEN UNIVERSITY
 * Melatener Str. 25
 * 52074 Aachen
 */

package org.dapnet.core.transmission;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.model.Transmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

public abstract class TransmitterDevice extends Transmitter implements Runnable {
	private static final long serialVersionUID = -1103013950116394580L;
	private static final Logger logger = LogManager.getLogger(TransmitterDevice.class.getName());

	protected TransmitterDevice(Transmitter transmitter, TransmitterDeviceListener deviceListener) {
		this.name = transmitter.getName();
		this.longitude = transmitter.getLongitude();
		this.latitude = transmitter.getLatitude();
		this.power = transmitter.getPower();
		this.address = transmitter.getAddress();
		this.timeSlot = transmitter.getTimeSlot();
		this.ownerNames = transmitter.getOwnerNames();
		this.deviceType = transmitter.getDeviceType();
		this.deviceMode = transmitter.getDeviceMode();
		this.status = transmitter.getStatus();

		this.deviceListener = deviceListener;
	}

	protected TransmitterDevice(Socket socket, TransmitterDeviceListener deviceListener) {
		this.deviceType = DeviceType.UNKNOWN;
		this.deviceMode = DeviceMode.CLIENT;
		this.deviceListener = deviceListener;
	}

	// Handle Thread
	protected boolean running;
	protected volatile Thread thread = null;
	protected TransmitterDeviceListener deviceListener;

	public void start() {
		if (thread == null) {
			running = true;
			thread = new Thread(this);
			thread.start();
		} else {
			logger.warn(this + " started although already running");
		}
	}

	public void stop() {
		if (thread != null) {
			running = false;
			thread.interrupt();
		} else {
			logger.warn(this + " stopped although not running");
		}
	}

	public abstract void run();

	protected void throwTransmitterDeviceException(TransmitterDeviceException e) {
		if (deviceListener != null) {
			logger.warn(this + " throws Exception: " + e.getMessage());
			deviceListener.handleTransmitterDeviceError(this, e);
		}
	}

	protected void throwTransmitterDeviceOffline(TransmitterDeviceException e) {
		if (deviceListener != null) {
			logger.warn(this + " is offline now and throws Exception: " + e.getMessage());
			deviceListener.handleTransmitterDeviceOffline(this, e);
		}
	}

	// Handle Socket
	protected Socket deviceSocket = null;
	protected BufferedReader fromServer = null;
	protected PrintWriter toServer = null;

	protected void disconnect() {
		if (deviceSocket != null) { // clean up aborted connection
			try {
				deviceSocket.close();
			} catch (IOException e1) {
				logger.warn(this + " could not close socket");
			}
			deviceSocket = null;
		}
	}

	protected void setupDeviceIO() throws IOException {
		toServer = new PrintWriter(deviceSocket.getOutputStream(), true);
		fromServer = new BufferedReader(new InputStreamReader(deviceSocket.getInputStream()));
	}

	protected void closeDeviceIO() {
		if (fromServer != null) {
			try {
				fromServer.close();
			} catch (IOException e) {
				logger.warn(this + " could not close BufferedReader");
			}
			fromServer = null;
		}

		if (toServer != null) {
			toServer.close();
			toServer = null;
		}
	}

	// Handle Messages
	protected final PriorityBlockingQueue<Message> messageQueue = new PriorityBlockingQueue<>();
	protected TransmitterDeviceProtocol deviceProtocol;

	public Message getMessage() throws InterruptedException {
		return messageQueue.take();
	}

	public void sendMessage(Message m) {
		messageQueue.add(m);
	}

	public void sendMessages(List<Message> m) {
		messageQueue.addAll(m);
	}

	public boolean isInterrupted() {
		return thread.isInterrupted();
	}

}
