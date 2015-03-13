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

/**
 * Server-side package constants.
 * 
 * @author Vaadin Ltd.
 * 
 */
public interface Constants {

	static final int DEFAULT_BUFFER_SIZE = 32 * 1024;

	static final int DEFAULT_THEME_CACHETIME = 1000 * 60 * 60 * 24;

	static final int MAX_BUFFER_SIZE = 64 * 1024;

	static final String AJAX_UIDL_URI = "/UIDL";

	static final String DEFAULT_THEME_NAME = "reindeer";

	// Name of the default widget set, used if not specified in web.xml
	static final String DEFAULT_WIDGETSET = "com.t4j.wtk.widgetset.T4jWtkWidgetset"; // "com.vaadin.terminal.gwt.DefaultWidgetSet";

	static final String ERROR_NO_WINDOW_FOUND = "No window found. Did you remember to setMainWindow()?";

	static final String INVALID_SECURITY_KEY_MSG = "Invalid security key.";

	static final String NOT_PRODUCTION_MODE_INFO = "\n" //
		+ "=================================================================\n" //
		+ "Vaadin is running in DEBUG MODE.\nAdd productionMode=true to web.xml " //
		+ "to disable debug features.\nTo show debug window, add ?debug to " //
		+ "your application URL.\n" //
		+ "=================================================================";

	// Configurable parameter names
	static final String PARAMETER_VAADIN_RESOURCES = "Resources";

	// Widget set parameter name
	static final String PARAMETER_WIDGETSET = "widgetset";

	static final String PORTAL_PARAMETER_VAADIN_RESOURCE_PATH = "vaadin.resources.path";

	static final String PORTAL_PARAMETER_VAADIN_THEME = "vaadin.theme";

	// portal configuration parameters
	static final String PORTAL_PARAMETER_VAADIN_WIDGETSET = "vaadin.widgetset";

	static final String SERVLET_PARAMETER_DEBUG = "Debug";

	static final String SERVLET_PARAMETER_DISABLE_XSRF_PROTECTION = "disable-xsrf-protection";

	static final String SERVLET_PARAMETER_PRODUCTION_MODE = "productionMode";

	static final String SERVLET_PARAMETER_RESOURCE_CACHE_TIME = "resourceCacheTime";

	static final String URL_PARAMETER_CLOSE_APPLICATION = "closeApplication";

	static final String URL_PARAMETER_REPAINT_ALL = "repaintAll";

	static final String URL_PARAMETER_RESTART_APPLICATION = "restartApplication";

	static final String URL_PARAMETER_THEME = "theme";

	static final String WARNING_RESOURCE_CACHING_TIME_NOT_NUMERIC = "\n" //
		+ "==============================================================\n" //
		+ "WARNING: resourceCacheTime has been set to a non integer value " //
		+ "in web.xml. The default of 1h will be used.\n" //
		+ "==============================================================";

	static final String WARNING_XSRF_PROTECTION_DISABLED = "\n" //
		+ "===========================================================\n" //
		+ "WARNING: Cross-site request forgery protection is disabled!\n" //
		+ "===========================================================";

	static final String WIDGETSET_DIRECTORY_PATH = "VAADIN/widgetsets/";

	static final String WIDGETSET_MISMATCH_INFO = "\n" //
		+ "=================================================================\n" //
		+ "The widgetset in use does not seem to be built for the Vaadin\n" //
		+ "version in use. This might cause strange problems - a\n" //
		+ "recompile/deploy is strongly recommended.\n" //
		+ " Vaadin version: %s\n" //
		+ " Widgetset version: %s\n" //
		+ "=================================================================";

	final String THEME_DIRECTORY_PATH = "VAADIN/themes/";

}
