package org.dapnet.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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

public final class StateManager {

	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Gson gson;
	private final Validator validator;
	private DefaultRepository repository = new DefaultRepository();

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

	public Repository getRepository() {
		return repository;
	}

	public CoreStatistics getStatistics() {
		return repository.getStatistics();
	}

	public void loadStateFromFile(String fileName) throws IOException {
		// We can load the state without locking first
		State newState = null;

		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(fileName), "UTF-8")) {
			newState = gson.fromJson(reader, State.class);
		}

		lock.writeLock().lock();

		try {
			repository.setState(newState);
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void writeStateToFile(String fileName) throws IOException {
		lock.readLock().lock();

		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8")) {
			writer.write(gson.toJson(this));
			writer.flush();
		} finally {
			lock.readLock().unlock();
		}
	}

	public void loadStateFromStream(InputStream istream) throws Exception {
		State newState = (State) Util.objectFromStream(new DataInputStream(istream));

		lock.writeLock().lock();

		try {
			repository.setState(newState);
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void writeStateToStream(OutputStream ostream) throws Exception {
		lock.readLock().lock();

		try {
			Util.objectToStream(repository.getState(), new DataOutputStream(ostream));
		} finally {
			lock.readLock().unlock();
		}
	}

	public Set<ConstraintViolation<Object>> validateState() {
		lock.readLock().lock();
		try {
			return validator.validate(repository.getState());
		} finally {
			lock.readLock().unlock();
		}
	}

	public <T> Set<ConstraintViolation<T>> validate(T object) {
		return validator.validate(object);
	}

}
