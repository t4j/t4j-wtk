
package com.t4j.wtk.components.buttons;

import java.io.Serializable;

import com.t4j.wtk.components.tables.plain.T4JTableComponent;
import com.vaadin.ui.Button;

/**
 *
 * @author Tenentia-4j, S.L.
 *
 */
public class T4JTableButton extends Button {

	private static final long serialVersionUID = 1L;

	private Boolean isAlwaysActive;

	private Boolean isActiveOnMultipleSelection;

	private T4JTableButtonEnabledValidator enabledValidator;

	private T4JTableButtonRenderedValidator renderedValidator;

	public T4JTableButton() {

		this(null, null, Boolean.FALSE, Boolean.FALSE);
	}

	public T4JTableButton(String caption) {

		this(caption, null, Boolean.FALSE, Boolean.FALSE);
	}

	public T4JTableButton(String caption, ClickListener listener) {

		this(caption, listener, Boolean.FALSE, Boolean.FALSE);
	}

	public T4JTableButton(String caption, ClickListener listener, Boolean isAlwaysActive, Boolean isActiveOnMultipleSelection) {

		this(caption, listener, isAlwaysActive, isActiveOnMultipleSelection, null, null);
	}

	public T4JTableButton(String caption, ClickListener listener, Boolean isAlwaysActive, Boolean isActiveOnMultipleSelection, T4JTableButtonRenderedValidator renderedValidator, T4JTableButtonEnabledValidator enabledValidator) {

		super(caption);

		try {
			addListener(listener);
		} catch (Exception e) {
			// Ignore
		}

		this.isAlwaysActive = isAlwaysActive;
		this.isActiveOnMultipleSelection = isActiveOnMultipleSelection;

		this.renderedValidator = renderedValidator;
		this.enabledValidator = enabledValidator;

		setupComponent();
	}

	public T4JTableButton(String caption, Object target, String methodName) {

		this(caption, target, methodName, Boolean.FALSE, Boolean.FALSE);
	}

	public T4JTableButton(String caption, Object target, String methodName, Boolean isAlwaysActive, Boolean isActiveOnMultipleSelection) {

	}

	public T4JTableButton(String caption, Object target, String methodName, Boolean isAlwaysActive, Boolean isActiveOnMultipleSelection, T4JTableButtonRenderedValidator renderedValidator, T4JTableButtonEnabledValidator enabledValidator) {

		super(caption);

		try {
			addListener(ClickEvent.class, target, methodName);
		} catch (Exception e) {
			// Ignore
		}

		this.isAlwaysActive = isAlwaysActive;
		this.isActiveOnMultipleSelection = isActiveOnMultipleSelection;

		this.renderedValidator = renderedValidator;
		this.enabledValidator = enabledValidator;

		setupComponent();
	}

	private void setupComponent() {

		if (null == isAlwaysActive) {

			isAlwaysActive = Boolean.FALSE;
		}

		if (null == isActiveOnMultipleSelection) {

			isActiveOnMultipleSelection = Boolean.FALSE;
		}

		addStyleName("t4j-button");
		addStyleName("t4j-table-button");
	}

	public boolean checkIsEnabled(T4JTableComponent table) {

		if (null == enabledValidator) {

			return true;
		} else {

			return enabledValidator.checkIsEnabled(table);
		}
	}

	public boolean checkIsRendered(T4JTableComponent table) {

		if (null == renderedValidator) {

			return true;
		} else {

			return renderedValidator.checkIsRendered(table);
		}
	}

	public T4JTableButtonEnabledValidator getEnabledValidator() {

		return enabledValidator;
	}

	public Boolean getIsActiveOnMultipleSelection() {

		return isActiveOnMultipleSelection;
	}

	public Boolean getIsAlwaysActive() {

		return isAlwaysActive;
	}

	public T4JTableButtonRenderedValidator getRenderedValidator() {

		return renderedValidator;
	}

	public void setEnabledValidator(T4JTableButtonEnabledValidator enabledValidator) {

		this.enabledValidator = enabledValidator;
	}

	public void setIsActiveOnMultipleSelection(Boolean isActiveOnMultipleSelection) {

		this.isActiveOnMultipleSelection = isActiveOnMultipleSelection;
	}

	public void setIsAlwaysActive(Boolean isAlwaysActive) {

		this.isAlwaysActive = isAlwaysActive;
	}

	public void setRenderedValidator(T4JTableButtonRenderedValidator renderedValidator) {

		this.renderedValidator = renderedValidator;
	}

	public interface T4JTableButtonEnabledValidator extends Serializable {

		public boolean checkIsEnabled(T4JTableComponent table);
	}

	public interface T4JTableButtonRenderedValidator extends Serializable {

		public boolean checkIsRendered(T4JTableComponent table);
	}

}
