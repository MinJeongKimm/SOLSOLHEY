package com.solsolhey.friend.repository;

import com.solsolhey.friend.entity.FriendLikePairState;
import com.solsolhey.user.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FriendLikePairStateRepository extends JpaRepository<FriendLikePairState, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM FriendLikePairState s WHERE s.userLow = :low AND s.userHigh = :high")
    Optional<FriendLikePairState> findForUpdate(@Param("low") User low,
                                                @Param("high") User high);

    @Query("SELECT s FROM FriendLikePairState s WHERE s.userLow = :low AND s.userHigh = :high")
    Optional<FriendLikePairState> findOne(@Param("low") User low,
                                          @Param("high") User high);
}

