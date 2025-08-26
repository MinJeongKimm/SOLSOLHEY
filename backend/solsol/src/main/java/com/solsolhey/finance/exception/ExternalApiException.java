package com.solsolhey.finance.exception;

import lombok.Getter;

@Getter
public class ExternalApiException extends RuntimeException {
    
    private final String errorCode;
    private final int httpStatus;
    
    public ExternalApiException(String message) {
        super(message);
        this.errorCode = "EXTERNAL_API_ERROR";
        this.httpStatus = 500;
    }
    
    public ExternalApiException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = 500;
    }
    
    public ExternalApiException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    
    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "EXTERNAL_API_ERROR";
        this.httpStatus = 500;
    }
}
