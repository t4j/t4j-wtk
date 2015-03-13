
package com.t4j.wtk.components.tables.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.t4j.wtk.T4JConstants;
import com.t4j.wtk.beans.T4JProperty;
import com.t4j.wtk.components.tables.containers.tree.T4JTreeTableContainer;
import com.t4j.wtk.components.tables.containers.tree.T4JTreeTableContainerItemSetChangeEvent;
import com.t4j.wtk.components.tables.plain.T4JTablePageChangeListener;
import com.t4j.wtk.util.T4JStringUtils;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.gwt.client.ui.VTreeTable;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public class T4JTreeTable extends TreeTable {

	private static final long serialVersionUID = 1L;

	private boolean animationsEnabled;

	private boolean clearFocusedRowPending;

	private boolean containerSupportsPartialUpdates;

	private Serializable focusedRowId;

	private Serializable hierarchyColumnId;

	private Serializable toggledItemId;

	private HashMap<Object, String> columnAlignments;

	private T4JTreeTableContainer container;

	private Integer itemsPerPage;

	private LinkedList<T4JTreeTablePageChangeListener> pageChangeListeners;

	private Integer pagesCount;

	private T4JHierarchicalStrategy containerStrategy;

	public T4JTreeTable(Container dataSource, Integer itemsPerPage, Boolean allowsMultipleSelection) {

		super();

		if (null == itemsPerPage || itemsPerPage.compareTo(Integer.valueOf(1)) < 0) {
			itemsPerPage = Integer.valueOf(10);
		}

		if (null == allowsMultipleSelection) {
			allowsMultipleSelection = Boolean.FALSE;
		}

		this.itemsPerPage = itemsPerPage;

		addStyleName("t4j-table");

		setContainerDataSource(dataSource);

		setPageLength(itemsPerPage.intValue());

		setColumnCollapsingAllowed(false);
		setColumnReorderingAllowed(false);
		setEditable(false);
		setImmediate(false);
		setMultiSelect(Boolean.TRUE.equals(allowsMultipleSelection) ? true : false);

		setReadOnly(true);
		setSelectable(false);
		setSortDisabled(false);

		setWidth(T4JConstants.FULL_WIDTH_PERCENTAGE);

		alwaysRecalculateColumnWidths = true;

		containerStrategy = new T4JHierarchicalStrategy();
	}

	private int countSubNodesRecursively(Hierarchical hc, Object itemId) {

		int count = 0;
		// we need the number of children for toggledItemId no matter if its
		// collapsed or expanded. Other items' children are only counted if the
		// item is expanded.
		if (containerStrategy.isNodeOpen(itemId) || itemId == toggledItemId) {
			Collection<?> children = hc.getChildren(itemId);
			if (children != null) {
				count += children != null ? children.size() : 0;
				for (Object id : children) {
					count += countSubNodesRecursively(hc, id);
				}
			}
		}
		return count;
	}

	private void firePageChangeEvent() {

		if (null == pageChangeListeners) {

			pageChangeListeners = new LinkedList<T4JTreeTablePageChangeListener>();
		}

		T4JTreeTablePageChangeEvent event = new T4JTreeTablePageChangeEvent(this);

		for (T4JTreeTablePageChangeListener listener : pageChangeListeners) {

			listener.pageChanged(event);
		}
	}

	private void focusParent(Object itemId) {

		boolean isInView = false;

		Object inPageId = getCurrentPageFirstItemId();

		for (int i = 0; inPageId != null && i < getPageLength(); i++) {

			if (inPageId.equals(itemId)) {

				isInView = true;
				break;
			}

			inPageId = nextItemId(inPageId);
			i++;
		}

		if (false == isInView) {

			setCurrentPageFirstItemId(itemId);
		}

		// Select the row if it is selectable.

		if (isSelectable()) {

			if (isMultiSelect()) {

				setValue(Collections.singleton(itemId));
			} else {

				setValue(itemId);
			}
		}

		setFocusedRow(itemId);
	}

	private void setFocusedRow(Object itemId) {

		focusedRowId = (Serializable) itemId;

		if (null == focusedRowId) {

			clearFocusedRowPending = true;
		}

		requestRepaint();
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

		containerStrategy.setPageFirstItemIndex(firstIndex);

		T4JTreeTableContainerItemSetChangeEvent paginatedTableItemSetChangeEvent = new T4JTreeTableContainerItemSetChangeEvent(container);

		containerItemSetChange(paginatedTableItemSetChangeEvent);

		for (Object columnId : container.getContainerPropertyIds()) {
			setColumnWidth(columnId, -1);
		}

		firePageChangeEvent();
	}

	private void toggleChildVisibility(Object itemId, boolean forceFullRefresh) {

		// logger.trace(Constants.STRING_L4J_TRACE);

		containerStrategy.toggleChildVisibility(itemId);

		// Ensure that page still has first item in page, DON'T clear the caches.

		// setCurrentPageFirstItemIndex(getCurrentPageFirstItemIndex());

		if (isCollapsed(itemId)) {
			fireCollapseEvent(itemId);
		} else {
			fireExpandEvent(itemId);
		}

		if (containerSupportsPartialUpdates && false == forceFullRefresh) {

			requestRepaint();
		} else {

			// For containers that do not send item set change events, always do full repaint instead of partial row update.
			refreshRowCache();
		}
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
			} else if (propertyValue instanceof Boolean || propertyValue instanceof Date) {

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

	@Override
	protected int getAddedRowCount() {

		return countSubNodesRecursively(container, toggledItemId);
	}

	@Override
	protected int getFirstAddedItemIndex() {

		return indexOfId(toggledItemId) + 1;
	}

	@Override
	protected Object getIdByIndex(int index) {

		return containerStrategy.getIdByIndex(index);
	}

	@Override
	protected int indexOfId(Object itemId) {

		return containerStrategy.indexOfId(itemId);
	}

	@Override
	protected boolean isPartialRowUpdate() {

		if (null == toggledItemId || false == containerSupportsPartialUpdates || isRowCacheInvalidated()) {

			return false;
		} else {

			return true;
		}
	}

	@Override
	protected void paintRowAttributes(PaintTarget target, Object itemId) throws PaintException {

		target.addAttribute("depth", containerStrategy.getDepth(itemId));

		if (container.areChildrenAllowed(itemId)) {

			target.addAttribute("ca", true);
			target.addAttribute("open", containerStrategy.isNodeOpen(itemId));
		}
	}

	@Override
	protected boolean shouldHideAddedRows() {

		return false == containerStrategy.isNodeOpen(toggledItemId);
	}

	public void addListener(T4JTreeTablePageChangeListener listener) {

		if (null == pageChangeListeners) {

			pageChangeListeners = new LinkedList<T4JTreeTablePageChangeListener>();
		}

		pageChangeListeners.add(listener);
	}

	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {

		super.changeVariables(source, variables);

		if (variables.containsKey("toggleCollapsed")) {

			String object = (String) variables.get("toggleCollapsed");
			Object itemId = itemIdMapper.get(object);

			toggledItemId = (Serializable) itemId;

			toggleChildVisibility(itemId, false);

			if (variables.containsKey("selectCollapsed")) {

				// ensure collapsed is selected unless opened with selection head

				if (isSelectable()) {

					select(itemId);
				}
			}
		} else if (variables.containsKey("focusParent")) {

			String key = (String) variables.get("focusParent");

			Object refId = itemIdMapper.get(key);
			Object itemId = getParent(refId);

			focusParent(itemId);
		}
	}

	public void clearExpandedNodes() {

		containerStrategy.clearExpandedNodes();
	}

	@Override
	public void containerItemSetChange(com.vaadin.data.Container.ItemSetChangeEvent event) {

		// Can't do partial repaints if items are added or removed during the
		// expand/collapse request
		toggledItemId = null;
		containerStrategy.containerItemSetChange(event);
		super.containerItemSetChange(event);
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

	public T4JTreeTableContainer getContainer() {

		return container;
	}

	@Override
	public Hierarchical getContainerDataSource() {

		return container;
	}

	@Override
	public Object getHierarchyColumnId() {

		return hierarchyColumnId;
	}

	@Override
	public Collection<?> getItemIds() {

		return containerStrategy.getItemIds();
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

	@Override
	public boolean isCollapsed(Object itemId) {

		return false == containerStrategy.isNodeOpen(itemId);
	}

	@Override
	public boolean isLastId(Object itemId) {

		return containerStrategy.isLastId(itemId);
	}

	@Override
	public Object lastItemId() {

		return containerStrategy.lastItemId();
	}

	@Override
	public Object nextItemId(Object itemId) {

		return containerStrategy.nextItemId(itemId);
	}

	public void nextPage() {

		setPageFirstIndex(container.getFirstItemIndex().intValue() + itemsPerPage.intValue());
	}

	@Override
	public void paintContent(PaintTarget target) throws PaintException {

		if (null == focusedRowId) {

			if (clearFocusedRowPending) {

				// Must still inform the client that the focusParent request has been processed

				target.addAttribute("clearFocusPending", true);
				clearFocusedRowPending = false;
			}
		} else {

			target.addAttribute("focusedRow", itemIdMapper.key(focusedRowId));
			focusedRowId = null;
		}

		target.addAttribute("animate", animationsEnabled);

		boolean useDefaultColumnId = null == hierarchyColumnId;

		if (false == useDefaultColumnId) {

			Object[] tmpArray = getVisibleColumns();

			for (int i = 0; i < tmpArray.length; i++) {

				Object tmpItem = tmpArray[i];

				if (hierarchyColumnId.equals(tmpItem)) {

					target.addAttribute(VTreeTable.ATTRIBUTE_HIERARCHY_COLUMN_INDEX, i);
					break;
				}
			}
		}

		super.paintContent(target);

		toggledItemId = null;
	}

	public void previousPage() {

		setPageFirstIndex(container.getFirstItemIndex().intValue() - itemsPerPage.intValue());
	}

	@Override
	public Object prevItemId(Object itemId) {

		return containerStrategy.prevItemId(itemId);
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

				if (newDataSource instanceof T4JTreeTableContainer) {

					T4JTreeTableContainer paginatedTableContainer = (T4JTreeTableContainer) newDataSource;
					container = paginatedTableContainer;
					firePageChangeEvent();
				}
			} else {

				throw new IllegalArgumentException();
			}
		}
	}

	public void setHierarchyColumnId(Object hierarchyColumnId) {

		this.hierarchyColumnId = (Serializable) hierarchyColumnId;

		if (null == this.hierarchyColumnId) {

			this.hierarchyColumnId = T4JConstants.EMPTY_STRING;
		}
	}

	public void setPageIndex(int pageIndex) {

		int firstItemIndex = (pageIndex - 1) * itemsPerPage.intValue();

		if (firstItemIndex < 0) {

			firstItemIndex = 0;
		}

		if (firstItemIndex == container.getFirstItemIndex().intValue()) {

			return;
		} else {

			setPageFirstIndex(firstItemIndex);
		}
	}

	@Override
	public int size() {

		return containerStrategy.size();
	}

	private class T4JHierarchicalStrategy implements Serializable {

		private static final long serialVersionUID = 1L;

		private final Set<Object> expandedNodes = new HashSet<Object>();

		private Integer pageFirstItemIndex = Integer.valueOf(0);

		private List<Object> pageItemsCache;

		private T4JHierarchicalStrategy() {

			super();
		}

		private void addVisibleChildTree(Object id) {

			if (isNodeOpen(id)) {

				Collection<?> children = container.getChildren(id);

				if (null == children) {

					return;
				} else {

					for (Object childId : children) {

						pageItemsCache.add(childId);

						addVisibleChildTree(childId);
					}
				}
			}

		}

		private void clearPageItemsCache() {

			pageItemsCache = null; // clear page items cache

			container.resetRootItemIds(pageFirstItemIndex);
		}

		/**
		 * Preorder of ids currently visible
		 * 
		 * @return
		 */
		private List<Object> getPageItemsCache() {

			if (null == pageItemsCache) {

				pageItemsCache = new ArrayList<Object>();

				Collection<?> rootItemIds = container.rootItemIds();

				for (Object rootItemId : rootItemIds) {

					pageItemsCache.add(rootItemId);

					addVisibleChildTree(rootItemId);
				}
			}

			return pageItemsCache;
		}

		public void clearExpandedNodes() {

			expandedNodes.clear();
		}

		public void containerItemSetChange(com.vaadin.data.Container.ItemSetChangeEvent event) {

			clearPageItemsCache();
		}

		public int getDepth(Object itemId) {

			int depth = 0;

			while (false == container.isRoot(itemId)) {

				depth++;
				itemId = container.getParent(itemId);
			}

			return depth;
		}

		public Object getIdByIndex(int index) {

			return getPageItemsCache().get(index);
		}

		public Collection<Object> getItemIds() {

			return Collections.unmodifiableCollection(getPageItemsCache());
		}

		public int indexOfId(Object id) {

			int index = getPageItemsCache().indexOf(id);

			return index;
		}

		public boolean isLastId(Object itemId) {

			if (itemId == null) {

				return false;
			}

			return itemId.equals(lastItemId());
		}

		public boolean isNodeOpen(Object itemId) {

			return expandedNodes.contains(itemId);
		}

		public Object lastItemId() {

			if (getPageItemsCache().size() > 0) {

				return getPageItemsCache().get(getPageItemsCache().size() - 1);
			} else {

				return null;
			}
		}

		public Object nextItemId(Object itemId) {

			int indexOf = getPageItemsCache().indexOf(itemId);

			if (indexOf == -1) {

				return null;
			}

			indexOf++;

			if (indexOf == getPageItemsCache().size()) {

				return null;
			} else {

				return getPageItemsCache().get(indexOf);
			}
		}

		public Object prevItemId(Object itemId) {

			int indexOf = getPageItemsCache().indexOf(itemId);

			indexOf--;

			if (indexOf < 0) {

				return null;
			} else {

				return getPageItemsCache().get(indexOf);
			}
		}

		public void setPageFirstItemIndex(int startIndex) {

			if (0 > startIndex) {

				startIndex = 0;
			}

			pageFirstItemIndex = Integer.valueOf(startIndex);

			clearPageItemsCache();
		}

		public int size() {

			return getPageItemsCache().size();
		}

		public void toggleChildVisibility(Object itemId) {

			if (false == expandedNodes.remove(itemId)) {

				expandedNodes.add(itemId);
			}

			clearPageItemsCache();
		}
	}
}
