package org.dapnet.core.rest;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

/**
 * HTTP Options filter implementation.
 * 
 * @author Philipp Thiel
 */
@Provider
@PreMatching
public class OptionsFilter implements ContainerRequestFilter {

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
			requestContext.abortWith(Response.status(Status.NO_CONTENT).build());
		}
	}

}
