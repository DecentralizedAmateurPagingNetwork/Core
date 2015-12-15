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

import java.io.Serializable;

public class RestSettings implements Serializable {
    private String baseUrl = "http://localhost:8080/";

    public String getBaseUrl() {
        return baseUrl;
    }
}
