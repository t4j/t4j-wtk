
package com.t4j.wtk.components.forms.fields.validators;

public class T4JPositiveDouble extends T4JFormFieldValidator {

	private static final long serialVersionUID = 1L;

	public T4JPositiveDouble() {

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

				if (tmpNumber.doubleValue() > 0.0) {

					return;
				} else {

					throw new InvalidValueException("value is negative or zero");
				}

			} else if (value instanceof String) {

				try {

					double d = Double.parseDouble((String) value);

					if (d > 0.0) {

						return;
					} else {

						throw new InvalidValueException("value is negative or zero");
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
