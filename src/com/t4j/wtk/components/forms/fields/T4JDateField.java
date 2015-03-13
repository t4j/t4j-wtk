
package com.t4j.wtk.components.forms.fields;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.t4j.wtk.T4JConstants;
import com.t4j.wtk.application.T4JWebApp;
import com.t4j.wtk.util.T4JDateUtils;
import com.t4j.wtk.util.T4JLogUtils;
import com.t4j.wtk.util.T4JStringUtils;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertyFormatter;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.BaseTheme;

/**
 *
 * @author Tenentia-4j, S.L.
 *
 */
public class T4JDateField extends TextField implements FocusListener {

	private static final long serialVersionUID = 1L;

	private static final Log logger = T4JLogUtils.getLogger(T4JDateField.class);

	private static final ThemeResource NEXT_MONTH_ICON = new ThemeResource("img/paginatorNext.gif");

	private static final ThemeResource PREVIOUS_MONTH_ICON = new ThemeResource("img/paginatorPrevious.gif");

	private int currentMonth = -1;

	private int lowerYear;

	private int upperYear;

	private String datePattern;

	private Locale locale;

	private DateFormatSymbols dateFormatSymbols;

	private Button selectButton;

	private Button resetButton;

	private Button nextMonthButton;

	private Button previousMonthButton;

	private Button todayButton;

	private GridLayout monthDatesGridLayout;

	private Calendar internalCalendar;

	private Calendar selectedDate;

	private Window modalWindow;

	private ArrayList<ClickListener> selectButtonClickListeners;

	private NativeSelect monthsNativeSelect;

	private NativeSelect yearsNativeSelect;

	public T4JDateField() {

		super();

		setupComponent();
	}

	public T4JDateField(Property dataSource) {

		super(dataSource);

		setupComponent();
	}

	public T4JDateField(String caption) {

		super(caption);

		setupComponent();
	}

	public T4JDateField(String caption, Date value) {

		super(caption);

		setValue(value);

		setupComponent();
	}

	public T4JDateField(String caption, Property dataSource) {

		super(caption, dataSource);

		setupComponent();
	}

	private void changeMonth(int gap) {

		int monthIndex = internalCalendar.get(Calendar.MONTH);

		int newMonthIndex = monthIndex + gap;

		if (0 > newMonthIndex) {
			newMonthIndex = 11;
			changeYear(-1, false);
		} else if (11 < newMonthIndex) {
			newMonthIndex = 0;
			changeYear(1, false);
		}

		monthsNativeSelect.setValue(Integer.valueOf(newMonthIndex));

		currentMonth = newMonthIndex;
		internalCalendar.set(Calendar.MONTH, currentMonth);
		refreshMonthDatesGridLayout();
	}

	private void changeYear(int gap, boolean refreshLayout) {

		int year = internalCalendar.get(Calendar.YEAR);

		int newYear = year + gap;

		if (lowerYear > newYear) {

			return;
		} else if (newYear < upperYear) {

			yearsNativeSelect.setValue(Integer.valueOf(newYear));
			internalCalendar.set(Calendar.YEAR, newYear);

			if (refreshLayout) {
				refreshMonthDatesGridLayout();
			}
		} else {

			return;
		}

	}

	private void closeModalWindow() {

		T4JWebApp.getInstance().getMainWindow().removeWindow(modalWindow);
		modalWindow = null;
	}

	private Date getNormalizedDate(Date d) {

		Calendar c = Calendar.getInstance(locale);

		c.setTime(d);

		c.set(Calendar.HOUR_OF_DAY, 12);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		return c.getTime();
	}

	private boolean isSelectedDate(Calendar c) {

		if (null == selectedDate) {
			return false;
		} else if (0 == T4JDateUtils.compareNormalizedDates(selectedDate.getTime(), c.getTime())) {
			return true;
		} else {
			return false;
		}
	}

