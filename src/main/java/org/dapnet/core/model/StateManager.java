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
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.dapnet.core.rest.GsonTypeAdapterFactory;
import org.jgroups.util.Util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The state manager is responsible for managing the DAPNET Core state and
 * provides load/save state functionality as well as validation.
 * 
 * @author Philipp Thiel
 */
public final class StateManager implements CoreRepository {

	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Gson gson;
	private final ValidatorFactory validatorFactory;
	private State state;
	private ModelRepository<CallSign> callsigns;
	private ModelRepository<Node> nodes;
	private ModelRepository<User> users;
	private ModelRepository<Transmitter> transmitters;
	private ModelRepository<TransmitterGroup> transmitterGroups;
	private ModelRepository<Rubric> rubrics;
	private ModelRepository<NewsList> news;

	/**
	 * Constructs a new state manager instance.
	 */
	public StateManager() {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		builder.registerTypeAdapterFactory(new GsonTypeAdapterFactory());
		gson = builder.create();

		validatorFactory = Validation.buildDefaultValidatorFactory();

		state = new State();
		setModelRepositories();
	}

	public ReadWriteLock getLock() {
		return lock;
	}

	@Override
	public Collection<Call> getCalls() {
		return state.getCalls();
	}

	@Override
	public ModelRepository<CallSign> getCallSigns() {
		return callsigns;
	}

	@Override
	public ModelRepository<Node> getNodes() {
		return nodes;
	}

	@Override
	public ModelRepository<User> getUsers() {
		return users;
	}

	@Override
	public ModelRepository<Transmitter> getTransmitters() {
		return transmitters;
	}

	@Override
	public ModelRepository<TransmitterGroup> getTransmitterGroups() {
		return transmitterGroups;
	}

	@Override
	public ModelRepository<Rubric> getRubrics() {
		return rubrics;
	}

	@Override
	public ModelRepository<NewsList> getNews() {
		return news;
	}

	@Override
	public CoreStatistics getStatistics() {
		return state.getStatistics();
	}

	/**
	 * Loads the state from the given state file.
	 * 
	 * @param fileName State file name
	 * @param force    Use the new state even if constraint violations are found
	 * @return Constraint violations
	 * @throws IOException on IO errors
	 */
	public Set<ConstraintViolation<Object>> loadStateFromFile(String fileName, boolean force) throws IOException {
		State newState = null;

		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(fileName), "UTF-8")) {
			newState = gson.fromJson(reader, State.class);
		}

		Set<ConstraintViolation<Object>> violations = validate(newState);
		if (violations.isEmpty() || force) {
			lock.writeLock().lock();

			try {
				state = newState;
				setModelRepositories();
			} finally {
				lock.writeLock().unlock();
			}
		}

		return violations;
	}

	/**
	 * Writes the state to the state file.
	 * 
	 * @param fileName State file name
	 * @throws IOException on IO errors
	 */
	public void writeStateToFile(String fileName) throws IOException {
		File stateFile = new File(fileName);
		if (stateFile.getParentFile() != null) {
			stateFile.getParentFile().mkdirs();
		}

		lock.readLock().lock();

		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(stateFile), "UTF-8")) {
			String json = gson.toJson(state);
			writer.write(json);
			writer.flush();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Loads the state from the given input stream.
	 * 
	 * @param istream Input stream
	 * @param force   Use the new state even if constraint violations are found
	 * @return Constraint violations
	 * @throws Exception on errors
	 */
	public Set<ConstraintViolation<Object>> loadStateFromStream(InputStream istream, boolean force) throws Exception {
		final State newState = (State) Util.objectFromStream(new DataInputStream(istream));

		Set<ConstraintViolation<Object>> violations = validate(newState);
		if (violations.isEmpty() || force) {
			lock.writeLock().lock();

			try {
				state = newState;
				setModelRepositories();
			} finally {
				lock.writeLock().unlock();
			}
		}

		return violations;
	}

	/**
	 * Writes the state to the given output stream.
	 * 
	 * @param ostream Output stream
	 * @throws Exception on errors
	 */
	public void writeStateToStream(OutputStream ostream) throws Exception {
		lock.readLock().lock();

		try {
			Util.objectToStream(state, new DataOutputStream(ostream));
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Validates an object using the current validator instance.
	 * 
	 * @param <T>    Object type
	 * @param object Object to validate
	 * @return Constraint violations
	 */
	public <T> Set<ConstraintViolation<T>> validate(T object) {
		return validatorFactory.getValidator().validate(object);
	}

	/**
	 * Sets the model repositories for the current state.
	 */
	private void setModelRepositories() {
		callsigns = new MapModelRepository<>(state.getCallSigns());
		nodes = new MapModelRepository<>(state.getNodes());
		users = new MapModelRepository<>(state.getUsers());
		transmitters = new MapModelRepository<>(state.getTransmitters());
		transmitterGroups = new MapModelRepository<>(state.getTransmitterGroups());
		rubrics = new MapModelRepository<>(state.getRubrics());
		news = new MapModelRepository<>(state.getNews());
	}

}
