
package com.t4j.wtk.components.forms.fields;

import java.io.Serializable;
import java.util.Map;

import com.t4j.wtk.widgetset.client.ui.T4JTextAreaUI;
import com.vaadin.data.Property;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.gwt.client.MouseEventDetails;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.ClientWidget.LoadStyle;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextArea;

@ClientWidget(value = T4JTextAreaUI.class, loadStyle = LoadStyle.EAGER)
public class T4JTextArea extends TextArea {

	private static final long serialVersionUID = 1L;

	private String labelHTML;

	private Object bean;

	public T4JTextArea() {

		super();

		setupField();
	}

	public T4JTextArea(Property dataSource) {

		super(dataSource);

		setupField();

	}

	public T4JTextArea(String caption) {

		super(caption);

		setupField();

	}

	public T4JTextArea(String caption, Property dataSource) {

		super(caption, dataSource);

		setupField();

	}

	public T4JTextArea(String caption, String value) {

		super(caption, value);

		setupField();
	}

	private void setupField() {

		setImmediate(true);
	}

	/**
	 * Fires a click event to all listeners without any event details.
	 *
	 * In subclasses, override {@link #fireClick(MouseEventDetails)} instead of this method.
	 */
	protected void fireClick() {

		T4JTextAreaClickEvent clickEvent = new T4JTextAreaClickEvent(this);
		fireEvent(clickEvent);
	}

	public void addListener(T4JTextAreaClickListener listener) {

		addListener(T4JTextAreaClickEvent.class, listener, "textAreaClick");
	}

	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {

		super.changeVariables(source, variables);

		// Variables set by the widget are returned in the "variables" map.

		if (variables.containsKey("click")) {

			fireClick();
		}
	}

	/**
	 * Simulates a button click, notifying all server-side listeners.
	 *
	 * No action is taken is the button is disabled.
	 */
	public void click() {

		if (isEnabled()) {

			fireClick();
		}
	}

	public Object getBean() {

		return bean;
	}

	public String getLabelHTML() {

		return labelHTML;
	}

	@Override
	public void paintContent(PaintTarget target) throws PaintException {

		if (null == labelHTML) {

			labelHTML = "";
		}

		target.addAttribute("labelHTML", labelHTML);
		super.paintContent(target);
	}

	public void removeListener(T4JTextAreaClickListener listener) {

		removeListener(T4JTextAreaClickEvent.class, listener, "textAreaClick");
	}

	public void setBean(Object bean) {

		this.bean = bean;
	}

	public void setLabelHTML(String labelHTML) {

		this.labelHTML = labelHTML;
		requestRepaint();
	}

	public class T4JTextAreaClickEvent extends Component.Event {

		private static final long serialVersionUID = 1L;

		public T4JTextAreaClickEvent(Component source) {

			super(source);
		}

	}

	public interface T4JTextAreaClickListener extends Serializable {

		public void textAreaClick(T4JTextAreaClickEvent event);

	}

}
