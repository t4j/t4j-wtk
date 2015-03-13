
package com.t4j.wtk.components.tables.plain;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.t4j.wtk.T4JConstants;
import com.t4j.wtk.application.T4JWebApp;
import com.t4j.wtk.components.tables.containers.plain.T4JTableContainerItem;
import com.t4j.wtk.util.T4JLogUtils;
import com.t4j.wtk.util.T4JStringUtils;

public abstract class T4JTableSqlExportWorker implements Runnable, Serializable {

	private static final long serialVersionUID = 1L;

	private static final Log logger = T4JLogUtils.getLogger(T4JTableSqlExportWorker.class);

	protected int currentTableContainerFirstRowIndex;

	protected int currentTableContainerItemsCount;

	protected int excelSheetRowIndex;

	protected int firstRowIndex;

	protected int lastRowIndex;

	protected int processedItemsCount;

	protected int cellIndex;

	protected int rowIndex;

	protected int sheetIndex;

	protected int totalItemsCount;

	protected Boolean isInterrupted;

	protected Integer fromPage;

	protected Integer toPage;

	protected Exception threadException;

	protected transient Workbook workbook;

	protected transient Sheet currentSheet;

	protected transient Row currentRow;

	protected transient Cell currentCell;

	protected transient CreationHelper creationHelper;

	protected transient CellStyle dateCellStyle;

	protected T4JTableComponent tableComponent;

	protected String destinationFilePath;

	protected File destinationFile;

	protected transient FileOutputStream destinationFileOutputStream = null;

	public T4JTableSqlExportWorker() {

		this(null, null, null);
	}

	public T4JTableSqlExportWorker(T4JTableComponent tableComponent) {

		this(tableComponent, null, null);
	}

	public T4JTableSqlExportWorker(T4JTableComponent tableComponent, Integer fromPage, Integer toPage) {

		super();

		this.tableComponent = tableComponent;
		this.fromPage = fromPage;
		this.toPage = toPage;
	}

	protected void addHeaderRow() {

		excelSheetRowIndex = 0;

		cellIndex = 0;

		currentSheet = workbook.createSheet(((T4JWebApp) tableComponent.getApplication()).getI18nString("T4JTableExcelExportWorker.sheet") + T4JConstants.WHITESPACE_STRING + sheetIndex);

		sheetIndex++;

		currentRow = currentSheet.createRow(excelSheetRowIndex);

		currentCell = currentRow.createCell(cellIndex, Cell.CELL_TYPE_STRING);

		currentCell.setCellValue(creationHelper.createRichTextString("INSERT"));

		cellIndex++;

		currentCell = currentRow.createCell(cellIndex, Cell.CELL_TYPE_STRING);

		currentCell.setCellValue(creationHelper.createRichTextString("UPDATE"));

		cellIndex++;

		excelSheetRowIndex = 2;

	}

	protected String generateDestinationFilePath() {

		return System.getProperty("user.home") + "/tmp_" + System.currentTimeMillis() + ".xls";
	}

	protected void generateExcelFile() throws Exception {

		destinationFileOutputStream = new FileOutputStream(destinationFile);

		workbook.write(destinationFileOutputStream);
	}

	protected abstract String generateItemInsertSql(T4JTableContainerItem<?> item);

	protected abstract String generateItemUpdateSql(T4JTableContainerItem<?> item);

	protected T4JTableComponent getTableComponent() {

		return tableComponent;
	}

	protected void initializeResources() throws Exception {

		isInterrupted = Boolean.FALSE;

		if (null == fromPage || Integer.valueOf(1).compareTo(fromPage) > 0) {
			fromPage = Integer.valueOf(1);
		}

		if (null == toPage || Integer.valueOf(1).compareTo(toPage) > 0) {
			toPage = Integer.valueOf(1);
		}

		if (toPage.compareTo(tableComponent.getPagesCount()) > 0) {
			toPage = tableComponent.getPagesCount();
		}

		if (fromPage.compareTo(toPage) > 0) {
			fromPage = toPage;
		}

		currentSheet = null;
		currentRow = null;
		currentCell = null;

		creationHelper = null;
		dateCellStyle = null;

		sheetIndex = 1;
		rowIndex = 0;

		threadException = null;

		destinationFilePath = generateDestinationFilePath();

		if (T4JStringUtils.isNullOrEmpty(destinationFilePath)) {

			destinationFilePath = System.getProperty("user.home") + "/tmp_" + System.currentTimeMillis() + ".xls";
		}

		destinationFilePath = destinationFilePath.replace("\\", "/");

		destinationFile = new File(destinationFilePath);

		destinationFile.createNewFile();

		workbook = new HSSFWorkbook();

		creationHelper = workbook.getCreationHelper();

		dateCellStyle = workbook.createCellStyle();
		dateCellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm"));

		currentTableContainerFirstRowIndex = tableComponent.getContainer().getFirstItemIndex().intValue();

		firstRowIndex = (fromPage.intValue() - 1) * tableComponent.getItemsPerPage().intValue();

		lastRowIndex = toPage.intValue() * tableComponent.getItemsPerPage().intValue();

		tableComponent.getContainer().setFirstItemIndexForExcel(Integer.valueOf(firstRowIndex));

		processedItemsCount = 0;

		totalItemsCount = lastRowIndex - firstRowIndex;

		currentTableContainerItemsCount = tableComponent.getContainer().getItemsCount().intValue();
	}

