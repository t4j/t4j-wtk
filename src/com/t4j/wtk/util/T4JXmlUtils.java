
package com.t4j.wtk.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/**
 * Clase de utilidades para documentos XML.
 *
 * @author Tenentia-4j, S.L.
 *
 */
public class T4JXmlUtils implements Serializable {

	private static final long serialVersionUID = 1L;

	private T4JXmlUtils() {

		super();
	}

	/**
	 * Devuelve un array de bytes con el contenido del documento XML formateado e indentado.
	 *
	 * @param sourceBytes
	 *            Array de bytes con el contenido del documento XML original.
	 * @return
	 */
	public static byte[] getFormattedBytes(byte[] sourceBytes) {

		try {

			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(sourceBytes);

			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document xmlDocument = documentBuilder.parse(byteArrayInputStream);
			byteArrayInputStream.close();

			// Guardamos el XML formado en una cadena de texto
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();
			ByteArrayOutputStream ost = new ByteArrayOutputStream();

			t.setOutputProperty(OutputKeys.METHOD, "xml");
			t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			t.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");

			ost.flush();
			ost.close();

			t.transform(new DOMSource(xmlDocument), new StreamResult(ost));

			return ost.toByteArray();
		} catch (Exception e) {
			return sourceBytes;
		}
	}
}
