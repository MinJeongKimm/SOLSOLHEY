package com.solsolhey.mascot.repository;

import com.solsolhey.mascot.domain.Mascot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MascotRepository extends JpaRepository<Mascot, Long> {
    
    /**
     * 사용자 ID로 마스코트 조회
     * @param userId 사용자 ID
     * @return 마스코트 Optional
     */
    Optional<Mascot> findByUserId(Long userId);
    
    /**
     * 사용자 ID로 마스코트 존재 여부 확인
     * @param userId 사용자 ID
     * @return 존재 여부
     */
    boolean existsByUserId(Long userId);
    
    /**
     * 사용자 ID로 마스코트 삭제
     * @param userId 사용자 ID
     */
    void deleteByUserId(Long userId);
}
