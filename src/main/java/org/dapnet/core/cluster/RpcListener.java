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

package org.dapnet.core.cluster;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.Settings;
import org.dapnet.core.model.Activation;
import org.dapnet.core.model.Call;
import org.dapnet.core.model.CallSign;
import org.dapnet.core.model.CoreRepository;
import org.dapnet.core.model.ModelRepository;
import org.dapnet.core.model.News;
import org.dapnet.core.model.NewsList;
import org.dapnet.core.model.Node;
import org.dapnet.core.model.Rubric;
import org.dapnet.core.model.StateManager;
import org.dapnet.core.model.Transmitter;
import org.dapnet.core.model.TransmitterGroup;
import org.dapnet.core.model.User;
import org.dapnet.core.transmission.TransmissionManager;

/**
 * This class implements the RPC listener and is responsible for handling RPCs
 * over jgroups.
 */
public class RpcListener {
	private static final Logger logger = LogManager.getLogger();
	private final ClusterManager clusterManager;
	private final StateManager stateManager;
	private final Settings settings;

	/**
	 * Constructs a new RPC listener instance.
	 * 
	 * @param clusterManager Cluster manager to use
	 */
	public RpcListener(ClusterManager clusterManager) {
		this.clusterManager = Objects.requireNonNull(clusterManager, "Cluster manager must not be null.");
		stateManager = Objects.requireNonNull(clusterManager.getStateManager(), "State manager must not be null.");
		settings = clusterManager.getTransmitterManager().getSettings();
	}

	/**
	 * Logs a RPC response.
	 * 
	 * @param methodName RPC method name
	 * @param object     Object affected
	 * @param response   Response type
	 */
	private static void logResponse(String methodName, Object object, RpcResponse response) {
		StringBuilder sb = new StringBuilder();
		sb.append(methodName);

		if (object != null) {
			sb.append(' ');
			sb.append(object);
		}

		sb.append(": ");
		sb.append(response);

		Level level = Level.WARN;
		if (response == null) {
			level = Level.ERROR;
		} else if (response == RpcResponse.INTERNAL_ERROR) {
			level = Level.ERROR;
		} else if (response == RpcResponse.OK) {
			level = Level.INFO;
		}

		logger.log(level, sb.toString());
	}

	// ### Call
	// #########################################################################################################
	public RpcResponse postCall(Call call) {
		RpcResponse response = null;

		try {
			// Check Arguments
			if (call == null) {
				return response = RpcResponse.BAD_REQUEST;
			}

			// Validation
			if (!stateManager.validate(call).isEmpty()) {
				return response = RpcResponse.VALIDATION_ERROR;
			}

			Lock lock = stateManager.getLock().writeLock();
			lock.lock();

			try {
				// Add new Object
				stateManager.getCalls().add(call);
			} finally {
				lock.unlock();
			}

			stateManager.getStatistics().incrementCalls();

			if (settings.getModelSettings().isSavingImmediately()) {
				stateManager.writeStateToFile();
			}

			// Transmit new Call
			clusterManager.getTransmissionManager().sendCall(call);

			return response = RpcResponse.OK;
		} catch (Exception e) {
			logger.error("Exception: ", e);

			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("PostCall", call, response);
		}
	}

	// ### Activation
	// ###################################################################################################
	public RpcResponse postActivation(Activation activation) {
		RpcResponse response = null;

		try {
			// Check Arguments
			if (activation == null) {
				return response = RpcResponse.BAD_REQUEST;
			}

			// Validation
			if (!stateManager.validate(activation).isEmpty()) {
				return response = RpcResponse.VALIDATION_ERROR;
			}

			// Transmit Activation
			clusterManager.getTransmissionManager().sendActivation(activation);

			return response = RpcResponse.OK;
		} catch (Exception e) {
			logger.error("Exception: ", e);

			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("PostActivation", activation, response);
		}
	}

