
package com.t4j.wtk.util;

import java.io.Serializable;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;

public class T4JExcelUtils implements Serializable {

	private static final long serialVersionUID = 1L;

	private int lastRowIndex;

	private transient HSSFWorkbook workbook;

	private transient HSSFCellStyle redCellStyle;

	private transient HSSFCellStyle greenCellStyle;

	public T4JExcelUtils(HSSFWorkbook workbook) {

		super();

		this.workbook = workbook;

		setupCellStyles();

	}

	private void setupCellStyles() {

		redCellStyle = workbook.createCellStyle();
		redCellStyle.setFillForegroundColor(HSSFColor.RED.index);
		redCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

		greenCellStyle = workbook.createCellStyle();
		greenCellStyle.setFillForegroundColor(HSSFColor.GREEN.index);
		greenCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
	}

	public Boolean getBooleanCellValue(String sheetName, String columnName, String userIdentifier) {

		return getBooleanCellValue(sheetName, columnName, userIdentifier, 0);
	}

	public Boolean getBooleanCellValue(String sheetName, String columnName, String userIdentifier, int fromIndex) {

		Double tmpDouble = (Double) getCellValue(sheetName, columnName, userIdentifier, T4JExcelCellTypeEnum.NUMERIC, fromIndex);

		if (null == tmpDouble || Double.valueOf(0.0).equals(tmpDouble)) {

			return Boolean.FALSE;
		} else {

			return Boolean.TRUE;
		}
	}

	public Object getCellValue(String sheetName, String columnName, String userIdentifier, T4JExcelCellTypeEnum type) {

		return getCellValue(sheetName, columnName, userIdentifier, type, 0);
	}

	public Object getCellValue(String sheetName, String columnName, String userIdentifier, T4JExcelCellTypeEnum type, int fromIndex) {

		Object result = null;

		try {

			int index = 0;

			HSSFSheet tmpSheet = null;

			try {

				while (null != (tmpSheet = workbook.getSheetAt(index))) {

					index++;

					if (sheetName.equalsIgnoreCase(tmpSheet.getSheetName())) {

						break;
					}
				}
			} catch (Exception e) {
				// Ignore
			}

			int rowCount = tmpSheet.getLastRowNum() + 1;

			HSSFRow firstRow = tmpSheet.getRow(0);

			int cellCount = firstRow.getLastCellNum() + 1;

			for (int i = 0; i < rowCount; i++) {

				if (i < fromIndex) {

					continue;
				} else {

					HSSFRow tmpRow = tmpSheet.getRow(i);

					if (null == tmpRow) {

						continue;
					} else {

						HSSFCell firstCell = tmpRow.getCell(0);

						if (null == firstCell) {

							continue;
						} else {

							if (userIdentifier.equals(firstCell.getStringCellValue())) {

								for (int j = 0; j < cellCount; j++) {

									if (columnName.equalsIgnoreCase(firstRow.getCell(j).getStringCellValue())) {

										lastRowIndex = i;

										HSSFCell tmpCell = tmpRow.getCell(j);

										if (null == tmpCell) {

											result = null;
										} else {

											if (T4JExcelCellTypeEnum.NUMERIC.equals(type)) {
												result = Double.valueOf(tmpCell.getNumericCellValue());
											} else if (T4JExcelCellTypeEnum.DATE.equals(type)) {
												result = tmpCell.getDateCellValue();
											} else {
												result = tmpCell.getStringCellValue();
											}
										}

										break;
									}
								}
							}
						}
					}
				}
			}

		} catch (Exception e) {
			result = null;
		}

		return result;
	}

	public Date getDateCellValue(String sheetName, String columnName, String userIdentifier) {

		return getDateCellValue(sheetName, columnName, userIdentifier, 0);
	}

	public Date getDateCellValue(String sheetName, String columnName, String userIdentifier, int fromIndex) {

		return (Date) getCellValue(sheetName, columnName, userIdentifier, T4JExcelCellTypeEnum.DATE, fromIndex);
	}

	public Double getDoubleCellValue(String sheetName, String columnName, String userIdentifier) {

		return getDoubleCellValue(sheetName, columnName, userIdentifier, 0);
	}

	public Double getDoubleCellValue(String sheetName, String columnName, String userIdentifier, int fromIndex) {

		return (Double) getCellValue(sheetName, columnName, userIdentifier, T4JExcelCellTypeEnum.NUMERIC, fromIndex);
	}

	public HSSFCellStyle getGreenCellStyle() {

		return greenCellStyle;
	}

	public int getLastRowIndex() {

		return lastRowIndex;
	}

	public HSSFCellStyle getRedCellStyle() {

		return redCellStyle;
	}

	public void setGreenCellStyle(HSSFCellStyle greenCellStyle) {

		this.greenCellStyle = greenCellStyle;
	}

	public void setRedCellStyle(HSSFCellStyle redCellStyle) {

		this.redCellStyle = redCellStyle;
	}

}
