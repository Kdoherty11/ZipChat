# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table chat_room_model (
  id                        varchar(255) not null,
  name                      varchar(255),
  constraint pk_chat_room_model primary key (id))
;

create table user (
  id                        varchar(255) not null,
  facebook_id               varchar(255),
  name                      varchar(255),
  registration_id           varchar(255),
  constraint pk_user primary key (id))
;

create sequence chat_room_model_seq;

create sequence user_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists chat_room_model;

drop table if exists user;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists chat_room_model_seq;

drop sequence if exists user_seq;

