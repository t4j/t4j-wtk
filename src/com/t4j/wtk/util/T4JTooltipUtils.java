
package com.t4j.wtk.util;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.t4j.wtk.T4JConstants;
import com.t4j.wtk.application.T4JWebApp;
import com.t4j.wtk.beans.T4JKeyValuePair;
import com.vaadin.data.Item;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public class T4JTooltipUtils implements Serializable {

	private static final long serialVersionUID = 1L;

	private T4JTooltipUtils() {

		super();
	}

	public static String generatePopupHtmlMessage(Map<?, ?> map) {

		if (T4JCollectionUtils.isNullOrEmpty(map)) {

			return T4JConstants.EMPTY_STRING;
		}

		StringBuffer stringBuffer = new StringBuffer(1024);

		stringBuffer.append("<div class=\"t4j-tooltip\">");
		stringBuffer.append("<table style='font-size:85%;'>");

		int rowsCount = 0;

		Set<?> keySet = map.keySet();

		for (Iterator<?> iterator = keySet.iterator(); iterator.hasNext();) {

			Object key = iterator.next();

			if (T4JStringUtils.isNullOrEmpty(key)) {

				continue;
			} else {

				Object value = map.get(key);

				if (T4JStringUtils.isNullOrEmpty(value)) {

					continue;
				} else {

					stringBuffer.append("<tr>");
					stringBuffer.append("<td style='text-align: right; vertical-align: top;'>");
					stringBuffer.append("<b>" + key.toString() + "</b>");
					stringBuffer.append("</td>");
					stringBuffer.append("<td style='padding-left: 0.5em;'>");
					stringBuffer.append(value.toString());
					stringBuffer.append("</td>");
					stringBuffer.append("</tr>");

					rowsCount++;

				}
			}
		}

		stringBuffer.append("</table>");
		stringBuffer.append("</div>");

		if (0 == rowsCount) {

			return T4JConstants.EMPTY_STRING;
		} else {

			return stringBuffer.toString();
		}
	}

	public static String generatePopupHtmlMessage(String s) {

		if (T4JStringUtils.isNullOrEmpty(s)) {

			return T4JConstants.EMPTY_STRING;
		} else {

			return "<div class=\"t4j-tooltip\">" + s + "</div>";
		}
	}

	public static String generatePopupMessage(Item item, Object[] propertyIds) {

		if (null == item) {

			return T4JConstants.EMPTY_STRING;
		}

		if (T4JCollectionUtils.isNullOrEmpty(propertyIds)) {

			return T4JConstants.EMPTY_STRING;
		}

		StringBuffer stringBuffer = new StringBuffer(1024);

		stringBuffer.append("<div class=\"t4j-tooltip\">");
		stringBuffer.append("<table style='font-size:85%;'>");

		int rowsCount = 0;

		for (int i = 0; i < propertyIds.length; i++) {

			Object propertyId = propertyIds[i];

			if (T4JStringUtils.isNullOrEmpty(propertyId)) {

				continue;
			} else {

				Object value = item.getItemProperty(propertyId).getValue();

				if (T4JStringUtils.isNullOrEmpty(value)) {

					continue;
				} else {

					stringBuffer.append("<tr>");
					stringBuffer.append("<td>");
					stringBuffer.append("<b>" + propertyId + "</b>");
					stringBuffer.append("</td>");
					stringBuffer.append("<td style='padding-left: 0.5em;'>");
					stringBuffer.append(value);
					stringBuffer.append("</td>");
					stringBuffer.append("</tr>");

					rowsCount++;

				}
			}

		}

		stringBuffer.append("</table>");
		stringBuffer.append("</div>");

		if (0 == rowsCount) {

			return T4JConstants.EMPTY_STRING;
		} else {

			return stringBuffer.toString();
		}

	}

	public static String generatePopupMessage(List<T4JKeyValuePair> list) {

		if (T4JCollectionUtils.isNullOrEmpty(list)) {

			return T4JConstants.EMPTY_STRING;
		}

		StringBuffer stringBuffer = new StringBuffer(1024);

		stringBuffer.append("<div class=\"t4j-tooltip\">");
		stringBuffer.append("<table style='font-size:85%;'>");

		int rowsCount = 0;

		for (Iterator<T4JKeyValuePair> iterator = list.iterator(); iterator.hasNext();) {

			T4JKeyValuePair keyValuePair = iterator.next();

			if (T4JStringUtils.isNullOrEmpty(keyValuePair.getKey())) {

				continue;
			} else {

				Object value = keyValuePair.getValue();

				if (T4JStringUtils.isNullOrEmpty(value)) {

					continue;
				} else {

					stringBuffer.append("<tr>");
					stringBuffer.append("<td>");
					stringBuffer.append("<b>" + T4JStringUtils.replaceHtmlEntities(keyValuePair.getKey().toString()) + "</b>");
					stringBuffer.append("</td>");
					stringBuffer.append("<td style='padding-left: 0.5em;'>");
					stringBuffer.append(T4JStringUtils.replaceHtmlEntities(value.toString()));
					stringBuffer.append("</td>");
					stringBuffer.append("</tr>");

					rowsCount++;

				}
			}

		}

		stringBuffer.append("</table>");
		stringBuffer.append("</div>");

		if (0 == rowsCount) {

			return T4JConstants.EMPTY_STRING;
		} else {

			return stringBuffer.toString();
		}
	}

	public static String generatePopupMessage(Map<?, ?> map) {

		if (T4JCollectionUtils.isNullOrEmpty(map)) {

			return T4JConstants.EMPTY_STRING;
		}

		StringBuffer stringBuffer = new StringBuffer(1024);

		stringBuffer.append("<div class=\"t4j-tooltip\">");
		stringBuffer.append("<table style='font-size:85%;'>");

		int rowsCount = 0;

		Set<?> keySet = map.keySet();

		for (Iterator<?> iterator = keySet.iterator(); iterator.hasNext();) {

			Object key = iterator.next();

			if (T4JStringUtils.isNullOrEmpty(key)) {

				continue;
			} else {

				Object value = map.get(key);

				if (T4JStringUtils.isNullOrEmpty(value)) {

					continue;
				} else {

					stringBuffer.append("<tr>");
					stringBuffer.append("<td style='text-align: right; vertical-align: top;'>");
					stringBuffer.append("<b>" + T4JStringUtils.replaceHtmlEntities(key.toString()) + "</b>");
					stringBuffer.append("</td>");
					stringBuffer.append("<td style='padding-left: 0.5em;'>");
					stringBuffer.append(T4JStringUtils.replaceHtmlEntities(value.toString()));
					stringBuffer.append("</td>");
					stringBuffer.append("</tr>");

					rowsCount++;

				}
			}
		}

		stringBuffer.append("</table>");
		stringBuffer.append("</div>");

		if (0 == rowsCount) {

			return T4JConstants.EMPTY_STRING;
		} else {

			return stringBuffer.toString();
		}
	}

	public static String generatePopupMessage(String s) {

		if (T4JStringUtils.isNullOrEmpty(s)) {

			return T4JConstants.EMPTY_STRING;
		} else {

			return "<div class=\"t4j-tooltip\"><span style='font-size:85%;'>" + T4JStringUtils.replaceHtmlEntities(s) + "</span></div>";
		}
	}

	public static String generateWarningMessage(String s) {

		if (T4JStringUtils.isNullOrEmpty(s)) {

			return T4JConstants.EMPTY_STRING;
		} else {

			return "<div class='table-warning'>" + //
				"<div class='table-warning-caption'>" + T4JWebApp.getInstance().getI18nString("T4JTooltipUtils.warningMessage") + "</div>" + //
				"<div class='table-warning-message'>" + T4JStringUtils.replaceHtmlEntities(s) + "</div>" + //
				"</div>";
		}
	}
}
