
package com.t4j.wtk.components.tables.tree;

import java.io.File;
import java.io.Serializable;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

import com.t4j.wtk.T4JConstants;
import com.t4j.wtk.application.T4JWebApp;
import com.t4j.wtk.application.enums.T4JWebAppTrayNotificationTypeEnum;
import com.t4j.wtk.components.buttons.T4JTreeTableButton;
import com.t4j.wtk.components.downloads.T4JZipStreamSource;
import com.t4j.wtk.components.forms.T4JFieldCaption;
import com.t4j.wtk.components.forms.T4JFieldDescriptor;
import com.t4j.wtk.components.forms.T4JFieldTypeEnum;
import com.t4j.wtk.components.forms.T4JForm;
import com.t4j.wtk.components.tables.containers.plain.T4JQueryBuilderFilterByItem;
import com.t4j.wtk.components.tables.containers.plain.T4JTableContainerColumnDescriptor;
import com.t4j.wtk.components.tables.containers.plain.T4JTableContainerFilter;
import com.t4j.wtk.components.tables.containers.plain.T4JTableContainerItem;
import com.t4j.wtk.components.tables.containers.tree.T4JTreeTableContainer;
import com.t4j.wtk.util.T4JCollectionUtils;
import com.t4j.wtk.util.T4JDateUtils;
import com.t4j.wtk.util.T4JLogUtils;
import com.t4j.wtk.util.T4JStringUtils;
import com.t4j.wtk.util.T4JTooltipUtils;
import com.vaadin.data.util.BeanItem;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutAction.ModifierKey;
import com.vaadin.event.ShortcutListener;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.BaseTheme;

/**
 *
 * @author Tenentia-4j, S.L.
 *
 */
public class T4JTreeTableComponent extends CustomComponent implements ClickListener, T4JTreeTablePageChangeListener, UncaughtExceptionHandler {

	private static final long serialVersionUID = 1L;

	private static final Log logger = T4JLogUtils.getLogger(T4JTreeTableComponent.class);

	public static final ThemeResource FIRST_PAGE_ICON = new ThemeResource("img/paginatorFirst.gif");

	public static final ThemeResource PREVIOUS_PAGE_ICON = new ThemeResource("img/paginatorPrevious.gif");

	public static final ThemeResource NEXT_PAGE_ICON = new ThemeResource("img/paginatorNext.gif");

	public static final ThemeResource LAST_PAGE_ICON = new ThemeResource("img/paginatorLast.gif");

	public static final ThemeResource EXCEL_ICON = new ThemeResource("img/excelIcon.gif");

	private final Boolean allowsMultipleSelection;

	private Boolean isExcelExportEnabled;

	private Boolean isKeyboardPaginationEnabled;

	private Boolean isMassiveToggleEvent;

	private final Integer itemsPerPage;

	private Integer pagesCount;

	private Serializable hierarchyColumnId;

	private LinkedList<CheckBox> checkBoxes;

	private LinkedList<Component> buttons;

	private HorizontalLayout buttonsBar;

	private HorizontalLayout paginationBar;

	private final VerticalLayout compositionRoot;

	private VerticalLayout filtersFormWrapper;

	private Integer currentPageIndex;

	private TextField currentPageIndexTextField;

	private Form filtersForm;

	private LinkedList<T4JTableContainerItem<?>> selectedItems;

	private T4JTreeTable treeTable;

	private final T4JTreeTableContainer container;

	private ProgressIndicator excelExportProgressIndicator;

	private Button generateExcelButton;

	private Button cancelExcelButton;

	private Thread excelExportThread;

	private String excelExportThreadName;

	private Panel filtersPanel;

	private LinkedList<ItemClickListener> itemClickListeners;

	private T4JTreeTableExcelExportWorker excelExportWorker;

	private String excelExportFileName;

	public T4JTreeTableComponent(T4JTreeTableContainer container) {

		this(container, null);
	}

	public T4JTreeTableComponent(T4JTreeTableContainer container, Boolean allowsMultipleSelection) {

		super();

		if (null == container) {
			throw new IllegalArgumentException();
		}

		if (null == allowsMultipleSelection) {
			allowsMultipleSelection = Boolean.FALSE;
		}

		this.container = container;
		this.allowsMultipleSelection = allowsMultipleSelection;

		addStyleName("t4j-table-component");

		currentPageIndex = Integer.valueOf(1);
		itemsPerPage = container.getItemsPerPage();

		compositionRoot = new VerticalLayout();
		compositionRoot.setSpacing(true);

		setCompositionRoot(compositionRoot);
	}

	private void clearExpandedNodes() {

		if (null == treeTable) {

			return;
		} else {

			treeTable.clearExpandedNodes();
		}
	}

