package com.frudoko.errors;


import com.frudoko.model.User;

public class ServiceResult <T>{

    private T item ;
    private boolean success;
    private String message;

    public ServiceResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    public ServiceResult( boolean success , String message , T item) {
       this.success= success;
        this.item = item;
        this.message = message;
    }
// constructor + getters


    public T getItem() {
        return item;
    }

    public void setItem(T item) {
        this.item = item;
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