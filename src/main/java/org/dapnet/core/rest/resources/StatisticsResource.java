/*
 * DAPNET CORE PROJECT
 * Copyright (C) 2017
 */

package org.dapnet.core.rest.resources;

import java.util.concurrent.locks.Lock;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.dapnet.core.model.CoreRepository;
import org.dapnet.core.model.NewsList;
import org.dapnet.core.model.Node;
import org.dapnet.core.model.Transmitter;
import org.dapnet.core.rest.RestSecurity;

@Path("/stats")
@Produces(MediaType.APPLICATION_JSON)
public class StatisticsResource extends AbstractResource {

	@GET
	public Response get() throws Exception {
		RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.PUBLIC);

		final CoreRepository repo = getRepository();
		Lock lock = repo.getLock().readLock();
		lock.lock();

		try {
			ObjectCounts counts = new ObjectCounts(repo);
			return getObject(counts, status);
		} finally {
			lock.unlock();
		}
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

		public ObjectCounts(CoreRepository repo) {
			users = repo.getUsers().size();
			calls = repo.getCalls().size();
			callSigns = repo.getCallSigns().size();
			news = repo.getNews().values().stream().mapToInt(NewsList::getSize).sum();
			callsTotal = repo.getStatistics().getCalls();
			newsTotal = repo.getStatistics().getNews();
			rubrics = repo.getRubrics().size();
			nodesTotal = repo.getNodes().size();
			nodesOnline = (int) repo.getNodes().values().stream().filter(n -> n.getStatus() == Node.Status.ONLINE)
					.count();
			transmittersTotal = repo.getTransmitters().size();
			transmittersOnline = (int) repo.getTransmitters().values().stream()
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
