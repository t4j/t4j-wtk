
package com.t4j.wtk.widgetset.client.ui;

import java.io.Serializable;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.ui.VTextArea;
import com.vaadin.terminal.gwt.client.ui.VTextField;

public class T4JTextAreaUI extends VTextArea implements Serializable, DoubleClickHandler {

	private static final long serialVersionUID = 1L;

	private Element label;

	public T4JTextAreaUI() {

		super();

		// Tell GWT we are interested in receiving click events
		sinkEvents(Event.ONCLICK);
		sinkEvents(Event.ONDBLCLICK);

		// Add a handler for the click events (this is similar to FocusWidget.addClickHandler())
		addDomHandler(this, DoubleClickEvent.getType());
	}

	public void onDoubleClick(DoubleClickEvent event) {

		client.updateVariable(id, "click", "clicked", true);
	}

	@Override
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {

		super.updateFromUIDL(uidl, client);

		String labelHTML = uidl.getStringAttribute("labelHTML");

		if (null == labelHTML) {

			labelHTML = "";
		}

		if (null == label) {

			label = DOM.createLabel();
			label.setClassName(VTextField.CLASSNAME + "-label");
			getElement().getParentElement().appendChild(label);
		}

		label.setInnerHTML(labelHTML);
	}

}
