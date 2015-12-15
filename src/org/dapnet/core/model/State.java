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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.dapnet.core.Settings;
import org.dapnet.core.model.list.SearchableArrayList;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.jgroups.util.Util.readFile;

public class State implements Serializable {
    @NotNull(message = "nicht vorhanden")
    @Valid
    private SearchableArrayList<CallSign> callSigns;

    @NotNull(message = "nicht vorhanden")
    @Valid
    private SearchableArrayList<Node> nodes;

    @NotNull(message = "nicht vorhanden")
    @Valid
    private SearchableArrayList<User> users;

    @NotNull(message = "nicht vorhanden")
    @Valid
    private List<Call> calls;

    @NotNull(message = "nicht vorhanden")
    @Valid
    private List<News> news;

    @NotNull(message = "nicht vorhanden")
    @Valid
    private SearchableArrayList<Transmitter> transmitters;

    @NotNull(message = "nicht vorhanden")
    @Valid
    private SearchableArrayList<TransmitterGroup> transmitterGroups;

    @NotNull(message = "nicht vorhanden")
    @Valid
    private SearchableArrayList<Rubric> rubrics;

    public State() {
        callSigns = new SearchableArrayList<>();
        nodes = new SearchableArrayList<>();
        users = new SearchableArrayList<>();
        calls = new ArrayList<>();
        news = new ArrayList<>();
        transmitters = new SearchableArrayList<>();
        transmitterGroups = new SearchableArrayList<>();
        rubrics = new SearchableArrayList<>();

        setModelReferences();
    }

    public void setModelReferences() {
        //Setting reference to state in model for allow returning of reference instead of strings
        Call.setState(this);
        CallSign.setState(this);
        News.setState(this);
        Rubric.setState(this);
        Transmitter.setState(this);
        TransmitterGroup.setState(this);
    }

    public static State readFromFile() throws Exception {
        return new Gson().fromJson(readFile(Settings.getModelSettings().getStateFile()), State.class);
    }

    public void writeToFile() {
        File file = new File(Settings.getModelSettings().getStateFile());
        try {
            FileWriter writer = new FileWriter(file);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String state = gson.toJson(this);
            writer.write(state);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Call> getCalls() {
        return calls;
    }

    public List<News> getNews() {
        return news;
    }


    public SearchableArrayList<CallSign> getCallSigns() {
        return callSigns;
    }

    public SearchableArrayList<Node> getNodes() {
        return nodes;
    }

    public SearchableArrayList<User> getUsers() {
        return users;
    }

    public SearchableArrayList<Transmitter> getTransmitters() {
        return transmitters;
    }

    public SearchableArrayList<TransmitterGroup> getTransmitterGroups() {
        return transmitterGroups;
    }

    public SearchableArrayList<Rubric> getRubrics() {
        return rubrics;
    }
}
