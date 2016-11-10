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

package org.dapnet.core.transmission;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.Settings;
import org.dapnet.core.model.Transmitter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;

public abstract class Raspager extends TransmitterDevice {
    private static final Logger logger = LogManager.getLogger(Raspager.class.getName());
    private static final TransmissionSettings.RaspagerSettings settings =
            Settings.getTransmissionSettings().getRaspagerSettings();


    private int messageCount = 0;

    public Raspager(Transmitter transmitter, TransmitterDeviceListener listener, DeviceType deviceType) {
        super(transmitter, listener);
        this.transmitterDeviceProtocol = new RaspagerProtocol(deviceType);
    }

    private void cleanup() {
        this.disconnect();
        this.closeDeviceIO();
        this.thread = null;
    }

    public void run() {
        while (true) {
            try {
                //Connect to Raspager
                connect();
                logger.info("Successfully connected to " + this);

                //Handle Welcome
                transmitterDeviceProtocol.handleWelcome(this, toServer, fromServer);
                logger.info("Successfully welcome with " + this);
                transmitterDeviceListener.handleTransmitterDeviceStarted(this);

                //Handle Messages until Interrupt or Error
                handleMessages();
            } catch (InterruptedException e) {
                //Device is called to stop, so break loop and stop
                break;
            } catch (IOException e) {
                //Exception while communication with device, so inform listener and try to reconnect
                throwTransmitterDeviceOffline(new TransmitterDeviceException("Connection error: " + e.getMessage()));
            } catch (TransmitterDeviceConnectionFailedException e) {
                //Device failed finally to connect, so break loop and stop
                throwTransmitterDeviceException(new TransmitterDeviceException("Connection error: " + e.getMessage()));
                break;
            } catch (TransmitterDeviceException e) {
                //Unknown TransmitterDeviceException, so inform listener and try to reconnect
                throwTransmitterDeviceOffline(new TransmitterDeviceException("Connection error: " + e.getMessage()));
            }
        }
        cleanup();
        logger.info("Successfully stopped " + this);
        transmitterDeviceListener.handleTransmitterDeviceStopped(this);

    }


    protected void connect() throws TransmitterDeviceException, InterruptedException {
        Socket socket;
        int numberOfReconnects = 0;
        while (!thread.isInterrupted()) {
            try {
                socket = new Socket();
                socket.connect(
                        new InetSocketAddress(address.getIpAddress().getHostAddress(),
                                address.getPort()), settings.getConnectionTimeout());
                this.deviceSocket = socket;
                setupDeviceIO();
                return;
            } catch (IOException e) {
                numberOfReconnects++;
                logger.warn(this + " could not create connection: " + e.getMessage());
            }

            if (settings.getMaxNumberOfReconnects() != -1 && numberOfReconnects > settings.getMaxNumberOfReconnects())
                throw new TransmitterDeviceConnectionFailedException("Connection could not been established");

            if (!thread.isInterrupted()) {
                if (settings.getReconnectWaitTime() != -1)
                    thread.sleep(settings.getReconnectWaitTime()); //Wait fixed time between reconnects
                else
                    thread.sleep((long) (7500 * Math.pow(2, numberOfReconnects)));  // Exponential wait time
            }
        }
        throw new InterruptedException();
    }

    protected void handleMessages() throws TransmitterDeviceException, InterruptedException, IOException {
        while (!thread.isInterrupted()) {
            //Blocking until Message is available
            Message message = this.getMessage();
            //Wait until next TX Slot
            //thread.sleep(getTimeToNextOpenSlot());

            startMessageCount();
            while (true) {
                this.transmitterDeviceProtocol.handleMessage(message, toServer, fromServer);
                logger.info("Successfully sent message \"" + message.getText() + "\" to " +
                        message.getAddress() + " with " + this);
                increaseMessageCount(message);
                if (messageQueue.isEmpty() || isMessageCountOverflowed(messageQueue.peek()))
                    break;
                else if (thread.isInterrupted())
                    throw new InterruptedException();
                else
                    message = this.getMessage();
            }
        }
        throw new InterruptedException();
    }

    protected void startMessageCount() {
        messageCount = 0;
    }

    protected void increaseMessageCount(Message message) {
        messageCount++; //No consideration of the message length
        //messageCount = messageCount + (message.getText().length() > 40 ? 2 : 1);
    }

    protected boolean isMessageCountOverflowed(Message message) {
        return (messageCount + 1) > settings.getMaxMessageCount();  //No consideration of the message length
        //return (messageCount + (message.getText().length() > 40 ? 2 : 1) > settings.getMaxMessageCount());
    }

    public String toString() {
        return name;
    }

    protected int getTimeToNextOpenSlot() {
        return getTimeToSlot(getNextOpenSlot());
    }

    private int getCurrentSlot() {
/*      long timeMilli = new Date().getTime() + settings.getTransmissionDelay();
        long phaseTime = timeMilli % 102400;
        int slot = (int) (phaseTime / 6400);
*/

        long timemillis = System.currentTimeMillis();
        long seconds = timemillis / 1000;
        long deltaTimemillis = timemillis - seconds * 1000; // additional milliseconds
        long time_tx = (seconds * 10 + deltaTimemillis / 100) & 0xffff;


        int slot = ((int) (time_tx / 37.5)) % 16;
        return slot;
    }

    private int getTimeToSlot(int slot) {
        long timeMilli = new Date().getTime();
        int phaseTime = (int) (timeMilli % 102400);
        int slotTime = slot * 6400;
        int timeDiff = slotTime - phaseTime;
        if (timeDiff < 0)
            timeDiff += 102400;
        return timeDiff - settings.getTransmissionDelay();
    }

    private int getNextOpenSlot() {
        int currentSlot = getCurrentSlot();
        for (int i = 0; i < timeSlot.length(); i++) {
            int slot = Integer.parseInt(timeSlot.substring(i, i + 1), 16);
            if (slot > currentSlot)
                return slot;
        }
        return Integer.parseInt(timeSlot.substring(0, 1), 16);
    }
}
