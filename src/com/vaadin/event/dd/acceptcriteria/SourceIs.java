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
/**
 * 
 */
package com.vaadin.event.dd.acceptcriteria;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.event.TransferableImpl;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.gwt.client.ui.dd.VDragSourceIs;
import com.vaadin.ui.Component;

/**
 * Client side criteria that checks if the drag source is one of the given
 * components.
 * 
 * @since 6.3
 */
@SuppressWarnings("serial")
@ClientCriterion(VDragSourceIs.class)
public class SourceIs extends ClientSideCriterion {

    private Component[] components;

    public SourceIs(Component... component) {
        components = component;
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        int paintedComponents = 0;
        for (int i = 0; i < components.length; i++) {
            Component c = components[i];
            if (c.getApplication() != null) {
                target.addAttribute("component" + paintedComponents++, c);
            } else {
                Logger.getLogger(SourceIs.class.getName())
                        .log(Level.WARNING,
                                "SourceIs component {0} at index {1} is not attached to the component hierachy and will thus be ignored",
                                new Object[] { c.getClass().getName(),
                                        Integer.valueOf(i) });
            }
        }
        target.addAttribute("c", paintedComponents);
    }

    public boolean accept(DragAndDropEvent dragEvent) {
        if (dragEvent.getTransferable() instanceof TransferableImpl) {
            Component sourceComponent = ((TransferableImpl) dragEvent
                    .getTransferable()).getSourceComponent();
            for (Component c : components) {
                if (c == sourceComponent) {
                    return true;
                }
            }
        }

        return false;
    }

}
