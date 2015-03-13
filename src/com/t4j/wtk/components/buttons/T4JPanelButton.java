
package com.t4j.wtk.components.buttons;

import java.io.Serializable;

import com.vaadin.ui.Button;

/**
 *
 * @author Tenentia-4j, S.L.
 *
 */
public class T4JPanelButton extends Button {

	private static final long serialVersionUID = 1L;

	private T4JPanelButtonEnabledValidator enabledValidator;

	private T4JPanelButtonRenderedValidator renderedValidator;

	public T4JPanelButton() {

		this(null, null, null, null);
	}

	public T4JPanelButton(String caption) {

		this(caption, null, null, null);
	}

	public T4JPanelButton(String caption, ClickListener listener) {

		this(caption, listener, null, null);
	}

	public T4JPanelButton(String caption, ClickListener listener, T4JPanelButtonRenderedValidator renderedValidator, T4JPanelButtonEnabledValidator enabledValidator) {

		super(caption);

		try {
			addListener(listener);
		} catch (Exception e) {
			// Ignore
		}

		this.renderedValidator = renderedValidator;
		this.enabledValidator = enabledValidator;

		setupComponent();
	}

	public T4JPanelButton(String caption, Object target, String methodName) {

		this(caption, target, methodName, null, null);
	}

	public T4JPanelButton(String caption, Object target, String methodName, T4JPanelButtonRenderedValidator renderedValidator, T4JPanelButtonEnabledValidator enabledValidator) {

		super(caption);

		try {
			addListener(ClickEvent.class, target, methodName);
		} catch (Exception e) {
			// Ignore
		}

		this.renderedValidator = renderedValidator;
		this.enabledValidator = enabledValidator;

		setupComponent();
	}

	private void setupComponent() {

		addStyleName("t4j-button");
		addStyleName("t4j-panel-button");
	}

	public boolean checkIsEnabled() {

		if (null == enabledValidator) {

			return true;
		} else {

			return enabledValidator.checkIsEnabled();
		}
	}

	public boolean checkIsRendered() {

		if (null == renderedValidator) {

			return true;
		} else {

			return renderedValidator.checkIsRendered();
		}
	}

	public T4JPanelButtonEnabledValidator getEnabledValidator() {

		return enabledValidator;
	}

	public T4JPanelButtonRenderedValidator getRenderedValidator() {

		return renderedValidator;
	}

	@Override
	public Class<T4JPanelButton> getType() {

		return T4JPanelButton.class;
	}

	public void setEnabledValidator(T4JPanelButtonEnabledValidator enabledValidator) {

		this.enabledValidator = enabledValidator;
	}

	public void setRenderedValidator(T4JPanelButtonRenderedValidator renderedValidator) {

		this.renderedValidator = renderedValidator;
	}

	public interface T4JPanelButtonEnabledValidator extends Serializable {

		public boolean checkIsEnabled();
	}

	public interface T4JPanelButtonRenderedValidator extends Serializable {

		public boolean checkIsRendered();
	}

}
