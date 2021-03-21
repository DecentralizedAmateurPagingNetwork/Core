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
 * This constraint will perform a repository key lookup. The key to be validated
 * must neither be {@code null}, empty or not existing.
 * 
 * @author Phulupp Thiel
 */
@Target({ METHOD, FIELD, TYPE_USE, ANNOTATION_TYPE })
@Constraint(validatedBy = RepositoryLookupValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepositoryLookup {

	/**
	 * The error message.
	 * 
	 * @return Error message
	 */
	String message() default "The key '${validatedValue}' was not found in the repository";

	/**
	 * The optional validation groups.
	 * 
	 * @return Validation groups
	 */
	Class<?>[] groups() default {};

	/**
	 * The optional payload.
	 * 
	 * @return Payload
	 */
	Class<? extends Payload>[] payload() default {};

	/**
	 * The model type to look for.
	 * 
	 * @return Model type
	 */
	Class<?> value();

}
