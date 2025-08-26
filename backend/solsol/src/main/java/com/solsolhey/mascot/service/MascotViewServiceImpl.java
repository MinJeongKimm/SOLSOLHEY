package com.solsolhey.mascot.service;

import com.solsolhey.common.exception.EntityNotFoundException;
import com.solsolhey.mascot.domain.Mascot;
import com.solsolhey.mascot.dto.view.*;
import com.solsolhey.mascot.exception.MascotNotFoundException;
import com.solsolhey.mascot.repository.MascotRepository;
import com.solsolhey.user.entity.User;
import com.solsolhey.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MascotViewServiceImpl implements MascotViewService {

    private final MascotRepository mascotRepository;
    private final UserRepository userRepository;

    @Override
    public MascotViewResponse getView(Long viewerId, Long ownerId) {
        if (ownerId == null) {
            throw new IllegalArgumentException("ownerId는 필수입니다.");
        }

        boolean self = viewerId != null && viewerId.equals(ownerId);
        ViewMode mode = self ? ViewMode.SELF : ViewMode.OTHER;
        Permissions perms = self ? Permissions.self() : Permissions.other();

        // 오너 요약 정보 조회
        User owner = userRepository.findByUserIdAndIsActiveTrue(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 마스코트 정보 조회
        Mascot mascot = mascotRepository.findByUserId(ownerId)
                .orElseThrow(() -> new MascotNotFoundException(ownerId));

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
                .backgroundId(mascot.getBackgroundId())
                .build();

        return MascotViewResponse.builder()
                .owner(ownerSummary)
                .viewer(viewerSummary)
                .viewMode(mode)
                .permissions(perms)
                .mascot(mascotSummary)
                .build();
    }
}

