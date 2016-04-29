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

package org.dapnet.core.model;

import org.dapnet.core.model.list.Searchable;
import org.dapnet.core.model.validator.DescriptionPayload;
import org.dapnet.core.model.validator.ValidName;
import org.dapnet.core.rest.RestAuthorizable;
import org.hibernate.validator.constraints.ScriptAssert;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;

public class CallSign implements Serializable, RestAuthorizable, Searchable {
    //ID
    @NotNull
    @Size(min = 3, max = 20)
    private String name;

    @NotNull
    @Size(min = 0, max = 60)
    private String description;

    @NotNull
    @Valid
    @Size(min = 1, message = "must contain at least one pager")
    private ArrayList<Pager> pagers;

    @NotNull
    @Size(min = 1, message = "must contain at least one ownerName")
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

    @ValidName(message = "must contain names of existing users",
            fieldName = "ownerNames", constraintName = "ValidOwnerNames")
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
        if(users.size()!=ownerNames.size())
            return null;
        return users;
    }

    @Override
    public String toString() {
        return "CallSign{" +
                "name='" + name + '\'' +
                '}';
    }
}
