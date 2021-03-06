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
package com.vaadin.terminal.gwt.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.ClientDataRequest;
import javax.portlet.MimeResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceURL;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequestWrapper;

import com.vaadin.Application;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.Paintable;
import com.vaadin.terminal.StreamVariable;
import com.vaadin.terminal.VariableOwner;
import com.vaadin.ui.Component;
import com.vaadin.ui.Window;

/**
 * TODO document me!
 * 
 * @author peholmst
 * 
 */
@SuppressWarnings("serial")
public class PortletCommunicationManager extends AbstractCommunicationManager {

    private transient ResourceResponse currentUidlResponse;

    private static class PortletRequestWrapper implements Request {

        private final PortletRequest request;

        public PortletRequestWrapper(PortletRequest request) {
            this.request = request;
        }

        public Object getAttribute(String name) {
            return request.getAttribute(name);
        }

        public int getContentLength() {
            return ((ClientDataRequest) request).getContentLength();
        }

        public InputStream getInputStream() throws IOException {
            return ((ClientDataRequest) request).getPortletInputStream();
        }

        public String getParameter(String name) {
            String value = request.getParameter(name);
            if (value == null) {
                // for GateIn portlet container simple-portal
                try {
                    Method getRealReq = request.getClass().getMethod(
                            "getRealRequest");
                    HttpServletRequestWrapper origRequest = (HttpServletRequestWrapper) getRealReq
                            .invoke(request);
                    value = origRequest.getParameter(name);
                } catch (Exception e) {
                    // do nothing - not on GateIn simple-portal
                }
            }
            return value;
        }

        public String getRequestID() {
            return "WindowID:" + request.getWindowID();
        }

        public Session getSession() {
            return new PortletSessionWrapper(request.getPortletSession());
        }

        public Object getWrappedRequest() {
            return request;
        }

        public boolean isRunningInPortlet() {
            return true;
        }

        public void setAttribute(String name, Object o) {
            request.setAttribute(name, o);
        }

    }

    private static class PortletResponseWrapper implements Response {

        private final PortletResponse response;

        public PortletResponseWrapper(PortletResponse response) {
            this.response = response;
        }

        public OutputStream getOutputStream() throws IOException {
            return ((MimeResponse) response).getPortletOutputStream();
        }

        public Object getWrappedResponse() {
            return response;
        }

        public void setContentType(String type) {
            ((MimeResponse) response).setContentType(type);
        }
    }

    private static class PortletSessionWrapper implements Session {

        private final PortletSession session;

        public PortletSessionWrapper(PortletSession session) {
            this.session = session;
        }

        public Object getAttribute(String name) {
            return session.getAttribute(name);
        }

        public int getMaxInactiveInterval() {
            return session.getMaxInactiveInterval();
        }

        public Object getWrappedSession() {
            return session;
        }

        public boolean isNew() {
            return session.isNew();
        }

        public void setAttribute(String name, Object o) {
            session.setAttribute(name, o);
        }

    }

    private static class AbstractApplicationPortletWrapper implements Callback {

        private final AbstractApplicationPortlet portlet;

        public AbstractApplicationPortletWrapper(
                AbstractApplicationPortlet portlet) {
            this.portlet = portlet;
        }

        public void criticalNotification(Request request, Response response,
                String cap, String msg, String details, String outOfSyncURL)
                throws IOException {
            portlet.criticalNotification(
                    (PortletRequest) request.getWrappedRequest(),
                    (MimeResponse) response.getWrappedResponse(), cap, msg,
                    details, outOfSyncURL);
        }

        public String getRequestPathInfo(Request request) {
            if (request.getWrappedRequest() instanceof ResourceRequest) {
                return ((ResourceRequest) request.getWrappedRequest())
                        .getResourceID();
            } else {
                // We do not use paths in portlet mode
                throw new UnsupportedOperationException(
                        "PathInfo only available when using ResourceRequests");
            }
        }

        public InputStream getThemeResourceAsStream(String themeName,
                String resource) throws IOException {
            return portlet.getPortletContext().getResourceAsStream(
                    "/" + AbstractApplicationPortlet.THEME_DIRECTORY_PATH
                            + themeName + "/" + resource);
        }

    }

    public PortletCommunicationManager(Application application) {
        super(application);
    }

