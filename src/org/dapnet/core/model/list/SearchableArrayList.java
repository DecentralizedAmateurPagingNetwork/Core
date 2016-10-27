/*
 * DAPNET CORE PROJECT
 * Copyright (C) 2016
 *
 * Daniel Sialkowski
 *
 * daniel.sialkowski@rwth-aachen.de
 *
 * Institute of High Frequency Technology
 * RWTH AACHEN UNIVERSITY
 * Melatener Str. 25
 * 52074 Aachen
 */

package org.dapnet.core.model.list;

import java.util.ArrayList;

public class SearchableArrayList<T extends Searchable> extends ArrayList<T> {
    public T findByName(String name) {
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).getName().equals(name))
                return (T) this.get(i);
        }
        return null;
    }

    public boolean removeByName(String name) {
        T toRemove = findByName(name);
        if (toRemove != null) {
            this.remove(findByName(name));
            return true;
        } else {
            return false;
        }
    }

    public boolean contains(String name) {
        return findByName(name) != null;
    }
}
