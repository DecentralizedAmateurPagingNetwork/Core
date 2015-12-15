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

package org.dapnet.core.model.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class TimeSlotValidator implements ConstraintValidator<TimeSlot, String> {
    @Override
    public void initialize(TimeSlot constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        /*Only [0-F] in correct order with only occurrence
          Examples for valid Strings:
            0123768EF
            47E
            0F
            4
          Examples for invalid Strings:
            98754
            47H
            33
         */

        int minSlot = -1;
        for (int i = 0; i < value.length(); i++) {
            try {
                int slot = Integer.parseInt(value.substring(i, i + 1), 16);
                if (slot <= minSlot || slot >= 16)
                    return false;
                minSlot = slot;
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }
}