	private void normalizeInternalCalendar() {

		internalCalendar.set(Calendar.HOUR_OF_DAY, 12);
		internalCalendar.set(Calendar.MINUTE, 0);
		internalCalendar.set(Calendar.SECOND, 0);
		internalCalendar.set(Calendar.MILLISECOND, 0);
	}

	private void refreshMonthDatesGridLayout() {

		if (null == monthDatesGridLayout) {

			logger.warn("GridLayout is null");
			return;
		} else {

			Calendar tmpCalendar = (Calendar) internalCalendar.clone();

			tmpCalendar.set(Calendar.MONTH, currentMonth);
			tmpCalendar.set(Calendar.DATE, 1);

			int firstDayOfWeek = tmpCalendar.getFirstDayOfWeek();

			int dayOfWeek = tmpCalendar.get(Calendar.DAY_OF_WEEK);

			if (dayOfWeek == Calendar.SUNDAY) {

				dayOfWeek = 6;
			} else {

				dayOfWeek = dayOfWeek - 2;
			}

			tmpCalendar.add(Calendar.DATE, -1 * dayOfWeek);

			monthDatesGridLayout.removeAllComponents();

			String[] dayNamesArray = dateFormatSymbols.getShortWeekdays();

			Map<String, Integer> dayNames = new HashMap<String, Integer>();

			for (int i = 0; i < dayNamesArray.length; i++) {

				String key = dayNamesArray[i];
				dayNames.put(key, Integer.valueOf(i));
			}

			for (int i = 0; i < 7; i++) {

				for (int j = 0; j < 7; j++) {

					if (i == 0) {

						int tmpInt = j + firstDayOfWeek;

						if (tmpInt > 7) {

							tmpInt = 1;
						}

						for (Iterator<String> iterator = dayNames.keySet().iterator(); iterator.hasNext();) {

							String dayName = iterator.next();

							if (dayNames.get(dayName).equals(Integer.valueOf(tmpInt))) {

								Label dayLabel = new Label(dayName);
								dayLabel.addStyleName("day-name");
								dayLabel.setWidth("42px");

								monthDatesGridLayout.addComponent(dayLabel, j, i);
								monthDatesGridLayout.setComponentAlignment(dayLabel, Alignment.MIDDLE_CENTER);

								break;
							}
						}

					} else {

						final long timeInMillis = tmpCalendar.getTimeInMillis();

						Button dayButton = new Button(String.valueOf(tmpCalendar.get(Calendar.DATE)), new ClickListener() {

							private static final long serialVersionUID = 1L;

							public void buttonClick(ClickEvent event) {

								if (null == selectedDate) {

									selectedDate = Calendar.getInstance(locale);
								}

								selectedDate.setTimeInMillis(timeInMillis);

								for (int i = 1; i < 7; i++) {

									for (int j = 0; j < 7; j++) {

										monthDatesGridLayout.getComponent(j, i).removeStyleName("day-button-selected");
									}
								}

								event.getButton().addStyleName("day-button-selected");
							}
						});

						dayButton.addStyleName(BaseTheme.BUTTON_LINK);

						if (tmpCalendar.get(Calendar.MONTH) == currentMonth) {

							dayButton.addStyleName("day-button");
						} else {

							dayButton.addStyleName("day-button-other");
						}

						if (isSelectedDate(tmpCalendar)) {

							dayButton.addStyleName("day-button-selected");
						}

						dayButton.setWidth("42px");

						monthDatesGridLayout.addComponent(dayButton, j, i);
						monthDatesGridLayout.setComponentAlignment(dayButton, Alignment.MIDDLE_CENTER);

						tmpCalendar.add(Calendar.DATE, 1);
					}
				}
			}
		}
	}

	private void resetFields() {

		selectedDate = null;
		refreshMonthDatesGridLayout();
	}

	private void updateValue() {

		if (null == selectedDate) {

			setValue(null);
		} else {

			setValue(selectedDate.getTime());
		}

		closeModalWindow();
	}

	protected DateFormat getDateFormatter() {

		if (T4JStringUtils.isNullOrEmpty(datePattern)) {
			return DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
		} else {
			return new SimpleDateFormat(getDatePattern(), locale);
		}
	}

