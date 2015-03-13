
package com.t4j.wtk.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;

import com.t4j.wtk.T4JConstants;

/**
 *
 * @author Tenentia-4j, S.L.
 *
 */
public class T4JExceptionUtils implements Serializable {

	private static final long serialVersionUID = 1L;

	private T4JExceptionUtils() {

		super();
	}

	public static String getStackTrace(Throwable t, int maxLength) {

		try {

			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(65536);

			PrintStream printStream = new PrintStream(byteArrayOutputStream);

			t.printStackTrace(printStream);

			printStream.flush();
			printStream.close();

			byteArrayOutputStream.flush();
			byteArrayOutputStream.close();

			String tmpString = new String(byteArrayOutputStream.toByteArray());

			if (maxLength > 4) {

				int tmpInt = maxLength - 4;

				if (tmpString.length() > tmpInt) {
					tmpString = tmpString.substring(0, tmpInt);
				}

				while (tmpString.getBytes(T4JConstants.DEFAULT_ENCODING).length > tmpInt) {
					tmpString = tmpString.substring(0, tmpString.length() - 1);
				}

				tmpString = tmpString.concat(" ...");
			}

			return tmpString;
		} catch (Exception e) {

			return T4JConstants.EMPTY_STRING;
		}
	}

}
