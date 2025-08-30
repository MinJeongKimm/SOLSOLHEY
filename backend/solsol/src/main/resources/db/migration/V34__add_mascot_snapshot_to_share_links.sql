-- Add missing mascot_snapshot column to share_links to match entity
ALTER TABLE share_links
    ADD COLUMN IF NOT EXISTS mascot_snapshot TEXT;

COMMENT ON COLUMN share_links.mascot_snapshot IS '공유 시점의 마스코트 스냅샷 이미지(Data URL 또는 URL)';

