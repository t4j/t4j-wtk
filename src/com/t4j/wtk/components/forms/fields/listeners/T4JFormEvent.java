
package com.t4j.wtk.components.forms.fields.listeners;

import com.t4j.wtk.components.forms.T4JForm;
import com.vaadin.ui.Component.Event;

public class T4JFormEvent extends Event {

	private static final long serialVersionUID = 1L;

	public T4JFormEvent(T4JForm source) {

		super(source);
	}

	@Override
	public T4JForm getComponent() {

		return (T4JForm) super.getComponent();
	}

}
