package io.j1st.utils.http.entity;


/**
 * Simple Result Response
 */
@SuppressWarnings("unused")
public class ResultEntity<T> {

    private String status ;
    private T data;
    private Integer code;
    private String message;

    /**
     * 成功
     * @param data
     */
    public ResultEntity(T data) {
        this.status = "success";
        this.data = data;
    }

    /**
     * 失败
     * @param code
     * @param message
     */
    public ResultEntity(Integer code ,String message) {
        this.status = "fail";
        this.code = code;
        this.message = message;
    }


    /**
     * 失败
     * @param lang
     * @param code
     */
    public ResultEntity(String lang ,Integer code) {
        this.status = "fail";
        this.code = code;
        this.message = ErrorMessageUtils.getMessage(lang,code.toString());
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}