
package com.t4j.wtk.components.uploads;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.vaadin.ui.Upload.FailedListener;
import com.vaadin.ui.Upload.FinishedListener;
import com.vaadin.ui.Upload.ProgressListener;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.StartedListener;
import com.vaadin.ui.Upload.SucceededListener;

/**
 * Clase abstracta que gestiona las cargas de fichero al servidor.
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public abstract class T4JFileUploadHandler implements Receiver, StartedListener, ProgressListener, SucceededListener, FailedListener, FinishedListener {

	private static final long serialVersionUID = 1L;

	public static final Integer MIN_BLOCK_SIZE = Integer.valueOf(1024 * 16);

	private int bytesCounter;

	protected Boolean writeToFile;

	protected Boolean isLazyUpload;

	protected Integer blockSize;

	protected String fileName;

	protected String mimeType;

	protected File destinationFile;

	protected transient ByteArrayOutputStream byteArrayOutputStream;

	protected transient FileOutputStream fileOutputStream;

	public T4JFileUploadHandler() {

		this(null);
	}

	public T4JFileUploadHandler(File destinationFile) {

		super();

		this.destinationFile = destinationFile;

		writeToFile = Boolean.valueOf(checkIsValidFile());
	}

	protected boolean checkIsValidFile() {

		boolean methodResult = false;

		boolean deleteFile = false;

		try {

			if (destinationFile.exists()) {

				if (destinationFile.isFile() && destinationFile.canWrite()) {

					methodResult = true;
				}
			} else {

				try {

					// Intentamos crear un nuevo fichero

					if (destinationFile.createNewFile()) {

						if (destinationFile.isFile() && destinationFile.canWrite()) {

							methodResult = true;
						} else {

							deleteFile = true;
						}
					}
				} catch (Exception e) {
					// Ignore
				}

				if (deleteFile) {

					try {

						destinationFile.delete();
					} catch (Exception e) {
						// Ignore
					}
				}
			}
		} catch (Exception e) {
			// Ignore
		}

		return methodResult;

	}

	protected void closeOutputStreams() {

		if (null != byteArrayOutputStream) {

			try {

				byteArrayOutputStream.flush();
				byteArrayOutputStream.close();
			} catch (Exception e) {
				// Ignore exception
			}

			byteArrayOutputStream = null;
		}

		if (null != fileOutputStream) {

			try {

				fileOutputStream.flush();
				fileOutputStream.close();
			} catch (Exception e) {
				// Ignore exception
			}

			fileOutputStream = null;
		}
	}

	@Override
	protected void finalize() throws Throwable {

		closeOutputStreams();

		super.finalize();
	}

	public Integer getBlockSize() {

		return blockSize;
	}

	public String getFileName() {

		return fileName;
	}

	public Boolean getIsLazyUpload() {

		return isLazyUpload;
	}

	public String getMimeType() {

		return mimeType;
	}

	public byte[] getUploadedBytes() {

		if (null == byteArrayOutputStream) {

			throw new UnsupportedOperationException();
		}

		return byteArrayOutputStream.toByteArray();
	}

	public InputStream getUploadedFileInputStream() throws IOException {

		if (Boolean.TRUE.equals(writeToFile)) {

			return new FileInputStream(destinationFile);
		} else {

			return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
		}
	}

	public OutputStream receiveUpload(String filename, String mimeType) {

		closeOutputStreams();

		OutputStream outputStream = null;

		fileName = filename;
		this.mimeType = mimeType;

		if (Boolean.TRUE.equals(writeToFile)) {

			try {
				fileOutputStream = new FileOutputStream(destinationFile);
				outputStream = fileOutputStream;
			} catch (Exception e) {
				byteArrayOutputStream = new ByteArrayOutputStream(65536);
				outputStream = byteArrayOutputStream;
			}
		} else {

			byteArrayOutputStream = new ByteArrayOutputStream(65536);
			outputStream = byteArrayOutputStream;
		}

		if (Boolean.TRUE.equals(isLazyUpload)) {

			if (null == blockSize || MIN_BLOCK_SIZE.compareTo(blockSize) > 0) {

				blockSize = MIN_BLOCK_SIZE;
			}

			outputStream = new OutputStream() {

				@Override
				public void write(int b) throws IOException {

					if (Boolean.TRUE.equals(writeToFile)) {
						if (null == fileOutputStream) {
							byteArrayOutputStream.write(b);
						} else {
							fileOutputStream.write(b);
						}
					} else {
						byteArrayOutputStream.write(b);
					}

					int tmpInt = blockSize.intValue();

					if (bytesCounter % tmpInt == 0) {

						// Cada 16KB detenemos el hilo 100 ms para simular un servidor lento.
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// Ignore
						}
					}
				}
			};
		}

		return outputStream;
	}

	public void setBlockSize(Integer blockSize) {

		this.blockSize = blockSize;
	}

	public void setIsLazyUpload(Boolean isLazyUpload) {

		this.isLazyUpload = isLazyUpload;
	}

}
