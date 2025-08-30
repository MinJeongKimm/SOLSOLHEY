package com.solsolhey.exp.service;

import com.solsolhey.challenge.entity.ChallengeCategory;
import com.solsolhey.user.entity.User;

import java.util.Optional;

public interface ExpDailyCounterService {

    Optional<ExpAwarded> awardAttendanceExp(User user, int consecutiveDays);

    Optional<ExpAwarded> awardFriendInteractionExpActive(User actor);

    Optional<ExpAwarded> awardFriendInteractionExpPassive(User receiver);

    Optional<ExpAwarded> awardChallengeCategoryExp(User user, ChallengeCategory.CategoryType categoryType);

    record ExpAwarded(int amount, String type, String category, int totalExp, int level) {}
}

