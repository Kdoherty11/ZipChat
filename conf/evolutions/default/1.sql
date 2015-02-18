# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table messages (
  id                        varchar(255) not null,
  message                   varchar(255),
  room_id                   varchar(255),
  user_id                   varchar(255),
  time_stamp                timestamp,
  constraint pk_messages primary key (id))
;

create table rooms (
  id                        varchar(255) not null,
  name                      varchar(255),
  latitude                  NUMERIC,
  longitude                 NUMERIC,
  radius                    integer,
  creation_time             timestamp,
  last_activity             timestamp,
  distance                  integer,
  score                     integer,
  constraint pk_rooms primary key (id))
;

create table users (
  id                        varchar(255) not null,
  facebook_id               varchar(255),
  name                      varchar(255),
  registration_id           varchar(255),
  constraint pk_users primary key (id))
;

create sequence messages_seq;

create sequence rooms_seq;

create sequence users_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists messages;

drop table if exists rooms;

drop table if exists users;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists messages_seq;

drop sequence if exists rooms_seq;

drop sequence if exists users_seq;

