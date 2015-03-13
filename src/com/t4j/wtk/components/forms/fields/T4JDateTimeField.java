
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
import com.t4j.wtk.util.T4JLogUtils;
import com.t4j.wtk.util.T4JStringUtils;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertyFormatter;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
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
public class T4JDateTimeField extends TextField implements FocusListener {

	private static final long serialVersionUID = 1L;

	private static final Log logger = T4JLogUtils.getLogger(T4JDateTimeField.class);

	private static final ThemeResource NEXT_MONTH_ICON = new ThemeResource("img/paginatorNext.gif");

	private static final ThemeResource PREVIOUS_MONTH_ICON = new ThemeResource("img/paginatorPrevious.gif");

	private int currentMonth;

	private Boolean isDateSelectionDisabled;

	private String dateTimePattern;

	private Calendar internalCalendar;

	private Calendar selectedDate;

	private GridLayout monthDatesGridLayout;

	private ArrayList<ClickListener> selectButtonClickListeners;

	private Button nextMonthButton;

	private Button previousMonthButton;

	private Button todayButton;

	private Button selectButton;

	private Button resetButton;

	private NativeSelect monthsNativeSelect;

	private NativeSelect yearsNativeSelect;

	private NativeSelect hoursNativeSelect;

	private NativeSelect minutesNativeSelect;

	private NativeSelect secondsNativeSelect;

	private Window modalDialog;

	private Locale locale;

	private DateFormatSymbols dateFormatSymbols;

	public T4JDateTimeField() {

		super();

		setupComponent();
	}

	public T4JDateTimeField(Property dataSource) {

		super(dataSource);

		setupComponent();
	}

	public T4JDateTimeField(String caption) {

		super(caption);

		setupComponent();
	}

	public T4JDateTimeField(String caption, Date value) {

		super(caption);

		setValue(value);

		setupComponent();
	}

	public T4JDateTimeField(String caption, Property dataSource) {

		super(caption, dataSource);

		setupComponent();
	}

	private void closeModalDialog() {

		T4JWebApp.getInstance().getMainWindow().removeWindow(modalDialog);
		modalDialog = null;
	}

