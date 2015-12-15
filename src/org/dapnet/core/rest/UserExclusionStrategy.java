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

package org.dapnet.core.rest;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import org.dapnet.core.model.CallSign;
import org.dapnet.core.model.Node;
import org.dapnet.core.model.Transmitter;
import org.dapnet.core.model.User;

public class UserExclusionStrategy implements ExclusionStrategy {

    public boolean shouldSkipClass(Class<?> arg0) {
        return false;
    }

    public boolean shouldSkipField(FieldAttributes f) {
        //All Fields to hide in JSON if User is no Admin or Owner
        return (f.getDeclaringClass() == CallSign.class && f.getName().equals("pagers"))
                || (f.getDeclaringClass() == Node.class && f.getName().equals("key"))
                || (f.getDeclaringClass() == Node.class && f.getName().equals("address"))
                || (f.getDeclaringClass() == Transmitter.class && f.getName().equals("address"))
                || (f.getDeclaringClass() == User.class && f.getName().equals("hash"));
    }

}