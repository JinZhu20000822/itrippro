package cn.itrip.common;

public class HttpResult {
    private String success;
    private String msg;
    private String errorCode;
    private Object data;

    public HttpResult(String success, String msg, String errorCode, Object data) {
        this.success = success;
        this.msg = msg;
        this.errorCode = errorCode;
        this.data = data;
    }

    public static HttpResult ok() {
        return new HttpResult("true",null,null,null);
    }
    public static HttpResult ok(String msg, Object data) {
        return new HttpResult("true",msg,null,data);
    }
    public static HttpResult error() {
        return new HttpResult("false",null,"500",null);
    }
    public static HttpResult error(String msg, Object data) {
        return new HttpResult("false",msg,"500",data);
    }
    public static HttpResult error(String errorCode, String msg, Object data) {
        return new HttpResult("false",msg,errorCode,data);
    }
    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
