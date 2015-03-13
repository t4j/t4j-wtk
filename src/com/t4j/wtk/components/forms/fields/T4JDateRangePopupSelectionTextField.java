
package com.t4j.wtk.components.forms.fields;

import java.util.Calendar;
import java.util.Date;

import com.t4j.wtk.T4JConstants;
import com.t4j.wtk.application.T4JWebApp;
import com.t4j.wtk.beans.dates.T4JDateRange;
import com.t4j.wtk.components.buttons.T4JLinkButton;
import com.t4j.wtk.components.forms.T4JForm;
import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 *
 * @author Tenentia-4j, S.L.
 *
 */
public class T4JDateRangePopupSelectionTextField extends T4JAbstractSelectionPopupTextField {

	private static final long serialVersionUID = 1L;

	private T4JDateRange dateRange;

	private T4JForm form;

	public T4JDateRangePopupSelectionTextField() {

		super();
	}

	public T4JDateRangePopupSelectionTextField(Property dataSource) {

		super(dataSource);
	}

	public T4JDateRangePopupSelectionTextField(String caption) {

		super(caption);
	}

	public T4JDateRangePopupSelectionTextField(String caption, Property dataSource) {

		super(caption, dataSource);
	}

	public T4JDateRangePopupSelectionTextField(String caption, String value) {

		super(caption, value);
	}

	private void resetFields() {

		form.getField("fromDate").setValue(null);
		form.getField("toDate").setValue(null);

		dateRange.setFromDate(null);
		dateRange.setToDate(null);

		setValue(null);
	}

	private void updateValue() {

		form.commit();

		Date fromDate = dateRange.getFromDate();

		try {

			Calendar tmpCalendar = Calendar.getInstance();
			tmpCalendar.setTime(fromDate);
			tmpCalendar.set(Calendar.HOUR_OF_DAY, 0);
			tmpCalendar.set(Calendar.MINUTE, 0);
			tmpCalendar.set(Calendar.SECOND, 0);
			tmpCalendar.set(Calendar.MILLISECOND, 1);

			fromDate = tmpCalendar.getTime();
		} catch (Exception e) {
			// Ignore
		}

		Date toDate = dateRange.getToDate();

		try {

			Calendar tmpCalendar = Calendar.getInstance();
			tmpCalendar.setTime(toDate);
			tmpCalendar.set(Calendar.HOUR_OF_DAY, 23);
			tmpCalendar.set(Calendar.MINUTE, 59);
			tmpCalendar.set(Calendar.SECOND, 59);
			tmpCalendar.set(Calendar.MILLISECOND, 999);

			toDate = tmpCalendar.getTime();
		} catch (Exception e) {
			// Ignore
		}

		if (null != fromDate && null != toDate && fromDate.after(toDate)) {

			InvalidValueException invalidValueException = new InvalidValueException(T4JWebApp.getInstance().getI18nString("T4JDateRangePopupSelectionTextField.invalidRange"));
			form.setComponentError(invalidValueException);
			return;
		}

		setValue(dateRange);

		T4JWebApp.getInstance().closeDialogWindow();
	}

	@Override
	protected Button generateResetButton() {

		T4JLinkButton resetButton = new T4JLinkButton(T4JWebApp.getInstance().getI18nString("button.clearSelection"), new ClickListener() {

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {

				resetFields();
			}
		});

		resetButton.addStyleName(BaseTheme.BUTTON_LINK);
		resetButton.addStyleName("smallFont");

		return resetButton;
	}

	@Override
	protected Button generateSelectButton() {

		Button generateButton = new Button(T4JWebApp.getInstance().getI18nString("button.accept"), new ClickListener() {

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {

				updateValue();
			}
		});

		generateButton.addStyleName("t4j-button");

		return generateButton;
	}

	@Override
	protected String getDialogTitle() {

		return T4JWebApp.getInstance().getI18nString("T4JDateRangePopupSelectionTextField.dialogTitle");
	}

	@Override
	protected Component getPopupContent() {

		Panel panel = new Panel();
		panel.addStyleName(T4JConstants.LIGHT_PANEL_STYLE_CLASS);

		form = new T4JForm();

		dateRange = new T4JDateRange();

		try {

			T4JDateRange currentValue = (T4JDateRange) getValue();

			if (null != currentValue) {

				dateRange.setFromDate(currentValue.getFromDate());
				dateRange.setToDate(currentValue.getToDate());
			}
		} catch (Exception e) {
			// Ignore
		}

		BeanItem<T4JDateRange> beanItem = new BeanItem<T4JDateRange>(dateRange);

		form.setItemDataSource(beanItem);

		form.setVisibleItemProperties(new Object[] {
			"fromDate", //
			"toDate"
		});

		VerticalLayout panelContent = new VerticalLayout();
		panelContent.addComponent(form);

		panel.setContent(panelContent);

		panel.addAction(new ShortcutListener(T4JConstants.EMPTY_STRING, KeyCode.DELETE, null) {

			private static final long serialVersionUID = 1L;

			@Override
			public void handleAction(Object sender, Object target) {

				resetFields();
			}
		});

		panel.addAction(new ShortcutListener(T4JConstants.EMPTY_STRING, KeyCode.ENTER, null) {

			private static final long serialVersionUID = 1L;

			@Override
			public void handleAction(Object sender, Object target) {

				updateValue();
			}
		});

		panel.addAction(new ShortcutListener(T4JConstants.EMPTY_STRING, KeyCode.ESCAPE, null) {

			private static final long serialVersionUID = 1L;

			@Override
			public void handleAction(Object sender, Object target) {

				T4JWebApp.getInstance().closeDialogWindow();
			}
		});

		return panel;
	}

	@Override
	protected String getTextFieldVisibleValue(Object value) {

		try {
			T4JDateRange t4jDateRange = (T4JDateRange) value;
			return t4jDateRange.toString();
		} catch (Exception e) {
			return T4JConstants.EMPTY_STRING;
		}
	}

	@Override
	protected Class<?> getTypeClass() {

		return T4JDateRange.class;
	}

	@Override
	protected boolean isCloseDialogButtonVisible() {

		return true;
	}

	@Override
	protected void setupComponent() {

		super.setupComponent();

		addStyleName("t4j-daterange-popup-textfield");
	}

}
