package com.solsolhey.exp.service;

import com.solsolhey.challenge.entity.ChallengeCategory;
import com.solsolhey.exp.entity.ExpDailyCounter;
import com.solsolhey.exp.repository.ExpDailyCounterRepository;
import com.solsolhey.mascot.domain.Mascot;
import com.solsolhey.mascot.repository.MascotRepository;
import com.solsolhey.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExpDailyCounterServiceImpl implements ExpDailyCounterService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final ExpDailyCounterRepository counterRepository;
    private final MascotRepository mascotRepository;

    @Override
    public Optional<ExpAwarded> awardAttendanceExp(User user, int consecutiveDays) {
        Optional<Mascot> mascotOpt = mascotRepository.findByUserId(user.getUserId());
        if (mascotOpt.isEmpty()) {
            log.debug("마스코트 없음으로 출석 EXP 스킵: userId={}", user.getUserId());
            return Optional.empty();
        }

        LocalDate todayKst = LocalDate.now(KST);
        ExpDailyCounter counter = getOrCreateLocked(user, todayKst);

        int amount = 0;
        if (!Boolean.TRUE.equals(counter.getAttendanceAwarded())) {
            amount += 5;
            counter.markAttendanceAwarded();
        }
        if (consecutiveDays >= 7 && !Boolean.TRUE.equals(counter.getAttendanceStreak7Awarded())) {
            amount += 20;
            counter.markStreak7Awarded();
        }

        if (amount <= 0) return Optional.empty();

        Mascot mascot = mascotOpt.get();
        mascot.addExp(amount);
        mascotRepository.save(mascot);
        counterRepository.save(counter);

        return Optional.of(new ExpAwarded(amount, "ATTENDANCE", null, mascot.getExp(), mascot.getLevel()));
    }

    @Override
    public Optional<ExpAwarded> awardFriendInteractionExpActive(User actor) {
        Optional<Mascot> mascotOpt = mascotRepository.findByUserId(actor.getUserId());
        if (mascotOpt.isEmpty()) {
            log.debug("마스코트 없음으로 친구 Active EXP 스킵: userId={}", actor.getUserId());
            return Optional.empty();
        }

        LocalDate todayKst = LocalDate.now(KST);
        ExpDailyCounter counter = getOrCreateLocked(actor, todayKst);

        int base = (counter.getFriendActiveAwardCount() < 3) ? 3 : 1;
        counter.incFriendActive();

        Mascot mascot = mascotOpt.get();
        mascot.addExp(base);
        mascotRepository.save(mascot);
        counterRepository.save(counter);

        return Optional.of(new ExpAwarded(base, "FRIEND_ACTIVE", null, mascot.getExp(), mascot.getLevel()));
    }

    @Override
    public Optional<ExpAwarded> awardFriendInteractionExpPassive(User receiver) {
        Optional<Mascot> mascotOpt = mascotRepository.findByUserId(receiver.getUserId());
        if (mascotOpt.isEmpty()) {
            log.debug("마스코트 없음으로 친구 Passive EXP 스킵: userId={}", receiver.getUserId());
            return Optional.empty();
        }

        LocalDate todayKst = LocalDate.now(KST);
        ExpDailyCounter counter = getOrCreateLocked(receiver, todayKst);

        int amount = 1;
        counter.incFriendPassive();

        Mascot mascot = mascotOpt.get();
        mascot.addExp(amount);
        mascotRepository.save(mascot);
        counterRepository.save(counter);

        return Optional.of(new ExpAwarded(amount, "FRIEND_PASSIVE", null, mascot.getExp(), mascot.getLevel()));
    }

    @Override
    public Optional<ExpAwarded> awardChallengeCategoryExp(User user, ChallengeCategory.CategoryType categoryType) {
        Optional<Mascot> mascotOpt = mascotRepository.findByUserId(user.getUserId());
        if (mascotOpt.isEmpty()) {
            log.debug("마스코트 없음으로 챌린지 카테고리 EXP 스킵: userId={}", user.getUserId());
            return Optional.empty();
        }

        LocalDate todayKst = LocalDate.now(KST);
        ExpDailyCounter counter = getOrCreateLocked(user, todayKst);

        boolean alreadyAwarded;
        switch (categoryType) {
            case FINANCE -> alreadyAwarded = counter.getCatFinanceAwarded();
            case ACADEMIC -> alreadyAwarded = counter.getCatAcademicAwarded();
            case SOCIAL -> alreadyAwarded = counter.getCatSocialAwarded();
            case EVENT -> alreadyAwarded = counter.getCatEventAwarded();
            default -> alreadyAwarded = true;
        }

        if (alreadyAwarded) return Optional.empty();

        // mark awarded
        switch (categoryType) {
            case FINANCE -> counter.markFinanceAwarded();
            case ACADEMIC -> counter.markAcademicAwarded();
            case SOCIAL -> counter.markSocialAwarded();
            case EVENT -> counter.markEventAwarded();
        }

        int amount = 5;
        Mascot mascot = mascotOpt.get();
        mascot.addExp(amount);
        mascotRepository.save(mascot);
        counterRepository.save(counter);

        return Optional.of(new ExpAwarded(amount, "CHALLENGE_CATEGORY", categoryType.name(), mascot.getExp(), mascot.getLevel()));
    }

    private ExpDailyCounter getOrCreateLocked(User user, LocalDate kstDate) {
        return counterRepository.findWithLockByUserAndCounterDate(user, kstDate)
                .orElseGet(() -> {
                    // 동시성으로 인한 중복 생성은 UNIQUE 제약으로 방지
                    ExpDailyCounter created = ExpDailyCounter.builder()
                            .user(user)
                            .counterDate(kstDate)
                            .build()
                            ;
                    try {
                        return counterRepository.save(created);
                    } catch (Exception e) {
                        // 충돌 시 재조회
                        return counterRepository.findWithLockByUserAndCounterDate(user, kstDate)
                                .orElseThrow(() -> new IllegalStateException("Failed to acquire daily counter row"));
                    }
                });
    }
}