	// ### CallSign
	// #####################################################################################################
	public RpcResponse putCallSign(CallSign callSign) {
		RpcResponse response = null;

		try {
			// Check for Quorum
			if (!clusterManager.isQuorum()) {
				return response = RpcResponse.NO_QUORUM;
			}

			// Check Arguments
			if (callSign == null) {
				return response = RpcResponse.BAD_REQUEST;
			}

			// Validation
			if (!stateManager.validate(callSign).isEmpty()) {
				return response = RpcResponse.VALIDATION_ERROR;
			}

			// Add new Object (will replace old one if present)
			Lock lock = stateManager.getLock().writeLock();
			lock.lock();

			try {
				stateManager.getCallSigns().put(callSign.getName(), callSign);
			} finally {
				lock.unlock();
			}

			if (settings.getModelSettings().isSavingImmediately()) {
				stateManager.writeStateToFile();
			}

			return response = RpcResponse.OK;
		} catch (Exception e) {
			logger.error("Exception: ", e);

			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("PutCallSign", callSign, response);
		}
	}

	public RpcResponse deleteCallSign(String callSign) {
		RpcResponse response = null;

		try {
			// Check for Quorum
			if (!clusterManager.isQuorum()) {
				return response = RpcResponse.NO_QUORUM;
			}

			// Check Arguments
			if (callSign == null || callSign.isEmpty()) {
				return response = RpcResponse.BAD_REQUEST;
			}

			Lock lock = stateManager.getLock().writeLock();
			lock.lock();

			try {
				CoreRepository repo = stateManager;

				// Delete depended Objects
				// Delete Calls
				Collection<Call> deleteCalls = new LinkedList<>();
				repo.getCalls().stream().filter(call -> call.getCallSignNames().contains(callSign)).forEach(call -> {
					if (call.getCallSignNames().size() == 1) {
						// Delete all Calls using only this CallSign
						deleteCalls.add(call);
					} else {
						// Remove this CallSign from Calls using more than
						// this CallSign
						call.getCallSignNames().remove(callSign);
					}
				});

				deleteCalls.stream().forEach(call -> repo.getCalls().remove(call));

				// Delete Object with same Name, if existing
				if (repo.getCallSigns().remove(callSign) != null) {
					response = RpcResponse.OK;
				} else {
					// Object not found
					response = RpcResponse.BAD_REQUEST;
				}
			} finally {
				lock.unlock();
			}

			if (settings.getModelSettings().isSavingImmediately()) {
				stateManager.writeStateToFile();
			}

			return response;
		} catch (Exception e) {
			logger.error("Exception: ", e);

			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("DeleteCallSign", callSign, response);
		}
	}

	// ### News
	// #########################################################################################################
	public RpcResponse postNews(News news) {
		RpcResponse response = null;

		try {
			// Check Arguments
			if (news == null) {
				return response = RpcResponse.BAD_REQUEST;
			}

			// Validation
			if (!stateManager.validate(news).isEmpty()) {
				return response = RpcResponse.VALIDATION_ERROR;
			}

			// Add new Object
			Lock lock = stateManager.getLock().writeLock();
			lock.lock();

			try {
				NewsList nl = stateManager.getNews().get(news.getRubricName());
				if (nl != null) {
					nl.add(news);
					stateManager.getStatistics().incrementNews();

					response = RpcResponse.OK;
				} else {
					response = RpcResponse.BAD_REQUEST;
				}
			} finally {
				lock.unlock();
			}

			if (settings.getModelSettings().isSavingImmediately()) {
				stateManager.writeStateToFile();
			}

			return response;
		} catch (Exception e) {
			logger.error("Exception: ", e);

			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("PostNews", news, response);
		}
	}

