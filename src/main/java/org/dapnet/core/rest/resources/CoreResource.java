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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.dapnet.core.Program;
import org.dapnet.core.rest.RestSecurity;

@Path("/core")
@Produces(MediaType.APPLICATION_JSON)
public class CoreResource extends AbstractResource {

	private static final Version version = new Version();

	@Path("/core_version")
	@GET
	public Response getCoreVersion() throws Exception {
		RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.PUBLIC);
		VersionWrapper version = new VersionWrapper(Program.getCoreVersion());
		return getObject(version, status);
	}

	@Path("/api_version")
	@GET
	public Response getApiVersion() throws Exception {
		RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.PUBLIC);
		VersionWrapper version = new VersionWrapper(Program.getApiVersion());
		return getObject(version, status);
	}

	@Path("/version")
	@GET
	public Response getVersion() throws Exception {
		RestSecurity.SecurityStatus status = checkAuthorization(RestSecurity.SecurityLevel.PUBLIC);
		return getObject(version, status);
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
		private final String core = Program.getCoreVersion();
		@SuppressWarnings("unused")
		private final String api = Program.getApiVersion();
	}

}
