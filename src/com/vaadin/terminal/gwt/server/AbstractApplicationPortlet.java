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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.MimeResponse;
import javax.portlet.PortalContext;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceURL;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import com.vaadin.Application;
import com.vaadin.Application.SystemMessages;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.Terminal;
import com.vaadin.terminal.gwt.client.ApplicationConfiguration;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.ui.Window;

/**
 * Portlet 2.0 base class. This replaces the servlet in servlet/portlet 1.0 deployments and handles various portlet requests from the browser.
 * 
 * TODO Document me!
 * 
 * @author peholmst
 */
public abstract class AbstractApplicationPortlet extends GenericPortlet implements Constants {

	/**
	 * This portlet parameter is used to add styles to the main element. E.g "height:500px" generates a style="height:500px" to the main element.
	 */
	public static final String PORTLET_PARAMETER_STYLE = "style";

	private static final String PORTAL_PARAMETER_VAADIN_THEME = "vaadin.theme";

	public static final String WRITE_AJAX_PAGE_SCRIPT_WIDGETSET_SHOULD_WRITE = "writeAjaxPageScriptWidgetsetShouldWrite";

	// TODO some parts could be shared with AbstractApplicationServlet

	// TODO Can we close the application when the portlet is removed? Do we know
	// when the portlet is removed?

	// TODO What happens when the portlet window is resized? Do we know when the
	// window is resized?

	private Properties applicationProperties;

	private boolean productionMode = false;

	private static String getGateInHTTPHeader(PortletRequest request, String name) {

		String value = null;
		try {
			Method getRealReq = request.getClass().getMethod("getRealRequest");
			HttpServletRequestWrapper origRequest = (HttpServletRequestWrapper) getRealReq.invoke(request);
			value = origRequest.getHeader(name);
		} catch (Exception e) {
			// do nothing - not on GateIn simple-portal
		}
		return value;
	}

	private static String getGateInHTTPRequestParameter(PortletRequest request, String name) {

		String value = null;
		try {
			Method getRealReq = request.getClass().getMethod("getRealRequest");
			HttpServletRequestWrapper origRequest = (HttpServletRequestWrapper) getRealReq.invoke(request);
			value = origRequest.getParameter(name);
		} catch (Exception e) {
			// do nothing - not on GateIn simple-portal
		}
		return value;
	}

	/**
	 * Try to get an HTTP header value from a request using portal specific APIs.
	 * 
	 * @param name
	 *            HTTP header name
	 * @return the value of the header (empty string if defined without a value, null if the parameter is not present or retrieving it failed)
	 */
	private static String getHTTPHeader(PortletRequest request, String name) {

		return getGateInHTTPHeader(request, name);
	}

	/**
	 * Try to get the value of a HTTP request parameter from a portlet request using portal specific APIs. It is not possible to get the HTTP request parameters using the official Portlet 2.0 API.
	 * 
	 * @param name
	 *            HTTP request parameter name
	 * @return the value of the parameter (empty string if parameter defined without a value, null if the parameter is not present or retrieving it failed)
	 */
	private static String getHTTPRequestParameter(PortletRequest request, String name) {

		String value = request.getParameter(name);
		if (value == null) {
			value = getGateInHTTPRequestParameter(request, name);
		}
		return value;
	}

	private static final Logger getLogger() {

		return Logger.getLogger(AbstractApplicationPortlet.class.getName());
	}

	/**
	 * Returns a portal configuration property.
	 * 
	 * Liferay is handled separately as {@link PortalContext#getProperty(String)} does not return portal properties from e.g. portal-ext.properties .
	 * 
	 * @param name
	 * @param context
	 * @return
	 */
	protected static String getPortalProperty(String name, PortalContext context) {

		return context.getProperty(name);
	}

	private void checkCrossSiteProtection() {

		if (getApplicationOrSystemProperty(SERVLET_PARAMETER_DISABLE_XSRF_PROTECTION, "false").equals("true")) {
			/*
			 * Print an information/warning message about running with xsrf protection disabled
			 */
			getLogger().warning(WARNING_XSRF_PROTECTION_DISABLED);
		}
	}

	private void checkProductionMode() {

		// Check if the application is in production mode.
		// We are in production mode if Debug=false or productionMode=true
		if (getApplicationOrSystemProperty(SERVLET_PARAMETER_DEBUG, "true").equals("false")) {
			// "Debug=true" is the old way and should no longer be used
			productionMode = true;
		} else if (getApplicationOrSystemProperty(SERVLET_PARAMETER_PRODUCTION_MODE, "false").equals("true")) {
			// "productionMode=true" is the real way to do it
			productionMode = true;
		}

		if (!productionMode) {
			/* Print an information/warning message about running in debug mode */
			// TODO Maybe we need a different message for portlets?
			getLogger().warning(NOT_PRODUCTION_MODE_INFO);
		}
	}

	/**
	 * Checks that the version reported by the client (widgetset) matches that of the server.
	 * 
	 * @param request
	 */
	private void checkWidgetsetVersion(PortletRequest request) {

		if (!AbstractApplicationServlet.VERSION.equals(getHTTPRequestParameter(request, "wsver"))) {
			getLogger().warning(String.format(WIDGETSET_MISMATCH_INFO, AbstractApplicationServlet.VERSION, getHTTPRequestParameter(request, "wsver")));
		}
	}

	private void closeApplication(Application application, PortletSession session) {

		if (application == null) {
			return;
		}

		application.close();
		if (session != null) {
			PortletApplicationContext2 context = getApplicationContext(session);
			context.removeApplication(application);
		}
	}

	private Application createApplication(PortletRequest request) throws PortletException, MalformedURLException {

		Application newApplication = getNewApplication(request);
		final PortletApplicationContext2 context = getApplicationContext(request.getPortletSession());
		context.addApplication(newApplication, request.getWindowID());
		return newApplication;
	}

	private void endApplication(PortletRequest request, PortletResponse response, Application application) throws IOException {

		final PortletSession session = request.getPortletSession();
		if (session != null) {
			getApplicationContext(session).removeApplication(application);
		}
		// Do not send any redirects when running inside a portlet.
	}