	private void goToPage() {

		Integer tmpInteger = null;

		try {

			int tmpInt = Integer.parseInt(currentPageIndexTextField.getValue().toString());

			tmpInteger = Integer.valueOf(tmpInt);

			if (tmpInteger.compareTo(Integer.valueOf(1)) < 0) {
				tmpInteger = Integer.valueOf(1);
			} else if (tmpInteger.compareTo(pagesCount) > 0) {
				tmpInteger = pagesCount;
			}
		} catch (Exception e) {

			tmpInteger = Integer.valueOf(1);
		}

		setCurrentPageIndex(tmpInteger);

	}

	private void prepareFiltersForm() {

		if (null == filtersPanel) {

			filtersFormWrapper = new VerticalLayout();
			filtersFormWrapper.setSpacing(true);

			filtersForm = new T4JForm();
			filtersForm.addStyleName("multipart");
			filtersForm.addStyleName("collapsible");
			filtersForm.setCaption("Filtros");

			for (Iterator<T4JTableContainerFilter> iterator = container.getContainerFilters().iterator(); iterator.hasNext();) {

				T4JTableContainerFilter filter = iterator.next();

				if (null != filter) {

					Field field = filter.generateField();
					filtersForm.addField(filter.getFormPropertyId(), field);
				}
			}

			filtersFormWrapper.addComponent(filtersForm);

			HorizontalLayout formButtonsWrapper = new HorizontalLayout();
			formButtonsWrapper.setSizeUndefined();
			formButtonsWrapper.setSpacing(true);

			Button clearButton = new Button(getT4JWebApp().getI18nString("button.clearFilters"), new ClickListener() {

				private static final long serialVersionUID = 1L;

				public void buttonClick(ClickEvent event) {

					clearFilters();
				}
			});

			clearButton.addStyleName("adq-button");
			clearButton.setClickShortcut(KeyCode.R, ModifierKey.CTRL);
			clearButton.setDescription(T4JTooltipUtils.generatePopupMessage(T4JWebApp.getInstance().getI18nString("T4JTableComponent.clearFiltersButtonTooltip")));

			formButtonsWrapper.addComponent(clearButton);

			Button submitButton = new Button(getT4JWebApp().getI18nString("button.filterResults"), new ClickListener() {

				private static final long serialVersionUID = 1L;

				public void buttonClick(ClickEvent event) {

					applyFilters();
				}
			});

			submitButton.addStyleName("adq-button");

			formButtonsWrapper.addComponent(submitButton);

			filtersFormWrapper.addComponent(formButtonsWrapper);
			filtersFormWrapper.setComponentAlignment(formButtonsWrapper, Alignment.MIDDLE_RIGHT);

			filtersPanel = new Panel(filtersFormWrapper);
			filtersPanel.setStyleName(T4JConstants.LIGHT_PANEL_STYLE_CLASS);
			filtersPanel.setScrollable(false);

			filtersPanel.addAction(new ShortcutListener(T4JConstants.EMPTY_STRING, KeyCode.ENTER, null) {

				private static final long serialVersionUID = 1L;

				@Override
				public void handleAction(Object sender, Object target) {

					applyFilters();
				}
			});
		}

		compositionRoot.addComponent(filtersPanel);
	}

	private void prepareTableButtons() {

		HorizontalLayout buttonsWrapper = new HorizontalLayout();
		buttonsWrapper.setSizeFull();

		buttonsBar = new HorizontalLayout();
		buttonsBar.addStyleName("t4j-table-buttons");
		buttonsBar.setMargin(true, false, true, false);
		buttonsBar.setSizeUndefined();
		buttonsBar.setSpacing(true);

		if (null == buttons) {

			buttons = new LinkedList<Component>();
		}

		for (Iterator<Component> iterator = buttons.iterator(); iterator.hasNext();) {

			Component button = iterator.next();

			if (button instanceof T4JTreeTableButton) {

				T4JTreeTableButton t4jTableButton = (T4JTreeTableButton) button;

				if (t4jTableButton.checkIsRendered(this)) {

					buttonsBar.addComponent(button);
				}
			} else {

				buttonsBar.addComponent(button);
			}
		}

		buttonsWrapper.addComponent(buttonsBar);
		buttonsWrapper.setComponentAlignment(buttonsBar, Alignment.MIDDLE_LEFT);

		if (Boolean.TRUE.equals(isExcelExportEnabled)) {

			Button excelExportButton = new Button(null, new ClickListener() {

				private static final long serialVersionUID = 1L;

				public void buttonClick(ClickEvent event) {

					showExcelExportDialog();
				}
			});

			excelExportButton.addStyleName(BaseTheme.BUTTON_LINK);
			excelExportButton.setDescription(T4JTooltipUtils.generatePopupMessage(T4JWebApp.getInstance().getI18nString("T4JTableComponent.excelExportButtonTooltip")));
			excelExportButton.setIcon(EXCEL_ICON);

			buttonsWrapper.addComponent(excelExportButton);
			buttonsWrapper.setComponentAlignment(excelExportButton, Alignment.MIDDLE_RIGHT);
		}

		compositionRoot.addComponent(buttonsWrapper);
	}

