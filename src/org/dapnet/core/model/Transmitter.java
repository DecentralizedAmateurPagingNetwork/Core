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

import org.dapnet.core.model.list.Searchable;
import org.dapnet.core.model.validator.TimeSlot;
import org.dapnet.core.rest.RestAuthorizable;
import org.jgroups.stack.IpAddress;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;

public class Transmitter implements Serializable, RestAuthorizable, Searchable {
    //ID
    @NotNull(message = "nicht vorhanden")
    @Size(min = 3, max = 20, message = "muss zwischen {min} und {max} Zeichen lang sein")
    protected String name;

    @NotNull(message = "nicht vorhanden")
    @Digits(integer=3, fraction=8)
    @Min(-180)
    @Max(+180)
    protected String longitude;

    @NotNull(message = "nicht vorhanden")
    @Digits(integer=3, fraction=8)
    @Min(-90)
    @Max(+90)
    protected String latitude;

    @NotNull(message = "nicht vorhanden")
    @Digits(integer=3, fraction=3)
    @Min(0)
    @Max(200)
    protected String power;

    @NotNull(message = "nicht vorhanden")
    protected String nodeName;

    @NotNull(message = "nicht vorhanden")
    protected IpAddress address;

    @NotNull(message = "nicht vorhanden")
    @TimeSlot()
    protected String timeSlot;

    @NotNull(message = "nicht vorhanden")
    @Size(min = 1, message = "müssen mindestens einen ownerNamen enthalten")
    protected ArrayList<String> ownerNames;

    @NotNull(message = "nicht vorhanden")
    protected DeviceType deviceType;

    public enum DeviceType {
        RASPPAGER1, ERICSSON
    }

    //Internally set
    @NotNull(message = "nicht vorhanden")
    protected Status status;

    public enum Status {
        ONLINE, OFFLINE, ERROR
    }

    public Transmitter() {
    }

    public Transmitter(String name, String longitude, String latitude,
                       String power, IpAddress address, DeviceType deviceType, String timeSlot, ArrayList<String> ownerNames) {
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.power = power;
        this.address = address;
        this.deviceType = deviceType;
        this.timeSlot = timeSlot;
        this.ownerNames = ownerNames;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public IpAddress getAddress() {
        return address;
    }

    public void setAddress(IpAddress address) {
        this.address = address;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public ArrayList<String> getOwnerNames() {
        return ownerNames;
    }

    public void setOwnerNames(ArrayList<String> owners) {
        this.ownerNames = owners;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    //Getter returning references instead of String
    private static State state;

    public static void setState(State statePar) {
        state = statePar;
    }

    @NotNull(message = "müssen existieren")
    @Size(min = 1, message = "müssen existieren")
    public ArrayList<User> getOwners() throws Exception {
        if (state == null)
            throw new Exception("StateNotSetException");
        ArrayList<User> users = new ArrayList<>();
        if (ownerNames == null)
            return null;
        for (String owner : ownerNames) {
            if (state.getUsers().contains(owner))
                users.add(state.getUsers().findByName(owner));
        }
        if (ownerNames.size() == users.size())
            return users;
        else
            return null;
    }

    @NotNull(message = "muss existieren")
    public Node getNode() throws Exception {
        if (state == null)
            throw new Exception("StateNotSetException");
        return state.getNodes().findByName(nodeName);
    }

    @Override
    public String toString() {
        return "Transmitter{" +
                "name='" + name + '\'' +
                ", status=" + status +
                '}';
    }
}