package com.solsolhey.solsol.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 엔티티를 찾을 수 없는 경우의 예외
 */
public class EntityNotFoundException extends BaseException {
    
    public EntityNotFoundException(String entityName, Long id) {
        super(String.format("%s를 찾을 수 없습니다. ID: %d", entityName, id), 
              HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND");
    }
    
    public EntityNotFoundException(String entityName, String identifier) {
        super(String.format("%s를 찾을 수 없습니다. %s", entityName, identifier), 
              HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND");
    }
    
    public EntityNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND");
    }
}
