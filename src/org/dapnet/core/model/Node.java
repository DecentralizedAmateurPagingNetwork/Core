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
import org.jgroups.stack.IpAddress;

import javax.validation.constraints.*;
import java.io.Serializable;

public class Node implements Serializable, Searchable {
    //ID
    @NotNull
    @Size(min = 3, max = 20)
    private String name;

    @NotNull
    @Digits(integer=3, fraction=8)
    @Min(-180)
    @Max(+180)
    protected String longitude;

    @NotNull
    @Digits(integer=3, fraction=8)
    @Min(-90)
    @Max(+90)
    protected String latitude;

    @NotNull
    private IpAddress address;

    @NotNull
    private Status status;

    @NotNull
    private String key;

    public enum Status {
        ONLINE, SUSPENDED, UNKNOWN
    }

    // Used in case of creating new cluster
    public Node(String name, IpAddress address, String longitude, String latitude, Status status, String key) {
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.address = address;
        this.status = status;
        this.key = key;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
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

    public IpAddress getAddress() {
        return address;
    }

    public void setAddress(IpAddress address) {
        this.address = address;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "Node{" +
                "status=" + status +
                ", name='" + name + '\'' +
                '}';
    }
}
