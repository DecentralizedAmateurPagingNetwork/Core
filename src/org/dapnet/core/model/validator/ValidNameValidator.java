/*
 * DAPNET CORE PROJECT
 * Copyright (C) 2016
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

import org.dapnet.core.model.User;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

public class ValidNameValidator implements ConstraintValidator<ValidName, Object> {
    @Override
    public void initialize(ValidName constraintAnnotation) {
        return;
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        return value!=null;
    }
}