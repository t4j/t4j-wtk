
package com.t4j.wtk.components.forms.fields;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.t4j.wtk.T4JConstants;
import com.t4j.wtk.application.T4JWebApp;
import com.t4j.wtk.beans.dates.T4JDateArray;
import com.t4j.wtk.util.T4JCollectionUtils;
import com.t4j.wtk.util.T4JDateUtils;
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
import com.vaadin.ui.TextArea;
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
public class T4JMultipleDateField extends TextArea implements FocusListener {

	private static final long serialVersionUID = 1L;

	private static final Log logger = T4JLogUtils.getLogger(T4JMultipleDateField.class);

	private static final ThemeResource NEXT_MONTH_ICON = new ThemeResource("img/paginatorNext.gif");

	private static final ThemeResource PREVIOUS_MONTH_ICON = new ThemeResource("img/paginatorPrevious.gif");

	private int currentMonth = -1;

	private String datePattern;

	private Locale locale;

	private Button selectButton;

	private Button resetButton;

	private Button nextMonthButton;

	private Button previousMonthButton;

	private Button todayButton;

	private GridLayout monthDatesGridLayout;

	private Calendar internalCalendar;

	private HashSet<Date> selectedDates;

	private Window modalWindow;

	private ArrayList<ClickListener> selectButtonClickListeners;

	private NativeSelect monthsNativeSelect;

	private NativeSelect yearsNativeSelect;

	private DateFormatSymbols dateFormatSymbols;

	public T4JMultipleDateField() {

		super();

		setupField();
	}

	public T4JMultipleDateField(Property dataSource) {

		super(dataSource);

		setupField();
	}

	public T4JMultipleDateField(String caption) {

		super(caption);

		setupField();
	}

	public T4JMultipleDateField(String caption, Property dataSource) {

		super(caption, dataSource);

		setupField();
	}

