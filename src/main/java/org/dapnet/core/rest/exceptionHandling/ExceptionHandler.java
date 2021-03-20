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

package org.dapnet.core.rest.exceptionHandling;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonSyntaxException;

import jakarta.validation.ConstraintViolationException;

@Provider
public class ExceptionHandler implements ExceptionMapper<Exception> {
	private static final Logger logger = LogManager.getLogger();

	@Context
	UriInfo uri;
	@Context
	Request request;

	@Override
	public Response toResponse(Exception e) {
		ExceptionDescriptor descriptor;

		// ClientErrorExceptions
		if (e instanceof BadRequestException) {
			descriptor = new ExceptionDescriptor(4000, "Bad Request",
					"Malformed request syntax or invalid message framing");
		} else if (e instanceof ConstraintViolationException) {
			descriptor = new ConstraintViolationExceptionDescriptor(4001, "Constraint Violation",
					"Missing fields or invalid values", ((ConstraintViolationException) e).getConstraintViolations());
		} else if (e instanceof EmptyBodyException) {
			descriptor = new ExceptionDescriptor(4002, "Empty Body", "Request body does not contain any data");
		} else if (e instanceof InvalidAddressException) {
			// Fake ConstraintViolation
			descriptor = new ConstraintViolationExceptionDescriptor(4001, "Constraint Violation",
					"Missing fields or invalid values", new ConstraintViolationExceptionDescriptor.Violation(6103,
							"address", null, "must be a valid IpAddress"));
		} else if (e instanceof ForbiddenException) {
			descriptor = new ExceptionDescriptor(4030, "Forbidden", "No permission for this request");
		} else if (e instanceof JsonSyntaxException) {
			descriptor = new ExceptionDescriptor(4003, "Invalid Json", e.getCause().getMessage());
		} else if (e instanceof NotAcceptableException) {
			descriptor = new ExceptionDescriptor(4060, "Not Acceptable",
					"Requested response is not available in the requested form");
		} else if (e instanceof NotAllowedException) {
			descriptor = new ExceptionDescriptor(4050, "Method not allowed",
					"The resource does not support the request method");
		} else if (e instanceof NotAuthorizedException) {
			descriptor = new ExceptionDescriptor(4010, "Not Authorized", "Invalid or missing username or password");
		} else if (e instanceof NotFoundException) {
			descriptor = new ExceptionDescriptor(4040, "Not Found", "The requested resource could not be found");
			logger.error(request.getMethod() + " " + uri.getPath() + " - " + descriptor.getLogMessage(), e);
		} else if (e instanceof NotSupportedException) {
			descriptor = new ExceptionDescriptor(4150, "Not Supported",
					"The request entity media type is not supported");
		}
		// ServerErrorExceptions
		else if (e instanceof InternalServerErrorException) {
			descriptor = new ExceptionDescriptor(5000, "Internal Server Error",
					"A server-side error occurred while executing the request");
			logger.error(request.getMethod() + " " + uri.getPath() + " - " + descriptor.getLogMessage(), e);
		} else if (e instanceof NoQuorumException) {
			descriptor = new ExceptionDescriptor(5031, "No Quorum",
					"Method temporarily not available, because only a minority of nodes could be contacted");
		} else if (e instanceof ServiceUnavailableException) {
			descriptor = new ExceptionDescriptor(5030, "Service Unavailable",
					"A server-side error occurred while executing the request");
			logger.error(request.getMethod() + " " + uri.getPath() + " - " + descriptor.getLogMessage(), e);
		}
		// UnknownExceptions
		else {
			descriptor = new ExceptionDescriptor(5001, "Internal Server Error",
					"A unknown server-side error occurred while executing the request");
			logger.error(request.getMethod() + " " + uri.getPath() + " - " + descriptor.getLogMessage(), e);
		}

		return Response.status(descriptor.getCode() / 10).entity(descriptor.toJson()).type(MediaType.APPLICATION_JSON)
				.build();
	}
}
