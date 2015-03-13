
package com.t4j.wtk.components.tabs;

import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;

public class T4JTabChangeEvent extends Event {

	private static final long serialVersionUID = 1L;

	private T4JTabbedPanel tabsPanel;

	public T4JTabChangeEvent(Component source) {

		super(source);
	}

	public T4JTabbedPanel getTabsPanel() {

		return tabsPanel;
	}

	public void setTabsPanel(T4JTabbedPanel tabsPanel) {

		this.tabsPanel = tabsPanel;
	}

}
