package com.solsolhey.solsol.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 인증/인가 관련 예외
 */
public class AuthException extends BaseException {
    
    public AuthException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "AUTH_ERROR");
    }
    
    public AuthException(String message, HttpStatus httpStatus) {
        super(message, httpStatus, "AUTH_ERROR");
    }
    
    public AuthException(String message, HttpStatus httpStatus, String errorCode) {
        super(message, httpStatus, errorCode);
    }
    
    /**
     * 인증 토큰이 유효하지 않은 경우
     */
    public static class InvalidTokenException extends AuthException {
        public InvalidTokenException() {
            super("유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
        }
        
        public InvalidTokenException(String message) {
            super(message, HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
        }
    }
    
    /**
     * 토큰이 만료된 경우
     */
    public static class TokenExpiredException extends AuthException {
        public TokenExpiredException() {
            super("토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED");
        }
    }
    
    /**
     * 접근 권한이 없는 경우
     */
    public static class AccessDeniedException extends AuthException {
        public AccessDeniedException() {
            super("접근 권한이 없습니다.", HttpStatus.FORBIDDEN, "ACCESS_DENIED");
        }
        
        public AccessDeniedException(String message) {
            super(message, HttpStatus.FORBIDDEN, "ACCESS_DENIED");
        }
    }
    
    /**
     * 사용자를 찾을 수 없는 경우
     */
    public static class UserNotFoundException extends AuthException {
        public UserNotFoundException() {
            super("사용자를 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND");
        }
        
        public UserNotFoundException(String message) {
            super(message, HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND");
        }
    }
    
    /**
     * 잘못된 인증 정보인 경우
     */
    public static class InvalidCredentialsException extends AuthException {
        public InvalidCredentialsException() {
            super("아이디 또는 비밀번호가 잘못되었습니다.", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
        }
        
        public InvalidCredentialsException(String message) {
            super(message, HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
        }
    }
}
