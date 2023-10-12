package com.atguigu.common.exception;

/***
 * 错误码和错误信息定义类
 * 1. 错误码定义规则为5为数字
 * 2. 前两位表示业务场景，最后三位表示错误码。例如：100001。10:通用 001:系统未知异常
 * 3. 维护错误码后需要维护错误描述，将他们定义为枚举形式
 * 错误码列表：
 *  10: 通用
 *      001：参数格式校验
 *  11: 商品
 *  12: 订单
 *  13: 购物车
 *  14: 物流
 *
 *
 */
public enum BizCodeEnume {
    UNKNOW_EXCEPTION(10000,"系统未知异常"),
    VAILD_EXCEPTION(10001,"参数格式校验失败"),
    TOO_MANY_REQUET (10002 , "请求次数过多"),
    ELASTIC_SAVE_EXCEPTION(11001, "尚品上架失败"),
    SMS_CODE_ALIVE(20001,"验证码获取频率太高，请稍后再试"),

    USERNAME_CHECK_EXCEPION(20002, "用户名已存在"),
    PHONE_CHECK_EXCEPION(20003 ,"手机号已被使用"),
    SOCIA_USER_LOGIN_EXCEPTION(20004, "社交账户登录失败"),
    LOGIN_ACCT_PASS_EXCEPTION(20005, "用户名或密码错误");


    private int code;
    private String msg;
    BizCodeEnume(int code,String msg){
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
