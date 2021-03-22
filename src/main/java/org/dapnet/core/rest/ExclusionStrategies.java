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

import org.dapnet.core.model.CallSign;
import org.dapnet.core.model.Transmitter;
import org.dapnet.core.model.User;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

/**
 * Defines gson exclusion strategies.
 * 
 * @author Philipp Thiel
 */
public final class ExclusionStrategies {

	/**
	 * The user exclusion strategy (limited access)
	 */
	public static final ExclusionStrategy USER = new UserStrategy();
	/**
	 * The admin exclusion strategy (full access, only passwords hidden)
	 */
	public static final ExclusionStrategy ADMIN = new AdminStrategy();

	private ExclusionStrategies() {
	}

	private static class UserStrategy implements ExclusionStrategy {

		@Override
		public boolean shouldSkipField(FieldAttributes f) {
			return (f.getDeclaringClass() == CallSign.class && f.getName().equals("pagers"))
					|| (f.getDeclaringClass() == Transmitter.class && f.getName().equals("authKey"))
					|| (f.getDeclaringClass() == User.class && f.getName().equals("hash"))
					|| (f.getDeclaringClass() == User.class && f.getName().equals("mail"));
		}

		@Override
		public boolean shouldSkipClass(Class<?> clazz) {
			return false;
		}
	}

	private static class AdminStrategy implements ExclusionStrategy {

		@Override
		public boolean shouldSkipField(FieldAttributes f) {
			return (f.getDeclaringClass() == User.class && f.getName().equals("hash"));
		}

		@Override
		public boolean shouldSkipClass(Class<?> clazz) {
			return false;
		}

	}

}