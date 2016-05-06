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

package org.dapnet.core.model;

import java.io.Serializable;

public class ModelSettings implements Serializable {
    private String stateFile = "data/State.json";
    private boolean savingImmediately = false;

    public String getStateFile() {
        return stateFile;
    }

    public boolean isSavingImmediately() {
        return savingImmediately;
    }
}
