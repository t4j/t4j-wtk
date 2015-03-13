
package com.t4j.wtk.beans.dates;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.t4j.wtk.application.T4JWebApp;
import com.t4j.wtk.components.forms.T4JFieldCaption;
import com.t4j.wtk.components.forms.T4JFieldDescriptor;
import com.t4j.wtk.components.forms.T4JFieldTypeEnum;
import com.t4j.wtk.util.T4JStringUtils;

/**
 * Componente Java que representa un rango de fechas y su plantilla para las conversiones de texto a fecha y viceversa.
 *
 * @author Tenentia-4j, S.L.
 *
 */
public class T4JDateRange implements Serializable {

	private static final long serialVersionUID = 1L;

	private String datePattern;

	@T4JFieldCaption(
		def = "Fecha inicial",
		en = "From date")
	@T4JFieldDescriptor(
		fieldType = T4JFieldTypeEnum.DATEFIELD)
	private Date fromDate;

	@T4JFieldCaption(
		def = "Fecha final",
		en = "To date")
	@T4JFieldDescriptor(
		fieldType = T4JFieldTypeEnum.DATEFIELD)
	private Date toDate;

	public T4JDateRange() {

		super();
	}

	public T4JDateRange(Date fromDate, Date toDate) {

		super();

		this.fromDate = fromDate;
		this.toDate = toDate;
	}

	public String getDatePattern() {

		return datePattern;
	}

	public Date getFromDate() {

		return fromDate;
	}

	public Date getToDate() {

		return toDate;
	}

	public void setDatePattern(String datePattern) {

		this.datePattern = datePattern;
	}

	public void setFromDate(Date fromDate) {

		this.fromDate = fromDate;
	}

	public void setToDate(Date toDate) {

		this.toDate = toDate;
	}

	@Override
	public String toString() {

		DateFormat dateFormatter = null;

		Locale sessionLocale = T4JWebApp.getInstance().getLocale();

		if (T4JStringUtils.isNullOrEmpty(datePattern)) {

			dateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM, sessionLocale);
		} else {

			try {
				dateFormatter = new SimpleDateFormat(datePattern, sessionLocale);
			} catch (Exception e) {
				dateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM, sessionLocale);
			}

		}

		String fromDateTxt = "???";

		if (null != fromDate) {

			try {
				fromDateTxt = dateFormatter.format(fromDate);
			} catch (Exception e) {
				fromDateTxt = "???";
			}
		}

		String toDateTxt = "???";

		if (null != toDate) {

			try {
				toDateTxt = dateFormatter.format(toDate);
			} catch (Exception e) {
				toDateTxt = "???";
			}
		}

		return fromDateTxt + " - " + toDateTxt;
	}
}