	private void prepareTablePaginationControls() {

		paginationBar = new HorizontalLayout();
		paginationBar.setWidth(T4JConstants.FULL_WIDTH_PERCENTAGE);

		HorizontalLayout paginationControlsWrapper = new HorizontalLayout();
		paginationControlsWrapper.addStyleName("t4j-pagination-controls");
		paginationControlsWrapper.setMargin(true, false, true, false);
		paginationControlsWrapper.setSizeUndefined();
		paginationControlsWrapper.setSpacing(true);

		currentPageIndexTextField = new TextField();
		currentPageIndexTextField.addStyleName("numeric");
		currentPageIndexTextField.setValue(currentPageIndex);
		currentPageIndexTextField.setWidth(getT4JWebApp().getSizeInEMs(60));

		currentPageIndexTextField.addListener(new BlurListener() {

			private static final long serialVersionUID = 1L;

			public void blur(BlurEvent event) {

				goToPage();
			}
		});

		Button firstPageButton = new Button(null, new ClickListener() {

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {

				setCurrentPageIndex(Integer.valueOf(1));

				currentPageIndex = Integer.valueOf(1);
				currentPageIndexTextField.setValue(currentPageIndex);
				treeTable.setPageIndex(currentPageIndex.intValue());

			}
		});
		firstPageButton.addStyleName(BaseTheme.BUTTON_LINK);
		firstPageButton.setIcon(FIRST_PAGE_ICON);

		if (Boolean.TRUE.equals(isKeyboardPaginationEnabled)) {

			firstPageButton.setClickShortcut(KeyCode.HOME, ModifierKey.CTRL, ModifierKey.SHIFT);
			firstPageButton.setDescription(T4JTooltipUtils.generatePopupMessage("CTRL + SHIFT + HOME"));
		}

		paginationControlsWrapper.addComponent(firstPageButton);
		paginationControlsWrapper.setComponentAlignment(firstPageButton, Alignment.MIDDLE_RIGHT);

		Button previousPageButton = new Button(null, new ClickListener() {

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {

				setCurrentPageIndex(Integer.valueOf(currentPageIndex.intValue() - 1));
			}
		});
		previousPageButton.addStyleName(BaseTheme.BUTTON_LINK);
		previousPageButton.setIcon(PREVIOUS_PAGE_ICON);

		if (Boolean.TRUE.equals(isKeyboardPaginationEnabled)) {

			previousPageButton.setClickShortcut(KeyCode.PAGE_UP, ModifierKey.CTRL, ModifierKey.SHIFT);
			previousPageButton.setDescription(T4JTooltipUtils.generatePopupMessage("CTRL + SHIFT + PG.UP"));
		}

		paginationControlsWrapper.addComponent(previousPageButton);
		paginationControlsWrapper.setComponentAlignment(previousPageButton, Alignment.MIDDLE_RIGHT);

		pagesCount = treeTable.getPagesCount();

		Label pageIndexPrefixLabel = new Label(getT4JWebApp().getI18nString("T4JTableComponent.pageNumber") + T4JConstants.WHITESPACE_STRING);
		paginationControlsWrapper.addComponent(pageIndexPrefixLabel);
		paginationControlsWrapper.setComponentAlignment(pageIndexPrefixLabel, Alignment.MIDDLE_RIGHT);

		paginationControlsWrapper.addComponent(currentPageIndexTextField);
		paginationControlsWrapper.setComponentAlignment(currentPageIndexTextField, Alignment.MIDDLE_RIGHT);

		Label pageIndexSuffixLabel = new Label(T4JConstants.WHITESPACE_STRING + getT4JWebApp().getI18nString("T4JTableComponent.ofPages") + T4JConstants.WHITESPACE_STRING + pagesCount);
		paginationControlsWrapper.addComponent(pageIndexSuffixLabel);
		paginationControlsWrapper.setComponentAlignment(pageIndexSuffixLabel, Alignment.MIDDLE_RIGHT);

		Button nextPageButton = new Button(null, new ClickListener() {

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {

				setCurrentPageIndex(Integer.valueOf(currentPageIndex.intValue() + 1));
			}
		});
		nextPageButton.addStyleName(BaseTheme.BUTTON_LINK);
		nextPageButton.setIcon(NEXT_PAGE_ICON);

		if (Boolean.TRUE.equals(isKeyboardPaginationEnabled)) {

			nextPageButton.setClickShortcut(KeyCode.PAGE_DOWN, ModifierKey.CTRL, ModifierKey.SHIFT);
			nextPageButton.setDescription(T4JTooltipUtils.generatePopupMessage("CTRL + SHIFT + PG.DOWN"));
		}

		paginationControlsWrapper.addComponent(nextPageButton);
		paginationControlsWrapper.setComponentAlignment(nextPageButton, Alignment.MIDDLE_RIGHT);

		Button lastPageButton = new Button(null, new ClickListener() {

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {

				setCurrentPageIndex(pagesCount);
			}
		});
		lastPageButton.addStyleName(BaseTheme.BUTTON_LINK);
		lastPageButton.setIcon(LAST_PAGE_ICON);

		if (Boolean.TRUE.equals(isKeyboardPaginationEnabled)) {

			lastPageButton.setClickShortcut(KeyCode.END, ModifierKey.CTRL, ModifierKey.SHIFT);
			lastPageButton.setDescription(T4JTooltipUtils.generatePopupMessage("CTRL + SHIFT + END"));
		}

		paginationControlsWrapper.addComponent(lastPageButton);
		paginationControlsWrapper.setComponentAlignment(lastPageButton, Alignment.MIDDLE_RIGHT);

		Label itemsTotalCountLabel = new Label("Total: " + container.getItemsCount());
		itemsTotalCountLabel.addStyleName("t4j-table-items-total-count");
		itemsTotalCountLabel.setSizeUndefined();

		paginationBar.addComponent(itemsTotalCountLabel);
		paginationBar.setComponentAlignment(itemsTotalCountLabel, Alignment.MIDDLE_LEFT);

		Panel paginationControlsPanel = new Panel(paginationControlsWrapper);
		paginationControlsPanel.setStyleName(T4JConstants.LIGHT_PANEL_STYLE_CLASS);
		paginationControlsPanel.setScrollable(false);
		paginationControlsPanel.setSizeUndefined();

		paginationControlsPanel.addAction(new ShortcutListener(T4JConstants.EMPTY_STRING, KeyCode.ENTER, null) {

			private static final long serialVersionUID = 1L;

			@Override
			public void handleAction(Object sender, Object target) {

				goToPage();
			}
		});

		paginationBar.addComponent(paginationControlsPanel);
		paginationBar.setComponentAlignment(paginationControlsPanel, Alignment.MIDDLE_RIGHT);

		compositionRoot.addComponent(paginationBar);

	}

