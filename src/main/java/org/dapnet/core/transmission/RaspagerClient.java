package org.dapnet.core.transmission;

import java.io.IOException;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.Settings;

public class RaspagerClient extends TransmitterDevice {

	private static final long serialVersionUID = 7055752986649942103L;
	private static final Logger LOGGER = LogManager.getLogger(RaspagerClient.class);
	private static final TransmissionSettings.RaspagerSettings SETTINGS = Settings.getTransmissionSettings()
			.getRaspagerSettings();

	public RaspagerClient(Socket socket, TransmitterDeviceListener deviceListener) {
		super(socket, deviceListener);
		// TODO Nope
		this.deviceType = DeviceType.RASPPAGER1;
		this.status = Status.OFFLINE;
		deviceProtocol = new RaspagerClientProtocol();
	}

	@Override
	public void run() {
		try {
			setupDeviceIO();

			LOGGER.info("Client connected.");

			deviceProtocol.handleWelcome(this, toServer, fromServer);
			LOGGER.info("Client online: {}", deviceType);
			deviceListener.handleTransmitterDeviceStarted(this);

			messageLoop();
		} catch (IOException e) {
			throwTransmitterDeviceOffline(new TransmitterDeviceException("Client error: " + e.getMessage()));
		} catch (TransmitterDeviceException e) {
			throwTransmitterDeviceException(e);
		} catch (InterruptedException e) {
		}

		cleanup();

		LOGGER.info("Client offline.");

		deviceListener.handleTransmitterDeviceStopped(this);
	}

	private void messageLoop() throws TransmitterDeviceException, IOException, InterruptedException {
		while (!thread.isInterrupted()) {
			// Blocking until Message is available
			Message message = getMessage();
			// Wait until next TX Slot
			// thread.sleep(getTimeToNextOpenSlot());

			int messageCount = 0;

			while (true) {
				deviceProtocol.handleMessage(message, toServer, fromServer);
				++messageCount;

				LOGGER.info("Successfully sent message \"{}\" to {} with {}", message.getText(), message.getAddress(),
						this);

				if (messageQueue.isEmpty() || ((messageCount + 1) > SETTINGS.getMaxMessageCount())) {
					break;
				} else if (thread.isInterrupted()) {
					throw new InterruptedException();
				} else {
					message = getMessage();
				}
			}
		}

		throw new InterruptedException();
	}

	private void cleanup() {
		disconnect();
		closeDeviceIO();
		thread = null;
	}

	@Override
	public String toString() {
		return name;
	}
}
