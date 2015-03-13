
package com.t4j.wtk.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.t4j.wtk.T4JConstants;
import com.t4j.wtk.application.T4JWebApp;
import com.t4j.wtk.beans.numbers.T4JDoubleWithUnit;
import com.t4j.wtk.beans.numbers.T4JNumberWithUnit;
import com.t4j.wtk.util.T4JStringUtils;
import com.vaadin.data.util.ObjectProperty;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public class T4JProperty extends ObjectProperty<Serializable> {

	private static final long serialVersionUID = 1L;

	private String propertyName;

	public T4JProperty(Serializable value) {

		this(null, value, null, false);
	}

	public T4JProperty(Serializable value, Class<Serializable> type) {

		this(null, value, type, false);
	}

	public T4JProperty(Serializable value, Class<Serializable> type, boolean readOnly) {

		this(null, value, type, readOnly);
	}

	public T4JProperty(String propertyName, Serializable value) {

		this(propertyName, value, null, false);
	}

	public T4JProperty(String propertyName, Serializable value, Class<Serializable> type) {

		this(propertyName, value, type, false);
	}

	@SuppressWarnings("unchecked")
	public T4JProperty(String propertyName, Serializable value, Class<Serializable> type, boolean readOnly) {

		super(value, null == type ? (Class<Serializable>) value.getClass() : type, readOnly);

		this.propertyName = propertyName;
	}

	private String getDateTimePattern(Object propertyId, Locale locale) {

		String pattern = "dd-MMM-yyyy";

		String tmpString = null;

		if (null == propertyId) {

			if (false == T4JStringUtils.isNullOrEmpty(propertyName)) {

				tmpString = propertyName;
			}
		} else {

			tmpString = propertyId.toString();
		}

		if (null == tmpString) {

			if (locale.getLanguage().equals("en")) {

				pattern = "MMM, dd yyyy";
			} else if (locale.getLanguage().equals("pt")) {

				pattern = "yyyy-MM-dd";
			} else if (locale.getLanguage().equals("cs")) {

				pattern = "dd.MM.yyyy";
			}
		} else {

			String tmpLowerCaseString = tmpString.toLowerCase();

			if (-1 != tmpLowerCaseString.indexOf("rosettanet")) {

				// TODO - Implement

				if (-1 != tmpLowerCaseString.indexOf("timestamp")) {

				} else if (-1 != tmpLowerCaseString.indexOf("time")) {

				} else {

				}
			} else if (-1 != tmpLowerCaseString.indexOf("xcbl")) {

				// TODO - Implement

				if (-1 != tmpLowerCaseString.indexOf("timestamp")) {

				} else if (-1 != tmpLowerCaseString.indexOf("time")) {

				} else {

				}
			} else if (-1 != tmpLowerCaseString.indexOf("cxml")) {

				// TODO - Implement

				if (-1 != tmpLowerCaseString.indexOf("timestamp")) {

				} else if (-1 != tmpLowerCaseString.indexOf("time")) {

				} else {

				}
			} else {

				if (-1 != tmpLowerCaseString.indexOf("timestamp")) {

					pattern = "yyyy-MM-dd'@'HH:mm:ss";
				} else if (-1 != tmpLowerCaseString.indexOf("time")) {

					if (locale.getLanguage().equals("en")) {

						pattern = "MMM, dd yyyy hh:mm a";

					} else if (locale.getLanguage().equals("pt")) {

						pattern = "dd/MM/yyyy HH:mm";
					} else if (locale.getLanguage().equals("cs")) {

						pattern = "dd.MM.yyyy hh.mm a";
					} else {

						pattern = "dd-MMM-yyyy HH:mm";
					}
				} else {

					if (locale.getLanguage().equals("en")) {

						pattern = "MMM, dd yyyy";
					} else if (locale.getLanguage().equals("pt")) {

						pattern = "dd/MM/yyyy";
					} else if (locale.getLanguage().equals("cs")) {

						pattern = "dd.MM.yyyy";
					}
				}
			}
		}

		return pattern;
	}

	private String getNumberPattern(Object propertyId, Number number, Locale locale) {

		if (number instanceof Float || number instanceof Double || number instanceof BigDecimal || number instanceof T4JDoubleWithUnit) {

			if (null == propertyId) {

				return "#,##0.00";
			} else {

				String propertyName = propertyId.toString().toLowerCase();

				if (-1 != propertyName.indexOf("percent")) {

					return "#,##0.00 '%'";
				} else if (-1 != propertyName.indexOf("quantity")) {

					return "#,##0.0000";
				} else if (-1 != propertyName.indexOf("price")) {

					return "#,##0.0000";
				} else if (-1 != propertyName.indexOf("kilobytes")) {

					return "#,##0.00 'KB'";
				} else if (-1 != propertyName.indexOf("megabytes")) {

					return "#,##0.00 'MB'";
				} else {

					return "#,##0.00";
				}
			}

		} else {

			if (null == propertyId) {

				return "0";
			} else {

				String propertyName = propertyId.toString().toLowerCase();

				if (-1 != propertyName.indexOf("bytes")) {

					return "#,##0 'bytes'";
				} else {

					return "0";
				}
			}
		}
	}

	public String getPropertyName() {

		return propertyName;
	}

	public void setPropertyName(String propertyName) {

		this.propertyName = propertyName;
	}

	@Override
	public String toString() {

		Object tmpValue = getValue();

		if (null == tmpValue) {

			return T4JConstants.EMPTY_STRING;
		} else {

			Locale sessionLocale = T4JWebApp.getInstance().getLocale();

			if (null == sessionLocale) {

				sessionLocale = new Locale("es", "ES");
			}

			if (tmpValue instanceof Boolean) {

				if (Boolean.TRUE.equals(tmpValue)) {

					return T4JWebApp.getInstance().getI18nString("boolean.true");
				} else {

					return T4JWebApp.getInstance().getI18nString("boolean.false");
				}
			} else if (tmpValue instanceof T4JNumberWithUnit) {

				return tmpValue.toString();
			} else if (tmpValue instanceof Number) {

				Number tmpNumber = (Number) tmpValue;
				String pattern = getNumberPattern(propertyName, tmpNumber, sessionLocale);
				DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(sessionLocale);
				DecimalFormat decimalFormat = new DecimalFormat(pattern, decimalFormatSymbols);

				return decimalFormat.format(tmpValue);

			} else if (tmpValue instanceof Date) {

				String pattern = getDateTimePattern(propertyName, sessionLocale);
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, sessionLocale);
				return simpleDateFormat.format((Date) tmpValue);

			} else {

				return super.toString();
			}
		}
	}
}