	protected void abortExcelExport() {

		try {
			excelExportThread.interrupt();
		} catch (Throwable t) {
			// Ignore
		}
	}

	protected void clearSelection() {

		if (null == checkBoxes) {
			checkBoxes = new LinkedList<CheckBox>();
		}

		checkBoxes.clear();

		if (null == selectedItems) {
			selectedItems = new LinkedList<T4JTableContainerItem<?>>();
		}

		selectedItems.clear();
	}

	protected void generateExcelFile(Integer excelExportFromPageIndex, Integer excelExportToPageIndex) {

		excelExportWorker = getExcelExportWorker();

		excelExportWorker.setFromPage(excelExportFromPageIndex);
		excelExportWorker.setToPage(excelExportToPageIndex);

		excelExportWorker.resetInstanceFields();

		excelExportThreadName = "excelExportThread-" + System.currentTimeMillis();

		excelExportThread = new Thread(excelExportWorker, excelExportThreadName);
		excelExportThread.setUncaughtExceptionHandler(this);
		excelExportThread.setPriority(Thread.MIN_PRIORITY);
		excelExportThread.start();
	}

	protected ColumnGenerator getColumnGenerator(Object propertyId) {

		ColumnGenerator columnGenerator = null;

		for (Iterator<T4JTableContainerColumnDescriptor> iterator = container.getColumnDescriptors().iterator(); iterator.hasNext();) {

			T4JTableContainerColumnDescriptor columnDescriptor = iterator.next();

			if (columnDescriptor.getPropertyId().equals(propertyId)) {

				columnGenerator = columnDescriptor.getColumnGenerator();
				break;
			}
		}

		return columnGenerator;
	}

	protected T4JWebApp getT4JWebApp() {

		try {

			T4JWebApp tmpApplication = (T4JWebApp) super.getApplication();

			if (null == tmpApplication) {

				tmpApplication = T4JWebApp.getInstance();
			}

			return tmpApplication;
		} catch (Exception e) {

			return null;
		}
	}

	public void addButton(Button button) {

		if (null == button) {

			return;
		} else {

			if (null == buttons) {
				buttons = new LinkedList<Component>();
			}

			buttons.add(button);
		}
	}

	public void addButton(Link button) {

		if (null == button) {

			return;
		} else {

			if (null == buttons) {
				buttons = new LinkedList<Component>();
			}

			buttons.add(button);
		}
	}

