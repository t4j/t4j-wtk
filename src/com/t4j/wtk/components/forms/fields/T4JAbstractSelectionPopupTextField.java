
package com.t4j.wtk.components.forms.fields;

import java.io.Serializable;

import com.t4j.wtk.T4JConstants;
import com.t4j.wtk.application.T4JWebApp;
import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 *
 * @author tEnEntia-4j, S.L.
 *
 */
public abstract class T4JAbstractSelectionPopupTextField extends TextField implements FocusListener {

	private static final long serialVersionUID = 1L;

	private Serializable bean;

	public T4JAbstractSelectionPopupTextField() {

		super();

		setupComponent();
	}

	public T4JAbstractSelectionPopupTextField(Property dataSource) {

		super(dataSource);

		setupComponent();
	}

	public T4JAbstractSelectionPopupTextField(String caption) {

		super(caption);

		setupComponent();
	}

	public T4JAbstractSelectionPopupTextField(String caption, Property dataSource) {

		super(caption, dataSource);

		setupComponent();
	}

	public T4JAbstractSelectionPopupTextField(String caption, String value) {

		super(caption, value);

		setupComponent();
	}

	protected abstract Button generateResetButton();

	protected abstract Button generateSelectButton();

	protected abstract String getDialogTitle();

	@Override
	@Deprecated
	protected String getFormattedValue() {

		return getTextFieldVisibleValue(getValue());
	}

	protected abstract Component getPopupContent();

	protected abstract String getTextFieldVisibleValue(Object value);

	protected abstract Class<?> getTypeClass();

	protected abstract boolean isCloseDialogButtonVisible();

	protected void setupComponent() {

		addListener((FocusListener) this);
		addStyleName("t4j-popup-textfield");
		setImmediate(true);
		setInputPrompt(T4JWebApp.getInstance().getI18nString("T4JPopupSelectionTextField.inputPrompt"));
		setNullRepresentation(T4JConstants.EMPTY_STRING);
	}

	public void focus(FocusEvent event) {

		if (isReadOnly()) {

			return;
		}

		VerticalLayout modalWindowLayout = new VerticalLayout();
		modalWindowLayout.setSpacing(true);

		modalWindowLayout.addComponent(getPopupContent());

		HorizontalLayout buttonsWrapperLayout = new HorizontalLayout();
		buttonsWrapperLayout.setSizeUndefined();
		buttonsWrapperLayout.setSpacing(true);

		Button resetButton = generateResetButton();
		buttonsWrapperLayout.addComponent(resetButton);
		buttonsWrapperLayout.setComponentAlignment(resetButton, Alignment.MIDDLE_RIGHT);

		Button selectButton = generateSelectButton();
		buttonsWrapperLayout.addComponent(selectButton);
		buttonsWrapperLayout.setComponentAlignment(selectButton, Alignment.MIDDLE_RIGHT);

		modalWindowLayout.addComponent(buttonsWrapperLayout);
		modalWindowLayout.setComponentAlignment(buttonsWrapperLayout, Alignment.MIDDLE_RIGHT);

		T4JWebApp.getInstance().showModalDialog(getDialogTitle(), modalWindowLayout, Boolean.valueOf(isCloseDialogButtonVisible()), Boolean.FALSE, Boolean.TRUE, null);
	}

	public Object getBean() {

		return bean;
	}

	@Override
	public Class<?> getType() {

		return getTypeClass();
	}

	public void setBean(Serializable bean) {

		this.bean = bean;
	}

}
