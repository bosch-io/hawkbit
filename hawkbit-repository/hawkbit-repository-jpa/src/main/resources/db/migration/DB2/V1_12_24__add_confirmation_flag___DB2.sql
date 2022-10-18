ALTER TABLE sp_rolloutgroup ADD COLUMN confirmation_required BOOLEAN;
UPDATE sp_rolloutgroup SET confirmation_required = 0;

ALTER TABLE sp_target_filter_query ADD COLUMN confirmation_required BOOLEAN;
UPDATE sp_target_filter_query SET confirmation_required = 0;