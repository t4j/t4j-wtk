
package com.t4j.wtk.components.forms.fields.generators;

import java.io.Serializable;

import com.vaadin.ui.AbstractSelect;

/**
 * Interfaz para generar las opciones disponibles de una lista desplegable.
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public interface T4JSelectItemsGenerator extends Serializable {

	/**
	 * Genera las opciones disponibles de una lista desplegable.
	 * 
	 * @param abstractSelect
	 * @param dataSource
	 */
	public void generateItems(AbstractSelect abstractSelect, Object dataSource);

}
