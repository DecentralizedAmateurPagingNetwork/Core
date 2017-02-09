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

package org.dapnet.core.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.HashUtil;
import org.dapnet.core.model.User;

import javax.ws.rs.core.HttpHeaders;

public class RestSecurity {
	public enum SecurityLevel {
		ADMIN_ONLY, OWNER_ONLY, USER_ONLY, EVERYBODY
	}

	public enum SecurityStatus {
		UNAUTHORIZED, FORBIDDEN, ADMIN, OWNER, USER, ANYBODY, INTERNAL_ERROR
	}

	private static final Logger logger = LogManager.getLogger(RestSecurity.class.getName());
	private RestListener restListener;

	public RestSecurity(RestListener restListener) {
		this.restListener = restListener;
	}

	public SecurityStatus getStatus(HttpHeaders httpHeaders, SecurityLevel minSecurityLevel) {
		return getStatus(httpHeaders, minSecurityLevel, null);
	}

	public SecurityStatus getStatus(HttpHeaders httpHeaders, SecurityLevel minSecurityLevel,
			RestAuthorizable restAuthorizable) {
		// Get LoginData
		LoginData loginData;
		try {
			loginData = new LoginData(httpHeaders);
		} catch (Exception e) {
			// No Authorization Data in Http Header
			logger.info("No Authorization Data in HttpHeader");
			return checkAuthorization(minSecurityLevel, SecurityStatus.ANYBODY);
		}
		// Get User
		User user = restListener.getState().getUsers().get(loginData.getUsername());
		if (user == null) {
			logger.info("No User with such name");
			return SecurityStatus.UNAUTHORIZED;
		}
		// ValidatePassword
		boolean authenticated = false;
		try {
			authenticated = HashUtil.validatePassword(loginData.getPassword(), user.getHash());
		} catch (Exception e) {
			logger.error("Error while validating password", e);
			return SecurityStatus.INTERNAL_ERROR;
		}
		if (!authenticated) {
			logger.info("Wrong Password");
			return SecurityStatus.UNAUTHORIZED;
		}

		// Check if admin
		if (user.isAdmin()) {
			return checkAuthorization(minSecurityLevel, SecurityStatus.ADMIN);
		}

		// Check if owner
		if (isOwner(user.getName(), restAuthorizable)) {
			return checkAuthorization(minSecurityLevel, SecurityStatus.OWNER);
		}

		// Is User
		return checkAuthorization(minSecurityLevel, SecurityStatus.USER);
	}

	private boolean isOwner(String name, RestAuthorizable restAuthorizable) {
		if (restAuthorizable == null) {
			return false;
		}
		for (String ownerName : restAuthorizable.getOwnerNames()) {
			if (name.equals(ownerName)) {
				return true;
			}
		}
		return false;
	}

	// Check SecurityStatus against minSecurityLevel
	private SecurityStatus checkAuthorization(SecurityLevel minSecurityLevel, SecurityStatus givenSecurityStatus) {
		switch (givenSecurityStatus) {
		case ADMIN:
			return SecurityStatus.ADMIN;
		case OWNER:
			switch (minSecurityLevel) {
			case ADMIN_ONLY:
				return SecurityStatus.FORBIDDEN;
			default:
				return SecurityStatus.OWNER;
			}
		case USER:
			switch (minSecurityLevel) {
			case ADMIN_ONLY:
				return SecurityStatus.FORBIDDEN;
			case OWNER_ONLY:
				return SecurityStatus.FORBIDDEN;
			default:
				return SecurityStatus.USER;
			}
		case ANYBODY:
			switch (minSecurityLevel) {
			case ADMIN_ONLY:
				return SecurityStatus.FORBIDDEN;
			case OWNER_ONLY:
				return SecurityStatus.FORBIDDEN;
			case USER_ONLY:
				return SecurityStatus.FORBIDDEN;
			default:
				return SecurityStatus.ANYBODY;
			}
		case INTERNAL_ERROR:
			return SecurityStatus.INTERNAL_ERROR;
		default:
			return SecurityStatus.INTERNAL_ERROR;
		}
	}
}
