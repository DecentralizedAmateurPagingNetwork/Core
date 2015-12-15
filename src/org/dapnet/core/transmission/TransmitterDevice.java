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


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.model.Transmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;


public abstract class TransmitterDevice extends Transmitter implements Runnable{
    private static final Logger logger = LogManager.getLogger(TransmitterDevice.class.getName());

    protected TransmitterDevice(Transmitter transmitter,
                                TransmitterDeviceListener transmitterDeviceListener)
    {
        this.name = transmitter.getName();
        this.longitude = transmitter.getLongitude();
        this.latitude = transmitter.getLatitude();
        this.power = transmitter.getPower();
        this.address = transmitter.getAddress();
        this.timeSlot = transmitter.getTimeSlot();
        this.ownerNames = transmitter.getOwnerNames();
        this.deviceType = transmitter.getDeviceType();
        this.status = transmitter.getStatus();

        this.transmitterDeviceListener = transmitterDeviceListener;

        this.messageQueue = new PriorityBlockingQueue<>();
    }

    //Handle Thread
    protected boolean running;
    protected volatile Thread thread = null;
    protected TransmitterDeviceListener transmitterDeviceListener;

    public void start() {
        if (thread == null) {
            running = true;
            thread = new Thread(this);
            thread.start();
        } else {
            logger.warn(this + " started although already running");
        }
    }

    public void stop() {
        if (thread != null) {
            running = false;
            thread.interrupt();
        } else {
            logger.warn(this + " stopped although not running");
        }
    }

    public abstract void run();

    protected void throwTransmitterDeviceException(TransmitterDeviceException e)
    {
        if(transmitterDeviceListener !=null) {
            logger.warn(this + " throws Exception: " + e.getMessage());
            transmitterDeviceListener.handleTransmitterDeviceError(this, e);
        }
    }

    //Handle Socket
    protected Socket deviceSocket = null;
    protected BufferedReader fromServer = null;
    protected PrintWriter toServer = null;

    protected void disconnect() {
        if (this.deviceSocket != null) { // clean up aborted connection
            try {
                this.deviceSocket.close();
            } catch (IOException e1) {
                logger.warn(this + " could not close socket");
            }
            this.deviceSocket = null;
        }
    }

    protected void setupDeviceIO() throws IOException{
            this.toServer = new PrintWriter(this.deviceSocket.getOutputStream(), true);
            this.fromServer = new BufferedReader(new InputStreamReader(this.deviceSocket.getInputStream()));
    }

    protected void closeDeviceIO() {
        if (this.fromServer != null) {
            try {
                this.fromServer.close();
            } catch (IOException e) {
                logger.warn(this + " could not close BufferedReader");
            }
            this.fromServer = null;
        }

        if (this.toServer != null) {
            this.toServer.close();
            this.fromServer = null;
        }
    }

    // Handle Messages
    protected PriorityBlockingQueue<Message> messageQueue;
    protected TransmitterDeviceProtocol transmitterDeviceProtocol;

    public Message getMessage() throws InterruptedException {
        return messageQueue.take();
    }

    public void sendMessage(Message m) {
        this.messageQueue.add(m);
    }

    public void sendMessages(List<Message> m) {
        messageQueue.addAll(m);
    }

    public boolean isInterrupted()
    {
        return thread.isInterrupted();
    }

}

