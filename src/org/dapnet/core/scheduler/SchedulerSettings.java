/*
 * DAPNET CORE PROJECT
 * Copyright (C) 2016
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

package org.dapnet.core.scheduler;

import java.io.Serializable;

public class SchedulerSettings implements Serializable {
    private String timeTransmissionCron = "15 0/20 * * * ?";
    private String rubricNameTransmissionCron = "15 0/20 * * * ?";

    public String getTimeTransmissionCron() {
        return timeTransmissionCron;
    }

    public String getRubricNameTransmissionCron() {
        return rubricNameTransmissionCron;
    }
}
