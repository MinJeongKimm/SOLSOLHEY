package com.solsolhey.mascot.service;

import com.solsolhey.mascot.domain.Mascot;
import com.solsolhey.mascot.dto.MascotCreateRequest;
import com.solsolhey.mascot.dto.MascotEquipRequest;
import com.solsolhey.mascot.dto.MascotResponse;
import com.solsolhey.mascot.dto.MascotUpdateRequest;
import com.solsolhey.mascot.exception.MascotAlreadyExistsException;
import com.solsolhey.mascot.exception.MascotNotFoundException;
import com.solsolhey.mascot.repository.MascotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MascotServiceImpl implements MascotService {
    
    private final MascotRepository mascotRepository;
    
    @Override
    @Transactional
    public MascotResponse createMascot(Long userId, MascotCreateRequest request) {
        log.info("마스코트 생성 요청 - 사용자 ID: {}, 이름: {}, 타입: {}", userId, request.getName(), request.getType());
        
        // 이미 마스코트가 존재하는지 확인
        if (mascotRepository.existsByUserId(userId)) {
            throw new MascotAlreadyExistsException(userId);
        }
        
        // 마스코트 생성
        Mascot mascot = Mascot.builder()
                .userId(userId)
                .name(request.getName())
                .type(request.getType())
                .equippedItem(request.getEquippedItem())
                .exp(0)
                .level(1)
                .build();
        
        Mascot savedMascot = mascotRepository.save(mascot);
        log.info("마스코트 생성 완료 - ID: {}", savedMascot.getId());
        
        return MascotResponse.from(savedMascot);
    }
    
    @Override
    public MascotResponse getMascot(Long userId) {
        log.info("마스코트 조회 요청 - 사용자 ID: {}", userId);
        
        Mascot mascot = mascotRepository.findByUserId(userId)
                .orElseThrow(() -> new MascotNotFoundException(userId));
        
        return MascotResponse.from(mascot);
    }
    
    @Override
    @Transactional
    public MascotResponse updateMascot(Long userId, MascotUpdateRequest request) {
        log.info("마스코트 업데이트 요청 - 사용자 ID: {}", userId);
        
        Mascot mascot = mascotRepository.findByUserId(userId)
                .orElseThrow(() -> new MascotNotFoundException(userId));
        
        // 이름 변경
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            mascot.changeName(request.getName());
            log.info("마스코트 이름 변경: {}", request.getName());
        }
        
        // 아이템 장착
        if (request.getEquippedItem() != null) {
            mascot.equipItem(request.getEquippedItem());
            log.info("마스코트 아이템 장착: {}", request.getEquippedItem());
        }
        
        // 경험치 증가
        if (request.getExpToAdd() != null && request.getExpToAdd() > 0) {
            mascot.addExp(request.getExpToAdd());
            log.info("마스코트 경험치 증가: +{} (현재: {})", request.getExpToAdd(), mascot.getExp());
        }
        
        Mascot updatedMascot = mascotRepository.save(mascot);
        log.info("마스코트 업데이트 완료 - ID: {}", updatedMascot.getId());
        
        return MascotResponse.from(updatedMascot);
    }
    
    @Override
    @Transactional
    public MascotResponse equipMascot(Long userId, MascotEquipRequest request) {
        log.info("마스코트 아이템 장착 요청 - 사용자 ID: {}, 아이템: {}", userId, request.getEquippedItem());
        
        Mascot mascot = mascotRepository.findByUserId(userId)
                .orElseThrow(() -> new MascotNotFoundException(userId));
        
        mascot.equipItem(request.getEquippedItem());
        Mascot updatedMascot = mascotRepository.save(mascot);
        
        log.info("마스코트 아이템 장착 완료 - ID: {}", updatedMascot.getId());
        
        return MascotResponse.from(updatedMascot);
    }
    
    @Override
    @Transactional
    public void deleteMascot(Long userId) {
        log.info("마스코트 삭제 요청 - 사용자 ID: {}", userId);
        
        if (!mascotRepository.existsByUserId(userId)) {
            throw new MascotNotFoundException(userId);
        }
        
        mascotRepository.deleteByUserId(userId);
        log.info("마스코트 삭제 완료 - 사용자 ID: {}", userId);
    }
}
