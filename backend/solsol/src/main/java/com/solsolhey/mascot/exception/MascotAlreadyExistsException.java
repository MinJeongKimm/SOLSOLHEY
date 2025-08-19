package com.solsolhey.mascot.exception;

public class MascotAlreadyExistsException extends RuntimeException {
    
    public MascotAlreadyExistsException(String message) {
        super(message);
    }
    
    public MascotAlreadyExistsException(Long userId) {
        super("사용자 ID " + userId + "는 이미 마스코트를 보유하고 있습니다.");
    }
}
