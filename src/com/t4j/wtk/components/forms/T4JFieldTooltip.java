
package com.t4j.wtk.components.forms;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para el texto descriptivo internacionalizado de los campos del formulario.
 *
 * @author Tenentia-4j, S.L.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface T4JFieldTooltip {

	String bundleKey() default "";

	String cs() default "";

	String de() default "";

	String def();

	String en() default "";

	String es() default "";

	String fr() default "";

	String it() default "";

	String pt() default "";

}
