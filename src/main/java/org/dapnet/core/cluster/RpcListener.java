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

import java.util.ArrayList;

import javax.validation.Validation;
import javax.validation.Validator;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.Settings;
import org.dapnet.core.model.Activation;
import org.dapnet.core.model.Call;
import org.dapnet.core.model.CallSign;
import org.dapnet.core.model.News;
import org.dapnet.core.model.NewsList;
import org.dapnet.core.model.Node;
import org.dapnet.core.model.Rubric;
import org.dapnet.core.model.Transmitter;
import org.dapnet.core.model.TransmitterGroup;
import org.dapnet.core.model.User;

public class RpcListener {
	private static final Logger logger = LogManager.getLogger();
	private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
	private final ClusterManager clusterManager;

	public RpcListener(ClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}

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
	public synchronized RpcResponse postCall(Call call) {
		RpcResponse response = null;
		try {
			// Check Arguments
			if (call == null) {
				return response = RpcResponse.BAD_REQUEST;
			}

			// Validation
			if (validator.validate(call).size() != 0) {
				return response = RpcResponse.VALIDATION_ERROR;
			}

			// Add new Object
			clusterManager.getState().getCalls().add(call);
			if (Settings.getModelSettings().isSavingImmediately()) {
				clusterManager.getState().writeToFile();
			}

			// Transmit new Call
			clusterManager.getTransmissionManager().handleCall(call);

			return response = RpcResponse.OK;
		} catch (Exception e) {
			logger.error("Exception : ", e);
			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("PostCall", call, response);
		}
	}

	// ### Activation
	// ###################################################################################################
	public synchronized RpcResponse postActivation(Activation activation) {
		RpcResponse response = null;
		try {
			// Check Arguments
			if (activation == null) {
				return response = RpcResponse.BAD_REQUEST;
			}

			// Validation
			if (validator.validate(activation).size() != 0) {
				return response = RpcResponse.VALIDATION_ERROR;
			}

			// Transmit Activation
			clusterManager.getTransmissionManager().handleActivation(activation);

			return response = RpcResponse.OK;
		} catch (Exception e) {
			logger.error("Exception : ", e);
			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("PostActivation", activation, response);
		}
	}

