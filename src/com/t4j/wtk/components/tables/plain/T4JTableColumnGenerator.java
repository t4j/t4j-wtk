
package com.t4j.wtk.components.tables.plain;

import org.apache.poi.ss.usermodel.Row;

import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public interface T4JTableColumnGenerator extends ColumnGenerator {

	public void generateExcelCell(Row excelRow, int tableColumnIndex, Table table, Object itemId, Object columnId);

}
