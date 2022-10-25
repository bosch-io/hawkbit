create table sp_target_conf_status
(
    target_id       bigint      not null,
    initiator       varchar(64) not null,
    activatedAt     bigint not null,
    remark          varchar(512),
    primary key (target_id)
);
ALTER TABLE sp_target_conf_status
    ADD CONSTRAINT fk_target_auto_conf FOREIGN KEY (target_id) REFERENCES sp_target (id) ON DELETE CASCADE;

ALTER TABLE sp_rolloutgroup ADD column confirmation_required BOOLEAN;
UPDATE sp_rolloutgroup SET confirmation_required = 0;
