package org.dapnet.core.transmission;

interface ClientStateHandler {

	void onReceive(TransmitterClient client, String msg) throws Exception;
}
