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

import java.util.Collection;

import com.vaadin.data.Container;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.gwt.client.ui.VFilterSelect;

/**
 * A filtering dropdown single-select. Suitable for newItemsAllowed, but it's
 * turned of by default to avoid mistakes. Items are filtered based on user
 * input, and loaded dynamically ("lazy-loading") from the server. You can turn
 * on newItemsAllowed and change filtering mode (and also turn it off), but you
 * can not turn on multi-select mode.
 * 
 */
@SuppressWarnings("serial")
@ClientWidget(VFilterSelect.class)
public class ComboBox extends Select {

    private String inputPrompt = null;

    /**
     * If text input is not allowed, the ComboBox behaves like a pretty
     * NativeSelect - the user can not enter any text and clicking the text
     * field opens the drop down with options
     */
    private boolean textInputAllowed = true;

    public ComboBox() {
        setMultiSelect(false);
        setNewItemsAllowed(false);
    }

    public ComboBox(String caption, Collection<?> options) {
        super(caption, options);
        setMultiSelect(false);
        setNewItemsAllowed(false);
    }

    public ComboBox(String caption, Container dataSource) {
        super(caption, dataSource);
        setMultiSelect(false);
        setNewItemsAllowed(false);
    }

    public ComboBox(String caption) {
        super(caption);
        setMultiSelect(false);
        setNewItemsAllowed(false);
    }

    @Override
    public void setMultiSelect(boolean multiSelect) {
        if (multiSelect && !isMultiSelect()) {
            throw new UnsupportedOperationException("Multiselect not supported");
        }
        super.setMultiSelect(multiSelect);
    }

    /**
     * Gets the current input prompt.
     * 
     * @see #setInputPrompt(String)
     * @return the current input prompt, or null if not enabled
     */
    public String getInputPrompt() {
        return inputPrompt;
    }

    /**
     * Sets the input prompt - a textual prompt that is displayed when the
     * select would otherwise be empty, to prompt the user for input.
     * 
     * @param inputPrompt
     *            the desired input prompt, or null to disable
     */
    public void setInputPrompt(String inputPrompt) {
        this.inputPrompt = inputPrompt;
        requestRepaint();
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        if (inputPrompt != null) {
            target.addAttribute("prompt", inputPrompt);
        }
        super.paintContent(target);

        if (!textInputAllowed) {
            target.addAttribute(VFilterSelect.ATTR_NO_TEXT_INPUT, true);
        }
    }

    /**
     * Sets whether it is possible to input text into the field or whether the
     * field area of the component is just used to show what is selected. By
     * disabling text input, the comboBox will work in the same way as a
     * {@link NativeSelect}
     * 
     * @see #isTextInputAllowed()
     * 
     * @param textInputAllowed
     *            true to allow entering text, false to just show the current
     *            selection
     */
    public void setTextInputAllowed(boolean textInputAllowed) {
        this.textInputAllowed = textInputAllowed;
        requestRepaint();
    }

    /**
     * Returns true if the user can enter text into the field to either filter
     * the selections or enter a new value if {@link #isNewItemsAllowed()}
     * returns true. If text input is disabled, the comboBox will work in the
     * same way as a {@link NativeSelect}
     * 
     * @return
     */
    public boolean isTextInputAllowed() {
        return textInputAllowed;
    }

}
