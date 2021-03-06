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

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.vaadin.terminal.gwt.client.BrowserInfo;
import com.vaadin.terminal.gwt.client.Util;

/**
 * In Vaadin UI this Overlay should always be used for all elements that
 * temporary float over other components like context menus etc. This is to deal
 * stacking order correctly with VWindow objects.
 */
public class VOverlay extends PopupPanel implements CloseHandler<PopupPanel> {

    public static class PositionAndSize {
        private int left, top, width, height;

        public PositionAndSize(int left, int top, int width, int height) {
            super();
            setLeft(left);
            setTop(top);
            setWidth(width);
            setHeight(height);
        }

        public int getLeft() {
            return left;
        }

        public void setLeft(int left) {
            this.left = left;
        }

        public int getTop() {
            return top;
        }

        public void setTop(int top) {
            this.top = top;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            if (width < 0) {
                width = 0;
            }

            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            if (height < 0) {
                height = 0;
            }

            this.height = height;
        }

        public void setAnimationFromCenterProgress(double progress) {
            left += (int) (width * (1.0 - progress) / 2.0);
            top += (int) (height * (1.0 - progress) / 2.0);
            width = (int) (width * progress);
            height = (int) (height * progress);
        }
    }

    /*
     * The z-index value from where all overlays live. This can be overridden in
     * any extending class.
     */
    protected static int Z_INDEX = 20000;

    private static int leftFix = -1;

    private static int topFix = -1;

    /*
     * Shadow element style. If an extending class wishes to use a different
     * style of shadow, it can use setShadowStyle(String) to give the shadow
     * element a new style name.
     */
    public static final String CLASSNAME_SHADOW = "v-shadow";

    /*
     * The shadow element for this overlay.
     */
    private Element shadow;

    /**
     * The shim iframe behind the overlay, allowing PDFs and applets to be
     * covered by overlays.
     */
    private IFrameElement shimElement;

    /**
     * The HTML snippet that is used to render the actual shadow. In consists of
     * nine different DIV-elements with the following class names:
     * 
     * <pre class='code'>
     *   .v-shadow[-stylename]
     *   ----------------------------------------------
     *   | .top-left     |   .top    |     .top-right |
     *   |---------------|-----------|----------------|
     *   |               |           |                |
     *   | .left         |  .center  |         .right |
     *   |               |           |                |
     *   |---------------|-----------|----------------|
     *   | .bottom-left  |  .bottom  |  .bottom-right |
     *   ----------------------------------------------
     * </pre>
     * 
     * See default theme 'shadow.css' for implementation example.
     */
    private static final String SHADOW_HTML = "<div class=\"top-left\"></div><div class=\"top\"></div><div class=\"top-right\"></div><div class=\"left\"></div><div class=\"center\"></div><div class=\"right\"></div><div class=\"bottom-left\"></div><div class=\"bottom\"></div><div class=\"bottom-right\"></div>";

    /**
     * Matches {@link PopupPanel}.ANIMATION_DURATION
     */
    private static final int POPUP_PANEL_ANIMATION_DURATION = 200;

    private boolean sinkShadowEvents = false;

    public VOverlay() {
        super();
        adjustZIndex();
    }

    public VOverlay(boolean autoHide) {
        super(autoHide);
        adjustZIndex();
    }

    public VOverlay(boolean autoHide, boolean modal) {
        super(autoHide, modal);
        adjustZIndex();
    }

    public VOverlay(boolean autoHide, boolean modal, boolean showShadow) {
        super(autoHide, modal);
        setShadowEnabled(showShadow);
        adjustZIndex();
    }

    /**
     * Method to controle whether DOM elements for shadow are added. With this
     * method subclasses can control displaying of shadow also after the
     * constructor.
     * 
     * @param enabled
     *            true if shadow should be displayed
     */
    protected void setShadowEnabled(boolean enabled) {
        if (enabled != isShadowEnabled()) {
            if (enabled) {
                shadow = DOM.createDiv();
                shadow.setClassName(CLASSNAME_SHADOW);
                shadow.setInnerHTML(SHADOW_HTML);
                DOM.setStyleAttribute(shadow, "position", "absolute");
                addCloseHandler(this);
            } else {
                removeShadowIfPresent();
                shadow = null;
            }
        }
    }

