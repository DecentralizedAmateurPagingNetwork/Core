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

	@NotNull
	protected Status status;

	public enum Status {
		ONLINE, OFFLINE, ERROR, DISABLED
	}

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

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	private static State state;

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