package org.dapnet.core.model.validator;

import org.dapnet.core.model.CallSign;
import org.dapnet.core.model.CoreRepository;
import org.dapnet.core.model.ModelRepository;
import org.dapnet.core.model.Rubric;
import org.dapnet.core.model.Transmitter;
import org.dapnet.core.model.TransmitterGroup;
import org.dapnet.core.model.User;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * This class implements a constraint validator for repository key lookup.
 * 
 * @author Philipp Thiel
 */
public class RepositoryLookupValidator implements ConstraintValidator<RepositoryLookup, String> {

	private Class<?> objectType;

	@Override
	public void initialize(RepositoryLookup constraintAnnotation) {
		this.objectType = constraintAnnotation.value();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.isEmpty()) {
			return false;
		}

		final CoreRepository repository = context.unwrap(HibernateConstraintValidatorContext.class)
				.getConstraintValidatorPayload(CoreRepository.class);
		if (repository == null) {
			return true;
		}

		if (objectType == CallSign.class) {
			return isValid(repository.getCallSigns(), value);
		} else if (objectType == TransmitterGroup.class) {
			return isValid(repository.getTransmitterGroups(), value);
		} else if (objectType == User.class) {
			return isValid(repository.getUsers(), value);
		} else if (objectType == Rubric.class) {
			return isValid(repository.getRubrics(), value);
		} else if (objectType == Transmitter.class) {
			return isValid(repository.getTransmitters(), value);
		}

		return false;
	}

	private static <T> boolean isValid(ModelRepository<T> repo, String value) {
		return repo.containsKey(value);
	}

}