	protected void markAsInterrupted() {

		isInterrupted = Boolean.TRUE;
	}

	protected void performPostIterationTasks() {

	}

	protected void performPreIterationTasks() {

	}

	protected void processItem(int itemIndex) {

		Object itemId = tableComponent.getContainer().getIdByIndexForExcelExport(itemIndex);

		if (null == itemId) {

			return;
		} else {

			T4JTableContainerItem<?> item = (T4JTableContainerItem<?>) tableComponent.getContainer().getItem(itemId);

			String insertSqlString = generateItemInsertSql(item);
			String updateSqlString = generateItemUpdateSql(item);

			if (false == T4JStringUtils.isNullOrEmpty(insertSqlString) || false == T4JStringUtils.isNullOrEmpty(updateSqlString)) {

				currentRow = currentSheet.createRow(excelSheetRowIndex);

				cellIndex = 0;

				if (false == T4JStringUtils.isNullOrEmpty(insertSqlString)) {

					currentCell = currentRow.createCell(0, Cell.CELL_TYPE_STRING);
					currentCell.setCellValue(creationHelper.createRichTextString(insertSqlString));
				}

				if (false == T4JStringUtils.isNullOrEmpty(updateSqlString)) {

					currentCell = currentRow.createCell(1, Cell.CELL_TYPE_STRING);
					currentCell.setCellValue(creationHelper.createRichTextString(updateSqlString));
				}

				excelSheetRowIndex++;
			}

			processedItemsCount++;
		}
	}

	protected void releaseResources() {

		try {
			destinationFileOutputStream.flush();
		} catch (Exception e2) {
			// Ignore
		}

		try {
			destinationFileOutputStream.close();
		} catch (Exception e2) {
			// Ignore
		}

		destinationFileOutputStream = null;

		currentCell = null;
		currentRow = null;
		currentSheet = null;
		dateCellStyle = null;
		creationHelper = null;
		workbook = null;

		tableComponent.getContainer().setFirstItemIndexForExcel(Integer.valueOf(currentTableContainerFirstRowIndex));
	}

	protected void showResults() {

		synchronized (tableComponent.getApplication()) {

			T4JWebApp webApp = (T4JWebApp) tableComponent.getApplication();

			webApp.closeDialogWindow();

			if (null == threadException) {

				if (Boolean.TRUE.equals(isInterrupted)) {
					tableComponent.showExportInterruptedNotification(destinationFile);
				} else {
					tableComponent.showExportFileDownloadDialog(destinationFile);
				}
			} else {

				if (Boolean.TRUE.equals(isInterrupted)) {
					tableComponent.showExportInterruptedNotification(destinationFile);
				} else {
					tableComponent.showExportErrorNotification(destinationFile);
				}
			}
		}
	}

	protected void updateProgressIndicator() {

		synchronized (tableComponent.getApplication()) {

			tableComponent.updateExportProgressIndicatorValue(Float.valueOf(1.0f * processedItemsCount / totalItemsCount));
		}
	}

	public Integer getFromPage() {

		return fromPage;
	}

	public Integer getToPage() {

		return toPage;
	}

	public void resetInstanceFields() {

	}

	public void run() {

		try {

			initializeResources();

			performPreIterationTasks();

			for (int i = firstRowIndex; i < lastRowIndex; i++) {

				if (Thread.interrupted()) {

					markAsInterrupted();
					break;
				} else {

					updateProgressIndicator();

					if (rowIndex % T4JConstants.MS_EXCEL_SHEET_MAX_ROWS == 0) {

						addHeaderRow();
					}

					if (i < currentTableContainerItemsCount) {

						processItem(i);
					} else {

						break;
					}

					rowIndex++;
				}
			}

			performPostIterationTasks();

			if (false == Boolean.TRUE.equals(isInterrupted)) {

				generateExcelFile();
			}
		} catch (Exception e) {

			logger.error(e, e);

			threadException = e;
		} finally {

			releaseResources();
		}

		showResults();
	}

	public void setFromPage(Integer fromPage) {

		this.fromPage = fromPage;
	}

	public void setTableComponent(T4JTableComponent tableComponent) {

		this.tableComponent = tableComponent;
	}

	public void setToPage(Integer toPage) {

		this.toPage = toPage;
	}
}
