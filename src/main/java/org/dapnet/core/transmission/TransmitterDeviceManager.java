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

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.model.Transmitter;
import org.dapnet.core.model.Transmitter.DeviceMode;
import org.dapnet.core.model.TransmitterGroup;
import org.dapnet.core.model.list.SearchableArrayList;

public class TransmitterDeviceManager implements TransmitterDeviceListener {
	private static final Logger logger = LogManager.getLogger(TransmitterDeviceManager.class.getName());
	private final SearchableArrayList<TransmitterDevice> connectedTransmitterDevices = new SearchableArrayList<>();
	private final SearchableArrayList<TransmitterDevice> connectingTransmitterDevices = new SearchableArrayList<>();
	private final SearchableArrayList<TransmitterDevice> disconnectingTransmitterDevices = new SearchableArrayList<>();
	private TransmitterDeviceManagerListener listener;
	private boolean disconnectingFromAll = false;
	private boolean reconnecting = false;
	private List<Transmitter> reconnectTransmitters;

	public void setListener(TransmitterDeviceManagerListener listener) {
		this.listener = listener;
	}

	public void performReconnect(List<Transmitter> transmitters) {
		reconnecting = true;
		reconnectTransmitters = transmitters;
		disconnectFromAllTransmitters();
	}

	public void connectToTransmitters(List<Transmitter> transmitters) {
		disconnectingFromAll = false;
		for (Transmitter transmitter : transmitters)
			connectToTransmitter(transmitter);
		if (reconnecting) {
			reconnecting = false;
			reconnectTransmitters = null;
			logger.info("Finished reconnect operation");
		}
	}

	public void connectToTransmitter(Transmitter transmitter) {
		if (transmitter.getStatus() == Transmitter.Status.ERROR
				|| transmitter.getStatus() == Transmitter.Status.DISABLED) {
			return;
		}

		disconnectingFromAll = false;

		if (transmitter.getDeviceType() == Transmitter.DeviceType.XOS
				|| transmitter.getDeviceType() == Transmitter.DeviceType.RASPPAGER1
				|| transmitter.getDeviceType() == Transmitter.DeviceType.PR430
				|| transmitter.getDeviceType() == Transmitter.DeviceType.SDRPAGER
				|| transmitter.getDeviceType() == Transmitter.DeviceType.DV4MINI) {
			connectToRaspagerDerivative(transmitter);
		}
	}

	private void connectToRaspagerDerivative(Transmitter transmitter) {
		TransmitterDevice device = null;
		if (transmitter instanceof RaspagerClient) {
			device = (RaspagerClient) transmitter;
		} else if (transmitter.getDeviceMode() == DeviceMode.SERVER) {
			logger.info("Start connecting to " + transmitter.getName());

			switch (transmitter.getDeviceType()) {
			case XOS:
				device = new XOS(transmitter, this);
				break;
			case PR430:
				device = new PR430(transmitter, this);
				break;
			case RASPPAGER1:
				device = new Raspager1(transmitter, this);
				break;
			case SDRPAGER:
				device = new SDRPager(transmitter, this);
				break;
			case DV4MINI:
				device = new DV4mini(transmitter, this);
				break;
			default:
				throw new IllegalArgumentException("Unsupported transmitter device type.");
			}
		} else {
			throw new IllegalArgumentException("Cannot connect to transmitter.");
		}

		connectingTransmitterDevices.add(device);
		device.start();
	}

	public synchronized void disconnectFromTransmitter(Transmitter transmitter) {
		if (connectedTransmitterDevices.contains(transmitter.getName())) {
			logger.info("Start disconnecting from " + transmitter.getName());
			TransmitterDevice device = connectedTransmitterDevices.findByName(transmitter.getName());
			device.stop();
			// Move Device to disconnecting List
			connectedTransmitterDevices.remove(device);
			disconnectingTransmitterDevices.add(device);
		} else if (connectingTransmitterDevices.contains(transmitter.getName())) {
			logger.info("Start disconnecting from " + transmitter.getName());
			TransmitterDevice device = connectingTransmitterDevices.findByName(transmitter.getName());
			device.stop();
			// Move Device to disconnecting List
			connectingTransmitterDevices.remove(device);
			disconnectingTransmitterDevices.add(device);
		} else {
			logger.warn("Cannot disconnect from transmitter " + transmitter.getName()
					+ " because it is not connected or connecting");
		}
	}

	public void disconnectFromAllTransmitters() {
		disconnectingFromAll = true;

		// No connected Devices?
		if (connectingTransmitterDevices.isEmpty() && connectedTransmitterDevices.isEmpty()
				&& disconnectingTransmitterDevices.isEmpty()) {
			logger.info("Successfully disconnected from all Transmitters");

			if (reconnecting) {
				connectToTransmitters(reconnectTransmitters);
			} else {
				listener.handleDisconnectedFromAllTransmitters();
			}
		}

		while (!connectingTransmitterDevices.isEmpty()) {
			disconnectFromTransmitter(connectingTransmitterDevices.get(0));
		}
		while (!connectedTransmitterDevices.isEmpty()) {
			disconnectFromTransmitter(connectedTransmitterDevices.get(0));
		}
	}

