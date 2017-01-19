package org.dapnet.core.transmission;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.Settings;

public class Server implements Runnable, AutoCloseable {

	private static final Logger LOGGER = LogManager.getLogger(Server.class);
	private final int port;
	private final TransmitterDeviceManager deviceManager;
	private volatile boolean shutdownRequested = false;
	private volatile ServerSocket serverSocket;

	public Server(TransmitterDeviceManager deviceManager) {
		this.port = Settings.getTransmissionSettings().getServerSettings().getPort();
		this.deviceManager = deviceManager;
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (Exception ex) {
			LOGGER.fatal("Failed to start server.", ex);
			return;
		}

		LOGGER.info("Server started on port: {}", port);

		while (!shutdownRequested) {
			Socket clsock = null;
			try {
				clsock = serverSocket.accept();
			} catch (Exception ex) {
				if (!shutdownRequested) {
					LOGGER.error("Exception in server.", ex);
				}
			}

			if (clsock != null) {
				handleClient(clsock);
			}
		}

		try {
			if (serverSocket != null) {
				serverSocket.close();
				serverSocket = null;
			}
		} catch (IOException ex) {
			LOGGER.error("Failed to close the server socket", ex);
		}

		LOGGER.info("Server stopped.");
	}

	private void handleClient(Socket socket) {
		try {
			RaspagerClient client = new RaspagerClient(socket, deviceManager);
			deviceManager.connectToTransmitter(client);
		} catch (Exception ex) {
			LOGGER.error("Failed to create client instance.", ex);

			try {
				socket.close();
			} catch (Exception ex2) {
				LOGGER.error("Failed to close client socket.", ex2);
			}
		}
	}

	@Override
	public void close() throws Exception {
		shutdownRequested = true;

		if (serverSocket != null) {
			serverSocket.close();
		}
	}

}
