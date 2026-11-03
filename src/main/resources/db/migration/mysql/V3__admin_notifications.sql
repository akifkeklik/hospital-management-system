-- Add support for notifications addressed to administrators.
-- Keep this as a new migration: V1 may already be applied in deployed databases.
ALTER TABLE notifications
    ADD COLUMN admin_id BIGINT NULL;

CREATE INDEX IDX_NOTIFICATION_ADMIN_DATE
    ON notifications (admin_id, created_at DESC);
