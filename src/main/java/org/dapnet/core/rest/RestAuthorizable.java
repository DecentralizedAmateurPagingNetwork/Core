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

import java.util.Set;

/**
 * Interface for objects that provide a list of owner names. This is used to
 * check if a user can modify an object if he owns it.
 * 
 * @author Philipp Thiel
 */
public interface RestAuthorizable {

	/**
	 * Gets a set of owner names.
	 * 
	 * @return Owner names
	 */
	Set<String> getOwnerNames();

}
