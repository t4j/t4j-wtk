
package com.t4j.wtk.beans.numbers;

/**
 * Componente Java que representa un número entero con su unidad y su plantilla para las conversiones de texto a número y viceversa.
 *
 * @author Tenentia-4j, S.L.
 *
 */
public class T4JLongWithUnit extends T4JNumberWithUnit {

	private static final long serialVersionUID = 1L;

	public T4JLongWithUnit(Long number, String unit, String pattern) {

		super(number, unit, pattern);
	}

	public static T4JLongWithUnit newInstance(Long number, String unit) {

		return new T4JLongWithUnit(number, unit, null);
	}

	public static T4JLongWithUnit newInstance(Long number, String unit, String pattern) {

		return new T4JLongWithUnit(number, unit, pattern);
	}

}
