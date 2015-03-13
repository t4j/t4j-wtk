
package com.t4j.wtk.components.tables.containers.plain;

import java.io.Serializable;

import com.t4j.wtk.beans.T4JProperty;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;

public class T4JTableContainerItem<BT> extends BeanItem<BT> {

	private static final long serialVersionUID = 1L;

	private Serializable itemId;

	public T4JTableContainerItem(BT bean) {

		super(bean);

		itemId = T4JTableContainer.getNextItemGeneratedId();
	}

	public T4JTableContainerItem(BT bean, Object itemId) {

		super(bean);

		if (null == itemId) {

			throw new IllegalArgumentException();
		}

		this.itemId = (Serializable) itemId;
	}

	@Override
	public boolean equals(Object other) {

		if (null == other) {
			return false;
		} else {
			if (this == other) {
				return true;
			} else {
				if (other instanceof T4JTableContainerItem) {
					try {
						T4JTableContainerItem<?> castOther = (T4JTableContainerItem<?>) other;
						return itemId.equals(castOther.itemId);
					} catch (Exception e) {
						return false;
					}
				} else {
					return false;
				}
			}
		}
	}

	public Object getItemId() {

		return itemId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.data.util.PropertysetItem#getItemProperty(java.lang.Object)
	 */
	@Override
	public Property getItemProperty(Object id) {

		Property tmpProperty = super.getItemProperty(id);

		if (null == tmpProperty || null == tmpProperty.getValue()) {

			return tmpProperty;
		} else {

			T4JProperty tmpObjectProperty = new T4JProperty((Serializable) tmpProperty.getValue());
			return tmpObjectProperty;
		}
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int hash = 17;

		hash = hash * prime + (null == itemId ? Long.valueOf(-1).hashCode() : itemId.hashCode());

		return hash;
	}
}
