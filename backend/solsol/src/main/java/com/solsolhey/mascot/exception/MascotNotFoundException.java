package com.solsolhey.mascot.exception;

public class MascotNotFoundException extends RuntimeException {
    
    public MascotNotFoundException(String message) {
        super(message);
    }
    
    public MascotNotFoundException(Long userId) {
        super("사용자 ID " + userId + "에 해당하는 마스코트를 찾을 수 없습니다.");
    }
}
