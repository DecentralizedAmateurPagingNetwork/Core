package org.dapnet.core.cluster;

import org.dapnet.core.model.Activation;
import org.dapnet.core.model.Call;
import org.dapnet.core.model.CallSign;
import org.dapnet.core.model.News;
import org.dapnet.core.model.Node;
import org.dapnet.core.model.Rubric;
import org.dapnet.core.model.Transmitter;
import org.dapnet.core.model.TransmitterGroup;
import org.dapnet.core.model.User;

/**
 * Interface for the remote methods supported by the cluster.
 * 
 * @author Philipp Thiel
 */
public interface RemoteMethods {

	boolean postActivation(Activation activation) throws Exception;

	boolean postCall(Call call) throws Exception;

	boolean putCallSign(CallSign callSign) throws Exception;

	boolean deleteCallSign(CallSign callSign) throws Exception;

	boolean postNews(News news) throws Exception;

	boolean putNode(Node node) throws Exception;

	boolean deleteNode(Node node) throws Exception;

	boolean putRubric(Rubric rubric) throws Exception;

	boolean deleteRubric(Rubric rubric) throws Exception;

	boolean putTransmitter(Transmitter transmitter) throws Exception;

	boolean deleteTransmitter(Transmitter transmitter) throws Exception;

	boolean putTransmitterGroup(TransmitterGroup group) throws Exception;

	boolean deleteTransmitterGroup(TransmitterGroup group) throws Exception;

	boolean putUser(User user) throws Exception;

	boolean deleteUser(User user) throws Exception;

	boolean sendRubricNames(String transmitterName) throws Exception;

}
