package com.atguigu.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class List2ValueConstraintValidate implements ConstraintValidator<List2Value, Integer> {


    private Set<Integer> vals = new HashSet<>();

    @Override
    public void initialize(List2Value constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        for (int value : constraintAnnotation.values()) {
            vals.add(value);
        }
    }

    @Override
    public boolean isValid(Integer integer, ConstraintValidatorContext constraintValidatorContext) {
        return vals.contains(integer);
    }



}
