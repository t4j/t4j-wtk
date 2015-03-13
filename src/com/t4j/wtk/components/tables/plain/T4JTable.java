
package com.t4j.wtk.components.tables.plain;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import com.t4j.wtk.T4JConstants;
import com.t4j.wtk.beans.T4JProperty;
import com.t4j.wtk.components.tables.containers.plain.T4JTableContainer;
import com.t4j.wtk.components.tables.containers.plain.T4JTableContainerItemSetChangeEvent;
import com.t4j.wtk.util.T4JStringUtils;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.ui.Table;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public class T4JTable extends Table {

	private static final long serialVersionUID = 1L;

	private HashMap<Object, String> columnAlignments;

	private T4JTableContainer container;

	private Integer itemsPerPage;

	private LinkedList<T4JTablePageChangeListener> pageChangeListeners;

	private Integer pagesCount;

	public T4JTable(Container dataSource, Integer itemsPerPage, Boolean isMultipleSelectionEnabled) {

		super();

		if (null == itemsPerPage || itemsPerPage.compareTo(Integer.valueOf(1)) < 0) {
			itemsPerPage = Integer.valueOf(10);
		}

		this.itemsPerPage = itemsPerPage;

		addStyleName("t4j-table");

		setContainerDataSource(dataSource);

		setPageLength(itemsPerPage.intValue());

		setColumnCollapsingAllowed(false);
		setColumnReorderingAllowed(false);
		setEditable(false);
		setImmediate(false);
		setMultiSelect(Boolean.TRUE.equals(isMultipleSelectionEnabled));

		setReadOnly(true);
		setSelectable(false);
		setSortDisabled(false);

		setWidth(T4JConstants.FULL_WIDTH_PERCENTAGE);

		alwaysRecalculateColumnWidths = true;
	}

	private void firePageChangeEvent() {

		if (null == pageChangeListeners) {

			pageChangeListeners = new LinkedList<T4JTablePageChangeListener>();
		}

		T4JTablePageChangeEvent event = new T4JTablePageChangeEvent(this);

		for (T4JTablePageChangeListener listener : pageChangeListeners) {

			listener.pageChanged(event);
		}
	}

	private void setPageFirstIndex(int firstIndex) {

		if (0 > firstIndex) {

			firstIndex = 0;
		}

		int lastIndexOf = container.getItemsCount() - 1;

		if (firstIndex > lastIndexOf) {

			int pageIndex = (int) Math.floor(1.0 * (lastIndexOf / itemsPerPage.intValue()));

			firstIndex = pageIndex * itemsPerPage.intValue();
		}

		container.setFirstItemIndex(Integer.valueOf(firstIndex));

		T4JTableContainerItemSetChangeEvent paginatedTableItemSetChangeEvent = new T4JTableContainerItemSetChangeEvent(container);

		containerItemSetChange(paginatedTableItemSetChangeEvent);

		for (Object columnId : container.getContainerPropertyIds()) {
			setColumnWidth(columnId, -1);
		}

		firePageChangeEvent();
	}

	@Override
	protected String formatPropertyValue(Object rowId, Object colId, Property property) {

		if (null == property) {

			return "";
		} else {

			Object propertyValue = property.getValue();

			if (null == columnAlignments) {

				columnAlignments = new HashMap<Object, String>();
			}

			if (propertyValue instanceof Number) {

				columnAlignments.put(colId, Table.ALIGN_RIGHT);
			} else if (propertyValue instanceof Boolean) {

				columnAlignments.put(colId, Table.ALIGN_CENTER);
			} else if (propertyValue instanceof Date) {

				columnAlignments.put(colId, Table.ALIGN_CENTER);
			} else {

				columnAlignments.put(colId, Table.ALIGN_LEFT);
			}

			if (property instanceof T4JProperty) {

				T4JProperty castedInstance = (T4JProperty) property;

				if (T4JStringUtils.isNullOrEmpty(castedInstance.getPropertyName())) {

					try {
						castedInstance.setPropertyName(colId.toString());
					} catch (Exception e) {
						// Ignore
					}
				}
			}

			return property.toString();
		}
	}

	public void addListener(T4JTablePageChangeListener listener) {

		if (null == pageChangeListeners) {

			pageChangeListeners = new LinkedList<T4JTablePageChangeListener>();
		}

		pageChangeListeners.add(listener);
	}

	/**
	 * Gets the specified column's alignment.
	 * 
	 * @param propertyId
	 *            the propertyID identifying the column.
	 * @return the specified column's alignment if it as one; null otherwise.
	 */

	@Override
	public String getColumnAlignment(Object propertyId) {

		Class<?> classType = container.getType(propertyId);

		if (null == classType) {

			if (null == columnAlignments) {

				columnAlignments = new HashMap<Object, String>();
			}

			String alignment = columnAlignments.get(propertyId);

			return null == alignment ? Table.ALIGN_LEFT : alignment;
		} else {

			if (Number.class.isAssignableFrom(classType)) {

				return Table.ALIGN_RIGHT;
			} else if (Boolean.class.isAssignableFrom(classType)) {

				return Table.ALIGN_CENTER;
			} else if (Date.class.isAssignableFrom(classType)) {

				return Table.ALIGN_CENTER;
			} else {

				return Table.ALIGN_LEFT;
			}
		}
	}

	public T4JTableContainer getContainer() {

		return container;
	}

	@Override
	public Indexed getContainerDataSource() {

		return container;
	}

	public Integer getItemsPerPage() {

		return itemsPerPage;
	}

	public Integer getPagesCount() {

		pagesCount = Integer.valueOf((int) Math.ceil(1.0 * container.getItemsCount().intValue() / itemsPerPage.intValue()));

		if (Integer.valueOf(1).compareTo(pagesCount) > 0) {

			pagesCount = Integer.valueOf(1);
		}

		return pagesCount;
	}

	public void nextPage() {

		setPageFirstIndex(container.getFirstItemIndex().intValue() + itemsPerPage.intValue());
	}

	public void previousPage() {

		setPageFirstIndex(container.getFirstItemIndex().intValue() - itemsPerPage.intValue());
	}

	public void refresh() {

		setPageFirstIndex(-1);
	}

	public void removeListener(T4JTablePageChangeListener listener) {

		if (null != pageChangeListeners) {

			pageChangeListeners.remove(listener);
		}
	}

	@Override
	public void setContainerDataSource(Container newDataSource) {

		if (null == newDataSource) {

			throw new IllegalArgumentException();
		} else {

			if (newDataSource instanceof Indexed) {

				super.setContainerDataSource(newDataSource);

				if (newDataSource instanceof T4JTableContainer) {

					T4JTableContainer paginatedTableContainer = (T4JTableContainer) newDataSource;
					container = paginatedTableContainer;
					firePageChangeEvent();
				}
			} else {

				throw new IllegalArgumentException();
			}
		}
	}

	public void setPageIndex(int pageIndex) {

		int firstItemIndex = (pageIndex - 1) * itemsPerPage.intValue();

		if (firstItemIndex < 0) {

			firstItemIndex = 0;
		}

		if (firstItemIndex != container.getFirstItemIndex().intValue()) {

			setPageFirstIndex(firstItemIndex);
		}
	}
}
