package com.jakubkras.project.exception;

public class MovieNotFoundException extends Exception{

    public MovieNotFoundException(String msg){
        super(msg);
    }
}
