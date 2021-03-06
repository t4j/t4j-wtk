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

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.text.CharacterIterator;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.vaadin.Application;
import com.vaadin.Application.SystemMessages;
import com.vaadin.terminal.ApplicationResource;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.Paintable;
import com.vaadin.terminal.Paintable.RepaintRequestEvent;
import com.vaadin.terminal.StreamVariable;
import com.vaadin.terminal.StreamVariable.StreamingEndEvent;
import com.vaadin.terminal.StreamVariable.StreamingErrorEvent;
import com.vaadin.terminal.Terminal.ErrorEvent;
import com.vaadin.terminal.Terminal.ErrorListener;
import com.vaadin.terminal.URIHandler;
import com.vaadin.terminal.VariableOwner;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.server.ComponentSizeValidator.InvalidLayout;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Component;
import com.vaadin.ui.Window;

/**
 * This is a common base class for the server-side implementations of the
 * communication system between the client code (compiled with GWT into
 * JavaScript) and the server side components. Its client side counterpart is
 * {@link ApplicationConnection}.
 * 
 * A server side component sends its state to the client in a paint request (see
 * {@link Paintable} and {@link PaintTarget} on the server side). The client
 * widget receives these paint requests as calls to
 * {@link com.vaadin.terminal.gwt.client.Paintable#updateFromUIDL()}. The client
 * component communicates back to the server by sending a list of variable
 * changes (see {@link ApplicationConnection#updateVariable()} and
 * {@link VariableOwner#changeVariables(Object, Map)}).
 * 
 * TODO Document better!
 */
