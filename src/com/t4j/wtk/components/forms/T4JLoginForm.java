
package com.t4j.wtk.components.forms;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.t4j.wtk.T4JConstants;
import com.t4j.wtk.application.T4JWebApp;
import com.t4j.wtk.util.T4JCollectionUtils;
import com.t4j.wtk.util.T4JLogUtils;
import com.vaadin.Application;
import com.vaadin.terminal.ApplicationResource;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.ParameterHandler;
import com.vaadin.terminal.URIHandler;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Window;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public class T4JLoginForm extends CustomComponent {

	private static final long serialVersionUID = 1L;

	private static final Log logger = T4JLogUtils.getLogger(T4JFormFieldFactory.class);

	private static final String NEW_LINE = "\r\n";

	private static final String LOGIN_RESPONSE_PAGE_HTML_STRING = "" //
		+ "<!DOCTYPE html>" //
		+ NEW_LINE //
		+ "<html>" //
		+ NEW_LINE //
		+ "<head>" //
		+ NEW_LINE //
		+ "<meta charset='UTF-8'>" //
		+ NEW_LINE //
		+ "</head>" //
		+ NEW_LINE //
		+ "<body>" //
		+ NEW_LINE //
		+ "<script type='text/javascript'>top.vaadin.forceSync();</script>" //
		+ NEW_LINE //
		+ "</body>" //
		+ NEW_LINE //
		+ "</html>"//
		+ NEW_LINE;

	private String formCaption;

	private String userTextFieldCaption;

	private String passwordTextFieldCaption;

	private String passwordRecoveryButtonCaption;

	private String loginButtonCaption;

	private ApplicationResource applicationResource = new ApplicationResource() {

		private static final long serialVersionUID = 1L;

		public Application getApplication() {

			return T4JLoginForm.this.getApplication();
		}

		public int getBufferSize() {

			return getHtmlBytes().length;
		}

		public long getCacheTime() {

			return -1;
		}

		public String getFilename() {

			return "loginForm.html";
		}

		public String getMIMEType() {

			return "text/html; charset=UTF-8";
		}

		public DownloadStream getStream() {

			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(getHtmlBytes());
			DownloadStream downloadStream = new DownloadStream(byteArrayInputStream, getMIMEType(), getFilename());
			return downloadStream;
		}
	};

	private ParameterHandler parameterHandler = new ParameterHandler() {

		private static final long serialVersionUID = 1L;

		public void handleParameters(Map<String, String[]> parameters) {

			if (parameters.containsKey("loginFormEventType")) {

				getWindow().addURIHandler(uriHandler);

				HashMap<String, String> params = new HashMap<String, String>();

				// expecting single params

				for (Iterator<String> it = parameters.keySet().iterator(); it.hasNext();) {

					String key = it.next();
					String value = parameters.get(key)[0];
					params.put(key, value);
				}

				String loginFormEventType = params.get("loginFormEventType");

				if ("login".equals(loginFormEventType)) {

					T4JLoginEvent event = new T4JLoginEvent(params);
					fireEvent(event);
				} else if ("passwordRecovery".equals(loginFormEventType)) {

					T4JPasswordRecoveryEvent event = new T4JPasswordRecoveryEvent(params);
					fireEvent(event);
				} else {

					logger.warn("Unknown event type: " + loginFormEventType);
				}
			}
		}
	};

	private URIHandler uriHandler = new URIHandler() {

		private static final long serialVersionUID = 1L;

		public DownloadStream handleURI(URL context, String relativeUri) {

			logger.debug("relativeUri: " + relativeUri);

			if (relativeUri != null && relativeUri.contains("loginFormHandler")) {

				if (window != null) {

					window.removeURIHandler(this);
				}

				DownloadStream downloadStream = new DownloadStream(new ByteArrayInputStream(LOGIN_RESPONSE_PAGE_HTML_STRING.getBytes()), "text/html; charset=UTF-8", "loginResponse.html");
				downloadStream.setCacheTime(-1);
				return downloadStream;
			} else {

				return null;
			}
		}
	};

	private Embedded internalFrame;

	private Window window;

	public T4JLoginForm() {

		super();

		addStyleName("t4j-login-form");

		setSizeUndefined();

		internalFrame = new Embedded();
		internalFrame.setSizeUndefined();
		internalFrame.setType(Embedded.TYPE_BROWSER);

		setCompositionRoot(internalFrame);
	}

	protected byte[] getHtmlBytes() {

		try {

			String applicationUrl = T4JWebApp.getInstance().getURL().toString();

			// String applicationUri = applicationUrl + getWindow().getName() + "/";

			String applicationUri = applicationUrl;

			if (false == applicationUri.endsWith(String.valueOf('/'))) {

				applicationUri = applicationUri.concat(String.valueOf('/'));
			}

			String htmlString = "<!DOCTYPE html>\n" //
				+ "<html>\n" //
				+ "<head>\n" //
				+ "<meta charset='utf-8'>\n" //
				+ "<script type='text/javascript'>\n" //
				+ "<!--\n" //
				+ "  var applicationUri = '" + applicationUri + "loginFormHandler';\n" //

				// Copiamos los scripts de la ventana padre

				+ "  var parentScripts = window.parent.document.getElementsByTagName('script');\n" //
				+ "  for (var j = 0; j < parentScripts.length; j++) {\n" //
				+ "    if (parentScripts[j].src && 0 < parentScripts[j].src.indexOf('/themes/')) { \n" //
				+ "      var iFrameScript = document.createElement('script');\n" //
				+ "      iFrameScript.setAttribute('type', 'text/javascript');\n" //
				+ "      iFrameScript.setAttribute('src', parentScripts[j].src);\n" //
				+ "      document.getElementsByTagName('head')[0].appendChild(iFrameScript);\n" //
				+ "    }\n" //
				+ "  }\n" //

				// Copiamos las hojas de estilo de la ventana padre

				+ "  var parentStyleSheets = window.parent.document.styleSheets;\n" //
				+ "  for (var j = 0; j < parentStyleSheets.length; j++) { \n" //
				+ "    if (parentStyleSheets[j].href) {\n" //
				+ "      var iFrameStyleSheet = document.createElement('link');\n" //
				+ "      iFrameStyleSheet.setAttribute('rel', 'stylesheet');\n" //
				+ "      iFrameStyleSheet.setAttribute('type', 'text/css');\n" //
				+ "      iFrameStyleSheet.setAttribute('href', parentStyleSheets[j].href);\n" //
				+ "      document.getElementsByTagName('head')[0].appendChild(iFrameStyleSheet);\n" //
				+ "    }\n" //
				+ "  }\n" //
				+ "// -->\n" //
				+ "</script>\n" //
				+ "</head>\n" //
				+ "<body onload='setupLoginForm();' class='" + ApplicationConnection.GENERATED_BODY_CLASSNAME + "' style='border:0;margin:0;padding:0;'>\n" //
				+ "  <form name='LoginForm' autocomplete='on' onkeypress='submitLoginFormOnEnter(event);' onsubmit='submitLoginForm(event)' method='post' target='loginTarget'>\n" //
				+ "    <div class='login-box-wrapper'>\n" //
				+ "      <div class='login-box'>\n" //
				+ "        <div class='caption-label-wrapper'><div class='v-label caption'><span>" + getFormCaption() + "</span></div></div>\n" //
				+ "        <div class='username-label-wrapper'><div class='v-label username'><span>" + getUserTextFieldCaption() + "</span></div></div>\n" //
				+ "        <div class='username-field-wrapper'><input type='text' name='username' class='v-textfield v-textfield-required loginField'></div>\n" //
				+ "        <div class='password-label-wrapper'><div class='v-label password'><span>" + getPasswordTextFieldCaption() + "</span></div></div>\n" //
				+ "        <div class='password-field-wrapper'><input type='password' name='password' class='v-textfield v-textfield-required loginField'></div>\n" //
				+ "        <div class='buttons-wrapper clearfix'>\n" //
				+ "          <div class='v-button v-button-link link password-recovery-link' onclick='setLoginFormTarget('passwordRecovery');submitLoginForm(null);' role='button' tabindex='0'><span class='v-button-wrap'><span class='v-button-caption'>" + getPasswordRecoveryButtonCaption() + "</span></span></div>\n" //
				+ "          <div class='v-button login-button' onclick='setLoginFormTarget('login');submitLoginForm(null);' role='button' tabindex='0'><span class='v-button-wrap'><span class='v-button-caption'>" + getLoginButtonCaption() + "</span></span></div>\n" //
				+ "        </div>\n" //
				+ "      </div>\n" //
				+ "    </div>\n" //
				+ "    <input type='hidden' name='loginFormEventType' value=''>\n" //
				+ "    <input type='hidden' name='fragment' value=''>\n" //
				+ "    <input type='hidden' name='usernameBase64' value=''>\n" //
				+ "    <input type='hidden' name='passwordBase64' value=''>\n" //
				+ "    <input type='hidden' name='fragmentBase64' value=''>\n" //
				+ "  </form>\n" //
				+ "  <iframe name='loginTarget' style='width:0;height:0;border:0;margin:0;padding:0;'></iframe>\n" //
				+ "</body>\n" //
				+ "</html>\n";

			return htmlString.getBytes(T4JConstants.DEFAULT_ENCODING);
		} catch (UnsupportedEncodingException e) {

			throw new RuntimeException(T4JConstants.DEFAULT_ENCODING + " encoding not avalable", e);
		}
	}

	/**
	 * Adds LoginListener to handle login logic
	 * 
	 * @param listener
	 */
	public void addListener(T4JLoginListener listener) {

		try {

			Method tmpMethod = T4JLoginListener.class.getDeclaredMethod("onLogin", new Class[] {
				T4JLoginEvent.class
			});

			addListener(T4JLoginEvent.class, listener, tmpMethod);
		} catch (Throwable t) {
			// This should never happen
			throw new RuntimeException("Internal error finding methods in LoginForm");
		}

	}

	/**
	 * Adds LoginListener to handle login logic
	 * 
	 * @param listener
	 */
	public void addListener(T4JPasswordRecoveryListener listener) {

		try {

			Method tmpMethod = T4JPasswordRecoveryListener.class.getDeclaredMethod("onPasswordRecovery", new Class[] {
				T4JPasswordRecoveryEvent.class
			});

			addListener(T4JPasswordRecoveryEvent.class, listener, tmpMethod);
		} catch (Throwable t) {
			// This should never happen
			throw new RuntimeException("Internal error finding methods in LoginForm");
		}

	}

	@Override
	public void attach() {

		super.attach();

		getApplication().addResource(applicationResource);
		getWindow().addParameterHandler(parameterHandler);

		internalFrame.setSource(applicationResource);
	}

	@Override
	public void detach() {

		getApplication().removeResource(applicationResource);
		getWindow().removeParameterHandler(parameterHandler);

		// store window temporary to properly remove uri handler once
		// response is handled. (May happen if login handler removes login
		// form
		window = getWindow();

		if (window.getParent() != null) {
			window = window.getParent();
		}

		super.detach();
	}

	public String getFormCaption() {

		return formCaption;
	}

	public String getLoginButtonCaption() {

		return loginButtonCaption;
	}

	public String getPasswordRecoveryButtonCaption() {

		return passwordRecoveryButtonCaption;
	}

	public String getPasswordTextFieldCaption() {

		return passwordTextFieldCaption;
	}

	public String getUserTextFieldCaption() {

		return userTextFieldCaption;
	}

	public void setFormCaption(String formCaption) {

		this.formCaption = formCaption;
	}

	@Override
	public void setHeight(float height, int unit) {

		super.setHeight(height, unit);

		if (null == internalFrame) {

			return;
		} else {
			if (height < 0) {
				internalFrame.setHeight("12em");
			} else {
				internalFrame.setHeight(T4JConstants.FULL_WIDTH_PERCENTAGE);
			}
		}
	}

	public void setLoginButtonCaption(String loginButtonCaption) {

		this.loginButtonCaption = loginButtonCaption;
	}

	public void setPasswordRecoveryButtonCaption(String passwordRecoveryButtonCaption) {

		this.passwordRecoveryButtonCaption = passwordRecoveryButtonCaption;
	}

	public void setPasswordTextFieldCaption(String passwordTextFieldCaption) {

		this.passwordTextFieldCaption = passwordTextFieldCaption;
	}

	public void setUserTextFieldCaption(String userTextFieldCaption) {

		this.userTextFieldCaption = userTextFieldCaption;
	}

	@Override
	public void setWidth(float width, int unit) {

		super.setWidth(width, unit);

		if (null == internalFrame) {

			return;
		} else {
			if (width < 0) {
				internalFrame.setWidth("20em");
			} else {
				internalFrame.setWidth(T4JConstants.FULL_WIDTH_PERCENTAGE);
			}
		}
	}

	/**
	 * This event is sent when login form is submitted.
	 */
	public class T4JLoginEvent extends Event {

		private static final long serialVersionUID = 1L;

		private Map<String, String> loginFormParameters;

		private T4JLoginEvent(Map<String, String> params) {

			super(T4JLoginForm.this);

			loginFormParameters = params;
		}

		/**
		 * Access method to form values by field names.
		 * 
		 * @param name
		 * @return value in given field
		 */
		public String getLoginParameter(String name) {

			if (T4JCollectionUtils.isNullOrEmpty(loginFormParameters)) {
				return null;
			} else {
				if (loginFormParameters.containsKey(name)) {
					return loginFormParameters.get(name);
				} else {
					return null;
				}
			}
		}
	}

	/**
	 * Login listener is a class capable to listen LoginEvents sent from LoginBox
	 */
	public interface T4JLoginListener extends Serializable {

		/**
		 * This method is fired on each login form post.
		 * 
		 * @param event
		 */
		public void onLogin(T4JLoginEvent event);
	}

	/**
	 * This event is sent when login form is submitted.
	 */
	public class T4JPasswordRecoveryEvent extends Event {

		private static final long serialVersionUID = 1L;

		private Map<String, String> passwordRecoveryParameters;

		private T4JPasswordRecoveryEvent(Map<String, String> params) {

			super(T4JLoginForm.this);

			passwordRecoveryParameters = params;
		}

		/**
		 * Access method to form values by field names.
		 * 
		 * @param name
		 * @return value in given field
		 */
		public String getPasswordRecoveryParameter(String name) {

			if (T4JCollectionUtils.isNullOrEmpty(passwordRecoveryParameters)) {
				return null;
			} else {
				if (passwordRecoveryParameters.containsKey(name)) {
					return passwordRecoveryParameters.get(name);
				} else {
					return null;
				}
			}
		}
	}

	/**
	 * Login listener is a class capable to listen LoginEvents sent from LoginBox
	 */
	public interface T4JPasswordRecoveryListener extends Serializable {

		/**
		 * This method is fired on each login form post.
		 * 
		 * @param event
		 */
		public void onPasswordRecovery(T4JPasswordRecoveryEvent event);
	}

}
