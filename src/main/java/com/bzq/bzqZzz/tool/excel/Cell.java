/**
 * @version $Id$
 * Create date: Aug 18, 2014
 * Create by: daniellee
 * Copyright (c) 2013 UniqueSoft.
 * All rights reserved.
 */
package com.bzq.bzqZzz.tool.excel;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author bai
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Cell {

    String titleName();

    int width() default 30;

    float order() default 0;

    String format() default "";

    String[] enums() default {};

}
