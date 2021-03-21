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

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.dapnet.core.model.validator.PagerAddress;
import org.dapnet.core.model.validator.RepositoryLookup;
import org.dapnet.core.model.validator.TimeSlot;
import org.dapnet.core.rest.RestAuthorizable;
import org.jgroups.stack.IpAddress;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class Transmitter implements Serializable, RestAuthorizable, NamedObject {

	private static final long serialVersionUID = 1L;

	@NotNull
	@Size(min = 3, max = 20)
	private String name;

	@NotNull
	@Size(min = 1, max = 64)
	private String authKey;

	@NotNull
	@Digits(integer = 3, fraction = 8)
	@Min(-180)
	@Max(+180)
	private String longitude;

	@NotNull
	@Digits(integer = 3, fraction = 8)
	@Min(-90)
	@Max(+90)
	private String latitude;

	@NotNull
	@Digits(integer = 3, fraction = 3)
	@Min(0)
	@Max(200)
	private String power;

	private String nodeName;

	private IpAddress address;

	@NotNull
	@TimeSlot
	private String timeSlot;

	@NotNull
	@Size(min = 1, message = "must contain at least one ownerName")
	private Set<@RepositoryLookup(User.class) String> ownerNames;

	private String deviceType;

	private String deviceVersion;

	private AtomicLong callCount = new AtomicLong();

	public enum Status {
		OFFLINE, ONLINE, ERROR, DISABLED
	}

	@NotNull
	private Status status;

	@Min(0)
	@Max(1000)
	private int antennaAboveGroundLevel;

	public enum AntennaType {
		OMNI, DIRECTIONAL
	}

	@NotNull
	private AntennaType antennaType;

	@Min(0)
	@Max(359)
	private int antennaDirection;

	@Min(-50)
	@Max(80)
	private float antennaGainDbi;

	private Instant lastUpdate;

	public enum Usage {
		WIDERANGE, PERSONAL
	}

	@NotNull
	private Usage usage;

	@PagerAddress(nullable = true, checkDuplicates = false)
	private int identificationAddress = 1;

	private Instant lastConnected;
	private Instant connectedSince;

	public Transmitter() {
	}

	public Transmitter(Transmitter other) {
		if (other == null) {
			throw new NullPointerException("Other object must not be null.");
		}

		name = other.name;
		authKey = other.authKey;
		longitude = other.longitude;
		latitude = other.latitude;
		power = other.power;
		nodeName = other.nodeName;
		address = other.address != null ? other.address.copy() : null;
		timeSlot = other.timeSlot;
		ownerNames = ModelUtils.copyStringSet(other.ownerNames);
		deviceType = other.deviceType;
		deviceVersion = other.deviceVersion;
		callCount = new AtomicLong(other.callCount.get());
		status = other.status;
		antennaAboveGroundLevel = other.antennaAboveGroundLevel;
		antennaType = other.antennaType;
		antennaDirection = other.antennaDirection;
		antennaGainDbi = other.antennaGainDbi;
		lastUpdate = other.lastUpdate;
		usage = other.usage;
		identificationAddress = other.identificationAddress;
		lastConnected = other.lastConnected;
		connectedSince = other.connectedSince;
	}

	@Override
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

	@Override
	public Set<String> getOwnerNames() {
		return ownerNames;
	}

	public void setOwnerNames(Set<String> owners) {
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
	 * @param antennaAboveGroundLevel Antenna height above ground level in meters.
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
	 * @param antennaType Antenna type.
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
	 * @param antennaDirection Antenna main direction.
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
	 * @param antennaGainDbi Antenna gain minus cable losses in dBi.
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
	 * @param usage Transmitter usage.
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
	 * @param status Transmitter status.
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * Gets the timepoint when the transmitter connected.
	 * 
	 * @return Timepoint
	 */
	public Instant getLastConnected() {
		return lastConnected;
	}

	/**
	 * Sets the timepoint when the transmitter connected.
	 * 
	 * @param when Timepoint
	 */
	public void setLastConnected(Instant when) {
		this.lastConnected = when;
	}

	/**
	 * Gets the timepoint since when the transmitter is connected.
	 * 
	 * @return Timepoint
	 */
	public Instant getConnectedSince() {
		return connectedSince;
	}

	/**
	 * Sets the timepoint since when the transmitter is connected.
	 * 
	 * @param since Timepoint
	 */
	public void setConnectedSince(Instant since) {
		this.connectedSince = since;
	}

	/**
	 * Gets the pager address used to send identification messages to.
	 * 
	 * @return Pager address to use for sending identification messages.
	 */
	public int getIdentificationAddress() {
		return identificationAddress;
	}

	/**
	 * Sets the pager address used to send identification messages to.
	 * 
	 * @param address Pager address to use for sending identification messages.
	 */
	public void setIdentificationAddress(int address) {
		this.identificationAddress = address;
	}

	/**
	 * Sets the last update timestamp.
	 * 
	 * @param when Last update timestamp
	 */
	public void setLastUpdate(Instant when) {
		this.lastUpdate = when;
	}

	/**
	 * Gets the last update timestamp.
	 * 
	 * @return Last update timestamp
	 */
	public Instant getLastUpdate() {
		return lastUpdate;
	}

	/**
	 * Gets the number of calls sent to this transmitter.
	 * 
	 * @return Call count
	 */
	public long getCallCount() {
		return callCount.get();
	}

	/**
	 * Sets the number of calls sent to this transmitter.
	 * 
	 * @param callCount Call count
	 */
	public void setCallCount(long callCount) {
		this.callCount.set(callCount);
	}

	/**
	 * Atomically updates the call counter.
	 * 
	 * @param delta Delta to add or subtract.
	 */
	public void updateCallCount(long delta) {
		callCount.addAndGet(delta);
	}

	@Override
	public String toString() {
		return String.format("Transmitter{name='%s', status=%s}", name, status);
	}

}