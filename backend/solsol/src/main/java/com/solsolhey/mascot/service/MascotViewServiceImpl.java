package com.solsolhey.mascot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solsolhey.common.exception.EntityNotFoundException;
import com.solsolhey.mascot.domain.Mascot;
import com.solsolhey.mascot.dto.MascotCustomizationDto;
import com.solsolhey.mascot.dto.view.*;
import com.solsolhey.mascot.exception.MascotNotFoundException;
import com.solsolhey.mascot.repository.MascotRepository;
import com.solsolhey.user.entity.User;
import com.solsolhey.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MascotViewServiceImpl implements MascotViewService {

    private final MascotRepository mascotRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public MascotViewResponse getView(Long viewerId, Long ownerId) {
        if (ownerId == null) {
            throw new IllegalArgumentException("ownerId는 필수입니다.");
        }

        boolean isOwner = viewerId != null && viewerId.equals(ownerId);
        ViewMode mode = isOwner ? ViewMode.SELF : ViewMode.OTHER;

        Permissions perms;
        if (isOwner) {
            perms = Permissions.self();
        } else {
            if (viewerId != null) { // 로그인한 방문자
                perms = Permissions.loggedInVisitor();
            } else { // 로그인하지 않은 방문자
                perms = Permissions.other();
            }
        }

        // 오너 요약 정보 조회
        User owner = userRepository.findByUserIdAndIsActiveTrue(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 마스코트 정보 조회
        Mascot mascot = mascotRepository.findByUserId(ownerId)
                .orElseThrow(() -> new MascotNotFoundException(ownerId));

        // 마스코트 커스터마이징 정보 조회 (간단한 버전)
        List<MascotCustomizationDto.Item> equippedItems = getMascotCustomizationItems(mascot);

        OwnerSummary ownerSummary = OwnerSummary.builder()
                .id(owner.getUserId())
                .nickname(owner.getNickname())
                .level(mascot.getLevel())
                .build();

        ViewerSummary viewerSummary = ViewerSummary.builder()
                .id(viewerId)
                .build();

        MascotSummary mascotSummary = MascotSummary.builder()
                .name(mascot.getName())
                .type(mascot.getType())
                .equippedItem(mascot.getEquippedItem())
                .level(mascot.getLevel())
                .backgroundColor(mascot.getBackgroundColor())
                .backgroundPattern(mascot.getBackgroundPattern())
                .equippedItems(equippedItems)
                .equippedLayout(mascot.getEquippedLayout())
                .build();

        return MascotViewResponse.builder()
                .owner(ownerSummary)
                .viewer(viewerSummary)
                .viewMode(mode)
                .permissions(perms)
                .mascot(mascotSummary)
                .build();
    }

    @Override
    public MascotViewResponse getPublicView(Long ownerId) {
        if (ownerId == null) {
            throw new IllegalArgumentException("ownerId는 필수입니다.");
        }

        log.info("공개 마스코트 조회: ownerId={}", ownerId);

        // 오너 요약 정보 조회
        User owner = userRepository.findByUserIdAndIsActiveTrue(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 마스코트 정보 조회
        Mascot mascot = mascotRepository.findByUserId(ownerId)
                .orElseThrow(() -> new MascotNotFoundException(ownerId));

        // 마스코트 커스터마이징 정보 조회 (간단한 버전)
        List<MascotCustomizationDto.Item> equippedItems = getMascotCustomizationItems(mascot);

        OwnerSummary ownerSummary = OwnerSummary.builder()
                .id(owner.getUserId())
                .nickname(owner.getNickname())
                .level(mascot.getLevel())
                .build();

        // 공개 조회는 viewer가 null (로그인하지 않은 사용자)
        ViewerSummary viewerSummary = ViewerSummary.builder()
                .id(null)
                .build();

        MascotSummary mascotSummary = MascotSummary.builder()
                .name(mascot.getName())
                .type(mascot.getType())
                .equippedItem(mascot.getEquippedItem())
                .level(mascot.getLevel())
                .backgroundColor(mascot.getBackgroundColor())
                .backgroundPattern(mascot.getBackgroundPattern())
                .equippedItems(equippedItems)
                .equippedLayout(mascot.getEquippedLayout())
                .build();

        return MascotViewResponse.builder()
                .owner(ownerSummary)
                .viewer(viewerSummary)
                .viewMode(ViewMode.OTHER) // 공개 조회는 항상 OTHER
                .permissions(Permissions.publicViewer()) // 공개 조회자 권한
                .mascot(mascotSummary)
                .build();
    }

    /**
     * 마스코트의 커스터마이징 정보(장착된 아이템)를 조회합니다.
     * equipped_layout JSON을 파싱하여 실제 아이템 정보를 반환합니다.
     */
    private List<MascotCustomizationDto.Item> getMascotCustomizationItems(Mascot mascot) {
        String equippedLayout = mascot.getEquippedLayout();
        
        // equipped_layout이 null이거나 빈 문자열인 경우
        if (equippedLayout == null || equippedLayout.trim().isEmpty()) {
            log.debug("equipped_layout is null or empty for mascot: {}", mascot.getId());
            return new ArrayList<>();
        }
        
        try {
            log.debug("Parsing equipped_layout for mascot {}: {}", mascot.getId(), equippedLayout);
            List<MascotCustomizationDto.Item> items = objectMapper.readValue(
                equippedLayout, 
                new TypeReference<List<MascotCustomizationDto.Item>>() {}
            );
            log.debug("Successfully parsed {} items for mascot: {}", items.size(), mascot.getId());
            return items;
        } catch (JsonProcessingException e) {
            log.error("Failed to parse equipped_layout for mascot {}: {}", mascot.getId(), equippedLayout, e);
            return new ArrayList<>(); // 파싱 실패 시 빈 리스트 반환
        } catch (Exception e) {
            log.error("Unexpected error while parsing equipped_layout for mascot {}: {}", mascot.getId(), equippedLayout, e);
            return new ArrayList<>();
        }
    }
}

