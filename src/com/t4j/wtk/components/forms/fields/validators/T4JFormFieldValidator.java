
package com.t4j.wtk.components.forms.fields.validators;

import com.vaadin.data.Validator;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;

public abstract class T4JFormFieldValidator implements Validator {

	private static final long serialVersionUID = 1L;

	protected Form form;

	protected Field field;

	public T4JFormFieldValidator() {

		this(null, null);
	}

	public T4JFormFieldValidator(Form form, Field field) {

		super();

		this.form = form;
		this.field = field;
	}

	public Field getField() {

		return field;
	}

	public Form getForm() {

		return form;
	}

	public void setField(Field field) {

		this.field = field;
	}

	public void setForm(Form form) {

		this.form = form;
	}
}
