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

package org.dapnet.core.model.validator;


import javax.validation.Payload;

public abstract class DescriptionPayload implements Payload {
    public static class ValidOwnerName extends DescriptionPayload {
        public static String constraintName = "Test";
        public static String fieldName = "Test";
    }

    public static class ValidOwnerNames extends DescriptionPayload {
    }

    public static class ValidTransmitterGroupNames extends DescriptionPayload {
    }

    public static class ValidCallSignNames extends DescriptionPayload {
    }

    public static class ValidRubricName extends DescriptionPayload {
    }

    public static class ValidNodeName extends DescriptionPayload {
    }

    public static class ValidTransmitterNames extends DescriptionPayload {
    }
}