	// ### Node
	// #########################################################################################################
	// Now only used to inform other nodes that going to suspend mode
	public RpcResponse updateNodeStatus(String nodeName, Node.Status status) {
		RpcResponse response = null;

		try {
			Lock lock = stateManager.getLock().writeLock();
			lock.lock();

			try {
				// Check Arguments
				Node node = stateManager.getNodes().get(nodeName);
				if (node == null || status == null) {
					return response = RpcResponse.BAD_REQUEST;
				}

				// Set Status
				node.setStatus(status);
			} finally {
				lock.unlock();
			}

			if (settings.getModelSettings().isSavingImmediately()) {
				stateManager.writeStateToFile();
			}

			clusterManager.checkQuorum();

			return response = RpcResponse.OK;
		} catch (Exception e) {
			logger.error("Exception: ", e);

			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("UpdateNodeStatus", nodeName + " to " + status, response);
		}
	}

	public RpcResponse putNode(Node node) {
		RpcResponse response = null;

		try {
			// Check for Quorum
			if (!clusterManager.isQuorum()) {
				return response = RpcResponse.NO_QUORUM;
			}

			// Check Arguments
			if (node == null) {
				return response = RpcResponse.BAD_REQUEST;
			}

			// Validation
			if (!stateManager.validate(node).isEmpty()) {
				return response = RpcResponse.VALIDATION_ERROR;
			}

			// Replace object
			Lock lock = stateManager.getLock().writeLock();
			lock.lock();

			try {
				stateManager.getNodes().put(node.getName(), node);
			} finally {
				lock.unlock();
			}

			if (settings.getModelSettings().isSavingImmediately()) {
				stateManager.writeStateToFile();
			}

			clusterManager.checkQuorum();

			return response = RpcResponse.OK;
		} catch (Exception e) {
			logger.error("Exception: ", e);

			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("PutNode", node, response);
		}
	}

	public RpcResponse deleteNode(String node) {
		RpcResponse response = null;

		try {
			// Check for Quorum
			if (!clusterManager.isQuorum()) {
				return response = RpcResponse.NO_QUORUM;
			}

			// Check Arguments
			if (node == null || node.isEmpty()) {
				return response = RpcResponse.BAD_REQUEST;
			}

			Lock lock = stateManager.getLock().writeLock();
			lock.lock();

			// TODO Disconnect transmitters
			// ArrayList<Transmitter> deleteTransmitterNames = new
			// ArrayList<>();
			// clusterManager.getState().getTransmitters().values().stream()
			// .filter(transmitter ->
			// transmitter.getNodeName().equalsIgnoreCase(node))
			// .forEach(transmitter -> deleteTransmitterNames.add(transmitter));
			// deleteTransmitterNames.stream().forEach(t ->
			// clusterManager.getTransmitterManager().disconnectFrom(t));

			try {
				// Delete Object with same Name, if existing
				if (stateManager.getNodes().remove(node) != null) {
					response = RpcResponse.OK;
				} else {
					// Object not found
					response = RpcResponse.BAD_REQUEST;
				}
			} finally {
				lock.unlock();
			}

			if (response == RpcResponse.OK && settings.getModelSettings().isSavingImmediately()) {
				stateManager.writeStateToFile();
			}

			clusterManager.checkQuorum();

			return response;
		} catch (Exception e) {
			logger.error("Exception: ", e);

			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("DeleteNode", node, response);
		}
	}

	// ### Rubric
	// #######################################################################################################
	public RpcResponse putRubric(Rubric rubric) {
		RpcResponse response = null;

		try {
			// Check for Quorum
			if (!clusterManager.isQuorum()) {
				return response = RpcResponse.NO_QUORUM;
			}

			// Check Arguments
			if (rubric == null) {
				return response = RpcResponse.BAD_REQUEST;
			}

			// Validation
			if (!stateManager.validate(rubric).isEmpty()) {
				return response = RpcResponse.VALIDATION_ERROR;
			}

			Lock lock = stateManager.getLock().writeLock();
			lock.lock();

			try {
				final String rubricName = rubric.getName();
				stateManager.getRubrics().put(rubricName, rubric);

				ModelRepository<NewsList> news = stateManager.getNews();
				if (!news.containsKey(rubricName)) {
					NewsList nl = new NewsList();
					nl.setHandler(clusterManager.getTransmissionManager()::sendNewsAsRubric);
					nl.setAddHandler(clusterManager.getTransmissionManager()::sendNewsAsCall);

					news.put(rubricName, nl);
				}
			} finally {
				lock.unlock();
			}

			if (settings.getModelSettings().isSavingImmediately()) {
				stateManager.writeStateToFile();
			}

			// Transmit new Rubric
			clusterManager.getTransmissionManager().sendRubric(rubric);

			return response = RpcResponse.OK;
		} catch (Exception e) {
			logger.error("Exception: ", e);

			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("PutRubric", rubric, response);
		}
	}

