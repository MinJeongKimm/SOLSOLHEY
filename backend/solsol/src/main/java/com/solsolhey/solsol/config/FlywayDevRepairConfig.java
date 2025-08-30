package com.solsolhey.solsol.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Dev 프로파일에서만 Flyway repair를 선행 실행하여
 * 과거 체크섬 불일치 등으로 인한 마이그레이션 실패를 자동 치유합니다.
 * 운영 프로파일에는 적용하지 않습니다.
 */
@Configuration
@Profile("dev")
public class FlywayDevRepairConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return (Flyway flyway) -> {
            // 1) 히스토리 테이블의 체크섬 불일치 등 오류를 수리
            flyway.repair();
            // 2) 이후 마이그레이션을 정상 적용
            flyway.migrate();
        };
    }
}

