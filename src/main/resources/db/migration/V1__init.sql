create table app_user (
    id         uuid primary key,
    handle     varchar(50) not null unique,
    created_at timestamptz not null
);

create table follow (
    id          uuid primary key,
    follower_id uuid not null references app_user (id),
    followee_id uuid not null references app_user (id),
    created_at  timestamptz not null,
    unique (follower_id, followee_id)
);
create index idx_follow_followee on follow (followee_id);

create table post (
    id         uuid primary key,
    author_id  uuid not null references app_user (id),
    content    varchar(560) not null,
    created_at timestamptz not null
);
create index idx_post_author on post (author_id);
