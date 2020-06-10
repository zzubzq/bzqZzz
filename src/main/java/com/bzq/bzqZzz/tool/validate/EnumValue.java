package com.bzq.bzqZzz.tool.validate;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

/**
 * @author zzubzq on 2018/11/20.
 * 入参校验
 * 此类只能适用于枚举类型校验
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValue.EnumValidator.class)
@Documented
public @interface EnumValue {
    String message() default "invalid param";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 限制的枚举类型
     */
    Class<? extends Enum> target();

    /**
     * 当前对象转换为枚举对象方法的方法名
     */
    String convertMethod();

    /**
     * 是否允许为空值
     */
    boolean allowNull() default false;

    class EnumValidator implements ConstraintValidator<EnumValue, Object> {

        private Class<? extends Enum> target;

        private boolean allowNull;

        private String convertMethod;

        @Override
        public void initialize(EnumValue constraintAnnotation) {
            target = constraintAnnotation.target();
            allowNull = constraintAnnotation.allowNull();
            convertMethod = constraintAnnotation.convertMethod();
        }

        @Override
        public boolean isValid(Object code, ConstraintValidatorContext constraintValidatorContext) {
            if (code == null) {
                return allowNull;
            }
            try {
                Method method = target.getDeclaredMethod(convertMethod, code.getClass());
                //invoke 静态方法
                return method.invoke(null, code) != null;
            } catch (Exception e) {
                return false;
            }
        }
    }
}