	@Override
	protected String getFormattedValue() {

		try {

			Object tmpValue = getValue();

			if (null == tmpValue) {

				if (null == selectedDate) {

					return T4JConstants.EMPTY_STRING;
				} else {

					tmpValue = new Date(selectedDate.getTimeInMillis());
				}
			}

			if (tmpValue instanceof Date) {

				try {

					return getDateFormatter().format(tmpValue);
				} catch (Exception e) {

					SimpleDateFormat defaultFormatter = new SimpleDateFormat(T4JConstants.DEFAULT_DATE_PATTERN, Locale.US);
					return defaultFormatter.format(tmpValue);
				}
			} else if (tmpValue instanceof String) {

				return (String) tmpValue;
			} else {

				return T4JConstants.EMPTY_STRING;
			}
		} catch (Exception e) {

			return T4JConstants.EMPTY_STRING;
		}
	}

	protected void setupComponent() {

		addListener((FocusListener) this);
		addStyleName("t4j-popup-textfield");
		addStyleName("t4j-date-popup-textfield");

		setImmediate(true);
		setNullRepresentation(T4JConstants.EMPTY_STRING);

		locale = T4JWebApp.getInstance().getLocale();

		dateFormatSymbols = new DateFormatSymbols(locale);

		internalCalendar = Calendar.getInstance(locale);

		normalizeInternalCalendar();
	}

	public void addSelectButtonClickListener(ClickListener clickListener) {

		if (null == selectButtonClickListeners) {
			selectButtonClickListeners = new ArrayList<ClickListener>();
		}

		selectButtonClickListeners.add(clickListener);
	}

