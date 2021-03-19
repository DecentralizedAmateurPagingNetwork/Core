package org.dapnet.core.model.validator;

import java.util.Objects;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ValidationException;

import org.dapnet.core.model.CoreRepository;

public class DapnetConstraintValidatorFactory implements ConstraintValidatorFactory {

	private final CoreRepository repository;

	public DapnetConstraintValidatorFactory(CoreRepository repository) {
		this.repository = Objects.requireNonNull(repository, "Repository must not be null.");
	}

	@Override
	public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
		try {
			if (key == RepositoryLookupValidator.class) {
				return key.getConstructor(CoreRepository.class).newInstance(repository);
			} else {
				// TODO Check how hibernate is doing it?
				return key.getConstructor().newInstance();
			}
		} catch (Exception e) {
			throw new ValidationException(e);
		}
	}

	@Override
	public void releaseInstance(ConstraintValidator<?, ?> instance) {
		// Nothing to do here
	}

}
