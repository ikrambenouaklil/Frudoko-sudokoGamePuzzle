package com.frudoko.errors;


public class ServiceResult <T>{

    private T data ;
    private boolean success;
    private String message;

    public ServiceResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    public ServiceResult( boolean success , String message , T data) {
       this.success= success;
        this.data = data;
        this.message = message;
    }
// constructor + getters


    public T getData() {
        return data;
    }

    public void setItem(T item) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}