	public RpcResponse deleteRubric(String rubricName) {
		RpcResponse response = null;

		try {
			// Check for Quorum
			if (!clusterManager.isQuorum()) {
				return response = RpcResponse.NO_QUORUM;
			}

			// Check Arguments
			if (rubricName == null || rubricName.isEmpty()) {
				return response = RpcResponse.BAD_REQUEST;
			}

			Lock lock = stateManager.getLock().writeLock();
			lock.lock();

			try {
				// Remove news list as well
				stateManager.getNews().remove(rubricName);

				// Delete Object with same Name, if existing
				if (stateManager.getRubrics().remove(rubricName) != null) {
					response = RpcResponse.OK;
				} else {
					// Object not found
					response = RpcResponse.BAD_REQUEST;
				}
			} finally {
				lock.unlock();
			}

			if (settings.getModelSettings().isSavingImmediately()) {
				stateManager.writeStateToFile();
			}

			return response;
		} catch (Exception e) {
			logger.error("Exception: ", e);

			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("DeleteRubric", rubricName, response);
		}
	}

	// ### Transmitter
	// ##################################################################################################
	public RpcResponse updateTransmitterStatus(Transmitter updated) {
		RpcResponse response = null;
		final String transmitterName = updated != null ? updated.getName() : null;

		try {
			if (updated == null) {
				return response = RpcResponse.BAD_REQUEST;
			}

			Lock lock = stateManager.getLock().writeLock();
			lock.lock();

			try {
				Transmitter transmitter = stateManager.getTransmitters().get(transmitterName);
				if (transmitter == null) {
					return response = RpcResponse.BAD_REQUEST;
				}

				transmitter.setNodeName(updated.getNodeName());
				transmitter.setStatus(updated.getStatus());
				transmitter.setConnectedSince(updated.getConnectedSince());
				transmitter.setLastConnected(updated.getLastConnected());
				transmitter.setAddress(updated.getAddress());
				transmitter.setDeviceType(updated.getDeviceType());
				transmitter.setDeviceVersion(updated.getDeviceVersion());
				// transmitter.setLastUpdate(updated.getLastUpdate());
			} finally {
				lock.unlock();
			}

			if (settings.getModelSettings().isSavingImmediately()) {
				stateManager.writeStateToFile();
			}

			return response = RpcResponse.OK;
		} catch (Exception e) {
			logger.error("Exception: ", e);

			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("UpdateTransmitterStatus", transmitterName, response);
		}
	}

	public RpcResponse putTransmitter(Transmitter transmitter) {
		RpcResponse response = null;

		try {
			// Check for Quorum
			if (!clusterManager.isQuorum()) {
				return response = RpcResponse.NO_QUORUM;
			}

			// Check Arguments
			if (transmitter == null) {
				return response = RpcResponse.BAD_REQUEST;
			}

			// Validation
			if (!stateManager.validate(transmitter).isEmpty()) {
				return response = RpcResponse.VALIDATION_ERROR;
			}

			Lock lock = stateManager.getLock().writeLock();
			lock.lock();

			try {
				// Replace object
				final Transmitter oldTransmitter = stateManager.getTransmitters().put(transmitter.getName(),
						transmitter);
				if (oldTransmitter != null) {
					// Disconnect from old transmitter
					clusterManager.getTransmitterManager().disconnectFrom(oldTransmitter);
				}
			} finally {
				lock.unlock();
			}

			if (settings.getModelSettings().isSavingImmediately()) {
				stateManager.writeStateToFile();
			}

			return response = RpcResponse.OK;
		} catch (Exception e) {
			logger.error("Exception: ", e);

			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("PutTransmitter", transmitter, response);
		}
	}

