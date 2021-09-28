package cn.lori.bean.populator.model;

public class Result<T> {
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILURE = "FAILURE";

    private String message;
    private String result;
    private T data;

    public Result() {
        this.result = SUCCESS;
    }

    public Result(String result, T data) {
        this.data = data;
        this.result = result;
    }

    public Result(String result, T data, String message) {
        this.data = data;
        this.result = result;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

}
