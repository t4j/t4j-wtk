
package com.t4j.wtk.components.tabs;

import java.io.Serializable;

public interface T4JTabChangeListener extends Serializable {

	public void prepareTabPanelContents(T4JTabChangeEvent event);
}
