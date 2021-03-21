package org.dapnet.core.model.validator;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE_USE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Constraint for checking a pager address. A pager address consists of a 21-bit
 * integer. Optionally the address can be checked for duplicates.
 * 
 * @author Philipp Thiel
 */
@Target({ METHOD, FIELD, TYPE_USE, ANNOTATION_TYPE })
@Constraint(validatedBy = PagerAddressValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PagerAddress {

	String message() default "The pager address '${validatedValue}' is invalid or already registered";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	boolean nullable() default false;

	boolean checkDuplicates() default false;

}
