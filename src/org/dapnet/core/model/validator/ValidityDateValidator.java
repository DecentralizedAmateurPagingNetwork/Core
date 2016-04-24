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

import org.dapnet.core.model.Validity;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidityDateValidator
        implements ConstraintValidator<ValidityDate, Validity> {

    @Override
    public void initialize(ValidityDate constraintAnnotation) {
    }

    @Override
    public boolean isValid(Validity validity, ConstraintValidatorContext context) {
        return validity.getStart()==null || validity.getEnd()==null
                || validity.getStart().before(validity.getEnd());
    }
}