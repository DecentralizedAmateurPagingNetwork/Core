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

package org.dapnet.core.transmission;

import java.util.Date;

public class Message implements Comparable<Message>{
    private String text;
    private int address;
    private Date timestamp;
    private MessagePriority priority;
    private MessageType type;

    public enum MessagePriority {
        EMERGENCY,TIME,CALL,NEWS,ACTIVATION,RUBRIC
    }

    public enum MessageType {
        NUMERIC		(5),
        ALPHANUM	(6);

        private int value;
        private MessageType(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }

    public Message(String text, int address, MessagePriority priority,  MessageType type) {
        this.text = text;
        this.address = address;
        this.timestamp = new Date();
        this.priority = priority;
        this.type =  type;
    }

    public String getText() {
        return text;
    }

    public int getAddress() {
        return address;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public MessagePriority getPriority() {
        return priority;
    }

    public MessageType getType() {
        return type;
    }


    @Override
    public int compareTo(Message message) {
        if(priority.ordinal()<message.getPriority().ordinal())
            return -1;
        if(priority.ordinal()>message.getPriority().ordinal())
            return 1;

        //Same Priority, check Timestamp
        if(timestamp.before(message.getTimestamp()))
            return -1;
        if(timestamp.after(message.getTimestamp()))
            return 1;

        //Also same Timestamp:
        return 0;
    }
}