	private boolean isSelectedDate(Calendar tmpCalendar, Calendar selectedDate) {

		boolean methodResult = false;

		try {
			if (tmpCalendar.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) //
				&& tmpCalendar.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) //
				&& tmpCalendar.get(Calendar.DAY_OF_MONTH) == selectedDate.get(Calendar.DAY_OF_MONTH)) {

				methodResult = true;
			}
		} catch (Exception e) {
			// Ignore
		}

		return methodResult;
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

			tmpCalendar.add(Calendar.DAY_OF_MONTH, -1 * dayOfWeek);

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
									selectedDate = Calendar.getInstance();
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

						if (isSelectedDate(tmpCalendar, selectedDate)) {
							dayButton.addStyleName("day-button-selected");
						}

						dayButton.setWidth("42px");

						dayButton.setEnabled(false == Boolean.TRUE.equals(isDateSelectionDisabled));

						monthDatesGridLayout.addComponent(dayButton, j, i);
						monthDatesGridLayout.setComponentAlignment(dayButton, Alignment.MIDDLE_CENTER);

						tmpCalendar.add(Calendar.DATE, 1);
					}
				}
			}
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

					return getDateTimeFormatter().format(tmpValue);
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

	protected void setupComponent() {

		addListener((FocusListener) this);
		addStyleName("t4j-popup-textfield");
		addStyleName("t4j-datetime-field");
		setImmediate(true);
		setNullRepresentation(T4JConstants.EMPTY_STRING);

		locale = T4JWebApp.getInstance().getLocale();

		dateFormatSymbols = new DateFormatSymbols(locale);

		internalCalendar = Calendar.getInstance(locale);
		internalCalendar.set(Calendar.HOUR_OF_DAY, 0);
		internalCalendar.set(Calendar.MINUTE, 0);
		internalCalendar.set(Calendar.SECOND, 0);
		internalCalendar.set(Calendar.MILLISECOND, 0);
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
					tmpDate = getDateTimeFormatter().parse((String) tmpObject);
				}

			} catch (Exception e) {

				// Ignore
			}

			if (null == tmpDate) {

				tmpDate = new Date();
			}

			internalCalendar.setTime(tmpDate);
			currentMonth = internalCalendar.get(Calendar.MONTH);
			selectedDate = (Calendar) internalCalendar.clone();

			int currentYear = internalCalendar.get(Calendar.YEAR);

			int firstYear = currentYear - 100;
			int lastYear = currentYear + 101;

			VerticalLayout dialogLayout = new VerticalLayout();
			dialogLayout.addStyleName("date-selection-dialog");
			dialogLayout.setMargin(true);
			dialogLayout.setSpacing(true);
			dialogLayout.setWidth(T4JWebApp.getInstance().getSizeInEMs(340));

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

			for (int i = firstYear; i < lastYear; i++) {

				yearsNativeSelect.addItem(Integer.valueOf(i));
			}

			yearsNativeSelect.setImmediate(true);
			yearsNativeSelect.setNullSelectionAllowed(false);
			yearsNativeSelect.setValue(Integer.valueOf(internalCalendar.get(Calendar.YEAR)));

			yearsNativeSelect.addListener(new ValueChangeListener() {

				private static final long serialVersionUID = 1L;

				public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {

					Integer tmpInteger = (Integer) event.getProperty().getValue();
					internalCalendar.set(Calendar.YEAR, tmpInteger.intValue());
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

			dialogLayout.addComponent(headerLayout);

			monthDatesGridLayout = new GridLayout(7, 7);
			monthDatesGridLayout.setWidth(T4JConstants.FULL_WIDTH_PERCENTAGE);

			refreshMonthDatesGridLayout();

			dialogLayout.addComponent(monthDatesGridLayout);

			HorizontalLayout timeFieldsLayout = new HorizontalLayout();

			timeFieldsLayout.setMargin(true, false, false, false);
			timeFieldsLayout.setSizeUndefined();
			timeFieldsLayout.setSpacing(true);

			Label timeLabel = new Label(T4JWebApp.getInstance().getI18nString("T4JDateTimeField.hour"));
			timeLabel.addStyleName("time-label");
			timeFieldsLayout.addComponent(timeLabel);
			timeFieldsLayout.setComponentAlignment(timeLabel, Alignment.MIDDLE_LEFT);

			hoursNativeSelect = new NativeSelect();
			hoursNativeSelect.setImmediate(true);

			hoursNativeSelect.setNullSelectionAllowed(false);

			for (int i = 0; i < 24; i++) {
				hoursNativeSelect.addItem(Integer.valueOf(i));
				if (i < 10) {
					hoursNativeSelect.setItemCaption(Integer.valueOf(i), "0" + Integer.valueOf(i) + " h");
				} else {
					hoursNativeSelect.setItemCaption(Integer.valueOf(i), Integer.valueOf(i) + " h");
				}
			}

			try {
				hoursNativeSelect.setValue(Integer.valueOf(internalCalendar.get(Calendar.HOUR_OF_DAY)));
			} catch (Exception e) {
				hoursNativeSelect.setValue(Integer.valueOf(0));
			}

			hoursNativeSelect.addListener(new ValueChangeListener() {

				private static final long serialVersionUID = 1L;

				public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {

					Integer tmpInteger = (Integer) event.getProperty().getValue();
					internalCalendar.set(Calendar.HOUR_OF_DAY, tmpInteger.intValue());
					selectedDate.set(Calendar.HOUR_OF_DAY, tmpInteger.intValue());
				}
			});

			timeFieldsLayout.addComponent(hoursNativeSelect);

			minutesNativeSelect = new NativeSelect();
			minutesNativeSelect.setImmediate(true);

			minutesNativeSelect.setNullSelectionAllowed(false);

			for (int i = 0; i < 60; i++) {
				minutesNativeSelect.addItem(Integer.valueOf(i));
				if (i < 10) {
					minutesNativeSelect.setItemCaption(Integer.valueOf(i), "0" + Integer.valueOf(i) + " m");
				} else {
					minutesNativeSelect.setItemCaption(Integer.valueOf(i), Integer.valueOf(i) + " m");
				}
			}

			try {
				minutesNativeSelect.setValue(Integer.valueOf(internalCalendar.get(Calendar.MINUTE)));
			} catch (Exception e) {
				minutesNativeSelect.setValue(Integer.valueOf(0));
			}

			minutesNativeSelect.addListener(new ValueChangeListener() {

				private static final long serialVersionUID = 1L;

				public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {

					Integer tmpInteger = (Integer) event.getProperty().getValue();
					internalCalendar.set(Calendar.MINUTE, tmpInteger.intValue());
					selectedDate.set(Calendar.MINUTE, tmpInteger.intValue());
				}
			});

			timeFieldsLayout.addComponent(minutesNativeSelect);

			secondsNativeSelect = new NativeSelect();
			secondsNativeSelect.setImmediate(true);

			secondsNativeSelect.setNullSelectionAllowed(false);

			for (int i = 0; i < 60; i++) {
				secondsNativeSelect.addItem(Integer.valueOf(i));
				if (i < 10) {
					secondsNativeSelect.setItemCaption(Integer.valueOf(i), "0" + Integer.valueOf(i) + " s");
				} else {
					secondsNativeSelect.setItemCaption(Integer.valueOf(i), Integer.valueOf(i) + " s");
				}
			}

			try {
				secondsNativeSelect.setValue(Integer.valueOf(internalCalendar.get(Calendar.SECOND)));
			} catch (Exception e) {
				secondsNativeSelect.setValue(Integer.valueOf(0));
			}

			secondsNativeSelect.addListener(new ValueChangeListener() {

				private static final long serialVersionUID = 1L;

				public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {

					Integer tmpInteger = (Integer) event.getProperty().getValue();
					internalCalendar.set(Calendar.SECOND, tmpInteger.intValue());
					selectedDate.set(Calendar.SECOND, tmpInteger.intValue());
				}
			});

			timeFieldsLayout.addComponent(secondsNativeSelect);

			dialogLayout.addComponent(timeFieldsLayout);
			dialogLayout.setComponentAlignment(timeFieldsLayout, Alignment.MIDDLE_LEFT);

			HorizontalLayout footerLayout = new HorizontalLayout();
			footerLayout.setMargin(true, false, false, false);
			footerLayout.setSpacing(true);
			footerLayout.setWidth(T4JConstants.FULL_WIDTH_PERCENTAGE);

			todayButton = new Button(T4JWebApp.getInstance().getI18nString("T4JDateTimeField.today"), new ClickListener() {

				private static final long serialVersionUID = 1L;

				public void buttonClick(ClickEvent event) {

					Date currentDate = new Date();

					internalCalendar.setTime(currentDate);

					currentMonth = internalCalendar.get(Calendar.MONTH);

					selectedDate = (Calendar) internalCalendar.clone();

					yearsNativeSelect.setValue(Integer.valueOf(internalCalendar.get(Calendar.YEAR)));
					monthsNativeSelect.setValue(Integer.valueOf(internalCalendar.get(Calendar.MONTH)));

					hoursNativeSelect.setValue(Integer.valueOf(internalCalendar.get(Calendar.HOUR_OF_DAY)));
					minutesNativeSelect.setValue(Integer.valueOf(internalCalendar.get(Calendar.MINUTE)));
					secondsNativeSelect.setValue(Integer.valueOf(internalCalendar.get(Calendar.SECOND)));

					refreshMonthDatesGridLayout();

				}
			});

			todayButton.addStyleName(BaseTheme.BUTTON_LINK);
			todayButton.addStyleName("grey-link-button");

			todayButton.setEnabled(false == Boolean.TRUE.equals(isDateSelectionDisabled));

			footerLayout.addComponent(todayButton);
			footerLayout.setExpandRatio(todayButton, 0.0f);
			footerLayout.setComponentAlignment(todayButton, Alignment.MIDDLE_LEFT);

			HorizontalLayout buttonLinksLayout = new HorizontalLayout();
			buttonLinksLayout.setSpacing(true);
			buttonLinksLayout.setSizeUndefined();

			resetButton = new Button(T4JWebApp.getInstance().getI18nString("T4JDateTimeField.delete"), new ClickListener() {

				private static final long serialVersionUID = 1L;

				public void buttonClick(ClickEvent event) {

					selectedDate = null;
					refreshMonthDatesGridLayout();
				}
			});

			resetButton.addStyleName(BaseTheme.BUTTON_LINK);
			resetButton.addStyleName("grey-link-button");

			buttonLinksLayout.addComponent(resetButton);
			buttonLinksLayout.setComponentAlignment(resetButton, Alignment.MIDDLE_RIGHT);

			selectButton = new Button("Aceptar", new ClickListener() {

				private static final long serialVersionUID = 1L;

				public void buttonClick(ClickEvent event) {

					if (null == selectedDate) {

						setValue(null);
					} else {

						setValue(selectedDate.getTime());
					}

					closeModalDialog();
				}
			});
			selectButton.addStyleName("adq-button");

			buttonLinksLayout.addComponent(selectButton);
			buttonLinksLayout.setComponentAlignment(selectButton, Alignment.MIDDLE_RIGHT);

			footerLayout.addComponent(buttonLinksLayout);
			footerLayout.setExpandRatio(buttonLinksLayout, 1.0f);
			footerLayout.setComponentAlignment(buttonLinksLayout, Alignment.MIDDLE_RIGHT);

			dialogLayout.addComponent(footerLayout);

			if (null != modalDialog) {

				try {
					closeModalDialog();
				} catch (Exception e) {
					// Ignore exception
				}
			}

			modalDialog = new Window(T4JWebApp.getInstance().getI18nString("T4JDateTimeField.dialogTitle"));

			modalDialog.addStyleName("modal-window");

			modalDialog.setSizeUndefined();

			modalDialog.setClosable(true);
			modalDialog.setContent(dialogLayout);

			modalDialog.setDraggable(true);
			modalDialog.setModal(true);
			modalDialog.setName("modal-window-" + System.currentTimeMillis());
			modalDialog.setResizable(false);

			T4JWebApp.getInstance().getMainWindow().addWindow(modalDialog);

			modalDialog.addListener(new CloseListener() {

				private static final long serialVersionUID = 1L;

				public void windowClose(CloseEvent e) {

					closeModalDialog();
				}
			});

			modalDialog.center();
			modalDialog.setVisible(true);

		}
	}

	public DateFormat getDateTimeFormatter() {

		if (T4JStringUtils.isNullOrEmpty(dateTimePattern)) {
			return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, T4JWebApp.getInstance().getLocale());
		} else {
			return new SimpleDateFormat(dateTimePattern, T4JWebApp.getInstance().getLocale());
		}
	}

	public Boolean getIsDateSelectionDisabled() {

		return isDateSelectionDisabled;
	}

	public Calendar getSelectedDate() {

		return selectedDate;
	}

	@Override
	public Class<?> getType() {

		return Date.class;
	}

	public void setDateTimePattern(String dateTimePattern) {

		this.dateTimePattern = dateTimePattern;
	}

	public void setIsDateSelectionDisabled(Boolean isDateSelectionDisabled) {

		this.isDateSelectionDisabled = isDateSelectionDisabled;
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
							return getDateTimeFormatter().format(value);
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

					if (T4JStringUtils.isNullOrEmpty(formattedValue)) {
						return null;
					} else {
						try {
							return getDateTimeFormatter().parse(formattedValue);
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

			throw new IllegalArgumentException("DateSelectionTextField only supports java.util.Date properties");
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
