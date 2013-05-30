# --- !Ups


create table users (
  id                        bigint not null,
  email                     varchar(255) not null unique,
  encrypted_password        varchar(255) not null,
  last_sign_in              timestamp not null,
  constraint pk_user primary key (id)
);

create sequence users_seq;

create table gift_list_role (
  user_id       bigint not null,
  gift_list_id  bigint not null,
  role          int not null,
  constraint pk_gift_list_role primary key (user_id, gift_list_id)
);

create table gift_list (
  id        bigint  not null,
  name      varchar(255) not null,
  purpose   text not null,
  due_date  timestamp,
  constraint  pk_gift_list primary key (id)
);

create sequence gift_list_seq;


# --- !Downs

drop table if exists users;

drop sequence if exists users_seq;

drop table if exists gift_list_role;

drop table if exists gift_list;

drop sequence if exists gift_list_seq;