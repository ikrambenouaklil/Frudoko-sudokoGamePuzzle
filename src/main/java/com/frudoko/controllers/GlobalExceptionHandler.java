//package com.frudoko.controllers;
//
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.servlet.NoHandlerFoundException;
//
//@ControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(NoHandlerFoundException.class)
//    public String handle404() {
//        return "error-404";
//    }
//
//    @ExceptionHandler(Exception.class)
//    public String handle500(Exception ex, Model model) {
//        model.addAttribute("message", ex.getMessage());
//        return "error-500";
//    }
//}