    public void handleFileUpload(ResourceRequest request,
            ResourceResponse response) throws IOException {
        String contentType = request.getContentType();
        String name = request.getParameter("name");
        String ownerId = request.getParameter("rec-owner");
        VariableOwner variableOwner = getVariableOwner(ownerId);
        StreamVariable streamVariable = ownerToNameToStreamVariable.get(
                variableOwner).get(name);

        if (contentType.contains("boundary")) {
            doHandleSimpleMultipartFileUpload(
                    new PortletRequestWrapper(request),
                    new PortletResponseWrapper(response), streamVariable, name,
                    variableOwner, contentType.split("boundary=")[1]);
        } else {
            doHandleXhrFilePost(new PortletRequestWrapper(request),
                    new PortletResponseWrapper(response), streamVariable, name,
                    variableOwner, request.getContentLength());
        }

    }

    @Override
    protected void unregisterPaintable(Component p) {
        super.unregisterPaintable(p);
        if (ownerToNameToStreamVariable != null) {
            ownerToNameToStreamVariable.remove(p);
        }
    }

    public void handleUidlRequest(ResourceRequest request,
            ResourceResponse response,
            AbstractApplicationPortlet applicationPortlet, Window window)
            throws InvalidUIDLSecurityKeyException, IOException {

        // Ensure that the browser does not cache UIDL responses.
        // iOS 6 Safari requires this (#9732)
        response.setProperty("Cache-Control", "no-cache");

        currentUidlResponse = response;
        doHandleUidlRequest(new PortletRequestWrapper(request),
                new PortletResponseWrapper(response),
                new AbstractApplicationPortletWrapper(applicationPortlet),
                window);
        currentUidlResponse = null;
    }

    DownloadStream handleURI(Window window, ResourceRequest request,
            ResourceResponse response,
            AbstractApplicationPortlet applicationPortlet) {
        return handleURI(window, new PortletRequestWrapper(request),
                new PortletResponseWrapper(response),
                new AbstractApplicationPortletWrapper(applicationPortlet));
    }

    /**
     * Gets the existing application or creates a new one. Get a window within
     * an application based on the requested URI.
     * 
     * @param request
     *            the portlet Request.
     * @param applicationPortlet
     * @param application
     *            the Application to query for window.
     * @param assumedWindow
     *            if the window has been already resolved once, this parameter
     *            must contain the window.
     * @return Window matching the given URI or null if not found.
     * @throws ServletException
     *             if an exception has occurred that interferes with the
     *             servlet's normal operation.
     */
    Window getApplicationWindow(PortletRequest request,
            AbstractApplicationPortlet applicationPortlet,
            Application application, Window assumedWindow) {

        return doGetApplicationWindow(new PortletRequestWrapper(request),
                new AbstractApplicationPortletWrapper(applicationPortlet),
                application, assumedWindow);
    }

    private Map<VariableOwner, Map<String, StreamVariable>> ownerToNameToStreamVariable;

    @Override
    String getStreamVariableTargetUrl(VariableOwner owner, String name,
            StreamVariable value) {
        if (ownerToNameToStreamVariable == null) {
            ownerToNameToStreamVariable = new HashMap<VariableOwner, Map<String, StreamVariable>>();
        }
        Map<String, StreamVariable> nameToReceiver = ownerToNameToStreamVariable
                .get(owner);
        if (nameToReceiver == null) {
            nameToReceiver = new HashMap<String, StreamVariable>();
            ownerToNameToStreamVariable.put(owner, nameToReceiver);
        }
        nameToReceiver.put(name, value);
        ResourceURL resurl = currentUidlResponse.createResourceURL();
        resurl.setResourceID("UPLOAD");
        resurl.setParameter("name", name);
        resurl.setParameter("rec-owner", getPaintableId((Paintable) owner));
        resurl.setProperty("name", name);
        resurl.setProperty("rec-owner", getPaintableId((Paintable) owner));
        return resurl.toString();
    }

    @Override
    protected void cleanStreamVariable(VariableOwner owner, String name) {
        Map<String, StreamVariable> map = ownerToNameToStreamVariable
                .get(owner);
        map.remove(name);
        if (map.isEmpty()) {
            ownerToNameToStreamVariable.remove(owner);
        }
    }

}
