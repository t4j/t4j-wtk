
package com.t4j.wtk.util;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.t4j.wtk.T4JConstants;

public class T4JDateUtils implements Serializable {

	private static final long serialVersionUID = 1L;

	private T4JDateUtils() {

		super();
	}

	/**
	 * Realiza la comparación de fechas normalizadas (Establece la hora de una nueva instancia de fecha con la hora establecida las 12.00.00 del mediodía)<br>
	 *
	 * Crea dos nuevas instancias de calendar con cada una de las instancias pasadas como parámetro (No modifica el valor de las instancias pasadas como parámetro)<br>
	 *
	 * Establece la hora de las dos nuevas instancias creadas a las 12.00.00 del mediodía.<br>
	 *
	 * Realiza la comparación entre las dos nuevas instancias.<br>
	 *
	 * @param d1
	 * @param d2
	 * @return
	 */
	public static int compareNormalizedDates(Calendar d1, Calendar d2) {

		if (null == d1 || null == d2) {

			throw new IllegalArgumentException();
		}

		Calendar c1 = Calendar.getInstance();
		c1.setTimeInMillis(d1.getTimeInMillis());
		c1.set(Calendar.HOUR_OF_DAY, 12);
		c1.set(Calendar.MINUTE, 0);
		c1.set(Calendar.SECOND, 0);
		c1.set(Calendar.MILLISECOND, 0);

		Calendar c2 = Calendar.getInstance();
		c2.setTimeInMillis(d2.getTimeInMillis());
		c2.set(Calendar.HOUR_OF_DAY, 12);
		c2.set(Calendar.MINUTE, 0);
		c2.set(Calendar.SECOND, 0);
		c2.set(Calendar.MILLISECOND, 0);

		return c1.compareTo(c2);
	}

	/**
	 * Realiza la comparación de fechas normalizadas (Establece la hora de una nueva instancia de fecha con la hora establecida las 12.00.00 del mediodía)<br>
	 *
	 * Crea dos nuevas instancias de calendar con cada una de las instancias pasadas como parámetro (No modifica el valor de las instancias pasadas como parámetro)<br>
	 *
	 * Establece la hora de las dos nuevas instancias creadas a las 12.00.00 del mediodía.<br>
	 *
	 * Realiza la comparación entre las dos nuevas instancias.<br>
	 *
	 * @param d1
	 * @param d2
	 * @return
	 */
	public static int compareNormalizedDates(Date d1, Date d2) {

		if (null == d1 || null == d2) {

			throw new IllegalArgumentException();
		}

		Calendar c1 = Calendar.getInstance();
		c1.setTime(d1);

		Calendar c2 = Calendar.getInstance();
		c2.setTime(d2);

		return compareNormalizedDates(c1, c2);
	}

	public static String getFormattedTimestamp(Date d) {

		try {
			SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS");
			return dateFormatter.format(d);
		} catch (Exception e) {
			return T4JConstants.EMPTY_STRING;
		}
	}

}
