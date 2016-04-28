/*
 * DAPNET CORE PROJECT
 * Copyright (C) 2015
 *
 * Daniel Sialkowski
 *
 * daniel.sialkowski@rwth-aachen.de
 *
 * Institut für Hochfrequenztechnik
 * RWTH AACHEN UNIVERSITY
 * Melatener Str. 25
 * 52074 Aachen
 */

package org.dapnet.core.cluster;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dapnet.core.model.*;

import javax.validation.Validation;
import javax.validation.Validator;
import java.util.ArrayList;

public class RpcListener {
    private static final Logger logger = LogManager.getLogger(RpcListener.class.getName());
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private ClusterManager clusterManager;

    public RpcListener(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

    private void logResponse(String methodName, Object object, RpcResponse response) {
        String objectString = object != null ? (" "+ object.toString()) : "";
        if (response == null) {
            logger.error(methodName + objectString + ": no response");
        } else if (response == RpcResponse.INTERNAL_ERROR) {
            logger.error(methodName + objectString + ": " + response);
        } else if (response == RpcResponse.OK) {
            logger.info(methodName + objectString + ": " + response);
        } else {
            logger.warn(methodName + objectString + ": " + response);
        }
    }

    //### Call #########################################################################################################
    public synchronized RpcResponse postCall(Call call) {
        RpcResponse response = null;
        try {
            //Check Arguments
            if (call == null) {
                return response = RpcResponse.BAD_REQUEST;
            }

            //Validation
            if (validator.validate(call).size() != 0) {
                return response = RpcResponse.VALIDATION_ERROR;
            }

            //Add new Object
            clusterManager.getState().getCalls().add(call);
            clusterManager.getState().writeToFile();

            //Transmit new Call
            clusterManager.getTransmissionManager().handleCall(call);

            return response = RpcResponse.OK;
        } catch (Exception e) {
            logger.error("Exception : ", e);
            return response = RpcResponse.INTERNAL_ERROR;
        } finally {
            logResponse("PostCall", call, response);
        }
    }

    //### Activation ###################################################################################################
    public synchronized RpcResponse postActivation(Activation activation) {
        RpcResponse response = null;
        try {
            //Check Arguments
            if (activation == null) {
                return response = RpcResponse.BAD_REQUEST;
            }

            //Validation
            if (validator.validate(activation).size() != 0) {
                return response = RpcResponse.VALIDATION_ERROR;
            }

            //Transmit Activation
            clusterManager.getTransmissionManager().handleActivation(activation);

            return response = RpcResponse.OK;
        } catch (Exception e) {
            logger.error("Exception : ", e);
            return response = RpcResponse.INTERNAL_ERROR;
        } finally {
            logResponse("PostActivation", activation, response);
        }
    }

    //### CallSign #####################################################################################################
    public synchronized RpcResponse putCallSign(CallSign callSign) {
        RpcResponse response = null;
        try {
            //Check for Quorum
            if (!clusterManager.isQuorum()) {
                return response = RpcResponse.NO_QUORUM;
            }

            //Check Arguments
            if (callSign == null) {
                return response = RpcResponse.BAD_REQUEST;
            }

            //Validation
            if (validator.validate(callSign).size() != 0) {
                return response = RpcResponse.VALIDATION_ERROR;
            }

            //Delete Object with same Name, if existing
            clusterManager.getState().getCallSigns().removeByName(callSign.getName());

            //Add new Object
            clusterManager.getState().getCallSigns().add(callSign);
            clusterManager.getState().writeToFile();
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
            //Check for Quorum
            if (!clusterManager.isQuorum()) {
                return response = RpcResponse.NO_QUORUM;
            }

            //Check Arguments
            if (callSign == null || callSign.isEmpty()) {
                return response = RpcResponse.BAD_REQUEST;
            }

            //Delete depended Objects
            //Delete Calls
            ArrayList<Call> deleteCalls = new ArrayList<>();
            clusterManager.getState().getCalls().stream()
                    .filter(call -> call.getCallSignNames().contains(callSign))
                    .forEach(call -> {
                        if (call.getCallSignNames().size() == 1) {
                            //Delete all Calls using only this CallSign
                            deleteCalls.add(call);
                        } else {
                            //Remove this CallSign from Calls using more than this CallSign
                            call.getCallSignNames().remove(callSign);
                        }
                    });
            deleteCalls.stream().forEach(call -> clusterManager.getState().getCalls().remove(call));

            //Delete Object with same Name, if existing
            if (!clusterManager.getState().getCallSigns().removeByName(callSign)) {
                //Object not found
                return response = RpcResponse.BAD_REQUEST;
            } else {
                clusterManager.getState().writeToFile();
                return response = RpcResponse.OK;
            }
        } catch (Exception e) {
            logger.error("Exception : ", e);
            return response = RpcResponse.INTERNAL_ERROR;
        } finally {
            logResponse("DeleteCallSign", callSign, response);
        }
    }

    //### News #########################################################################################################
    public synchronized RpcResponse postNews(News news) {
        RpcResponse response = null;
        try {
            //Check Arguments
            if (news == null) {
                return response = RpcResponse.BAD_REQUEST;
            }

            //Validation
            if (validator.validate(news).size() != 0) {
                return response = RpcResponse.VALIDATION_ERROR;
            }

            //Add new Object
            clusterManager.getState().getNews().add(news);
            clusterManager.getState().writeToFile();
            clusterManager.getTransmissionManager().handleNews(news);
            return response = RpcResponse.OK;
        } catch (Exception e) {
            logger.error("Exception : ", e);
            return response = RpcResponse.INTERNAL_ERROR;
        } finally {
            logResponse("PostNews", news, response);
        }
    }

    //### Node #########################################################################################################
    public synchronized RpcResponse updateNodeStatus(String nodeName, Node.Status status) {
        RpcResponse response = null;
        try {
            //Check Arguments
            Node node = clusterManager.getState().getNodes().findByName(nodeName);
            if(node == null || status == null)
            {
                return response = RpcResponse.BAD_REQUEST;
            }

            //Set Status
            node.setStatus(status);
            clusterManager.getState().writeToFile();
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
            //Check for Quorum
            if (!clusterManager.isQuorum()) {
                return response = RpcResponse.NO_QUORUM;
            }

            //Check Arguments
            if (node == null) {
                return response = RpcResponse.BAD_REQUEST;
            }

            //Validation
            if (validator.validate(node).size() != 0) {
                return response = RpcResponse.VALIDATION_ERROR;
            }

            //Delete Object with same Name, if existing
            clusterManager.getState().getNodes().removeByName(node.getName());

            //Add new Object
            clusterManager.getState().getNodes().add(node);
            clusterManager.getState().writeToFile();
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
            //Check for Quorum
            if (!clusterManager.isQuorum()) {
                return response = RpcResponse.NO_QUORUM;
            }

            //Check Arguments
            if (node == null || node.isEmpty()) {
                return response = RpcResponse.BAD_REQUEST;
            }

            //Delete depended Objects
            //Delete Transmitters
            ArrayList<String> deleteTransmitterNames = new ArrayList<>();
            clusterManager.getState().getTransmitters().stream()
                    .filter(transmitter -> transmitter.getNodeName().equals(node))
                    .forEach(transmitter -> deleteTransmitterNames.add(transmitter.getName()));
            deleteTransmitterNames.stream().forEach(name -> deleteTransmitter(name));

            //Delete Object with same Name, if existing
            if (!clusterManager.getState().getNodes().removeByName(node)) {
                //Object not found
                return response = RpcResponse.BAD_REQUEST;
            } else {
                clusterManager.getState().writeToFile();
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

    //### Rubric #######################################################################################################
    public synchronized RpcResponse putRubric(Rubric rubric) {
        RpcResponse response = null;
        try {
            //Check for Quorum
            if (!clusterManager.isQuorum()) {
                return response = RpcResponse.NO_QUORUM;
            }

            //Check Arguments
            if (rubric == null) {
                return response = RpcResponse.BAD_REQUEST;
            }

            //Validation
            if (validator.validate(rubric).size() != 0) {
                return response = RpcResponse.VALIDATION_ERROR;
            }

            //Delete Object with same Name, if existing
            clusterManager.getState().getRubrics().removeByName(rubric.getName());

            //Add new Object
            clusterManager.getState().getRubrics().add(rubric);
            clusterManager.getState().writeToFile();

            //Transmit new Rubric
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
            //Check for Quorum
            if (!clusterManager.isQuorum()) {
                return response = RpcResponse.NO_QUORUM;
            }

            //Check Arguments
            if (rubric == null || rubric.isEmpty()) {
                return response = RpcResponse.BAD_REQUEST;
            }

            //Delete depended Objects
            //Delete News
            ArrayList<News> deleteNews = new ArrayList<>();
            clusterManager.getState().getNews().stream()
                    .filter(news -> news.getRubricName().equals(rubric))
                    .forEach(news -> deleteNews.add(news));
            deleteNews.stream().forEach(news -> clusterManager.getState().getNews().remove(news));

            //Delete Object with same Name, if existing
            if (!clusterManager.getState().getRubrics().removeByName(rubric)) {
                //Object not found
                return response = RpcResponse.BAD_REQUEST;
            } else {
                clusterManager.getState().writeToFile();
                return response = RpcResponse.OK;
            }
        } catch (Exception e) {
            logger.error("Exception : ", e);
            return response = RpcResponse.INTERNAL_ERROR;
        } finally {
            logResponse("DeleteRubric", rubric, response);
        }
    }

    //### Transmitter ##################################################################################################
    public synchronized RpcResponse updateTransmitterStatus(String transmitterName, Transmitter.Status status) {
        RpcResponse response = null;
        try {
            //Check Arguments
            Transmitter transmitter = clusterManager.getState().getTransmitters().findByName(transmitterName);
            if(transmitter == null || status == null)
            {
                return response = RpcResponse.BAD_REQUEST;
            }

            //Set Status
            transmitter.setStatus(status);
            clusterManager.getState().writeToFile();
            return response = RpcResponse.OK;
        } catch (Exception e) {
            logger.error("Exception : ", e);
            return response = RpcResponse.INTERNAL_ERROR;
        } finally {
            logResponse("UpdateTransmitterStatus", transmitterName + " to " + status, response);
        }
    }

    public synchronized RpcResponse putTransmitter(Transmitter transmitter) {
        RpcResponse response = null;
        try {
            //Check for Quorum
            if (!clusterManager.isQuorum()) {
                return response = RpcResponse.NO_QUORUM;
            }

            //Check Arguments
            if(transmitter == null)
            {
                return response = RpcResponse.BAD_REQUEST;
            }

            //Validation
            if (validator.validate(transmitter).size() != 0) {
                return response = RpcResponse.VALIDATION_ERROR;
            }

            //Delete Object with same Name, if existing
            Transmitter oldTransmitter = clusterManager.getState().getTransmitters().findByName(transmitter.getName());
            if (oldTransmitter!=null) {
                clusterManager.getState().getTransmitters().remove(oldTransmitter);

                //Disconnect from Transmitter if my Transmitter
                String myNodeName = clusterManager.getChannel().getName();
                if(oldTransmitter.getNodeName().equals(myNodeName)) {
                    clusterManager.getTransmitterDeviceManager().disconnectFromTransmitter(oldTransmitter);
                }
            }

            //Add new Object
            clusterManager.getState().getTransmitters().add(transmitter);
            clusterManager.getState().writeToFile();

            //Connect to Transmitter if my Transmitter
            String myNodeName = clusterManager.getChannel().getName();
            if(transmitter.getNodeName().equals(myNodeName)) {
                clusterManager.getTransmitterDeviceManager().connectToTransmitter(transmitter);
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
            //Check for Quorum
            if (!clusterManager.isQuorum()) {
                return response = RpcResponse.NO_QUORUM;
            }

            //Check Arguments
            if (transmitterName == null || transmitterName.isEmpty()) {
                return response = RpcResponse.BAD_REQUEST;
            }

            //Delete depended Objects
            //Delete TransmitterGroups
            ArrayList<String> deleteTransmitterGroupNames = new ArrayList<>();
            clusterManager.getState().getTransmitterGroups().stream()
                    .filter(transmitterGroup -> transmitterGroup.getTransmitterNames().contains(transmitterName))
                    .forEach(transmitterGroup -> {
                        if (transmitterGroup.getTransmitterNames().size() == 1) {
                            //Delete all TransmitterGroups using only this Transmitter
                            deleteTransmitterGroupNames.add(transmitterGroup.getName());
                        } else {
                            //Remove this Transmitter from TransmitterGroup using more than this Transmitter
                            transmitterGroup.getTransmitterNames().remove(transmitterName);
                        }
                    });
            deleteTransmitterGroupNames.stream().forEach(name -> deleteTransmitterGroup(name));

            Transmitter transmitter = clusterManager.getState().getTransmitters().findByName(transmitterName);
            if(transmitter==null)
            {
                //Object not found
                return response = RpcResponse.BAD_REQUEST;
            }
            else{
                clusterManager.getState().getTransmitters().removeByName(transmitterName);
                clusterManager.getState().writeToFile();

                //Disconnect from Transmitter if my Transmitter
                String myNodeName = clusterManager.getChannel().getName();
                if(transmitter.getNodeName().equals(myNodeName)) {
                    clusterManager.getTransmitterDeviceManager().disconnectFromTransmitter(transmitter);
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

    //### TransmitterGroup #############################################################################################
    public synchronized RpcResponse putTransmitterGroup(TransmitterGroup transmitterGroup) {
        RpcResponse response = null;
        try {
            //Check for Quorum
            if (!clusterManager.isQuorum()) {
                return response = RpcResponse.NO_QUORUM;
            }

            //Check Arguments
            if (transmitterGroup == null) {
                return response = RpcResponse.BAD_REQUEST;
            }

            //Validation
            if (validator.validate(transmitterGroup).size() != 0) {
                return response = RpcResponse.VALIDATION_ERROR;
            }

            //Delete Object with same Name, if existing
            clusterManager.getState().getTransmitterGroups().removeByName(transmitterGroup.getName());

            //Add new Object
            clusterManager.getState().getTransmitterGroups().add(transmitterGroup);
            clusterManager.getState().writeToFile();
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
            //Check for Quorum
            if (!clusterManager.isQuorum()) {
                return response = RpcResponse.NO_QUORUM;
            }

            //Check Arguments
            if (transmitterGroup == null || transmitterGroup.isEmpty()) {
                return response = RpcResponse.BAD_REQUEST;
            }

            //Delete depended Objects
            //Delete Rubrics
            ArrayList<String> deleteRubricNames = new ArrayList<>();
            clusterManager.getState().getRubrics().stream()
                    .filter(rubric -> rubric.getTransmitterGroupNames().contains(transmitterGroup))
                    .forEach(rubric -> {
                        if (rubric.getTransmitterGroupNames().size() == 1) {
                            //Delete all Rubrics using only this TransmitterGroup
                            deleteRubricNames.add(rubric.getName());
                        } else {
                            //Remove this TransmitterGroup from Rubrics using more than this TransmitterGroup
                            rubric.getTransmitterGroupNames().remove(transmitterGroup);
                        }
                    });
            deleteRubricNames.stream().forEach(name -> deleteRubric(name));

            //Delete Calls
            ArrayList<Call> deleteCalls = new ArrayList<>();
            clusterManager.getState().getCalls().stream()
                    .filter(call -> call.getTransmitterGroupNames().contains(transmitterGroup))
                    .forEach(call -> {
                        if (call.getTransmitterGroupNames().size() == 1) {
                            //Delete all Calls using only this TransmitterGroup
                            deleteCalls.add(call);
                        } else {
                            //Remove this TransmitterGroup from Calls using more than this TransmitterGroup
                            call.getTransmitterGroupNames().remove(transmitterGroup);
                        }
                    });
            deleteCalls.stream().forEach(call -> clusterManager.getState().getCalls().remove(call));

            //Delete Object with same Name, if existing
            if (!clusterManager.getState().getTransmitterGroups().removeByName(transmitterGroup)) {
                //Object not found
                return response = RpcResponse.BAD_REQUEST;
            } else {
                clusterManager.getState().writeToFile();
                return response = RpcResponse.OK;
            }
        } catch (Exception e) {
            logger.error("Exception : ", e);
            return response = RpcResponse.INTERNAL_ERROR;
        } finally {
            logResponse("DeleteTransmitterGroup", transmitterGroup, response);
        }
    }

    //### User #########################################################################################################
    public synchronized RpcResponse putUser(User user) {
        RpcResponse response = null;
        try {
            //Check for Quorum
            if (!clusterManager.isQuorum()) {
                return response = RpcResponse.NO_QUORUM;
            }

            //Check Arguments
            if (user == null) {
                return response = RpcResponse.BAD_REQUEST;
            }

            //Validation
            if (validator.validate(user).size() != 0) {
                return response = RpcResponse.VALIDATION_ERROR;
            }

            //Delete Object with same Name, if existing
            clusterManager.getState().getUsers().removeByName(user.getName());

            //Add new Object
            clusterManager.getState().getUsers().add(user);
            clusterManager.getState().writeToFile();
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
            //Check for Quorum
            if (!clusterManager.isQuorum()) {
                return response = RpcResponse.NO_QUORUM;
            }

            //Check Arguments
            if (user == null || user.isEmpty()) {
                return response = RpcResponse.BAD_REQUEST;
            }

            //Delete depended Objects
            //Delete CallSigns
            ArrayList<String> deleteCallSignNames = new ArrayList<>();
            clusterManager.getState().getCallSigns().stream()
                    .filter(callSign -> callSign.getOwnerNames().contains(user))
                    .forEach(callSign -> {
                        if (callSign.getOwnerNames().size() == 1) {
                            //Delete all CallSigns which have only this Owner
                            deleteCallSignNames.add(callSign.getName());
                        } else {
                            //Remove this Owner from Calls which have more than this Owner
                            callSign.getOwnerNames().remove(user);
                        }
                    });
            deleteCallSignNames.stream().forEach(name -> deleteCallSign(name));

            //Delete Calls
            ArrayList<Call> deleteCalls = new ArrayList<>();
            clusterManager.getState().getCalls().stream()
                    .filter(call -> call.getOwnerName().equals(user))
                    .forEach(call -> deleteCalls.add(call));
            deleteCalls.stream().forEach(call -> clusterManager.getState().getCalls().remove(call));

            //Delete News
            ArrayList<News> deleteNews = new ArrayList<>();
            clusterManager.getState().getNews().stream()
                    .filter(news -> news.getOwnerName().equals(user))
                    .forEach(news -> deleteNews.add(news));
            deleteNews.stream().forEach(news -> clusterManager.getState().getNews().remove(news));

            //Delete Rubrics
            ArrayList<String> deleteRubricNames = new ArrayList<>();
            clusterManager.getState().getRubrics().stream()
                    .filter(rubric -> rubric.getOwnerNames().contains(user))
                    .forEach(rubric -> {
                        if (rubric.getOwnerNames().size() == 1) {
                            //Delete all Rubrics which have only this Owner
                            deleteRubricNames.add(rubric.getName());
                        } else {
                            //Remove this Owner from Rubric which have more than this Owner
                            rubric.getOwnerNames().remove(user);
                        }
                    });
            deleteRubricNames.stream().forEach(name -> deleteRubric(name));

            //Delete TransmitterGroups
            ArrayList<String> deleteTransmitterGroupNames = new ArrayList<>();
            clusterManager.getState().getTransmitterGroups().stream()
                    .filter(transmitterGroup -> transmitterGroup.getOwnerNames().contains(user))
                    .forEach(transmitterGroup -> {
                        if (transmitterGroup.getOwnerNames().size() == 1) {
                            //Delete all TransmitterGroups which have only this Owner
                            deleteTransmitterGroupNames.add(transmitterGroup.getName());
                        } else {
                            //Remove this Owner from TransmitterGroups which have more than this Owner
                            transmitterGroup.getOwnerNames().remove(user);
                        }
                    });
            deleteTransmitterGroupNames.stream().forEach(name -> deleteTransmitterGroup(name));

            //Delete Transmitter
            ArrayList<String> deleteTransmitterNames = new ArrayList<>();
            clusterManager.getState().getTransmitters().stream()
                    .filter(transmitter -> transmitter.getOwnerNames().contains(user))
                    .forEach(transmitter -> {
                        if (transmitter.getOwnerNames().size() == 1) {
                            //Delete all Transmitter which have only this Owner
                            deleteTransmitterNames.add(transmitter.getName());
                        } else {
                            //Remove this Owner from Transmitters which have more than this Owner
                            transmitter.getOwnerNames().remove(user);
                        }
                    });
            deleteTransmitterNames.stream().forEach(name -> deleteTransmitter(name));

            //Delete Object with same Name, if existing
            if (!clusterManager.getState().getUsers().removeByName(user)) {
                //Object not found
                return response = RpcResponse.BAD_REQUEST;
            } else {
                clusterManager.getState().writeToFile();
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