	// ### CallSign
	// #####################################################################################################
	public synchronized RpcResponse putCallSign(CallSign callSign) {
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
			if (validator.validate(callSign).size() != 0) {
				return response = RpcResponse.VALIDATION_ERROR;
			}

			// Add new Object (will replace old one if present)
			clusterManager.getState().getCallSigns().put(callSign.getName(), callSign);
			if (Settings.getModelSettings().isSavingImmediately()) {
				clusterManager.getState().writeToFile();
			}

			return response = RpcResponse.OK;
		} catch (Exception e) {
			logger.error("Exception : ", e);
			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("PutCallSign", callSign, response);
		}
	}

	public synchronized RpcResponse deleteCallSign(String callSign) {
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

			// Delete depended Objects
			// Delete Calls
			ArrayList<Call> deleteCalls = new ArrayList<>();
			clusterManager.getState().getCalls().stream().filter(call -> call.getCallSignNames().contains(callSign))
					.forEach(call -> {
						if (call.getCallSignNames().size() == 1) {
							// Delete all Calls using only this CallSign
							deleteCalls.add(call);
						} else {
							// Remove this CallSign from Calls using more than
							// this CallSign
							call.getCallSignNames().remove(callSign);
						}
					});
			deleteCalls.stream().forEach(call -> clusterManager.getState().getCalls().remove(call));

			// Delete Object with same Name, if existing
			if (clusterManager.getState().getCallSigns().remove(callSign) == null) {
				// Object not found
				return response = RpcResponse.BAD_REQUEST;
			} else {
				if (Settings.getModelSettings().isSavingImmediately()) {
					clusterManager.getState().writeToFile();
				}

				return response = RpcResponse.OK;
			}
		} catch (Exception e) {
			logger.error("Exception : ", e);
			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("DeleteCallSign", callSign, response);
		}
	}

	// ### News
	// #########################################################################################################
	public synchronized RpcResponse postNews(News news) {
		RpcResponse response = null;
		try {
			// Check Arguments
			if (news == null) {
				return response = RpcResponse.BAD_REQUEST;
			}

			// Validation
			if (validator.validate(news).size() != 0) {
				return response = RpcResponse.VALIDATION_ERROR;
			}

			// Add new Object
			NewsList nl = clusterManager.getState().getNews().get(news.getRubricName().toLowerCase());
			if (nl != null) {
				nl.add(news);

				if (Settings.getModelSettings().isSavingImmediately()) {
					clusterManager.getState().writeToFile();
				}

				return response = RpcResponse.OK;
			} else {
				return response = RpcResponse.BAD_REQUEST;
			}
		} catch (Exception e) {
			logger.error("Exception : ", e);
			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("PostNews", news, response);
		}
	}

	// ### Node
	// #########################################################################################################
	// Now only used to inform other nodes that going to suspend mode
	public synchronized RpcResponse updateNodeStatus(String nodeName, Node.Status status) {
		RpcResponse response = null;
		try {
			// Check Arguments
			Node node = clusterManager.getState().getNodes().get(nodeName);
			if (node == null || status == null) {
				return response = RpcResponse.BAD_REQUEST;
			}

			// Set Status
			node.setStatus(status);
			if (Settings.getModelSettings().isSavingImmediately()) {
				clusterManager.getState().writeToFile();
			}

			clusterManager.checkQuorum();

			return response = RpcResponse.OK;
		} catch (Exception e) {
			logger.error("Exception : ", e);
			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("UpdateNodeStatus", nodeName + " to " + status, response);
		}
	}

	public synchronized RpcResponse putNode(Node node) {
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
			if (validator.validate(node).size() != 0) {
				return response = RpcResponse.VALIDATION_ERROR;
			}

			// Replace object
			clusterManager.getState().getNodes().put(node.getName(), node);
			if (Settings.getModelSettings().isSavingImmediately()) {
				clusterManager.getState().writeToFile();
			}

			clusterManager.checkQuorum();

			return response = RpcResponse.OK;
		} catch (Exception e) {
			logger.error("Exception : ", e);
			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("PutNode", node, response);
		}
	}

	public synchronized RpcResponse deleteNode(String node) {
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

			// Delete depended Objects
			// Delete Transmitters
			ArrayList<String> deleteTransmitterNames = new ArrayList<>();
			clusterManager.getState().getTransmitters().values().stream()
					.filter(transmitter -> transmitter.getNodeName().equalsIgnoreCase(node))
					.forEach(transmitter -> deleteTransmitterNames.add(transmitter.getName()));
			deleteTransmitterNames.stream().forEach(name -> deleteTransmitter(name));

			// Delete Object with same Name, if existing
			if (clusterManager.getState().getNodes().remove(node) == null) {
				// Object not found
				return response = RpcResponse.BAD_REQUEST;
			} else {
				if (Settings.getModelSettings().isSavingImmediately()) {
					clusterManager.getState().writeToFile();
				}

				clusterManager.checkQuorum();

				return response = RpcResponse.OK;
			}
		} catch (Exception e) {
			logger.error("Exception : ", e);
			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("DeleteNode", node, response);
		}
	}

	// ### Rubric
	// #######################################################################################################
	public synchronized RpcResponse putRubric(Rubric rubric) {
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
			if (validator.validate(rubric).size() != 0) {
				return response = RpcResponse.VALIDATION_ERROR;
			}

			// Replace object
			final String rubricName = rubric.getName().toLowerCase();
			clusterManager.getState().getRubrics().put(rubricName, rubric);

			// Register new news list if missing
			if (!clusterManager.getState().getNews().containsKey(rubricName)) {
				NewsList nl = new NewsList();
				nl.setHandler(clusterManager.getTransmissionManager()::handleNews);
				clusterManager.getState().getNews().put(rubricName, nl);
			}

			if (Settings.getModelSettings().isSavingImmediately()) {
				clusterManager.getState().writeToFile();
			}

			// Transmit new Rubric
			clusterManager.getTransmissionManager().handleRubric(rubric);

			return response = RpcResponse.OK;
		} catch (Exception e) {
			logger.error("Exception : ", e);
			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("PutRubric", rubric, response);
		}
	}

	public synchronized RpcResponse deleteRubric(String rubric) {
		RpcResponse response = null;
		try {
			// Check for Quorum
			if (!clusterManager.isQuorum()) {
				return response = RpcResponse.NO_QUORUM;
			}

			// Check Arguments
			if (rubric == null || rubric.isEmpty()) {
				return response = RpcResponse.BAD_REQUEST;
			}

			rubric = rubric.toLowerCase();
			// Remove news list as well
			clusterManager.getState().getNews().remove(rubric);

			// Delete Object with same Name, if existing
			if (clusterManager.getState().getRubrics().remove(rubric) == null) {
				// Object not found
				return response = RpcResponse.BAD_REQUEST;
			} else {
				if (Settings.getModelSettings().isSavingImmediately()) {
					clusterManager.getState().writeToFile();
				}

				return response = RpcResponse.OK;
			}
		} catch (Exception e) {
			logger.error("Exception : ", e);
			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("DeleteRubric", rubric, response);
		}
	}

	// ### Transmitter
	// ##################################################################################################
	public synchronized RpcResponse updateTransmitterStatus(Transmitter updated) {
		RpcResponse response = null;
		String name = updated != null ? updated.getName() : null;

		try {
			Transmitter transmitter = clusterManager.getState().getTransmitters().get(name);
			if (transmitter == null) {
				return response = RpcResponse.BAD_REQUEST;
			}

			transmitter.setStatus(updated.getStatus());
			transmitter.setConnectedSince(updated.getConnectedSince());
			transmitter.setAddress(updated.getAddress());
			transmitter.setDeviceType(updated.getDeviceType());
			transmitter.setDeviceVersion(updated.getDeviceVersion());

			if (Settings.getModelSettings().isSavingImmediately()) {
				clusterManager.getState().writeToFile();
			}

			return response = RpcResponse.OK;
		} catch (Exception e) {
			logger.error("Exception : ", e);
			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("UpdateTransmitterStatus", name, response);
		}
	}

	public synchronized RpcResponse putTransmitter(Transmitter transmitter) {
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
			if (validator.validate(transmitter).size() != 0) {
				return response = RpcResponse.VALIDATION_ERROR;
			}

			// Replace object
			Transmitter oldTransmitter = clusterManager.getState().getTransmitters().put(transmitter.getName(),
					transmitter);
			if (oldTransmitter != null) {
				// Disconnect from old transmitter if my Transmitter
				String myNodeName = clusterManager.getChannel().getName();
				if (oldTransmitter.getNodeName().equalsIgnoreCase(myNodeName)) {
					clusterManager.getTransmitterManager().removeTransmitter(oldTransmitter);
				}
			}

			if (Settings.getModelSettings().isSavingImmediately()) {
				clusterManager.getState().writeToFile();
			}

			// Connect to Transmitter if my Transmitter
			String myNodeName = clusterManager.getChannel().getName();
			if (transmitter.getNodeName().equalsIgnoreCase(myNodeName)) {
				clusterManager.getTransmitterManager().addTransmitter(transmitter);
			}

			return response = RpcResponse.OK;
		} catch (Exception e) {
			logger.error("Exception : ", e);
			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("PutTransmitter", transmitter, response);
		}
	}

	public synchronized RpcResponse deleteTransmitter(String transmitterName) {
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

			// Delete depended Objects
			// Delete TransmitterGroups
			ArrayList<String> deleteTransmitterGroupNames = new ArrayList<>();
			clusterManager.getState().getTransmitterGroups().values().stream()
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

			Transmitter transmitter = clusterManager.getState().getTransmitters().remove(transmitterName);
			if (transmitter == null) {
				// Object not found
				return response = RpcResponse.BAD_REQUEST;
			} else {
				if (Settings.getModelSettings().isSavingImmediately()) {
					clusterManager.getState().writeToFile();
				}

				// Disconnect from Transmitter if my Transmitter
				String myNodeName = clusterManager.getChannel().getName();
				if (transmitter.getNodeName().equalsIgnoreCase(myNodeName)) {
					clusterManager.getTransmitterManager().removeTransmitter(transmitter);
				}

				return response = RpcResponse.OK;
			}
		} catch (Exception e) {
			logger.error("Exception : ", e);
			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("DeleteTransmitter", transmitterName, response);
		}
	}

	// ### TransmitterGroup
	// #############################################################################################
	public synchronized RpcResponse putTransmitterGroup(TransmitterGroup transmitterGroup) {
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
			if (validator.validate(transmitterGroup).size() != 0) {
				return response = RpcResponse.VALIDATION_ERROR;
			}

			// Replace object
			clusterManager.getState().getTransmitterGroups().put(transmitterGroup.getName(), transmitterGroup);
			if (Settings.getModelSettings().isSavingImmediately()) {
				clusterManager.getState().writeToFile();
			}

			return response = RpcResponse.OK;
		} catch (Exception e) {
			logger.error("Exception : ", e);
			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("PutTransmitterGroup", transmitterGroup, response);
		}
	}

	public synchronized RpcResponse deleteTransmitterGroup(String transmitterGroup) {
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

			// Delete depended Objects
			// Delete Rubrics
			ArrayList<String> deleteRubricNames = new ArrayList<>();
			clusterManager.getState().getRubrics().values().stream()
					.filter(rubric -> rubric.getTransmitterGroupNames().contains(transmitterGroup)).forEach(rubric -> {
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
			ArrayList<Call> deleteCalls = new ArrayList<>();
			clusterManager.getState().getCalls().stream()
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
			deleteCalls.stream().forEach(call -> clusterManager.getState().getCalls().remove(call));

			// Delete Object with same Name, if existing
			if (clusterManager.getState().getTransmitterGroups().remove(transmitterGroup) == null) {
				// Object not found
				return response = RpcResponse.BAD_REQUEST;
			} else {
				if (Settings.getModelSettings().isSavingImmediately()) {
					clusterManager.getState().writeToFile();
				}

				return response = RpcResponse.OK;
			}
		} catch (Exception e) {
			logger.error("Exception : ", e);
			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("DeleteTransmitterGroup", transmitterGroup, response);
		}
	}

	// ### User
	// #########################################################################################################
	public synchronized RpcResponse putUser(User user) {
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
			if (validator.validate(user).size() != 0) {
				return response = RpcResponse.VALIDATION_ERROR;
			}

			// Add new Object
			clusterManager.getState().getUsers().put(user.getName(), user);
			if (Settings.getModelSettings().isSavingImmediately()) {
				clusterManager.getState().writeToFile();
			}

			return response = RpcResponse.OK;
		} catch (Exception e) {
			logger.error("Exception : ", e);
			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("PutUser", user, response);
		}
	}

	public synchronized RpcResponse deleteUser(String user) {
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

			// Delete depended Objects
			// Delete CallSigns
			ArrayList<String> deleteCallSignNames = new ArrayList<>();
			clusterManager.getState().getCallSigns().values().stream()
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
			ArrayList<Call> deleteCalls = new ArrayList<>();
			clusterManager.getState().getCalls().stream().filter(call -> call.getOwnerName().equalsIgnoreCase(user))
					.forEach(call -> deleteCalls.add(call));
			deleteCalls.stream().forEach(call -> clusterManager.getState().getCalls().remove(call));

			// Delete Rubrics
			ArrayList<String> deleteRubricNames = new ArrayList<>();
			clusterManager.getState().getRubrics().values().stream()
					.filter(rubric -> rubric.getOwnerNames().contains(user)).forEach(rubric -> {
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
			deleteRubricNames.stream().forEach(name -> clusterManager.getState().getNews().remove(name));
			deleteRubricNames.stream().forEach(name -> deleteRubric(name));

			// Delete TransmitterGroups
			ArrayList<String> deleteTransmitterGroupNames = new ArrayList<>();
			clusterManager.getState().getTransmitterGroups().values().stream()
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
			ArrayList<String> deleteTransmitterNames = new ArrayList<>();
			clusterManager.getState().getTransmitters().values().stream()
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
			if (clusterManager.getState().getUsers().remove(user) == null) {
				// Object not found
				return response = RpcResponse.BAD_REQUEST;
			} else {
				if (Settings.getModelSettings().isSavingImmediately()) {
					clusterManager.getState().writeToFile();
				}

				return response = RpcResponse.OK;
			}
		} catch (Exception e) {
			logger.error("Exception : ", e);
			return response = RpcResponse.INTERNAL_ERROR;
		} finally {
			logResponse("DeleteUser", user, response);
		}
	}
}