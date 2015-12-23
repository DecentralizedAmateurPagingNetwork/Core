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

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Activation implements Serializable {
    //No ID
    @NotNull(message = "nicht vorhanden")
    @Min(value = 0)
    @Max(value = 2097151)
    private int number;

    @NotNull(message = "nicht vorhanden")
    @Size(min = 1, message = "müssen mindestens einen transmitterGroupNamen enthalten")
    private List<String> transmitterGroupNames;

    //Internally set
    @NotNull(message = "nicht vorhanden")
    private Date timestamp;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public List<String> getTransmitterGroupNames() {
        return transmitterGroupNames;
    }

    public void setTransmitterGroupNames(List<String> transmitterGroupNames) {
        this.transmitterGroupNames = transmitterGroupNames;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    //Getter returning references instead of String
    private static State state;

    public static void setState(State statePar) {
        state = statePar;
    }

    @NotNull(message = "müssen existieren")
    @Size(min = 1, message = "müssen existieren")
    public ArrayList<TransmitterGroup> getTransmitterGroups() throws Exception {
        if (state == null)
            throw new Exception("StateNotSetException");
        ArrayList<TransmitterGroup> transmitterGroups = new ArrayList<>();
        if (transmitterGroupNames == null)
            return null;
        for (String transmitterGroup : transmitterGroupNames) {
            if (state.getTransmitterGroups().contains(transmitterGroup))
                transmitterGroups.add(state.getTransmitterGroups().findByName(transmitterGroup));
        }
        if (transmitterGroups.size() == transmitterGroups.size())
            return transmitterGroups;
        else
            return null;
    }

    @Override
    public String toString() {
        return "Activation{" +
                "pagerNumber='" + number + '\'' +
                '}';
    }
}
