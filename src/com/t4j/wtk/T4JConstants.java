
package com.t4j.wtk;

import java.io.Serializable;
import java.util.Locale;

/**
 * Interfaz con constantes generales.
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public interface T4JConstants extends Serializable {

	public static final byte[] NULL_BYTES = {
		0x6e,
		0x75,
		0x6c,
		0x6c
	};

	public static final byte[] ZIP_FILE_START_BYTES = {
		0x50,
		0x4B,
		0x03,
		0x04
	};

	public static final int MS_EXCEL_SHEET_MAX_ROWS = 1024 * 64 - 2;

	public static final Double DOUBLE_PIXEL_TO_EM = Double.valueOf(0.0769230769);

	public static final Double DOUBLE_PIXEL_TO_EM_IE6 = Double.valueOf(0.0750018751);

	public static final String DEFAULT_DATE_PATTERN = "EEE MMM dd HH:mm:ss zzz yyyy";

	public static final String DEFAULT_ERROR_URL = "/";

	public static final String DEFAULT_ENCODING = "UTF-8";

	public static final String DEFAULT_THEME = "t4j-wtk";

	public static final String DEFAULT_BUNDLE_BASE_NAME = "com.t4j.wtk.resources.i18n.messages";

	public static final String DEFAULT_GENERATOR_PACKAGE = "com.t4j.wtk.forms.fields.generators";

	public static final String DEFAULT_VALIDATOR_PACKAGE = "com.t4j.wtk.forms.fields.validators";

	public static final String EMPTY_STRING = "";

	public static final String FULL_WIDTH_PERCENTAGE = "100%";

	public static final String L4J_METHOD_START = "INICIO";

	public static final String LINK_BUTTON_STYLE_CLASS = "link";

	public static final String LIGHT_PANEL_STYLE_CLASS = "light";

	public static final String STRING_NON_SPANISH_CHARACTERS = "[^A-Za-z0-9áçéíñóúÁÇÉÍÑÓÚ]+";

	public static final String TESTS_PASSWORD_HASH = "921519917873387C89BDDC9E2A73C3DE8E608AF4";

	public static final String HTML_ACCESIBLE_ELEMENT = "<span class='accesible'></span>";

	public static final String HTML_WHITESPACE = "&nbsp;";

	public static final String WHITESPACE_STRING = " ";

	public static final Locale DEFAULT_LOCALE = new Locale("es", "ES");

}
