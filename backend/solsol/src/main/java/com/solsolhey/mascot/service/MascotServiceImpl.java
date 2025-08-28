package com.solsolhey.mascot.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solsolhey.mascot.domain.Background;
import com.solsolhey.mascot.domain.Mascot;
import com.solsolhey.mascot.domain.UserBackground;
import com.solsolhey.mascot.dto.ApplyBackgroundRequest;
import com.solsolhey.mascot.dto.ApplyBackgroundResponse;
import com.solsolhey.mascot.dto.AvailableItemResponse;
import com.solsolhey.mascot.dto.BackgroundResponse;
import com.solsolhey.mascot.dto.MascotCreateRequest;
import com.solsolhey.mascot.dto.MascotBackgroundUpdateRequest;
import com.solsolhey.mascot.dto.MascotEquipRequest;
import com.solsolhey.mascot.dto.MascotResponse;
import com.solsolhey.mascot.dto.MascotUpdateRequest;
import com.solsolhey.mascot.dto.UserBackgroundResponse;
import com.solsolhey.mascot.exception.MascotAlreadyExistsException;
import com.solsolhey.mascot.exception.MascotNotFoundException;
import com.solsolhey.mascot.repository.BackgroundRepository;
import com.solsolhey.mascot.repository.MascotRepository;
import com.solsolhey.mascot.repository.UserBackgroundRepository;
import com.solsolhey.shop.domain.Item;
import com.solsolhey.shop.domain.UserItem;
import com.solsolhey.shop.repository.ItemRepository;
import com.solsolhey.shop.repository.UserItemRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MascotServiceImpl implements MascotService {
    
    private final MascotRepository mascotRepository;
    private final BackgroundRepository backgroundRepository;
    private final UserBackgroundRepository userBackgroundRepository;
    private final ItemRepository itemRepository;
    private final UserItemRepository userItemRepository;
    private final ObjectMapper objectMapper;
    private final com.solsolhey.mascot.repository.MascotSnapshotRepository mascotSnapshotRepository;
    private final com.solsolhey.util.MediaStorageService mediaStorageService;
    private final com.solsolhey.solsol.config.MediaStorageProperties mediaStorageProperties;
    
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

    @Override
    @Transactional
    public MascotResponse updateBackgroundCustomization(Long userId, String backgroundColor, String patternType) {
        Mascot mascot = mascotRepository.findByUserId(userId)
                .orElseThrow(() -> new MascotNotFoundException(userId));
        mascot.updateBackgroundCustomization(backgroundColor, patternType);
        Mascot updated = mascotRepository.save(mascot);
        return MascotResponse.from(updated);
    }
    
    @Override
    public List<AvailableItemResponse> getAvailableItems(Long userId) {
        log.info("사용자 보유 아이템 목록 조회 요청 - userId: {}", userId);
        
        List<UserItem> userItems = userItemRepository.findByUserId(userId);
        log.info("사용자 {} 보유 아이템 수: {}", userId, userItems.size());
        
        return userItems.stream()
                .map(userItem -> {
                    Item item = itemRepository.findById(userItem.getItemId())
                            .orElse(null);
                    if (item == null) {
                        log.warn("아이템 ID {}가 존재하지 않습니다.", userItem.getItemId());
                        return null;
                    }
                    
                    // 장착 상태 확인 (현재는 단순히 false로 설정, 추후 로직 구현 필요)
                    Boolean isEquipped = checkIfEquipped(userId, item.getId());
                    
                    AvailableItemResponse response = AvailableItemResponse.builder()
                            .itemId(item.getId())
                            .itemName(item.getName())
                            .itemType(item.getType().name())
                            .imageUrl(item.getImageUrl())
                            .quantity(userItem.getQuantity())
                            .isEquipped(isEquipped)
                            .purchasedAt(userItem.getPurchasedAt())
                            .build();
                    
                    log.debug("아이템 {} 응답 생성 완료: {}", item.getId(), response);
                    return response;
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }
    
    /**
     * 아이템 장착 상태 확인 (임시 구현)
     * TODO: 실제 장착 상태 확인 로직 구현 필요
     */
    private Boolean checkIfEquipped(Long userId, Long itemId) {
        // 현재는 단순히 false 반환
        // 추후 마스코트의 equippedItem 필드와 비교하는 로직 구현 필요
        return false;
    }

    // === 커스터마이징 레이아웃 저장/조회 ===
    @Override
    public com.solsolhey.mascot.dto.MascotCustomizationDto getCustomization(Long userId) {
        Mascot mascot = mascotRepository.findByUserId(userId)
                .orElseThrow(() -> new MascotNotFoundException(userId));

        String json = mascot.getEquippedLayout();
        if (json == null || json.isBlank()) {
            return com.solsolhey.mascot.dto.MascotCustomizationDto.builder()
                    .version("v1")
                    .equippedItems(java.util.List.of())
                    .build();
        }
        try {
            return objectMapper.readValue(json, com.solsolhey.mascot.dto.MascotCustomizationDto.class);
        } catch (Exception e) {
            log.warn("커스터마이징 JSON 파싱 실패, 빈 레이아웃 반환: {}", e.getMessage());
            return com.solsolhey.mascot.dto.MascotCustomizationDto.builder()
                    .version("v1")
                    .equippedItems(java.util.List.of())
                    .build();
        }
    }

    @Override
    @Transactional
    public com.solsolhey.mascot.dto.MascotCustomizationDto saveCustomization(Long userId, com.solsolhey.mascot.dto.MascotCustomizationDto dto) {
        Mascot mascot = mascotRepository.findByUserId(userId)
                .orElseThrow(() -> new MascotNotFoundException(userId));

        // 간단 검증
        if (dto.getEquippedItems() != null) {
            for (var it : dto.getEquippedItems()) {
                if (it.getRelativePosition() == null) continue;
                Double x = it.getRelativePosition().getX();
                Double y = it.getRelativePosition().getY();
                if (x == null || y == null || x.isNaN() || y.isNaN()) {
                    throw new IllegalArgumentException("좌표 값이 유효하지 않습니다.");
                }
            }
        }

        try {
            String json = objectMapper.writeValueAsString(dto);
            mascot.updateEquippedLayout(json);
            // 스냅샷 이미지 저장: Data URL -> 파일시스템 -> URL 저장 (선택 입력)
            if (dto.getSnapshotImageDataUrl() != null && !dto.getSnapshotImageDataUrl().isBlank()) {
                String dataUrl = dto.getSnapshotImageDataUrl();
                try {
                    var saved = mediaStorageService.saveImageDataUrl(dataUrl, "snapshots");
                    String url = saved.urlPath; // e.g., /uploads/snapshots/...
                    mascot.updateSnapshotImage(url);
                    // 이력 테이블에 저장 (최대 20개 유지)
                    mascotSnapshotRepository.save(
                        com.solsolhey.mascot.domain.MascotSnapshot.builder()
                            .userId(userId)
                            .mascotId(mascot.getId())
                            .imageUrl(url)
                            .build()
                    );
                    long cnt = mascotSnapshotRepository.countByUserId(userId);
                    int maxEntries = Math.max(1, mediaStorageProperties.getSnapshotMaxEntries());
                    if (cnt > maxEntries) {
                        var olds = mascotSnapshotRepository.findByUserIdOrderByCreatedAtAsc(userId);
                        int toDelete = (int)(cnt - maxEntries);
                        for (int i = 0; i < toDelete && i < olds.size(); i++) {
                            var s = olds.get(i);
                            try { mediaStorageService.deleteUrlPath(s.getImageUrl()); } catch (Exception ignore) {}
                            mascotSnapshotRepository.deleteById(s.getId());
                        }
                    }
                } catch (Exception ex) {
                    log.warn("스냅샷 저장 실패: userId={}", userId, ex);
                }
            }
            mascotRepository.save(mascot);
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("커스터마이징 저장 실패", e);
        }
    }

    @Override
    public java.util.List<com.solsolhey.mascot.dto.MascotSnapshotResponse> getSnapshotHistory(Long userId) {
        var list = mascotSnapshotRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId);
        return list.stream().map(com.solsolhey.mascot.dto.MascotSnapshotResponse::from).toList();
    }
}
