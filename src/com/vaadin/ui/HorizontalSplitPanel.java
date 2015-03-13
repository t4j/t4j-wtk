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

import com.vaadin.terminal.gwt.client.ui.VSplitPanelHorizontal;
import com.vaadin.ui.ClientWidget.LoadStyle;

/**
 * A horizontal split panel contains two components and lays them horizontally.
 * The first component is on the left side.
 * 
 * <pre>
 * 
 *      +---------------------++----------------------+
 *      |                     ||                      |
 *      | The first component || The second component |
 *      |                     ||                      |
 *      +---------------------++----------------------+
 *                              
 *                            ^
 *                            |
 *                      the splitter
 * 
 * </pre>
 * 
 * @author Vaadin Ltd.
 * @version
 * 6.8.13
 * @since 6.5
 */
@ClientWidget(value = VSplitPanelHorizontal.class, loadStyle = LoadStyle.EAGER)
public class HorizontalSplitPanel extends AbstractSplitPanel {
    public HorizontalSplitPanel() {
        super();
        setSizeFull();
    }
}
