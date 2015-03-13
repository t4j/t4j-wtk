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

import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.BrowserInfo;
import com.vaadin.terminal.gwt.client.UIDL;

public class VAudio extends VMediaBase {
    private static String CLASSNAME = "v-audio";

    private AudioElement audio;

    public VAudio() {
        audio = Document.get().createAudioElement();
        setMediaElement(audio);
        setStyleName(CLASSNAME);
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        if (client.updateComponent(this, uidl, true)) {
            return;
        }
        super.updateFromUIDL(uidl, client);
        Style style = audio.getStyle();

        // Make sure that the controls are not clipped if visible.
        if (shouldShowControls(uidl)
                && (style.getHeight() == null || "".equals(style.getHeight()))) {
            if (BrowserInfo.get().isChrome()) {
                style.setHeight(32, Unit.PX);
            } else {
                style.setHeight(25, Unit.PX);
            }
        }
    }

    @Override
    protected String getDefaultAltHtml() {
        return "Your browser does not support the <code>audio</code> element.";
    }
}
