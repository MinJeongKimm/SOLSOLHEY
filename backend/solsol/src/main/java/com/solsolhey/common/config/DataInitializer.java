package com.solsolhey.common.config;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.solsolhey.campus.Campus;
import com.solsolhey.campus.CampusRepository;
import com.solsolhey.challenge.entity.Challenge;
import com.solsolhey.challenge.entity.ChallengeCategory;
import com.solsolhey.challenge.repository.ChallengeCategoryRepository;
import com.solsolhey.challenge.repository.ChallengeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 초기 데이터 설정
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ChallengeCategoryRepository categoryRepository;
    private final ChallengeRepository challengeRepository;
    private final CampusRepository campusRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeCampusesIfEmpty();
        initializeChallengeCategories();
        initializeSampleChallenges();
    }

    /**
     * 로컬(H2) 개발 환경에서 CAMPUS 테이블이 비어있으면 기본 데이터 시드
     */
    private void initializeCampusesIfEmpty() {
        try {
            if (campusRepository.count() > 0) {
                log.info("캠퍼스 데이터가 이미 존재함. 스킵.");
                return;
            }

            log.info("캠퍼스 기본 데이터 초기화 시작");

            String[] campuses = new String[]{
                // SSAFY 캠퍼스
                "SSAFY 서울캠퍼스", "SSAFY 대전캠퍼스", "SSAFY 광주캠퍼스", "SSAFY 구미캠퍼스", "SSAFY 부울경캠퍼스"
            };

            for (String name : campuses) {
                Campus c = new Campus(name);
                campusRepository.save(c);
            }

            log.info("캠퍼스 기본 데이터 초기화 완료 ({}건)", campuses.length);
        } catch (Exception e) {
            log.warn("캠퍼스 데이터 초기화 중 오류 발생 (무시 가능): {}", e.getMessage());
        }
    }

    private void initializeChallengeCategories() {
        log.info("챌린지 카테고리 초기화 시작");

        // 기존 카테고리가 있는지 확인
        if (categoryRepository.count() > 0) {
            log.info("챌린지 카테고리가 이미 존재함. 스킵.");
            return;
        }

        // 카테고리 생성
        ChallengeCategory finance = ChallengeCategory.builder()
                .categoryName(ChallengeCategory.CategoryType.FINANCE)
                .displayName("금융")
                .description("금융 관련 챌린지")
                .build();

        ChallengeCategory academic = ChallengeCategory.builder()
                .categoryName(ChallengeCategory.CategoryType.ACADEMIC)
                .displayName("학사")
                .description("학업 관련 챌린지")
                .build();

        ChallengeCategory social = ChallengeCategory.builder()
                .categoryName(ChallengeCategory.CategoryType.SOCIAL)
                .displayName("소셜")
                .description("사회 활동 관련 챌린지")
                .build();

        ChallengeCategory event = ChallengeCategory.builder()
                .categoryName(ChallengeCategory.CategoryType.EVENT)
                .displayName("이벤트")
                .description("특별 이벤트 챌린지")
                .build();

        categoryRepository.save(finance);
        categoryRepository.save(academic);
        categoryRepository.save(social);
        categoryRepository.save(event);

        log.info("챌린지 카테고리 초기화 완료");
    }

    private void initializeSampleChallenges() {
        log.info("샘플 챌린지 데이터 초기화 시작");

        // 기존 챌린지가 있는지 확인
        if (challengeRepository.count() > 0) {
            log.info("챌린지가 이미 존재함. 스킵.");
            return;
        }

        // 카테고리 조회
        ChallengeCategory financeCategory = categoryRepository.findByCategoryName(ChallengeCategory.CategoryType.FINANCE)
                .orElseThrow(() -> new RuntimeException("금융 카테고리를 찾을 수 없습니다."));
        
        ChallengeCategory academicCategory = categoryRepository.findByCategoryName(ChallengeCategory.CategoryType.ACADEMIC)
                .orElseThrow(() -> new RuntimeException("학사 카테고리를 찾을 수 없습니다."));

        ChallengeCategory socialCategory = categoryRepository.findByCategoryName(ChallengeCategory.CategoryType.SOCIAL)
                .orElseThrow(() -> new RuntimeException("소셜 카테고리를 찾을 수 없습니다."));

        // 샘플 챌린지 생성
        LocalDateTime now = LocalDateTime.now();
        
        // 금융 챌린지
        Challenge savingChallenge = Challenge.builder()
                .category(financeCategory)
                .challengeName("7일 연속 용돈 기입장 작성하기")
                .description("매일 하루 용돈 사용 내역을 기록해보세요!")
                .rewardPoints(100)
                .rewardExp(50)
                .challengeType(Challenge.ChallengeType.WEEKLY)
                .difficulty(Challenge.ChallengeDifficulty.EASY)
                .startDate(now.minusDays(1))
                .endDate(now.plusDays(30))
                .targetCount(7)
                .build();

        Challenge budgetChallenge = Challenge.builder()
                .category(financeCategory)
                .challengeName("한 달 예산 계획 세우기")
                .description("이번 달 수입과 지출 계획을 세워보세요.")
                .rewardPoints(200)
                .rewardExp(100)
                .challengeType(Challenge.ChallengeType.MONTHLY)
                .difficulty(Challenge.ChallengeDifficulty.MEDIUM)
                .startDate(now.minusDays(1))
                .endDate(now.plusDays(60))
                .targetCount(1)
                .build();

        // 학사 챌린지
        Challenge studyChallenge = Challenge.builder()
                .category(academicCategory)
                .challengeName("매일 2시간 공부하기")
                .description("매일 최소 2시간 이상 공부하고 인증해보세요!")
                .rewardPoints(50)
                .rewardExp(25)
                .challengeType(Challenge.ChallengeType.DAILY)
                .difficulty(Challenge.ChallengeDifficulty.MEDIUM)
                .startDate(now.minusDays(1))
                .endDate(now.plusDays(7))
                .targetCount(7)
                .build();

        Challenge assignmentChallenge = Challenge.builder()
                .category(academicCategory)
                .challengeName("과제 미루지 않기")
                .description("과제를 마감일 하루 전까지 완료해보세요.")
                .rewardPoints(150)
                .rewardExp(75)
                .challengeType(Challenge.ChallengeType.SPECIAL)
                .difficulty(Challenge.ChallengeDifficulty.HARD)
                .startDate(now.minusDays(1))
                .endDate(now.plusDays(14))
                .targetCount(3)
                .build();

        // 소셜 챌린지
        Challenge friendChallenge = Challenge.builder()
                .category(socialCategory)
                .challengeName("친구와 함께 커피 마시기")
                .description("친구들과 만나서 소중한 시간을 보내보세요.")
                .rewardPoints(80)
                .rewardExp(40)
                .challengeType(Challenge.ChallengeType.WEEKLY)
                .difficulty(Challenge.ChallengeDifficulty.EASY)
                .startDate(now.minusDays(1))
                .endDate(now.plusDays(21))
                .targetCount(2)
                .build();

        // 챌린지 저장
        challengeRepository.save(savingChallenge);
        challengeRepository.save(budgetChallenge);
        challengeRepository.save(studyChallenge);
        challengeRepository.save(assignmentChallenge);
        challengeRepository.save(friendChallenge);

        log.info("샘플 챌린지 데이터 초기화 완료");
    }
}
