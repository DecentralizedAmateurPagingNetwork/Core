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

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.dapnet.core.model.NamedObject;
import org.dapnet.core.rest.ExclusionStrategies;
import org.dapnet.core.rest.GsonTypeAdapterFactory;
import org.dapnet.core.rest.RestAuthorizable;
import org.dapnet.core.rest.RestListener;
import org.dapnet.core.rest.RestSecurity;
import org.dapnet.core.rest.exceptionHandling.EmptyBodyException;
import org.dapnet.core.rest.exceptionHandling.NoQuorumException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class AbstractResource {
	@Context
	UriInfo uriInfo;
	@Context
	HttpHeaders httpHeaders;

	protected static final Gson gson;
	protected static final Gson userGson;
	// Resources are created with Jersey, cannot pass parameters, so using
	// instead static attributes
	protected static volatile RestListener restListener;
	protected static volatile RestSecurity restSecurity;

	static {
		gson = createBuilder().addSerializationExclusionStrategy(ExclusionStrategies.ADMIN).create();
		userGson = createBuilder().setExclusionStrategies(ExclusionStrategies.USER).create();
	}

	private static GsonBuilder createBuilder() {
		GsonBuilder build = new GsonBuilder();
		build.serializeNulls();
		build.setPrettyPrinting();
		build.registerTypeAdapterFactory(new GsonTypeAdapterFactory());

		return build;
	}

	protected Gson getExclusionGson(RestSecurity.SecurityStatus status) {
		switch (status) {
		case ADMIN:
			return gson;
		case OWNER:
			return gson;
		case USER:
			return userGson;
		case ANYBODY:
			return userGson;
		default:
			return gson;
		}
	}

	public static void setRestListener(RestListener restListenerPar) {
		restListener = restListenerPar;
	}

	public static void setRestSecurity(RestSecurity restSecurityPar) {
		restSecurity = restSecurityPar;
	}

	// Authorization Helper
	protected RestSecurity.SecurityStatus checkAuthorization(RestSecurity.SecurityLevel level,
			RestAuthorizable restAuthorizable) throws Exception {
		RestSecurity.SecurityStatus status = restSecurity.getStatus(httpHeaders, level, restAuthorizable);

		switch (status) {
		case INTERNAL_ERROR:
			throw new InternalServerErrorException();
		case UNAUTHORIZED:
			throw new NotAuthorizedException(Response.status(Response.Status.UNAUTHORIZED).build());
		case FORBIDDEN:
			throw new ForbiddenException();
		default:
			return status;
		}
	}

	protected RestSecurity.SecurityStatus checkAuthorization(RestSecurity.SecurityLevel level) throws Exception {
		return checkAuthorization(level, null);
	}

	// Validation Helper
	protected static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	protected void validateObject(Object object) {
		Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object);
		if (constraintViolations.size() != 0) {
			throw new ConstraintViolationException(constraintViolations);
		}
	}

	// Operation Helper
	protected Response getObject(Object object, RestSecurity.SecurityStatus status) throws Exception {
		if (object == null) {
			throw new NotFoundException();
		}

		return Response.ok(getExclusionGson(status).toJson(object)).build();
	}

	public Response handleObject(Object object, String methodName, boolean creation, boolean quorumNeeded)
			throws Exception {
		// Check Quorum
		if (quorumNeeded && !restListener.isQuorum()) {
			throw new NoQuorumException();
		}

		// Validation
		if (object == null) {
			throw new EmptyBodyException();
		}

		validateObject(object);

		// Send to Cluster
		if (restListener.handleStateOperation(null, methodName, new Object[] { object },
				new Class[] { object.getClass() })) {
			if (creation) {
				return Response.created(uriInfo.getAbsolutePath()).entity(gson.toJson(object)).build();
			} else {
				return Response.ok(gson.toJson(object)).build();
			}
		} else {
			throw new InternalServerErrorException();
		}
	}

	protected Response deleteObject(NamedObject object, String methodName, boolean quorumNeeded) throws Exception {
		// Check Quorum
		if (quorumNeeded && !restListener.isQuorum()) {
			throw new NoQuorumException();
		}

		// Validation
		if (object == null) {
			throw new NotFoundException();
		}

		// Send to Cluster
		if (restListener.handleStateOperation(null, methodName, new Object[] { object.getNormalizedName() },
				new Class[] { String.class })) {
			// TODO Why do we return the deleted object here?
			return Response.ok(gson.toJson(object)).build();
		} else {
			throw new InternalServerErrorException();
		}
	}
}