	public void addItemClickListener(ItemClickListener itemClickListener) {

		if (null == itemClickListener) {

			return;
		} else {

			if (null == itemClickListeners) {

				itemClickListeners = new LinkedList<ItemClickEvent.ItemClickListener>();
			}

			itemClickListeners.add(itemClickListener);
		}
	}

	public void applyFilters() {

		refreshContainerFilterParameters();

		setCurrentPageIndex(null);

		forceRefresh();

	}

	@Override
	public void attach() {

		refreshContents();

		super.attach();
	}

	public void buttonClick(ClickEvent event) {

		if (null == selectedItems) {
			selectedItems = new LinkedList<T4JTableContainerItem<?>>();
		}

		Object itemId = event.getButton().getData();

		T4JTableContainerItem<?> selectedItem = (T4JTableContainerItem<?>) container.getItem(itemId);

		if (Boolean.TRUE.equals(allowsMultipleSelection)) {

			if (selectedItems.contains(selectedItem)) {
				selectedItems.remove(selectedItem);
			} else {
				selectedItems.add(selectedItem);
			}
		} else {

			for (Iterator<CheckBox> iterator = checkBoxes.iterator(); iterator.hasNext();) {

				CheckBox checkBox = iterator.next();

				if (checkBox.getData().equals(itemId)) {
					// Do nothing
				} else {
					checkBox.setValue(Boolean.FALSE);
				}
			}

			selectedItems.clear();

			if (event.getButton().booleanValue()) {
				selectedItems.add(selectedItem);
			}
		}

		refreshButtons();
	}

	public void clearButtons() {

		if (null == buttons) {
			buttons = new LinkedList<Component>();
		}

		buttons.clear();
	}

	public void clearFilters() {

		for (Iterator<T4JTableContainerFilter> iterator = container.getContainerFilters().iterator(); iterator.hasNext();) {

			T4JTableContainerFilter filter = iterator.next();

			if (null != filter) {
				filter.clearField(filtersForm.getField(filter.getFormPropertyId()));
			}
		}
	}

	public void clearFiltersAndRefresh() {

		clearFilters();
		applyFilters();
	}

	public void clearSelectedItems() {

		if (null == checkBoxes) {
			checkBoxes = new LinkedList<CheckBox>();
		}

		for (Iterator<CheckBox> iterator = checkBoxes.iterator(); iterator.hasNext();) {

			CheckBox checkBox = iterator.next();
			checkBox.setValue(Boolean.FALSE);
		}

		if (null == selectedItems) {
			selectedItems = new LinkedList<T4JTableContainerItem<?>>();
		}

		refreshButtons();
	}

	public void forceRefresh() {

		container.setFirstItemIndex(null);
		currentPageIndex = Integer.valueOf(1);
		refreshContents();
	}

	public List<CheckBox> getCheckBoxes() {

		return checkBoxes;
	}

	public T4JTreeTableContainer getContainer() {

		return container;
	}

	public String getExcelExportFileName() {

		return excelExportFileName;
	}

	public T4JTreeTableExcelExportWorker getExcelExportWorker() {

		if (null == excelExportWorker) {

			excelExportWorker = new T4JTreeTableExcelExportWorker(this);
		}

		return excelExportWorker;
	}

	public Object getHierarchyColumnId() {

		return hierarchyColumnId;
	}

	public Boolean getIsExcelExportEnabled() {

		return isExcelExportEnabled;
	}

	public Boolean getIsKeyboardPaginationEnabled() {

		return isKeyboardPaginationEnabled;
	}

	public List<ItemClickListener> getItemClickListeners() {

		if (null == itemClickListeners) {

			itemClickListeners = new LinkedList<ItemClickEvent.ItemClickListener>();
		}

		return itemClickListeners;
	}

	public Integer getItemsPerPage() {

		return itemsPerPage;
	}

	public Integer getPagesCount() {

		return pagesCount;
	}

	public List<T4JTableContainerItem<?>> getSelectedItems() {

		if (null == selectedItems) {
			selectedItems = new LinkedList<T4JTableContainerItem<?>>();
		}

		return selectedItems;
	}

	public T4JTreeTable getTreeTable() {

		return treeTable;
	}

	public void pageChanged(T4JTreeTablePageChangeEvent event) {

		if (null == selectedItems) {
			selectedItems = new LinkedList<T4JTableContainerItem<?>>();
		}

		selectedItems.clear();

		refreshButtons();
	}

