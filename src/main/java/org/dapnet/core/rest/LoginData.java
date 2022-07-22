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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.HttpHeaders;

/**
 * This class holds login data as extracted from the HTTP request.
 * 
 * @author Philipp Thiel
 */
public class LoginData {
	/**
	 * Authorization header name.
	 */
	public static final String AUTHORIZATION_HEADER = "Authorization";

	private String username;
	private String password;

	/**
	 * Constructs a new login data object.
	 * 
	 * @param username User name
	 * @param password Password
	 */
	public LoginData(String username, String password) {
		this.username = Objects.requireNonNull(username, "User name must not be null.");
		this.password = Objects.requireNonNull(password, "Password must not be null.");
	}

	/**
	 * Gets the username.
	 * 
	 * @return Username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the username.
	 * 
	 * @param username Username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Extracts the login data from the given basic auth token.
	 * 
	 * @param authenticationToken Authentication token
	 * @return Login data instance or {@code null} if no login data could be
	 *         extracted
	 */
	public static LoginData fromToken(String authenticationToken) {
		if (authenticationToken == null || authenticationToken.isBlank()) {
			return null;
		}

		String encodedUserPassword = authenticationToken.replaceFirst("Basic ", "");
		byte[] decodedBytes = Base64.getDecoder().decode(encodedUserPassword);
		String usernameAndPassword = new String(decodedBytes, StandardCharsets.UTF_8);

		int index = usernameAndPassword.indexOf(':');
		if (index == -1) {
			throw new IllegalArgumentException("Invalid basic auth format.");
		}

		final String username = usernameAndPassword.substring(0, index);
		final String password = usernameAndPassword.substring(index + 1);

		return new LoginData(username, password);
	}

	/**
	 * Extracts the login data from the given HTTP headers. This will try to extract
	 * basic auth information from the authorization header.
	 * 
	 * @param headers HTTP headers to extract the login data from
	 * @return Login data instance or {@code null} if no login data could be
	 *         extracted
	 */
	public static LoginData fromHttpHeaders(HttpHeaders headers) {
		final List<String> authorization = headers.getRequestHeader(AUTHORIZATION_HEADER);
		if (authorization == null || authorization.isEmpty()) {
			return null;
		}

		return fromToken(authorization.get(0));
	}
}