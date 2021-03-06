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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.BodyElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.DomEvent.Type;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.BrowserInfo;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.Focusable;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.Util;
import com.vaadin.terminal.gwt.client.VConsole;
import com.vaadin.terminal.gwt.client.ui.ShortcutActionHandler.ShortcutActionHandlerOwner;

/**
 *
 */
public class VView extends SimplePanel implements Container, ResizeHandler,
        Window.ClosingHandler, ShortcutActionHandlerOwner, Focusable {

    public static final String BROWSER_HEIGHT_VAR = "browserHeight";

    public static final String BROWSER_WIDTH_VAR = "browserWidth";

    private static final String CLASSNAME = "v-view";

    public static final String NOTIFICATION_HTML_CONTENT_NOT_ALLOWED = "useplain";

    private static int MONITOR_PARENT_TIMER_INTERVAL = 1000;

    private String theme;

    private Paintable layout;

    private final LinkedHashSet<VWindow> subWindows = new LinkedHashSet<VWindow>();

    private String id;

    private ShortcutActionHandler actionHandler;

    /*
     * Last known window size used to detect whether VView should be layouted
     * again. Detection must check window size, because the VView size might be
     * fixed and thus not automatically adapt to changed window sizes.
     */
    private int windowWidth;
    private int windowHeight;

    private ApplicationConnection connection;

    /**
     * Keep track of possible parent size changes when an embedded application.
     * 
     * Uses {@link #parentWidth} and {@link #parentHeight} as an optimization to
     * keep track of when there is a real change.
     */
    private Timer resizeTimer;

    /** stored width of parent for embedded application auto-resize */
    private int parentWidth;

    /** stored height of parent for embedded application auto-resize */
    private int parentHeight;

    private int scrollTop;

    private int scrollLeft;

    private boolean rendering;

    private boolean scrollable;

    private boolean immediate;

    private boolean resizeLazy = false;

    public static final String RESIZE_LAZY = "rL";
    /**
     * Reference to the parent frame/iframe. Null if there is no parent (i)frame
     * or if the application and parent frame are in different domains.
     */
    private Element parentFrame;

    private ClickEventHandler clickEventHandler = new ClickEventHandler(this,
            VPanel.CLICK_EVENT_IDENTIFIER) {

        @Override
        protected <H extends EventHandler> HandlerRegistration registerHandler(
                H handler, Type<H> type) {
            return addDomHandler(handler, type);
        }
    };

    private VLazyExecutor delayedResizeExecutor = new VLazyExecutor(200,
            new ScheduledCommand() {
                public void execute() {
                    performSizeCheck();
                }

            });

    public VView() {
        super();
        setStyleName(CLASSNAME);

        // Allow focusing the view by using the focus() method, the view
        // should not be in the document focus flow
        getElement().setTabIndex(-1);
        TouchScrollDelegate.enableTouchScrolling(this, getElement());
    }

    /**
     * Start to periodically monitor for parent element resizes if embedded
     * application (e.g. portlet).
     */
    @Override
    protected void onLoad() {
        super.onLoad();
        if (isMonitoringParentSize()) {
            resizeTimer = new Timer() {
                @Override
                public void run() {
                    // trigger check to see if parent size has changed,
                    // recalculate layouts
                    performSizeCheck();
                    resizeTimer.schedule(MONITOR_PARENT_TIMER_INTERVAL);
                }
            };
            resizeTimer.schedule(MONITOR_PARENT_TIMER_INTERVAL);
        }
    }

    /**
     * Stop monitoring for parent element resizes.
     */
    @Override
    protected void onUnload() {
        if (resizeTimer != null) {
            resizeTimer.cancel();
            resizeTimer = null;
        }
        super.onUnload();
    }

    /**
     * Called when the window or parent div might have been resized.
     * 
     * This immediately checks the sizes of the window and the parent div (if
     * monitoring it) and triggers layout recalculation if they have changed.
     */
    protected void performSizeCheck() {
        windowSizeMaybeChanged(Window.getClientWidth(),
                Window.getClientHeight());
    }

    /**
     * Called when the window or parent div might have been resized.
     * 
     * This immediately checks the sizes of the window and the parent div (if
     * monitoring it) and triggers layout recalculation if they have changed.
     * 
     * @param newWindowWidth
     *            The new width of the window
     * @param newWindowHeight
     *            The new height of the window
     * 
     * @deprecated use {@link #performSizeCheck()}
     */
    @Deprecated
    protected void windowSizeMaybeChanged(int newWindowWidth,
            int newWindowHeight) {
        boolean changed = false;
        if (windowWidth != newWindowWidth) {
            windowWidth = newWindowWidth;
            changed = true;
            VConsole.log("New window width: " + windowWidth);
        }
        if (windowHeight != newWindowHeight) {
            windowHeight = newWindowHeight;
            changed = true;
            VConsole.log("New window height: " + windowHeight);
        }
        Element parentElement = getElement().getParentElement();
        if (isMonitoringParentSize() && parentElement != null) {
            // check also for parent size changes
            int newParentWidth = parentElement.getClientWidth();
            int newParentHeight = parentElement.getClientHeight();
            if (parentWidth != newParentWidth) {
                parentWidth = newParentWidth;
                changed = true;
                VConsole.log("New parent width: " + parentWidth);
            }
            if (parentHeight != newParentHeight) {
                parentHeight = newParentHeight;
                changed = true;
                VConsole.log("New parent height: " + parentHeight);
            }
        }
        if (changed) {
            /*
             * If the window size has changed, layout the VView again and send
             * new size to the server if the size changed. (Just checking VView
             * size would cause us to ignore cases when a relatively sized VView
             * should shrink as the content's size is fixed and would thus not
             * automatically shrink.)
             */
            VConsole.log("Running layout functions due to window or parent resize");

            connection.runDescendentsLayout(VView.this);
            Util.runWebkitOverflowAutoFix(getElement());

            // update size to avoid (most) redundant re-layout passes
            // there can still be an extra layout recalculation if webkit
            // overflow fix updates the size in a deferred block
            if (isMonitoringParentSize() && parentElement != null) {
                parentWidth = parentElement.getClientWidth();
                parentHeight = parentElement.getClientHeight();
            }

            sendClientResized();
        }
    }

    public String getTheme() {
        return theme;
    }

    /**
     * Used to reload host page on theme changes.
     */
    private static native void reloadHostPage()
    /*-{
         $wnd.location.reload();
     }-*/;

    /**
     * Evaluate the given script in the browser document.
     * 
     * @param script
     *            Script to be executed.
     */
    private static native void eval(String script)
    /*-{
      try {
         if (script == null) return;
         $wnd.eval(script);
      } catch (e) {
      }
    }-*/;

    /**
     * Returns true if the body is NOT generated, i.e if someone else has made
     * the page that we're running in. Otherwise we're in charge of the whole
     * page.
     * 
     * @return true if we're running embedded
     */
    public boolean isEmbedded() {
        return !getElement().getOwnerDocument().getBody().getClassName()
                .contains(ApplicationConnection.GENERATED_BODY_CLASSNAME);
    }

    /**
     * Returns true if the size of the parent should be checked periodically and
     * the application should react to its changes.
     * 
     * @return true if size of parent should be tracked
     */
    protected boolean isMonitoringParentSize() {
        // could also perform a more specific check (Liferay portlet)
        return isEmbedded();
    }

    private native void open(String url, String name)
    /*-{
        $wnd.open(url, name);
     }-*/;

    public void updateFromUIDL(final UIDL uidl, ApplicationConnection client) {
        rendering = true;

        id = uidl.getId();
        boolean firstPaint = connection == null;
        connection = client;

        immediate = uidl.hasAttribute("immediate");
        resizeLazy = uidl.hasAttribute(RESIZE_LAZY);
        String newTheme = uidl.getStringAttribute("theme");
        if (theme != null && !newTheme.equals(theme)) {
            // Complete page refresh is needed due css can affect layout
            // calculations etc
            reloadHostPage();
        } else {
            theme = newTheme;
        }
        if (uidl.hasAttribute("style")) {
            setStyleName(getStylePrimaryName() + " "
                    + uidl.getStringAttribute("style"));
        }

        if (uidl.hasAttribute("name")) {
            client.setWindowName(uidl.getStringAttribute("name"));
        }

        clickEventHandler.handleEventHandlerRegistration(client);

        if (!isEmbedded()) {
            // only change window title if we're in charge of the whole page
            com.google.gwt.user.client.Window.setTitle(uidl
                    .getStringAttribute("caption"));
        }

        // Process children
        int childIndex = 0;

        // Open URL:s
        boolean isClosed = false; // was this window closed?
        while (childIndex < uidl.getChildCount()
                && "open".equals(uidl.getChildUIDL(childIndex).getTag())) {
            final UIDL open = uidl.getChildUIDL(childIndex);
            final String url = client.translateVaadinUri(open
                    .getStringAttribute("src"));
            final String target = open.getStringAttribute("name");
            if (target == null) {
                // source will be opened to this browser window, but we may have
                // to finish rendering this window in case this is a download
                // (and window stays open).
                Scheduler.get().scheduleDeferred(new Command() {
                    public void execute() {
                        goTo(url);
                    }
                });
            } else if ("_self".equals(target)) {
                // This window is closing (for sure). Only other opens are
                // relevant in this change. See #3558, #2144
                isClosed = true;
                goTo(url);
            } else {
                String options;
                boolean alwaysAsPopup = true;
                if (open.hasAttribute("popup")) {
                    alwaysAsPopup = open.getBooleanAttribute("popup");
                }
                if (alwaysAsPopup) {
                    if (open.hasAttribute("border")) {
                        if (open.getStringAttribute("border").equals("minimal")) {
                            options = "menubar=yes,location=no,status=no";
                        } else {
                            options = "menubar=no,location=no,status=no";
                        }

                    } else {
                        options = "resizable=yes,menubar=yes,toolbar=yes,directories=yes,location=yes,scrollbars=yes,status=yes";
                    }

                    if (open.hasAttribute("width")) {
                        int w = open.getIntAttribute("width");
                        options += ",width=" + w;
                    }
                    if (open.hasAttribute("height")) {
                        int h = open.getIntAttribute("height");
                        options += ",height=" + h;
                    }

                    Window.open(url, target, options);
                } else {
                    open(url, target);
                }
            }
            childIndex++;
        }
        if (isClosed) {
            // don't render the content, something else will be opened to this
            // browser view
            rendering = false;
            return;
        }

        // Draw this application level window
        UIDL childUidl = uidl.getChildUIDL(childIndex);
        final Paintable lo = client.getPaintable(childUidl);

        if (layout != null) {
            if (layout != lo) {
                // remove old
                client.unregisterPaintable(layout);
                // add new
                setWidget((Widget) lo);
                layout = lo;
            }
        } else {
            setWidget((Widget) lo);
            layout = lo;
        }

        boolean childLayoutCached = childUidl.getBooleanAttribute("cached");

        if (!childLayoutCached) {
            // Liferay #5521: update portlet-body element size
            updateLiferayPortletBodySize(childUidl);
        }

        layout.updateFromUIDL(childUidl, client);
        if (!childLayoutCached) {
            updateParentFrameSize();
        }

        // Save currently open subwindows to track which will need to be closed
        final HashSet<VWindow> removedSubWindows = new HashSet<VWindow>(
                subWindows);

        // Handle other UIDL children
        while ((childUidl = uidl.getChildUIDL(++childIndex)) != null) {
            String tag = childUidl.getTag().intern();
            if (tag == "actions") {
                if (actionHandler == null) {
                    actionHandler = new ShortcutActionHandler(id, client);
                }
                actionHandler.updateActionMap(childUidl);
            } else if (tag == "execJS") {
                String script = childUidl.getStringAttribute("script");
                eval(script);
            } else if (tag == "notifications") {
                for (final Iterator<?> it = childUidl.getChildIterator(); it
                        .hasNext();) {
                    final UIDL notification = (UIDL) it.next();
                    VNotification.showNotification(client, notification);
                }
            } else {
                // subwindows
                final Paintable w = client.getPaintable(childUidl);
                if (subWindows.contains(w)) {
                    removedSubWindows.remove(w);
                } else {
                    subWindows.add((VWindow) w);
                }
                w.updateFromUIDL(childUidl, client);
            }
        }

        // Close old windows which where not in UIDL anymore
        for (final Iterator<VWindow> rem = removedSubWindows.iterator(); rem
                .hasNext();) {
            final VWindow w = rem.next();
            client.unregisterPaintable(w);
            subWindows.remove(w);
            w.hide();
        }

        if (uidl.hasAttribute("focused")) {
            // set focused component when render phase is finished
            Scheduler.get().scheduleDeferred(new Command() {
                public void execute() {
                    final Paintable toBeFocused = uidl.getPaintableAttribute(
                            "focused", connection);

                    /*
                     * Two types of Widgets can be focused, either implementing
                     * GWT HasFocus of a thinner Vaadin specific Focusable
                     * interface.
                     */
                    if (toBeFocused instanceof com.google.gwt.user.client.ui.Focusable) {
                        final com.google.gwt.user.client.ui.Focusable toBeFocusedWidget = (com.google.gwt.user.client.ui.Focusable) toBeFocused;
                        toBeFocusedWidget.setFocus(true);
                    } else if (toBeFocused instanceof Focusable) {
                        ((Focusable) toBeFocused).focus();
                    } else {
                        VConsole.log("Could not focus component");
                    }
                }
            });
        }

        // Add window listeners on first paint, to prevent premature
        // variablechanges
        if (firstPaint) {
            Window.addWindowClosingHandler(this);
            Window.addResizeHandler(this);
        }

        triggerSizeChangeCheck();

        // finally set scroll position from UIDL
        if (uidl.hasVariable("scrollTop")) {
            scrollable = true;
            scrollTop = uidl.getIntVariable("scrollTop");
            DOM.setElementPropertyInt(getElement(), "scrollTop", scrollTop);
            scrollLeft = uidl.getIntVariable("scrollLeft");
            DOM.setElementPropertyInt(getElement(), "scrollLeft", scrollLeft);
        } else {
            scrollable = false;
        }

        // Safari workaround must be run after scrollTop is updated as it sets
        // scrollTop using a deferred command.
        if (BrowserInfo.get().isSafari()) {
            Util.runWebkitOverflowAutoFix(getElement());
        }

        scrollIntoView(uidl);

        rendering = false;
    }

    private void updateLiferayPortletBodySize(UIDL childUidl) {
        // Liferay #5521: update portlet-body element size
        Element parentOfVApp = getElement().getParentElement()
                .getParentElement();
        if (parentOfVApp != null
                && parentOfVApp.getClassName().contains("portlet-body")) {
            // portlet-body needs to get size 100% if e.g. in a freeform
            // layout or otherwise in a situation where the application
            // layout size is specified as a percentage
            for (String direction : new String[] { "height", "width" }) {
                if (childUidl.hasAttribute(direction)
                        && childUidl.getStringAttribute(direction)
                                .contains("%")) {
                    // if layout has a percentual size, set portlet body size
                    // as percentual
                    if ("".equals(parentOfVApp.getStyle()
                            .getProperty(direction))) {
                        parentOfVApp.getStyle().setProperty(direction, "100%");
                    }
                } else if (parentOfVApp.getStyle().getProperty(direction)
                        .contains("%")) {
                    // if layout size is undefined or fixed and we have set a
                    // percentual size for portlet-body, remove size setting on
                    // portlet body
                    parentOfVApp.getStyle().setProperty(direction, "");
                }
            }
        }
    }

    /**
     * Tries to scroll paintable referenced from given UIDL snippet to be
     * visible.
     * 
     * @param uidl
     */
    void scrollIntoView(final UIDL uidl) {
        if (uidl.hasAttribute("scrollTo")) {
            Scheduler.get().scheduleDeferred(new Command() {
                public void execute() {
                    final Paintable paintable = uidl.getPaintableAttribute(
                            "scrollTo", connection);
                    ((Widget) paintable).getElement().scrollIntoView();
                }
            });
        }
    }

    @Override
    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);
        int type = DOM.eventGetType(event);
        if (type == Event.ONKEYDOWN && actionHandler != null) {
            actionHandler.handleKeyboardEvent(event);
            return;
        } else if (scrollable && type == Event.ONSCROLL) {
            updateScrollPosition();
        }
    }

    /**
     * Updates scroll position from DOM and saves variables to server.
     */
    private void updateScrollPosition() {
        int oldTop = scrollTop;
        int oldLeft = scrollLeft;
        scrollTop = DOM.getElementPropertyInt(getElement(), "scrollTop");
        scrollLeft = DOM.getElementPropertyInt(getElement(), "scrollLeft");
        if (connection != null && !rendering) {
            if (oldTop != scrollTop) {
                connection.updateVariable(id, "scrollTop", scrollTop, false);
            }
            if (oldLeft != scrollLeft) {
                connection.updateVariable(id, "scrollLeft", scrollLeft, false);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.google.gwt.event.logical.shared.ResizeHandler#onResize(com.google
     * .gwt.event.logical.shared.ResizeEvent)
     */
    public void onResize(ResizeEvent event) {
        triggerSizeChangeCheck();
    }

    /**
     * Called when a resize event is received.
     * 
     * This may trigger a lazy refresh or perform the size check immediately
     * depending on the browser used and whether the server side requests
     * resizes to be lazy.
     */
    private void triggerSizeChangeCheck() {
        /*
         * IE (pre IE9 at least) will give us some false resize events due to
         * problems with scrollbars. Firefox 3 might also produce some extra
         * events. We postpone both the re-layouting and the server side event
         * for a while to deal with these issues.
         * 
         * We may also postpone these events to avoid slowness when resizing the
         * browser window. Constantly recalculating the layout causes the resize
         * operation to be really slow with complex layouts.
         */
        boolean lazy = resizeLazy
                || (BrowserInfo.get().isIE() && BrowserInfo.get()
                        .getIEVersion() <= 8) || BrowserInfo.get().isFF3();

        if (lazy) {
            delayedResizeExecutor.trigger();
        } else {
            performSizeCheck();
        }
    }

    /**
     * Send new dimensions to the server.
     */
    private void sendClientResized() {
        Element parentElement = getElement().getParentElement();
        int viewHeight = parentElement.getClientHeight();
        int viewWidth = parentElement.getClientWidth();

        connection.updateVariable(id, "height", viewHeight, false);
        connection.updateVariable(id, "width", viewWidth, false);

        int windowWidth = Window.getClientWidth();
        int windowHeight = Window.getClientHeight();

        connection.updateVariable(id, BROWSER_WIDTH_VAR, windowWidth, false);
        connection.updateVariable(id, BROWSER_HEIGHT_VAR, windowHeight,
                immediate);
    }

    public native static void goTo(String url)
    /*-{
       $wnd.location = url;
     }-*/;

    public void onWindowClosing(Window.ClosingEvent event) {
        // Change focus on this window in order to ensure that all state is
        // collected from textfields
        // TODO this is a naive hack, that only works with text fields and may
        // cause some odd issues. Should be replaced with a decent solution, see
        // also related BeforeShortcutActionListener interface. Same interface
        // might be usable here.
        VTextField.flushChangesFromFocusedTextField();

        // Send the closing state to server
        connection.updateVariable(id, "close", true, false);
        connection.sendPendingVariableChangesSync();
    }

    private final RenderSpace myRenderSpace = new RenderSpace() {
        private int excessHeight = -1;
        private int excessWidth = -1;

        @Override
        public int getHeight() {
            return getElement().getOffsetHeight() - getExcessHeight();
        }

        private int getExcessHeight() {
            if (excessHeight < 0) {
                detectExcessSize();
            }
            return excessHeight;
        }

        private void detectExcessSize() {
            // TODO define that iview cannot be themed and decorations should
            // get to parent element, then get rid of this expensive and error
            // prone function
            final String overflow = getElement().getStyle().getProperty(
                    "overflow");
            getElement().getStyle().setProperty("overflow", "hidden");
            if (BrowserInfo.get().isIE()
                    && getElement().getPropertyInt("clientWidth") == 0) {
                // can't detect possibly themed border/padding width in some
                // situations (with some layout configurations), use empty div
                // to measure width properly
                DivElement div = Document.get().createDivElement();
                div.setInnerHTML("&nbsp;");
                div.getStyle().setProperty("overflow", "hidden");
                div.getStyle().setProperty("height", "1px");
                getElement().appendChild(div);
                excessWidth = getElement().getOffsetWidth()
                        - div.getOffsetWidth();
                getElement().removeChild(div);
            } else {
                excessWidth = getElement().getOffsetWidth()
                        - getElement().getPropertyInt("clientWidth");
            }
            excessHeight = getElement().getOffsetHeight()
                    - getElement().getPropertyInt("clientHeight");

            getElement().getStyle().setProperty("overflow", overflow);
        }

        @Override
        public int getWidth() {
            int w = getRealWidth();
            if (w < 10 && BrowserInfo.get().isIE7()) {
                // Overcome an IE7 bug #3295
                Util.shakeBodyElement();
                w = getRealWidth();
            }
            return w;
        }

        private int getRealWidth() {
            if (connection.getConfiguration().isStandalone()) {
                return getElement().getOffsetWidth() - getExcessWidth();
            }

            // If not running standalone, there might be multiple Vaadin apps
            // that won't shrink with the browser window as the components have
            // calculated widths (#3125)

            // Find all Vaadin applications on the page
            ArrayList<String> vaadinApps = new ArrayList<String>();
            loadAppIdListFromDOM(vaadinApps);

            // Store original styles here so they can be restored
            ArrayList<String> originalDisplays = new ArrayList<String>(
                    vaadinApps.size());

            String ownAppId = connection.getConfiguration().getRootPanelId();

            // Hiding elements causes browser to forget scroll position -> must
            // save values and restore when the elements are visible again #7976
            int originalScrollTop = Window.getScrollTop();
            int originalScrollLeft = Window.getScrollLeft();

            /*
             * As described in #12336, removing applications will decrease the
             * BODY height in such a way that you might temporarily lose the
             * scrollbar. As we hide the applications, we need to conserve the
             * original height of the applications, so that this can be taken
             * into account.
             */
            int heightOfApplications = 0;

            // Set display: none for all Vaadin apps
            for (int i = 0; i < vaadinApps.size(); i++) {
                String appId = vaadinApps.get(i);
                Element targetElement;
                if (appId.equals(ownAppId)) {
                    // Only hide the contents of current application
                    targetElement = ((Widget) layout).getElement();
                } else {
                    // Hide everything for other applications
                    targetElement = Document.get().getElementById(appId);
                }
                heightOfApplications += targetElement.getOffsetHeight();

                Style layoutStyle = targetElement.getStyle();

                originalDisplays.add(i, layoutStyle.getDisplay());
                layoutStyle.setDisplay(Display.NONE);
            }

            /*
             * before measuring the width, let's make sure the body height is
             * still the same (to account for any possible scrollbars).
             */
            com.google.gwt.user.client.Element expander = DOM
                    .createElement("div");
            expander.getStyle().setHeight(heightOfApplications, Unit.PX);
            BodyElement body = Document.get().getBody();
            body.appendChild(expander);

            int w = getElement().getOffsetWidth() - getExcessWidth();

            expander.removeFromParent();

            // Then restore the old display style before returning
            for (int i = 0; i < vaadinApps.size(); i++) {
                String appId = vaadinApps.get(i);
                Element targetElement;
                if (appId.equals(ownAppId)) {
                    targetElement = ((Widget) layout).getElement();
                } else {
                    targetElement = Document.get().getElementById(appId);
                }
                Style layoutStyle = targetElement.getStyle();
                String originalDisplay = originalDisplays.get(i);

                if (originalDisplay.length() == 0) {
                    layoutStyle.clearDisplay();
                } else {
                    layoutStyle.setProperty("display", originalDisplay);
                }
            }

            // Scroll back to original location
            Window.scrollTo(originalScrollLeft, originalScrollTop);

            return w;
        }

        private int getExcessWidth() {
            if (excessWidth < 0) {
                detectExcessSize();
            }
            return excessWidth;
        }

        @Override
        public int getScrollbarSize() {
            return Util.getNativeScrollbarSize();
        }
    };

    private native static void loadAppIdListFromDOM(ArrayList<String> list)
    /*-{
         var j;
         for(j in $wnd.vaadin.vaadinConfigurations) {
            // $entry not needed as function is not exported
            list.@java.util.Collection::add(Ljava/lang/Object;)(j);
         }
     }-*/;

    public RenderSpace getAllocatedSpace(Widget child) {
        return myRenderSpace;
    }

    public boolean hasChildComponent(Widget component) {
        return (component != null && component == layout);
    }

    public void replaceChildComponent(Widget oldComponent, Widget newComponent) {
        // TODO This is untested as no layouts require this
        if (oldComponent != layout) {
            return;
        }

        setWidget(newComponent);
        layout = (Paintable) newComponent;
    }

    public boolean requestLayout(Set<Paintable> child) {
        /*
         * Can never propagate further and we do not want need to re-layout the
         * layout which has caused this request.
         */
        updateParentFrameSize();

        // layout size change may affect its available space (scrollbars)
        connection.handleComponentRelativeSize((Widget) layout);

        return true;

    }

    private void updateParentFrameSize() {
        if (parentFrame == null) {
            return;
        }

        int childHeight = Util.getRequiredHeight(getWidget().getElement());
        int childWidth = Util.getRequiredWidth(getWidget().getElement());

        parentFrame.getStyle().setPropertyPx("width", childWidth);
        parentFrame.getStyle().setPropertyPx("height", childHeight);
    }

    private static native Element getParentFrame()
    /*-{
        try {
            var frameElement = $wnd.frameElement;
            if (frameElement == null) {
                return null;
            }
            if (frameElement.getAttribute("autoResize") == "true") {
                return frameElement;
            }
        } catch (e) {
        }
        return null;
    }-*/;

    public void updateCaption(Paintable component, UIDL uidl) {
        // NOP Subwindows never draw caption for their first child (layout)
    }

    /**
     * Return an iterator for current subwindows. This method is meant for
     * testing purposes only.
     * 
     * @return
     */
    public ArrayList<VWindow> getSubWindowList() {
        ArrayList<VWindow> windows = new ArrayList<VWindow>(subWindows.size());
        for (VWindow widget : subWindows) {
            windows.add(widget);
        }
        return windows;
    }

    public void init(String rootPanelId,
            ApplicationConnection applicationConnection) {
        DOM.sinkEvents(getElement(), Event.ONKEYDOWN | Event.ONSCROLL);

        // iview is focused when created so element needs tabIndex
        // 1 due 0 is at the end of natural tabbing order
        DOM.setElementProperty(getElement(), "tabIndex", "1");

        RootPanel root = RootPanel.get(rootPanelId);

        // Remove the v-app-loading or any splash screen added inside the div by
        // the user
        root.getElement().setInnerHTML("");
        // For backwards compatibility with static index pages only.
        // No longer added by AbstractApplicationServlet/Portlet
        root.removeStyleName("v-app-loading");

        root.add(this);

        if (applicationConnection.getConfiguration().isStandalone()) {
            // set focus to iview element by default to listen possible keyboard
            // shortcuts. For embedded applications this is unacceptable as we
            // don't want to steal focus from the main page nor we don't want
            // side-effects from focusing (scrollIntoView).
            getElement().focus();
        }

        parentFrame = getParentFrame();
    }

    public ShortcutActionHandler getShortcutActionHandler() {
        return actionHandler;
    }

    public void focus() {
        getElement().focus();
    }

}