	public void refreshButtons() {

		if (Boolean.TRUE.equals(isMassiveToggleEvent)) {

			return;
		} else {

			if (null == buttons) {
				buttons = new LinkedList<Component>();
			}

			if (null == selectedItems) {
				selectedItems = new LinkedList<T4JTableContainerItem<?>>();
			}

			int selectedItemsCount = selectedItems.size();

			for (Iterator<Component> iterator = buttons.iterator(); iterator.hasNext();) {

				Component button = iterator.next();

				if (button instanceof T4JTreeTableButton) {

					T4JTreeTableButton tableButton = (T4JTreeTableButton) button;

					if (tableButton.checkIsRendered(this)) {

						if (Boolean.TRUE.equals(tableButton.getIsAlwaysActive())) {
							tableButton.setEnabled(true);
						} else if (Boolean.TRUE.equals(tableButton.getIsActiveOnMultipleSelection())) {
							if (selectedItemsCount > 0) {
								tableButton.setEnabled(true);
							} else {
								tableButton.setEnabled(false);
							}
						} else {
							if (selectedItemsCount > 0) {
								tableButton.setEnabled(tableButton.checkIsEnabled(this));
							} else {
								tableButton.setEnabled(false);
							}
						}
					}
				}
			}
		}
	}

	public void refreshContainerFilterParameters() {

		StringBuffer stringBuffer = new StringBuffer(1024);

		ArrayList<T4JQueryBuilderFilterByItem> whereClauses = new ArrayList<T4JQueryBuilderFilterByItem>();

		List<T4JTableContainerFilter> containerFilters = container.getContainerFilters();

		if (false == T4JCollectionUtils.isNullOrEmpty(containerFilters)) {

			for (Iterator<T4JTableContainerFilter> iterator = containerFilters.iterator(); iterator.hasNext();) {

				T4JTableContainerFilter filter = iterator.next();

				if (null == filter) {

					continue;
				} else {

					Object whereClause = filter.generateWhereClause(filtersForm.getField(filter.getFormPropertyId()));

					if (null == whereClause) {

						continue;
					} else {
						if (whereClause instanceof T4JQueryBuilderFilterByItem) {

							whereClauses.add((T4JQueryBuilderFilterByItem) whereClause);
						} else if (whereClause instanceof String) {

							if (false == T4JStringUtils.isNullOrEmpty(whereClause)) {

								stringBuffer.append(" and ");
								stringBuffer.append(whereClause);
							}
						}
					}
				}
			}
		}

		if (T4JCollectionUtils.isNullOrEmpty(whereClauses)) {

			String tmpString = stringBuffer.toString();
			container.setFilterParameters(tmpString);
		} else {

			container.setFilterParameters(whereClauses);
		}
	}

	public void refreshContents() {

		compositionRoot.removeAllComponents();

		clearSelection();

		container.setTableComponent(this);

		if (null == treeTable) {

			treeTable = new T4JTreeTable(container, itemsPerPage, allowsMultipleSelection);

			treeTable.addListener(this);

			if (false == T4JCollectionUtils.isNullOrEmpty(itemClickListeners)) {

				for (Iterator<ItemClickListener> iterator = itemClickListeners.iterator(); iterator.hasNext();) {

					ItemClickListener listener = iterator.next();
					treeTable.addListener(listener);
				}

				treeTable.addStyleName("clickable");
			}

			if (Boolean.TRUE.equals(allowsMultipleSelection) && Boolean.TRUE.equals(container.getIsCheckboxVisible())) {

				treeTable.setImmediate(true);

				treeTable.addActionHandler(new Handler() {

					final Action selectAll = new Action(T4JWebApp.getInstance().getI18nString("button.selectAll"));

					final Action clearSelection = new Action(T4JWebApp.getInstance().getI18nString("button.clearSelection"));

					private static final long serialVersionUID = 1L;

					public Action[] getActions(Object target, Object sender) {

						return new Action[] {
							selectAll, //
							clearSelection
						};
					}

					public void handleAction(Action action, Object sender, Object target) {

						isMassiveToggleEvent = Boolean.TRUE;

						if (null == checkBoxes) {
							checkBoxes = new LinkedList<CheckBox>();
						}

						if (null == selectedItems) {
							selectedItems = new LinkedList<T4JTableContainerItem<?>>();
						}

						selectedItems.clear();

						if (selectAll.equals(action)) {
							for (Iterator<CheckBox> iterator = checkBoxes.iterator(); iterator.hasNext();) {

								CheckBox checkBox = iterator.next();
								checkBox.setValue(Boolean.TRUE);

								selectedItems.add((T4JTableContainerItem<?>) container.getItem(checkBox.getData()));
							}
						} else if (clearSelection.equals(action)) {
							for (Iterator<CheckBox> iterator = checkBoxes.iterator(); iterator.hasNext();) {

								CheckBox checkBox = iterator.next();
								checkBox.setValue(Boolean.FALSE);
							}
						} else {

						}

						isMassiveToggleEvent = Boolean.FALSE;

						refreshButtons();
					}
				});
			}

			List<?> propertyIds = (List<?>) container.getContainerPropertyIds();
			List<ColumnGenerator> columnGenerators = container.getColumnGenerators();

			int size = propertyIds.size();

			for (int i = 0; i < size; i++) {

				Object propertyId = propertyIds.get(i);
				ColumnGenerator columnGenerator = columnGenerators.get(i);

				if (null != columnGenerator) {
					treeTable.addGeneratedColumn(propertyId, columnGenerator);
				}
			}

			treeTable.setColumnHeaders(container.getColumnHeaders());

			if (null == hierarchyColumnId) {
				hierarchyColumnId = T4JConstants.EMPTY_STRING;
			}

			treeTable.setHierarchyColumnId(hierarchyColumnId);

		} else {

			treeTable.refresh();
		}

		if (false == T4JCollectionUtils.isNullOrEmpty(container.getContainerFilters())) {

			prepareFiltersForm();
		}

		prepareTablePaginationControls();
		compositionRoot.addComponent(treeTable);
		prepareTableButtons();

		refreshButtons();

	}

