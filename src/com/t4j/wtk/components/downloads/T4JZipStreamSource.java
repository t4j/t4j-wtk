
package com.t4j.wtk.components.downloads;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;

import com.t4j.wtk.util.T4JByteArrayUtils;
import com.t4j.wtk.util.T4JLogUtils;
import com.t4j.wtk.util.T4JStringUtils;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public class T4JZipStreamSource implements StreamSource {

	private static final long serialVersionUID = 1L;

	private static final Log logger = T4JLogUtils.getLogger(T4JZipStreamSource.class);

	private boolean deleteSourceFile;

	private boolean isReaded;

	private String fileName;

	private String tmpZipFilePath;

	private String zipEntryName;

	private File sourceFile;

	private File tmpZipFile;

	private transient InputStream inputStream;

	private transient InputStream internalInputStream;

	private CloseListener closeListener;

	public T4JZipStreamSource(File sourceFile) {

		this(sourceFile, false);
	}

	public T4JZipStreamSource(File sourceFile, boolean deleteSourceFile) {

		this(sourceFile, null, deleteSourceFile);
	}

	public T4JZipStreamSource(File sourceFile, String zipEntryName, boolean deleteSourceFile) {

		super();

		this.sourceFile = sourceFile;
		this.zipEntryName = zipEntryName;
		this.deleteSourceFile = deleteSourceFile;
		isReaded = false;
	}

	public T4JZipStreamSource(String fileName, InputStream inputStream) {

		super();

		this.fileName = fileName;
		this.inputStream = inputStream;
	}

	public T4JZipStreamSource(String fileName, String zipEntryName, InputStream inputStream) {

		super();

		this.fileName = fileName;
		this.zipEntryName = zipEntryName;
		this.inputStream = inputStream;
	}

	@Override
	protected void finalize() throws Throwable {

		isReaded = true;

		cleanUp();

		super.finalize();
	}

	public void cleanUp() {

		logger.trace("START");

		if (null != internalInputStream) {

			try {

				internalInputStream.close();
			} catch (Exception e) {

				// Ignore exception
			}

			internalInputStream = null;
		}

		if (null != tmpZipFile) {

			try {
				logger.trace("Removing temporal zip file: " + tmpZipFile.getCanonicalPath() + (tmpZipFile.delete() ? " - DONE" : " - ERROR"));
			} catch (Exception e) {
				logger.trace(e, e);
			}

			tmpZipFile = null;
		}

		if (null != sourceFile && deleteSourceFile && isReaded) {

			try {
				logger.trace("Removing source file: " + sourceFile.getCanonicalPath() + (sourceFile.delete() ? " - DONE" : " - ERROR"));
			} catch (Exception e) {
				logger.trace(e, e);
			}
			sourceFile = null;
		}
	}

	public void forceCleanUp() {

		logger.trace("START");

		try {
			internalInputStream.close();
		} catch (Exception e) {
			// Ignore exception
		}

		internalInputStream = null;

		try {
			logger.trace("Removing temporal zip file: " + tmpZipFile.getCanonicalPath() + (tmpZipFile.delete() ? " - DONE" : " - ERROR"));
		} catch (Exception e) {
			// Ignore exception
		}

		tmpZipFile = null;

		if (null != sourceFile && deleteSourceFile) {

			try {
				logger.trace("Removing source file: " + sourceFile.getCanonicalPath() + (sourceFile.delete() ? " - DONE" : " - ERROR"));
			} catch (Exception e) {
				// Ignore exception
			}

			sourceFile = null;
		}
	}

	public CloseListener getCloseListener() {

		return closeListener;
	}

	public String getFileName() {

		if (null == sourceFile) {

			return fileName;
		} else {

			return null == zipEntryName ? sourceFile.getName() : zipEntryName;
		}
	}

	public File getSourceFile() {

		return sourceFile;
	}

	public InputStream getStream() {

		cleanUp();

		FileOutputStream fileOutputStream = null;
		ZipOutputStream zipOutputStream = null;

		byte[] zipFileStartBytes = new byte[] {
			0x50, 0x4B, 0x03, 0x04
		};

		if (null == sourceFile) {

			try {

				tmpZipFilePath = System.getProperty("user.home") + "/tmp_" + System.currentTimeMillis() + ".zip";

				tmpZipFilePath = tmpZipFilePath.replaceAll("\\\\", "/");

				tmpZipFile = new File(tmpZipFilePath);

				tmpZipFile.createNewFile();

				logger.trace(tmpZipFile.getCanonicalPath());

				fileOutputStream = new FileOutputStream(tmpZipFile);

				byte[] bytesBuffer = new byte[4096];

				int readedBytes = inputStream.read(bytesBuffer);

				byte[] tmpBytes = new byte[4];

				System.arraycopy(bytesBuffer, 0, tmpBytes, 0, 4);

				boolean isAlreadyZipped = false;

				if (0 == T4JByteArrayUtils.indexOf(tmpBytes, zipFileStartBytes)) {
					isAlreadyZipped = true;
				}

				if (isAlreadyZipped) {

					while (readedBytes != -1) {

						fileOutputStream.write(bytesBuffer, 0, readedBytes);
						readedBytes = inputStream.read(bytesBuffer);
					}

					inputStream.close();

					inputStream = null;

					fileOutputStream.flush();
					fileOutputStream.close();

					fileOutputStream = null;
				} else {

					zipOutputStream = new ZipOutputStream(fileOutputStream);

					ZipEntry zipEntry = new ZipEntry(T4JStringUtils.isNullOrEmpty(zipEntryName) ? fileName : zipEntryName);

					zipOutputStream.putNextEntry(zipEntry);

					while (readedBytes != -1) {

						zipOutputStream.write(bytesBuffer, 0, readedBytes);
						readedBytes = inputStream.read(bytesBuffer);
					}

					inputStream.close();

					inputStream = null;

					zipOutputStream.flush();
					zipOutputStream.closeEntry();
					zipOutputStream.close();
					zipOutputStream = null;

					fileOutputStream.flush();
					fileOutputStream.close();

					fileOutputStream = null;

				}

				internalInputStream = new FileInputStream(tmpZipFile);

			} catch (Exception e) {

				logger.error(e, e);
			} finally {

				if (null != inputStream) {

					try {

						inputStream.close();
					} catch (Exception e2) {

						// Ignore exception
					}

					inputStream = null;
				}

				if (null != zipOutputStream) {

					try {
						zipOutputStream.flush();
						zipOutputStream.close();
					} catch (Exception e2) {

						// Ignore exception
					}

					zipOutputStream = null;
				}

				if (null != fileOutputStream) {

					try {
						fileOutputStream.flush();
						fileOutputStream.close();
					} catch (Exception e2) {

						// Ignore exception
					}

					fileOutputStream = null;
				}
			}
		} else {

			FileInputStream fileInputStream = null;

			try {

				tmpZipFilePath = System.getProperty("user.home") + "/tmp_" + System.currentTimeMillis() + ".zip";

				tmpZipFilePath = tmpZipFilePath.replaceAll("\\\\", "/");

				tmpZipFile = new File(tmpZipFilePath);

				tmpZipFile.createNewFile();

				logger.trace(tmpZipFile.getCanonicalPath());

				fileOutputStream = new FileOutputStream(tmpZipFile);

				fileInputStream = new FileInputStream(sourceFile);

				byte[] bytesBuffer = new byte[4096];

				int readedBytes = fileInputStream.read(bytesBuffer);

				byte[] tmpBytes = new byte[4];

				System.arraycopy(bytesBuffer, 0, tmpBytes, 0, 4);

				boolean isAlreadyZipped = false;

				if (0 == T4JByteArrayUtils.indexOf(tmpBytes, zipFileStartBytes)) {
					isAlreadyZipped = true;
				}

				if (isAlreadyZipped) {

					while (readedBytes != -1) {

						fileOutputStream.write(bytesBuffer, 0, readedBytes);
						readedBytes = fileInputStream.read(bytesBuffer);
					}

					fileInputStream.close();
					fileInputStream = null;

					fileOutputStream.flush();
					fileOutputStream.close();

					fileOutputStream = null;
				} else {

					zipOutputStream = new ZipOutputStream(fileOutputStream);

					String entryName = null == zipEntryName ? sourceFile.getName() : zipEntryName;

					ZipEntry zipEntry = new ZipEntry(entryName);

					zipOutputStream.putNextEntry(zipEntry);

					while (readedBytes != -1) {

						zipOutputStream.write(bytesBuffer, 0, readedBytes);
						readedBytes = fileInputStream.read(bytesBuffer);
					}

					fileInputStream.close();
					fileInputStream = null;

					zipOutputStream.flush();
					zipOutputStream.closeEntry();
					zipOutputStream.close();
					zipOutputStream = null;

					fileOutputStream.flush();
					fileOutputStream.close();

					fileOutputStream = null;
				}

				internalInputStream = new FileInputStream(tmpZipFile);

				if (deleteSourceFile) {

					try {
						logger.trace("Removing source file: " + sourceFile.getCanonicalPath() + (sourceFile.delete() ? " - DONE" : " - ERROR"));
					} catch (Exception e) {
						logger.trace(e, e);
					}
					sourceFile = null;
				}

				isReaded = true;

			} catch (Exception e) {

				logger.error(e, e);
			} finally {

				if (null != fileInputStream) {

					try {

						fileInputStream.close();
					} catch (Exception e2) {

						// Ignore exception
					}

					fileInputStream = null;
				}

				if (null != zipOutputStream) {

					try {
						zipOutputStream.flush();
						zipOutputStream.close();
					} catch (Exception e2) {

						// Ignore exception
					}

					zipOutputStream = null;
				}

				if (null != fileOutputStream) {

					try {
						fileOutputStream.flush();
						fileOutputStream.close();
					} catch (Exception e2) {

						// Ignore exception
					}

					fileOutputStream = null;
				}
			}
		}

		return internalInputStream;
	}

	public String getTmpZipFilePath() {

		return tmpZipFilePath;
	}

	public boolean isReaded() {

		return isReaded;
	}

	public void refreshComponents(CloseEvent closeEvent) {

		try {
			closeListener.windowClose(closeEvent);
		} catch (Exception e) {
			// Ignore
		}
	}

	public void setCloseListener(CloseListener closeListener) {

		this.closeListener = closeListener;
	}
}