	public void focus(FocusEvent event) {

		if (false == isReadOnly()) {

			Date tmpDate = null;

			try {

				Object tmpObject = getValue();

				if (Date.class.isAssignableFrom(tmpObject.getClass())) {
					tmpDate = (Date) tmpObject;
				} else {
					tmpDate = getDateFormatter().parse((String) tmpObject);
				}

			} catch (Exception e) {

				// Ignore
			}

			if (null == tmpDate) {

				tmpDate = new Date();
			}

			tmpDate = getNormalizedDate(tmpDate);

			internalCalendar.setTime(tmpDate);
			currentMonth = internalCalendar.get(Calendar.MONTH);
			selectedDate = (Calendar) internalCalendar.clone();

			int currentYear = internalCalendar.get(Calendar.YEAR);

			lowerYear = currentYear - 100;
			upperYear = currentYear + 101;

			Panel layoutWrapper = new Panel();
			layoutWrapper.addStyleName(T4JConstants.LIGHT_PANEL_STYLE_CLASS);
			layoutWrapper.setWidth(T4JWebApp.getInstance().getSizeInEMs(340));

			VerticalLayout modalDialogLayout = new VerticalLayout();
			modalDialogLayout.addStyleName("t4j-date-selection-dialog");
			modalDialogLayout.setSpacing(true);

			HorizontalLayout headerLayout = new HorizontalLayout();
			headerLayout.setSpacing(true);
			headerLayout.setWidth(T4JConstants.FULL_WIDTH_PERCENTAGE);

			previousMonthButton = new Button();
			previousMonthButton.addListener(new ClickListener() {

				private static final long serialVersionUID = 1L;

				public void buttonClick(ClickEvent event) {

					currentMonth--;

					if (0 > currentMonth) {

						currentMonth = 11;
						internalCalendar.add(Calendar.YEAR, -1);
					}

					internalCalendar.set(Calendar.MONTH, currentMonth);

					yearsNativeSelect.setValue(Integer.valueOf(internalCalendar.get(Calendar.YEAR)));
					monthsNativeSelect.setValue(Integer.valueOf(internalCalendar.get(Calendar.MONTH)));
				}
			});
			previousMonthButton.addStyleName(BaseTheme.BUTTON_LINK);
			previousMonthButton.setIcon(PREVIOUS_MONTH_ICON);

			headerLayout.addComponent(previousMonthButton);
			headerLayout.setComponentAlignment(previousMonthButton, Alignment.MIDDLE_CENTER);
			headerLayout.setExpandRatio(previousMonthButton, 1.0f);

			monthsNativeSelect = new NativeSelect();

			String[] monthNamesArray = dateFormatSymbols.getMonths();

			Map<String, Integer> monthNames = new HashMap<String, Integer>();

			for (int i = 0; i < monthNamesArray.length; i++) {

				String key = monthNamesArray[i];
				monthNames.put(key, Integer.valueOf(i));
			}

			for (int i = 0; i < 12; i++) {

				monthsNativeSelect.addItem(Integer.valueOf(i));

				for (Iterator<String> iterator = monthNames.keySet().iterator(); iterator.hasNext();) {

					String monthName = iterator.next();

					if (monthNames.get(monthName).equals(Integer.valueOf(i))) {

						monthsNativeSelect.setItemCaption(Integer.valueOf(i), monthName);
						break;
					}
				}
			}

			monthsNativeSelect.setImmediate(true);
			monthsNativeSelect.setNullSelectionAllowed(false);
			monthsNativeSelect.setValue(Integer.valueOf(internalCalendar.get(Calendar.MONTH)));

			monthsNativeSelect.addListener(new ValueChangeListener() {

				private static final long serialVersionUID = 1L;

				public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {

					Integer newValue = (Integer) event.getProperty().getValue();
					currentMonth = newValue.intValue();
					internalCalendar.set(Calendar.MONTH, currentMonth);
					refreshMonthDatesGridLayout();
				}
			});

			headerLayout.addComponent(monthsNativeSelect);
			headerLayout.setComponentAlignment(monthsNativeSelect, Alignment.MIDDLE_CENTER);
			headerLayout.setExpandRatio(monthsNativeSelect, 3.0f);

			yearsNativeSelect = new NativeSelect();

			for (int i = lowerYear; i < upperYear; i++) {

				yearsNativeSelect.addItem(Integer.valueOf(i));
			}

			yearsNativeSelect.setImmediate(true);
			yearsNativeSelect.setNullSelectionAllowed(false);
			yearsNativeSelect.setValue(Integer.valueOf(internalCalendar.get(Calendar.YEAR)));

			yearsNativeSelect.addListener(new ValueChangeListener() {

				private static final long serialVersionUID = 1L;

				public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {

					Integer newValue = (Integer) event.getProperty().getValue();
					internalCalendar.set(Calendar.YEAR, newValue.intValue());
					refreshMonthDatesGridLayout();
				}
			});

			headerLayout.addComponent(yearsNativeSelect);
			headerLayout.setComponentAlignment(yearsNativeSelect, Alignment.MIDDLE_CENTER);
			headerLayout.setExpandRatio(yearsNativeSelect, 2.0f);

			nextMonthButton = new Button();
			nextMonthButton.addListener(new ClickListener() {

				private static final long serialVersionUID = 1L;

				public void buttonClick(ClickEvent event) {

					currentMonth++;

					if (11 < currentMonth) {

						currentMonth = 0;
						internalCalendar.add(Calendar.YEAR, 1);
					}

					internalCalendar.set(Calendar.MONTH, currentMonth);

					yearsNativeSelect.setValue(Integer.valueOf(internalCalendar.get(Calendar.YEAR)));
					monthsNativeSelect.setValue(Integer.valueOf(internalCalendar.get(Calendar.MONTH)));
				}
			});
			nextMonthButton.addStyleName(BaseTheme.BUTTON_LINK);
			nextMonthButton.setIcon(NEXT_MONTH_ICON);

			headerLayout.addComponent(nextMonthButton);
			headerLayout.setComponentAlignment(nextMonthButton, Alignment.MIDDLE_CENTER);
			headerLayout.setExpandRatio(nextMonthButton, 1.0f);

			modalDialogLayout.addComponent(headerLayout);

			monthDatesGridLayout = new GridLayout(7, 7);
			monthDatesGridLayout.setWidth(T4JConstants.FULL_WIDTH_PERCENTAGE);

			refreshMonthDatesGridLayout();

			modalDialogLayout.addComponent(monthDatesGridLayout);

			HorizontalLayout footerLayout = new HorizontalLayout();
			footerLayout.setMargin(true, false, false, false);
			footerLayout.setSpacing(true);
			footerLayout.setWidth(T4JConstants.FULL_WIDTH_PERCENTAGE);

			todayButton = new Button(T4JWebApp.getInstance().getI18nString("T4JDateField.today"), new ClickListener() {

				private static final long serialVersionUID = 1L;

				public void buttonClick(ClickEvent event) {

					internalCalendar.setTimeInMillis(System.currentTimeMillis());

					normalizeInternalCalendar();

					currentMonth = internalCalendar.get(Calendar.MONTH);

					selectedDate = (Calendar) internalCalendar.clone();

					yearsNativeSelect.setValue(Integer.valueOf(internalCalendar.get(Calendar.YEAR)));
					monthsNativeSelect.setValue(Integer.valueOf(internalCalendar.get(Calendar.MONTH)));

					refreshMonthDatesGridLayout();
				}
			});

			todayButton.addStyleName(BaseTheme.BUTTON_LINK);
			todayButton.addStyleName("smallFont");
			todayButton.addStyleName("grey");

			footerLayout.addComponent(todayButton);
			footerLayout.setComponentAlignment(todayButton, Alignment.MIDDLE_LEFT);

			HorizontalLayout buttonLinksLayout = new HorizontalLayout();
			buttonLinksLayout.setSpacing(true);
			buttonLinksLayout.setSizeUndefined();

			resetButton = new Button(T4JWebApp.getInstance().getI18nString("button.clearSelection"), new ClickListener() {

				private static final long serialVersionUID = 1L;

				public void buttonClick(ClickEvent event) {

					resetFields();
				}
			});

			resetButton.addStyleName(BaseTheme.BUTTON_LINK);
			resetButton.addStyleName("smallFont");

			buttonLinksLayout.addComponent(resetButton);
			buttonLinksLayout.setComponentAlignment(resetButton, Alignment.MIDDLE_RIGHT);

			selectButton = new Button(T4JWebApp.getInstance().getI18nString("button.accept"), new ClickListener() {

				private static final long serialVersionUID = 1L;

				public void buttonClick(ClickEvent event) {

					updateValue();
				}
			});
			selectButton.addStyleName("t4j-button");

			buttonLinksLayout.addComponent(selectButton);
			buttonLinksLayout.setComponentAlignment(selectButton, Alignment.MIDDLE_RIGHT);

			footerLayout.addComponent(buttonLinksLayout);
			footerLayout.setComponentAlignment(buttonLinksLayout, Alignment.MIDDLE_RIGHT);

			modalDialogLayout.addComponent(footerLayout);

			layoutWrapper.addComponent(modalDialogLayout);

			layoutWrapper.addAction(new ShortcutListener(T4JConstants.EMPTY_STRING, KeyCode.ARROW_LEFT, null) {

				private static final long serialVersionUID = 1L;

				@Override
				public void handleAction(Object sender, Object target) {

					changeMonth(-1);
				}
			});

			layoutWrapper.addAction(new ShortcutListener(T4JConstants.EMPTY_STRING, KeyCode.ARROW_RIGHT, null) {

				private static final long serialVersionUID = 1L;

				@Override
				public void handleAction(Object sender, Object target) {

					changeMonth(1);
				}
			});

			layoutWrapper.addAction(new ShortcutListener(T4JConstants.EMPTY_STRING, KeyCode.ARROW_UP, null) {

				private static final long serialVersionUID = 1L;

				@Override
				public void handleAction(Object sender, Object target) {

					changeYear(-1, true);
				}
			});

			layoutWrapper.addAction(new ShortcutListener(T4JConstants.EMPTY_STRING, KeyCode.ARROW_DOWN, null) {

				private static final long serialVersionUID = 1L;

				@Override
				public void handleAction(Object sender, Object target) {

					changeYear(1, true);
				}
			});

			layoutWrapper.addAction(new ShortcutListener(T4JConstants.EMPTY_STRING, KeyCode.DELETE, null) {

				private static final long serialVersionUID = 1L;

				@Override
				public void handleAction(Object sender, Object target) {

					resetFields();
				}
			});

			layoutWrapper.addAction(new ShortcutListener(T4JConstants.EMPTY_STRING, KeyCode.ENTER, null) {

				private static final long serialVersionUID = 1L;

				@Override
				public void handleAction(Object sender, Object target) {

					updateValue();
				}
			});

			layoutWrapper.addAction(new ShortcutListener(T4JConstants.EMPTY_STRING, KeyCode.ESCAPE, null) {

				private static final long serialVersionUID = 1L;

				@Override
				public void handleAction(Object sender, Object target) {

					closeModalWindow();
				}
			});

			if (null != modalWindow) {

				try {
					closeModalWindow();
				} catch (Exception e) {
					// Ignore exception
				}
			}

			modalWindow = new Window(T4JWebApp.getInstance().getI18nString("T4JDateField.dialogTitle"));

			modalWindow.addStyleName("modal-window");

			modalWindow.setSizeUndefined();

			modalWindow.setClosable(true);
			modalWindow.setContent(layoutWrapper);

			modalWindow.setDraggable(true);
			modalWindow.setModal(true);
			modalWindow.setName("modal-window-" + System.currentTimeMillis());
			modalWindow.setResizable(false);

			T4JWebApp.getInstance().getMainWindow().addWindow(modalWindow);

			modalWindow.addListener(new CloseListener() {

				private static final long serialVersionUID = 1L;

				public void windowClose(CloseEvent e) {

					closeModalWindow();
				}
			});

			modalWindow.center();

			modalWindow.setVisible(true);

			try {
				monthsNativeSelect.focus();
			} catch (Exception e) {
				// Ignore
			}
		}
	}

