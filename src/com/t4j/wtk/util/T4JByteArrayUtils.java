
package com.t4j.wtk.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Clase de utilidades para manipular cadenas de bytes.
 *
 * @author Tenentia-4j, S.L.
 *
 */
public class T4JByteArrayUtils implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Private constructor.
	 */
	private T4JByteArrayUtils() {

		super();
	}

	public static Object byteArrayToObject(byte[] byteArray) {

		if (T4JCollectionUtils.isNullOrEmpty(byteArray)) {

			return null;
		}

		ObjectInputStream objectInputStream = null;
		ByteArrayInputStream byteArrayInputStream = null;

		Object methodResult = null;

		try {

			byteArrayInputStream = new ByteArrayInputStream(byteArray);
			objectInputStream = new ObjectInputStream(byteArrayInputStream);

			methodResult = objectInputStream.readObject();

			objectInputStream.close();
			byteArrayInputStream.close();
		} catch (Exception e) {

			// Ignore
		} finally {

			try {
				objectInputStream.close();
			} catch (Exception e) {
				// Ignore exception
			}

			objectInputStream = null;

			try {
				byteArrayInputStream.close();
			} catch (Exception e) {
				// Ignore exception
			}

			byteArrayInputStream = null;
		}

		return methodResult;
	}

	public static ObjectInputStream byteArrayToObjectInputStream(byte[] byteArray) {

		if (T4JCollectionUtils.isNullOrEmpty(byteArray)) {

			return null;
		}

		try {
			return new ObjectInputStream(new ByteArrayInputStream(byteArray));
		} catch (Exception e) {
			// Ignore exception
		}

		return null;
	}

	public static int indexOf(byte[] data, byte[] pattern) {

		return indexOf(data, pattern, 0);
	}

	public static int indexOf(byte[] data, byte[] pattern, int fromIndex) {

		if (T4JCollectionUtils.isNullOrEmpty(data)) {

			throw new IllegalArgumentException();
		}

		if (T4JCollectionUtils.isNullOrEmpty(pattern)) {

			throw new IllegalArgumentException();
		}

		int dataLength = data.length;

		int patternLength = pattern.length;

		if (patternLength > dataLength) {

			throw new IllegalArgumentException();
		}

		int index = -1;

		int otherIndex = -1;

		boolean differenceFound = false;

		if (fromIndex < dataLength) {

			for (int i = fromIndex; i < dataLength; i++) {
				if (data[i] == pattern[0]) {
					differenceFound = false;
					for (int j = 1; j < patternLength; j++) {

						otherIndex = i + j;

						if (otherIndex >= dataLength || data[otherIndex] != pattern[j]) {
							differenceFound = true;
							break;
						}
					}
					if (!differenceFound) {
						index = i;
						break;
					}
				}
			}

			return index;
		} else {

			throw new IllegalArgumentException();
		}
	}

	public static int lastIndexOf(byte[] data, byte[] pattern) {

		if (T4JCollectionUtils.isNullOrEmpty(data)) {

			throw new IllegalArgumentException();
		}

		if (T4JCollectionUtils.isNullOrEmpty(pattern)) {

			throw new IllegalArgumentException();
		}

		int dataLength = data.length;

		int patternLength = pattern.length;

		if (patternLength > dataLength) {

			throw new IllegalArgumentException();
		}

		int index = -1;

		int otherIndex = -1;

		boolean differenceFound = false;

		for (int i = 0; i < dataLength; i++) {
			if (data[i] == pattern[0]) {
				differenceFound = false;
				for (int j = 1; j < patternLength; j++) {

					otherIndex = i + j;

					if (otherIndex >= dataLength || data[otherIndex] != pattern[j]) {
						differenceFound = true;
						break;
					}
				}
				if (!differenceFound) {
					index = i;
				}
			}
		}

		return index;
	}

	public static byte[] objectToByteArray(Serializable serializable) {

		if (null == serializable) {

			return null;
		} else {

			ByteArrayOutputStream byteArrayOutputStream = null;
			ObjectOutputStream objectOutputStream = null;
			byte[] byteArray = null;

			try {

				byteArrayOutputStream = new ByteArrayOutputStream();
				objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
				objectOutputStream.writeObject(serializable);

				objectOutputStream.flush();
				objectOutputStream.close();

				byteArrayOutputStream.flush();
				byteArrayOutputStream.close();

				byteArray = byteArrayOutputStream.toByteArray();
			} catch (Exception e) {

				// Ignore
			} finally {

				try {
					objectOutputStream.flush();
				} catch (Exception e) {
					// Ignore exception
				}

				try {
					objectOutputStream.close();
				} catch (Exception e) {
					// Ignore exception
				}

				objectOutputStream = null;

				try {
					byteArrayOutputStream.flush();
				} catch (Exception e) {
					// Ignore exception
				}

				try {
					byteArrayOutputStream.close();
				} catch (Exception e) {
					// Ignore exception
				}

				byteArrayOutputStream = null;
			}

			return byteArray;
		}
	}

	public static byte[] replace(byte[] data, byte[] pattern, byte[] replacement) throws Exception {

		if (T4JCollectionUtils.isNullOrEmpty(data)) {

			throw new IllegalArgumentException();
		}

		if (T4JCollectionUtils.isNullOrEmpty(pattern)) {

			throw new IllegalArgumentException();
		}

		if (T4JCollectionUtils.isNullOrEmpty(replacement)) {

			throw new IllegalArgumentException();
		}

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(65536);

		int j = 0;

		int dataLength = data.length;

		int patternLength = pattern.length;

		boolean matchFound = false;

		boolean differenceFound = false;

		for (int i = 0; i < dataLength; i++) {
			if (data[i] == pattern[0]) {
				j = 0;
				differenceFound = false;
				ByteArrayOutputStream tmpByteArrayOutputStream = new ByteArrayOutputStream();
				tmpByteArrayOutputStream.write(new byte[] {
					data[i]
				});
				for (j = 1; j < patternLength; j++) {
					tmpByteArrayOutputStream.write(new byte[] {
						data[i + j]
					});
					if (data[i + j] != pattern[j]) {
						differenceFound = true;
						break;
					}
				}
				tmpByteArrayOutputStream.flush();
				tmpByteArrayOutputStream.close();
				if (!differenceFound && !matchFound) {
					byteArrayOutputStream.write(replacement);
					i = i + patternLength - 1;
					matchFound = true;
				} else {
					byte[] tmpBytes = tmpByteArrayOutputStream.toByteArray();
					int tmpBytesLength = tmpBytes.length;
					byteArrayOutputStream.write(tmpBytes);
					i = i + tmpBytesLength - 1;
				}
			} else {
				byteArrayOutputStream.write(new byte[] {
					data[i]
				});
			}
		}

		byteArrayOutputStream.flush();
		byteArrayOutputStream.close();

		return byteArrayOutputStream.toByteArray();
	}

	public static byte[] replaceAll(byte[] data, byte[] pattern, byte[] replacement) throws Exception {

		if (T4JCollectionUtils.isNullOrEmpty(data)) {

			throw new IllegalArgumentException();
		}

		if (T4JCollectionUtils.isNullOrEmpty(pattern)) {

			throw new IllegalArgumentException();
		}

		if (T4JCollectionUtils.isNullOrEmpty(replacement)) {

			throw new IllegalArgumentException();
		}

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(65536);

		int j = 0;

		int dataLength = data.length;

		int patternLength = pattern.length;

		boolean differenceFound = false;

		for (int i = 0; i < dataLength; i++) {
			if (data[i] == pattern[0]) {
				j = 0;
				differenceFound = false;
				ByteArrayOutputStream tmpByteArrayOutputStream = new ByteArrayOutputStream();
				tmpByteArrayOutputStream.write(new byte[] {
					data[i]
				});
				for (j = 1; j < patternLength; j++) {
					tmpByteArrayOutputStream.write(new byte[] {
						data[i + j]
					});
					if (data[i + j] != pattern[j]) {
						differenceFound = true;
						break;
					}
				}
				tmpByteArrayOutputStream.flush();
				tmpByteArrayOutputStream.close();
				if (!differenceFound) {
					byteArrayOutputStream.write(replacement);
					i = i + patternLength - 1;
				} else {
					byte[] tmpBytes = tmpByteArrayOutputStream.toByteArray();
					int tmpBytesLength = tmpBytes.length;
					byteArrayOutputStream.write(tmpBytes);
					i = i + tmpBytesLength - 1;
				}
			} else {
				byteArrayOutputStream.write(new byte[] {
					data[i]
				});
			}
		}

		byteArrayOutputStream.flush();
		byteArrayOutputStream.close();

		return byteArrayOutputStream.toByteArray();
	}

}
