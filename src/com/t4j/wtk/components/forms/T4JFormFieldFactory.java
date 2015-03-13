
package com.t4j.wtk.components.forms;

import java.io.Serializable;
import java.util.Collection;
import java.util.Locale;

import javax.persistence.Column;

import org.apache.commons.logging.Log;

import com.t4j.wtk.T4JConstants;
import com.t4j.wtk.application.T4JWebApp;
import com.t4j.wtk.components.forms.fields.T4JAbstractSelectionPopupTextField;
import com.t4j.wtk.components.forms.fields.T4JDateField;
import com.t4j.wtk.components.forms.fields.T4JDateTimeField;
import com.t4j.wtk.components.forms.fields.T4JMultipleDateField;
import com.t4j.wtk.components.forms.fields.T4JTextArea;
import com.t4j.wtk.components.forms.fields.T4JTextField;
import com.t4j.wtk.components.forms.fields.generators.T4JSelectItemsGenerator;
import com.t4j.wtk.components.forms.fields.listeners.T4JFormEventListener;
import com.t4j.wtk.components.forms.fields.validators.T4JAbstractFormValidator;
import com.t4j.wtk.components.forms.fields.validators.T4JFormFieldValidator;
import com.t4j.wtk.util.T4JCollectionUtils;
import com.t4j.wtk.util.T4JLogUtils;
import com.t4j.wtk.util.T4JStringUtils;
import com.vaadin.data.Item;
import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.PropertyFormatter;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Listener;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.TextArea;

/**
 * Clase que genera los campos de un formulario a partir de las anotaciones en los atributos de los POJO.
 *
 * @author Tenentia-4j, S.L.
 *
 */
public class T4JFormFieldFactory extends DefaultFieldFactory {

	private static final long serialVersionUID = 1L;

	private static final Log logger = T4JLogUtils.getLogger(T4JFormFieldFactory.class);

	public T4JFormFieldFactory() {

		super();
	}

	protected String getDefaultGeneratorClassNameWithPackage(String generatorClassName) {

		return T4JConstants.DEFAULT_VALIDATOR_PACKAGE + "." + generatorClassName;
	}

	protected String getDefaultValidatorClassNameWithPackage(String validatorClassName) {

		return T4JConstants.DEFAULT_VALIDATOR_PACKAGE + "." + validatorClassName;
	}

	protected int getFieldMaxLength(Object bean, T4JFieldDescriptor fieldDescriptorAnnotation, Object propertyId) {

		int maxLength = -1;

		try {

			try {
				maxLength = fieldDescriptorAnnotation.maxLength();
			} catch (Exception e) {
				// Ignore
			}

			if (1 > maxLength) {

				// Intentamos recuperar la información de la anotación JPA,

				Column column = null;

				try {

					column = bean.getClass().getDeclaredField(propertyId.toString()).getAnnotation(Column.class);
				} catch (Exception e) {
					// Ignore
				}

				if (null == column) {

					maxLength = -1;
				} else {

					if (0 < column.precision()) {

						// 4 = Signo, separador decimal, exponencial y signo exponencial.

						maxLength = 4 + column.precision();
					} else {

						if (255 == column.length()) {

							maxLength = -1;
						} else {

							maxLength = column.length();
						}
					}
				}
			}
		} catch (Exception e) {

			maxLength = -1;
		}

		return maxLength;
	}

