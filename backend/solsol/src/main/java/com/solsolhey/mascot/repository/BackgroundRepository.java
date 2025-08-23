package com.solsolhey.mascot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.solsolhey.mascot.domain.Background;

/**
 * 배경 정보 레포지토리
 */
@Repository
public interface BackgroundRepository extends JpaRepository<Background, String> {

    /**
     * 활성화된 배경 목록 조회
     */
    List<Background> findByEnabledTrue();

    /**
     * 활성화 여부별 배경 목록 조회
     */
    List<Background> findByEnabled(Boolean enabled);

    /**
     * 특정 태그를 포함한 활성화된 배경 목록 조회
     */
    @Query("SELECT b FROM Background b WHERE b.enabled = true AND " +
           "EXISTS (SELECT t FROM b.tags t WHERE t IN :tags)")
    List<Background> findByEnabledTrueAndTagsIn(@Param("tags") List<String> tags);

    /**
     * 활성화 여부와 태그 조건으로 배경 목록 조회
     */
    @Query("SELECT b FROM Background b WHERE b.enabled = :enabled AND " +
           "EXISTS (SELECT t FROM b.tags t WHERE t IN :tags)")
    List<Background> findByEnabledAndTagsIn(@Param("enabled") Boolean enabled, 
                                           @Param("tags") List<String> tags);

    /**
     * 특정 태그를 포함한 배경 목록 조회 (활성화 여부 무관)
     */
    @Query("SELECT b FROM Background b WHERE " +
           "EXISTS (SELECT t FROM b.tags t WHERE t IN :tags)")
    List<Background> findByTagsIn(@Param("tags") List<String> tags);

    /**
     * 배경 ID로 활성화된 배경 조회
     */
    Optional<Background> findByIdAndEnabledTrue(String id);

    /**
     * 배경 이름으로 검색
     */
    List<Background> findByNameContainingIgnoreCase(String name);

    /**
     * 활성화된 배경 수 조회
     */
    long countByEnabledTrue();

    /**
     * 특정 태그를 가진 활성화된 배경 수 조회
     */
    @Query("SELECT COUNT(b) FROM Background b WHERE b.enabled = true AND " +
           "EXISTS (SELECT t FROM b.tags t WHERE t IN :tags)")
    long countByEnabledTrueAndTagsIn(@Param("tags") List<String> tags);

    /**
     * 기본 배경 조회
     */
    @Query("SELECT b FROM Background b WHERE b.id = 'bg_room_basic'")
    Optional<Background> findDefaultBackground();
}
