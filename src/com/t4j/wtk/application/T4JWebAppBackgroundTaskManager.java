
package com.t4j.wtk.application;

import java.io.Serializable;
import java.lang.Thread.UncaughtExceptionHandler;

import org.apache.commons.logging.Log;

import com.t4j.wtk.application.enums.T4JWebAppTrayNotificationTypeEnum;
import com.t4j.wtk.util.T4JLogUtils;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public class T4JWebAppBackgroundTaskManager implements Serializable, UncaughtExceptionHandler {

	private static final long serialVersionUID = 1L;

	private static final Log logger = T4JLogUtils.getLogger(T4JWebAppBackgroundTaskManager.class);

	private Boolean isRunning;

	private String backgroundTaskThreadName;

	private transient Thread backgroundTaskThread;

	private T4JWebAppBackgroundTask backgroundTask;

	public T4JWebAppBackgroundTaskManager(T4JWebAppBackgroundTask backgroundTask) {

		super();

		this.backgroundTask = backgroundTask;
	}

	private static synchronized String generateBackgroundTaskThreadName() {

		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			// Ignore
		}

		return "t4jBackgroundTaskThread_" + System.currentTimeMillis();
	}

	public void cancel() {

		backgroundTaskThread.interrupt();
	}

	public T4JWebAppBackgroundTask getBackgroundTask() {

		return backgroundTask;
	}

	public Boolean getIsRunning() {

		return isRunning;
	}

	public void start() {

		backgroundTaskThreadName = generateBackgroundTaskThreadName();

		backgroundTaskThread = new Thread(backgroundTask, backgroundTaskThreadName);

		backgroundTaskThread.setUncaughtExceptionHandler(this);

		backgroundTaskThread.setPriority(Thread.MIN_PRIORITY);

		backgroundTaskThread.start();
	}

	public void uncaughtException(Thread thread, Throwable throwable) {

		String errorDescription = "Thread: " + thread + ", Throwable: " + throwable;

		logger.error(errorDescription, throwable);

		try {
			T4JWebApp webApp = backgroundTask.getThreadWebApp();

			synchronized (webApp) {

				webApp.showTrayNotification(T4JWebAppTrayNotificationTypeEnum.ERROR, "Error", errorDescription);
			}
		} catch (Exception e) {
			// Ignore
		}
	}
}
