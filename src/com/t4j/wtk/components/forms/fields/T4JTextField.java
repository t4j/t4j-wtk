
package com.t4j.wtk.components.forms.fields;

import java.io.Serializable;
import java.util.Map;

import com.t4j.wtk.widgetset.client.ui.T4JTextFieldUI;
import com.vaadin.data.Property;
import com.vaadin.data.util.PropertyFormatter;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.gwt.client.MouseEventDetails;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.ClientWidget.LoadStyle;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;

@ClientWidget(value = T4JTextFieldUI.class, loadStyle = LoadStyle.EAGER)
public class T4JTextField extends TextField {

	private static final long serialVersionUID = 1L;

	private String labelHTML;

	private Object bean;

	private PropertyFormatter propertyFormatter;

	public T4JTextField() {

		super();

		setupField();
	}

	public T4JTextField(Property dataSource) {

		super(dataSource);

		setupField();
	}

	public T4JTextField(String caption) {

		super(caption);

		setupField();
	}

	public T4JTextField(String caption, Property dataSource) {

		super(caption, dataSource);

		setupField();
	}

	public T4JTextField(String caption, String value) {

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

		T4JTextFieldClickEvent clickEvent = new T4JTextFieldClickEvent(this);
		fireEvent(clickEvent);
	}

	public void addListener(T4JTextFieldClickListener listener) {

		addListener(T4JTextFieldClickEvent.class, listener, "textFieldClick");
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

	public PropertyFormatter getPropertyFormatter() {

		return propertyFormatter;
	}

	@Override
	public void paintContent(PaintTarget target) throws PaintException {

		if (null == labelHTML) {

			labelHTML = "";
		}

		target.addAttribute("labelHTML", labelHTML);
		super.paintContent(target);
	}

	public void removeListener(T4JTextFieldClickListener listener) {

		removeListener(T4JTextFieldClickEvent.class, listener, "textFieldClick");
	}

	public void setBean(Object bean) {

		this.bean = bean;
	}

	public void setLabelHTML(String labelHTML) {

		this.labelHTML = labelHTML;
		requestRepaint();
	}

	@Override
	public void setPropertyDataSource(Property newDataSource) {

		if (null == propertyFormatter) {
			super.setPropertyDataSource(newDataSource);
		} else {
			propertyFormatter.setPropertyDataSource(newDataSource);
			super.setPropertyDataSource(propertyFormatter);
		}
	}

	public void setPropertyFormatter(PropertyFormatter propertyFormatter) {

		this.propertyFormatter = propertyFormatter;
	}

	public class T4JTextFieldClickEvent extends Component.Event {

		private static final long serialVersionUID = 1L;

		public T4JTextFieldClickEvent(Component source) {

			super(source);
		}

	}

	public interface T4JTextFieldClickListener extends Serializable {

		public void textFieldClick(T4JTextFieldClickEvent event);

	}

}
