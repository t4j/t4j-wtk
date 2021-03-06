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
package com.vaadin.terminal.gwt.client.ui;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.BrowserInfo;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;

/**
 * Client side implementation for UriFragmentUtility. Uses GWT's History object
 * as an implementation.
 * 
 */
public class VUriFragmentUtility extends Widget implements Paintable,
        ValueChangeHandler<String> {

    private String fragment;
    private ApplicationConnection client;
    private String paintableId;
    private boolean immediate;
    private HandlerRegistration historyValueHandlerRegistration;

    public VUriFragmentUtility() {
        setElement(Document.get().createDivElement());
        if (BrowserInfo.get().isIE6()) {
            getElement().getStyle().setProperty("overflow", "hidden");
            getElement().getStyle().setProperty("height", "0");
        }
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        historyValueHandlerRegistration = History.addValueChangeHandler(this);
        History.fireCurrentHistoryState();
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        historyValueHandlerRegistration.removeHandler();
    }

    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        if (client.updateComponent(this, uidl, false)) {
            return;
        }
        String uidlFragment = uidl.getStringVariable("fragment");
        immediate = uidl.getBooleanAttribute("immediate");
        if (this.client == null) {
            // initial paint has some special logic
            this.client = client;
            paintableId = uidl.getId();
            // Use fragment from server side for comparison so that an event is
            // sent if the current fragment is not the same as "uidlFragment"
            fragment = uidlFragment;
            History.fireCurrentHistoryState();
        } else {
            if (uidlFragment != null && !uidlFragment.equals(fragment)) {
                fragment = uidlFragment;
                // normal fragment change from server, add new history item
                History.newItem(uidlFragment, false);
            }
        }
    }

    public void onValueChange(ValueChangeEvent<String> event) {
        if (client == null) {
            // can't send the initial fragment before updateFromUIDL has been
            // executed even though the listener is added in attach()

            // We don't want to update the fragment variable either so that we
            // can compare and possibly send the fragment on the next call to
            // this method
            return;
        }
        // TODO hack limited to VTextField - see VView.onWindowClosing()
        VTextField.flushChangesFromFocusedTextField();

        String historyToken = event.getValue();
        if (fragment != null && fragment.equals(historyToken)) {
            // Do nothing if the fragment has not changed. This can at least
            // happen when detaching and attaching a UriFragmentUtility.
            return;
        }
        fragment = historyToken;

        client.updateVariable(paintableId, "fragment", fragment, immediate);
    }
}
