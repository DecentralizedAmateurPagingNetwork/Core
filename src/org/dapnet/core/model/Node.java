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

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

public class Node implements Serializable, Searchable {
    //ID
    @NotNull(message = "nicht vorhanden")
    @Size(min = 3, max = 20, message = "muss zwischen {min} und {max} Zeichen lang sein")
    private String name;

    @NotNull(message = "nicht vorhanden")
    private IpAddress address;

    @NotNull(message = "nicht vorhanden")
    private Status status;

    @NotNull(message = "nicht vorhanden")
    private String key;

    public enum Status {
        ONLINE, SUSPENDED, UNKNOWN
    }

    // Used in case of creating new cluster
    public Node(String name, IpAddress address, Status status, String key) {
        this.name = name;
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