	private Application findApplicationInstance(PortletRequest request, RequestType requestType) throws PortletException, SessionExpiredException, MalformedURLException {

		boolean requestCanCreateApplication = requestCanCreateApplication(request, requestType);

		/* Find an existing application for this request. */
		Application application = getExistingApplication(request, requestCanCreateApplication);

		if (application != null) {
			/*
			 * There is an existing application. We can use this as long as the user not specifically requested to close or restart it.
			 */

			final boolean restartApplication = getHTTPRequestParameter(request, URL_PARAMETER_RESTART_APPLICATION) != null;
			final boolean closeApplication = getHTTPRequestParameter(request, URL_PARAMETER_CLOSE_APPLICATION) != null;

			if (restartApplication) {
				closeApplication(application, request.getPortletSession(false));
				return createApplication(request);
			} else if (closeApplication) {
				closeApplication(application, request.getPortletSession(false));
				return null;
			} else {
				return application;
			}
		}

		// No existing application was found

		if (requestCanCreateApplication) {
			return createApplication(request);
		} else {
			throw new SessionExpiredException();
		}
	}

	/**
	 * Creates and returns a unique ID for the DIV where the application is to be rendered. We need to generate a unique ID because some portals already create a DIV with the portlet's Window ID as the DOM ID.
	 * 
	 * @param request
	 *            PortletRequest
	 * @return the id to use in the DOM
	 */
	private String getApplicationDomId(PortletRequest request) {

		return "v-" + request.getWindowID();
	}

	private Application getExistingApplication(PortletRequest request, boolean allowSessionCreation) throws MalformedURLException, SessionExpiredException {

		final PortletSession session = request.getPortletSession(allowSessionCreation);

		if (session == null) {
			throw new SessionExpiredException();
		}

		PortletApplicationContext2 context = getApplicationContext(session);
		Application application = context.getApplicationForWindowId(request.getWindowID());
		if (application == null) {
			return null;
		}
		if (application.isRunning()) {
			return application;
		}
		// application found but not running
		context.removeApplication(application);

		return null;
	}

	private void handleDownload(DownloadStream stream, ResourceRequest request, ResourceResponse response) throws IOException {

		if (stream.getParameter("Location") != null) {
			response.setProperty(ResourceResponse.HTTP_STATUS_CODE, Integer.toString(HttpServletResponse.SC_MOVED_TEMPORARILY));
			response.setProperty("Location", stream.getParameter("Location"));
			return;
		}

		// Download from given stream
		final InputStream data = stream.getStream();
		if (data != null) {

			OutputStream out = null;
			try {

				// Sets content type
				response.setContentType(stream.getContentType());

				// Sets cache headers
				final long cacheTime = stream.getCacheTime();
				if (cacheTime <= 0) {
					response.setProperty("Cache-Control", "no-cache");
					response.setProperty("Pragma", "no-cache");
					response.setProperty("Expires", "0");
				} else {
					response.setProperty("Cache-Control", "max-age=" + cacheTime / 1000);
					response.setProperty("Expires", "" + System.currentTimeMillis() + cacheTime);
					// Required to apply caching in some Tomcats
					response.setProperty("Pragma", "cache");
				}

				// Copy download stream parameters directly
				// to HTTP headers.
				final Iterator<String> i = stream.getParameterNames();
				if (i != null) {
					while (i.hasNext()) {
						final String param = i.next();
						response.setProperty(param, stream.getParameter(param));
					}
				}

				// suggest local filename from DownloadStream if
				// Content-Disposition
				// not explicitly set
				String contentDispositionValue = stream.getParameter("Content-Disposition");
				if (contentDispositionValue == null) {
					contentDispositionValue = "filename=\"" + stream.getFileName() + "\"";
					response.setProperty("Content-Disposition", contentDispositionValue);
				}

				int bufferSize = stream.getBufferSize();
				if (bufferSize <= 0 || bufferSize > MAX_BUFFER_SIZE) {
					bufferSize = DEFAULT_BUFFER_SIZE;
				}
				final byte[] buffer = new byte[bufferSize];
				int bytesRead = 0;

				out = response.getPortletOutputStream();

				while ((bytesRead = data.read(buffer)) > 0) {
					out.write(buffer, 0, bytesRead);
					out.flush();
				}
			} finally {
				AbstractCommunicationManager.tryToCloseStream(data);
				AbstractCommunicationManager.tryToCloseStream(out);
			}
		}
	}

	/**
	 * Handle a portlet request that is not for static files, UIDL or upload. Also render requests are handled here.
	 * 
	 * This method is called after starting the application and calling portlet and transaction listeners.
	 * 
	 * @param request
	 * @param response
	 * @param requestType
	 * @param application
	 * @param applicationContext
	 * @param applicationManager
	 * @throws PortletException
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	private void handleOtherRequest(PortletRequest request, PortletResponse response, RequestType requestType, Application application, Window window, PortletApplicationContext2 applicationContext, PortletCommunicationManager applicationManager) throws PortletException, IOException, MalformedURLException {

		if (window == null) {
			throw new PortletException(ERROR_NO_WINDOW_FOUND);
		}

		/*
		 * Sets terminal type for the window, if not already set
		 */
		if (window.getTerminal() == null) {
			window.setTerminal(applicationContext.getBrowser());
		}

		/*
		 * Handle parameters
		 */
		final Map<String, String[]> parameters = request.getParameterMap();
		if (window != null && parameters != null) {
			window.handleParameters(parameters);
		}

