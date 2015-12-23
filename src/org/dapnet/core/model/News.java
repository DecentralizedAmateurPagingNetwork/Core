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

import org.dapnet.core.model.validator.ValidName;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

public class News implements Serializable {
    //No ID
    @NotNull
    @Size(min = 1, max = 80)
    private String text;

    @NotNull
    private String rubricName;

    @NotNull
    @Min(value = 1)
    @Max(value = 10)
    private int number;

    //Internally set
    @NotNull
    private Date timestamp;

    //Internally set
    @NotNull
    @Size(min = 1, message = "must contain at least one ownerName")
    private String ownerName;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getRubricName() {
        return rubricName;
    }

    public void setRubricName(String rubricName) {
        this.rubricName = rubricName;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

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

    public static State getState() {
        return state;
    }

    //Getter returning references instead of String
    private static State state;

    public static void setState(State statePar) {
        state = statePar;
    }

    @ValidName(message = "must contain the name of an existing rubric",
            fieldName = "rubricName", constraintName = "ValidRubricName")
    public Rubric getRubric() throws Exception {
        if (state == null)
            throw new Exception("StateNotSetException");
        if (rubricName == null)
            return null;
        else
            return state.getRubrics().findByName(rubricName);
    }

    @ValidName(message = "must contain the name of an existing user",
            fieldName = "ownerNames", constraintName = "ValidOwnerNames")
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
        return "News{" +
                "rubricName='" + rubricName + '\'' +
                ", number=" + number +
                '}';
    }
}
