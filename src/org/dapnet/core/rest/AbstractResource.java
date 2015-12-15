/*
 * DAPNET CORE PROJECT
 * Copyright (C) 2015
 *
 * Daniel Sialkowski
 *
 * daniel.sialkowski@rwth-aachen.de
 *
 * Institut für Hochfrequenztechnik
 * RWTH AACHEN UNIVERSITY
 * Melatener Str. 25
 * 52074 Aachen
 */

package org.dapnet.core.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.model.list.Searchable;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Set;

public abstract class AbstractResource {
    //Gsons with different Exclusion Strategies
    protected static final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    protected static final Gson userGson = new GsonBuilder().setPrettyPrinting()
            .setExclusionStrategies(new UserExclusionStrategy()).create();

    protected Gson getExclusionGson(RestSecurity.SecurityStatus status) {
        //Add here other Exclusion Strategies
        switch (status) {
            case ADMIN:
                return gson;
            case OWNER:
                return gson;
            case USER:
                return userGson;
            case ANYBODY:
                return gson;
            default:
                return gson;
        }
    }


    //Validator
    protected static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();


    //Resources are created with Jersey, cannot pass parameters, so using instead static attributes
    protected static RestListener restListener;
    protected static RestSecurity restSecurity;

    public static void setRestListener(RestListener restListenerPar) {
        restListener = restListenerPar;
    }

    public static void setRestSecurity(RestSecurity restSecurityPar) {
        restSecurity = restSecurityPar;
    }


    //Logging
    private static final Logger logger = LogManager.getLogger(AbstractResource.class.getName());
    protected Logger getLogger()
    {
        return logger;
    }

    protected void logResponse(Response response, RestMethod method, String object) {
        String objectString = object != null ? " " + object : "";
        if (response == null) {
            getLogger().error(method + objectString + ": Internal Server Error: No Response");
        } else if (response.getStatusInfo() == Response.Status.INTERNAL_SERVER_ERROR) {
            getLogger().error(method + objectString + ": " + response.getStatusInfo());
        } else if (response.getStatusInfo() == Response.Status.OK
                || response.getStatusInfo() == Response.Status.CREATED) {
            getLogger().info(method + objectString + ": " + response.getStatusInfo());
        } else {
            getLogger().warn(method + objectString + ": " + response.getStatusInfo());
        }
    }

    protected void logResponse(Response response, RestMethod method) {
        logResponse(response, method, null);
    }

    protected enum RestMethod {
        GET, POST, PUT, DELETE
    }


    //Universal ApiMethods
    protected Response getObject(HttpHeaders httpHeaders, Object object, RestSecurity.SecurityLevel level,
                                 RestAuthorizable restAuthorizable) {
        Response response = null;
        try {
            //Check Authorization and Authentication
            RestSecurity.SecurityStatus status = null;
            if (level != RestSecurity.SecurityLevel.OWNER_ONLY) {
                status = restSecurity.getStatus(httpHeaders, level);
            } else {
                status = restSecurity.getStatus(httpHeaders, level, restAuthorizable);
            }

            if (isErrorSecurityStatus(status)) {
                return response = buildErrorSecurityStatusResponse(status);
            }

            //Validation
            if (object == null) {
                return response = Response.status(Response.Status.NOT_FOUND).build();
            }

            return response = Response.status(Response.Status.OK)
                    .entity(getExclusionGson(status).toJson(object)).build();
        }
        //Internal Error
        catch (Exception e) {
            e.printStackTrace();
            return response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    public Response handleObject(HttpHeaders httpHeaders, UriInfo uriInfo, Object object,
                                 RestSecurity.SecurityLevel level, RestAuthorizable restAuthorizable,
                                 boolean creation, String methodName, boolean quorumNeeded) {
        Response response = null;
        try {
            //Check Authorization and Authentication
            RestSecurity.SecurityStatus status = null;
            if (level != RestSecurity.SecurityLevel.OWNER_ONLY) {
                status = restSecurity.getStatus(httpHeaders, level);
            } else {
                status = restSecurity.getStatus(httpHeaders, level, restAuthorizable);
            }

            if (isErrorSecurityStatus(status)) {
                return response = buildErrorSecurityStatusResponse(status);
            }

            //Check Quorum
            if (quorumNeeded && !restListener.isQuorum()) {
                return response = Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
            }

            //Validation
            if (object == null) {
                return response = Response.status(Response.Status.BAD_REQUEST).build();
            }
            Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object);
            if (constraintViolations.size() != 0) {
                return response = buildArgumentErrorResponse(constraintViolations);
            }

            //Send to Cluster
            if (restListener.handleStateOperation(null, methodName,
                    new Object[]{object}, new Class[]{object.getClass()})) {
                if (creation) {
                    URI uri = uriInfo.getAbsolutePathBuilder().build();
                    return response = Response.created(uri).entity(gson.toJson(object)).build();
                } else {
                    return response = Response.ok(gson.toJson(object)).build();
                }
            } else
                return response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        //Internal Error
        catch (Exception e) {
            e.printStackTrace();
            return response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    protected Response deleteObject(HttpHeaders httpHeaders, Searchable object, RestSecurity.SecurityLevel level,
                                    RestAuthorizable restAuthorizable, String methodName, boolean quorumNeeded) {
        Response response = null;
        try {
            //Check Authorization and Authentication
            RestSecurity.SecurityStatus status = null;
            if (level != RestSecurity.SecurityLevel.OWNER_ONLY) {
                status = restSecurity.getStatus(httpHeaders, level);
            } else {
                status = restSecurity.getStatus(httpHeaders, level, restAuthorizable);
            }

            if (isErrorSecurityStatus(status)) {
                return response = buildErrorSecurityStatusResponse(status);
            }

            //Check Quorum
            if (quorumNeeded && !restListener.isQuorum()) {
                return response = Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
            }

            //Validation
            if (object == null) {
                return response = Response.status(Response.Status.NOT_FOUND).build();
            }

            //Send to Cluster
            if (restListener.handleStateOperation(null, methodName,
                    new Object[]{object.getName()}, new Class[]{String.class}))
                return response = Response.status(Response.Status.OK).entity(gson.toJson(object)).build();
            else
                return response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        //Internal Error
        catch (Exception e) {
            e.printStackTrace();
            return response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Helper
    protected Response buildErrorSecurityStatusResponse(RestSecurity.SecurityStatus status) {
        switch (status) {
            case INTERNAL_ERROR:
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            case UNAUTHORIZED:
                return Response.status(Response.Status.UNAUTHORIZED).build();
            case FORBIDDEN:
                return Response.status(Response.Status.FORBIDDEN).build();
            default:
                return null;
        }
    }

    protected Response buildArgumentErrorResponse(Set<ConstraintViolation<Object>> constraintViolations) {
        String response = "";
        for (ConstraintViolation<Object> violation : constraintViolations) {
            response = response + violation.getPropertyPath() + " " + violation.getMessage() + "\n";
        }
        return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
    }

    protected boolean isErrorSecurityStatus(RestSecurity.SecurityStatus status) {
        switch (status) {
            case INTERNAL_ERROR:
                return true;
            case UNAUTHORIZED:
                return true;
            case FORBIDDEN:
                return true;
            default:
                return false;
        }
    }
}
