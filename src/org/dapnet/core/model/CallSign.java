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
import org.dapnet.core.rest.RestAuthorizable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;

public class CallSign implements Serializable, RestAuthorizable, Searchable {
    //ID
    @NotNull(message = "nicht vorhanden")
    @Size(min = 3, max = 20, message = "muss zwischen {min} und {max} Zeichen lang sein")
    private String name;

    @NotNull(message = "nicht vorhanden")
    @Size(min = 0, max = 60, message = "muss zwischen {min} und {max} Zeichen lang sein")
    private String description;

    @NotNull(message = "nicht vorhanden")
    @Valid
    private ArrayList<Pager> pagers;

    @NotNull(message = "nicht vorhanden")
    @Size(min = 1, message = "müssen mindestens einen ownerNamen enthalten")
    private ArrayList<String> ownerNames;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Pager> getPagers() {
        return pagers;
    }

    public void setPagers(ArrayList<Pager> pagers) {
        this.pagers = pagers;
    }

    public ArrayList<String> getOwnerNames() {
        return ownerNames;
    }

    public void setOwnerNames(ArrayList<String> owners) {
        this.ownerNames = owners;
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

    @Override
    public String toString() {
        return "CallSign{" +
                "name='" + name + '\'' +
                '}';
    }
}
