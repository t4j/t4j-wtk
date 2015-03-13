
package com.t4j.wtk.components.forms;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author Tenentia-4j, S.L.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface T4JFieldDescriptor {

	boolean immediate() default false;

	boolean nullSelectionAllowed() default true;

	boolean readonly() default false;

	boolean required() default false;

	int maxLength() default -1;

	int visibleRows() default 1;

	String cssClassNames() default "";

	String fieldConfiguratorClassName() default "";
	
	String formatterClassName() default "";

	String itemsGeneratorClassName() default "";

	String listenerClassNames() default "";

	String popupGeneratorClassName() default "";

	String validatorClassNames() default "";	

	T4JFieldTypeEnum fieldType() default T4JFieldTypeEnum.UNDEFINED;

}
