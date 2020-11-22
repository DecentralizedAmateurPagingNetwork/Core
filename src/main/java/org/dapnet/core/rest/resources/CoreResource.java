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

package org.dapnet.core.rest.resources;

import java.util.concurrent.locks.Lock;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.dapnet.core.DAPNETCore;
import org.dapnet.core.model.State;
import org.dapnet.core.rest.RestSecurity;

@Path("/core")
@Produces(MediaType.APPLICATION_JSON)
public class CoreResource extends AbstractResource {

	public static final Version version = new Version();

	@Path("/core_version")
	@GET
	public Response getCoreVersion() throws Exception {
		Lock lock = State.getReadLock();
		lock.lock();

		try {
			RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.EVERYBODY);
			VersionWrapper version = new VersionWrapper(DAPNETCore.getCoreVersion());
			return getObject(version, status);
		} finally {
			lock.unlock();
		}
	}

	@Path("/api_version")
	@GET
	public Response getApiVersion() throws Exception {
		Lock lock = State.getReadLock();
		lock.lock();

		try {
			RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.EVERYBODY);
			VersionWrapper version = new VersionWrapper(DAPNETCore.getApiVersion());
			return getObject(version, status);
		} finally {
			lock.unlock();
		}
	}

	@Path("/version")
	@GET
	public Response getVersion() throws Exception {
		Lock lock = State.getReadLock();
		lock.lock();

		try {
			RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.EVERYBODY);
			return getObject(version, status);
		} finally {
			lock.unlock();
		}
	}

	// Workaround to get a proper JSON object
	private static final class VersionWrapper {

		@SuppressWarnings("unused")
		private final String version;

		public VersionWrapper(String version) {
			this.version = version;
		}

	}

	private static final class Version {
		@SuppressWarnings("unused")
		private final String core = DAPNETCore.getCoreVersion();
		@SuppressWarnings("unused")
		private final String api = DAPNETCore.getApiVersion();
	}

}