	public RpcResponse deleteTransmitter(String transmitterName) {
		RpcResponse response = null;

		try {
			// Check for Quorum
			if (!clusterManager.isQuorum()) {
				return response = RpcResponse.NO_QUORUM;
			}

			// Check Arguments
			if (transmitterName == null || transmitterName.isEmpty()) {
				return response = RpcResponse.BAD_REQUEST;
			}

			Transmitter transmitter = null;
			Lock lock = stateManager.getLock().writeLock();
			lock.lock();

			try {
				// Delete depended Objects
				// Delete TransmitterGroups
				Set<String> deleteTransmitterGroupNames = new HashSet<>();
				stateManager.getTransmitterGroups().values().stream()
						.filter(transmitterGroup -> transmitterGroup.getTransmitterNames().contains(transmitterName))
						.forEach(transmitterGroup -> {
							if (transmitterGroup.getTransmitterNames().size() == 1) {
								// Delete all TransmitterGroups using only this
								// Transmitter
								deleteTransmitterGroupNames.add(transmitterGroup.getName());
							} else {
								// Remove this Transmitter from TransmitterGroup
								// using more than this Transmitter
								transmitterGroup.getTransmitterNames().remove(transmitterName);
							}
						});
				deleteTransmitterGroupNames.stream().forEach(name -> deleteTransmitterGroup(name));

				transmitter = stateManager.getTransmitters().remove(transmitterName);
				if (transmitter != null) {
					response = RpcResponse.OK;
				} else {
					// Object not found
					response = RpcResponse.BAD_REQUEST;
				}
			} finally {
				lock.unlock();
			}

			if (settings.getModelSettings().isSavingImmediately()) {
				stateManager.writeStateToFile();
			}

			// Disconnect from transmitter
			if (transmitter != null) {
				clusterManager.getTransmitterManager().disconnectFrom(transmitter);
			}

			return response;
		} catch (Exception e) {
			logger.error("Exception: ", e);

			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("DeleteTransmitter", transmitterName, response);
		}
	}

	// ### TransmitterGroup
	// #############################################################################################
	public RpcResponse putTransmitterGroup(TransmitterGroup transmitterGroup) {
		RpcResponse response = null;

		try {
			// Check for Quorum
			if (!clusterManager.isQuorum()) {
				return response = RpcResponse.NO_QUORUM;
			}

			// Check Arguments
			if (transmitterGroup == null) {
				return response = RpcResponse.BAD_REQUEST;
			}

			// Validation
			if (!stateManager.validate(transmitterGroup).isEmpty()) {
				return response = RpcResponse.VALIDATION_ERROR;
			}

			Lock lock = stateManager.getLock().writeLock();
			lock.lock();

			try {
				// Replace object
				stateManager.getTransmitterGroups().put(transmitterGroup.getName(), transmitterGroup);
			} finally {
				lock.unlock();
			}

			if (settings.getModelSettings().isSavingImmediately()) {
				stateManager.writeStateToFile();
			}

			return response = RpcResponse.OK;
		} catch (Exception e) {
			logger.error("Exception: ", e);

			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("PutTransmitterGroup", transmitterGroup, response);
		}
	}

