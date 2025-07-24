package com.ms.user.exceptions;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class ValidationError extends StandardError{
    private Map<String, String> errors = new HashMap<>();

    public ValidationError(Instant timestamp, int status, String error, String message){
        super(timestamp,status,error,message);
    }
    public Map<String, String> getErrors(){
        return errors;
    }
    public void addError(String field, String message){
        errors.put(field, message);
    }
}
