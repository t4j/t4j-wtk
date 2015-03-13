
package com.t4j.wtk.components.tabs;

import com.t4j.wtk.T4JConstants;
import com.vaadin.ui.VerticalLayout;

/**
 * Clase abstracta que representa una pestaña en un panel de pestañas.
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public abstract class T4JTab extends VerticalLayout implements T4JTabChangeListener {

	private static final long serialVersionUID = 1L;

	public T4JTab() {

		super();

		addStyleName("t4j-tab-panel-wrapper");
		setMargin(true);
		setSpacing(true);
		setWidth(T4JConstants.FULL_WIDTH_PERCENTAGE);
	}

}