	public RpcResponse deleteTransmitterGroup(String transmitterGroup) {
		RpcResponse response = null;

		try {
			// Check for Quorum
			if (!clusterManager.isQuorum()) {
				return response = RpcResponse.NO_QUORUM;
			}

			// Check Arguments
			if (transmitterGroup == null || transmitterGroup.isEmpty()) {
				return response = RpcResponse.BAD_REQUEST;
			}

			Lock lock = stateManager.getLock().writeLock();
			lock.lock();

			try {
				// Delete depended Objects
				// Delete Rubrics
				Set<String> deleteRubricNames = new HashSet<>();
				stateManager.getRubrics().values().stream()
						.filter(rubric -> rubric.getTransmitterGroupNames().contains(transmitterGroup))
						.forEach(rubric -> {
							if (rubric.getTransmitterGroupNames().size() == 1) {
								// Delete all Rubrics using only this
								// TransmitterGroup
								deleteRubricNames.add(rubric.getName());
							} else {
								// Remove this TransmitterGroup from Rubrics using
								// more than this TransmitterGroup
								rubric.getTransmitterGroupNames().remove(transmitterGroup);
							}
						});
				deleteRubricNames.stream().forEach(name -> deleteRubric(name));

				// Delete Calls
				Collection<Call> deleteCalls = new LinkedList<>();
				stateManager.getCalls().stream()
						.filter(call -> call.getTransmitterGroupNames().contains(transmitterGroup)).forEach(call -> {
							if (call.getTransmitterGroupNames().size() == 1) {
								// Delete all Calls using only this TransmitterGroup
								deleteCalls.add(call);
							} else {
								// Remove this TransmitterGroup from Calls using
								// more than this TransmitterGroup
								call.getTransmitterGroupNames().remove(transmitterGroup);
							}
						});
				deleteCalls.stream().forEach(call -> stateManager.getCalls().remove(call));

				// Delete Object with same Name, if existing
				if (stateManager.getTransmitterGroups().remove(transmitterGroup) != null) {
					response = RpcResponse.OK;
				} else {
					// Object not found
					response = RpcResponse.BAD_REQUEST;
				}
			} finally {
				lock.unlock();
			}

			if (settings.getModelSettings().isSavingImmediately()) {
				stateManager.writeStateToFile();
			}

			return response;
		} catch (Exception e) {
			logger.error("Exception: ", e);

			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("DeleteTransmitterGroup", transmitterGroup, response);
		}
	}

	// ### User
	// #########################################################################################################
	public RpcResponse putUser(User user) {
		RpcResponse response = null;

		try {
			// Check for Quorum
			if (!clusterManager.isQuorum()) {
				return response = RpcResponse.NO_QUORUM;
			}

			// Check Arguments
			if (user == null) {
				return response = RpcResponse.BAD_REQUEST;
			}

			// Validation
			if (!stateManager.validate(user).isEmpty()) {
				return response = RpcResponse.VALIDATION_ERROR;
			}

			Lock lock = stateManager.getLock().writeLock();
			lock.lock();

			try {
				// Add new Object
				stateManager.getUsers().put(user.getName(), user);
			} finally {
				lock.unlock();
			}

			if (settings.getModelSettings().isSavingImmediately()) {
				stateManager.writeStateToFile();
			}

			return response = RpcResponse.OK;
		} catch (Exception e) {
			logger.error("Exception: ", e);

			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("PutUser", user, response);
		}
	}

