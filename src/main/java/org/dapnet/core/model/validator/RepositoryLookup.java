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

@Target({ METHOD, FIELD, TYPE_USE, ANNOTATION_TYPE })
@Constraint(validatedBy = RepositoryLookupValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepositoryLookup {

	String message() default "The key '${validatedValue}' was not found in the repository";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	Class<?> value();

}
