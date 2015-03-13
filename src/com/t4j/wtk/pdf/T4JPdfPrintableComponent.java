
package com.t4j.wtk.pdf;

import java.io.Serializable;

import com.itextpdf.text.Document;
import com.vaadin.ui.Component;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
public interface T4JPdfPrintableComponent extends Serializable {

	/**
	 * Genera los elementos a incluir en el PDF.
	 * 
	 * @param pdfDocument
	 * @param component
	 * @throws T4JPdfGenerationException
	 */
	public void printComponent(Document pdfDocument, Component component) throws T4JPdfGenerationException;
}
