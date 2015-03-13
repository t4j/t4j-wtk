
package com.t4j.wtk.components.forms.fields.validators;

import java.io.Serializable;

import com.vaadin.data.Validator;
import com.vaadin.ui.Form;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public abstract class T4JAbstractFormValidator implements Validator, Serializable {

	private static final long serialVersionUID = 1L;

	private Form form;

	public T4JAbstractFormValidator() {

		this(null);
	}

	public T4JAbstractFormValidator(Form form) {

		super();

		this.form = form;
	}

	public Form getForm() {

		return form;
	}

	public void setForm(Form form) {

		this.form = form;
	}

}
