CREATE UNIQUE INDEX uk_ann_read_receipt_announcement_user
    ON announcement_read_receipt (tenant_id, announcement_id, user_id)
    WHERE deleted = FALSE;
