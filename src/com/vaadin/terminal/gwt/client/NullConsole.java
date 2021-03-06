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

package com.vaadin.terminal.gwt.client;

import java.util.Set;

import com.google.gwt.core.client.GWT;

/**
 * Client side console implementation for non-debug mode that discards all
 * messages.
 * 
 */
public class NullConsole implements Console {

    public void dirUIDL(ValueMap u, ApplicationConfiguration cnf) {
    }

    public void error(String msg) {
        GWT.log(msg);
    }

    public void log(String msg) {
        GWT.log(msg);
    }

    public void printObject(Object msg) {
        GWT.log(msg.toString());
    }

    public void printLayoutProblems(ValueMap meta,
            ApplicationConnection applicationConnection,
            Set<Paintable> zeroHeightComponents,
            Set<Paintable> zeroWidthComponents) {
    }

    public void log(Throwable e) {
        GWT.log(e.getMessage(), e);
    }

    public void error(Throwable e) {
        GWT.log(e.getMessage(), e);
    }

    public void setQuietMode(boolean quietDebugMode) {
    }

    public void init() {
    }

}
