
package com.t4j.wtk.application;

import java.io.Serializable;

import org.apache.commons.logging.Log;

import com.t4j.wtk.util.T4JLogUtils;

/**
 *
 * @author Tenentia-4j, S.L.
 *
 */
public abstract class T4JWebAppBackgroundTask implements Runnable, Serializable {

	private static final long serialVersionUID = 1L;

	private static final Log logger = T4JLogUtils.getLogger(T4JWebAppBackgroundTask.class);

	private volatile Boolean isRunning;

	private int totalCount;

	private int counter;

	protected T4JWebApp threadWebApp;

	public T4JWebAppBackgroundTask() {

		this(null);
	}

	public T4JWebAppBackgroundTask(T4JWebApp threadWebApp) {

		super();

		this.threadWebApp = threadWebApp;
	}

	protected abstract int getCount() throws Exception;

	protected abstract boolean hasNext() throws Exception;

	protected abstract void processElement(int index) throws Exception;

	protected abstract void processNext() throws Exception;

	protected abstract void setup() throws Exception;

	protected abstract void showResults() throws Exception;

	public Boolean getIsRunning() {

		return isRunning;
	}

	public T4JWebApp getThreadWebApp() {

		return threadWebApp;
	}

	public void run() {

		isRunning = Boolean.TRUE;

		try {

			synchronized (threadWebApp) {

				threadWebApp.getBackgroundTaskProgressIndicator().setEnabled(true);
				threadWebApp.getBackgroundTaskProgressIndicator().setCaption(threadWebApp.getI18nString("T4JWebAppBackgroundTask.preparing"));
				Thread.sleep(1500);
			}

			setup();

			synchronized (threadWebApp) {

				threadWebApp.getBackgroundTaskProgressIndicator().setCaption(threadWebApp.getI18nString("T4JWebAppBackgroundTask.starting"));
				Thread.sleep(1500);
			}

			totalCount = getCount();

			if (totalCount > 0) {

				synchronized (threadWebApp) {

					threadWebApp.getBackgroundTaskProgressIndicator().setIndeterminate(false);
				}

				counter = 1;

				for (int i = 0; i < totalCount; i++) {

					try {
						processElement(i);
					} catch (Throwable t) {
						logger.error(t, t);
					}

					synchronized (threadWebApp) {

						threadWebApp.getBackgroundTaskProgressIndicator().setCaption(threadWebApp.getI18nString("T4JWebAppBackgroundTask.processedIndexOfTotal", new Object[] {
							Integer.valueOf(counter),
							Integer.valueOf(totalCount)
						}));

						threadWebApp.getBackgroundTaskProgressIndicator().setValue(Float.valueOf(1.0f * counter / totalCount));
					}

					counter++;

					if (Thread.interrupted()) {

						logger.info("Interrupted");
						break;
					}
				}
			} else {

				counter = 1;

				while (hasNext()) {

					try {
						processNext();
					} catch (Throwable t) {
						logger.error(t, t);
					}

					counter++;

					synchronized (threadWebApp) {

						threadWebApp.getBackgroundTaskProgressIndicator().setCaption(threadWebApp.getI18nString("T4JWebAppBackgroundTask.processedItemsCount", new Object[] {
							Integer.valueOf(counter)
						}));
					}

					if (Thread.interrupted()) {

						logger.info("Background task interrupted");
						break;
					}
				}
			}
		} catch (Throwable t) {

			logger.error(t, t);
		}

		isRunning = Boolean.FALSE;

		synchronized (threadWebApp) {

			threadWebApp.closeBackgroundTaskWindow();
		}

	}

	public void setThreadWebApp(T4JWebApp threadWebApp) {

		this.threadWebApp = threadWebApp;
	}
}
