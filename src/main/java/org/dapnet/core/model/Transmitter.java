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

package org.dapnet.core.model;

import org.dapnet.core.model.list.Searchable;
import org.dapnet.core.model.validator.TimeSlot;
import org.dapnet.core.model.validator.ValidName;
import org.dapnet.core.rest.RestAuthorizable;
import org.jgroups.stack.IpAddress;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;

public class Transmitter implements Serializable, RestAuthorizable, Searchable {

	private static final long serialVersionUID = -8142160974834002456L;
	private static State state;

	@NotNull
	@Size(min = 3, max = 20)
	protected String name;

	@NotNull
	protected String authKey;

	@NotNull
	@Digits(integer = 3, fraction = 8)
	@Min(-180)
	@Max(+180)
	protected String longitude;

	@NotNull
	@Digits(integer = 3, fraction = 8)
	@Min(-90)
	@Max(+90)
	protected String latitude;

	@NotNull
	@Digits(integer = 3, fraction = 3)
	@Min(0)
	@Max(200)
	protected String power;

	@NotNull
	protected String nodeName;

	protected IpAddress address;

	@NotNull
	@TimeSlot()
	protected String timeSlot;

	@NotNull
	@Size(min = 1, message = "must contain at least one ownerName")
	protected ArrayList<String> ownerNames;

	protected String deviceType;

	protected String deviceVersion;

	public enum Status {
		ONLINE, OFFLINE, ERROR, DISABLED
	}

	@NotNull
	protected Status status;

	@Min(0)
	@Max(1000)
	protected int antennaAboveGroundLevel;

	public enum AntennaType {
		OMNI, DIRECTIONAL
	}

	@NotNull
	protected AntennaType antennaType;

	@Min(0)
	@Max(359)
	protected int antennaDirection;

	@Min(-50)
	@Max(80)
	protected float antennaGainDbi;

	public enum Usage {
		WIDERANGE, PERSONAL
	}

	@NotNull
	protected Usage usage;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAuthKey() {
		return authKey;
	}

	public void setAuthKey(String authKey) {
		this.authKey = authKey;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getPower() {
		return power;
	}

	public void setPower(String power) {
		this.power = power;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public IpAddress getAddress() {
		return address;
	}

	public void setAddress(IpAddress address) {
		this.address = address;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getDeviceVersion() {
		return deviceVersion;
	}

	public void setDeviceVersion(String deviceVersion) {
		this.deviceVersion = deviceVersion;
	}

	public String getTimeSlot() {
		return timeSlot;
	}

	public void setTimeSlot(String timeSlot) {
		this.timeSlot = timeSlot;
	}

	public ArrayList<String> getOwnerNames() {
		return ownerNames;
	}

	public void setOwnerNames(ArrayList<String> owners) {
		this.ownerNames = owners;
	}

	/**
	 * Gets the antenna height above ground level in meters.
	 * 
	 * @return Antenna height above ground level in meters.
	 */
	public int getAntennaAboveGroundLevel() {
		return antennaAboveGroundLevel;
	}

	/**
	 * Sets the antenna height above ground level in meters.
	 * 
	 * @param antennaAboveGroundLevel
	 *            Antenna height above ground level in meters.
	 */
	public void setAntennaAboveGroundLevel(int antennaAboveGroundLevel) {
		this.antennaAboveGroundLevel = antennaAboveGroundLevel;
	}

	/**
	 * Gets the antenna type.
	 * 
	 * @return Antenna type.
	 */
	public AntennaType getAntennaType() {
		return antennaType;
	}

	/**
	 * Sets the antenna type.
	 * 
	 * @param antennaType
	 *            Antenna type.
	 */
	public void setAntennaType(AntennaType antennaType) {
		this.antennaType = antennaType;
	}

	/**
	 * Gets the antenna main direction (only for directional antennas).
	 * 
	 * @return Antenna main direction.
	 */
	public int getAntennaDirection() {
		return antennaDirection;
	}

	/**
	 * Sets the antenna main direction (only for directional antennas).
	 * 
	 * @param antennaDirection
	 *            Antenna main direction.
	 */
	public void setAntennaDirection(int antennaDirection) {
		this.antennaDirection = antennaDirection;
	}

	/**
	 * Gets the antenna gain minus cable losses in dBi.
	 * 
	 * @return Antenna gain minus cable losses in dBi.
	 */
	public float getAntennaGainDbi() {
		return antennaGainDbi;
	}

	/**
	 * Sets the antenna gain minus cable losses in dBi.
	 * 
	 * @param antennaGainDbi
	 *            Antenna gain minus cable losses in dBi.
	 */
	public void setAntennaGainDbi(float antennaGainDbi) {
		this.antennaGainDbi = antennaGainDbi;
	}

	/**
	 * Gets the transmitter usage.
	 * 
	 * @return Transmitter usage.
	 */
	public Usage getUsage() {
		return usage;
	}

	/**
	 * Sets the transmitter usage.
	 * 
	 * @param usage
	 *            Transmitter usage.
	 */
	public void setUsage(Usage usage) {
		this.usage = usage;
	}

	/**
	 * Gets the transmitter status.
	 * 
	 * @return Transmitter status.
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Sets the transmitter status.
	 * 
	 * @param status
	 *            Transmitter status.
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

	public static void setState(State statePar) {
		state = statePar;
	}

	@ValidName(message = "must contain names of existing users", fieldName = "ownerNames", constraintName = "ValidOwnerNames")
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
		if (ownerNames.size() == users.size())
			return users;
		else
			return null;
	}

	@ValidName(message = "must contain the name of an existing node", fieldName = "nodeName", constraintName = "ValidNodeName")
	public Node getNode() throws Exception {
		if (state != null) {
			return state.getNodes().findByName(nodeName);
		} else {
			throw new Exception("StateNotSetException");
		}
	}

	@Override
	public String toString() {
		return String.format("Transmitter{name=\'%s', status=%s}", name, status);
		// return "Transmitter{" + "name='" + name + '\'' + ", status=" + status
		// + '}';
	}

}