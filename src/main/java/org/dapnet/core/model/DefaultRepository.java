package org.dapnet.core.model;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

class DefaultRepository implements Repository {

	private State state;

	public DefaultRepository() {
		this.state = new State();
	}

	public DefaultRepository(State state) {
		this.state = Objects.requireNonNull(state, "State must not be null.");
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

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = Objects.requireNonNull(state, "State must not be null.");
	}

}