@SuppressWarnings("serial")
public abstract class AbstractCommunicationManager implements
        Paintable.RepaintRequestListener, Serializable {

    private static final String DASHDASH = "--";

    /**
     * Generic interface of a (HTTP or Portlet) request to the application.
     * 
     * This is a wrapper interface that allows
     * {@link AbstractCommunicationManager} to use a unified API.
     * 
     * @see javax.servlet.ServletRequest
     * @see javax.portlet.PortletRequest
     * 
     * @author peholmst
     */
    public interface Request {

        /**
         * Gets a {@link Session} wrapper implementation representing the
         * session for which this request was sent.
         * 
         * Multiple Vaadin applications can be associated with a single session.
         * 
         * @return Session
         */
        public Session getSession();

        /**
         * Are the applications in this session running in a portlet or directly
         * as servlets.
         * 
         * @return true if in a portlet
         */
        public boolean isRunningInPortlet();

        /**
         * Get the named HTTP or portlet request parameter.
         * 
         * @see javax.servlet.ServletRequest#getParameter(String)
         * @see javax.portlet.PortletRequest#getParameter(String)
         * 
         * @param name
         * @return
         */
        public String getParameter(String name);

        /**
         * Returns the length of the request content that can be read from the
         * input stream returned by {@link #getInputStream()}.
         * 
         * @return content length in bytes
         */
        public int getContentLength();

        /**
         * Returns an input stream from which the request content can be read.
         * The request content length can be obtained with
         * {@link #getContentLength()} without reading the full stream contents.
         * 
         * @return
         * @throws IOException
         */
        public InputStream getInputStream() throws IOException;

        /**
         * Returns the request identifier that identifies the target Vaadin
         * window for the request.
         * 
         * @return String identifier for the request target window
         */
        public String getRequestID();

        /**
         * @see javax.servlet.ServletRequest#getAttribute(String)
         * @see javax.portlet.PortletRequest#getAttribute(String)
         */
        public Object getAttribute(String name);

        /**
         * @see javax.servlet.ServletRequest#setAttribute(String, Object)
         * @see javax.portlet.PortletRequest#setAttribute(String, Object)
         */
        public void setAttribute(String name, Object value);

        /**
         * Gets the underlying request object. The request is typically either a
         * {@link ServletRequest} or a {@link PortletRequest}.
         * 
         * @return wrapped request object
         */
        public Object getWrappedRequest();

    }

    /**
     * Generic interface of a (HTTP or Portlet) response from the application.
     * 
     * This is a wrapper interface that allows
     * {@link AbstractCommunicationManager} to use a unified API.
     * 
     * @see javax.servlet.ServletResponse
     * @see javax.portlet.PortletResponse
     * 
     * @author peholmst
     */
    public interface Response {

        /**
         * Gets the output stream to which the response can be written.
         * 
         * @return
         * @throws IOException
         */
        public OutputStream getOutputStream() throws IOException;

        /**
         * Sets the MIME content type for the response to be communicated to the
         * browser.
         * 
         * @param type
         */
        public void setContentType(String type);

        /**
         * Gets the wrapped response object, usually a class implementing either
         * {@link ServletResponse} or {@link PortletResponse}.
         * 
         * @return wrapped request object
         */
        public Object getWrappedResponse();

    }

    /**
     * Generic wrapper interface for a (HTTP or Portlet) session.
     * 
     * Several applications can be associated with a single session.
     * 
     * TODO Document me!
     * 
     * @see javax.servlet.http.HttpSession
     * @see javax.portlet.PortletSession
     * 
     * @author peholmst
     */
    protected interface Session {

        public boolean isNew();

        public Object getAttribute(String name);

        public void setAttribute(String name, Object o);

        public int getMaxInactiveInterval();

        public Object getWrappedSession();

    }

    /**
     * TODO Document me!
     * 
     * @author peholmst
     */
    public interface Callback {

        public void criticalNotification(Request request, Response response,
                String cap, String msg, String details, String outOfSyncURL)
                throws IOException;

        public String getRequestPathInfo(Request request);

        public InputStream getThemeResourceAsStream(String themeName,
                String resource) throws IOException;

    }

    static class UploadInterruptedException extends Exception {
        public UploadInterruptedException() {
            super("Upload interrupted by other thread");
        }
    }

    private static String GET_PARAM_REPAINT_ALL = "repaintAll";

    // flag used in the request to indicate that the security token should be
    // written to the response
    private static final String WRITE_SECURITY_TOKEN_FLAG = "writeSecurityToken";

    /* Variable records indexes */
    private static final int VAR_PID = 1;
    private static final int VAR_NAME = 2;
    private static final int VAR_TYPE = 3;
    private static final int VAR_VALUE = 0;

    private static final char VTYPE_PAINTABLE = 'p';
    private static final char VTYPE_BOOLEAN = 'b';
    private static final char VTYPE_DOUBLE = 'd';
    private static final char VTYPE_FLOAT = 'f';
    private static final char VTYPE_LONG = 'l';
    private static final char VTYPE_INTEGER = 'i';
    private static final char VTYPE_STRING = 's';
    private static final char VTYPE_ARRAY = 'a';
    private static final char VTYPE_STRINGARRAY = 'c';
    private static final char VTYPE_MAP = 'm';

    private static final char VAR_RECORD_SEPARATOR = '\u001e';

    private static final char VAR_FIELD_SEPARATOR = '\u001f';

    public static final char VAR_BURST_SEPARATOR = '\u001d';

    public static final char VAR_ARRAYITEM_SEPARATOR = '\u001c';

    public static final char VAR_ESCAPE_CHARACTER = '\u001b';

    private final HashMap<String, OpenWindowCache> currentlyOpenWindowsInClient = new HashMap<String, OpenWindowCache>();

    private static final int MAX_BUFFER_SIZE = 64 * 1024;

    /* Same as in apache commons file upload library that was previously used. */
    private static final int MAX_UPLOAD_BUFFER_SIZE = 4 * 1024;

    private static final String GET_PARAM_ANALYZE_LAYOUTS = "analyzeLayouts";

    private final ArrayList<Paintable> dirtyPaintables = new ArrayList<Paintable>();

    private final HashMap<Paintable, String> paintableIdMap = new HashMap<Paintable, String>();

    private final HashMap<String, Paintable> idPaintableMap = new HashMap<String, Paintable>();

    private int idSequence = 0;

    private final Application application;

    // Note that this is only accessed from synchronized block and
    // thus should be thread-safe.
    private String closingWindowName = null;

    private List<String> locales;

    private int pendingLocalesIndex;

    private int timeoutInterval = -1;

    private DragAndDropService dragAndDropService;

    private String requestThemeName;

    private int maxInactiveInterval;

    private static int nextUnusedWindowSuffix = 1;

    /**
     * TODO New constructor - document me!
     * 
     * @param application
     */
    public AbstractCommunicationManager(Application application) {
        this.application = application;
        requireLocale(application.getLocale().toString());
    }

    protected Application getApplication() {
        return application;
    }

    private static final int LF = "\n".getBytes()[0];

    private static final String CRLF = "\r\n";

    private static final String UTF8 = "UTF-8";

    private static final String GET_PARAM_HIGHLIGHT_COMPONENT = "highlightComponent";

    private Paintable highLightedPaintable;

    private static String readLine(InputStream stream) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int readByte = stream.read();
        while (readByte != LF) {
            bout.write(readByte);
            readByte = stream.read();
        }
        byte[] bytes = bout.toByteArray();
        return new String(bytes, 0, bytes.length - 1, UTF8);
    }

    /**
     * Method used to stream content from a multipart request (either from
     * servlet or portlet request) to given StreamVariable
     * 
     * 
     * @param request
     * @param response
     * @param streamVariable
     * @param owner
     * @param boundary
     * @throws IOException
     */
    protected void doHandleSimpleMultipartFileUpload(Request request,
            Response response, StreamVariable streamVariable,
            String variableName, VariableOwner owner, String boundary)
            throws IOException {
        // multipart parsing, supports only one file for request, but that is
        // fine for our current terminal

        final InputStream inputStream = request.getInputStream();

        int contentLength = request.getContentLength();

        boolean atStart = false;
        boolean firstFileFieldFound = false;

        String rawfilename = "unknown";
        String rawMimeType = "application/octet-stream";

        /*
         * Read the stream until the actual file starts (empty line). Read
         * filename and content type from multipart headers.
         */
        while (!atStart) {
            String readLine = readLine(inputStream);
            contentLength -= (readLine.getBytes(UTF8).length + CRLF.length());
            if (readLine.startsWith("Content-Disposition:")
                    && readLine.indexOf("filename=") > 0) {
                rawfilename = readLine.replaceAll(".*filename=", "");
                char quote = rawfilename.charAt(0);
                rawfilename = rawfilename.substring(1);
                rawfilename = rawfilename.substring(0,
                        rawfilename.indexOf(quote));
                firstFileFieldFound = true;
            } else if (firstFileFieldFound && readLine.equals("")) {
                atStart = true;
            } else if (readLine.startsWith("Content-Type")) {
                rawMimeType = readLine.split(": ")[1];
            }
        }

        contentLength -= (boundary.length() + CRLF.length() + 2
                * DASHDASH.length() + CRLF.length());

        /*
         * Reads bytes from the underlying stream. Compares the read bytes to
         * the boundary string and returns -1 if met.
         * 
         * The matching happens so that if the read byte equals to the first
         * char of boundary string, the stream goes to "buffering mode". In
         * buffering mode bytes are read until the character does not match the
         * corresponding from boundary string or the full boundary string is
         * found.
         * 
         * Note, if this is someday needed elsewhere, don't shoot yourself to
         * foot and split to a top level helper class.
         */
        InputStream simpleMultiPartReader = new SimpleMultiPartInputStream(
                inputStream, boundary);

        /*
         * Should report only the filename even if the browser sends the path
         */
        final String filename = removePath(rawfilename);
        final String mimeType = rawMimeType;

        try {
            /*
             * safe cast as in GWT terminal all variable owners are expected to
             * be components.
             */
            Component component = (Component) owner;
            if (component.isReadOnly()) {
                throw new UploadException(
                        "Warning: file upload ignored because the componente was read-only");
            }
            boolean forgetVariable = streamToReceiver(simpleMultiPartReader,
                    streamVariable, filename, mimeType, contentLength);
            if (forgetVariable) {
                cleanStreamVariable(owner, variableName);
            }
        } catch (Exception e) {
            synchronized (application) {
                handleChangeVariablesError(application, (Component) owner, e,
                        new HashMap<String, Object>());
            }
        }
        sendUploadResponse(request, response);

    }

    /**
     * Used to stream plain file post (aka XHR2.post(File))
     * 
     * @param request
     * @param response
     * @param streamVariable
     * @param owner
     * @param contentLength
     * @throws IOException
     */
    protected void doHandleXhrFilePost(Request request, Response response,
            StreamVariable streamVariable, String variableName,
            VariableOwner owner, int contentLength) throws IOException {

        // These are unknown in filexhr ATM, maybe add to Accept header that
        // is accessible in portlets
        final String filename = "unknown";
        final String mimeType = filename;
        final InputStream stream = request.getInputStream();
        try {
            /*
             * safe cast as in GWT terminal all variable owners are expected to
             * be components.
             */
            Component component = (Component) owner;
            if (component.isReadOnly()) {
                throw new UploadException(
                        "Warning: file upload ignored because the component was read-only");
            }
            boolean forgetVariable = streamToReceiver(stream, streamVariable,
                    filename, mimeType, contentLength);
            if (forgetVariable) {
                cleanStreamVariable(owner, variableName);
            }
        } catch (Exception e) {
            synchronized (application) {
                handleChangeVariablesError(application, (Component) owner, e,
                        new HashMap<String, Object>());
            }
        }
        sendUploadResponse(request, response);
    }

    /**
     * @param in
     * @param streamVariable
     * @param filename
     * @param type
     * @param contentLength
     * @return true if the streamvariable has informed that the terminal can
     *         forget this variable
     * @throws UploadException
     */
    protected final boolean streamToReceiver(final InputStream in,
            StreamVariable streamVariable, String filename, String type,
            int contentLength) throws UploadException {
        if (streamVariable == null) {
            throw new IllegalStateException(
                    "StreamVariable for the post not found");
        }

        final Application application = getApplication();

        OutputStream out = null;
        int totalBytes = 0;
        StreamingStartEventImpl startedEvent = new StreamingStartEventImpl(
                filename, type, contentLength);
        try {
            boolean listenProgress;
            synchronized (application) {
                streamVariable.streamingStarted(startedEvent);
                out = streamVariable.getOutputStream();
                listenProgress = streamVariable.listenProgress();
            }

            // Gets the output target stream
            if (out == null) {
                throw new NoOutputStreamException();
            }

            if (null == in) {
                // No file, for instance non-existent filename in html upload
                throw new NoInputStreamException();
            }

            final byte buffer[] = new byte[MAX_UPLOAD_BUFFER_SIZE];
            int bytesReadToBuffer = 0;
            while ((bytesReadToBuffer = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesReadToBuffer);
                totalBytes += bytesReadToBuffer;
                if (listenProgress) {
                    // update progress if listener set and contentLength
                    // received
                    synchronized (application) {
                        StreamingProgressEventImpl progressEvent = new StreamingProgressEventImpl(
                                filename, type, contentLength, totalBytes);
                        streamVariable.onProgress(progressEvent);
                    }
                }
                if (streamVariable.isInterrupted()) {
                    throw new UploadInterruptedException();
                }
            }

            // upload successful
            out.close();
            StreamingEndEvent event = new StreamingEndEventImpl(filename, type,
                    totalBytes);
            synchronized (application) {
                streamVariable.streamingFinished(event);
            }

        } catch (UploadInterruptedException e) {
            // Download interrupted by application code
            tryToCloseStream(out);
            StreamingErrorEvent event = new StreamingErrorEventImpl(filename,
                    type, contentLength, totalBytes, e);
            synchronized (application) {
                streamVariable.streamingFailed(event);
            }
            // Note, we are not throwing interrupted exception forward as it is
            // not a terminal level error like all other exception.
        } catch (final Exception e) {
            tryToCloseStream(out);
            synchronized (application) {
                StreamingErrorEvent event = new StreamingErrorEventImpl(
                        filename, type, contentLength, totalBytes, e);
                synchronized (application) {
                    streamVariable.streamingFailed(event);
                }
                // throw exception for terminal to be handled (to be passed to
                // terminalErrorHandler)
                throw new UploadException(e);
            }
        }
        return startedEvent.isDisposed();
    }

    static void tryToCloseStream(OutputStream out) {
        try {
            // try to close output stream (e.g. file handle)
            if (out != null) {
                out.close();
            }
        } catch (IOException e1) {
            // NOP
        }
    }

    static void tryToCloseStream(InputStream in) {
        try {
            // try to close output stream (e.g. file handle)
            if (in != null) {
                in.close();
            }
        } catch (IOException e1) {
            // NOP
        }
    }

    /**
     * Removes any possible path information from the filename and returns the
     * filename. Separators / and \\ are used.
     * 
     * @param name
     * @return
     */
    private static String removePath(String filename) {
        if (filename != null) {
            filename = filename.replaceAll("^.*[/\\\\]", "");
        }

        return filename;
    }

    /**
     * TODO document
     * 
     * @param request
     * @param response
     * @throws IOException
     */
    protected void sendUploadResponse(Request request, Response response)
            throws IOException {
        response.setContentType("text/html");
        final OutputStream out = response.getOutputStream();
        final PrintWriter outWriter = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(out, "UTF-8")));
        outWriter.print("<html><body>download handled</body></html>");
        outWriter.flush();
        out.close();
    }

    /**
     * Internally process a UIDL request from the client.
     * 
     * This method calls
     * {@link #handleVariables(Request, Response, Callback, Application, Window)}
     * to process any changes to variables by the client and then repaints
     * affected components using {@link #paintAfterVariableChanges()}.
     * 
     * Also, some cleanup is done when a request arrives for an application that
     * has already been closed.
     * 
     * The method handleUidlRequest(...) in subclasses should call this method.
     * 
     * TODO better documentation
     * 
     * @param request
     * @param response
     * @param callback
     * @param window
     *            target window for the UIDL request, can be null if target not
     *            found
     * @throws IOException
     * @throws InvalidUIDLSecurityKeyException
     */
    protected void doHandleUidlRequest(Request request, Response response,
            Callback callback, Window window) throws IOException,
            InvalidUIDLSecurityKeyException {

        requestThemeName = request.getParameter("theme");
        maxInactiveInterval = request.getSession().getMaxInactiveInterval();
        // repaint requested or session has timed out and new one is created
        boolean repaintAll;
        final OutputStream out;

        repaintAll = (request.getParameter(GET_PARAM_REPAINT_ALL) != null);
        // || (request.getSession().isNew()); FIXME What the h*ll is this??
        out = response.getOutputStream();

        boolean analyzeLayouts = false;
        if (repaintAll) {
            // analyzing can be done only with repaintAll
            analyzeLayouts = (request.getParameter(GET_PARAM_ANALYZE_LAYOUTS) != null);

            if (request.getParameter(GET_PARAM_HIGHLIGHT_COMPONENT) != null) {
                String pid = request
                        .getParameter(GET_PARAM_HIGHLIGHT_COMPONENT);
                highLightedPaintable = idPaintableMap.get(pid);
                highlightPaintable(highLightedPaintable);
            }
        }

        final PrintWriter outWriter = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(out, "UTF-8")));

        // The rest of the process is synchronized with the application
        // in order to guarantee that no parallel variable handling is
        // made
        synchronized (application) {

            // Finds the window within the application
            if (application.isRunning()) {
                // Returns if no window found
                if (window == null) {
                    // This should not happen, no windows exists but
                    // application is still open.
                    getLogger().warning(
                            "Could not get window for application with request ID "
                                    + request.getRequestID());
                    return;
                }
            } else {
                // application has been closed
                endApplication(request, response, application);
                return;
            }

            // Change all variables based on request parameters
            if (!handleVariables(request, response, callback, application,
                    window)) {

                // var inconsistency; the client is probably out-of-sync
                SystemMessages ci = null;
                try {
                    Method m = application.getClass().getMethod(
                            "getSystemMessages", (Class[]) null);
                    ci = (Application.SystemMessages) m.invoke(null,
                            (Object[]) null);
                } catch (Exception e2) {
                    // FIXME: Handle exception
                    // Not critical, but something is still wrong; print
                    // stacktrace
                    getLogger().log(Level.WARNING,
                            "getSystemMessages() failed - continuing", e2);
                }
                if (ci != null) {
                    String msg = ci.getOutOfSyncMessage();
                    String cap = ci.getOutOfSyncCaption();
                    if (msg != null || cap != null) {
                        callback.criticalNotification(request, response, cap,
                                msg, null, ci.getOutOfSyncURL());
                        // will reload page after this
                        return;
                    }
                }
                // No message to show, let's just repaint all.
                repaintAll = true;
            }

            paintAfterVariableChanges(request, response, callback, repaintAll,
                    outWriter, window, analyzeLayouts);

            if (closingWindowName != null) {
                currentlyOpenWindowsInClient.remove(closingWindowName);
                closingWindowName = null;
            }
        }

        outWriter.close();
        requestThemeName = null;
    }

    protected void highlightPaintable(Paintable highLightedPaintable2) {
        StringBuilder sb = new StringBuilder();
        sb.append("*** Debug details of a component:  *** \n");
        sb.append("Type: ");
        sb.append(highLightedPaintable2.getClass().getName());
        if (highLightedPaintable2 instanceof AbstractComponent) {
            AbstractComponent component = (AbstractComponent) highLightedPaintable2;
            sb.append("\nId:");
            sb.append(paintableIdMap.get(component));
            if (component.getCaption() != null) {
                sb.append("\nCaption:");
                sb.append(component.getCaption());
            }

            printHighlightedComponentHierarchy(sb, component);
        }
        getLogger().info(sb.toString());
    }

    protected void printHighlightedComponentHierarchy(StringBuilder sb,
            AbstractComponent component) {
        LinkedList<Component> h = new LinkedList<Component>();
        h.add(component);
        Component parent = component.getParent();
        while (parent != null) {
            h.addFirst(parent);
            parent = parent.getParent();
        }

        sb.append("\nComponent hierarchy:\n");
        Application application2 = component.getApplication();
        sb.append(application2.getClass().getName());
        sb.append(".");
        sb.append(application2.getClass().getSimpleName());
        sb.append("(");
        sb.append(application2.getClass().getSimpleName());
        sb.append(".java");
        sb.append(":1)");
        int l = 1;
        for (Component component2 : h) {
            sb.append("\n");
            for (int i = 0; i < l; i++) {
                sb.append("  ");
            }
            l++;
            Class<? extends Component> componentClass = component2.getClass();
            Class<?> topClass = componentClass;
            while (topClass.getEnclosingClass() != null) {
                topClass = topClass.getEnclosingClass();
            }
            sb.append(componentClass.getName());
            sb.append(".");
            sb.append(componentClass.getSimpleName());
            sb.append("(");
            sb.append(topClass.getSimpleName());
            sb.append(".java:1)");
        }
    }

    /**
     * TODO document
     * 
     * @param request
     * @param response
     * @param callback
     * @param repaintAll
     * @param outWriter
     * @param window
     * @param analyzeLayouts
     * @throws PaintException
     * @throws IOException
     */
    private void paintAfterVariableChanges(Request request, Response response,
            Callback callback, boolean repaintAll, final PrintWriter outWriter,
            Window window, boolean analyzeLayouts) throws PaintException,
            IOException {

        if (repaintAll) {
            makeAllPaintablesDirty(window);
        }

        // Removes application if it has stopped during variable changes
        if (!application.isRunning()) {
            endApplication(request, response, application);
            return;
        }

        openJsonMessage(outWriter, response);

        // security key
        Object writeSecurityTokenFlag = request
                .getAttribute(WRITE_SECURITY_TOKEN_FLAG);

        if (writeSecurityTokenFlag != null) {
            String seckey = (String) request.getSession().getAttribute(
                    ApplicationConnection.UIDL_SECURITY_TOKEN_ID);
            if (seckey == null) {
                seckey = UUID.randomUUID().toString();
                request.getSession().setAttribute(
                        ApplicationConnection.UIDL_SECURITY_TOKEN_ID, seckey);
            }
            outWriter.print("\"" + ApplicationConnection.UIDL_SECURITY_TOKEN_ID
                    + "\":\"");
            outWriter.print(seckey);
            outWriter.print("\",");
        }

        // If the browser-window has been closed - we do not need to paint it at
        // all
        if (window.getName().equals(closingWindowName)) {
            outWriter.print("\"changes\":[]");
        } else {
            // re-get window - may have been changed
            Window newWindow = doGetApplicationWindow(request, callback,
                    application, window);
            if (newWindow != window) {
                window = newWindow;
                repaintAll = true;
            }

            writeUidlResponce(callback, repaintAll, outWriter, window,
                    analyzeLayouts);

        }
        closeJsonMessage(outWriter);

        outWriter.close();

    }

    public void writeUidlResponce(Callback callback, boolean repaintAll,
            final PrintWriter outWriter, Window window, boolean analyzeLayouts)
            throws PaintException {
        outWriter.print("\"changes\":[");

        ArrayList<Paintable> paintables = null;

        List<InvalidLayout> invalidComponentRelativeSizes = null;

        JsonPaintTarget paintTarget = new JsonPaintTarget(this, outWriter,
                !repaintAll);
        OpenWindowCache windowCache = currentlyOpenWindowsInClient.get(window
                .getName());
        if (windowCache == null) {
            windowCache = new OpenWindowCache();
            currentlyOpenWindowsInClient.put(window.getName(), windowCache);
        }

        // Paints components
        if (repaintAll) {
            paintables = new ArrayList<Paintable>();
            paintables.add(window);

            // Reset sent locales
            locales = null;
            requireLocale(application.getLocale().toString());

        } else {
            // remove detached components from paintableIdMap so they
            // can be GC'ed
            /*
             * TODO figure out if we could move this beyond the painting phase,
             * "respond as fast as possible, then do the cleanup". Beware of
             * painting the dirty detatched components.
             */
            for (Iterator<Paintable> it = paintableIdMap.keySet().iterator(); it
                    .hasNext();) {
                Component p = (Component) it.next();
                if (p.getApplication() == null) {
                    unregisterPaintable(p);
                    // Take into account that some other component may have
                    // reused p's ID by now (this can happen when manually
                    // assigning IDs with setDebugId().) See #8090.
                    String pid = paintableIdMap.get(p);
                    if (idPaintableMap.get(pid) == p) {
                        idPaintableMap.remove(pid);
                    }
                    it.remove();
                    dirtyPaintables.remove(p);
                }
            }
            paintables = getDirtyVisibleComponents(window);
        }
        if (paintables != null) {

            // We need to avoid painting children before parent.
            // This is ensured by ordering list by depth in component
            // tree
            Collections.sort(paintables, new Comparator<Paintable>() {
                public int compare(Paintable o1, Paintable o2) {
                    Component c1 = (Component) o1;
                    Component c2 = (Component) o2;
                    int d1 = 0;
                    while (c1.getParent() != null) {
                        d1++;
                        c1 = c1.getParent();
                    }
                    int d2 = 0;
                    while (c2.getParent() != null) {
                        d2++;
                        c2 = c2.getParent();
                    }
                    if (d1 < d2) {
                        return -1;
                    }
                    if (d1 > d2) {
                        return 1;
                    }
                    return 0;
                }
            });

            for (final Iterator<Paintable> i = paintables.iterator(); i
                    .hasNext();) {
                final Paintable p = i.next();

                // TODO CLEAN
                if (p instanceof Window) {
                    final Window w = (Window) p;
                    if (w.getTerminal() == null) {
                        w.setTerminal(application.getMainWindow().getTerminal());
                    }
                }
                /*
                 * This does not seem to happen in tk5, but remember this case:
                 * else if (p instanceof Component) { if (((Component)
                 * p).getParent() == null || ((Component) p).getApplication() ==
                 * null) { // Component requested repaint, but is no // longer
                 * attached: skip paintablePainted(p); continue; } }
                 */

                // TODO we may still get changes that have been
                // rendered already (changes with only cached flag)
                if (paintTarget.needsToBePainted(p)) {
                    paintTarget.startTag("change");
                    paintTarget.addAttribute("format", "uidl");
                    final String pid = getPaintableId(p);
                    paintTarget.addAttribute("pid", pid);

                    p.paint(paintTarget);

                    paintTarget.endTag("change");
                }
                paintablePainted(p);

                if (analyzeLayouts) {
                    Window w = (Window) p;
                    invalidComponentRelativeSizes = ComponentSizeValidator
                            .validateComponentRelativeSizes(w.getContent(),
                                    null, null);

                    // Also check any existing subwindows
                    if (w.getChildWindows() != null) {
                        for (Window subWindow : w.getChildWindows()) {
                            invalidComponentRelativeSizes = ComponentSizeValidator
                                    .validateComponentRelativeSizes(
                                            subWindow.getContent(),
                                            invalidComponentRelativeSizes, null);
                        }
                    }
                }
            }
        }

        paintTarget.close();
        outWriter.print("]"); // close changes

        outWriter.print(", \"meta\" : {");
        boolean metaOpen = false;

        if (repaintAll) {
            metaOpen = true;
            outWriter.write("\"repaintAll\":true");
            if (analyzeLayouts) {
                outWriter.write(", \"invalidLayouts\":");
                outWriter.write("[");
                if (invalidComponentRelativeSizes != null) {
                    boolean first = true;
                    for (InvalidLayout invalidLayout : invalidComponentRelativeSizes) {
                        if (!first) {
                            outWriter.write(",");
                        } else {
                            first = false;
                        }
                        invalidLayout.reportErrors(outWriter, this, System.err);
                    }
                }
                outWriter.write("]");
            }
            if (highLightedPaintable != null) {
                outWriter.write(", \"hl\":\"");
                outWriter.write(paintableIdMap.get(highLightedPaintable));
                outWriter.write("\"");
                highLightedPaintable = null;
            }
        }

        SystemMessages ci = null;
        try {
            Method m = application.getClass().getMethod("getSystemMessages",
                    (Class[]) null);
            ci = (Application.SystemMessages) m.invoke(null, (Object[]) null);
        } catch (NoSuchMethodException e) {
            getLogger().log(Level.WARNING,
                    "getSystemMessages() failed - continuing", e);
        } catch (IllegalArgumentException e) {
            getLogger().log(Level.WARNING,
                    "getSystemMessages() failed - continuing", e);
        } catch (IllegalAccessException e) {
            getLogger().log(Level.WARNING,
                    "getSystemMessages() failed - continuing", e);
        } catch (InvocationTargetException e) {
            getLogger().log(Level.WARNING,
                    "getSystemMessages() failed - continuing", e);
        }

        // meta instruction for client to enable auto-forward to
        // sessionExpiredURL after timer expires.
        if (ci != null && ci.getSessionExpiredMessage() == null
                && ci.getSessionExpiredCaption() == null
                && ci.isSessionExpiredNotificationEnabled()) {
            int newTimeoutInterval = getTimeoutInterval();
            if (repaintAll || (timeoutInterval != newTimeoutInterval)) {
                String escapedURL = ci.getSessionExpiredURL() == null ? "" : ci
                        .getSessionExpiredURL().replace("/", "\\/");
                if (metaOpen) {
                    outWriter.write(",");
                }
                outWriter.write("\"timedRedirect\":{\"interval\":"
                        + (newTimeoutInterval + 15) + ",\"url\":\""
                        + escapedURL + "\"}");
                metaOpen = true;
            }
            timeoutInterval = newTimeoutInterval;
        }

        outWriter.print("}, \"resources\" : {");

        // Precache custom layouts

        // TODO We should only precache the layouts that are not
        // cached already (plagiate from usedPaintableTypes)
        int resourceIndex = 0;
        for (final Iterator<Object> i = paintTarget.getUsedResources()
                .iterator(); i.hasNext();) {
            final String resource = (String) i.next();
            InputStream is = null;
            try {
                is = callback.getThemeResourceAsStream(getTheme(window),
                        resource);
            } catch (final Exception e) {
                // FIXME: Handle exception
                getLogger().log(Level.FINER,
                        "Failed to get theme resource stream.", e);
            }
            if (is != null) {

                outWriter.print((resourceIndex++ > 0 ? ", " : "") + "\""
                        + resource + "\" : ");
                final StringBuffer layout = new StringBuffer();

                try {
                    final InputStreamReader r = new InputStreamReader(is,
                            "UTF-8");
                    final char[] buffer = new char[20000];
                    int charsRead = 0;
                    while ((charsRead = r.read(buffer)) > 0) {
                        layout.append(buffer, 0, charsRead);
                    }
                    r.close();
                } catch (final java.io.IOException e) {
                    // FIXME: Handle exception
                    getLogger().log(Level.INFO, "Resource transfer failed", e);
                }
                outWriter.print("\""
                        + JsonPaintTarget.escapeJSON(layout.toString()) + "\"");
            } else {
                // FIXME: Handle exception
                getLogger().severe("CustomLayout not found: " + resource);
            }
        }
        outWriter.print("}");

        Collection<Class<? extends Paintable>> usedPaintableTypes = paintTarget
                .getUsedPaintableTypes();
        boolean typeMappingsOpen = false;
        for (Class<? extends Paintable> class1 : usedPaintableTypes) {
            if (windowCache.cache(class1)) {
                // client does not know the mapping key for this type, send
                // mapping to client
                if (!typeMappingsOpen) {
                    typeMappingsOpen = true;
                    outWriter.print(", \"typeMappings\" : { ");
                } else {
                    outWriter.print(" , ");
                }
                String canonicalName = class1.getCanonicalName();
                outWriter.print("\"");
                outWriter.print(canonicalName);
                outWriter.print("\" : ");
                outWriter.print(getTagForType(class1));
            }
        }
        if (typeMappingsOpen) {
            outWriter.print(" }");
        }

        // add any pending locale definitions requested by the client
        printLocaleDeclarations(outWriter);

        if (dragAndDropService != null) {
            dragAndDropService.printJSONResponse(outWriter);
        }

        writePerformanceData(outWriter);
    }

    /**
     * Adds the performance timing data (used by TestBench 3) to the UIDL
     * response.
     */
    private void writePerformanceData(final PrintWriter outWriter) {
        AbstractWebApplicationContext ctx = (AbstractWebApplicationContext) application
                .getContext();
        outWriter.write(String.format(", \"timings\":[%d, %d]",
                ctx.getTotalSessionTime(), ctx.getLastRequestTime()));
    }

    private int getTimeoutInterval() {
        return maxInactiveInterval;
    }

    private String getTheme(Window window) {
        String themeName = window.getTheme();
        String requestThemeName = getRequestTheme();

        if (requestThemeName != null) {
            themeName = requestThemeName;
        }
        if (themeName == null) {
            themeName = AbstractApplicationServlet.getDefaultTheme();
        }
        return themeName;
    }

    private String getRequestTheme() {
        return requestThemeName;
    }

    public void makeAllPaintablesDirty(Window window) {
        // If repaint is requested, clean all ids in this root window
        for (final Iterator<String> it = idPaintableMap.keySet().iterator(); it
                .hasNext();) {
            final Component c = (Component) idPaintableMap.get(it.next());
            if (isChildOf(window, c)) {
                it.remove();
                paintableIdMap.remove(c);
            }
        }
        // clean WindowCache
        OpenWindowCache openWindowCache = currentlyOpenWindowsInClient
                .get(window.getName());
        if (openWindowCache != null) {
            openWindowCache.clear();
        }
    }

    /**
     * Called when communication manager stops listening for repaints for given
     * component.
     * 
     * @param p
     */
    protected void unregisterPaintable(Component p) {
        p.removeListener(this);
    }

    /**
     * TODO document
     * 
     * If this method returns false, something was submitted that we did not
     * expect; this is probably due to the client being out-of-sync and sending
     * variable changes for non-existing pids
     * 
     * @return true if successful, false if there was an inconsistency
     */
    private boolean handleVariables(Request request, Response response,
            Callback callback, Application application2, Window window)
            throws IOException, InvalidUIDLSecurityKeyException {
        boolean success = true;

        String changes = getRequestPayload(request);
        if (changes != null) {

            // Manage bursts one by one
            final String[] bursts = changes.split(String
                    .valueOf(VAR_BURST_SEPARATOR));

            // Security: double cookie submission pattern unless disabled by
            // property
            if (!"true"
                    .equals(application2
                            .getProperty(AbstractApplicationServlet.SERVLET_PARAMETER_DISABLE_XSRF_PROTECTION))) {
                if (bursts.length == 1 && "init".equals(bursts[0])) {
                    // init request; don't handle any variables, key sent in
                    // response.
                    request.setAttribute(WRITE_SECURITY_TOKEN_FLAG, true);
                    return true;
                } else {
                    // ApplicationServlet has stored the security token in the
                    // session; check that it matched the one sent in the UIDL
                    String sessId = (String) request.getSession().getAttribute(
                            ApplicationConnection.UIDL_SECURITY_TOKEN_ID);

                    if (sessId == null || !sessId.equals(bursts[0])) {
                        throw new InvalidUIDLSecurityKeyException(
                                "Security key mismatch");
                    }
                }

            }

            for (int bi = 1; bi < bursts.length; bi++) {
                final String burst = bursts[bi];
                success = handleVariableBurst(request, application2, success,
                        burst);

                // In case that there were multiple bursts, we know that this is
                // a special synchronous case for closing window. Thus we are
                // not interested in sending any UIDL changes back to client.
                // Still we must clear component tree between bursts to ensure
                // that no removed components are updated. The painting after
                // the last burst is handled normally by the calling method.
                if (bi < bursts.length - 1) {

                    // We will be discarding all changes
                    final PrintWriter outWriter = new PrintWriter(
                            new CharArrayWriter());

                    paintAfterVariableChanges(request, response, callback,
                            true, outWriter, window, false);

                }

            }
        }
        /*
         * Note that we ignore inconsistencies while handling unload request.
         * The client can't remove invalid variable changes from the burst, and
         * we don't have the required logic implemented on the server side. E.g.
         * a component is removed in a previous burst.
         */
        return success || closingWindowName != null;
    }

    public boolean handleVariableBurst(Object source, Application app,
            boolean success, final String burst) {
        // extract variables to two dim string array
        final String[] tmp = burst.split(String.valueOf(VAR_RECORD_SEPARATOR));
        final String[][] variableRecords = new String[tmp.length][4];
        for (int i = 0; i < tmp.length; i++) {
            variableRecords[i] = tmp[i].split(String
                    .valueOf(VAR_FIELD_SEPARATOR));
        }

        for (int i = 0; i < variableRecords.length; i++) {
            String[] variable = variableRecords[i];
            String[] nextVariable = null;
            if (i + 1 < variableRecords.length) {
                nextVariable = variableRecords[i + 1];
            }
            final VariableOwner owner = getVariableOwner(variable[VAR_PID]);
            if (owner != null && owner.isEnabled()) {
                Map<String, Object> m;
                if (nextVariable != null
                        && variable[VAR_PID].equals(nextVariable[VAR_PID])) {
                    // we have more than one value changes in row for
                    // one variable owner, collect them in HashMap
                    m = new HashMap<String, Object>();
                    m.put(variable[VAR_NAME], decodeVariable(variable));
                } else {
                    // use optimized single value map
                    m = Collections.singletonMap(variable[VAR_NAME],
                            decodeVariable(variable));
                }

                // collect following variable changes for this owner
                while (nextVariable != null
                        && variable[VAR_PID].equals(nextVariable[VAR_PID])) {
                    i++;
                    variable = nextVariable;
                    if (i + 1 < variableRecords.length) {
                        nextVariable = variableRecords[i + 1];
                    } else {
                        nextVariable = null;
                    }
                    m.put(variable[VAR_NAME], decodeVariable(variable));
                }
                try {
                    changeVariables(source, owner, m);

                    // Special-case of closing browser-level windows:
                    // track browser-windows currently open in client
                    if (owner instanceof Window
                            && ((Window) owner).getParent() == null) {
                        final Boolean close = (Boolean) m.get("close");
                        if (close != null && close.booleanValue()) {
                            closingWindowName = ((Window) owner).getName();
                        }
                    }
                } catch (Exception e) {
                    Component errorComponent = null;
                    if (owner instanceof Component) {
                        errorComponent = (Component) owner;
                    } else if (owner instanceof DragAndDropService) {
                        if (m.get("dhowner") instanceof Component) {
                            errorComponent = (Component) m.get("dhowner");
                        }
                    }

                    handleChangeVariablesError(app, errorComponent, e, m);
                }
            } else {

                // Handle special case where window-close is called
                // after the window has been removed from the
                // application or the application has closed
                if ("close".equals(variable[VAR_NAME])
                        && "true".equals(variable[VAR_VALUE])) {
                    // Silently ignore this
                    continue;
                }

                // Ignore variable change
                String msg = "Warning: Ignoring variable change for ";
                if (owner != null) {
                    msg += "disabled component " + owner.getClass();
                    String caption = ((Component) owner).getCaption();
                    if (caption != null) {
                        msg += ", caption=" + caption;
                    }
                } else {
                    msg += "non-existent component, VAR_PID="
                            + variable[VAR_PID];
                    success = false;
                }
                getLogger().warning(msg);
                continue;
            }
        }
        return success;
    }

    protected void changeVariables(Object source, final VariableOwner owner,
            Map<String, Object> m) {
        owner.changeVariables(source, m);
    }

    protected VariableOwner getVariableOwner(String string) {
        VariableOwner owner = (VariableOwner) idPaintableMap.get(string);
        if (owner == null && string.startsWith("DD")) {
            return getDragAndDropService();
        }
        return owner;
    }

    private VariableOwner getDragAndDropService() {
        if (dragAndDropService == null) {
            dragAndDropService = new DragAndDropService(this);
        }
        return dragAndDropService;
    }

    /**
     * Reads the request data from the Request and returns it converted to an
     * UTF-8 string.
     * 
     * @param request
     * @return
     * @throws IOException
     */
    protected String getRequestPayload(Request request) throws IOException {

        int requestLength = request.getContentLength();
        if (requestLength == 0) {
            return null;
        }

        ByteArrayOutputStream bout = requestLength <= 0 ? new ByteArrayOutputStream()
                : new ByteArrayOutputStream(requestLength);

        InputStream inputStream = request.getInputStream();
        byte[] buffer = new byte[MAX_BUFFER_SIZE];

        while (true) {
            int read = inputStream.read(buffer);
            if (read == -1) {
                break;
            }
            bout.write(buffer, 0, read);
        }
        String result = new String(bout.toByteArray(), "utf-8");

        return result;
    }

    public class ErrorHandlerErrorEvent implements ErrorEvent, Serializable {
        private final Throwable throwable;

        public ErrorHandlerErrorEvent(Throwable throwable) {
            this.throwable = throwable;
        }

        public Throwable getThrowable() {
            return throwable;
        }

    }

    /**
     * Handles an error (exception) that occurred when processing variable
     * changes from the client or a failure of a file upload.
     * 
     * For {@link AbstractField} components,
     * {@link AbstractField#handleError(com.vaadin.ui.AbstractComponent.ComponentErrorEvent)}
     * is called. In all other cases (or if the field does not handle the
     * error), {@link ErrorListener#terminalError(ErrorEvent)} for the
     * application error handler is called.
     * 
     * @param application
     * @param owner
     *            component that the error concerns
     * @param e
     *            exception that occurred
     * @param m
     *            map from variable names to values
     */
    private void handleChangeVariablesError(Application application,
            Component owner, Exception e, Map<String, Object> m) {
        boolean handled = false;
        ChangeVariablesErrorEvent errorEvent = new ChangeVariablesErrorEvent(
                owner, e, m);

        if (owner instanceof AbstractField) {
            try {
                handled = ((AbstractField) owner).handleError(errorEvent);
            } catch (Exception handlerException) {
                /*
                 * If there is an error in the component error handler we pass
                 * the that error to the application error handler and continue
                 * processing the actual error
                 */
                application.getErrorHandler().terminalError(
                        new ErrorHandlerErrorEvent(handlerException));
                handled = false;
            }
        }

        if (!handled) {
            application.getErrorHandler().terminalError(errorEvent);
        }

    }

    private Object decodeVariable(String[] variable) {
        try {
            return convertVariableValue(variable[VAR_TYPE].charAt(0),
                    variable[VAR_VALUE]);
        } catch (Exception e) {
            String pid = variable[VAR_PID];
            VariableOwner variableOwner = getVariableOwner(pid);
            String targetType = variableOwner == null ? "unknown VariableOwner"
                    : variableOwner.getClass().getName();
            throw new RuntimeException("Could not convert variable \""
                    + variable[VAR_NAME] + "\" for " + targetType + " (" + pid
                    + ")", e);
        }
    }

    private Object convertVariableValue(char variableType, String strValue) {
        Object val = null;
        switch (variableType) {
        case VTYPE_ARRAY:
            val = convertArray(strValue);
            break;
        case VTYPE_MAP:
            val = convertMap(strValue);
            break;
        case VTYPE_STRINGARRAY:
            val = convertStringArray(strValue);
            break;
        case VTYPE_STRING:
            // decode encoded separators
            val = decodeVariableValue(strValue);
            break;
        case VTYPE_INTEGER:
            val = Integer.valueOf(strValue);
            break;
        case VTYPE_LONG:
            val = Long.valueOf(strValue);
            break;
        case VTYPE_FLOAT:
            val = Float.valueOf(strValue);
            break;
        case VTYPE_DOUBLE:
            val = Double.valueOf(strValue);
            break;
        case VTYPE_BOOLEAN:
            val = Boolean.valueOf(strValue);
            break;
        case VTYPE_PAINTABLE:
            val = idPaintableMap.get(strValue);
            break;
        }

        return val;
    }

    private Object convertMap(String strValue) {
        String[] parts = strValue.split(
                String.valueOf(VAR_ARRAYITEM_SEPARATOR), -1);
        HashMap<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < parts.length; i += 2) {
            String key = parts[i];
            if (key.length() > 0) {
                char variabletype = key.charAt(0);
                // decode encoded separators
                String decodedValue = decodeVariableValue(parts[i + 1]);
                String decodedKey = decodeVariableValue(key.substring(1));
                Object value = convertVariableValue(variabletype, decodedValue);
                map.put(decodedKey, value);
            }
        }
        return map;
    }

    private String[] convertStringArray(String strValue) {
        // need to return delimiters and filter them out; otherwise empty
        // strings are lost
        // an extra empty delimiter at the end is automatically eliminated
        final String arrayItemSeparator = String
                .valueOf(VAR_ARRAYITEM_SEPARATOR);
        StringTokenizer tokenizer = new StringTokenizer(strValue,
                arrayItemSeparator, true);
        List<String> tokens = new ArrayList<String>();
        String prevToken = arrayItemSeparator;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (!arrayItemSeparator.equals(token)) {
                // decode encoded separators
                tokens.add(decodeVariableValue(token));
            } else if (arrayItemSeparator.equals(prevToken)) {
                tokens.add("");
            }
            prevToken = token;
        }
        return tokens.toArray(new String[tokens.size()]);
    }

    private Object convertArray(String strValue) {
        String[] val = strValue.split(String.valueOf(VAR_ARRAYITEM_SEPARATOR),
                -1);
        if (val.length == 0 || (val.length == 1 && val[0].length() == 0)) {
            return new Object[0];
        }
        Object[] values = new Object[val.length];
        for (int i = 0; i < values.length; i++) {
            String string = val[i];
            // first char of string is type
            char variableType = string.charAt(0);
            values[i] = convertVariableValue(variableType, string.substring(1));
        }
        return values;
    }

    /**
     * Decode encoded burst, record, field and array item separator characters
     * in a variable value String received from the client. This protects from
     * separator injection attacks.
     * 
     * @param encodedValue
     *            to decode
     * @return decoded value
     */
    protected String decodeVariableValue(String encodedValue) {
        final StringBuilder result = new StringBuilder();
        final StringCharacterIterator iterator = new StringCharacterIterator(
                encodedValue);
        char character = iterator.current();
        while (character != CharacterIterator.DONE) {
            if (VAR_ESCAPE_CHARACTER == character) {
                character = iterator.next();
                switch (character) {
                case VAR_ESCAPE_CHARACTER + 0x30:
                    // escaped escape character
                    result.append(VAR_ESCAPE_CHARACTER);
                    break;
                case VAR_BURST_SEPARATOR + 0x30:
                case VAR_RECORD_SEPARATOR + 0x30:
                case VAR_FIELD_SEPARATOR + 0x30:
                case VAR_ARRAYITEM_SEPARATOR + 0x30:
                    // +0x30 makes these letters for easier reading
                    result.append((char) (character - 0x30));
                    break;
                case CharacterIterator.DONE:
                    // error
                    throw new RuntimeException(
                            "Communication error: Unexpected end of message");
                default:
                    // other escaped character - probably a client-server
                    // version mismatch
                    throw new RuntimeException(
                            "Invalid escaped character from the client - check that the widgetset and server versions match");
                }
            } else {
                // not a special character - add it to the result as is
                result.append(character);
            }
            character = iterator.next();
        }
        return result.toString();
    }

    /**
     * Prints the queued (pending) locale definitions to a {@link PrintWriter}
     * in a (UIDL) format that can be sent to the client and used there in
     * formatting dates, times etc.
     * 
     * @param outWriter
     */
    private void printLocaleDeclarations(PrintWriter outWriter) {
        /*
         * ----------------------------- Sending Locale sensitive date
         * -----------------------------
         */

        // Send locale informations to client
        outWriter.print(", \"locales\":[");
        for (; pendingLocalesIndex < locales.size(); pendingLocalesIndex++) {

            final Locale l = generateLocale(locales.get(pendingLocalesIndex));
            // Locale name
            outWriter.print("{\"name\":\"" + l.toString() + "\",");

            /*
             * Month names (both short and full)
             */
            final DateFormatSymbols dfs = new DateFormatSymbols(l);
            final String[] short_months = dfs.getShortMonths();
            final String[] months = dfs.getMonths();
            outWriter.print("\"smn\":[\""
                    + // ShortMonthNames
                    short_months[0] + "\",\"" + short_months[1] + "\",\""
                    + short_months[2] + "\",\"" + short_months[3] + "\",\""
                    + short_months[4] + "\",\"" + short_months[5] + "\",\""
                    + short_months[6] + "\",\"" + short_months[7] + "\",\""
                    + short_months[8] + "\",\"" + short_months[9] + "\",\""
                    + short_months[10] + "\",\"" + short_months[11] + "\""
                    + "],");
            outWriter.print("\"mn\":[\""
                    + // MonthNames
                    months[0] + "\",\"" + months[1] + "\",\"" + months[2]
                    + "\",\"" + months[3] + "\",\"" + months[4] + "\",\""
                    + months[5] + "\",\"" + months[6] + "\",\"" + months[7]
                    + "\",\"" + months[8] + "\",\"" + months[9] + "\",\""
                    + months[10] + "\",\"" + months[11] + "\"" + "],");

            /*
             * Weekday names (both short and full)
             */
            final String[] short_days = dfs.getShortWeekdays();
            final String[] days = dfs.getWeekdays();
            outWriter.print("\"sdn\":[\""
                    + // ShortDayNames
                    short_days[1] + "\",\"" + short_days[2] + "\",\""
                    + short_days[3] + "\",\"" + short_days[4] + "\",\""
                    + short_days[5] + "\",\"" + short_days[6] + "\",\""
                    + short_days[7] + "\"" + "],");
            outWriter.print("\"dn\":[\""
                    + // DayNames
                    days[1] + "\",\"" + days[2] + "\",\"" + days[3] + "\",\""
                    + days[4] + "\",\"" + days[5] + "\",\"" + days[6] + "\",\""
                    + days[7] + "\"" + "],");

            /*
             * First day of week (0 = sunday, 1 = monday)
             */
            final Calendar cal = new GregorianCalendar(l);
            outWriter.print("\"fdow\":" + (cal.getFirstDayOfWeek() - 1) + ",");

            /*
             * Date formatting (MM/DD/YYYY etc.)
             */

            DateFormat dateFormat = DateFormat.getDateTimeInstance(
                    DateFormat.SHORT, DateFormat.SHORT, l);
            if (!(dateFormat instanceof SimpleDateFormat)) {
                getLogger().warning(
                        "Unable to get default date pattern for locale "
                                + l.toString());
                dateFormat = new SimpleDateFormat();
            }
            final String df = ((SimpleDateFormat) dateFormat).toPattern();

            int timeStart = df.indexOf("H");
            if (timeStart < 0) {
                timeStart = df.indexOf("h");
            }
            final int ampm_first = df.indexOf("a");
            // E.g. in Korean locale AM/PM is before h:mm
            // TODO should take that into consideration on client-side as well,
            // now always h:mm a
            if (ampm_first > 0 && ampm_first < timeStart) {
                timeStart = ampm_first;
            }
            // Hebrew locale has time before the date
            final boolean timeFirst = timeStart == 0;
            String dateformat;
            if (timeFirst) {
                int dateStart = df.indexOf(' ');
                if (ampm_first > dateStart) {
                    dateStart = df.indexOf(' ', ampm_first);
                }
                dateformat = df.substring(dateStart + 1);
            } else {
                dateformat = df.substring(0, timeStart - 1);
            }

            outWriter.print("\"df\":\"" + dateformat.trim() + "\",");

            /*
             * Time formatting (24 or 12 hour clock and AM/PM suffixes)
             */
            final String timeformat = df.substring(timeStart, df.length());
            /*
             * Doesn't return second or milliseconds.
             * 
             * We use timeformat to determine 12/24-hour clock
             */
            final boolean twelve_hour_clock = timeformat.indexOf("a") > -1;
            // TODO there are other possibilities as well, like 'h' in french
            // (ignore them, too complicated)
            final String hour_min_delimiter = timeformat.indexOf(".") > -1 ? "."
                    : ":";
            // outWriter.print("\"tf\":\"" + timeformat + "\",");
            outWriter.print("\"thc\":" + twelve_hour_clock + ",");
            outWriter.print("\"hmd\":\"" + hour_min_delimiter + "\"");
            if (twelve_hour_clock) {
                final String[] ampm = dfs.getAmPmStrings();
                outWriter.print(",\"ampm\":[\"" + ampm[0] + "\",\"" + ampm[1]
                        + "\"]");
            }
            outWriter.print("}");
            if (pendingLocalesIndex < locales.size() - 1) {
                outWriter.print(",");
            }
        }
        outWriter.print("]"); // Close locales
    }

    /**
     * TODO New method - document me!
     * 
     * @param request
     * @param callback
     * @param application
     * @param assumedWindow
     * @return
     */
    protected Window doGetApplicationWindow(Request request, Callback callback,
            Application application, Window assumedWindow) {

        Window window = null;

        // If the client knows which window to use, use it if possible
        String windowClientRequestedName = request.getParameter("windowName");

        if (assumedWindow != null
                && application.getWindows().contains(assumedWindow)) {
            windowClientRequestedName = assumedWindow.getName();
        }
        if (windowClientRequestedName != null) {
            window = application.getWindow(windowClientRequestedName);
            if (window != null) {
                return window;
            }
        }

        // If client does not know what window it wants
        if (window == null && !request.isRunningInPortlet()) {
            // This is only supported if the application is running inside a
            // servlet

            // Get the path from URL
            String path = callback.getRequestPathInfo(request);

            /*
             * If the path is specified, create name from it.
             * 
             * An exception is if UIDL request have come this far. This happens
             * if main window is refreshed. In that case we always return main
             * window (infamous hacky support for refreshes if only main window
             * is used). However we are not returning with main window here (we
             * will later if things work right), because the code is so cryptic
             * that nobody really knows what it does.
             */
            boolean pathMayContainWindowName = path != null
                    && path.length() > 0 && !path.equals("/");
            if (pathMayContainWindowName) {
                boolean uidlRequest = path.startsWith("/UIDL");
                if (!uidlRequest) {
                    String windowUrlName = null;
                    if (path.charAt(0) == '/') {
                        path = path.substring(1);
                    }
                    final int index = path.indexOf('/');
                    if (index < 0) {
                        windowUrlName = path;
                        path = "";
                    } else {
                        windowUrlName = path.substring(0, index);
                        path = path.substring(index + 1);
                    }

                    window = application.getWindow(windowUrlName);
                }
            }

        }

        // By default, use mainwindow
        if (window == null) {
            window = application.getMainWindow();
            // Return null if no main window was found
            if (window == null) {
                return null;
            }
        }

        // If the requested window is already open, resolve conflict
        if (currentlyOpenWindowsInClient.containsKey(window.getName())) {
            String newWindowName = window.getName();

            synchronized (AbstractCommunicationManager.class) {
                while (currentlyOpenWindowsInClient.containsKey(newWindowName)) {
                    newWindowName = window.getName() + "_"
                            + nextUnusedWindowSuffix++;
                }
            }

            window = application.getWindow(newWindowName);

            // If everything else fails, use main window even in case of
            // conflicts
            if (window == null) {
                window = application.getMainWindow();
            }
        }

        return window;
    }

    /**
     * Ends the Application.
     * 
     * The browser is redirected to the Application logout URL set with
     * {@link Application#setLogoutURL(String)}, or to the application URL if no
     * logout URL is given.
     * 
     * @param request
     *            the request instance.
     * @param response
     *            the response to write to.
     * @param application
     *            the Application to end.
     * @throws IOException
     *             if the writing failed due to input/output error.
     */
    private void endApplication(Request request, Response response,
            Application application) throws IOException {

        String logoutUrl = application.getLogoutURL();
        if (logoutUrl == null) {
            logoutUrl = application.getURL().toString();
        }
        // clients JS app is still running, send a special json file to tell
        // client that application has quit and where to point browser now
        // Set the response type
        final OutputStream out = response.getOutputStream();
        final PrintWriter outWriter = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(out, "UTF-8")));
        openJsonMessage(outWriter, response);
        outWriter.print("\"redirect\":{");
        outWriter.write("\"url\":\"" + logoutUrl + "\"}");
        closeJsonMessage(outWriter);
        outWriter.flush();
        outWriter.close();
        out.flush();
    }

    protected void closeJsonMessage(PrintWriter outWriter) {
        outWriter.print("}]");
    }

    /**
     * Writes the opening of JSON message to be sent to client.
     * 
     * @param outWriter
     * @param response
     */
    protected void openJsonMessage(PrintWriter outWriter, Response response) {
        // Sets the response type
        response.setContentType("application/json; charset=UTF-8");
        // some dirt to prevent cross site scripting
        outWriter.print("for(;;);[{");
    }

    /**
     * Gets the Paintable Id. If Paintable has debug id set it will be used
     * prefixed with "PID_S". Otherwise a sequenced ID is created.
     * 
     * @param paintable
     * @return the paintable Id.
     */
    public String getPaintableId(Paintable paintable) {

        String id = paintableIdMap.get(paintable);
        if (id == null) {
            // use testing identifier as id if set
            String debugId = paintable.getDebugId();
            if (debugId == null) {
                id = "PID" + idSequence++;
            } else {
                id = "PID_S" + debugId;
                for (int seq = 0;; ++seq) {
                    // In case of a duplicate debug id, uniquify the PID by
                    // inserting a sequential integer. Try successive numbers
                    // until finding a PID that is either not used at all or
                    // used by a detached component. See #5109.
                    Paintable old = idPaintableMap.get(id);
                    if (old == null
                            || (old instanceof Component && ((Component) old)
                                    .getApplication() == null)) {
                        break;
                    }
                    id = "PID_" + seq + "S" + debugId;
                }
            }
            idPaintableMap.put(id, paintable);
            paintableIdMap.put(paintable, id);
        }
        return id;
    }

    public boolean hasPaintableId(Paintable paintable) {
        return paintableIdMap.containsKey(paintable);
    }

    /**
     * Returns dirty components which are in given window. Components in an
     * invisible subtrees are omitted.
     * 
     * @param w
     *            root window for which dirty components is to be fetched
     * @return
     */
    private ArrayList<Paintable> getDirtyVisibleComponents(Window w) {
        final ArrayList<Paintable> resultset = new ArrayList<Paintable>(
                dirtyPaintables);

        // The following algorithm removes any components that would be painted
        // as a direct descendant of other components from the dirty components
        // list. The result is that each component should be painted exactly
        // once and any unmodified components will be painted as "cached=true".

        for (final Iterator<Paintable> i = dirtyPaintables.iterator(); i
                .hasNext();) {
            final Paintable p = i.next();
            if (p instanceof Component) {
                final Component component = (Component) p;
                if (component.getApplication() == null) {
                    // component is detached after requestRepaint is called
                    resultset.remove(p);
                    i.remove();
                } else {
                    Window componentsRoot = component.getWindow();
                    if (componentsRoot == null) {
                        // This should not happen unless somebody has overriden
                        // getApplication or getWindow in an illegal way.
                        throw new IllegalStateException(
                                "component.getWindow() returned null for a component attached to the application");
                    }
                    if (componentsRoot.getParent() != null) {
                        // this is a subwindow
                        componentsRoot = componentsRoot.getParent();
                    }
                    if (componentsRoot != w) {
                        resultset.remove(p);
                    } else if (component.getParent() != null
                            && !component.getParent().isVisible()) {
                        /*
                         * Do not return components in an invisible subtree.
                         * 
                         * Components that are invisible in visible subree, must
                         * be rendered (to let client know that they need to be
                         * hidden).
                         */
                        resultset.remove(p);
                    }
                }
            }
        }

        return resultset;
    }

    /**
     * @see com.vaadin.terminal.Paintable.RepaintRequestListener#repaintRequested(com.vaadin.terminal.Paintable.RepaintRequestEvent)
     */
    public void repaintRequested(RepaintRequestEvent event) {
        final Paintable p = event.getPaintable();
        if (!dirtyPaintables.contains(p)) {
            dirtyPaintables.add(p);
        }
    }

    /**
     * Internally mark a {@link Paintable} as painted and start collecting new
     * repaint requests for it.
     * 
     * @param paintable
     */
    private void paintablePainted(Paintable paintable) {
        dirtyPaintables.remove(paintable);
        paintable.requestRepaintRequests();
    }

    /**
     * Implementation of {@link URIHandler.ErrorEvent} interface.
     */
    public class URIHandlerErrorImpl implements URIHandler.ErrorEvent,
            Serializable {

        private final URIHandler owner;

        private final Throwable throwable;

        /**
         * 
         * @param owner
         * @param throwable
         */
        private URIHandlerErrorImpl(URIHandler owner, Throwable throwable) {
            this.owner = owner;
            this.throwable = throwable;
        }

        /**
         * @see com.vaadin.terminal.Terminal.ErrorEvent#getThrowable()
         */
        public Throwable getThrowable() {
            return throwable;
        }

        /**
         * @see com.vaadin.terminal.URIHandler.ErrorEvent#getURIHandler()
         */
        public URIHandler getURIHandler() {
            return owner;
        }
    }

    /**
     * Queues a locale to be sent to the client (browser) for date and time
     * entry etc. All locale specific information is derived from server-side
     * {@link Locale} instances and sent to the client when needed, eliminating
     * the need to use the {@link Locale} class and all the framework behind it
     * on the client.
     * 
     * @see Locale#toString()
     * 
     * @param value
     */
    public void requireLocale(String value) {
        if (locales == null) {
            locales = new ArrayList<String>();
            locales.add(application.getLocale().toString());
            pendingLocalesIndex = 0;
        }
        if (!locales.contains(value)) {
            locales.add(value);
        }
    }

    /**
     * Constructs a {@link Locale} instance to be sent to the client based on a
     * short locale description string.
     * 
     * @see #requireLocale(String)
     * 
     * @param value
     * @return
     */
    private Locale generateLocale(String value) {
        final String[] temp = value.split("_");
        if (temp.length == 1) {
            return new Locale(temp[0]);
        } else if (temp.length == 2) {
            return new Locale(temp[0], temp[1]);
        } else {
            return new Locale(temp[0], temp[1], temp[2]);
        }
    }

    /**
     * Helper method to test if a component contains another
     * 
     * @param parent
     * @param child
     */
    private static boolean isChildOf(Component parent, Component child) {
        Component p = child.getParent();
        while (p != null) {
            if (parent == p) {
                return true;
            }
            p = p.getParent();
        }
        return false;
    }

    protected class InvalidUIDLSecurityKeyException extends
            GeneralSecurityException {

        InvalidUIDLSecurityKeyException(String message) {
            super(message);
        }

    }

    /**
     * Calls the Window URI handler for a request and returns the
     * {@link DownloadStream} returned by the handler.
     * 
     * If the window is the main window of an application, the (deprecated)
     * {@link Application#handleURI(java.net.URL, String)} is called first to
     * handle {@link ApplicationResource}s, and the window handler is only
     * called if it returns null.
     * 
     * @param window
     *            the target window of the request
     * @param request
     *            the request instance
     * @param response
     *            the response to write to
     * @return DownloadStream if the request was handled and further processing
     *         should be suppressed, null otherwise.
     * @see com.vaadin.terminal.URIHandler
     */
    @SuppressWarnings("deprecation")
    protected DownloadStream handleURI(Window window, Request request,
            Response response, Callback callback) {

        String uri = callback.getRequestPathInfo(request);

        // If no URI is available
        if (uri == null) {
            uri = "";
        } else {
            // Removes the leading /
            while (uri.startsWith("/") && uri.length() > 0) {
                uri = uri.substring(1);
            }
        }

        // Handles the uri
        try {
            URL context = application.getURL();
            if (window == application.getMainWindow()) {
                DownloadStream stream = null;
                /*
                 * Application.handleURI run first. Handles possible
                 * ApplicationResources.
                 */
                stream = application.handleURI(context, uri);
                if (stream == null) {
                    stream = window.handleURI(context, uri);
                }
                return stream;
            } else {
                // Resolve the prefix end index
                final int index = uri.indexOf('/');
                if (index > 0) {
                    String prefix = uri.substring(0, index);
                    URL windowContext;
                    windowContext = new URL(context, prefix + "/");
                    final String windowUri = (uri.length() > prefix.length() + 1) ? uri
                            .substring(prefix.length() + 1) : "";
                    return window.handleURI(windowContext, windowUri);
                } else {
                    return null;
                }
            }

        } catch (final Throwable t) {
            application.getErrorHandler().terminalError(
                    new URIHandlerErrorImpl(application, t));
            return null;
        }
    }

    private final HashMap<Class<? extends Paintable>, Integer> typeToKey = new HashMap<Class<? extends Paintable>, Integer>();
    private int nextTypeKey = 0;

    String getTagForType(Class<? extends Paintable> class1) {
        Integer object = typeToKey.get(class1);
        if (object == null) {
            object = nextTypeKey++;
            typeToKey.put(class1, object);
        }
        return object.toString();
    }

    /**
     * Helper class for terminal to keep track of data that client is expected
     * to know.
     * 
     * TODO make customlayout templates (from theme) to be cached here.
     */
    class OpenWindowCache implements Serializable {

        private final Set<Object> res = new HashSet<Object>();

        /**
         * 
         * @param paintable
         * @return true if the given class was added to cache
         */
        boolean cache(Object object) {
            return res.add(object);
        }

        public void clear() {
            res.clear();
        }

    }

    abstract String getStreamVariableTargetUrl(VariableOwner owner,
            String name, StreamVariable value);

    abstract protected void cleanStreamVariable(VariableOwner owner, String name);

    /**
     * Stream that extracts content from another stream until the boundary
     * string is encountered.
     * 
     * Public only for unit tests, should be considered private for all other
     * purposes.
     */
    public static class SimpleMultiPartInputStream extends InputStream {

        /**
         * Counter of how many characters have been matched to boundary string
         * from the stream
         */
        int matchedCount = -1;

        /**
         * Used as pointer when returning bytes after partly matched boundary
         * string.
         */
        int curBoundaryIndex = 0;
        /**
         * The byte found after a "promising start for boundary"
         */
        private int bufferedByte = -1;
        private boolean atTheEnd = false;

        private final char[] boundary;

        private final InputStream realInputStream;

        public SimpleMultiPartInputStream(InputStream realInputStream,
                String boundaryString) {
            boundary = (CRLF + DASHDASH + boundaryString).toCharArray();
            this.realInputStream = realInputStream;
        }

        @Override
        public int read() throws IOException {
            if (atTheEnd) {
                // End boundary reached, nothing more to read
                return -1;
            } else if (bufferedByte >= 0) {
                /* Purge partially matched boundary if there was such */
                return getBuffered();
            } else if (matchedCount != -1) {
                /*
                 * Special case where last "failed" matching ended with first
                 * character from boundary string
                 */
                return matchForBoundary();
            } else {
                int fromActualStream = realInputStream.read();
                if (fromActualStream == -1) {
                    // unexpected end of stream
                    throw new IOException(
                            "The multipart stream ended unexpectedly");
                }
                if (boundary[0] == fromActualStream) {
                    /*
                     * If matches the first character in boundary string, start
                     * checking if the boundary is fetched.
                     */
                    return matchForBoundary();
                }
                return fromActualStream;
            }
        }

        /**
         * Reads the input to expect a boundary string. Expects that the first
         * character has already been matched.
         * 
         * @return -1 if the boundary was matched, else returns the first byte
         *         from boundary
         * @throws IOException
         */
        private int matchForBoundary() throws IOException {
            matchedCount = 0;
            /*
             * Going to "buffered mode". Read until full boundary match or a
             * different character.
             */
            while (true) {
                matchedCount++;
                if (matchedCount == boundary.length) {
                    /*
                     * The whole boundary matched so we have reached the end of
                     * file
                     */
                    atTheEnd = true;
                    return -1;
                }
                int fromActualStream = realInputStream.read();
                if (fromActualStream != boundary[matchedCount]) {
                    /*
                     * Did not find full boundary, cache the mismatching byte
                     * and start returning the partially matched boundary.
                     */
                    bufferedByte = fromActualStream;
                    return getBuffered();
                }
            }
        }

        /**
         * Returns the partly matched boundary string and the byte following
         * that.
         * 
         * @return
         * @throws IOException
         */
        private int getBuffered() throws IOException {
            int b;
            if (matchedCount == 0) {
                // The boundary has been returned, return the buffered byte.
                b = bufferedByte;
                bufferedByte = -1;
                matchedCount = -1;
            } else {
                b = boundary[curBoundaryIndex++];
                if (curBoundaryIndex == matchedCount) {
                    // The full boundary has been returned, remaining is the
                    // char that did not match the boundary.

                    curBoundaryIndex = 0;
                    if (bufferedByte != boundary[0]) {
                        /*
                         * next call for getBuffered will return the
                         * bufferedByte that came after the partial boundary
                         * match
                         */
                        matchedCount = 0;
                    } else {
                        /*
                         * Special case where buffered byte again matches the
                         * boundaryString. This could be the start of the real
                         * end boundary.
                         */
                        matchedCount = 0;
                        bufferedByte = -1;
                    }
                }
            }
            if (b == -1) {
                throw new IOException("The multipart stream ended unexpectedly");
            }
            return b;
        }
    }

    private static final Logger getLogger() {
        return Logger.getLogger(AbstractCommunicationManager.class.getName());
    }
}
