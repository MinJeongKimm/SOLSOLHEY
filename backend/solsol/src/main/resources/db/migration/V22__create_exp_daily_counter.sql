-- 일일 EXP 카운터 테이블 생성 (엔티티: com.solsolhey.exp.entity.ExpDailyCounter)

CREATE TABLE IF NOT EXISTS exp_daily_counter (
    counter_id                 BIGSERIAL PRIMARY KEY,
    user_id                    BIGINT NOT NULL,
    counter_date               DATE NOT NULL,
    attendance_awarded         BOOLEAN NOT NULL DEFAULT FALSE,
    attendance_streak7_awarded BOOLEAN NOT NULL DEFAULT FALSE,
    friend_active_award_count  INTEGER NOT NULL DEFAULT 0,
    friend_passive_award_count INTEGER NOT NULL DEFAULT 0,
    cat_finance_awarded        BOOLEAN NOT NULL DEFAULT FALSE,
    cat_academic_awarded       BOOLEAN NOT NULL DEFAULT FALSE,
    cat_social_awarded         BOOLEAN NOT NULL DEFAULT FALSE,
    cat_event_awarded          BOOLEAN NOT NULL DEFAULT FALSE,
    created_at                 TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                 TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ux_exp_daily_counter_user_date UNIQUE (user_id, counter_date)
);

CREATE INDEX IF NOT EXISTS ix_exp_daily_counter_user ON exp_daily_counter(user_id);
CREATE INDEX IF NOT EXISTS ix_exp_daily_counter_date ON exp_daily_counter(counter_date);

ALTER TABLE exp_daily_counter
    ADD CONSTRAINT fk_exp_daily_counter_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;

