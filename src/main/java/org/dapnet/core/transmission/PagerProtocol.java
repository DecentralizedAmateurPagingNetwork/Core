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

package org.dapnet.core.transmission;

import org.dapnet.core.model.Activation;
import org.dapnet.core.model.Call;
import org.dapnet.core.model.News;
import org.dapnet.core.model.Rubric;

import java.util.Date;
import java.util.List;

public interface PagerProtocol {
    Message createMessageFromTime(Date date);

    Message createMessageFromRubric(Rubric rubric);

    Message createMessageFromNews(News news);

    Message createMessageFromActivation(Activation avtivation);

    List<Message> createMessagesFromCall(Call call);
}
