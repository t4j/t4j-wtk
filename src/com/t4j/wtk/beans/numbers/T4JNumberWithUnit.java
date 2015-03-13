
package com.t4j.wtk.beans.numbers;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import com.t4j.wtk.T4JConstants;
import com.t4j.wtk.application.T4JWebApp;
import com.t4j.wtk.util.T4JStringUtils;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public class T4JNumberWithUnit extends Number  {

	private static final long serialVersionUID = 1L;

	private Number number;

	private String pattern;

	private String unit;

	public T4JNumberWithUnit(Number number, String unit, String pattern) {

		super();

		if (null == number) {
			throw new IllegalArgumentException();
		}

		this.number = number;
		this.unit = unit;
		this.pattern = pattern;
	}

	public static T4JNumberWithUnit newInstance(Number number, String unit) {

		return new T4JNumberWithUnit(number, unit, null);
	}

	public static T4JNumberWithUnit newInstance(Number number, String unit, String pattern) {

		return new T4JNumberWithUnit(number, unit, pattern);
	}

	@Override
	public double doubleValue() {

		return number.doubleValue();
	}

	@Override
	public float floatValue() {

		return number.floatValue();
	}

	public Number getNumber() {

		return number;
	}

	public String getPattern() {

		return pattern;
	}

	public String getUnit() {

		return unit;
	}

	@Override
	public int intValue() {

		return number.intValue();
	}

	@Override
	public long longValue() {

		return number.longValue();
	}

	public void setNumber(Number number) {

		this.number = number;
	}

	public void setPattern(String pattern) {

		this.pattern = pattern;
	}

	public void setUnit(String unit) {

		this.unit = unit;
	}

	@Override
	public String toString() {

		if (null == unit) {

			unit = T4JConstants.EMPTY_STRING;
		}

		if (T4JStringUtils.isNullOrEmpty(pattern)) {

			if (number instanceof Double) {
				pattern = "#,##0.00";
			} else {
				pattern = "#,##0";
			}

		}

		try {

			Locale sessionLocale = T4JWebApp.getInstance().getLocale();

			DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(sessionLocale);
			DecimalFormat decimalFormat = new DecimalFormat(pattern, decimalFormatSymbols);
			return decimalFormat.format(number) + (T4JConstants.EMPTY_STRING.equals(unit) ? T4JConstants.EMPTY_STRING : " " + unit);
		} catch (Exception e) {

			return number.toString() + (T4JConstants.EMPTY_STRING.equals(unit) ? T4JConstants.EMPTY_STRING : " " + unit);
		}
	}

}
