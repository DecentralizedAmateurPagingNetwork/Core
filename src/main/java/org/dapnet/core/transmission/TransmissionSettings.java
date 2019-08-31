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

import java.io.Serializable;

public final class TransmissionSettings implements Serializable {
	private static final long serialVersionUID = 6990520510506509511L;
	private PagingProtocolSettings pagingProtocolSettings = new PagingProtocolSettings();
	private ServerSettings serverSettings = new ServerSettings();

	public PagingProtocolSettings getPagingProtocolSettings() {
		return pagingProtocolSettings;
	}

	public ServerSettings getServerSettings() {
		return serverSettings;
	}

	public final class PagingProtocolSettings implements Serializable {
		private static final long serialVersionUID = 2535179621136596934L;
		private int numberOfSyncLoops = 5;
		private int sendSpeed = 1;// 0: 512, 1: 1200, 2:2400
		private String activationCode = "0 7 50,0 7 34,0 7 53,0 7 51,0 7 51,0 7 52,0 7 52,0 7 56";

		public int getNumberOfSyncLoops() {
			return numberOfSyncLoops;
		}

		public int getSendSpeed() {
			return sendSpeed;
		}

		public String getActivationCode() {
			return activationCode;
		}
	}

	public final class ServerSettings implements Serializable {
		private static final long serialVersionUID = 5452321606678683312L;
		private int port = 43434;

		public int getPort() {
			return port;
		}
	}
}
