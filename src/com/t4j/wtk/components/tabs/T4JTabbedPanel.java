
package com.t4j.wtk.components.tabs;

import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;

public class T4JTabbedPanel extends TabSheet implements SelectedTabChangeListener {

	private static final long serialVersionUID = 1L;

	public T4JTabbedPanel() {

		this(Boolean.TRUE);
	}

	public T4JTabbedPanel(Boolean enableListener) {

		super();

		addStyleName("t4j-tabbed-panel");

		if (Boolean.TRUE.equals(enableListener)) {

			addListener(this);
		}
	}

	public void selectedTabChange(SelectedTabChangeEvent event) {

		Component selectedTab = event.getTabSheet().getSelectedTab();

		if (selectedTab instanceof T4JTabChangeListener) {

			T4JTabChangeListener t4jTab = (T4JTabChangeListener) selectedTab;

			T4JTabChangeEvent customEvent = new T4JTabChangeEvent(selectedTab);

			customEvent.setTabsPanel(this);

			t4jTab.prepareTabPanelContents(customEvent);
		}
	}
}
