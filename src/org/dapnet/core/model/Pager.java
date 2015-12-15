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

public class Pager implements Serializable {
    //No ID

    @NotNull(message = "nicht vorhanden")
    @Min(value = 0, message = "muss zwischen 0 und 2097151 liegen")
    @Max(value = 2097151, message = "muss zwischen 0 und 2097151 liegen")
    int number;

    @NotNull(message = "nicht vorhanden")
    @Size(min = 3, max = 20, message = "muss zwischen {min} und {max} Zeichen lang sein")
    String name;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Pager{" +
                "number=" + number +
                ", name='" + name + '\'' +
                '}';
    }
}
