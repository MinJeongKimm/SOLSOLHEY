package com.solsolhey.common.exception;

/**
 * 포인트 관련 예외
 */
public class PointException extends BusinessException {
    
    public PointException(String message) {
        super(message, "POINT_ERROR");
    }
    
    /**
     * 포인트 부족 예외
     */
    public static class InsufficientPointsException extends PointException {
        public InsufficientPointsException(int currentPoints, int requiredPoints) {
            super(String.format("포인트가 부족합니다. 현재: %d, 필요: %d", currentPoints, requiredPoints));
        }
        
        public InsufficientPointsException() {
            super("포인트가 부족합니다.");
        }
    }
    
    /**
     * 잘못된 포인트 값 예외
     */
    public static class InvalidPointAmountException extends PointException {
        public InvalidPointAmountException() {
            super("포인트 값이 유효하지 않습니다.");
        }
        
        public InvalidPointAmountException(String message) {
            super(message);
        }
    }
}
