package org.dapnet.core.rest.resources;

import java.util.concurrent.locks.Lock;

import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.dapnet.core.model.State;
import org.dapnet.core.model.Transmitter;
import org.dapnet.core.rest.RestSecurity;
import org.dapnet.core.rest.exceptionHandling.EmptyBodyException;

@Path("/transmitterControl")
public class TransmitterControl extends AbstractResource {

	@Path("sendRubricNames/{transmitter}")
	@GET
	public Response sendRubricNames(@PathParam("transmitter") String transmitterName) throws Exception {
		Lock lock = State.getReadLock();
		lock.lock();

		Transmitter transmitter = null;

		try {
			transmitter = restListener.getState().getTransmitters().get(transmitterName);
			if (transmitter != null) {
				checkAuthorization(RestSecurity.SecurityLevel.OWNER_ONLY, transmitter);
			} else {
				throw new EmptyBodyException();
			}
		} finally {
			lock.unlock();
		}

		if (restListener.handleStateOperation(null, "sendRubricNames", new Object[] { transmitterName },
				new Class[] { String.class })) {
			return Response.ok().build();
		} else {
			throw new InternalServerErrorException();
		}
	}
}