    protected boolean isShadowEnabled() {
        return shadow != null;
    }

    private void removeShimElement() {
        if (shimElement != null) {
            shimElement.removeFromParent();
        }
    }

    private void removeShadowIfPresent() {
        if (isShadowAttached()) {
            shadow.removeFromParent();

            // Remove event listener from the shadow
            unsinkShadowEvents();
        }
    }

    private boolean isShadowAttached() {
        return isShadowEnabled() && shadow.getParentElement() != null;
    }

    private boolean isShimElementAttached() {
        return shimElement != null && shimElement.hasParentElement();
    }

    private void adjustZIndex() {
        setZIndex(Z_INDEX);
    }

    /**
     * Set the z-index (visual stack position) for this overlay.
     * 
     * @param zIndex
     *            The new z-index
     */
    protected void setZIndex(int zIndex) {
        DOM.setStyleAttribute(getElement(), "zIndex", "" + zIndex);
        if (isShadowEnabled()) {
            DOM.setStyleAttribute(shadow, "zIndex", "" + zIndex);
        }
    }

    @Override
    public void setPopupPosition(int left, int top) {
        // TODO, this should in fact be part of
        // Document.get().getBodyOffsetLeft/Top(). Would require overriding DOM
        // for all permutations. Now adding fix as margin instead of fixing
        // left/top because parent class saves the position.
        Style style = getElement().getStyle();
        style.setMarginLeft(-adjustByRelativeLeftBodyMargin(), Unit.PX);
        style.setMarginTop(-adjustByRelativeTopBodyMargin(), Unit.PX);
        super.setPopupPosition(left, top);
        positionOrSizeUpdated(isAnimationEnabled() ? 0 : 1);
    }

    private IFrameElement getShimElement() {
        if (shimElement == null && needsShimElement()) {
            shimElement = Document.get().createIFrameElement();

            // Insert shim iframe before the main overlay element. It does not
            // matter if it is in front or behind the shadow as we cannot put a
            // shim behind the shadow due to its transparency.
            shimElement.getStyle().setPosition(Position.ABSOLUTE);
            shimElement.getStyle().setBorderStyle(BorderStyle.NONE);
            shimElement.setTabIndex(-1);
            shimElement.setFrameBorder(0);
            shimElement.setMarginHeight(0);
        }
        return shimElement;
    }

    private int getActualTop() {
        int y = getAbsoluteTop();

        /* This is needed for IE7 at least */
        // Account for the difference between absolute position and the
        // body's positioning context.
        y -= Document.get().getBodyOffsetTop();
        y -= adjustByRelativeTopBodyMargin();

        return y;
    }

    private int getActualLeft() {
        int x = getAbsoluteLeft();

        /* This is needed for IE7 at least */
        // Account for the difference between absolute position and the
        // body's positioning context.
        x -= Document.get().getBodyOffsetLeft();
        x -= adjustByRelativeLeftBodyMargin();

        return x;
    }

    private static int adjustByRelativeTopBodyMargin() {
        if (topFix == -1) {
            boolean ie6OrIe7 = BrowserInfo.get().isIE()
                    && BrowserInfo.get().getIEVersion() <= 7;
            topFix = detectRelativeBodyFixes("top", ie6OrIe7);
        }
        return topFix;
    }

    private native static int detectRelativeBodyFixes(String axis,
            boolean removeClientLeftOrTop)
    /*-{
        try {
            var b = $wnd.document.body;
            var cstyle = b.currentStyle ? b.currentStyle : getComputedStyle(b);
            if(cstyle && cstyle.position == 'relative') {
                var offset = b.getBoundingClientRect()[axis];
                if (removeClientLeftOrTop) {
                    // IE6 and IE7 include the top left border of the client area into the boundingClientRect
                    var clientTopOrLeft = 0;
                    if (axis == "top")
                        clientTopOrLeft = $wnd.document.documentElement.clientTop;
                    else
                        clientTopOrLeft = $wnd.document.documentElement.clientLeft;

                    offset -= clientTopOrLeft;
                }
                return offset;
            }
        } catch(e){}
        return 0;
    }-*/;

