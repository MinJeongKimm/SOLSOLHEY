-- 출석 테이블 생성 (엔티티: com.solsolhey.user.entity.Attendance)

CREATE TABLE IF NOT EXISTS attendance (
    attendance_id   BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    consecutive_days INTEGER NOT NULL DEFAULT 1,
    exp_reward      INTEGER NOT NULL DEFAULT 0,
    point_reward    INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ux_attendance_user_date UNIQUE (user_id, attendance_date)
);

CREATE INDEX IF NOT EXISTS ix_attendance_user ON attendance(user_id);
CREATE INDEX IF NOT EXISTS ix_attendance_date ON attendance(attendance_date);

ALTER TABLE attendance
    ADD CONSTRAINT fk_attendance_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;

