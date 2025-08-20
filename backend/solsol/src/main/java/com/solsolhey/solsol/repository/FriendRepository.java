package com.solsolhey.solsol.repository;

import com.solsolhey.solsol.entity.Friend;
import com.solsolhey.solsol.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 친구 관계 Repository
 */
@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {

    /**
     * 사용자의 친구 목록 조회 (활성 상태만)
     */
    List<Friend> findByUserAndStatusOrderByCreatedAtDesc(User user, Friend.FriendStatus status);

    /**
     * 사용자의 친구 목록 조회 (페이징, 활성 상태만)
     */
    Page<Friend> findByUserAndStatusOrderByCreatedAtDesc(User user, Friend.FriendStatus status, Pageable pageable);

    /**
     * 친구 관계 존재 여부 확인
     */
    boolean existsByUserAndFriendUser(User user, User friendUser);

    /**
     * 양방향 친구 관계 존재 여부 확인
     */
    @Query("SELECT COUNT(f) > 0 FROM Friend f WHERE " +
           "(f.user = :user1 AND f.friendUser = :user2) OR " +
           "(f.user = :user2 AND f.friendUser = :user1)")
    boolean existsFriendshipBetween(@Param("user1") User user1, @Param("user2") User user2);

    /**
     * 특정 친구 관계 조회
     */
    Optional<Friend> findByUserAndFriendUser(User user, User friendUser);

    /**
     * 사용자의 친구 수 조회 (활성 상태만)
     */
    long countByUserAndStatus(User user, Friend.FriendStatus status);

    /**
     * 사용자가 특정 사용자를 친구로 추가했는지 확인
     */
    boolean existsByUserAndFriendUserAndStatus(User user, User friendUser, Friend.FriendStatus status);

    /**
     * 사용자를 친구로 추가한 모든 사용자 조회 (나를 친구로 추가한 사람들)
     */
    List<Friend> findByFriendUserAndStatusOrderByCreatedAtDesc(User friendUser, Friend.FriendStatus status);

    /**
     * 양방향 친구 관계 조회 (서로 친구인 관계)
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "JOIN Friend f1 ON f1.friendUser = u " +
           "JOIN Friend f2 ON f2.user = u " +
           "WHERE f1.user = :user AND f2.friendUser = :user " +
           "AND f1.status = 'ACTIVE' AND f2.status = 'ACTIVE'")
    List<User> findMutualFriends(@Param("user") User user);
}
