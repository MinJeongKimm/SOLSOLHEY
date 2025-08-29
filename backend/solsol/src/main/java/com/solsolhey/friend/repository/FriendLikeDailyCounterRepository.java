package com.solsolhey.friend.repository;

import com.solsolhey.friend.entity.FriendLikeDailyCounter;
import com.solsolhey.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;

public interface FriendLikeDailyCounterRepository extends JpaRepository<FriendLikeDailyCounter, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM FriendLikeDailyCounter c WHERE c.fromUser = :from AND c.toUser = :to AND c.likeDate = :date")
    Optional<FriendLikeDailyCounter> findForUpdate(@Param("from") User from,
                                                   @Param("to") User to,
                                                   @Param("date") LocalDate date);

    @Query("SELECT c FROM FriendLikeDailyCounter c WHERE c.fromUser = :from AND c.toUser = :to AND c.likeDate = :date")
    Optional<FriendLikeDailyCounter> findOne(@Param("from") User from,
                                             @Param("to") User to,
                                             @Param("date") LocalDate date);
}

