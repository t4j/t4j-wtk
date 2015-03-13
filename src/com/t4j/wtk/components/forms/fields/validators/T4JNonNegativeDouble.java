
package com.t4j.wtk.components.forms.fields.validators;

public class T4JNonNegativeDouble extends T4JFormFieldValidator {

	private static final long serialVersionUID = 1L;

	public T4JNonNegativeDouble() {

		super();
	}

	public boolean isValid(Object value) {

		try {
			validate(value);
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	public void validate(Object value) throws InvalidValueException {

		if (null == value) {

			if (field.isRequired()) {

				throw new InvalidValueException("value is null");
			}
		} else {

			if (value instanceof Number) {

				Number tmpNumber = (Number) value;

				if (tmpNumber.doubleValue() < 0.0) {

					throw new InvalidValueException("value is negative");
				} else {

					return;
				}

			} else if (value instanceof String) {

				try {

					double d = Double.parseDouble((String) value);

					if (d < 0.0) {

						throw new InvalidValueException("value is negative");
					} else {

						return;
					}

				} catch (Exception e) {

					throw new InvalidValueException("value is not a number");
				}
			} else {

				throw new InvalidValueException("invalid value type");
			}
		}
	}
}
