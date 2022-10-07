ALTER TABLE sp_rolloutgroup ADD column consent_given BOOLEAN;
UPDATE sp_rolloutgroup SET consent_given = 0;