	public void removeButton(Component button) {

		if (null == buttons) {
			buttons = new LinkedList<Component>();
		}

		buttons.remove(button);
	}

	public void setCurrentPageIndex(Integer newPageIndex) {

		boolean isNullParameter = false;

		if (null == newPageIndex) {

			newPageIndex = Integer.valueOf(1);
			isNullParameter = true;
		} else {
			try {

				if (newPageIndex.compareTo(Integer.valueOf(1)) < 0) {
					newPageIndex = Integer.valueOf(1);
				} else if (newPageIndex.compareTo(pagesCount) > 0) {
					newPageIndex = pagesCount;
				}
			} catch (Exception e) {

				newPageIndex = Integer.valueOf(1);
			}
		}

		if (null != currentPageIndexTextField) {
			currentPageIndexTextField.setValue(newPageIndex);
		}

		logger.debug("currentPageIndex: " + newPageIndex);

		if (isNullParameter || newPageIndex.compareTo(currentPageIndex) != 0) {

			currentPageIndex = newPageIndex;

			if (null == treeTable) {

				return;
			} else {

				clearExpandedNodes();
				clearSelection();
				treeTable.setPageIndex(newPageIndex.intValue());
				refreshButtons();
			}
		}
	}

	public void setExcelExportFileName(String excelExportFileName) {

		this.excelExportFileName = excelExportFileName;
	}

	public void setExcelExportWorker(T4JTreeTableExcelExportWorker excelExportWorker) {

		this.excelExportWorker = excelExportWorker;
	}

	public void setHierarchyColumnId(Serializable hierarchyColumnId) {

		this.hierarchyColumnId = hierarchyColumnId;

		try {

			// Posible NullPointerException
			treeTable.setHierarchyColumnId(hierarchyColumnId);
		} catch (Exception e) {
			// Ignore
		}

	}

	public void setIsExcelExportEnabled(Boolean isExcelExportEnabled) {

		this.isExcelExportEnabled = isExcelExportEnabled;
	}

	public void setIsKeyboardPaginationEnabled(Boolean isKeyboardPaginationEnabled) {

		this.isKeyboardPaginationEnabled = isKeyboardPaginationEnabled;
	}

