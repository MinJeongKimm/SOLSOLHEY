package com.solsolhey.mascot.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solsolhey.mascot.domain.Background;
import com.solsolhey.mascot.domain.Mascot;
import com.solsolhey.mascot.domain.UserBackground;
import com.solsolhey.mascot.dto.ApplyBackgroundRequest;
import com.solsolhey.mascot.dto.ApplyBackgroundResponse;
import com.solsolhey.mascot.dto.BackgroundResponse;
import com.solsolhey.mascot.dto.MascotCreateRequest;
import com.solsolhey.mascot.dto.MascotEquipRequest;
import com.solsolhey.mascot.dto.MascotResponse;
import com.solsolhey.mascot.dto.MascotUpdateRequest;
import com.solsolhey.mascot.dto.UserBackgroundResponse;
import com.solsolhey.mascot.exception.MascotAlreadyExistsException;
import com.solsolhey.mascot.exception.MascotNotFoundException;
import com.solsolhey.mascot.repository.BackgroundRepository;
import com.solsolhey.mascot.repository.MascotRepository;
import com.solsolhey.mascot.repository.UserBackgroundRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MascotServiceImpl implements MascotService {
    
    private final MascotRepository mascotRepository;
    private final BackgroundRepository backgroundRepository;
    private final UserBackgroundRepository userBackgroundRepository;
    
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
    
    // === 배경 관련 메서드 구현 ===
    
    @Override
    public List<BackgroundResponse> getBackgrounds(Boolean enabled, List<String> tags, Long userId) {
        log.info("배경 목록 조회 요청 - enabled: {}, tags: {}, userId: {}", enabled, tags, userId);
        
        List<Background> backgrounds;
        
        // 조건에 따른 배경 조회
        if (enabled != null && tags != null && !tags.isEmpty()) {
            backgrounds = backgroundRepository.findByEnabledAndTagsIn(enabled, tags);
        } else if (enabled != null) {
            backgrounds = backgroundRepository.findByEnabled(enabled);
        } else if (tags != null && !tags.isEmpty()) {
            backgrounds = backgroundRepository.findByTagsIn(tags);
        } else {
            backgrounds = backgroundRepository.findAll();
        }
        
        // 사용자 보유 배경 ID 목록 조회
        List<String> backgroundIds = backgrounds.stream()
                .map(Background::getId)
                .toList();
        List<String> ownedBackgroundIds = userBackgroundRepository.findOwnedBackgroundIds(userId, backgroundIds);
        
        return BackgroundResponse.fromList(backgrounds, ownedBackgroundIds);
    }
    
    @Override
    public List<UserBackgroundResponse> getUserOwnedBackgrounds(Long userId) {
        log.info("사용자 보유 배경 목록 조회 요청 - userId: {}", userId);
        
        List<UserBackground> userBackgrounds = userBackgroundRepository.findByUserIdOrderByAcquiredAtDesc(userId);
        
        List<String> backgroundIds = userBackgrounds.stream()
                .map(UserBackground::getBackgroundId)
                .toList();
        
        List<Background> backgrounds = backgroundRepository.findAllById(backgroundIds);
        
        return UserBackgroundResponse.fromLists(userBackgrounds, backgrounds);
    }
    
    @Override
    @Transactional
    public ApplyBackgroundResponse applyBackground(Long mascotId, ApplyBackgroundRequest request, Long userId) {
        log.info("마스코트 배경 적용 요청 - mascotId: {}, backgroundId: {}, userId: {}", 
                 mascotId, request.getBackgroundId(), userId);
        
        // 마스코트 조회 및 소유자 확인
        Mascot mascot = mascotRepository.findById(mascotId)
                .orElseThrow(() -> new MascotNotFoundException(mascotId));
        
        if (!mascot.getUserId().equals(userId)) {
            throw new RuntimeException("권한이 없습니다.");
        }
        
        // 배경 존재 여부 및 활성화 상태 확인
        backgroundRepository.findByIdAndEnabledTrue(request.getBackgroundId())
                .orElseThrow(() -> new RuntimeException("해당 배경은 사용할 수 없습니다."));
        
        // 사용자 배경 보유 여부 확인
        if (!userBackgroundRepository.existsByUserIdAndBackgroundId(userId, request.getBackgroundId())) {
            throw new RuntimeException("해당 배경은 보유하고 있지 않습니다.");
        }
        
        // 배경 적용
        mascot.changeBackground(request.getBackgroundId());
        Mascot updatedMascot = mascotRepository.save(mascot);
        
        log.info("마스코트 배경 적용 완료 - mascotId: {}, backgroundId: {}", mascotId, request.getBackgroundId());
        
        return ApplyBackgroundResponse.success(
                updatedMascot.getId(), 
                updatedMascot.getBackgroundId(), 
                updatedMascot.getUpdatedAt()
        );
    }
    
    @Override
    @Transactional
    public ApplyBackgroundResponse resetBackground(Long mascotId, Long userId) {
        log.info("마스코트 배경 해제 요청 - mascotId: {}, userId: {}", mascotId, userId);
        
        // 마스코트 조회 및 소유자 확인
        Mascot mascot = mascotRepository.findById(mascotId)
                .orElseThrow(() -> new MascotNotFoundException(mascotId));
        
        if (!mascot.getUserId().equals(userId)) {
            throw new RuntimeException("권한이 없습니다.");
        }
        
        // 기본 배경으로 초기화
        mascot.resetToDefaultBackground();
        Mascot updatedMascot = mascotRepository.save(mascot);
        
        log.info("마스코트 배경 해제 완료 - mascotId: {}, backgroundId: {}", mascotId, updatedMascot.getBackgroundId());
        
        return ApplyBackgroundResponse.success(
                updatedMascot.getId(), 
                updatedMascot.getBackgroundId(), 
                updatedMascot.getUpdatedAt()
        );
    }
}