		if (requestType == RequestType.APPLICATION_RESOURCE) {
			handleURI(applicationManager, window, (ResourceRequest) request, (ResourceResponse) response);
		} else if (requestType == RequestType.RENDER) {
			writeAjaxPage((RenderRequest) request, (RenderResponse) response, window, application);
		} else if (requestType == RequestType.EVENT) {
			// nothing to do, listeners do all the work
		} else if (requestType == RequestType.ACTION) {
			// nothing to do, listeners do all the work
		} else {
			throw new IllegalStateException("handleRequest() without anything to do - should never happen!");
		}
	}

	private void handleServiceException(PortletRequest request, PortletResponse response, Application application, Throwable e) throws IOException, PortletException {

		// TODO Check that this error handler is working when running inside a
		// portlet

		// if this was an UIDL request, response UIDL back to client
		if (getRequestType(request) == RequestType.UIDL) {
			Application.SystemMessages ci = getSystemMessages();
			criticalNotification(request, (ResourceResponse) response, ci.getInternalErrorCaption(), ci.getInternalErrorMessage(), null, ci.getInternalErrorURL());
			if (application != null) {
				application.getErrorHandler().terminalError(new RequestError(e));
			} else {
				throw new PortletException(e);
			}
		} else {
			// Re-throw other exceptions
			throw new PortletException(e);
		}

	}

	private void handleUnknownRequest(PortletRequest request, PortletResponse response) {

		getLogger().warning("Unknown request type");
	}

	private boolean handleURI(PortletCommunicationManager applicationManager, Window window, ResourceRequest request, ResourceResponse response) throws IOException {

		// Handles the URI
		DownloadStream download = applicationManager.handleURI(window, request, response, this);

		// A download request
		if (download != null) {
			// Client downloads an resource
			handleDownload(download, request, response);
			return true;
		}

		return false;
	}

	private boolean isApplicationResourceRequest(ResourceRequest request) {

		return request.getResourceID() != null && request.getResourceID().startsWith("APP");
	}

	private boolean isDummyRequest(ResourceRequest request) {

		return request.getResourceID() != null && request.getResourceID().equals("DUMMY");
	}

	private boolean isFileUploadRequest(ResourceRequest request) {

		return "UPLOAD".equals(request.getResourceID());
	}

	private boolean isRepaintAll(PortletRequest request) {

		return request.getParameter(URL_PARAMETER_REPAINT_ALL) != null && request.getParameter(URL_PARAMETER_REPAINT_ALL).equals("1");
	}

	private boolean isUIDLRequest(ResourceRequest request) {

		return request.getResourceID() != null && request.getResourceID().equals("UIDL");
	}

	private void startApplication(PortletRequest request, Application application, PortletApplicationContext2 context) throws PortletException, MalformedURLException {

		if (!application.isRunning()) {
			Locale locale = request.getLocale();
			application.setLocale(locale);
			// No application URL when running inside a portlet
			application.start(null, applicationProperties, context);
		}
	}

	private void updateBrowserProperties(WebBrowser browser, PortletRequest request) {

		String userAgent = getHTTPHeader(request, "user-agent");
		browser.updateRequestDetails(request.getLocale(), null, request.isSecure(), userAgent);
		if (getHTTPRequestParameter(request, "repaintAll") != null) {
			browser.updateClientSideDetails(getHTTPRequestParameter(request, "sw"), getHTTPRequestParameter(request, "sh"), getHTTPRequestParameter(request, "tzo"), getHTTPRequestParameter(request, "rtzo"), getHTTPRequestParameter(request, "dstd"), getHTTPRequestParameter(request, "dstActive"), getHTTPRequestParameter(request, "curdate"), getHTTPRequestParameter(request, "td") != null);
		}
	}

	/**
	 * Send notification to client's application. Used to notify client of critical errors and session expiration due to long inactivity. Server has no knowledge of what application client refers to.
	 * 
	 * @param request
	 *            the Portlet request instance.
	 * @param response
	 *            the Portlet response to write to.
	 * @param caption
	 *            for the notification
	 * @param message
	 *            for the notification
	 * @param details
	 *            a detail message to show in addition to the passed message. Currently shown directly but could be hidden behind a details drop down.
	 * @param url
	 *            url to load after message, null for current page
	 * @throws IOException
	 *             if the writing failed due to input/output error.
	 */
	void criticalNotification(PortletRequest request, MimeResponse response, String caption, String message, String details, String url) throws IOException {

		// clients JS app is still running, but server application either
		// no longer exists or it might fail to perform reasonably.
		// send a notification to client's application and link how
		// to "restart" application.

		if (caption != null) {
			caption = "\"" + caption + "\"";
		}
		if (details != null) {
			if (message == null) {
				message = details;
			} else {
				message += "<br/><br/>" + details;
			}
		}
		if (message != null) {
			message = "\"" + message + "\"";
		}
		if (url != null) {
			url = "\"" + url + "\"";
		}

		// Set the response type
		response.setContentType("application/json; charset=UTF-8");
		final OutputStream out = response.getPortletOutputStream();
		final PrintWriter outWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out, "UTF-8")));
		outWriter.print("for(;;);[{\"changes\":[], \"meta\" : {" + "\"appError\": {" + "\"caption\":" + caption + "," + "\"message\" : " + message + "," + "\"url\" : " + url + "}}, \"resources\": {}, \"locales\":[]}]");
		outWriter.close();
	}

	boolean requestCanCreateApplication(PortletRequest request, RequestType requestType) {

		if (requestType == RequestType.UIDL && isRepaintAll(request)) {
			return true;
		} else if (requestType == RequestType.RENDER) {
			// In most cases the first request is a render request that renders
			// the HTML fragment. This should create an application instance.
			return true;
		} else if (requestType == RequestType.EVENT) {
			// A portlet can also be sent an event even though it has not been
			// rendered, e.g. portlet on one page sends an event to a portlet on
			// another page and then moves the user to that page.
			return true;
		}
		return false;
	}

	@Override
	protected void doDispatch(RenderRequest request, RenderResponse response) throws PortletException, IOException {

		try {
			// try to let super handle - it'll call methods annotated for
			// handling, the default doXYZ(), or throw if a handler for the mode
			// is not found
			super.doDispatch(request, response);

		} catch (PortletException e) {
			if (e.getCause() == null) {
				// No cause interpreted as 'unknown mode' - pass that trough
				// so that the application can handle
				handleRequest(request, response);

			} else {
				// Something else failed, pass on
				throw e;
			}
		}
	}

	protected abstract Class<? extends Application> getApplicationClass() throws ClassNotFoundException;

	/**
	 * 
	 * Gets the application context for a PortletSession. If no context is currently stored in a session a new context is created and stored in the session.
	 * 
	 * @param portletSession
	 *            the portlet session.
	 * @return the application context for the session.
	 */
	protected PortletApplicationContext2 getApplicationContext(PortletSession portletSession) {

		return PortletApplicationContext2.getApplicationContext(portletSession);
	}

	/**
	 * Gets an application or system property value.
	 * 
	 * @param parameterName
	 *            the Name or the parameter.
	 * @param defaultValue
	 *            the Default to be used.
	 * @return String value or default if not found
	 */
	protected String getApplicationOrSystemProperty(String parameterName, String defaultValue) {

		String val = null;

		// Try application properties
		val = getApplicationProperty(parameterName);
		if (val != null) {
			return val;
		}

		// Try system properties
		val = getSystemProperty(parameterName);
		if (val != null) {
			return val;
		}

		return defaultValue;
	}

	/**
	 * Gets an application property value.
	 * 
	 * @param parameterName
	 *            the Name or the parameter.
	 * @return String value or null if not found
	 */
	protected String getApplicationProperty(String parameterName) {

		String val = applicationProperties.getProperty(parameterName);
		if (val != null) {
			return val;
		}

		// Try lower case application properties for backward compatibility with
		// 3.0.2 and earlier
		val = applicationProperties.getProperty(parameterName.toLowerCase());

		return val;
	}

	protected ClassLoader getClassLoader() throws PortletException {

		// TODO Add support for custom class loader
		return getClass().getClassLoader();
	}

	protected Application getNewApplication(PortletRequest request) throws PortletException {

		try {
			final Application application = getApplicationClass().newInstance();
			return application;
		} catch (final IllegalAccessException e) {
			throw new PortletException("getNewApplication failed", e);
		} catch (final InstantiationException e) {
			throw new PortletException("getNewApplication failed", e);
		} catch (final ClassNotFoundException e) {
			throw new PortletException("getNewApplication failed", e);
		}
	}

	/**
	 * Returns a message printed for browsers without scripting support or if browsers scripting support is disabled.
	 */
	protected String getNoScriptMessage() {

		return "You have to enable javascript in your browser to use an application built with Vaadin.";
	}

	protected RequestType getRequestType(PortletRequest request) {

		if (request instanceof RenderRequest) {
			return RequestType.RENDER;
		} else if (request instanceof ResourceRequest) {
			if (isUIDLRequest((ResourceRequest) request)) {
				return RequestType.UIDL;
			} else if (isFileUploadRequest((ResourceRequest) request)) {
				return RequestType.FILE_UPLOAD;
			} else if (isApplicationResourceRequest((ResourceRequest) request)) {
				return RequestType.APPLICATION_RESOURCE;
			} else if (isDummyRequest((ResourceRequest) request)) {
				return RequestType.DUMMY;
			} else {
				// these are not served with ResourceRequests, but by a servlet
				// on the portal at portlet root path (configured by default by
				// Liferay at deployment time, similar on other portals)
				return RequestType.STATIC_FILE;
			}
		} else if (request instanceof ActionRequest) {
			return RequestType.ACTION;
		} else if (request instanceof EventRequest) {
			return RequestType.EVENT;
		}
		return RequestType.UNKNOWN;
	}

	/**
	 * Return the URL from where static files, e.g. the widgetset and the theme, are served. In a standard configuration the VAADIN folder inside the returned folder is what is used for widgetsets and themes.
	 * 
	 * @param request
	 * @return The location of static resources (inside which there should be a VAADIN directory). Does not end with a slash (/).
	 */
	protected String getStaticFilesLocation(PortletRequest request) {

		// TODO allow overriding on portlet level?
		String staticFileLocation = getPortalProperty(Constants.PORTAL_PARAMETER_VAADIN_RESOURCE_PATH, request.getPortalContext());
		if (staticFileLocation != null) {
			// remove trailing slash if any
			while (staticFileLocation.endsWith(".")) {
				staticFileLocation = staticFileLocation.substring(0, staticFileLocation.length() - 1);
			}
			return staticFileLocation;
		} else {
			// default for Liferay
			return "/html";
		}
	}

	/**
	 * Get system messages from the current application class
	 * 
	 * @return
	 */
	protected SystemMessages getSystemMessages() {

		try {
			Class<? extends Application> appCls = getApplicationClass();
			Method m = appCls.getMethod("getSystemMessages", (Class[]) null);
			return (Application.SystemMessages) m.invoke(null, (Object[]) null);
		} catch (ClassNotFoundException e) {
			// This should never happen
			throw new SystemMessageException(e);
		} catch (SecurityException e) {
			throw new SystemMessageException("Application.getSystemMessage() should be static public", e);
		} catch (NoSuchMethodException e) {
			// This is completely ok and should be silently ignored
		} catch (IllegalArgumentException e) {
			// This should never happen
			throw new SystemMessageException(e);
		} catch (IllegalAccessException e) {
			throw new SystemMessageException("Application.getSystemMessage() should be static public", e);
		} catch (InvocationTargetException e) {
			// This should never happen
			throw new SystemMessageException(e);
		}
		return Application.getSystemMessages();
	}

	/**
	 * Gets an system property value.
	 * 
	 * @param parameterName
	 *            the Name or the parameter.
	 * @return String value or null if not found
	 */
	protected String getSystemProperty(String parameterName) {

		String val = null;

		String pkgName;
		final Package pkg = getClass().getPackage();
		if (pkg != null) {
			pkgName = pkg.getName();
		} else {
			final String className = getClass().getName();
			pkgName = new String(className.toCharArray(), 0, className.lastIndexOf('.'));
		}
		val = System.getProperty(pkgName + "." + parameterName);
		if (val != null) {
			return val;
		}

		// Try lowercased system properties
		val = System.getProperty(pkgName + "." + parameterName.toLowerCase());
		return val;
	}

	/**
	 * Returns the theme for given request/window
	 * 
	 * @param request
	 * @param window
	 * @return
	 */
	protected String getThemeForWindow(PortletRequest request, Window window) {

		// Finds theme name
		String themeName;

		// theme defined for the window?
		themeName = window.getTheme();

		if (themeName == null) {
			// no, is the default theme defined by the portal?
			themeName = getPortalProperty(Constants.PORTAL_PARAMETER_VAADIN_THEME, request.getPortalContext());
		}

		if (themeName == null) {
			// no, using the default theme defined by Vaadin
			themeName = DEFAULT_THEME_NAME;
		}

		return themeName;
	}

	/**
	 * Returns the theme URI for the named theme on the portal.
	 * 
	 * Note that this is not the only location referring to the theme URI - also e.g. PortletCommunicationManager uses its own way to access the portlet 2.0 theme resources.
	 * 
	 * @param themeName
	 * @param request
	 * @return
	 */
	protected String getThemeURI(String themeName, PortletRequest request) {

		return getStaticFilesLocation(request) + "/" + THEME_DIRECTORY_PATH + themeName;
	}

	/**
	 * Returns the configuration parameters to pass to the client.
	 * 
	 * To add configuration parameters for the client, override, call the super method and then modify the map. Overriding this method may also require client side changes in {@link ApplicationConnection} and {@link ApplicationConfiguration}.
	 * 
	 * Note that this method must escape and quote the values when appropriate.
	 * 
	 * The map returned is typically a {@link LinkedHashMap} to preserve insertion order, but it is not guaranteed to be one.
	 * 
	 * @param request
	 * @param response
	 * @param application
	 * @param themeURI
	 * @return modifiable Map from parameter name to its full value
	 * @throws PortletException
	 */
	protected Map<String, String> getVaadinConfigurationMap(RenderRequest request, RenderResponse response, Application application, String themeURI) throws PortletException {

		Map<String, String> config = new LinkedHashMap<String, String>();

		/*
		 * We need this in order to get uploads to work. TODO this is not needed for uploads anymore, check if this is needed for some other things
		 */
		PortletURL appUri = response.createActionURL();
		config.put("appUri", "'" + appUri.toString() + "'");
		config.put("usePortletURLs", "true");
		ResourceURL uidlUrlBase = response.createResourceURL();
		uidlUrlBase.setResourceID("UIDL");
		config.put("portletUidlURLBase", "'" + uidlUrlBase.toString() + "'");
		config.put("pathInfo", "''");
		config.put("themeUri", "'" + themeURI + "'");

		String versionInfo = "{vaadinVersion:\"" + AbstractApplicationServlet.VERSION + "\",applicationVersion:\"" + application.getVersion() + "\"}";
		config.put("versionInfo", versionInfo);

		// Get system messages
		Application.SystemMessages systemMessages = null;
		try {
			systemMessages = getSystemMessages();
		} catch (SystemMessageException e) {
			// failing to get the system messages is always a problem
			throw new PortletException("Failed to obtain system messages!", e);
		}
		if (systemMessages != null) {
			// Write the CommunicationError -message to client
			String caption = systemMessages.getCommunicationErrorCaption();
			if (caption != null) {
				caption = "\"" + caption + "\"";
			}
			String message = systemMessages.getCommunicationErrorMessage();
			if (message != null) {
				message = "\"" + message + "\"";
			}
			String url = systemMessages.getCommunicationErrorURL();
			if (url != null) {
				url = "\"" + url + "\"";
			}

			config.put("\"comErrMsg\"", "{" + "\"caption\":" + caption + "," + "\"message\" : " + message + "," + "\"url\" : " + url + "}");

			// Write the AuthenticationError -message to client
			caption = systemMessages.getAuthenticationErrorCaption();
			if (caption != null) {
				caption = "\"" + caption + "\"";
			}
			message = systemMessages.getAuthenticationErrorMessage();
			if (message != null) {
				message = "\"" + message + "\"";
			}
			url = systemMessages.getAuthenticationErrorURL();
			if (url != null) {
				url = "\"" + url + "\"";
			}

			config.put("\"authErrMsg\"", "{" + "\"caption\":" + caption + "," + "\"message\" : " + message + "," + "\"url\" : " + url + "}");
		}

		return config;
	}

	/**
	 * Returns the URL from which the widgetset is served on the portal.
	 * 
	 * @param request
	 * @return
	 */
	protected String getWidgetsetURL(RenderRequest request) {

		String requestWidgetset = getApplicationOrSystemProperty(PARAMETER_WIDGETSET, null);
		String sharedWidgetset = getPortalProperty(PORTAL_PARAMETER_VAADIN_WIDGETSET, request.getPortalContext());

		String widgetset;
		if (requestWidgetset != null) {
			widgetset = requestWidgetset;
		} else if (sharedWidgetset != null) {
			widgetset = sharedWidgetset;
		} else {
			widgetset = DEFAULT_WIDGETSET;
		}
		String widgetsetURL = getWidgetsetURL(widgetset, request);
		return widgetsetURL;
	}

	/**
	 * Returns the URL from which the widgetset is served on the portal.
	 * 
	 * @param widgetset
	 * @param request
	 * @return
	 */
	protected String getWidgetsetURL(String widgetset, PortletRequest request) {

		return getStaticFilesLocation(request) + "/" + WIDGETSET_DIRECTORY_PATH + widgetset + "/" + widgetset + ".nocache.js?" + new Date().getTime();
	}

	protected void handleRequest(PortletRequest request, PortletResponse response) throws PortletException, IOException {

		RequestTimer requestTimer = new RequestTimer();
		requestTimer.start();

		RequestType requestType = getRequestType(request);

		if (requestType == RequestType.UNKNOWN) {
			handleUnknownRequest(request, response);
		} else if (requestType == RequestType.DUMMY) {
			/*
			 * This dummy page is used by action responses to redirect to, in order to prevent the boot strap code from being rendered into strange places such as iframes.
			 */
			((ResourceResponse) response).setContentType("text/html");
			final OutputStream out = ((ResourceResponse) response).getPortletOutputStream();
			final PrintWriter outWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out, "UTF-8")));
			outWriter.print("<html><body>dummy page</body></html>");
			outWriter.close();
		} else {
			Application application = null;
			boolean transactionStarted = false;
			boolean requestStarted = false;

			try {
				// TODO What about PARAM_UNLOADBURST & redirectToApplication??

				/* Find out which application this request is related to */
				application = findApplicationInstance(request, requestType);
				if (application == null) {
					return;
				}

				/*
				 * Get or create an application context and an application manager for the session
				 */
				PortletApplicationContext2 applicationContext = getApplicationContext(request.getPortletSession());
				applicationContext.setResponse(response);
				applicationContext.setPortletConfig(getPortletConfig());

				PortletCommunicationManager applicationManager = applicationContext.getApplicationManager(application);

				/* Update browser information from request */
				updateBrowserProperties(applicationContext.getBrowser(), request);

				/*
				 * Call application requestStart before Application.init() is called (bypasses the limitation in TransactionListener)
				 */
				if (application instanceof PortletRequestListener) {
					((PortletRequestListener) application).onRequestStart(request, response);
					requestStarted = true;
				}

				/* Start the newly created application */
				startApplication(request, application, applicationContext);

				/*
				 * Transaction starts. Call transaction listeners. Transaction end is called in the finally block below.
				 */
				applicationContext.startTransaction(application, request);
				transactionStarted = true;

				/* Notify listeners */

				// Finds the window within the application
				Window window = null;
				synchronized (application) {
					if (application.isRunning()) {
						switch (requestType) {
							case FILE_UPLOAD:
								// no window
								break;
							case APPLICATION_RESOURCE:
								// use main window - should not need any window
								window = application.getMainWindow();
								break;
							default:
								window = applicationManager.getApplicationWindow(request, this, application, null);
						}
						// if window not found, not a problem - use null
					}
				}

				// TODO Should this happen before or after the transaction
				// starts?
				if (request instanceof RenderRequest) {
					applicationContext.firePortletRenderRequest(application, window, (RenderRequest) request, (RenderResponse) response);
				} else if (request instanceof ActionRequest) {
					applicationContext.firePortletActionRequest(application, window, (ActionRequest) request, (ActionResponse) response);
				} else if (request instanceof EventRequest) {
					applicationContext.firePortletEventRequest(application, window, (EventRequest) request, (EventResponse) response);
				} else if (request instanceof ResourceRequest) {
					applicationContext.firePortletResourceRequest(application, window, (ResourceRequest) request, (ResourceResponse) response);
				}

				/* Handle the request */
				if (requestType == RequestType.FILE_UPLOAD) {
					applicationManager.handleFileUpload((ResourceRequest) request, (ResourceResponse) response);
					return;
				} else if (requestType == RequestType.UIDL) {
					// Handles AJAX UIDL requests
					if (isRepaintAll(request)) {
						// warn if versions do not match
						checkWidgetsetVersion(request);
					}
					applicationManager.handleUidlRequest((ResourceRequest) request, (ResourceResponse) response, this, window);
					return;
				} else {
					/*
					 * Removes the application if it has stopped
					 */
					if (!application.isRunning()) {
						endApplication(request, response, application);
						return;
					}

					handleOtherRequest(request, response, requestType, application, window, applicationContext, applicationManager);
				}
			} catch (final SessionExpiredException e) {
				// TODO Figure out a better way to deal with
				// SessionExpiredExceptions
				getLogger().finest("A user session has expired");
			} catch (final GeneralSecurityException e) {
				// TODO Figure out a better way to deal with
				// GeneralSecurityExceptions
				getLogger().fine("General security exception, the security key was probably incorrect.");
			} catch (final Throwable e) {
				handleServiceException(request, response, application, e);
			} finally {
				// Notifies transaction end
				try {
					if (transactionStarted) {
						((PortletApplicationContext2) application.getContext()).endTransaction(application, request);
					}
				} finally {
					try {
						if (requestStarted) {
							((PortletRequestListener) application).onRequestEnd(request, response);

						}
					} finally {
						PortletSession session = request.getPortletSession(false);
						if (session != null) {
							requestTimer.stop(getApplicationContext(session));
						}
					}
				}
			}
		}
	}

	/**
	 * Checks whether the widgetset should be loaded directly in the generated HTML or indirectly using <code>document.write</code>.
	 * <p>
	 * In the normal case, indirect loading using <code>document.write</code> should be used as it ensures that the widgetset is only loaded once on each page. Usage of <code>document.write</code> does however cause problems with GWT Super Dev Mode and various other techniques that depend on loading scripts in a special order (see <a href= "http://dev.vaadin.com/ticket/8924">#8924</a>). These cases can be supported by using the direct loading technique instead, even though that way of loading the widgetset can cause race conditions if a page contains multiple portlets using the same widgetset (see <a href="http://dev.vaadin.com/ticket/9774">#9774</a>).
	 * <p>
	 * For the default indirect loading, {@link #writeAjaxPageScriptWidgetset(RenderRequest, RenderResponse, BufferedWriter)} is adding the <code>document.write</code> code. The direct loading instead uses {@link #writeAjaxPageWidgetset(RenderRequest, BufferedWriter)} to create HTML that loads the widgetset.
	 * <p>
	 * By default, indirect loading is used.
	 * 
	 * @param request
	 *            the render request for which the widgetset loading method should be determined
	 * 
	 * @return <code>true</code> to use direct widgetset loading, <code>false</code> to use the default indirect widgetset loading.
	 */
	protected boolean usesDirectWidgetsetLoad(RenderRequest request) {

		return false;
	}

	/**
	 * Writes the html host page (aka kickstart page) that starts the actual Vaadin application.
	 * 
	 * If one needs to override parts of the portlet HTML contents creation, it is suggested that one overrides one of several submethods including:
	 * <ul>
	 * <li>
	 * {@link #writeAjaxPageHtmlMainDiv(RenderRequest, RenderResponse, BufferedWriter, String)}
	 * <li>
	 * {@link #getVaadinConfigurationMap(RenderRequest, RenderResponse, Application, String)}
	 * <li>
	 * {@link #writeAjaxPageHtmlVaadinScripts(RenderRequest, RenderResponse, BufferedWriter, Application, String)}
	 * </ul>
	 * 
	 * @param request
	 *            the portlet request.
	 * @param response
	 *            the portlet response to write to.
	 * @param window
	 * @param application
	 * @throws IOException
	 *             if the writing failed due to input/output error.
	 * @throws MalformedURLException
	 *             if the application is denied access the persistent data store represented by the given URL.
	 * @throws PortletException
	 */
	protected void writeAjaxPage(RenderRequest request, RenderResponse response, Window window, Application application) throws IOException, MalformedURLException, PortletException {

		response.setContentType("text/html");
		final BufferedWriter page = new BufferedWriter(new OutputStreamWriter(response.getPortletOutputStream(), "UTF-8"));

		// TODO Currently, we can only load widgetsets and themes from the
		// portal

		String themeName = getThemeForWindow(request, window);

		writeAjaxPageHtmlVaadinScripts(request, response, page, application, themeName);

		/*- Add classnames;
		 *      .v-app
		 *      .v-app-loading
		 *      .v-app-<simpleName for app class>
		 *      .v-theme-<themeName, remove non-alphanum>
		 */
		String appClass = "v-app-";
		try {
			appClass += getApplicationClass().getSimpleName();
		} catch (ClassNotFoundException e) {
			appClass += "unknown";
			getLogger().log(Level.SEVERE, "Could not find application class", e);
		}
		String themeClass = "v-theme-" + themeName.replaceAll("[^a-zA-Z0-9]", "");

		String classNames = "v-app " + themeClass + " " + appClass;

		String style = getApplicationProperty(PORTLET_PARAMETER_STYLE);
		String divStyle = "";
		if (style != null) {
			divStyle = "style=\"" + style + "\"";
		}

		writeAjaxPageHtmlMainDiv(request, response, page, getApplicationDomId(request), classNames, divStyle);

		page.close();
	}

	/**
	 * Method to write the div element into which that actual Vaadin application is rendered.
	 * <p>
	 * Override this method if you want to add some custom html around around the div element into which the actual Vaadin application will be rendered.
	 * 
	 * @param request
	 * @param response
	 * @param writer
	 * @param id
	 * @param classNames
	 * @param divStyle
	 * @throws IOException
	 */
	protected void writeAjaxPageHtmlMainDiv(RenderRequest request, RenderResponse response, final BufferedWriter writer, String id, String classNames, String divStyle) throws IOException {

		writer.write("<div id=\"" + id + "\" class=\"" + classNames + "\" " + divStyle + ">");
		writer.write("<div class=\"v-app-loading\"></div>");
		writer.write("</div>\n");
		writer.write("<noscript>" + getNoScriptMessage() + "</noscript>");
	}

	/**
	 * Writes the Vaadin theme loading section of the portlet HTML. Loads both the portal theme and the portlet theme in this order, skipping loading of themes that are already loaded (matched by name).
	 * 
	 * @param request
	 * @param writer
	 * @param themeName
	 * @param themeURI
	 * @param portalTheme
	 * @throws IOException
	 */
	protected void writeAjaxPageHtmlTheme(RenderRequest request, final BufferedWriter writer, String themeName, String themeURI, String portalTheme) throws IOException {

		writer.write("<script type=\"text/javascript\">\n");

		if (portalTheme == null) {
			portalTheme = DEFAULT_THEME_NAME;
		}

		writer.write("if(!vaadin.themesLoaded['" + portalTheme + "']) {\n");
		writer.write("var defaultStylesheet = document.createElement('link');\n");
		writer.write("defaultStylesheet.setAttribute('rel', 'stylesheet');\n");
		writer.write("defaultStylesheet.setAttribute('type', 'text/css');\n");
		writer.write("defaultStylesheet.setAttribute('href', '" + getThemeURI(portalTheme, request) + "/styles.css');\n");
		writer.write("document.getElementsByTagName('head')[0].appendChild(defaultStylesheet);\n");
		writer.write("vaadin.themesLoaded['" + portalTheme + "'] = true;\n}\n");

		if (!portalTheme.equals(themeName)) {
			writer.write("if(!vaadin.themesLoaded['" + themeName + "']) {\n");
			writer.write("var stylesheet = document.createElement('link');\n");
			writer.write("stylesheet.setAttribute('rel', 'stylesheet');\n");
			writer.write("stylesheet.setAttribute('type', 'text/css');\n");
			writer.write("stylesheet.setAttribute('href', '" + themeURI + "/styles.css');\n");
			writer.write("document.getElementsByTagName('head')[0].appendChild(stylesheet);\n");
			writer.write("vaadin.themesLoaded['" + themeName + "'] = true;\n}\n");
		}

		writer.write("</script>\n");
	}

	/**
	 * This method writes the scripts to load the widgetset and the themes as well as define Vaadin configuration parameters on the HTML fragment that starts the actual Vaadin application.
	 * 
	 * @param request
	 * @param response
	 * @param writer
	 * @param application
	 * @param themeName
	 * @throws IOException
	 * @throws PortletException
	 */
	protected void writeAjaxPageHtmlVaadinScripts(RenderRequest request, RenderResponse response, final BufferedWriter writer, Application application, String themeName) throws IOException, PortletException {

		String themeURI = getThemeURI(themeName, request);

		// fixed base theme to use - all portal pages with Vaadin
		// applications will load this exactly once
		String portalTheme = getPortalProperty(PORTAL_PARAMETER_VAADIN_THEME, request.getPortalContext());

		writer.write("<script type=\"text/javascript\">\n");
		writer.write("if(!vaadin || !vaadin.vaadinConfigurations) {\n " + "if(!vaadin) { var vaadin = {}} \n" + "vaadin.vaadinConfigurations = {};\n" + "if (!vaadin.themesLoaded) { vaadin.themesLoaded = {}; }\n");
		if (!isProductionMode()) {
			writer.write("vaadin.debug = true;\n");
		}

		writeAjaxPageScriptWidgetset(request, response, writer);

		Map<String, String> config = getVaadinConfigurationMap(request, response, application, themeURI);
		writeAjaxPageScriptConfigurations(request, response, writer, config);

		writer.write("</script>\n");

		writeAjaxPageWidgetset(request, writer);

		writeAjaxPageHtmlTheme(request, writer, themeName, themeURI, portalTheme);

		// TODO Warn if widgetset has not been loaded after 15 seconds
	}

	/**
	 * Constructs the Vaadin configuration section for {@link ApplicationConnection} and {@link ApplicationConfiguration}.
	 * 
	 * Typically this method should not be overridden. Instead, modify {@link #getVaadinConfigurationMap(RenderRequest, RenderResponse, Application, String)} .
	 * 
	 * @param request
	 * @param response
	 * @param writer
	 * @param config
	 * @throws IOException
	 * @throws PortletException
	 */
	protected void writeAjaxPageScriptConfigurations(RenderRequest request, RenderResponse response, final BufferedWriter writer, Map<String, String> config) throws IOException, PortletException {

		writer.write("vaadin.vaadinConfigurations[\"" + getApplicationDomId(request) + "\"] = {");

		Iterator<String> keyIt = config.keySet().iterator();
		while (keyIt.hasNext()) {
			String key = keyIt.next();
			writer.write(key + ": " + config.get(key));
			if (keyIt.hasNext()) {
				writer.write(", ");
			}
		}

		writer.write("};\n");
	}

	/**
	 * Writes the script to load the widgetset on the HTML fragment created by the portlet.
	 * 
	 * @param request
	 * @param response
	 * @param writer
	 * @throws IOException
	 * 
	 */
	protected void writeAjaxPageScriptWidgetset(RenderRequest request, RenderResponse response, final BufferedWriter writer) throws IOException {

		if (usesDirectWidgetsetLoad(request)) {
			/*
			 * Direct widgetset loading has been enabled so let writeAjaxPageWidgetset() write the script even though it might cause loading problems with multiple portlets.
			 */

			// But we still need to close this one block, for compatibility
			writer.append("\n}\n");

			/*
			 * Set a flag so writeAjaxPageWidgetset() knows that this method has not generated the document.write code.
			 */
			request.setAttribute(WRITE_AJAX_PAGE_SCRIPT_WIDGETSET_SHOULD_WRITE, Boolean.TRUE);
		} else {
			String widgetsetURL = getWidgetsetURL(request);
			writer.write("document.write('<iframe tabIndex=\"-1\" id=\"__gwt_historyFrame\" " + "style=\"position:absolute;width:0;height:0;border:0;overflow:" + "hidden;opacity:0;top:-100px;left:-100px;\" src=\"javascript:false\"></iframe>');\n");
			writer.write("document.write(\"<script language='javascript' src='" + widgetsetURL + "'><\\/script>\");\n}\n");
		}
	}

	/**
	 * Writes the script to load the widgetset on the HTML fragment created by the portlet if the request attribute {@value #WRITE_AJAX_PAGE_SCRIPT_WIDGETSET_SHOULD_WRITE} is set to Boolean.TRUE.
	 * <p>
	 * <b>Warning!</b> Loading widgetsets in this may cause a race condition in the browser if there are multiple portlets on the same page (see <a href="http://dev.vaadin.com/ticket/9774">#9774</a>).
	 * <p>
	 * This method doesn't do anything unless {@link #usesDirectWidgetsetLoad(RenderRequest)} has been overridden to return <code>true</code>.
	 * </p>
	 * 
	 * @param request
	 * @param writer
	 * @throws IOException
	 * 
	 */
	protected void writeAjaxPageWidgetset(RenderRequest request, BufferedWriter writer) throws IOException {

		/*
		 * If the writeAjaxPageWidgetset added the code using document.write, we shouldn't do anything here because then the iframe and script tags have already been added.
		 */
		if (request.getAttribute(WRITE_AJAX_PAGE_SCRIPT_WIDGETSET_SHOULD_WRITE) != Boolean.TRUE) {
			return;
		}
		String widgetsetURL = getWidgetsetURL(request);
		writer.write("<iframe tabIndex=\"-1\" id=\"__gwt_historyFrame\" " + "style=\"position:absolute;width:0;height:0;border:0;overflow:" + "hidden;opacity:0;top:-100px;left:-100px;\" src=\"javascript:false\"></iframe>\n");
		writer.write("<script language='javascript' src='" + widgetsetURL + "'></script>\n");
	}

	@Override
	public void init(PortletConfig config) throws PortletException {

		super.init(config);
		// Stores the application parameters into Properties object
		applicationProperties = new Properties();
		for (final Enumeration<String> e = config.getInitParameterNames(); e.hasMoreElements();) {
			final String name = e.nextElement();
			applicationProperties.setProperty(name, config.getInitParameter(name));
		}

		// Overrides with server.xml parameters
		final PortletContext context = config.getPortletContext();
		for (final Enumeration<String> e = context.getInitParameterNames(); e.hasMoreElements();) {
			final String name = e.nextElement();
			applicationProperties.setProperty(name, context.getInitParameter(name));
		}
		checkProductionMode();
		checkCrossSiteProtection();
	}

	/**
	 * Returns true if the servlet is running in production mode. Production mode disables all debug facilities.
	 * 
	 * @return true if in production mode, false if in debug mode
	 */
	public boolean isProductionMode() {

		return productionMode;
	}

	@Override
	public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException {

		handleRequest(request, response);
	}

	@Override
	public void processEvent(EventRequest request, EventResponse response) throws PortletException, IOException {

		handleRequest(request, response);
	}

	@Override
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {

		handleRequest(request, response);
	}

	protected enum RequestType {
		FILE_UPLOAD, UIDL, RENDER, STATIC_FILE, APPLICATION_RESOURCE, DUMMY, EVENT, ACTION, UNKNOWN;
	}

	@SuppressWarnings("serial")
	public class RequestError implements Terminal.ErrorEvent, Serializable {

		private final Throwable throwable;

		public RequestError(Throwable throwable) {

			this.throwable = throwable;
		}

		public Throwable getThrowable() {

			return throwable;
		}

	}

}
