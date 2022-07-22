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

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.dapnet.core.cluster.RemoteMethods;
import org.dapnet.core.model.CoreRepository;
import org.dapnet.core.rest.JsonConverter;
import org.dapnet.core.rest.RestAuthorizable;
import org.dapnet.core.rest.RestSecurity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

public abstract class AbstractResource {
	@Context
	protected UriInfo uriInfo;
	@Context
	protected HttpHeaders httpHeaders;

	@Inject
	private CoreRepository repository;
	@Inject
	private RestSecurity restSecurity;
	@Inject
	private RemoteMethods rpcMethods;
	@Inject
	private JsonConverter jsonConverter;

	protected CoreRepository getRepository() {
		return repository;
	}

	protected RemoteMethods getRpcMethods() {
		return rpcMethods;
	}

	protected JsonConverter getJsonConverter() {
		return jsonConverter;
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

	protected <T> void validateObject(T object) {
		Set<ConstraintViolation<T>> constraintViolations = repository.validate(object);
		if (!constraintViolations.isEmpty()) {
			throw new ConstraintViolationException(constraintViolations);
		}
	}

	// Operation Helper
	protected Response getObject(Object object, RestSecurity.SecurityStatus status) throws Exception {
		if (object == null) {
			throw new NotFoundException();
		}

		return Response.ok(jsonConverter.toJson(object, status)).build();
	}

}
