package com.atguigu.gulimall.product.exception;


import com.atguigu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import static com.atguigu.common.exception.StatusCode.UNKWONW_EXCEPTION;
import static com.atguigu.common.exception.StatusCode.VALID_EXCEPTION;

@Slf4j
@RestControllerAdvice(basePackages = "com.atguigu.gulimall.product.controller" )
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    public R handlerValidExcption(MethodArgumentNotValidException ex){

        Map<String,  String> errors = new HashMap<>();

        BindingResult res = ex.getBindingResult();
        res.getFieldErrors().forEach((item)->{
            errors.put(item.getField(), item.getDefaultMessage());

        });

        return R.error(VALID_EXCEPTION.getCode(), VALID_EXCEPTION.getMes()).put("data", errors);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public R handlerException(Exception e)
    {

        return R.error(UNKWONW_EXCEPTION.getCode(), UNKWONW_EXCEPTION.getMes());
    }
}