	public RpcResponse deleteUser(String user) {
		RpcResponse response = null;

		try {
			// Check for Quorum
			if (!clusterManager.isQuorum()) {
				return response = RpcResponse.NO_QUORUM;
			}

			// Check Arguments
			if (user == null || user.isEmpty()) {
				return response = RpcResponse.BAD_REQUEST;
			}

			Lock lock = stateManager.getLock().writeLock();
			lock.lock();

			try {
				// Delete depended Objects
				// Delete CallSigns
				Set<String> deleteCallSignNames = new HashSet<>();
				stateManager.getCallSigns().values().stream()
						.filter(callSign -> callSign.getOwnerNames().contains(user)).forEach(callSign -> {
							if (callSign.getOwnerNames().size() == 1) {
								// Delete all CallSigns which have only this Owner
								deleteCallSignNames.add(callSign.getName());
							} else {
								// Remove this Owner from Calls which have more than
								// this Owner
								callSign.getOwnerNames().remove(user);
							}
						});
				deleteCallSignNames.stream().forEach(name -> deleteCallSign(name));

				// Delete Calls
				Collection<Call> deleteCalls = new LinkedList<>();
				final Collection<Call> calls = stateManager.getCalls();
				calls.stream().filter(call -> call.getOwnerName().equalsIgnoreCase(user))
						.forEach(call -> deleteCalls.add(call));
				deleteCalls.stream().forEach(call -> calls.remove(call));

				// Delete Rubrics
				Set<String> deleteRubricNames = new HashSet<>();
				stateManager.getRubrics().values().stream().filter(rubric -> rubric.getOwnerNames().contains(user))
						.forEach(rubric -> {
							if (rubric.getOwnerNames().size() == 1) {
								// Delete all Rubrics which have only this Owner
								deleteRubricNames.add(rubric.getName());
							} else {
								// Remove this Owner from Rubric which have more
								// than this Owner
								rubric.getOwnerNames().remove(user);
							}
						});
				// Delete news first
				deleteRubricNames.stream().forEach(name -> stateManager.getNews().remove(name));
				deleteRubricNames.stream().forEach(name -> deleteRubric(name));

				// Delete TransmitterGroups
				Set<String> deleteTransmitterGroupNames = new HashSet<>();
				stateManager.getTransmitterGroups().values().stream()
						.filter(transmitterGroup -> transmitterGroup.getOwnerNames().contains(user))
						.forEach(transmitterGroup -> {
							if (transmitterGroup.getOwnerNames().size() == 1) {
								// Delete all TransmitterGroups which have only this
								// Owner
								deleteTransmitterGroupNames.add(transmitterGroup.getName());
							} else {
								// Remove this Owner from TransmitterGroups which
								// have more than this Owner
								transmitterGroup.getOwnerNames().remove(user);
							}
						});
				deleteTransmitterGroupNames.stream().forEach(name -> deleteTransmitterGroup(name));

				// Delete Transmitter
				Set<String> deleteTransmitterNames = new HashSet<>();
				stateManager.getTransmitters().values().stream()
						.filter(transmitter -> transmitter.getOwnerNames().contains(user)).forEach(transmitter -> {
							if (transmitter.getOwnerNames().size() == 1) {
								// Delete all Transmitter which have only this Owner
								deleteTransmitterNames.add(transmitter.getName());
							} else {
								// Remove this Owner from Transmitters which have
								// more than this Owner
								transmitter.getOwnerNames().remove(user);
							}
						});
				deleteTransmitterNames.stream().forEach(name -> deleteTransmitter(name));

				// Delete Object with same Name, if existing
				if (stateManager.getUsers().remove(user) != null) {
					response = RpcResponse.OK;
				} else {
					// Object not found
					response = RpcResponse.BAD_REQUEST;
				}
			} finally {
				lock.unlock();
			}

			if (settings.getModelSettings().isSavingImmediately()) {
				stateManager.writeStateToFile();
			}

			return response;
		} catch (Exception e) {
			logger.error("Exception: ", e);

			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("DeleteUser", user, response);
		}
	}

	public RpcResponse sendRubricNames(String transmitterName) {
		RpcResponse response = null;

		try {
			Lock lock = stateManager.getLock().readLock();
			lock.lock();

			try {
				if (!stateManager.getTransmitters().containsKey(transmitterName)) {
					return RpcResponse.BAD_REQUEST;
				}

				final TransmissionManager manager = clusterManager.getTransmissionManager();
				for (Rubric r : stateManager.getRubrics().values()) {
					manager.sendRubricToTransmitter(r, transmitterName);
				}
			} finally {
				lock.unlock();
			}

			return response = RpcResponse.OK;
		} catch (Exception e) {
			logger.error("Exception: ", e);

			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("SendRubricNames", transmitterName, response);
		}
	}
}