	protected void setFieldCaption(Object bean, Field field, Object propertyId, String defaultFieldCaption) {

		try {

			T4JFieldCaption fieldAnnotation = bean.getClass().getDeclaredField(propertyId.toString()).getAnnotation(T4JFieldCaption.class);

			if (null == fieldAnnotation) {

				field.setCaption(defaultFieldCaption);
			} else {

				String fieldCaptionString = null;

				Locale locale = T4JWebApp.getInstance().getLocale();

				if (null == locale) {

					locale = new Locale("def", T4JConstants.EMPTY_STRING, T4JConstants.EMPTY_STRING);
				}

				String bundleKey = fieldAnnotation.bundleKey();

				if (false == T4JStringUtils.isNullOrEmpty(bundleKey)) {

					fieldCaptionString = T4JWebApp.getInstance().getI18nString(bundleKey);
				}

				if (T4JStringUtils.isNullOrEmpty(fieldCaptionString)) {

					if ("cs".equals(locale.getLanguage())) {

						// Checo
						fieldCaptionString = fieldAnnotation.cs();
					} else if ("de".equals(locale.getLanguage())) {

						// Alemán
						fieldCaptionString = fieldAnnotation.de();
					} else if ("en".equals(locale.getLanguage())) {

						// Inglés
						fieldCaptionString = fieldAnnotation.en();
					} else if ("es".equals(locale.getLanguage())) {

						// Castellano
						fieldCaptionString = fieldAnnotation.es();
					} else if ("fr".equals(locale.getLanguage())) {

						// Francés
						fieldCaptionString = fieldAnnotation.fr();
					} else if ("it".equals(locale.getLanguage())) {

						// Italiano
						fieldCaptionString = fieldAnnotation.it();
					} else if ("pt".equals(locale.getLanguage())) {

						// Portugués
						fieldCaptionString = fieldAnnotation.pt();
					}

					if (T4JStringUtils.isNullOrEmpty(fieldCaptionString)) {

						// Valor por defecto
						fieldCaptionString = fieldAnnotation.def();
					}
				}

				if (null == fieldCaptionString) {

					fieldCaptionString = defaultFieldCaption;
				}

				field.setCaption(fieldCaptionString);
			}

		} catch (Exception e) {

			field.setDescription(defaultFieldCaption);
		}
	}

	protected void setFieldTooltip(Object bean, Field field, Object propertyId) {

		try {

			T4JFieldTooltip fieldAnnotation = bean.getClass().getDeclaredField(propertyId.toString()).getAnnotation(T4JFieldTooltip.class);

			if (null == fieldAnnotation) {

				field.setDescription(T4JConstants.EMPTY_STRING);
			} else {

				String fieldTooltipString = null;

				Locale locale = T4JWebApp.getInstance().getLocale();

				if (null == locale) {

					locale = new Locale("def", T4JConstants.EMPTY_STRING, T4JConstants.EMPTY_STRING);
				}

				String bundleKey = fieldAnnotation.bundleKey();

				if (false == T4JStringUtils.isNullOrEmpty(bundleKey)) {

					fieldTooltipString = T4JWebApp.getInstance().getI18nString(bundleKey);
				}

				if (T4JStringUtils.isNullOrEmpty(fieldTooltipString)) {

					if ("cs".equals(locale.getLanguage())) {

						// Checo
						fieldTooltipString = fieldAnnotation.cs();
					} else if ("de".equals(locale.getLanguage())) {

						// Alemán
						fieldTooltipString = fieldAnnotation.de();
					} else if ("en".equals(locale.getLanguage())) {

						// Inglés
						fieldTooltipString = fieldAnnotation.en();
					} else if ("es".equals(locale.getLanguage())) {

						// Castellano
						fieldTooltipString = fieldAnnotation.es();
					} else if ("fr".equals(locale.getLanguage())) {

						// Francés
						fieldTooltipString = fieldAnnotation.fr();
					} else if ("it".equals(locale.getLanguage())) {

						// Italiano
						fieldTooltipString = fieldAnnotation.it();
					} else if ("pt".equals(locale.getLanguage())) {

						// Portugués
						fieldTooltipString = fieldAnnotation.pt();
					}

					if (T4JStringUtils.isNullOrEmpty(fieldTooltipString)) {

						// Valor por defecto
						fieldTooltipString = fieldAnnotation.def();
					}
				}

				if (null == fieldTooltipString) {

					fieldTooltipString = T4JConstants.EMPTY_STRING;
				}

				field.setDescription(fieldTooltipString);
			}

		} catch (Exception e) {

			field.setDescription(T4JConstants.EMPTY_STRING);
		}
	}

