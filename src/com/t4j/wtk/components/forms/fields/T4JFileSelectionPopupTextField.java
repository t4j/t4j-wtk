
package com.t4j.wtk.components.forms.fields;

import java.io.File;
import java.io.Serializable;

import com.t4j.wtk.T4JConstants;
import com.t4j.wtk.application.T4JWebApp;
import com.t4j.wtk.beans.files.T4JFileInfo;
import com.t4j.wtk.components.downloads.T4JZipStreamSource;
import com.t4j.wtk.components.forms.T4JForm;
import com.t4j.wtk.components.uploads.T4JFileUploadHandler;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 *
 * @author tEnEntia-4j, S.L.
 *
 */
public abstract class T4JFileSelectionPopupTextField extends TextField implements FocusListener {

	private static final long serialVersionUID = 1L;

	protected byte[] uploadedBytes;

	protected File uploadedFile;

	protected Button deleteButton;

	protected Button downloadButton;

	protected Button uploadButton;

	protected T4JZipStreamSource streamSource;

	protected T4JFileUploadHandler uploadHandler;

	protected T4JFileInfo fileInfo;

	private Serializable bean;

	public T4JFileSelectionPopupTextField() {

		super();

		setupField();
	}

	public T4JFileSelectionPopupTextField(Property dataSource) {

		super(dataSource);

		setupField();
	}

	public T4JFileSelectionPopupTextField(String caption) {

		super(caption);

		setupField();
	}

	public T4JFileSelectionPopupTextField(String caption, Property dataSource) {

		super(caption, dataSource);

		setupField();
	}

	public T4JFileSelectionPopupTextField(String caption, String value) {

		super(caption, value);

		setupField();
	}

	protected abstract void deleteFile();

	protected Button generateDeleteButton() {

		Button button = new Button(T4JWebApp.getInstance().getI18nString("button.deleteFile"), new ClickListener() {

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {

				deleteFile();
			}
		});

		button.addStyleName(T4JConstants.LINK_BUTTON_STYLE_CLASS);
		button.addStyleName("smallFont");

		return button;
	}

	protected Button generateDownloadButton() {

		Button button = new Button(T4JWebApp.getInstance().getI18nString("button.downloadFile"), new ClickListener() {

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {

				streamSource = getDownloadSource();

				T4JWebApp.getInstance().showFileDownloadDialog(streamSource);
			}
		});

		return button;
	}

	protected Component generatePopupComponent() {

		T4JFileInfo tmpFileInfo = getFileInfo();

		if (null == tmpFileInfo) {

			tmpFileInfo = new T4JFileInfo();
		}

		T4JForm form = new T4JForm();

		BeanItem<T4JFileInfo> beanItem = new BeanItem<T4JFileInfo>(tmpFileInfo);

		form.setItemDataSource(beanItem);

		Object[] visibleProperties = {
			"fileName", //
			"fileSize"
		};

		form.setVisibleItemProperties(visibleProperties);

		return form;
	}

	protected Button generateUploadButton() {

		Button button = new Button(T4JWebApp.getInstance().getI18nString("button.uploadFile"), new ClickListener() {

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {

				uploadHandler = getUploadHandler();

				T4JWebApp.getInstance().showFileUploadDialog(uploadHandler);
			}
		});

		return button;
	}

	protected String getDialogTitle() {

		return T4JWebApp.getInstance().getI18nString("T4JFilePopupSelectionTextField.dialogTitle", "Detalle de fichero");
	}

	protected abstract T4JZipStreamSource getDownloadSource();

	@Override
	@Deprecated
	protected String getFormattedValue() {

		return getTextFieldVisibleValue(getValue());
	}

	protected abstract String getTextFieldVisibleValue(Object value);

	protected abstract Class<?> getTypeClass();

	protected abstract T4JFileUploadHandler getUploadHandler();

	protected boolean isCloseDialogButtonVisible() {

		return true;
	}

	protected boolean isDeleteButtonEnabled() {

		return true;
	}

	protected boolean isDeleteButtonVisible() {

		return true;
	}

	protected boolean isDownloadButtonEnabled() {

		return true;
	}

	protected boolean isDownloadButtonVisible() {

		return true;
	}

	protected boolean isUploadButtonEnabled() {

		return true;
	}

	protected boolean isUploadButtonVisible() {

		return true;
	}

	protected void setupField() {

		addListener((FocusListener) this);
		addStyleName("t4j-popup-textfield");
		setImmediate(true);
		setInputPrompt(T4JWebApp.getInstance().getI18nString("T4JFilePopupSelectionTextField.inputPrompt"));
		setNullRepresentation(T4JConstants.EMPTY_STRING);
	}

	public void focus(FocusEvent event) {

		if (isReadOnly()) {

			return;
		}

		VerticalLayout modalWindowLayout = new VerticalLayout();
		modalWindowLayout.setSpacing(true);

		modalWindowLayout.addComponent(generatePopupComponent());

		HorizontalLayout buttonsWrapperLayout = new HorizontalLayout();
		buttonsWrapperLayout.setSizeUndefined();
		buttonsWrapperLayout.setSpacing(true);

		deleteButton = generateDeleteButton();
		buttonsWrapperLayout.addComponent(deleteButton);
		buttonsWrapperLayout.setComponentAlignment(deleteButton, Alignment.MIDDLE_RIGHT);
		deleteButton.setVisible(isDeleteButtonVisible());
		deleteButton.setEnabled(isDeleteButtonEnabled());

		uploadButton = generateUploadButton();
		buttonsWrapperLayout.addComponent(uploadButton);
		buttonsWrapperLayout.setComponentAlignment(uploadButton, Alignment.MIDDLE_RIGHT);

		downloadButton = generateDownloadButton();
		buttonsWrapperLayout.addComponent(downloadButton);
		buttonsWrapperLayout.setComponentAlignment(downloadButton, Alignment.MIDDLE_RIGHT);

		modalWindowLayout.addComponent(buttonsWrapperLayout);
		modalWindowLayout.setComponentAlignment(buttonsWrapperLayout, Alignment.MIDDLE_RIGHT);

		T4JWebApp.getInstance().showModalDialog(getDialogTitle(), modalWindowLayout, Boolean.valueOf(isCloseDialogButtonVisible()), Boolean.FALSE, Boolean.TRUE, null);
	}

	public Object getBean() {

		return bean;
	}

	public Button getDeleteButton() {

		return deleteButton;
	}

	public Button getDownloadButton() {

		return downloadButton;
	}

	public T4JFileInfo getFileInfo() {

		return fileInfo;
	}

	public T4JZipStreamSource getStreamSource() {

		return streamSource;
	}

	@Override
	public Class<?> getType() {

		return getTypeClass();
	}

	public Button getUploadButton() {

		return uploadButton;
	}

	public byte[] getUploadedBytes() {

		return uploadedBytes;
	}

	public File getUploadedFile() {

		return uploadedFile;
	}

	public void setBean(Serializable bean) {

		this.bean = bean;
	}

	public void setFileInfo(T4JFileInfo fileInfo) {

		this.fileInfo = fileInfo;
	}

	public void setStreamSource(T4JZipStreamSource streamSource) {

		this.streamSource = streamSource;
	}

	public void setUploadHandler(T4JFileUploadHandler uploadHandler) {

		this.uploadHandler = uploadHandler;
	}

}