    private static int adjustByRelativeLeftBodyMargin() {
        if (leftFix == -1) {
            boolean ie6OrIe7 = BrowserInfo.get().isIE()
                    && BrowserInfo.get().getIEVersion() <= 7;
            leftFix = detectRelativeBodyFixes("left", ie6OrIe7);

        }
        return leftFix;
    }

    @Override
    public void show() {
        super.show();
        if (isAnimationEnabled()) {
            new ResizeAnimation().run(POPUP_PANEL_ANIMATION_DURATION);
        } else {
            positionOrSizeUpdated(1.0);
        }
        Util.runIE7ZeroSizedBodyFix();
    }

    @Override
    public void hide(boolean autoClosed) {
        super.hide(autoClosed);
        Util.runIE7ZeroSizedBodyFix();
    }

    @Override
    protected void onDetach() {
        super.onDetach();

        // Always ensure shadow is removed when the overlay is removed.
        removeShadowIfPresent();
        removeShimElement();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (isShadowEnabled()) {
            shadow.getStyle().setProperty("visibility",
                    visible ? "visible" : "hidden");
        }
    }

    @Override
    public void setWidth(String width) {
        super.setWidth(width);
        positionOrSizeUpdated(1.0);
    }

    @Override
    public void setHeight(String height) {
        super.setHeight(height);
        positionOrSizeUpdated(1.0);
    }

    /**
     * Sets the shadow style for this overlay. Will override any previous style
     * for the shadow. The default style name is defined by CLASSNAME_SHADOW.
     * The given style will be prefixed with CLASSNAME_SHADOW.
     * 
     * @param style
     *            The new style name for the shadow element. Will be prefixed by
     *            CLASSNAME_SHADOW, e.g. style=='foobar' -> actual style
     *            name=='v-shadow-foobar'.
     */
    protected void setShadowStyle(String style) {
        if (isShadowEnabled()) {
            shadow.setClassName(CLASSNAME_SHADOW + "-" + style);
        }
    }

    /**
     * Extending classes should always call this method after they change the
     * size of overlay without using normal 'setWidth(String)' and
     * 'setHeight(String)' methods (if not calling super.setWidth/Height).
     * 
     * @deprecated Call {@link #positionOrSizeUpdated()} instead.
     */
    @Deprecated
    protected void updateShadowSizeAndPosition() {
        positionOrSizeUpdated();
    }

    protected void positionOrSizeUpdated() {
        positionOrSizeUpdated(1.0);
    }

    /**
     * Recalculates proper position and dimensions for the shadow and shim
     * elements. Can be used to animate the related elements, using the
     * 'progress' parameter (used to animate the shadow in sync with GWT
     * PopupPanel's default animation 'PopupPanel.AnimationType.CENTER').
     * 
     * @param progress
     *            A value between 0.0 and 1.0, indicating the progress of the
     *            animation (0=start, 1=end).
     */
    private void positionOrSizeUpdated(final double progress) {
        // Don't do anything if overlay element is not attached
        if (!isAttached()) {
            return;
        }
        // Calculate proper z-index
        String zIndex = null;
        try {
            // Odd behaviour with Windows Hosted Mode forces us to use
            // this redundant try/catch block (See dev.vaadin.com #2011)
            zIndex = DOM.getStyleAttribute(getElement(), "zIndex");
        } catch (Exception ignore) {
            // Ignored, will cause no harm
            zIndex = "1000";
        }
        if (zIndex == null) {
            zIndex = "" + Z_INDEX;
        }
        // Calculate position and size
        if (BrowserInfo.get().isIE()) {
            // Shake IE
            getOffsetHeight();
            getOffsetWidth();
        }

        PositionAndSize positionAndSize = new PositionAndSize(getActualLeft(),
                getActualTop(), getOffsetWidth(), getOffsetHeight());

        // Animate the size
        positionAndSize.setAnimationFromCenterProgress(progress);

        // Opera needs some shaking to get parts of the shadow showing
        // properly
        // (ticket #2704)
        if (BrowserInfo.get().isOpera() && isShadowEnabled()) {
            // Clear the height of all middle elements
            DOM.getChild(shadow, 3).getStyle().setProperty("height", "auto");
            DOM.getChild(shadow, 4).getStyle().setProperty("height", "auto");
            DOM.getChild(shadow, 5).getStyle().setProperty("height", "auto");
        }

        // Update correct values
        if (isShadowEnabled()) {
            updatePositionAndSize(shadow, positionAndSize);
            DOM.setStyleAttribute(shadow, "zIndex", zIndex);
            DOM.setStyleAttribute(shadow, "display", progress < 0.9 ? "none"
                    : "");
        }
        if (needsShimElement()) {
            updatePositionAndSize((Element) Element.as(getShimElement()),
                    positionAndSize);
        }

        // Opera fix, part 2 (ticket #2704)
        if (BrowserInfo.get().isOpera() && isShadowEnabled()) {
            // We'll fix the height of all the middle elements
            DOM.getChild(shadow, 3)
                    .getStyle()
                    .setPropertyPx("height",
                            DOM.getChild(shadow, 3).getOffsetHeight());
            DOM.getChild(shadow, 4)
                    .getStyle()
                    .setPropertyPx("height",
                            DOM.getChild(shadow, 4).getOffsetHeight());
            DOM.getChild(shadow, 5)
                    .getStyle()
                    .setPropertyPx("height",
                            DOM.getChild(shadow, 5).getOffsetHeight());
        }

        // Attach to dom if not there already
        if (isShadowEnabled() && !isShadowAttached()) {
            RootPanel.get().getElement().insertBefore(shadow, getElement());
            sinkShadowEvents();
        }
        if (needsShimElement() && !isShimElementAttached()) {
            RootPanel.get().getElement()
                    .insertBefore(getShimElement(), getElement());
        }

    }

