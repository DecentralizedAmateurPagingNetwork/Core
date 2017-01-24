package org.dapnet.core.transmission;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dapnet.core.model.Transmitter;

final class ClientStateHandlers {

	private ClientStateHandlers() {
	}

	public static class AckStateHandler implements ClientStateHandler {

		// Ack message #04 +
		private static final Pattern ackPattern = Pattern.compile("#(\\p{XDigit}+) (\\+)");

		@Override
		public void onReceive(TransmitterClient client, String msg) throws Exception {
			Matcher ackMatcher = ackPattern.matcher(msg);
			if (!ackMatcher.matches()) {
				throw new TransmitterDeviceException("Invalid response received.");
			}

			int seq = Integer.parseInt(ackMatcher.group(1), 16);
			String ack = ackMatcher.group(2);
			if (!ack.equals("+") || !client.ackSequenceNumber(seq)) {
				throw new TransmitterDeviceException("Unexpected response received.");
			}
		}

	}

	public static class LoginStateHandler implements ClientStateHandler {
		// Welcome string [RasPager v1.0-SCP-#2345678 abcde]
		private static final Pattern authPattern = Pattern
				.compile("\\[(\\w+) v(\\d+\\.\\d+[-#\\p{Alnum}]*) (\\p{Alnum}+)\\]");

		@Override
		public void onReceive(TransmitterClient client, String msg) throws Exception {
			Matcher authMatcher = authPattern.matcher(msg);
			if (!authMatcher.matches()) {
				throw new TransmitterDeviceException("Invalid welcome message format.");
			}

			String type = authMatcher.group(1);
			String version = authMatcher.group(2);
			String key = authMatcher.group(3);

			// TODO lookup key
			Transmitter t = manager.get(key);
			if (t == null) {
				throw new TransmitterDeviceException("The received auth key is not registered.");
			}

			t.setDeviceType(type);
			t.setDeviceVersion(version);

			client.setTransmitter(t);
		}

	}
}
