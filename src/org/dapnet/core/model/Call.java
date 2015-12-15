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

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Call implements Serializable {
    //No ID

    @NotNull
    @Size(min = 1, max = 80, message = "muss zwischen {min} und {max} Zeichen lang sein")
    private String text;

    @NotNull(message = "nicht vorhanden")
    @Size(min = 1, message = "müssen mindestens einen callSignNamen enthalten")
    private List<String> callSignNames;

    @NotNull(message = "nicht vorhanden")
    @Size(min = 1, message = "müssen mindestens einen transmitterGroupNamen enthalten")
    private List<String> transmitterGroupNames;

    //No Validation necessary
    private boolean emergency;

    //Internally set
    @NotNull(message = "nicht vorhanden")
    private Date timestamp;

    //Internally set
    @NotNull(message = "nicht vorhanden")
    private String ownerName;


    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getText() {
        return text;
    }

    public List<String> getCallSignNames() {
        return callSignNames;
    }

    public List<String> getTransmitterGroupNames() {
        return transmitterGroupNames;
    }

    public boolean isEmergency() {
        return emergency;
    }

    //Getter returning references instead of String
    private static State state;

    public static void setState(State statePar) {
        state = statePar;
    }

    @NotNull(message = "müssen existieren")
    @Size(min = 1, message = "müssen existieren")
    public ArrayList<CallSign> getCallSigns() throws Exception {
        if (state == null)
            throw new Exception("StateNotSetException");
        ArrayList<CallSign> callSigns = new ArrayList<>();
        if (callSignNames == null)
            return null;
        for (String callSign : callSignNames) {
            if (state.getCallSigns().contains(callSign))
                callSigns.add(state.getCallSigns().findByName(callSign));
        }
        if (callSigns.size() == callSignNames.size())
            return callSigns;
        else
            return null;
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

    @NotNull(message = "muss existieren")
    public User getOwner() throws Exception {
        if (state == null)
            throw new Exception("StateNotSetException");
        if (ownerName == null)
            return null;
        else
            return state.getUsers().findByName(ownerName);
    }

    @Override
    public String toString() {
        return "Call{" +
                "ownerName='" + ownerName + '\'' +
                '}';
    }
}
