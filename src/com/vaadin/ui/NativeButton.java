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

import com.vaadin.data.Property;
import com.vaadin.terminal.gwt.client.ui.VNativeButton;

@SuppressWarnings("serial")
@ClientWidget(VNativeButton.class)
public class NativeButton extends Button {

    public NativeButton() {
        super();
    }

    public NativeButton(String caption) {
        super(caption);
    }

    public NativeButton(String caption, ClickListener listener) {
        super(caption, listener);
    }

    public NativeButton(String caption, Object target, String methodName) {
        super(caption, target, methodName);
    }

    /**
     * Creates a new switch button with initial value.
     * 
     * @param state
     *            the Initial state of the switch-button.
     * @param initialState
     * @deprecated use the {@link CheckBox} component instead
     */
    @Deprecated
    public NativeButton(String caption, boolean initialState) {
        super(caption, initialState);
    }

    /**
     * Creates a new switch button that is connected to a boolean property.
     * 
     * @param state
     *            the Initial state of the switch-button.
     * @param dataSource
     * @deprecated use the {@link CheckBox} component instead
     */
    @Deprecated
    public NativeButton(String caption, Property dataSource) {
        super(caption, dataSource);
    }

}
