
package com.t4j.wtk.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class T4JZipUtils implements Serializable {

	private static final long serialVersionUID = 1L;

	public static byte[] compress(byte[] uncompressedBytes) {

		if (null == uncompressedBytes) {

			return null;

		} else if (0 == uncompressedBytes.length) {

			return uncompressedBytes;
		} else {

			try {

				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
				gzipOutputStream.write(uncompressedBytes);
				gzipOutputStream.finish();
				gzipOutputStream.flush();
				gzipOutputStream.close();
				byteArrayOutputStream.flush();
				byteArrayOutputStream.close();

				byte[] compressedBytes = byteArrayOutputStream.toByteArray();

				return compressedBytes;
			} catch (Exception e) {

				return uncompressedBytes;
			}
		}
	}

	public static byte[] expand(byte[] compressedBytes) {

		if (null == compressedBytes) {

			return null;

		} else if (0 == compressedBytes.length) {

			return compressedBytes;
		} else {

			try {

				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedBytes);
				GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(65536);

				byte[] bytesBuffer = new byte[65536];
				int readedBytes = -1;

				while ((readedBytes = gzipInputStream.read(bytesBuffer)) != -1) {

					byteArrayOutputStream.write(bytesBuffer, 0, readedBytes);
				}

				gzipInputStream.close();

				byteArrayInputStream.close();

				byteArrayOutputStream.flush();
				byteArrayOutputStream.close();

				byte[] uncompressedBytes = byteArrayOutputStream.toByteArray();

				return uncompressedBytes;
			} catch (Exception e) {

				// Not compressed

				return compressedBytes;
			}
		}
	}

}
