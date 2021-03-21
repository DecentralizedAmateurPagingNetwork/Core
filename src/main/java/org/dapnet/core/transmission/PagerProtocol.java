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

import java.time.LocalDateTime;
import java.util.Collection;

import org.dapnet.core.model.Activation;
import org.dapnet.core.model.Call;
import org.dapnet.core.model.News;
import org.dapnet.core.model.Rubric;

public interface PagerProtocol {
	PagerMessage createMessageFromTime(LocalDateTime time);

	PagerMessage createMessageFromTimeSwissphone(LocalDateTime time);

	PagerMessage createMessageFromLocalTimeSwissphone(LocalDateTime time);

	PagerMessage createMessageFromTimeAlphaPoc(LocalDateTime time);

	PagerMessage createMessageFromLocalTimeAlphaPoc(LocalDateTime time);

	PagerMessage createMessageFromRubric(Rubric rubric);

	PagerMessage createMessageFromNews(News news);

	PagerMessage createMessageFromNewsAsCall(News news);

	PagerMessage createMessageFromActivation(Activation avtivation);

	Collection<PagerMessage> createMessagesFromCall(Call call);
}
