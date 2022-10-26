ALTER TABLE sp_rolloutgroup ADD confirmation_required BIT DEFAULT 0;
ALTER TABLE sp_target_filter_query ADD confirmation_required BIT DEFAULT 0;

CREATE TABLE sp_target_conf_status
(
    target_id       NUMERIC(19) NOT NULL,
    initiator       VARCHAR(64) NOT NULL,
    activatedAt     NUMERIC(19) NOT NULL,
    remark          VARCHAR(512),
    PRIMARY KEY (target_id)
);
ALTER TABLE sp_target_conf_status
    ADD CONSTRAINT fk_target_auto_conf FOREIGN KEY (target_id) REFERENCES sp_target (id) ON DELETE CASCADE;
