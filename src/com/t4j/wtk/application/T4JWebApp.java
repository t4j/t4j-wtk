
package com.t4j.wtk.application;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

import com.t4j.wtk.T4JConstants;
import com.t4j.wtk.application.enums.T4JWebAppErrorTypeEnum;
import com.t4j.wtk.application.enums.T4JWebAppPopupMessageTypeEnum;
import com.t4j.wtk.application.enums.T4JWebAppTrayNotificationTypeEnum;
import com.t4j.wtk.application.enums.T4JWebAppWindowTypeEnum;
import com.t4j.wtk.components.downloads.T4JZipStreamSource;
import com.t4j.wtk.components.uploads.T4JFileUploadHandler;
import com.t4j.wtk.util.T4JCollectionUtils;
import com.t4j.wtk.util.T4JLogUtils;
import com.t4j.wtk.util.T4JStringUtils;
import com.t4j.wtk.util.T4JTooltipUtils;
import com.vaadin.Application;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.terminal.gwt.server.WebBrowser;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedListener;
import com.vaadin.ui.Upload.FinishedListener;
import com.vaadin.ui.Upload.ProgressListener;
import com.vaadin.ui.Upload.StartedListener;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.Window.Notification;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public abstract class T4JWebApp extends Application implements CloseListener, HttpServletRequestListener {

	private static final long serialVersionUID = 1L;

	private static final Log logger = T4JLogUtils.getLogger(T4JWebApp.class);

	private static final ThreadLocal<T4JWebApp> threadLocal = new ThreadLocal<T4JWebApp>();

	protected Boolean isIE6;

	protected AbstractLayout applicationLayout;

	protected Window backgroundTaskWindow;

	protected Window dialogWindow;

	protected Window fileDownloadWindow;

	protected Window fileUploadWindow;

	protected Window popupMessageWindow;

	protected ProgressIndicator fileUploadProgressIndicator;

	protected ProgressIndicator backgroundTaskProgressIndicator;

	protected T4JWebAppBackgroundTaskManager backgroundTaskManager;

	protected T4JZipStreamSource zipStreamSource;

	public T4JWebApp() {

		super();
	}

	private static void setInstance(T4JWebApp application) {

		threadLocal.set(application);
	}

	/**
	 * Devuelve la instancia almacenada en threadLocal.
	 * 
	 * @return T4JWebApp
	 */
	public static T4JWebApp getInstance() {

		return threadLocal.get();
	}

	public static SystemMessages getSystemMessages() {

		Locale tmpLocale = null;

		try {
			tmpLocale = T4JWebApp.getInstance().getLocale();
		} catch (Exception e) {
			// Ignore
		}

		if (null == tmpLocale) {

			tmpLocale = T4JConstants.DEFAULT_LOCALE;
		}

		ResourceBundle resourceBundle = ResourceBundle.getBundle(T4JConstants.DEFAULT_BUNDLE_BASE_NAME, tmpLocale);

		CustomizedSystemMessages customizedSystemMessages = new CustomizedSystemMessages();

		String applicationErrorUrl = null;

		try {
			applicationErrorUrl = T4JWebApp.getInstance().getErrorUrl(T4JWebAppErrorTypeEnum.APPLICATION_ERROR);
		} catch (Exception e) {
			// Ignore
		}

		if (T4JStringUtils.isNullOrEmpty(applicationErrorUrl)) {

			applicationErrorUrl = T4JConstants.DEFAULT_ERROR_URL;
		}

		customizedSystemMessages.setAuthenticationErrorCaption(resourceBundle.getString("T4JWebApp.AuthenticationErrorCaption"));
		customizedSystemMessages.setAuthenticationErrorMessage(resourceBundle.getString("T4JWebApp.AuthenticationErrorMessage"));
		customizedSystemMessages.setAuthenticationErrorNotificationEnabled(true);
		customizedSystemMessages.setAuthenticationErrorURL(applicationErrorUrl);

		String communicationErrorUrl = null;

		try {
			communicationErrorUrl = T4JWebApp.getInstance().getErrorUrl(T4JWebAppErrorTypeEnum.COMMUNICATION_ERROR);
		} catch (Exception e) {
			// Ignore
		}

		if (T4JStringUtils.isNullOrEmpty(communicationErrorUrl)) {

			communicationErrorUrl = T4JConstants.DEFAULT_ERROR_URL;
		}

		customizedSystemMessages.setCommunicationErrorCaption(resourceBundle.getString("T4JWebApp.CommunicationErrorCaption"));
		customizedSystemMessages.setCommunicationErrorMessage(resourceBundle.getString("T4JWebApp.CommunicationErrorMessage"));
		customizedSystemMessages.setCommunicationErrorNotificationEnabled(true);
		customizedSystemMessages.setCommunicationErrorURL(communicationErrorUrl);

		String cookiesDisabledUrl = null;

		try {
			cookiesDisabledUrl = T4JWebApp.getInstance().getErrorUrl(T4JWebAppErrorTypeEnum.COOKIES_DISABLED);
		} catch (Exception e) {
			// Ignore
		}

		if (T4JStringUtils.isNullOrEmpty(cookiesDisabledUrl)) {

			cookiesDisabledUrl = T4JConstants.DEFAULT_ERROR_URL;
		}

		customizedSystemMessages.setCookiesDisabledCaption(resourceBundle.getString("T4JWebApp.CookiesDisabledCaption"));
		customizedSystemMessages.setCookiesDisabledMessage(resourceBundle.getString("T4JWebApp.CookiesDisabledMessage"));
		customizedSystemMessages.setCookiesDisabledNotificationEnabled(true);
		customizedSystemMessages.setCookiesDisabledURL(cookiesDisabledUrl);

		String internalErrorUrl = null;

		try {
			internalErrorUrl = T4JWebApp.getInstance().getErrorUrl(T4JWebAppErrorTypeEnum.INTERNAL_ERROR);
		} catch (Exception e) {
			// Ignore
		}

		if (T4JStringUtils.isNullOrEmpty(internalErrorUrl)) {

			internalErrorUrl = T4JConstants.DEFAULT_ERROR_URL;
		}

		customizedSystemMessages.setInternalErrorCaption(resourceBundle.getString("T4JWebApp.InternalErrorCaption"));
		customizedSystemMessages.setInternalErrorMessage(resourceBundle.getString("T4JWebApp.InternalErrorMessage"));
		customizedSystemMessages.setInternalErrorNotificationEnabled(true);
		customizedSystemMessages.setInternalErrorURL(internalErrorUrl);

		String outOfSyncUrl = null;

		try {
			outOfSyncUrl = T4JWebApp.getInstance().getErrorUrl(T4JWebAppErrorTypeEnum.OUT_OF_SYNC);
		} catch (Exception e) {
			// Ignore
		}

		if (T4JStringUtils.isNullOrEmpty(outOfSyncUrl)) {

			outOfSyncUrl = T4JConstants.DEFAULT_ERROR_URL;
		}

		customizedSystemMessages.setOutOfSyncCaption(resourceBundle.getString("T4JWebApp.OutOfSyncCaption"));
		customizedSystemMessages.setOutOfSyncMessage(resourceBundle.getString("T4JWebApp.OutOfSyncMessage"));
		customizedSystemMessages.setOutOfSyncNotificationEnabled(true);
		customizedSystemMessages.setOutOfSyncURL(outOfSyncUrl);

		String sessionExpiredUrl = null;

		try {
			sessionExpiredUrl = T4JWebApp.getInstance().getErrorUrl(T4JWebAppErrorTypeEnum.SESSION_EXPIRED);
		} catch (Exception e) {
			// Ignore
		}

		if (T4JStringUtils.isNullOrEmpty(sessionExpiredUrl)) {

			sessionExpiredUrl = T4JConstants.DEFAULT_ERROR_URL;
		}

		customizedSystemMessages.setSessionExpiredCaption(resourceBundle.getString("T4JWebApp.SessionExpiredCaption"));
		customizedSystemMessages.setSessionExpiredMessage(resourceBundle.getString("T4JWebApp.SessionExpiredMessage"));
		customizedSystemMessages.setSessionExpiredNotificationEnabled(true);
		customizedSystemMessages.setSessionExpiredURL(sessionExpiredUrl);

		return customizedSystemMessages;
	}

	private String getResourceBundleValue(String bundleBaseName, String key, Locale locale) throws Exception {

		List<Locale> locales = new ArrayList<Locale>();

		if (null == locale) {

			locale = getLocale();
		}

		if (null == locale) {

			locale = T4JConstants.DEFAULT_LOCALE;
		}

		String variant = locale.getVariant();
		String country = locale.getCountry();
		String language = locale.getLanguage();

		boolean hasVariant = false == T4JStringUtils.isNullOrEmpty(variant);
		boolean hasCountry = false == T4JStringUtils.isNullOrEmpty(country);
		boolean hasLanguage = false == T4JStringUtils.isNullOrEmpty(language);

		if (hasLanguage && hasCountry && hasVariant) {

			locales.add(locale);
			locales.add(new Locale(language, country, T4JConstants.EMPTY_STRING));
			locales.add(new Locale(language, T4JConstants.EMPTY_STRING, T4JConstants.EMPTY_STRING));

		} else {

			if (hasVariant) {

				// Tiene variante

				if (hasCountry) {

					// No tiene idioma

					locales.add(new Locale(T4JConstants.EMPTY_STRING, country, variant));
					locales.add(new Locale(T4JConstants.EMPTY_STRING, country, T4JConstants.EMPTY_STRING));
				} else {

					// No tiene país

					if (hasLanguage) {

						// Tiene variante e idioma

						locales.add(new Locale(language, T4JConstants.EMPTY_STRING, variant));
						locales.add(new Locale(language, T4JConstants.EMPTY_STRING, T4JConstants.EMPTY_STRING));
					} else {

						// Únicamente tiene variante

						locales.add(new Locale(T4JConstants.EMPTY_STRING, T4JConstants.EMPTY_STRING, variant));
					}
				}
			} else {

				// No tiene variante

				if (hasCountry) {

					// Tiene país

					if (hasLanguage) {

						// Tiene país e idioma

						locales.add(new Locale(language, country, T4JConstants.EMPTY_STRING));
						locales.add(new Locale(language, T4JConstants.EMPTY_STRING, T4JConstants.EMPTY_STRING));
					} else {

						// Únicamente tiene país

						locales.add(new Locale(T4JConstants.EMPTY_STRING, country, T4JConstants.EMPTY_STRING));
					}
				} else {

					// No tiene país

					if (hasLanguage) {

						// Únicamente tiene idioma

						locales.add(new Locale(language, T4JConstants.EMPTY_STRING, T4JConstants.EMPTY_STRING));
					}
				}
			}
		}

		locales.add(new Locale(T4JConstants.EMPTY_STRING));

		String methodResult = null;

		for (Iterator<Locale> iterator = locales.iterator(); iterator.hasNext();) {

			Locale tmpLocale = iterator.next();

			try {
				methodResult = ResourceBundle.getBundle(bundleBaseName, tmpLocale).getString(key);
			} catch (Exception e) {
				// Ignore
			}

			if (false == T4JStringUtils.isNullOrEmpty(methodResult)) {
				break;
			}
		}

		if (null == methodResult) {

			throw new MissingResourceException("Can't find resource for bundle " + bundleBaseName + ", key " + key, //
				bundleBaseName, //
				key);
		} else {

			return methodResult;
		}
	}

	protected void closeWindow(T4JWebAppWindowTypeEnum windowType) {

		if (null == windowType) {

			return;
		} else {

			boolean isBackgroundTaskWindow = windowType.equals(T4JWebAppWindowTypeEnum.BACKGROUND_TASK_WINDOW);

			boolean isDialogWindow = windowType.equals(T4JWebAppWindowTypeEnum.DIALOG_WINDOW);

			boolean isFileDownloadWindow = windowType.equals(T4JWebAppWindowTypeEnum.FILE_DOWNLOAD_WINDOW);

			boolean isFileUploadWindow = windowType.equals(T4JWebAppWindowTypeEnum.FILE_UPLOAD_WINDOW);

			boolean isPopupMessageWindow = windowType.equals(T4JWebAppWindowTypeEnum.POPUP_MESSAGE_WINDOW);

			Window window = getWindowByType(windowType);

			if (null == window) {

				return;
			}

			try {
				getMainWindow().removeWindow(window);
			} catch (Exception e) {
				logger.trace(e, e);
			}

			window = null;

			if (isBackgroundTaskWindow) {

				backgroundTaskWindow = null;

				try {
					backgroundTaskProgressIndicator.setEnabled(false);
				} catch (Exception e) {
					logger.trace(e, e);
				}

				backgroundTaskProgressIndicator = null;

				try {
					backgroundTaskManager.cancel();
				} catch (Exception e) {
					logger.trace(e, e);
				}

				try {
					backgroundTaskManager.getBackgroundTask().showResults();
				} catch (Exception e) {
					logger.trace(e, e);
				}

				backgroundTaskManager = null;
			}

			if (isDialogWindow) {

				dialogWindow = null;

				try {
					zipStreamSource.forceCleanUp();
				} catch (Exception e) {
					logger.trace(e, e);
				}

				zipStreamSource = null;
			}

			if (isFileDownloadWindow) {

				fileDownloadWindow = null;

				try {
					zipStreamSource.forceCleanUp();
				} catch (Exception e) {
					logger.trace(e, e);
				}

				zipStreamSource = null;
			}

			if (isFileUploadWindow) {

				fileUploadWindow = null;
			}

			if (isPopupMessageWindow) {

				popupMessageWindow = null;
			}
		}
	}

	protected Window getWindowByType(T4JWebAppWindowTypeEnum windowType) {

		if (null == windowType) {

			return null;
		} else {

			Window window = null;

			switch (windowType) {
				case BACKGROUND_TASK_WINDOW:

					window = backgroundTaskWindow;
					break;
				case FILE_DOWNLOAD_WINDOW:

					window = fileDownloadWindow;
					break;
				case FILE_UPLOAD_WINDOW:

					window = fileUploadWindow;
					break;
				case POPUP_MESSAGE_WINDOW:

					window = popupMessageWindow;
					break;

				default:
					window = dialogWindow;
					break;
			}

			return window;
		}
	}

	protected abstract AbstractLayout setupApplicationLayout();

	protected void setWindowByType(T4JWebAppWindowTypeEnum windowType, Window window) {

		if (null == windowType) {

			return;
		} else {

			switch (windowType) {
				case BACKGROUND_TASK_WINDOW:

					backgroundTaskWindow = window;
					break;
				case FILE_DOWNLOAD_WINDOW:

					fileDownloadWindow = window;
					break;
				case FILE_UPLOAD_WINDOW:

					fileUploadWindow = window;
					break;
				case POPUP_MESSAGE_WINDOW:

					popupMessageWindow = window;
					break;

				default:
					dialogWindow = window;
					break;
			}
		}
	}

	public void closeBackgroundTaskWindow() {

		closeWindow(T4JWebAppWindowTypeEnum.BACKGROUND_TASK_WINDOW);
	}

	public void closeDialogWindow() {

		closeWindow(T4JWebAppWindowTypeEnum.DIALOG_WINDOW);
	}

	public void closeFileDownloadWindow() {

		closeWindow(T4JWebAppWindowTypeEnum.FILE_DOWNLOAD_WINDOW);
	}

	public void closeFileUploadWindow() {

		closeWindow(T4JWebAppWindowTypeEnum.FILE_UPLOAD_WINDOW);
	}

	@Deprecated
	public void closeModalWindow() {

		closeDialogWindow();
	}

	public void closePopupMessageModalWindow() {

		closeWindow(T4JWebAppWindowTypeEnum.POPUP_MESSAGE_WINDOW);
	}

	public AbstractLayout getApplicationLayout() {

		return applicationLayout;
	}

	public abstract String getApplicationResourceBundleBaseName();

	public ProgressIndicator getBackgroundTaskProgressIndicator() {

		return backgroundTaskProgressIndicator;
	}

	public abstract String getErrorUrl(T4JWebAppErrorTypeEnum errorType);

	public ProgressIndicator getFileUploadProgressIndicator() {

		return fileUploadProgressIndicator;
	}

	public String getI18nString(String key) {

		return getI18nString(key, new Object[] {});
	}

	public String getI18nString(String key, Object[] replacements) {

		String methodResult = null;

		if (T4JStringUtils.isNullOrEmpty(key)) {

			methodResult = T4JConstants.EMPTY_STRING;
		} else {

			String tmpBaseName = getApplicationResourceBundleBaseName();

			if (T4JStringUtils.isNullOrEmpty(tmpBaseName)) {

				tmpBaseName = T4JConstants.DEFAULT_BUNDLE_BASE_NAME;
			}

			Locale tmpLocale = getLocale();

			if (null == tmpLocale) {

				tmpLocale = T4JConstants.DEFAULT_LOCALE;
			}

			try {

				String bundleValue = getResourceBundleValue(getApplicationResourceBundleBaseName(), key, getLocale()); // ResourceBundle.getBundle(getApplicationResourceBundleBaseName(), getLocale()).getString(key);

				if (T4JStringUtils.isNullOrEmpty(bundleValue) && false == T4JCollectionUtils.isNullOrEmpty(replacements)) {

					methodResult = bundleValue;
				} else {

					try {

						methodResult = MessageFormat.format(bundleValue, replacements);
					} catch (Exception e) {

						methodResult = bundleValue;
					}
				}
			} catch (Exception e1) {

				try {

					String bundleValue = getResourceBundleValue(T4JConstants.DEFAULT_BUNDLE_BASE_NAME, key, getLocale()); // ResourceBundle.getBundle(T4JConstants.DEFAULT_BUNDLE_BASE_NAME, getLocale()).getString(key);

					if (T4JStringUtils.isNullOrEmpty(bundleValue) && false == T4JCollectionUtils.isNullOrEmpty(replacements)) {

						methodResult = bundleValue;
					} else {

						try {

							methodResult = MessageFormat.format(bundleValue, replacements);
						} catch (Exception e) {

							methodResult = bundleValue;
						}
					}
				} catch (Exception e) {

					// Ignore
				}
			}

			if (null == methodResult) {

				methodResult = T4JConstants.EMPTY_STRING;
			}
		}

		return methodResult;

	}

	public String getI18nString(String key, String defaultValue) {

		String localizedMessage = getI18nString(key, new Object[] {});

		if (T4JStringUtils.isNullOrEmpty(localizedMessage)) {

			if (T4JStringUtils.isNullOrEmpty(defaultValue)) {

				return localizedMessage;
			} else {

				return defaultValue;
			}
		} else {

			return localizedMessage;
		}
	}

	public String getI18nString(String key, String defaultValue, Object[] replacements) {

		String localizedMessage = getI18nString(key, replacements);

		if (T4JStringUtils.isNullOrEmpty(localizedMessage)) {

			if (T4JStringUtils.isNullOrEmpty(defaultValue)) {

				return localizedMessage;
			} else {

				if (false == T4JCollectionUtils.isNullOrEmpty(replacements)) {

					localizedMessage = MessageFormat.format(defaultValue, replacements);
				}

				return localizedMessage;
			}
		} else {

			if (false == T4JCollectionUtils.isNullOrEmpty(replacements)) {

				localizedMessage = MessageFormat.format(localizedMessage, replacements);
			}

			return localizedMessage;
		}
	}

	public abstract String getMainWindowTitle();

	public String getSizeInEMs(int sizeInPixels) {

		if (null == isIE6) {

			WebApplicationContext webApplicationContext = (WebApplicationContext) getContext();
			WebBrowser webBrowser = webApplicationContext.getBrowser();
			isIE6 = Boolean.valueOf(webBrowser.isIE() && webBrowser.getBrowserMajorVersion() == 6);
		}

		if (Boolean.TRUE.equals(isIE6)) {
			return BigDecimal.valueOf(sizeInPixels * T4JConstants.DOUBLE_PIXEL_TO_EM_IE6.doubleValue()).toPlainString().concat("em");
		} else {
			return BigDecimal.valueOf(sizeInPixels * T4JConstants.DOUBLE_PIXEL_TO_EM.doubleValue()).toPlainString().concat("em");
		}
	}

	public abstract String getThemeName();

	@Override
	public void init() {

		logger.debug(T4JConstants.L4J_METHOD_START);

		T4JWebApp.setInstance(this);

		Window mainWindow = getMainWindow();

		if (null == mainWindow) {

			mainWindow = new Window();

			applicationLayout = setupApplicationLayout();

			mainWindow.setContent(applicationLayout);

			String caption = getMainWindowTitle();

			if (false == T4JStringUtils.isNullOrEmpty(caption)) {

				mainWindow.setCaption(caption);
			}

			String themeName = getThemeName();

			if (T4JStringUtils.isNullOrEmpty(themeName)) {

				mainWindow.setTheme(T4JConstants.DEFAULT_THEME);
			} else {

				mainWindow.setTheme(themeName);
			}

			mainWindow.addListener(this);

			setMainWindow(mainWindow);
		}
	}

	public void launchBackgroundTask(T4JWebAppBackgroundTask backgroundTask) {

		if (null == backgroundTaskManager) {

			VerticalLayout dialogLayout = new VerticalLayout();
			dialogLayout.addStyleName("background-task-window");
			dialogLayout.setSizeUndefined();
			dialogLayout.setSpacing(true);

			VerticalLayout progressIndicatorWrapper = new VerticalLayout();
			progressIndicatorWrapper.setWidth(getSizeInEMs(300));
			progressIndicatorWrapper.setHeight(getSizeInEMs(150));

			dialogLayout.addComponent(progressIndicatorWrapper);

			closeBackgroundTaskWindow();

			backgroundTask.setThreadWebApp(this);

			backgroundTaskProgressIndicator = new ProgressIndicator();

			backgroundTaskProgressIndicator.setEnabled(false);
			backgroundTaskProgressIndicator.setIndeterminate(true);
			backgroundTaskProgressIndicator.setPollingInterval(1000);
			backgroundTaskProgressIndicator.setWidth(T4JConstants.FULL_WIDTH_PERCENTAGE);

			progressIndicatorWrapper.addComponent(backgroundTaskProgressIndicator);
			progressIndicatorWrapper.setComponentAlignment(backgroundTaskProgressIndicator, Alignment.BOTTOM_CENTER);

			backgroundTaskManager = new T4JWebAppBackgroundTaskManager(backgroundTask);

			backgroundTaskWindow = new Window(getI18nString("T4JWebApp.backgrountTaskDialogTitle"));

			backgroundTaskWindow.addStyleName("modal-window");
			backgroundTaskWindow.setSizeUndefined();

			backgroundTaskWindow.getContent().addComponent(dialogLayout);
			backgroundTaskWindow.getContent().setSizeUndefined();

			backgroundTaskWindow.center();

			backgroundTaskWindow.setClosable(true);
			backgroundTaskWindow.setDraggable(true);
			backgroundTaskWindow.setName("background-task-window-" + System.currentTimeMillis());
			backgroundTaskWindow.setResizable(false);

			getMainWindow().addWindow(backgroundTaskWindow);

			CloseListener defaultCloseListener = new CloseListener() {

				private static final long serialVersionUID = 1L;

				public void windowClose(CloseEvent e) {

					closeBackgroundTaskWindow();
				}
			};

			backgroundTaskWindow.addListener(defaultCloseListener);

			backgroundTaskWindow.setVisible(true);

			backgroundTaskManager.start();
		} else {

			showTrayNotification(T4JWebAppTrayNotificationTypeEnum.ERROR, null, getI18nString("T4JWebAppBackgroundTask.launchError"));
		}
	}

	public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {

		logger.trace(T4JConstants.L4J_METHOD_START);

		threadLocal.remove();

	}

	public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {

		logger.trace(T4JConstants.L4J_METHOD_START);

		T4JWebApp.setInstance(this);
	}

	public void showDialog(String caption, ComponentContainer content, Boolean isModal, Boolean isCloseButtonRendered, Boolean isResizable, Boolean isSizeUndefined, CloseListener closeListener) {

		showDialogWindow(T4JWebAppWindowTypeEnum.DIALOG_WINDOW, caption, content, isModal, isCloseButtonRendered, isResizable, isSizeUndefined, closeListener);
	}

	public void showDialogWindow(T4JWebAppWindowTypeEnum windowType, String caption, ComponentContainer content, Boolean isModal, Boolean isCloseButtonRendered, Boolean isResizable, Boolean isSizeUndefined, CloseListener closeListener) {

		if (null == windowType) {

			windowType = T4JWebAppWindowTypeEnum.DIALOG_WINDOW;
		}

		final T4JWebAppWindowTypeEnum tmpWindowType = windowType;

		closeWindow(tmpWindowType);

		Window window = new Window(caption);

		window.addStyleName("t4j-dialog-window");

		if (Boolean.TRUE.equals(isModal)) {

			window.addStyleName("t4j-modal-dialog-window");
		}

		window.setSizeUndefined();

		if (Boolean.FALSE.equals(isCloseButtonRendered)) {

			window.setClosable(false);
		} else {

			window.setClosable(true);
		}

		window.getContent().addComponent(content);

		if (Boolean.TRUE.equals(isSizeUndefined)) {
			window.getContent().setSizeUndefined();
		}

		window.center();

		window.setDraggable(true);
		window.setModal(Boolean.TRUE.equals(isModal));
		window.setName("t4j-dialog-" + System.currentTimeMillis());

		window.setResizable(Boolean.TRUE.equals(isResizable));

		setWindowByType(tmpWindowType, window);

		getMainWindow().addWindow(window);

		CloseListener defaultCloseListener = new CloseListener() {

			private static final long serialVersionUID = 1L;

			public void windowClose(CloseEvent e) {

				closeWindow(tmpWindowType);
			}
		};

		if (null == closeListener) {

			window.addListener(defaultCloseListener);
		} else {

			window.addListener(closeListener);
			window.addListener(defaultCloseListener);
		}

		window.setVisible(true);

	}

	/**
	 * Muestra un diálogo para la descarga de un fichero comprimido.
	 * 
	 * @param zipFileStreamSource
	 *            El recurso a descargar
	 */
	public void showFileDownloadDialog(final T4JZipStreamSource zipFileStreamSource) {

		String fileName = zipFileStreamSource.getFileName();

		if (false == fileName.endsWith(".zip")) {
			fileName = fileName.concat(".zip");
		}

		StreamResource streamResource = new StreamResource(zipFileStreamSource, fileName, this);

		Resource linkIcon = new ThemeResource("img/linkIcon.gif");

		Link downloadLink = new Link(getI18nString("button.download"), streamResource);
		downloadLink.addStyleName("download-link");

		String linkDescription = zipFileStreamSource.getFileName();

		if (false == T4JStringUtils.isNullOrEmpty(linkDescription)) {

			downloadLink.setDescription(T4JTooltipUtils.generatePopupMessage(linkDescription));
		}

		downloadLink.setIcon(linkIcon);
		downloadLink.setTargetName("_blank");
		downloadLink.setWidth(getSizeInEMs(200));

		VerticalLayout modalWindowLayout = new VerticalLayout();
		modalWindowLayout.addStyleName("zip-download-dialog-layout");
		modalWindowLayout.setSizeUndefined();
		modalWindowLayout.setHeight(getSizeInEMs(100));

		modalWindowLayout.addComponent(downloadLink);
		modalWindowLayout.setComponentAlignment(downloadLink, Alignment.MIDDLE_LEFT);

		CloseListener closeListener = new CloseListener() {

			private static final long serialVersionUID = 1L;

			public void windowClose(CloseEvent e) {

				zipFileStreamSource.cleanUp();
				zipFileStreamSource.refreshComponents(e);
			}
		};

		showDialogWindow(T4JWebAppWindowTypeEnum.FILE_DOWNLOAD_WINDOW, getI18nString("T4JWebApp.fileDownloadDialogTitle"), modalWindowLayout, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, closeListener);
	}

	/**
	 * Muestra un díalogo para la carga de fichero.
	 * 
	 * @param fileUploadHandler
	 *            Manejador de la carga de fichero.
	 */
	public void showFileUploadDialog(T4JFileUploadHandler fileUploadHandler) {

		final Upload fileUploadControl = new Upload(null, fileUploadHandler);

		fileUploadControl.setButtonCaption(getI18nString("button.select"));
		fileUploadControl.setImmediate(true);
		fileUploadControl.setWidth(getSizeInEMs(100));

		fileUploadControl.addListener((StartedListener) fileUploadHandler);
		fileUploadControl.addListener((ProgressListener) fileUploadHandler);
		fileUploadControl.addListener((SucceededListener) fileUploadHandler);
		fileUploadControl.addListener((FailedListener) fileUploadHandler);
		fileUploadControl.addListener((FinishedListener) fileUploadHandler);

		VerticalLayout modalWindowLayout = new VerticalLayout();
		modalWindowLayout.setSizeUndefined();
		modalWindowLayout.setSpacing(true);

		VerticalLayout fileUploadControlWrapper = new VerticalLayout();
		fileUploadControlWrapper.addStyleName("file-upload-dialog-layout");
		fileUploadControlWrapper.setSizeUndefined();
		fileUploadControlWrapper.setHeight(getSizeInEMs(100));
		fileUploadControlWrapper.setWidth(getSizeInEMs(200));

		fileUploadControlWrapper.addComponent(fileUploadControl);
		fileUploadControlWrapper.setComponentAlignment(fileUploadControl, Alignment.MIDDLE_RIGHT);

		modalWindowLayout.addComponent(fileUploadControlWrapper);

		fileUploadProgressIndicator = new ProgressIndicator();
		fileUploadProgressIndicator.setEnabled(false);
		fileUploadProgressIndicator.setIndeterminate(false);
		fileUploadProgressIndicator.setWidth(T4JConstants.FULL_WIDTH_PERCENTAGE);

		modalWindowLayout.addComponent(fileUploadProgressIndicator);
		modalWindowLayout.setComponentAlignment(fileUploadProgressIndicator, Alignment.MIDDLE_CENTER);

		CloseListener closeListener = new CloseListener() {

			private static final long serialVersionUID = 1L;

			public void windowClose(CloseEvent closeEvent) {

				try {
					fileUploadControl.interruptUpload();
				} catch (Exception e) {
					// Ignore exception
				}
			}
		};

		showDialogWindow(T4JWebAppWindowTypeEnum.FILE_UPLOAD_WINDOW, getI18nString("T4JWebApp.fileUploadDialogTitle"), modalWindowLayout, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, closeListener);

	}

	public void showModalDialog(String caption, ComponentContainer content, Boolean isCloseButtonRendered, Boolean isResizable, Boolean isSizeUndefined, CloseListener closeListener) {

		showDialog(caption, content, Boolean.TRUE, isCloseButtonRendered, isResizable, isSizeUndefined, closeListener);
	}

	public void showPopupMessageModalWindow(T4JWebAppPopupMessageTypeEnum type, String caption, Label label, List<Button> buttons, Boolean isCloseButtonRendered) {

		closePopupMessageModalWindow();

		String iconClassName = null;

		boolean generateAcceptButton = false;

		switch (type) {
			case QUESTION:
				if (T4JStringUtils.isNullOrEmpty(caption)) {
					caption = getI18nString("T4JWebApp.popupMessage.question");
				}
				iconClassName = "questionIcon";
				break;
			case WARNING:
				if (T4JStringUtils.isNullOrEmpty(caption)) {
					caption = getI18nString("T4JWebApp.popupMessage.warning");
				}
				iconClassName = "warningIcon";
				break;
			case ERROR:
				if (T4JStringUtils.isNullOrEmpty(caption)) {
					caption = getI18nString("T4JWebApp.popupMessage.error");
				}
				iconClassName = "errorIcon";
				generateAcceptButton = true;
				break;
			default:
				if (T4JStringUtils.isNullOrEmpty(caption)) {
					caption = getI18nString("T4JWebApp.popupMessage.information");
				}
				iconClassName = "informationIcon";
				generateAcceptButton = true;
				break;
		}

		popupMessageWindow = new Window(caption);

		popupMessageWindow.addStyleName("modal-window");
		popupMessageWindow.setSizeUndefined();

		VerticalLayout modalWindowLayout = new VerticalLayout();
		modalWindowLayout.addStyleName("modal-window-layout");
		modalWindowLayout.setMargin(true);
		modalWindowLayout.setSizeUndefined();
		modalWindowLayout.setSpacing(true);

		HorizontalLayout messageLayout = new HorizontalLayout();
		messageLayout.setSpacing(true);

		CssLayout iconLayout = new CssLayout();
		iconLayout.addStyleName("modal-window-icon");
		iconLayout.addStyleName(iconClassName);
		iconLayout.setWidth(getSizeInEMs(100));
		iconLayout.setHeight(getSizeInEMs(100));

		messageLayout.addComponent(iconLayout);
		messageLayout.setComponentAlignment(iconLayout, Alignment.MIDDLE_LEFT);

		label.removeStyleName("modal-window-message-label");
		label.addStyleName("modal-window-message-label");

		label.setWidth(getSizeInEMs(300));

		messageLayout.addComponent(label);
		messageLayout.setComponentAlignment(label, Alignment.MIDDLE_LEFT);

		modalWindowLayout.addComponent(messageLayout);
		modalWindowLayout.setComponentAlignment(messageLayout, Alignment.TOP_LEFT);

		boolean addShortcutListener = false;

		if (T4JCollectionUtils.isNullOrEmpty(buttons)) {

			popupMessageWindow.setClosable(true);

			if (generateAcceptButton) {

				buttons = new ArrayList<Button>();

				Button defaultAcceptButton = new Button(getI18nString("button.accept"), new ClickListener() {

					private static final long serialVersionUID = 1L;

					public void buttonClick(ClickEvent event) {

						closePopupMessageModalWindow();
					}
				});

				buttons.add(defaultAcceptButton);

				addShortcutListener = true;
			}
		}

		if (false == T4JCollectionUtils.isNullOrEmpty(buttons)) {

			HorizontalLayout buttonsBarLayout = new HorizontalLayout();
			buttonsBarLayout.addStyleName("t4j-dialog-buttons");

			buttonsBarLayout.setSizeUndefined();

			buttonsBarLayout.setMargin(true, false, false, false);
			buttonsBarLayout.setSpacing(true);

			for (Iterator<Button> iterator = buttons.iterator(); iterator.hasNext();) {

				Button button = iterator.next();
				buttonsBarLayout.addComponent(button);
			}

			modalWindowLayout.addComponent(buttonsBarLayout);
			modalWindowLayout.setComponentAlignment(buttonsBarLayout, Alignment.BOTTOM_RIGHT);

			if (Boolean.FALSE.equals(isCloseButtonRendered)) {

				popupMessageWindow.setClosable(false);
			} else {

				popupMessageWindow.setClosable(true);
			}
		}

		Panel modalWindowPanel = new Panel(modalWindowLayout);

		modalWindowPanel.addStyleName(T4JConstants.LIGHT_PANEL_STYLE_CLASS);

		if (addShortcutListener) {

			modalWindowPanel.addAction(new ShortcutListener(T4JConstants.EMPTY_STRING, KeyCode.ENTER, null) {

				private static final long serialVersionUID = 1L;

				@Override
				public void handleAction(Object sender, Object target) {

					closePopupMessageModalWindow();
				}
			});
		}

		modalWindowPanel.setSizeUndefined();

		popupMessageWindow.setContent(modalWindowPanel);

		popupMessageWindow.center();

		popupMessageWindow.setDraggable(true);
		popupMessageWindow.setModal(true);
		popupMessageWindow.setName("modal-window-" + System.currentTimeMillis());
		popupMessageWindow.setResizable(false);

		getMainWindow().addWindow(popupMessageWindow);

		if (popupMessageWindow.isClosable()) {

			popupMessageWindow.addListener(new CloseListener() {

				private static final long serialVersionUID = 1L;

				public void windowClose(CloseEvent e) {

					closePopupMessageModalWindow();
				}
			});
		}

		popupMessageWindow.setVisible(true);
	}

	public void showTrayNotification(T4JWebAppTrayNotificationTypeEnum type, String caption, String description) {

		int delay = -1;

		String styleName = "tray-information";

		switch (type) {

			case WARNING:

				if (T4JStringUtils.isNullOrEmpty(caption)) {
					caption = getI18nString("T4JWebApp.trayNotification.warning");
				}

				styleName = "tray-warning";

				break;
			case ERROR:

				if (T4JStringUtils.isNullOrEmpty(caption)) {
					caption = getI18nString("T4JWebApp.trayNotification.error");
				}

				styleName = "tray-error";

				break;

			default:

				if (T4JStringUtils.isNullOrEmpty(caption)) {
					caption = getI18nString("T4JWebApp.trayNotification.information");
				}

				delay = 3000;

				break;
		}

		Notification notification = new Notification(caption, description, Notification.TYPE_TRAY_NOTIFICATION, true);

		notification.setDelayMsec(delay);
		notification.setStyleName(styleName);

		getMainWindow().showNotification(notification);
	}

	public void windowClose(CloseEvent e) {

		logger.trace(T4JConstants.L4J_METHOD_START);

	}
}