	public String getDatePattern() {

		return datePattern;
	}

	public Calendar getSelectedDate() {

		return selectedDate;
	}

	@Override
	public Class<?> getType() {

		return Date.class;
	}

	public void setDatePattern(String datePattern) {

		this.datePattern = datePattern;
	}

	@Override
	public void setPropertyDataSource(Property newDataSource) {

		if (null == newDataSource || Date.class.isAssignableFrom(newDataSource.getType())) {

			if (null == newDataSource) {

				newDataSource = new ObjectProperty<Date>(null, Date.class);
			}

			PropertyFormatter propertyFormatter = new PropertyFormatter(newDataSource) {

				private static final long serialVersionUID = 1L;

				@Override
				public String format(Object value) {

					if (null == value) {
						return null;
					} else {
						try {
							return getDateFormatter().format(value);
						} catch (Exception e) {
							return null;
						}
					}
				}

				@Override
				public Class<?> getType() {

					return Date.class;
				}

				@Override
				public Object parse(String formattedValue) throws Exception {

					if (null == formattedValue) {
						return null;
					} else {
						try {
							return getDateFormatter().parse(formattedValue);
						} catch (Exception e1) {
							try {
								return new SimpleDateFormat(T4JConstants.DEFAULT_DATE_PATTERN, Locale.US).parse(formattedValue);
							} catch (Exception e2) {
								return null;
							}
						}
					}
				}
			};

			super.setPropertyDataSource(propertyFormatter);
		} else {

			throw new IllegalArgumentException("DateSelectionTextField only supports Date properties");
		}
	}

	public void setSelectedDate(Calendar selectedDate) {

		if (null == selectedDate) {

			setInternalValue(null);
		} else {

			setInternalValue(selectedDate.getTime());
		}

		this.selectedDate = selectedDate;
	}
}
