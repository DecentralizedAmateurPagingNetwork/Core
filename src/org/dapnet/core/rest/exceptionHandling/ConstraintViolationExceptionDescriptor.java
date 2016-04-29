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

package org.dapnet.core.rest.exceptionHandling;

import javax.validation.ConstraintViolation;
import java.io.Serializable;
import java.util.*;

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

    //Used for faked ConstraintViolations
    public ConstraintViolationExceptionDescriptor(int code, String message, String description, Violation violation) {
        super(code, message, description);
        this.violations = new ArrayList<>();
        this.violations.add(violation);
    }

    public List<Violation> getViolations() {
        return violations;
    }

    public void setViolations(List<Violation> violations) {
        this.violations = violations;
    }

    public static class Violation implements Serializable {
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
            //Set Code for all ConstraintViolations

            if(violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName().equals("ValidName"))
            {//Constraint Violation due to invalid names
                Map<String,Object> attributes = violation.getConstraintDescriptor().getAttributes();
                this.constraint = (String) attributes.get("constraintName");
                this.field = (String) attributes.get("fieldName");

                switch(this.constraint)
                {
                    case "ValidOwnerName":
                        this.code = 6201;
                        break;
                    case "ValidOwnerNames":
                        this.code = 6202;
                        break;
                    case "ValidTransmitterGroupNames":
                        this.code = 6203;
                        break;
                    case "ValidCallSignNames":
                        this.code = 6204;
                        break;
                    case "ValidRubricName":
                        this.code = 6205;
                        break;
                    case "ValidNodeName":
                        this.code = 6206;
                        break;
                    case "ValidTransmitterNames":
                        this.code = 6207;
                        break;
                    default: // default code if constraint invalid
                        this.code = 6200;
                }
            } else { //regular constraint violation
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
           }
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