	public void showExcelExportDialog() {

		VerticalLayout modalWindowlLayout = new VerticalLayout();
		modalWindowlLayout.setSpacing(true);
		modalWindowlLayout.setWidth(getT4JWebApp().getSizeInEMs(376));

		final Form excelExportForm = new T4JForm();
		excelExportForm.setSizeUndefined();
		excelExportForm.addStyleName("excel-export-form");
		excelExportForm.setHeight(getT4JWebApp().getSizeInEMs(100));

		final T4JExcelExportPageRangeBean bean = new T4JExcelExportPageRangeBean(Integer.valueOf(1), Integer.valueOf(pagesCount));
		BeanItem<T4JExcelExportPageRangeBean> beanItem = new BeanItem<T4JExcelExportPageRangeBean>(bean);

		excelExportForm.setItemDataSource(beanItem);

		excelExportForm.setVisibleItemProperties(new Object[] {
			"fromPage", //
			"toPage"
		});

		modalWindowlLayout.addComponent(excelExportForm);

		excelExportProgressIndicator = new ProgressIndicator();
		excelExportProgressIndicator.setEnabled(false);
		excelExportProgressIndicator.setWidth(T4JConstants.FULL_WIDTH_PERCENTAGE);

		modalWindowlLayout.addComponent(excelExportProgressIndicator);

		HorizontalLayout buttonsWrapper = new HorizontalLayout();
		buttonsWrapper.setMargin(true, false, false, false);
		buttonsWrapper.setSpacing(true);
		buttonsWrapper.setSizeUndefined();

		generateExcelButton = new Button(getT4JWebApp().getI18nString("button.generate"), new ClickListener() {

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {

				excelExportForm.commit();

				Integer i1 = bean.getFromPage();
				Integer i2 = bean.getToPage();

				if (null == i1 || Integer.valueOf(1).compareTo(i1) > 0) {

					i1 = Integer.valueOf(1);
				}

				if (null == i2 || Integer.valueOf(1).compareTo(i2) > 0) {

					i2 = Integer.valueOf(2);
				}

				excelExportProgressIndicator.setEnabled(true);

				cancelExcelButton.setEnabled(true);

				generateExcelFile(i1, i2);
			}
		});

		generateExcelButton.addStyleName("adq-button");
		generateExcelButton.setDisableOnClick(true);

		cancelExcelButton = new Button(getT4JWebApp().getI18nString("button.cancel"), new ClickListener() {

			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {

				abortExcelExport();
			}
		});

		cancelExcelButton.addStyleName("adq-button");
		cancelExcelButton.setEnabled(false);

		buttonsWrapper.addComponent(generateExcelButton);
		buttonsWrapper.addComponent(cancelExcelButton);

		modalWindowlLayout.addComponent(buttonsWrapper);
		modalWindowlLayout.setComponentAlignment(buttonsWrapper, Alignment.MIDDLE_RIGHT);

		CloseListener closeListener = new CloseListener() {

			private static final long serialVersionUID = 1L;

			public void windowClose(CloseEvent e) {

				abortExcelExport();

			}
		};

		getT4JWebApp().showModalDialog(T4JWebApp.getInstance().getI18nString("T4JTableComponent.excelExportDialogTitle"), modalWindowlLayout, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, closeListener);
	}

	public void showExcelExportErrorNotification(File excelFile) {

		try {
			excelFile.delete();
		} catch (Exception e) {
			try {
				excelFile.deleteOnExit();
			} catch (Exception e2) {
				// Ignore
			}
		}

		T4JWebApp t4jWebApp = getT4JWebApp();

		t4jWebApp.closeDialogWindow();
		t4jWebApp.showTrayNotification(T4JWebAppTrayNotificationTypeEnum.ERROR, null, "MS EXCEL FILE GENERATION FAILED");
	}

	public void showExcelExportFileDownloadDialog(File excelFile) {

		T4JWebApp tmpWebApp = getT4JWebApp();

		Locale tmpLocale = tmpWebApp.getLocale();

		String tmpString = excelExportFileName;

		if (T4JStringUtils.isNullOrEmpty(tmpString)) {

			tmpString = "TableExport";
		}

		T4JZipStreamSource zipFileStreamSource = new T4JZipStreamSource(excelFile, T4JDateUtils.getFormattedTimestamp(Calendar.getInstance(tmpLocale).getTime()) + "." + tmpString + ".xls", true);

		tmpWebApp.showFileDownloadDialog(zipFileStreamSource);
	}

	public void showExcelExportInterruptedNotification(File excelFile) {

		try {
			excelFile.delete();
		} catch (Exception e) {
			try {
				excelFile.deleteOnExit();
			} catch (Exception e2) {
				// Ignore
			}
		}

		T4JWebApp t4jWebApp = getT4JWebApp();

		t4jWebApp.closeDialogWindow();
		t4jWebApp.showTrayNotification(T4JWebAppTrayNotificationTypeEnum.WARNING, null, "MS EXCEL FILE GENERATION ABORTED");
	}

	public void uncaughtException(Thread t, Throwable e) {

		logger.error("Thread: " + t + ", Exception: " + e, e);
	}

	public void updateExcelExportProgressIndicatorValue(Float newValue) {

		excelExportProgressIndicator.setValue(newValue);
	}

	public class T4JExcelExportPageRangeBean implements Serializable {

		private static final long serialVersionUID = 1L;

		@T4JFieldCaption(def = "Página inicial")
		@T4JFieldDescriptor(fieldType = T4JFieldTypeEnum.TEXTFIELD, cssClassNames = "numeric")
		private Integer fromPage;

		@T4JFieldCaption(def = "Página final")
		@T4JFieldDescriptor(fieldType = T4JFieldTypeEnum.TEXTFIELD, cssClassNames = "numeric")
		private Integer toPage;

		public T4JExcelExportPageRangeBean() {

			super();
		}

		public T4JExcelExportPageRangeBean(Integer fromPage, Integer toPage) {

			super();

			this.fromPage = fromPage;
			this.toPage = toPage;
		}

		public Integer getFromPage() {

			return fromPage;
		}

		public Integer getToPage() {

			return toPage;
		}

		public void setFromPage(Integer fromPage) {

			this.fromPage = fromPage;
		}

		public void setToPage(Integer toPage) {

			this.toPage = toPage;
		}
	}
}
