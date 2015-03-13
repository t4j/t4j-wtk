
package com.t4j.wtk.util;

import java.io.OutputStreamWriter;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public class T4JLogUtils implements Serializable {

	private static final long serialVersionUID = 1L;

	private static boolean isInitialized = false;

	private static Log logger;

	private T4JLogUtils() {

		super();
	}

	private static Log getLoggerInstance(String s) {

		if (false == isInitialized) {

			setupDefaultLogger();
		}

		if (T4JStringUtils.isNullOrEmpty(s)) {

			return LogFactory.getLog(T4JLogUtils.class.getPackage().getName());
		} else {

			return LogFactory.getLog(s);
		}
	}

	public static Log getLogger(Class<?> c) {

		if (null == c) {

			if (false == isInitialized) {

				setupDefaultLogger();
			}

			return LogFactory.getLog(T4JLogUtils.class.getPackage().getName());
		} else {

			try {

				return getLoggerInstance(c.getPackage().getName());
			} catch (Exception e) {

				return LogFactory.getLog(T4JLogUtils.class.getPackage().getName());
			}
		}
	}

	public static Log getLogger(String className) {

		if (T4JStringUtils.isNullOrEmpty(className)) {

			if (false == Boolean.TRUE.equals(isInitialized)) {

				setupDefaultLogger();
			}

			return LogFactory.getLog(T4JLogUtils.class.getPackage().getName());

		} else {

			try {

				return getLogger(Class.forName(className));
			} catch (Exception e1) {

				try {

					Package p = Package.getPackage(className);

					if (null == p) {

						return LogFactory.getLog(T4JLogUtils.class.getPackage().getName());
					} else {

						return getLoggerInstance(className);
					}
				} catch (Exception e2) {

					return LogFactory.getLog(T4JLogUtils.class.getPackage().getName());
				}
			}
		}
	}

	public static synchronized void setupDefaultLogger() {

		if (isInitialized) {
			return;
		}

		Logger rootLogger = null;

		try {
			rootLogger = Logger.getRootLogger();
		} catch (Exception e) {
			System.err.print("CAN NOT GET ROOT LOGGER");
			return;
		}

		if (null == rootLogger) {

			System.err.print("ROOT LOGGER IS NULL");
			return;
		}

		boolean hasAppenders = false;

		try {
			hasAppenders = rootLogger.getAllAppenders().hasMoreElements();
		} catch (Exception e) {
			// Ignore
		}

		if (hasAppenders) {

			if (isInitialized) {

				return;
			} else {

				LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Log4JLogger");
				logger = LogFactory.getLog(T4JLogUtils.class.getPackage().getName());
				logger.info("LOG4J CONFIGURED SUCCESSFULLY");
				isInitialized = Boolean.TRUE;
			}
		} else {

			try {

				PatternLayout patternLayout = new PatternLayout("%-5p %d{HH:mm:ss.SSS} --%t-- %c.%F @ %M(%L) %m%n");
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(System.out);

				ConsoleAppender consoleAppender = new ConsoleAppender();

				consoleAppender.setName("ApplicationConsoleAppender");
				consoleAppender.setLayout(patternLayout);
				consoleAppender.setWriter(outputStreamWriter);

				consoleAppender.activateOptions();

				rootLogger.addAppender(consoleAppender);
				rootLogger.setLevel(Level.ALL);

				LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Log4JLogger");
				logger = LogFactory.getLog(T4JLogUtils.class.getPackage().getName());
				logger.info("LOG4J CONFIGURED SUCCESSFULLY");

				isInitialized = true;

			} catch (Exception e) {

				System.err.println("CAN NOT CONFIGURE LOG4J");
				e.printStackTrace();
			}
		}
	}
}
