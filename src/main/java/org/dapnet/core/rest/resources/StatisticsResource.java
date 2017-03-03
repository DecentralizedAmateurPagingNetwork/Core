package org.dapnet.core.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.dapnet.core.model.Node;
import org.dapnet.core.model.State;
import org.dapnet.core.model.Transmitter;
import org.dapnet.core.rest.RestSecurity;

@Path("/stats")
@Produces(MediaType.APPLICATION_JSON)
public class StatisticsResource extends AbstractResource {

	@GET
	public Response get() throws Exception {
		RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.EVERYBODY);

		State state = restListener.getState();
		Data data = new Data();
		data.users = state.getUsers().size();
		data.calls = state.getCalls().size();
		data.callSigns = state.getCallSigns().size();
		data.news = state.getNews().size();
		data.rubrics = state.getRubrics().size();
		data.nodesTotal = state.getNodes().size();
		data.nodesOnline = (int) state.getNodes().values().stream().filter(n -> n.getStatus() == Node.Status.ONLINE)
				.count();
		data.transmittersTotal = state.getTransmitters().size();
		data.transmittersOnline = (int) state.getTransmitters().values().stream()
				.filter(t -> t.getStatus() == Transmitter.Status.ONLINE).count();

		return getObject(data, status);
	}

	@SuppressWarnings("unused")
	private final class Data {
		public int users;
		public int calls;
		public int callSigns;
		public int news;
		public int rubrics;
		public int nodesTotal;
		public int nodesOnline;
		public int transmittersTotal;
		public int transmittersOnline;
	}

}
