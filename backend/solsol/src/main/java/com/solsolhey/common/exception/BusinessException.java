package com.solsolhey.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 비즈니스 로직 관련 예외
 */
public class BusinessException extends BaseException {
    
    public BusinessException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BUSINESS_ERROR");
    }
    
    public BusinessException(String message, String errorCode) {
        super(message, HttpStatus.BAD_REQUEST, errorCode);
    }
    
    public BusinessException(String message, HttpStatus httpStatus, String errorCode) {
        super(message, httpStatus, errorCode);
    }
}
