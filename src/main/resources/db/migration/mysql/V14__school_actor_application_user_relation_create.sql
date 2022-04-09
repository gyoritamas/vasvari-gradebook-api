drop table if exists school_actor_application_user_relation CASCADE;

create table school_actor_application_user_relation
(
    id              bigint NOT NULL AUTO_INCREMENT,
    app_user_id     bigint,
    school_actor_id bigint,
    user_role       varchar(255),
    primary key (id)
);