	@Override
	public Field createField(Item item, Object propertyId, Component uiContext) {

		if (null == item) {

			logger.warn("item is null");
			return null;
		}

		if (null == propertyId) {

			logger.warn("propertyId is null");
			return null;
		}

		boolean useDefaultField = false;

		Field defaultField = null;

		try {
			defaultField = super.createField(item, propertyId, uiContext);
		} catch (Exception e) {
			// Ignore
		}

		if (null == defaultField) {

			logger.warn("defaultField is null");
			return defaultField;
		}

		if (null == uiContext) {

			logger.warn("uiContext is null");
			return defaultField;
		}

		if (item instanceof BeanItem<?>) {

			String defaultCaption = T4JConstants.EMPTY_STRING;

			try {
				defaultCaption = defaultField.getCaption();
			} catch (Exception e) {
				// Ignore
			}

			Field field = null;

			try {

				Form form = (Form) uiContext;

				BeanItem<?> beanItem = (BeanItem<?>) item;

				Serializable bean = null;

				try {
					bean = (Serializable) beanItem.getBean();
				} catch (Exception e) {
					logger.warn("bean does not implement Serializable...");
					return null;
				}

				Collection<?> visiblePropertyIds = null;

				if (form instanceof T4JForm) {

					T4JForm t4jForm = (T4JForm) form;

					if (Boolean.TRUE.equals(t4jForm.getIsVisibleItemPropertiesValueSet())) {

						visiblePropertyIds = form.getVisibleItemProperties();
					} else {

						logger.warn("visibleItemProperties are not already set");
						return null;
					}
				} else {

					visiblePropertyIds = form.getVisibleItemProperties();
				}

				if (T4JCollectionUtils.isNullOrEmpty(visiblePropertyIds) || false == visiblePropertyIds.contains(propertyId)) {

					throw new Exception(propertyId + " is not visible");
				}

				T4JFieldDescriptor descriptor = null;

				try {

					descriptor = bean.getClass().getDeclaredField(propertyId.toString()).getAnnotation(T4JFieldDescriptor.class);
				} catch (Exception e) {

					useDefaultField = true;

					throw new Exception("field " + propertyId + " not found in class " + bean.getClass().getName());
				}

				if (null == descriptor) {

					field = defaultField;
				} else {

					int visibleRows = descriptor.visibleRows();

					if (1 > visibleRows) {

						visibleRows = 1;
					}

					switch (descriptor.fieldType()) {

						case TEXTFIELD:

							T4JTextField textField = new T4JTextField();
							textField.setImmediate(descriptor.immediate());
							textField.setMaxLength(getFieldMaxLength(bean, descriptor, propertyId));
							textField.setNullRepresentation(T4JConstants.EMPTY_STRING);
							field = textField;
							break;

						case FORMATTED_TEXTFIELD:

							T4JTextField formattedTextField = new T4JTextField();
							formattedTextField.setImmediate(descriptor.immediate());
							formattedTextField.setMaxLength(getFieldMaxLength(bean, descriptor, propertyId));
							formattedTextField.setNullRepresentation(T4JConstants.EMPTY_STRING);
							field = formattedTextField;
							break;

						case TEXTAREA:

							TextArea textArea = new TextArea();
							textArea.setImmediate(descriptor.immediate());
							textArea.setMaxLength(getFieldMaxLength(bean, descriptor, propertyId));
							textArea.setNullRepresentation(T4JConstants.EMPTY_STRING);

							textArea.setRows(visibleRows);
							field = textArea;
							break;

						case RICHTEXTAREA:

							RichTextArea richTextArea = new RichTextArea();
							richTextArea.setImmediate(descriptor.immediate());
							richTextArea.setNullRepresentation(T4JConstants.EMPTY_STRING);
							field = richTextArea;
							break;

						case PASSWORDFIELD:

							PasswordField passwordField = new PasswordField();
							passwordField.setImmediate(descriptor.immediate());
							passwordField.setMaxLength(getFieldMaxLength(bean, descriptor, propertyId));
							passwordField.setNullRepresentation(T4JConstants.EMPTY_STRING);
							field = passwordField;
							break;

						case DATEFIELD:

							T4JDateField dateField = new T4JDateField();
							dateField.setNullRepresentation(T4JConstants.EMPTY_STRING);
							field = dateField;
							break;

						case MULTIPLE_DATEFIELD:

							T4JMultipleDateField multipleDateField = new T4JMultipleDateField();
							multipleDateField.setNullRepresentation(T4JConstants.EMPTY_STRING);
							field = multipleDateField;
							break;

						case DATETIMEFIELD:

							T4JDateTimeField dateTimeField = new T4JDateTimeField();
							dateTimeField.setNullRepresentation(T4JConstants.EMPTY_STRING);
							field = dateTimeField;
							break;

						case CHECKBOX:

							CheckBox checkBox = new CheckBox();
							checkBox.setImmediate(descriptor.immediate());
							field = checkBox;
							break;

						case SINGLE_SELECTION:

							NativeSelect singleSelect = new NativeSelect();

							singleSelect.setImmediate(descriptor.immediate());
							singleSelect.setMultiSelect(false);
							singleSelect.setNullSelectionAllowed(descriptor.nullSelectionAllowed());

							if (false == T4JConstants.EMPTY_STRING.equals(descriptor.itemsGeneratorClassName())) {

								String className = descriptor.itemsGeneratorClassName();

								try {

									T4JSelectItemsGenerator itemsGenerator = (T4JSelectItemsGenerator) Class.forName(className).newInstance();
									itemsGenerator.generateItems(singleSelect, bean);
								} catch (Exception e) {
									// Ignore
								}

							}

							field = singleSelect;
							break;

						case MULTIPLE_SELECTION:

							ListSelect multipleSelect = new ListSelect();

							multipleSelect.setImmediate(descriptor.immediate());
							multipleSelect.setNullSelectionAllowed(descriptor.nullSelectionAllowed());
							multipleSelect.setMultiSelect(true);

							multipleSelect.setRows(visibleRows);

							if (false == T4JConstants.EMPTY_STRING.equals(descriptor.itemsGeneratorClassName())) {

								String className = descriptor.itemsGeneratorClassName();
								try {

									T4JSelectItemsGenerator itemsGenerator = (T4JSelectItemsGenerator) Class.forName(className).newInstance();
									itemsGenerator.generateItems(multipleSelect, bean);
								} catch (Exception e) {
									// Ignore
								}
							}

							field = multipleSelect;
							break;

						case POPUP_TEXTFIELD:

							T4JAbstractSelectionPopupTextField popupSelectionTextField = null;

							try {

								popupSelectionTextField = (T4JAbstractSelectionPopupTextField) Class.forName(descriptor.popupGeneratorClassName()).newInstance();
								popupSelectionTextField.setBean(bean);
								popupSelectionTextField.setImmediate(descriptor.immediate());

							} catch (Exception e) {
								// Ignore
							}

							field = popupSelectionTextField;
							break;

						case POPUP_TEXTAREA:

							T4JTextArea popupTextArea = null;

							try {

								popupTextArea = (T4JTextArea) Class.forName(descriptor.popupGeneratorClassName()).newInstance();
								popupTextArea.setBean(bean);
								popupTextArea.setImmediate(descriptor.immediate());

							} catch (Exception e) {
								// Ignore
							}

							field = popupTextArea;
							break;

						case COMBOBOX:

							ComboBox comboBox = new ComboBox();

							comboBox.setImmediate(descriptor.immediate());
							comboBox.setNullSelectionAllowed(descriptor.nullSelectionAllowed());

							if (false == T4JStringUtils.isNullOrEmpty(descriptor.itemsGeneratorClassName())) {

								String className = descriptor.itemsGeneratorClassName();

								try {

									T4JSelectItemsGenerator itemsGenerator = (T4JSelectItemsGenerator) Class.forName(className).newInstance();
									itemsGenerator.generateItems(comboBox, bean);
								} catch (Exception e) {
									// Ignore
								}
							}

							field = comboBox;
							break;

						case UNDEFINED:

							field = defaultField;
							break;
					}

					useDefaultField = true;

					if (false == T4JStringUtils.isNullOrEmpty(descriptor.validatorClassNames())) {

						String[] validatorClassNames = descriptor.validatorClassNames().split("\\s+");

						if (false == T4JCollectionUtils.isNullOrEmpty(validatorClassNames)) {

							for (int i = 0; i < validatorClassNames.length; i++) {

								try {

									String validatorClassName = validatorClassNames[i];

									if (T4JStringUtils.isNullOrEmpty(validatorClassName)) {

										continue;
									} else {

										if (-1 == validatorClassName.indexOf('.')) {

											validatorClassName = getDefaultValidatorClassNameWithPackage(validatorClassName);
										}

										Validator validator = (Validator) Class.forName(validatorClassName).newInstance();

										if (validator instanceof T4JFormFieldValidator) {

											T4JFormFieldValidator t4jFormFieldValidator = (T4JFormFieldValidator) validator;
											t4jFormFieldValidator.setForm(form);
											t4jFormFieldValidator.setField(field);
										} else if (validator instanceof T4JAbstractFormValidator) {

											T4JAbstractFormValidator t4jFormValidator = (T4JAbstractFormValidator) validator;
											t4jFormValidator.setForm(form);
										}

										field.addValidator(validator);
									}
								} catch (Exception e) {
									logger.warn("CAN NOT ADD VALIDATOR " + validatorClassNames[i]);
								}
							}
						}
					}

					if (false == T4JStringUtils.isNullOrEmpty(descriptor.listenerClassNames())) {

						String[] listenerClassNames = descriptor.listenerClassNames().split("\\s+");

						if (false == T4JCollectionUtils.isNullOrEmpty(listenerClassNames)) {

							for (int i = 0; i < listenerClassNames.length; i++) {

								try {

									Listener listener = (Listener) Class.forName(listenerClassNames[i]).newInstance();

									if (listener instanceof T4JFormEventListener) {

										T4JFormEventListener t4jFormEventListener = (T4JFormEventListener) listener;
										t4jFormEventListener.setForm(form);
									}

									field.addListener(listener);
								} catch (Exception e) {
									logger.warn("CAN NOT ADD LISTENER " + listenerClassNames[i]);
								}
							}
						}
					}

					if (false == T4JStringUtils.isNullOrEmpty(descriptor.cssClassNames())) {

						String[] cssClassNames = descriptor.cssClassNames().split("\\s+");

						if (false == T4JCollectionUtils.isNullOrEmpty(cssClassNames)) {

							for (int i = 0; i < cssClassNames.length; i++) {

								String cssClassName = cssClassNames[i];

								if (false == T4JStringUtils.isNullOrEmpty(cssClassName)) {

									field.addStyleName(cssClassName);
								}
							}
						}

					}

					if (descriptor.required()) {

						field.addStyleName("t4j-required");
						field.setRequired(true);
					}

					if (descriptor.readonly()) {

						field.addStyleName("t4j-readonly");
						field.setReadOnly(true);
					}

					if (false == T4JStringUtils.isNullOrEmpty(descriptor.formatterClassName())) {

						try {
							PropertyFormatter propertyFormatter = (PropertyFormatter) Class.forName(descriptor.formatterClassName()).newInstance();
							field.setPropertyDataSource(propertyFormatter);
						} catch (Exception e) {
							// Ignore
						}
					}

					if (false == T4JStringUtils.isNullOrEmpty(descriptor.fieldConfiguratorClassName())) {

						try {

							T4JFieldConfigurator fieldConfigurator = (T4JFieldConfigurator) Class.forName(descriptor.fieldConfiguratorClassName()).newInstance();
							field = fieldConfigurator.configureField(form, field, bean, propertyId);
						} catch (Exception e) {
							// Ignore
						}
					}
				}

				setFieldCaption(bean, field, propertyId, defaultCaption);

				setFieldTooltip(bean, field, propertyId);

			} catch (Exception e) {

				logger.error(e, e);

				if (useDefaultField) {

					field = defaultField;
				} else {

					field = null;
				}
			}

			return field;
		} else {

			logger.debug("item is not instance of BeanItem");
			return defaultField;
		}
	}
}
