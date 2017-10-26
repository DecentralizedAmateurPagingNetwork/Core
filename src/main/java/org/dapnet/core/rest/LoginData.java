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

import java.io.UnsupportedEncodingException;
import java.util.Base64;

import javax.ws.rs.core.HttpHeaders;

public class LoginData {
	private String username;
	private String password;

	public LoginData(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public LoginData(HttpHeaders httpHeaders) throws UnsupportedEncodingException {
		this(httpHeaders.getRequestHeader("Authorization").get(0));
	}

	public LoginData(String authorizationToken) throws UnsupportedEncodingException {
		String encodedUserPassword = authorizationToken.replaceFirst("Basic ", "");
		byte[] decodedBytes = Base64.getDecoder().decode(encodedUserPassword);
		String usernameAndPassword = new String(decodedBytes, "UTF-8");

		int index = usernameAndPassword.indexOf(':');
		if (index == -1) {
			throw new IllegalArgumentException("Invalid basic auth format.");
		}

		this.username = usernameAndPassword.substring(0, index);
		this.password = usernameAndPassword.substring(index + 1);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}