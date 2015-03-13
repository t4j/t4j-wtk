
package com.t4j.wtk.components.forms;

import java.util.Collection;
import java.util.LinkedList;

import com.t4j.wtk.util.T4JCollectionUtils;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Form;
import com.vaadin.ui.FormFieldFactory;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Layout;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public class T4JForm extends Form {

	private static final long serialVersionUID = 1L;

	private Boolean isVisibleItemPropertiesValueSet;

	private T4JForm(Layout formLayout, FormFieldFactory fieldFactory) {

		super(formLayout, fieldFactory);
	}

	public T4JForm() {

		this(new FormLayout(), new T4JFormFieldFactory());
	}

	public T4JForm(Layout formLayout) {

		this(formLayout, new T4JFormFieldFactory());
	}

	/**
	 * Updates the internal form datasource.
	 * 
	 * Method setFormDataSource.
	 * 
	 * @param data
	 * @param properties
	 */
	@Override
	protected void setFormDataSource(Object data, Collection<?> properties) {

		if (null == data) {

			this.setItemDataSource(null);
		} else {

			Item item = null;

			if (data instanceof Item) {

				item = (Item) data;
			} else {

				item = new BeanItem<Object>(data);
			}

			if (T4JCollectionUtils.isNullOrEmpty(properties)) {

				this.setItemDataSource(item);
			} else {

				this.setItemDataSource(item, properties);
			}
		}
	}

	public Boolean getIsVisibleItemPropertiesValueSet() {

		return isVisibleItemPropertiesValueSet;
	}

	public void setIsVisibleItemPropertiesValueSet(Boolean isVisibleItemPropertiesValueSet) {

		this.isVisibleItemPropertiesValueSet = isVisibleItemPropertiesValueSet;
	}

	@Override
	public void setVisibleItemProperties(Collection<?> visibleProperties) {

		if (T4JCollectionUtils.isNullOrEmpty(visibleProperties)) {

			setIsVisibleItemPropertiesValueSet(Boolean.FALSE);
			super.setVisibleItemProperties((Collection<?>) null);
		} else {

			setIsVisibleItemPropertiesValueSet(Boolean.TRUE);
			super.setVisibleItemProperties(visibleProperties);
		}
	}

	/**
	 * Sets the visibleProperties.
	 * 
	 * @param visibleProperties
	 *            the visibleProperties to set.
	 */
	@Override
	public void setVisibleItemProperties(Object[] visibleProperties) {

		if (null == visibleProperties) {

			setVisibleItemProperties((Collection<?>) null);
		} else {

			LinkedList<Object> linkedList = new LinkedList<Object>();

			for (int i = 0; i < visibleProperties.length; i++) {

				linkedList.add(visibleProperties[i]);
			}

			setVisibleItemProperties(linkedList);
		}
	}
}