	public T4JMultipleDateField(String caption, T4JDateArray value) {

		super(caption);

		setValue(value);

		setupField();
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

		boolean methodResult = false;

		for (Iterator<Date> iterator = selectedDates.iterator(); iterator.hasNext();) {

			Date tmpDate = iterator.next();

			if (0 == T4JDateUtils.compareNormalizedDates(tmpDate, c.getTime())) {

				methodResult = true;
				break;
			}
		}

		return methodResult;

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

								Date tmpDate = new Date(timeInMillis);

								if (selectedDates.contains(tmpDate)) {

									selectedDates.remove(tmpDate);
									event.getButton().removeStyleName("day-button-selected");
								} else {

									selectedDates.add(tmpDate);
									event.getButton().addStyleName("day-button-selected");
								}
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

	private void updateFieldValue() {

		if (T4JCollectionUtils.isNullOrEmpty(selectedDates)) {

			setValue(null);
		} else {

			List<Date> tmpList = new ArrayList<Date>(selectedDates);

			Collections.sort(tmpList);

			Date[] tmpArray = new Date[tmpList.size()];

			tmpArray = tmpList.toArray(tmpArray);

			T4JDateArray dateArray = new T4JDateArray(tmpArray);

			setValue(dateArray);
		}
	}

	protected DateFormat getDateFormatter() {

		if (T4JStringUtils.isNullOrEmpty(datePattern)) {

			return DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
		} else {

			try {

				return new SimpleDateFormat(datePattern, locale);
			} catch (Exception e) {

				return DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
			}
		}
	}

	@Override
	protected String getFormattedValue() {

		Object tmpValue = getValue();

		if (null == tmpValue) {

			return T4JConstants.EMPTY_STRING;
		} else {

			if (tmpValue instanceof T4JDateArray) {

				T4JDateArray t4jDateArray = (T4JDateArray) tmpValue;
				t4jDateArray.setDatePattern(getDatePattern());
				t4jDateArray.setLocale(locale);
				return t4jDateArray.toString();
			} else if (tmpValue instanceof String) {

				try {

					String[] tmpStringArray = tmpValue.toString().split("\\r\\n");

					SimpleDateFormat defaultDateFormatter = new SimpleDateFormat(T4JConstants.DEFAULT_DATE_PATTERN, Locale.US);

					List<Date> tmpList = new ArrayList<Date>();

					for (int i = 0; i < tmpStringArray.length; i++) {

						String tmpString = tmpStringArray[i];

						if (T4JStringUtils.isNullOrEmpty(tmpString)) {

							continue;
						} else {

							try {
								tmpList.add(getDateFormatter().parse(tmpString));
							} catch (Exception e1) {
								try {
									tmpList.add(defaultDateFormatter.parse(tmpStringArray[i]));
								} catch (Exception e2) {
									// Ignore
								}
							}
						}
					}

					Collections.sort(tmpList);

					StringBuffer stringBuffer = new StringBuffer(1024);

					for (Iterator<Date> iterator = tmpList.iterator(); iterator.hasNext();) {

						Date tmpDate = iterator.next();

						if (null == tmpDate) {

							continue;
						} else {

							if (0 < stringBuffer.length()) {

								stringBuffer.append("\r\n");
							}

							try {

								stringBuffer.append(getDateFormatter().format(tmpDate));
							} catch (Exception e) {

								stringBuffer.append(defaultDateFormatter.format(tmpDate));
							}
						}
					}

					return stringBuffer.toString();
				} catch (Exception e) {

					return tmpValue.toString();
				}
			} else {

				return T4JConstants.EMPTY_STRING;
			}

		}
	}

	protected void setupField() {

		addListener((FocusListener) this);

		addStyleName("t4j-popup-textfield");

		setImmediate(true);

		setNullRepresentation(T4JConstants.EMPTY_STRING);

		locale = T4JWebApp.getInstance().getLocale();

		dateFormatSymbols = new DateFormatSymbols(locale);

		internalCalendar = Calendar.getInstance(locale);

		normalizeInternalCalendar();

		selectedDates = new HashSet<Date>();

	}

	public void addSelectButtonClickListener(ClickListener clickListener) {

		if (null == selectButtonClickListeners) {
			selectButtonClickListeners = new ArrayList<ClickListener>();
		}

		selectButtonClickListeners.add(clickListener);
	}

	public void focus(FocusEvent event) {

		if (false == isReadOnly()) {

			Date[] dates = null;

			try {

				Object currentValue = getValue();

				if (null == currentValue) {

					dates = new Date[0];
				} else {

					if (currentValue instanceof T4JDateArray) {

						T4JDateArray tmpBean = (T4JDateArray) currentValue;

						dates = tmpBean.getDateArray();

						if (T4JCollectionUtils.isNullOrEmpty(dates)) {

							dates = new Date[0];
						} else {

							List<Date> tmpList = new ArrayList<Date>();

							for (int i = 0; i < dates.length; i++) {

								Date tmpDate = dates[i];

								if (null == tmpDate) {

									continue;
								} else {

									tmpDate = getNormalizedDate(tmpDate);

									if (false == tmpList.contains(tmpDate)) {

										tmpList.add(tmpDate);
									}
								}
							}

							if (T4JCollectionUtils.isNullOrEmpty(tmpList)) {

								dates = new Date[0];
							} else {

								Collections.sort(tmpList);
								dates = new Date[tmpList.size()];
								dates = tmpList.toArray(dates);
							}
						}
					} else {

						String tmpString = (String) currentValue;

						if (T4JStringUtils.isNullOrEmpty(tmpString)) {

							dates = new Date[0];
						} else {

							String[] tmpStringArray = tmpString.split("\\r\\n");

							SimpleDateFormat defaultFormatter = new SimpleDateFormat(T4JConstants.DEFAULT_DATE_PATTERN, Locale.US);

							List<Date> tmpList = new ArrayList<Date>();

							for (int i = 0; i < tmpStringArray.length; i++) {

								String tmpDateString = tmpStringArray[i];

								if (T4JStringUtils.isNullOrEmpty(tmpDateString)) {

									continue;
								} else {

									Date tmpDate = null;

									try {
										tmpDate = getDateFormatter().parse(tmpDateString);
									} catch (Exception e1) {
										try {
											tmpDate = defaultFormatter.parse(tmpDateString);
										} catch (Exception e2) {
											// Ignore
										}
									}

									if (null == tmpDate) {

										continue;
									} else {

										tmpDate = getNormalizedDate(tmpDate);

										if (false == tmpList.contains(tmpDate)) {

											tmpList.add(tmpDate);
										}
									}
								}
							}

							if (T4JCollectionUtils.isNullOrEmpty(tmpList)) {

								dates = new Date[0];
							} else {

								Collections.sort(tmpList);
								dates = new Date[tmpList.size()];
								dates = tmpList.toArray(dates);
							}
						}
					}
				}
			} catch (Exception e) {

				dates = new Date[0];
			}

			Date lastSelectedDate = null;

			if (T4JCollectionUtils.isNullOrEmpty(dates)) {

				lastSelectedDate = getNormalizedDate(Calendar.getInstance(locale).getTime());
			} else {

				for (int i = 0; i < dates.length; i++) {

					lastSelectedDate = dates[i];
				}
			}

			internalCalendar.setTime(lastSelectedDate);

			currentMonth = internalCalendar.get(Calendar.MONTH);

			if (T4JCollectionUtils.isNullOrEmpty(selectedDates)) {

				selectedDates.clear();

				for (int i = 0; i < dates.length; i++) {
					selectedDates.add(dates[i]);
				}
			}

			int currentYear = internalCalendar.get(Calendar.YEAR);

			int firstYear = currentYear - 100;
			int lastYear = currentYear + 101;

			VerticalLayout modalDialogLayout = new VerticalLayout();
			modalDialogLayout.addStyleName("date-selection-dialog");
			modalDialogLayout.setMargin(true);
			modalDialogLayout.setSpacing(true);
			modalDialogLayout.setWidth(T4JWebApp.getInstance().getSizeInEMs(340));

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

			DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(locale);

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

			modalDialogLayout.addComponent(headerLayout);

			monthDatesGridLayout = new GridLayout(7, 7);
			monthDatesGridLayout.setWidth(T4JConstants.FULL_WIDTH_PERCENTAGE);

			refreshMonthDatesGridLayout();

			modalDialogLayout.addComponent(monthDatesGridLayout);

			HorizontalLayout footerLayout = new HorizontalLayout();
			footerLayout.setMargin(true, false, false, false);
			footerLayout.setSpacing(true);
			footerLayout.setWidth(T4JConstants.FULL_WIDTH_PERCENTAGE);

			todayButton = new Button(T4JWebApp.getInstance().getI18nString("T4JMultipleDateField.today"), new ClickListener() {

				private static final long serialVersionUID = 1L;

				public void buttonClick(ClickEvent event) {

					internalCalendar.setTimeInMillis(System.currentTimeMillis());

					normalizeInternalCalendar();

					currentMonth = internalCalendar.get(Calendar.MONTH);

					yearsNativeSelect.setValue(Integer.valueOf(internalCalendar.get(Calendar.YEAR)));
					monthsNativeSelect.setValue(Integer.valueOf(internalCalendar.get(Calendar.MONTH)));

					selectedDates.clear();
					selectedDates.add(internalCalendar.getTime());

					refreshMonthDatesGridLayout();
				}
			});

			todayButton.addStyleName(BaseTheme.BUTTON_LINK);
			todayButton.addStyleName("grey-link-button");

			footerLayout.addComponent(todayButton);
			footerLayout.setComponentAlignment(todayButton, Alignment.MIDDLE_LEFT);

			HorizontalLayout buttonLinksLayout = new HorizontalLayout();
			buttonLinksLayout.setSpacing(true);
			buttonLinksLayout.setSizeUndefined();

			resetButton = new Button(T4JWebApp.getInstance().getI18nString("button.clearSelection"), new ClickListener() {

				private static final long serialVersionUID = 1L;

				public void buttonClick(ClickEvent event) {

					selectedDates.clear();

					refreshMonthDatesGridLayout();
				}
			});

			resetButton.addStyleName(BaseTheme.BUTTON_LINK);
			resetButton.addStyleName("grey-link-button");

			buttonLinksLayout.addComponent(resetButton);
			buttonLinksLayout.setComponentAlignment(resetButton, Alignment.MIDDLE_RIGHT);

			selectButton = new Button(T4JWebApp.getInstance().getI18nString("button.accept"), new ClickListener() {

				private static final long serialVersionUID = 1L;

				public void buttonClick(ClickEvent event) {

					updateFieldValue();
					closeModalWindow();
				}
			});
			selectButton.addStyleName("t4j-button");

			buttonLinksLayout.addComponent(selectButton);
			buttonLinksLayout.setComponentAlignment(selectButton, Alignment.MIDDLE_RIGHT);

			footerLayout.addComponent(buttonLinksLayout);
			footerLayout.setComponentAlignment(buttonLinksLayout, Alignment.MIDDLE_RIGHT);

			modalDialogLayout.addComponent(footerLayout);

			if (null != modalWindow) {

				try {
					closeModalWindow();
				} catch (Exception e) {
					// Ignore exception
				}
			}

			modalWindow = new Window(T4JWebApp.getInstance().getI18nString("T4JMultipleDateField.dialogTitle"));

			modalWindow.addStyleName("modal-window");

			modalWindow.setSizeUndefined();

			modalWindow.setClosable(true);
			modalWindow.setContent(modalDialogLayout);

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

		}
	}

	public String getDatePattern() {

		return datePattern;
	}

	public Set<Date> getSelectedDates() {

		return selectedDates;
	}

	@Override
	public Class<?> getType() {

		return T4JDateArray.class;
	}

	public void setDatePattern(String datePattern) {

		this.datePattern = datePattern;
	}

	@Override
	public void setPropertyDataSource(Property newDataSource) {

		if (null == newDataSource || getType().isAssignableFrom(newDataSource.getType())) {

			if (null == newDataSource) {

				newDataSource = new ObjectProperty<T4JDateArray>(null, T4JDateArray.class);
			}

			PropertyFormatter propertyFormatter = new PropertyFormatter(newDataSource) {

				private static final long serialVersionUID = 1L;

				@Override
				public String format(Object value) {

					if (null == value) {

						return T4JConstants.EMPTY_STRING;
					} else {
						try {

							T4JDateArray dateArray = (T4JDateArray) value;

							Date[] tmpArray = dateArray.getDateArray();

							if (null == tmpArray) {

								return T4JConstants.EMPTY_STRING;
							} else {

								StringBuffer stringBuffer = new StringBuffer(512);

								SimpleDateFormat defaultFormatter = new SimpleDateFormat(T4JConstants.DEFAULT_DATE_PATTERN, Locale.US);

								for (int i = 0; i < tmpArray.length; i++) {

									Date tmpDate = tmpArray[i];

									if (null == tmpDate) {

										continue;
									} else {

										if (0 < stringBuffer.length()) {
											stringBuffer.append("\r\n");
										}

										try {
											stringBuffer.append(getDateFormatter().format(tmpDate));
										} catch (Exception e) {
											stringBuffer.append(defaultFormatter.format(tmpDate));
										}
									}

								}

								return stringBuffer.toString();
							}
						} catch (Exception e) {

							return T4JConstants.EMPTY_STRING;
						}
					}
				}

				@Override
				public Class<?> getType() {

					return T4JDateArray.class;
				}

				@Override
				public Object parse(String formattedValue) throws Exception {

					if (T4JStringUtils.isNullOrEmpty(formattedValue)) {

						return new T4JDateArray();
					} else {

						try {

							String[] tmpStringArray = formattedValue.split("\\r\\n");

							List<Date> tmpList = new ArrayList<Date>();

							SimpleDateFormat defaultFormatter = new SimpleDateFormat(T4JConstants.DEFAULT_DATE_PATTERN, Locale.US);

							for (int i = 0; i < tmpStringArray.length; i++) {

								String tmpDateString = tmpStringArray[i];

								if (T4JStringUtils.isNullOrEmpty(tmpDateString)) {

									continue;
								} else {

									try {
										tmpList.add(getDateFormatter().parse(tmpDateString));
									} catch (Exception e) {
										tmpList.add(defaultFormatter.parse(tmpDateString));
									}
								}
							}

							Collections.sort(tmpList);

							Date[] tmpDateArray = new Date[tmpList.size()];

							tmpDateArray = tmpList.toArray(tmpDateArray);

							return new T4JDateArray(tmpDateArray);
						} catch (Exception e1) {

							return new T4JDateArray();
						}
					}
				}
			};

			super.setPropertyDataSource(propertyFormatter);
		} else {

			throw new IllegalArgumentException("DateSelectionTextField only supports Date properties");
		}
	}

	public void setSelectedDates(HashSet<Date> selectedDates) {

		this.selectedDates = selectedDates;
	}
}
