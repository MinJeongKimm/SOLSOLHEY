package com.solsolhey.challenge.repository;

import com.solsolhey.challenge.entity.ChallengeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 챌린지 카테고리 레포지토리
 */
@Repository
public interface ChallengeCategoryRepository extends JpaRepository<ChallengeCategory, Long> {

    /**
     * 활성화된 카테고리 목록 조회
     */
    List<ChallengeCategory> findByIsActiveTrueOrderByCategoryId();

    /**
     * 카테고리 타입으로 조회
     */
    Optional<ChallengeCategory> findByCategoryName(ChallengeCategory.CategoryType categoryName);

    /**
     * 활성화된 카테고리를 타입으로 조회
     */
    Optional<ChallengeCategory> findByCategoryNameAndIsActiveTrue(ChallengeCategory.CategoryType categoryName);
}