    /**
     * Returns true if we should add a shim iframe below the overlay to deal
     * with zindex issues with PDFs and applets. Can be overriden to disable
     * shim iframes if they are not needed.
     * 
     * @return true if a shim iframe should be added, false otherwise
     */
    protected boolean needsShimElement() {
        BrowserInfo info = BrowserInfo.get();
        return info.isIE() && info.isBrowserVersionNewerOrEqual(8, 0);
    }

    private void updatePositionAndSize(Element e,
            PositionAndSize positionAndSize) {
        e.getStyle().setLeft(positionAndSize.getLeft(), Unit.PX);
        e.getStyle().setTop(positionAndSize.getTop(), Unit.PX);
        e.getStyle().setWidth(positionAndSize.getWidth(), Unit.PX);
        e.getStyle().setHeight(positionAndSize.getHeight(), Unit.PX);
    }

    protected class ResizeAnimation extends Animation {
        @Override
        protected void onUpdate(double progress) {
            positionOrSizeUpdated(progress);
        }
    }

    public void onClose(CloseEvent<PopupPanel> event) {
        removeShadowIfPresent();
    }

    @Override
    public void sinkEvents(int eventBitsToAdd) {
        super.sinkEvents(eventBitsToAdd);
        // Also sink events on the shadow if present
        sinkShadowEvents();
    }

    private void sinkShadowEvents() {
        if (isSinkShadowEvents() && isShadowAttached()) {
            // Sink the same events as the actual overlay has sunk
            DOM.sinkEvents(shadow, DOM.getEventsSunk(getElement()));
            // Send events to VOverlay.onBrowserEvent
            DOM.setEventListener(shadow, this);
        }
    }

    private void unsinkShadowEvents() {
        if (isShadowAttached()) {
            DOM.setEventListener(shadow, null);
            DOM.sinkEvents(shadow, 0);
        }
    }

    /**
     * Enables or disables sinking the events of the shadow to the same
     * onBrowserEvent as events to the actual overlay goes.
     * 
     * Please note, that if you enable this, you can't assume that e.g.
     * event.getEventTarget returns an element inside the DOM structure of the
     * overlay
     * 
     * @param sinkShadowEvents
     */
    protected void setSinkShadowEvents(boolean sinkShadowEvents) {
        this.sinkShadowEvents = sinkShadowEvents;
        if (sinkShadowEvents) {
            sinkShadowEvents();
        } else {
            unsinkShadowEvents();
        }
    }

    protected boolean isSinkShadowEvents() {
        return sinkShadowEvents;
    }
}
