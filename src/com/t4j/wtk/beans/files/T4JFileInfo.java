
package com.t4j.wtk.beans.files;

import java.io.Serializable;
import java.util.Date;

import com.t4j.wtk.beans.numbers.T4JLongWithUnit;
import com.t4j.wtk.components.forms.T4JFieldCaption;
import com.t4j.wtk.components.forms.T4JFieldDescriptor;
import com.t4j.wtk.components.forms.T4JFieldTypeEnum;

public class T4JFileInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	@T4JFieldCaption(
		def = "Nombre",
		en = "File name")
	@T4JFieldDescriptor(
		fieldType = T4JFieldTypeEnum.TEXTFIELD,
		cssClassNames = "large",
		readonly = true)
	private String fileName;

	@T4JFieldCaption(
		def = "Tipo",
		en = "Content type")
	@T4JFieldDescriptor(
		fieldType = T4JFieldTypeEnum.TEXTFIELD,
		cssClassNames = "large",
		readonly = true)
	private String fileType;

	@T4JFieldCaption(
		def = "Tamaño",
		en = "File length")
	@T4JFieldDescriptor(
		fieldType = T4JFieldTypeEnum.TEXTFIELD,
		readonly = true)
	private T4JLongWithUnit fileSize;

	@T4JFieldCaption(
		def = "Última modificación",
		en = "Last updated")
	@T4JFieldDescriptor(
		fieldType = T4JFieldTypeEnum.DATETIMEFIELD,
		readonly = true)
	private Date lastUpdated;

	public T4JFileInfo() {

		super();
	}

	public String getFileName() {

		return fileName;
	}

	public T4JLongWithUnit getFileSize() {

		return fileSize;
	}

	public String getFileType() {

		return fileType;
	}

	public Date getLastUpdated() {

		return lastUpdated;
	}

	public void setFileName(String fileName) {

		this.fileName = fileName;
	}

	public void setFileSize(T4JLongWithUnit fileSize) {

		this.fileSize = fileSize;
	}

	public void setFileType(String fileType) {

		this.fileType = fileType;
	}

	public void setLastUpdated(Date lastUpdated) {

		this.lastUpdated = lastUpdated;
	}

}
