
package com.t4j.wtk.beans.numbers;

/**
 * Componente Java que representa un número decimal con su unidad y su plantilla para las conversiones de texto a número y viceversa.
 *
 * @author Tenentia-4j, S.L.
 *
 */
public class T4JDoubleWithUnit extends T4JNumberWithUnit {

	private static final long serialVersionUID = 1L;

	public T4JDoubleWithUnit(Double number, String unit, String pattern) {

		super(number, unit, pattern);
	}

	public static T4JDoubleWithUnit newInstance(Double number, String unit) {

		return new T4JDoubleWithUnit(number, unit, null);
	}

	public static T4JDoubleWithUnit newInstance(Double number, String unit, String pattern) {

		return new T4JDoubleWithUnit(number, unit, pattern);
	}
}
