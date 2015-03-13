
package com.t4j.wtk.components.tables.containers.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.logging.Log;

import com.t4j.wtk.T4JConstants;
import com.t4j.wtk.components.tables.containers.plain.T4JTableContainerColumnDescriptor;
import com.t4j.wtk.components.tables.containers.plain.T4JTableContainerFilter;
import com.t4j.wtk.components.tables.containers.plain.T4JTableContainerItem;
import com.t4j.wtk.components.tables.tree.T4JTreeTableComponent;
import com.t4j.wtk.util.T4JCollectionUtils;
import com.t4j.wtk.util.T4JLogUtils;
import com.t4j.wtk.util.T4JStringUtils;
import com.t4j.wtk.util.T4JTooltipUtils;
import com.vaadin.data.Container.Hierarchical;
import com.vaadin.data.Container.Indexed;
import com.vaadin.data.Container.ItemSetChangeNotifier;
import com.vaadin.data.Container.Sortable;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public abstract class T4JTreeTableContainer implements Indexed, ItemSetChangeNotifier, Hierarchical, Sortable {

	private static final long serialVersionUID = 1L;

	private static long itemGeneratedId = 0;

	private static final Log logger = T4JLogUtils.getLogger(T4JTreeTableContainer.class);

	private LinkedList<T4JTableContainerColumnDescriptor> columnDescriptors;

	private LinkedList<ColumnGenerator> columnGenerators;

	private LinkedList<String> columnHeaders;

	private LinkedList<T4JTableContainerFilter> containerFilters;

	private LinkedList<Object> containerPropertyIds;

	private LinkedHashMap<Object, Class<?>> containerPropertyTypes;

	private LinkedList<String> excelColumnHeaders;

	private Serializable filterParameters;

	private Integer firstItemIndex;

	private ColumnGenerator idColumnGenerator;

	private Boolean isCheckboxVisible;

	private Integer itemsCount;

	private LinkedList<ItemSetChangeListener> itemSetChangeListeners;

	private Integer itemsPerPage;

	private Integer offset;

	private LinkedList<T4JTableContainerItem<?>> pageItems;

	private Integer previousFirstItemIndex;

	private LinkedList<Object> sortablePropertyIds;

	private Serializable sortParameters;

	protected transient EntityManager excelExportEntityManager;

	protected LinkedList<Object> pageRootItemIds;

	protected T4JTreeTableComponent treeTableComponent;

	public T4JTreeTableContainer() {

		this(Integer.valueOf(10));
	}

	public T4JTreeTableContainer(Integer itemsPerPage) {

		super();

		if (null == itemsPerPage || Integer.valueOf(1).compareTo(itemsPerPage) > 0) {

			itemsPerPage = Integer.valueOf(10);
		}

		this.itemsPerPage = itemsPerPage;

		columnGenerators = new LinkedList<ColumnGenerator>();

		columnHeaders = new LinkedList<String>();
		containerPropertyIds = new LinkedList<Object>();
		containerPropertyTypes = new LinkedHashMap<Object, Class<?>>();
		excelColumnHeaders = new LinkedList<String>();
		containerFilters = new LinkedList<T4JTableContainerFilter>();
		sortablePropertyIds = new LinkedList<Object>();

		columnDescriptors = new LinkedList<T4JTableContainerColumnDescriptor>();

		isCheckboxVisible = Boolean.TRUE;

	}

	public static synchronized Long getNextItemGeneratedId() {

		return Long.valueOf(--itemGeneratedId);
	}

	protected void doRefresh(boolean isNormalExecution) {

		try {

			if (null == pageItems) {

				pageItems = new LinkedList<T4JTableContainerItem<?>>();
			} else {

				pageItems.clear();
			}

			if (null == pageRootItemIds) {

				pageRootItemIds = new LinkedList<Object>();
			} else {

				pageRootItemIds.clear();
			}

			if (T4JStringUtils.isNullOrEmpty(filterParameters)) {

				List<T4JTableContainerFilter> containerFilters = getContainerFilters();

				if (false == T4JCollectionUtils.isNullOrEmpty(containerFilters)) {

					refreshContainerFilterParameters();
				}
			}

			itemsCount = getCount(filterParameters);

			if (null == firstItemIndex) {

				firstItemIndex = Integer.valueOf(0);
				offset = Integer.valueOf(0);
			}

			pageItems = new LinkedList<T4JTableContainerItem<?>>(getPageItems(filterParameters, sortParameters, firstItemIndex, itemsPerPage));

			if (false == T4JCollectionUtils.isNullOrEmpty(pageItems)) {

				for (Iterator<T4JTableContainerItem<?>> iterator = pageItems.iterator(); iterator.hasNext();) {

					T4JTableContainerItem<?> pageItem = iterator.next();
					pageRootItemIds.add(pageItem.getItemId());
				}
			}

			boolean firstItemIndexChanged = false == firstItemIndex.equals(previousFirstItemIndex);

			if (isNormalExecution && firstItemIndexChanged) {

				previousFirstItemIndex = firstItemIndex;

				notifyListeners();
			}

		} catch (Exception e) {

			logger.trace(e, e);
		}
	}

	protected abstract T4JTableContainerItem<?> generateItemByItemId(Object itemId);

	protected void notifyListeners() {

		T4JTreeTableContainerItemSetChangeEvent event = new T4JTreeTableContainerItemSetChangeEvent(this);

		if (null == itemSetChangeListeners) {

			itemSetChangeListeners = new LinkedList<ItemSetChangeListener>();
		}

		for (Iterator<ItemSetChangeListener> iterator = itemSetChangeListeners.iterator(); iterator.hasNext();) {

			iterator.next().containerItemSetChange(event);
		}
	}

	protected void refreshContainerFilterParameters() {

		if (null == treeTableComponent) {

			return;
		} else {

			treeTableComponent.refreshContainerFilterParameters();
		}
	}

	public void addColumnDescriptor(T4JTableContainerColumnDescriptor columnDescriptor) {

		if (null == columnDescriptor) {

			throw new IllegalArgumentException();
		} else {

			columnGenerators.add(columnDescriptor.getColumnGenerator());
			columnHeaders.add(columnDescriptor.getHeaderText());
			containerPropertyIds.add(columnDescriptor.getPropertyId());
			containerPropertyTypes.put(columnDescriptor.getPropertyId(), columnDescriptor.getType());
			excelColumnHeaders.add(columnDescriptor.getExcelHeaderText());

			if (null != columnDescriptor.getFilter()) {

				containerFilters.add(columnDescriptor.getFilter());
			}

			if (Boolean.TRUE.equals(columnDescriptor.getIsSortable())) {

				sortablePropertyIds.add(columnDescriptor.getPropertyId());
			}

			columnDescriptors.add(columnDescriptor);
		}
	}

	public void addContainerFilter(T4JTableContainerFilter containerFilter) {

		if (null == containerFilter) {
			return;
		} else {
			containerFilters.add(containerFilter);
		}
	}

	public boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue) throws UnsupportedOperationException {

		throw new UnsupportedOperationException();
	}

	public Object addItem() throws UnsupportedOperationException {

		throw new UnsupportedOperationException();
	}

	public Item addItem(Object itemId) throws UnsupportedOperationException {

		throw new UnsupportedOperationException();
	}

	public Object addItemAfter(Object previousItemId) throws UnsupportedOperationException {

		throw new UnsupportedOperationException();
	}

	public Item addItemAfter(Object previousItemId, Object newItemId) throws UnsupportedOperationException {

		throw new UnsupportedOperationException();
	}

	public Object addItemAt(int index) throws UnsupportedOperationException {

		throw new UnsupportedOperationException();
	}

	public Item addItemAt(int index, Object newItemId) throws UnsupportedOperationException {

		throw new UnsupportedOperationException();
	}

	public void addListener(ItemSetChangeListener listener) {

		if (null == itemSetChangeListeners) {

			itemSetChangeListeners = new LinkedList<ItemSetChangeListener>();
		}

		itemSetChangeListeners.add(listener);

	}

	public boolean containsId(Object itemId) {

		return getItemIds().contains(itemId);
	}

	public Object firstItemId() {

		if (T4JCollectionUtils.isNullOrEmpty(pageItems)) {

			refresh();
		}

		if (T4JCollectionUtils.isNullOrEmpty(pageItems)) {

			return null;
		} else {

			return pageItems.get(0).getItemId();
		}
	}

	public List<T4JTableContainerColumnDescriptor> getColumnDescriptors() {

		return columnDescriptors;
	}

	public List<ColumnGenerator> getColumnGenerators() {

		if (null == idColumnGenerator) {

			idColumnGenerator = new ColumnGenerator() {

				private static final long serialVersionUID = 1L;

				public Object generateCell(Table source, Object itemId, Object columnId) {

					CheckBox checkBox = new CheckBox(null, treeTableComponent);
					checkBox.setData(itemId);
					checkBox.setDescription(T4JTooltipUtils.generatePopupHtmlMessage("<b>Id:</b> " + itemId));
					checkBox.setImmediate(true);

					treeTableComponent.getCheckBoxes().add(checkBox);

					return checkBox;
				}
			};
		}

		List<ColumnGenerator> tmpList = new ArrayList<ColumnGenerator>(columnGenerators);

		if (Boolean.TRUE.equals(isCheckboxVisible)) {

			tmpList.add(0, idColumnGenerator);
		}

		return Collections.unmodifiableList(tmpList);
	}

	public String[] getColumnHeaders() {

		if (null == columnHeaders) {

			columnHeaders = new LinkedList<String>();
		}

		List<String> tmpList = new ArrayList<String>(columnHeaders);

		if (Boolean.TRUE.equals(isCheckboxVisible)) {

			tmpList.add(0, T4JConstants.EMPTY_STRING);
		}

		String[] tmpArray = new String[tmpList.size()];

		return tmpList.toArray(tmpArray);
	}

	public List<T4JTableContainerFilter> getContainerFilters() {

		return Collections.unmodifiableList(containerFilters);
	}

	public Property getContainerProperty(Object itemId, Object propertyId) {

		if (T4JCollectionUtils.isNullOrEmpty(pageItems)) {

			refresh();
		}

		List<?> tmpList = (List<?>) getItemIds();

		if (tmpList.contains(itemId)) {

			Item tmpItem = pageItems.get(tmpList.indexOf(itemId));
			return tmpItem.getItemProperty(propertyId);
		} else {

			T4JTableContainerItem<?> tmpItem = generateItemByItemId(itemId);
			pageItems.add(tmpItem);

			return tmpItem.getItemProperty(propertyId);
		}
	}

	public Collection<?> getContainerPropertyIds() {

		List<Object> tmpList = new ArrayList<Object>(containerPropertyIds);

		if (Boolean.TRUE.equals(isCheckboxVisible)) {

			tmpList.add(0, T4JConstants.EMPTY_STRING);
		}

		return Collections.unmodifiableList(tmpList);
	}

	public abstract Integer getCount(Object filterParameters);

	public List<String> getExcelColumnHeaders() {

		return excelColumnHeaders;
	}

	public Object getFilterParameters() {

		if (null == filterParameters) {

			if (false == T4JCollectionUtils.isNullOrEmpty(containerFilters)) {

				refreshContainerFilterParameters();
			}
		}

		return null == filterParameters ? T4JConstants.EMPTY_STRING : filterParameters;
	}

	public Integer getFirstItemIndex() {

		if (null == firstItemIndex) {

			firstItemIndex = Integer.valueOf(0);
			offset = Integer.valueOf(0);
		}

		return firstItemIndex;
	}

	public Object getIdByIndex(int index) {

		if (T4JCollectionUtils.isNullOrEmpty(pageItems)) {

			refresh();
		}

		if (T4JCollectionUtils.isNullOrEmpty(pageItems)) {

			return null;
		} else {

			if (null == offset) {

				offset = Integer.valueOf(0);
			}

			try {

				return pageItems.get(index - offset.intValue()).getItemId();
			} catch (Exception e) {

				firstItemIndex = Integer.valueOf(index);
				offset = Integer.valueOf(index);

				refresh();

				try {

					return pageItems.get(index - offset.intValue()).getItemId();
				} catch (Exception e2) {

					return null;
				}
			}
		}
	}

	public Object getIdByIndexForExcelExport(int index) {

		if (T4JCollectionUtils.isNullOrEmpty(pageItems)) {

			silentRefresh();
		}

		if (null == pageItems) {

			return null;
		} else {

			if (null == offset) {

				offset = Integer.valueOf(0);
			}

			try {

				return pageItems.get(index - offset.intValue()).getItemId();
			} catch (Exception e) {

				firstItemIndex = Integer.valueOf(index);
				offset = Integer.valueOf(index);

				silentRefresh();

				try {

					return pageItems.get(index - offset.intValue()).getItemId();
				} catch (Exception e2) {

					logger.trace(e, e);
					return null;
				}
			}
		}
	}

	public ColumnGenerator getIdColumnGenerator() {

		return idColumnGenerator;
	}

	public Boolean getIsCheckboxVisible() {

		return isCheckboxVisible;
	}

	public Item getItem(Object itemId) {

		if (T4JCollectionUtils.isNullOrEmpty(pageItems)) {

			refresh();
		}

		if (null == pageItems) {

			return null;
		} else {

			List<?> tmpList = (List<?>) getItemIds();

			if (tmpList.contains(itemId)) {

				return pageItems.get(tmpList.indexOf(itemId));
			} else {

				if (isRoot(itemId)) {

					return null;
				} else {

					T4JTableContainerItem<?> tmpItem = generateItemByItemId(itemId);

					pageItems.add(tmpItem);

					return tmpItem;
				}
			}
		}
	}

	public Collection<?> getItemIds() {

		if (T4JCollectionUtils.isNullOrEmpty(pageItems)) {

			refresh();
		}

		List<Object> itemIds = new ArrayList<Object>();

		if (null != pageItems) {

			for (Iterator<?> iterator = pageItems.iterator(); iterator.hasNext();) {

				T4JTableContainerItem<?> item = (T4JTableContainerItem<?>) iterator.next();
				itemIds.add(item.getItemId());
			}
		}

		return itemIds;
	}

	public Integer getItemsCount() {

		if (null == itemsCount) {

			refresh();
		}

		if (null == itemsCount) {

			return Integer.valueOf(0);
		} else {

			return itemsCount;
		}
	}

	public Integer getItemsPerPage() {

		return itemsPerPage;
	}

	public abstract List<T4JTableContainerItem<?>> getPageItems(Object filterParameters, Object sortParameters, Integer firstIndex, Integer pageSize);

	public Collection<?> getSortableContainerPropertyIds() {

		List<Object> tmpList = new ArrayList<Object>(sortablePropertyIds);

		return Collections.unmodifiableList(tmpList);
	}

	public Object getSortParameters() {

		return null == sortParameters ? T4JConstants.EMPTY_STRING : sortParameters;
	}

	public Class<?> getType(Object propertyId) {

		return containerPropertyTypes.get(propertyId);
	}

	public int indexOfId(Object itemId) {

		if (T4JCollectionUtils.isNullOrEmpty(pageItems)) {

			refresh();
		}

		int methodResult = -1;

		int indexOf = 0;

		if (null != pageItems) {

			for (Iterator<?> iterator = pageItems.iterator(); iterator.hasNext();) {

				T4JTableContainerItem<?> item = (T4JTableContainerItem<?>) iterator.next();

				if (item.getItemId().equals(itemId)) {

					methodResult = indexOf;
					break;
				} else {
					indexOf++;
				}
			}
		}

		return methodResult;
	}

	public boolean isFirstId(Object itemId) {

		if (T4JCollectionUtils.isNullOrEmpty(pageItems)) {

			refresh();
		}

		if (T4JCollectionUtils.isNullOrEmpty(pageItems)) {

			return false;
		} else {

			return pageItems.get(0).getItemId().equals(itemId);
		}
	}

	public boolean isLastId(Object itemId) {

		if (T4JCollectionUtils.isNullOrEmpty(pageItems)) {

			refresh();
		}

		if (T4JCollectionUtils.isNullOrEmpty(pageItems)) {

			return false;
		} else {

			int lastIndex = pageItems.size() - 1;
			return pageItems.get(lastIndex).getItemId().equals(itemId);
		}
	}

	public Object lastItemId() {

		if (T4JCollectionUtils.isNullOrEmpty(pageItems)) {

			refresh();
		}

		if (T4JCollectionUtils.isNullOrEmpty(pageItems)) {

			return null;
		} else {

			int lastIndex = pageItems.size() - 1;
			return pageItems.get(lastIndex);
		}
	}

	public Object nextItemId(Object itemId) {

		if (T4JCollectionUtils.isNullOrEmpty(pageItems)) {

			refresh();
		}

		if (T4JCollectionUtils.isNullOrEmpty(pageItems)) {

			return null;
		} else {

			int indexOf = indexOfId(itemId);

			try {

				return pageItems.get(indexOf + 1);
			} catch (Exception e) {

				return null;
			}
		}
	}

	public Object prevItemId(Object itemId) {

		if (T4JCollectionUtils.isNullOrEmpty(pageItems)) {

			refresh();
		}

		if (T4JCollectionUtils.isNullOrEmpty(pageItems)) {

			return null;
		} else {

			int indexOf = indexOfId(itemId);

			try {

				return pageItems.get(indexOf - 1);
			} catch (Exception e) {

				return null;
			}
		}
	}

	public void refresh() {

		doRefresh(true);
	}

	public boolean removeAllItems() throws UnsupportedOperationException {

		throw new UnsupportedOperationException();
	}

	public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException {

		throw new UnsupportedOperationException();
	}

	public boolean removeItem(Object itemId) throws UnsupportedOperationException {

		throw new UnsupportedOperationException();
	}

	public void removeListener(ItemSetChangeListener listener) {

		if (null != itemSetChangeListeners) {

			itemSetChangeListeners.remove(listener);
		}
	}

	public void resetRootItemIds(Integer pageFirstItemIndex) {

	}

	public boolean setChildrenAllowed(Object itemId, boolean areChildrenAllowed) throws UnsupportedOperationException {

		throw new UnsupportedOperationException();
	}

	public void setFilterParameters(Serializable filterParameters) {

		this.filterParameters = filterParameters;
	}

	public void setFirstItemIndex(Integer newFirstItemIndex) {

		if (null == newFirstItemIndex || Integer.valueOf(0).compareTo(newFirstItemIndex) > 0) {

			// Nulo o menor que cero

			if (null == newFirstItemIndex) {

				previousFirstItemIndex = null;
			}

			newFirstItemIndex = Integer.valueOf(0);
			offset = Integer.valueOf(0);

			firstItemIndex = newFirstItemIndex;

			refresh();
		} else {

			if (false == newFirstItemIndex.equals(firstItemIndex)) {

				firstItemIndex = newFirstItemIndex;
				refresh();
			}
		}
	}

	public void setFirstItemIndexForExcel(Integer newFirstItemIndex) {

		if (null == newFirstItemIndex || Integer.valueOf(0).compareTo(newFirstItemIndex) > 0) {

			if (null == firstItemIndex) {

				previousFirstItemIndex = null;
			}

			firstItemIndex = Integer.valueOf(0);
			offset = Integer.valueOf(0);

			firstItemIndex = newFirstItemIndex;

			silentRefresh();
		} else {

			if (false == newFirstItemIndex.equals(firstItemIndex)) {

				firstItemIndex = newFirstItemIndex;
				silentRefresh();
			}
		}
	}

	public void setIdColumnGenerator(ColumnGenerator idColumnGenerator) {

		this.idColumnGenerator = idColumnGenerator;
	}

	public void setIsCheckboxVisible(Boolean isCheckboxVisible) {

		this.isCheckboxVisible = isCheckboxVisible;
	}

	public boolean setParent(Object itemId, Object newParentId) throws UnsupportedOperationException {

		throw new UnsupportedOperationException();
	}

	public void setSortParameters(Serializable sortParameters) {

		this.sortParameters = sortParameters;
	}

	public void setTableComponent(T4JTreeTableComponent paginatedTableComponent) {

		treeTableComponent = paginatedTableComponent;
	}

	public void silentRefresh() {

		doRefresh(false);
	}

	public int size() {

		if (null == itemsCount) {

			refresh();
		}

		if (null == itemsCount) {

			return 0;
		} else {

			if (null == firstItemIndex) {

				firstItemIndex = Integer.valueOf(0);
				offset = Integer.valueOf(0);
			}

			int i = itemsCount.intValue() - firstItemIndex.intValue();

			if (i > itemsPerPage.intValue()) {

				return itemsPerPage.intValue();
			} else {

				return i;
			}
		}
	}

	public void sort(Object[] propertyIds, boolean[] ascending) {

		StringBuffer stringBuffer = new StringBuffer(256);
		stringBuffer.append(" order by ");

		for (int i = 0; i < propertyIds.length; i++) {

			if (i > 0) {
				stringBuffer.append(", ");
			}

			stringBuffer.append("e.");
			stringBuffer.append(propertyIds[i]);
			stringBuffer.append(T4JConstants.WHITESPACE_STRING);
			stringBuffer.append(ascending[i] ? "asc" : "desc");
		}

		String tmpString = stringBuffer.toString();

		logger.debug(tmpString);

		setSortParameters(tmpString);
		setFirstItemIndex(null);

		treeTableComponent.setCurrentPageIndex(null);
	}

}
