package org.dapnet.core.model.validator;

import java.util.Objects;

import javax.sound.midi.Transmitter;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.dapnet.core.model.CallSign;
import org.dapnet.core.model.CoreRepository;
import org.dapnet.core.model.ModelRepository;
import org.dapnet.core.model.Rubric;
import org.dapnet.core.model.TransmitterGroup;
import org.dapnet.core.model.User;

public class RepositoryLookupValidator implements ConstraintValidator<RepositoryLookup, String> {

	private final CoreRepository repository;
	private Class<?> objectType;

	public RepositoryLookupValidator() {
		// TODO Use custom implementation
		repository = null;
	}

	public RepositoryLookupValidator(CoreRepository repository) {
		this.repository = Objects.requireNonNull(repository, "Repository must not be null.");
	}

	@Override
	public void initialize(RepositoryLookup constraintAnnotation) {
		this.objectType = constraintAnnotation.value();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
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
