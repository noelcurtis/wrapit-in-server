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
  name      text not null,
  purpose   text not null,
  due_date  timestamp,
  constraint  pk_gift_list primary key (id)
);

create sequence gift_list_seq;

create table item (
  id            bigint not null,
  gift_list_id  int not null,
  name          varchar(255) not null,
  url           text not null,
  needed        int not null,
  purchased     int not null,
  img_url       text not null,
  constraint  pk_item primary key (id),
  foreign key(gift_list_id) references gift_list(id) on delete cascade
);

create sequence item_seq;

create table photo (
  id      bigint not null,
  folder  varchar(255) not null,
  path    text not null,
  constraint pk_photo primary key (id)
);

create sequence photo_seq;

create table photo_relation (
  owner_id  bigint not null,
  photo_id bigint not null,
  constraint pk_photo_join primary key (owner_id, photo_id)
);

# --- !Downs

drop table if exists users;

drop sequence if exists users_seq;

drop table if exists gift_list_role;

drop table if exists gift_list cascade;

drop sequence if exists gift_list_seq;

drop table if exists item cascade;

drop sequence if exists item_seq;

drop table if exists photo cascade;

drop sequence if exists photo_seq;

drop table if exists photo_relation;

