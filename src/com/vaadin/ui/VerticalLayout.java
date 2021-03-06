/*
 * Copyright 2011 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.vaadin.ui;

import com.t4j.wtk.T4JConstants;
import com.vaadin.terminal.gwt.client.ui.VVerticalLayout;
import com.vaadin.ui.ClientWidget.LoadStyle;

/**
 * Vertical layout
 * 
 * <code>VerticalLayout</code> is a component container, which shows the subcomponents in the order of their addition (vertically). A vertical layout is by default 100% wide.
 * 
 * @author Vaadin Ltd.
 * @version 6.8.13
 * @since 5.3
 */
@SuppressWarnings("serial")
@ClientWidget(value = VVerticalLayout.class, loadStyle = LoadStyle.EAGER)
public class VerticalLayout extends AbstractOrderedLayout {

	public VerticalLayout() {

		setWidth(T4JConstants.FULL_WIDTH_PERCENTAGE);
	}

}
