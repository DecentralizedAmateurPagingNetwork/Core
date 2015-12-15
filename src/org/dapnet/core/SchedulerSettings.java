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

package org.dapnet.core;

import java.io.Serializable;

public class SchedulerSettings implements Serializable {
    private int timeTransmissionPeriod = 20 * 60 * 1000;
    private int rubricTransmissionTimer = 6 * 60 * 60 * 1000;

    public int getTimeTransmissionPeriod() {
        return timeTransmissionPeriod;
    }

    public int getRubricTransmissionTimer() {
        return rubricTransmissionTimer;
    }
}
