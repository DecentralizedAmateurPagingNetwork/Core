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

package org.dapnet.core.model;

import org.dapnet.core.model.validator.Cron;

import javax.validation.constraints.Min;
import java.io.Serializable;

public class Scheduling implements Serializable{
    @Min(value = 0)
    int repeatIntervallInMinutes = 0;

    @Cron
    String cron;

    public int getRepeatIntervallInMinutes() {
        return repeatIntervallInMinutes;
    }

    public String getCron() {
        return cron;
    }
}
