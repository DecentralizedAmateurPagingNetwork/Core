package org.dapnet.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.dapnet.core.rest.GsonTypeAdapterFactory;
import org.jgroups.util.Util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class StateManager implements Repository {

	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Gson gson;
	private final Validator validator;
	private State state;

	public StateManager() {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		builder.registerTypeAdapterFactory(new GsonTypeAdapterFactory());
		gson = builder.create();

		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	public ReadWriteLock getLock() {
		return lock;
	}

	@Override
	public Collection<Call> getCalls() {
		return state.getCalls();
	}

	@Override
	public Map<String, CallSign> getCallSigns() {
		return state.getCallSigns();
	}

	@Override
	public Map<String, Node> getNodes() {
		return state.getNodes();
	}

	@Override
	public Map<String, User> getUsers() {
		return state.getUsers();
	}

	@Override
	public Map<String, Transmitter> getTransmitters() {
		return state.getTransmitters();
	}

	@Override
	public Map<String, TransmitterGroup> getTransmitterGroups() {
		return state.getTransmitterGroups();
	}

	@Override
	public Map<String, Rubric> getRubrics() {
		return state.getRubrics();
	}

	@Override
	public Map<String, NewsList> getNews() {
		return state.getNews();
	}

	public CoreStatistics getStatistics() {
		return state.getStatistics();
	}

	public void loadStateFromFile(String fileName) throws IOException {
		// We can load the state without locking first
		State newState = null;

		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(fileName), "UTF-8")) {
			newState = gson.fromJson(reader, State.class);
		}

		lock.writeLock().lock();

		try {
			this.state = newState;
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void writeStateToFile(String fileName) throws IOException {
		File stateFile = new File(fileName);
		if (stateFile.getParentFile() != null) {
			stateFile.getParentFile().mkdirs();
		}

		lock.readLock().lock();

		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(stateFile), "UTF-8")) {
			writer.write(gson.toJson(state));
			writer.flush();
		} finally {
			lock.readLock().unlock();
		}
	}

	public void loadStateFromStream(InputStream istream) throws Exception {
		State newState = (State) Util.objectFromStream(new DataInputStream(istream));

		lock.writeLock().lock();

		try {
			this.state = newState;
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void writeStateToStream(OutputStream ostream) throws Exception {
		lock.readLock().lock();

		try {
			Util.objectToStream(state, new DataOutputStream(ostream));
		} finally {
			lock.readLock().unlock();
		}
	}

	public Set<ConstraintViolation<Object>> validateState() {
		lock.readLock().lock();
		try {
			return validator.validate(state);
		} finally {
			lock.readLock().unlock();
		}
	}

	public <T> Set<ConstraintViolation<T>> validate(T object) {
		return validator.validate(object);
	}

}
