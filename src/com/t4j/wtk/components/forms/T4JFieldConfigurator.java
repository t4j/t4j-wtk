
package com.t4j.wtk.components.forms;

import java.io.Serializable;

import com.vaadin.ui.Field;
import com.vaadin.ui.Form;

/**
 * Interfaz para configurar los distintos parámetros de un campo de un formulario.
 *
 * @author Tenentia-4j, S.L.
 *
 */
public interface T4JFieldConfigurator extends Serializable {

	/**
	 *
	 * @param form
	 * @param field
	 * @param bean
	 * @param propertyId
	 */
	public Field configureField(Form form, Field field, Object bean, Object propertyId);
}
