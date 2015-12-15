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
import org.dapnet.core.model.validator.EMail;
import org.dapnet.core.rest.RestAuthorizable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable, Searchable, RestAuthorizable {
    //ID
    @NotNull(message = "nicht vorhanden")
    @Size(min = 3, max = 20, message = "muss zwischen {min} und {max} Zeichen lang sein")
    private String name;

    @NotNull(message = "nicht vorhanden")
    @Size(min = 102, max = 102, message = "ist ungültig")
    private String hash;

    @NotNull(message = "nicht vorhanden")
    @EMail
    private String mail;

    private boolean admin;

    // Used in case of creating new cluster
    public User(String name, String hash, String mail, boolean admin) {
        this.name = name;
        this.hash = hash;
        this.mail = mail;
        this.admin = admin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public ArrayList<String> getOwnerNames() {
        ArrayList<String> list = new ArrayList();
        list.add(name);
        return list;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                '}';
    }
}
