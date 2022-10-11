ALTER TABLE sp_rolloutgroup ADD column confirmation_required BOOLEAN;
UPDATE sp_rolloutgroup SET confirmation_required = 0;