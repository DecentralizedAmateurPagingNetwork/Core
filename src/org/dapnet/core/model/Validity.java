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

import org.dapnet.core.model.validator.ValidityDate;

import java.io.Serializable;
import java.util.Date;

@ValidityDate
public class Validity implements Serializable {
    Date start;
    Date end;

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }
}
