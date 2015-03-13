
package com.t4j.wtk.components.forms.fields.listeners;

import java.io.Serializable;

import com.vaadin.ui.Component.Listener;
import com.vaadin.ui.Form;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public abstract class T4JFormEventListener implements Listener, Serializable {

	private static final long serialVersionUID = 1L;

	private Form form;

	public T4JFormEventListener() {

		this(null);
	}

	public T4JFormEventListener(Form form) {

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
