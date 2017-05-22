/*
 * DAPNET CORE PROJECT
 * Copyright (C) 2017
 */

package org.dapnet.core.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.dapnet.core.model.NewsList;
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
		return getObject(new ObjectCounts(restListener.getState()), status);
	}

	public static final class ObjectCounts {
		private final int users;
		private final int calls;
		private final long callsTotal;
		private final int callSigns;
		private final int news;
		private final long newsTotal;
		private final int rubrics;
		private final int nodesTotal;
		private final int nodesOnline;
		private final int transmittersTotal;
		private final int transmittersOnline;

		public ObjectCounts(State state) {
			users = state.getUsers().size();
			calls = state.getCalls().size();
			callSigns = state.getCallSigns().size();
			news = state.getNews().values().stream().mapToInt(NewsList::getSize).sum();
			callsTotal = state.getCoreStats().getCalls();
			newsTotal = state.getCoreStats().getNews();
			rubrics = state.getRubrics().size();
			nodesTotal = state.getNodes().size();
			nodesOnline = (int) state.getNodes().values().stream().filter(n -> n.getStatus() == Node.Status.ONLINE)
					.count();
			transmittersTotal = state.getTransmitters().size();
			transmittersOnline = (int) state.getTransmitters().values().stream()
					.filter(t -> t.getStatus() == Transmitter.Status.ONLINE).count();
		}

		public int getUsers() {
			return users;
		}

		public int getCalls() {
			return calls;
		}

		public long getCallsTotal() {
			return callsTotal;
		}

		public int getCallSigns() {
			return callSigns;
		}

		public int getNews() {
			return news;
		}

		public long getNewsTotal() {
			return newsTotal;
		}

		public int getRubrics() {
			return rubrics;
		}

		public int getNodesTotal() {
			return nodesTotal;
		}

		public int getNodesOnline() {
			return nodesOnline;
		}

		public int getTransmittersTotal() {
			return transmittersTotal;
		}

		public int getTransmittersOnline() {
			return transmittersOnline;
		}

	}

}
