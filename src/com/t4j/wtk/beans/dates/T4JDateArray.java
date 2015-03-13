
package com.t4j.wtk.beans.dates;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.t4j.wtk.T4JConstants;
import com.t4j.wtk.util.T4JCollectionUtils;
import com.t4j.wtk.util.T4JStringUtils;

public class T4JDateArray implements Serializable {

	private static final long serialVersionUID = 1L;

	private String datePattern;

	private Date[] dateArray;

	private Locale locale;

	public T4JDateArray() {

		this(null);
	}

	public T4JDateArray(Date[] dateArray) {

		super();

		this.dateArray = dateArray;
	}

	public static T4JDateArray getNewInstance(Date[] dates) {

		return new T4JDateArray(dates);
	}

	private Date getNormalizedDate(Date d) {

		Calendar c = Calendar.getInstance(locale);

		c.setTime(d);

		c.set(Calendar.HOUR_OF_DAY, 12);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		return c.getTime();
	}

	private void sortDateArray() {

		try {

			List<Date> tmpList = Arrays.asList(dateArray);
			Collections.sort(tmpList);
			dateArray = tmpList.toArray(dateArray);
		} catch (Exception e) {
			// Ignore
		}
	}

	public Date[] getDateArray() {

		sortDateArray();

		return dateArray;
	}

	public String getDatePattern() {

		return datePattern;
	}

	public Locale getLocale() {

		return locale;
	}

	public void parseString(String source) {

		if (T4JStringUtils.isNullOrEmpty(source)) {

			dateArray = new Date[0];
		} else {

			try {

				String[] tmpStringArray = source.split("\\r\\n");

				List<Date> tmpList = new ArrayList<Date>();

				SimpleDateFormat dateFormatter = null;

				try {
					dateFormatter = new SimpleDateFormat(datePattern, locale);
				} catch (Exception e) {
					// Ignore
				}

				SimpleDateFormat defaultDateFormatter = new SimpleDateFormat(T4JConstants.DEFAULT_DATE_PATTERN, Locale.US);

				for (int i = 0; i < tmpStringArray.length; i++) {

					String tmpDateString = tmpStringArray[i];

					if (T4JStringUtils.isNullOrEmpty(tmpDateString)) {

						continue;
					} else {

						Date tmpDate = null;

						if (null == dateFormatter) {

							try {
								tmpDate = defaultDateFormatter.parse(tmpDateString);
							} catch (Exception e) {
								// Ignore
							}
						} else {

							try {
								tmpDate = dateFormatter.parse(tmpDateString);
							} catch (Exception e1) {
								try {
									tmpDate = defaultDateFormatter.parse(tmpDateString);
								} catch (Exception e2) {
									// Ignore
								}
							}
						}

						if (null == tmpDate) {

							continue;
						} else {

							tmpDate = getNormalizedDate(tmpDate);

							if (false == tmpList.contains(tmpDate)) {

								tmpList.add(tmpDate);
							}
						}
					}
				}

				dateArray = new Date[tmpList.size()];

				dateArray = tmpList.toArray(dateArray);

			} catch (Exception e) {
				// Ignore
			}
		}
	}

	public void setDateArray(Date[] dateArray) {

		this.dateArray = dateArray;
	}

	public void setDatePattern(String datePattern) {

		this.datePattern = datePattern;
	}

	public void setLocale(Locale locale) {

		this.locale = locale;
	}

	@Override
	public String toString() {

		if (T4JCollectionUtils.isNullOrEmpty(dateArray)) {

			return T4JConstants.EMPTY_STRING;
		} else {

			StringBuffer stringBuffer = new StringBuffer(1024);

			SimpleDateFormat dateFormatter = null;

			try {
				dateFormatter = new SimpleDateFormat(datePattern, locale);
			} catch (Exception e) {
				// Ignore
			}

			SimpleDateFormat defaultDateFormatter = new SimpleDateFormat(T4JConstants.DEFAULT_DATE_PATTERN, Locale.US);

			sortDateArray();

			for (int i = 0; i < dateArray.length; i++) {

				Date tmpDate = dateArray[i];

				if (null == tmpDate) {

					continue;
				} else {

					String tmpString = T4JConstants.EMPTY_STRING;

					if (null == dateFormatter) {

						try {
							tmpString = defaultDateFormatter.format(tmpDate);
						} catch (Exception e) {
							// Ignore
						}

					} else {
						try {
							tmpString = dateFormatter.format(tmpDate);
						} catch (Exception e1) {
							try {
								tmpString = defaultDateFormatter.format(tmpDate);
							} catch (Exception e2) {
								// Ignore
							}
						}
					}

					if (false == T4JStringUtils.isNullOrEmpty(tmpString)) {

						if (0 < stringBuffer.length()) {

							stringBuffer.append("\r\n");
						}

						stringBuffer.append(tmpString);
					}
				}
			}

			return stringBuffer.toString();
		}
	}
}
