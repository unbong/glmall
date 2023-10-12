package com.atguigu.common.exception;

public enum StatusCode {
    UNKWONW_EXCEPTION (10000, "未知的异常"),
    VALID_EXCEPTION (10001, "校验的异常");
    private int code ;

    private String Message;

    StatusCode(int code ,  String mes)
    {
        this.code = code;
        this.Message = mes;
    }

    public int getCode ()
    {
        return this.code;
    }


    public String getMes(){
        return this.Message;
    }

}
