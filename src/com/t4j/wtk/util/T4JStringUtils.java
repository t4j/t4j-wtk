
package com.t4j.wtk.util;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import com.t4j.wtk.T4JConstants;

/**
 *
 * @author Tenentia-4j, S.L.
 *
 */
public class T4JStringUtils implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String STRING_NON_SPANISH_CHARACTERS = "[^A-Za-z0-9áéíñóúÁÉÍÑÓÚ]+";

	private T4JStringUtils() {

		super();
	}

	public static boolean booleanCompare(String s1, String s2) {

		if (null == s1 && null == s2) {

			return true;
		} else {

			if (null == s1 || null == s2) {
				return false;
			} else {

				return s1.equals(s2);
			}
		}
	}

	public static String decodeUTF8(String s) throws UnsupportedEncodingException {

		if (isNullOrEmpty(s)) {

			return s;
		} else {

			byte[] stringBytes = s.getBytes();
			String methodResult = new String(stringBytes, "UTF-8");
			return methodResult;
		}
	}

	public static boolean isNullOrEmpty(Object o) {

		if (null == o) {
			return true;
		} else {
			String s = o.toString();
			if (0 == s.trim().length()) {
				return true;
			} else {
				return false;
			}
		}
	}

	public static String replaceHtmlEntities(String s) {

		if (isNullOrEmpty(s)) {

			return s;
		} else {

			s = s.replaceAll("\\x26", "&amp;");
			s = s.replaceAll("\\x3C", "&lt;");
			s = s.replaceAll("\\x3E", "&gt;");

			return s;
		}
	}

	public static String replaceNonSpanishCharacters(String s) {

		if (isNullOrEmpty(s)) {

			return s;
		} else {

			try {
				return s.replaceAll(STRING_NON_SPANISH_CHARACTERS, "%");
			} catch (Exception e) {
				return T4JConstants.EMPTY_STRING;
			}
		}
	}

	public static String shrinkToBytesLength(String source, String encoding, int maxLength) throws UnsupportedEncodingException {

		if (null == source) {

			return null;
		} else {

			if (null == encoding) {

				encoding = "UTF-8";
			}

			if (1 > maxLength) {

				return T4JConstants.EMPTY_STRING;
			} else {

				char[] tmpCharArray = source.toCharArray();
				char[] tmpCharArrayCopy = new char[tmpCharArray.length];

				System.arraycopy(tmpCharArray, 0, tmpCharArrayCopy, 0, tmpCharArray.length);

				String tmpString = new String(tmpCharArrayCopy);
				byte[] tmpByteArray = tmpString.getBytes(encoding);

				while (tmpByteArray.length > maxLength) {

					tmpString = tmpString.substring(0, tmpString.length() - 1);
					tmpByteArray = tmpString.getBytes(encoding);
				}

				return tmpString;
			}
		}
	}
}
