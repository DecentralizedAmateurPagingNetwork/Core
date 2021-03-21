package org.dapnet.core.model.validator;

import org.dapnet.core.model.CallSign;
import org.dapnet.core.model.CoreRepository;
import org.dapnet.core.model.ModelRepository;
import org.dapnet.core.model.Pager;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Constraint validator for pager addresses.
 * 
 * @author Philipp Thiel
 */
public class PagerAddressValidator implements ConstraintValidator<PagerAddress, Integer> {

	private boolean nullable = false;
	private boolean checkDuplicates = false;

	@Override
	public void initialize(PagerAddress constraintAnnotation) {
		nullable = constraintAnnotation.nullable();
		checkDuplicates = constraintAnnotation.checkDuplicates();
	}

	@Override
	public boolean isValid(Integer value, ConstraintValidatorContext context) {
		// Must be set
		if (value == null) {
			return nullable;
		}

		// 21 bits maximum
		if (value < 0 || value > 2097151) {
			return false;
		}

		if (checkDuplicates) {
			final CoreRepository repository = context.unwrap(HibernateConstraintValidatorContext.class)
					.getConstraintValidatorPayload(CoreRepository.class);
			if (repository != null) {
				return checkAddress(repository.getCallSigns(), value);
			}
		}

		return true;
	}

	private static boolean checkAddress(ModelRepository<CallSign> callsigns, int address) {
		for (CallSign cs : callsigns.values()) {
			for (Pager pgr : cs.getPagers()) {
				if (pgr.getNumber() == address) {
					return false;
				}
			}
		}

		return true;
	}

}
