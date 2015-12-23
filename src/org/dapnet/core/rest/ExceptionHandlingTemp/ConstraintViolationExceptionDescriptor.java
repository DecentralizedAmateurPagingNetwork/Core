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

package org.dapnet.core.rest.ExceptionHandlingTemp;

import javax.validation.ConstraintViolation;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ConstraintViolationExceptionDescriptor extends ExceptionDescriptor {
    protected List<Violation> violations;

    public ConstraintViolationExceptionDescriptor(int code, String message, String description, Set<ConstraintViolation<?>> violations) {
        super(code, message, description);
        this.violations = new ArrayList<>();
        //lambda expressions seems not to be supported by this version of jersey
        Iterator<ConstraintViolation<?>> it = violations.iterator();
        while(it.hasNext()){
            this.violations.add(new Violation(it.next()));
        }
    }

    public List<Violation> getViolations() {
        return violations;
    }

    public void setViolations(List<Violation> violations) {
        this.violations = violations;
    }

    public class Violation implements Serializable {
        private int code;
        private String constraint;
        private String field;
        private String value;
        private String message;

        public Violation(int code, String field, String value, String message) {
            this.code = code;

            this.field = field;
            this.value = value;
            this.message = message;
        }

        public Violation(ConstraintViolation<?> violation)
        {
            //if(violation.getConstraintDescriptor().getPayload() != null){
                //String name = violation.getConstraintDescriptor().getPayload().toArray()[0].getSimpleName();
                //this.constraint = ((DescriptionPayload) violation.getConstraintDescriptor().getPayload()).getConstraintName();
                //this.field = ((DescriptionPayload) violation.getConstraintDescriptor().getPayload()).getFieldName();

            /*if(violation.getInvalidValue()==null && ) //Method validation
            {
                switch(violation.getPropertyPath().toString()) //Method name without "get" at the beginning
                {
                    case "owner":
                        this.code = 6201;
                        this.constraint = "ValidOwnerName";
                        this.field = "ownerName";
                        break;
                    case "owners":
                        this.code = 6201;
                        this.constraint = "ValidOwnerNames";
                        this.field = "ownerNames";
                        break;
                    case "transmitterGroups":
                        this.code = 6201;
                        this.constraint = "ValidTransmitterGroupNames";
                        this.field = "transmitterGroupNames";
                        break;
                    case "callSigns":
                        this.code = 6201;
                        this.constraint = "ValidCallSignNames";
                        this.field = "CallSignNames";
                        break;
                    case "rubric":
                        this.code = 6201;
                        this.constraint = "ValidRubricName";
                        this.field = "rubricName";
                        break;
                    case "node":
                        this.code = 6201;
                        this.constraint = "ValidNodeName";
                        this.field = "nodeName";
                        break;
                    case "transmitters":
                        this.code = 6201;
                        this.constraint = "ValidTransmitterName";
                        this.field = "transmitterNames";
                        break;
                } */
            //} else { //Field validation
                this.constraint = violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
                this.field = violation.getPropertyPath().toString();
                this.value = (violation.getInvalidValue() == null) ? null : violation.getInvalidValue().toString();
                switch (this.constraint) {
                    case "NotNull":
                        this.code = 6001;
                        break;
                    case "Size":
                        this.code = 6002;
                        break;
                    case "Min":
                        this.code = 6003;
                        break;
                    case "Max":
                        this.code = 6004;
                        break;
                    case "Digits":
                        this.code = 6005;
                        break;
                    case "TimeSlot":
                        this.code = 6101;
                        break;
                    case "EMail":
                        this.code = 6102;
                        break;
                    default:
                        this.code = 6000;
                }
           // }
            this.message = violation.getMessage();
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
