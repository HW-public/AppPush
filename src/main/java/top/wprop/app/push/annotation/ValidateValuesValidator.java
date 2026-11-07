package top.wprop.app.push.annotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//自定义注解的验证器
public class ValidateValuesValidator implements ConstraintValidator<ValidateValues, Object> {
    private List<String> allowedStringValues;
    private List<Integer> allowedIntValues;

    @Override
    public void initialize(ValidateValues constraintAnnotation) {
        allowedStringValues = Arrays.asList(constraintAnnotation.allowedStringValues());
        allowedIntValues =
                Arrays.stream(constraintAnnotation.allowedIntValues()).boxed().collect(Collectors.toList());
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // 如果需要字段非空，请结合 @NotNull 使用
        }
        if (value instanceof String) {
            return allowedStringValues.contains(value);
        } else if (value instanceof Integer) {
            return allowedIntValues.contains(value);
        }
        return false;
    }
}