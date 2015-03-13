
package com.t4j.wtk.components.buttons;

import com.vaadin.ui.Button;
import com.vaadin.ui.themes.BaseTheme;

/**
 *
 * @author Tenentia-4j, S.L.
 *
 */
public class T4JLeftMenuButton extends Button {

	private static final long serialVersionUID = 1L;

	public T4JLeftMenuButton() {

		super();

		setupComponent();
	}

	public T4JLeftMenuButton(String caption) {

		super(caption);

		setupComponent();
	}

	public T4JLeftMenuButton(String caption, ClickListener listener) {

		super(caption, listener);

		setupComponent();
	}

	public T4JLeftMenuButton(String caption, Object target, String methodName) {

		super(caption, target, methodName);

		setupComponent();
	}

	private void setupComponent() {

		addStyleName(BaseTheme.BUTTON_LINK);
		addStyleName("t4j-left-menu-button");
	}

}