	public List<TransmitterDevice> getTransmitterDevices() {
		return connectedTransmitterDevices;
	}

	public List<TransmitterDevice> getTransmitterDevices(List<TransmitterGroup> transmitterGroups) {
		List<TransmitterDevice> transmitterDevices = new ArrayList<>();
		List<String> transmitterNames = getTransmitterNames(transmitterGroups);
		for (TransmitterDevice transmitterDevice : connectedTransmitterDevices) {
			if (transmitterNames.contains(transmitterDevice.getName())) {
				transmitterDevices.add(transmitterDevice);
			}
		}
		return transmitterDevices;
	}

	private List<String> getTransmitterNames(List<TransmitterGroup> transmitterGroups) {
		try {
			ArrayList<String> transmitter = new ArrayList<>();

			for (TransmitterGroup transmitterGroup : transmitterGroups) {
				transmitter.addAll(transmitterGroup.getTransmitterNames());
			}

			return transmitter;
		} catch (Exception e) {
			logger.error("Failed to get TransmitterNames", e);
			return null;
		}
	}

	public void handleTransmitterDeviceError(TransmitterDevice transmitterDevice, TransmitterDeviceException e) {
		logger.warn(transmitterDevice.getName() + " throw Exception: " + e.getMessage());

		// Set Device Status
		transmitterDevice.setStatus(Transmitter.Status.ERROR);
		if (listener != null) {
			logger.info("Setting status of " + transmitterDevice.getName() + " to " + Transmitter.Status.ERROR);
			listener.handleTransmitterStatusChanged(transmitterDevice.getName(), transmitterDevice.getStatus());
		}
	}

	public void handleTransmitterDeviceOffline(TransmitterDevice transmitterDevice, TransmitterDeviceException e) {
		logger.warn(transmitterDevice.getName() + " is offline due to an exception: " + e.getMessage());

		if (connectedTransmitterDevices.contains(transmitterDevice)) {
			connectedTransmitterDevices.remove(transmitterDevice);

			// Move device to connecting list if it is a server device
			if (transmitterDevice.getDeviceMode() == DeviceMode.SERVER) {
				connectingTransmitterDevices.add(transmitterDevice);
			}

			// Set DeviceStatus
			transmitterDevice.setStatus(Transmitter.Status.OFFLINE);
			if (listener != null) {
				logger.info("Setting status of " + transmitterDevice.getName() + " to " + Transmitter.Status.OFFLINE);
				listener.handleTransmitterStatusChanged(transmitterDevice.getName(), transmitterDevice.getStatus());
			}
		} else {
			logger.error("Unknown TransmitterDevice " + transmitterDevice.getName() + " goes offline");
		}
	}

	@Override
	public void handleTransmitterDeviceStarted(TransmitterDevice transmitterDevice) {
		if (connectingTransmitterDevices.contains(transmitterDevice)) {
			logger.info("Connected to " + transmitterDevice.getName() + " successfully");

			// Move Device to connected List
			connectingTransmitterDevices.remove(transmitterDevice);
			connectedTransmitterDevices.add(transmitterDevice);

			// Set DeviceStatus
			transmitterDevice.setStatus(Transmitter.Status.ONLINE);
			if (listener != null) {
				logger.info(
						"Setting status of " + transmitterDevice.getName() + " to " + transmitterDevice.getStatus());
				listener.handleTransmitterStatusChanged(transmitterDevice.getName(), transmitterDevice.getStatus());
			}
		} else {
			logger.error("Unknown TransmitterDevice " + transmitterDevice.getName() + " connected");
		}
	}

	@Override
	public void handleTransmitterDeviceStopped(TransmitterDevice transmitterDevice) {
		// Log and change Status
		switch (transmitterDevice.getStatus()) {
		case ERROR:
			logger.info("Disconnected from " + transmitterDevice.getName() + " after Error");
			break;
		case ONLINE:
			logger.info("Disconnected successfully from " + transmitterDevice.getName());

			// Set Device Status
			transmitterDevice.setStatus(Transmitter.Status.OFFLINE);
			if (listener != null) {
				logger.info(
						"Setting status of " + transmitterDevice.getName() + " to " + transmitterDevice.getStatus());
				listener.handleTransmitterStatusChanged(transmitterDevice.getName(), transmitterDevice.getStatus());
			}
			break;
		case OFFLINE:
			logger.info("Disconnected from " + transmitterDevice.getName() + " before Connection established");
			break;
		default:
			break;
		}

		// Remove from TransmitterDeviceLists (remove returns false if device
		// does not exist)
		connectingTransmitterDevices.remove(transmitterDevice);
		connectedTransmitterDevices.remove(transmitterDevice);
		disconnectingTransmitterDevices.remove(transmitterDevice);

		// DisconnectingFromAll completed?
		if (disconnectingFromAll && connectingTransmitterDevices.isEmpty() && connectedTransmitterDevices.isEmpty()
				&& disconnectingTransmitterDevices.isEmpty()) {
			logger.info("Successfully disconnected from all Transmitters");
			if (reconnecting) {
				connectToTransmitters(reconnectTransmitters);
			} else {
				listener.handleDisconnectedFromAllTransmitters();
			}
		}
	}
}
