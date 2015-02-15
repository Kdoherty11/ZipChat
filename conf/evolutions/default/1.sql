# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table rooms (
  id                        varchar(255) not null,
  name                      varchar(255),
  latitude                  double,
  longitude                 double,
  radius                    integer,
  time_stamp                bigint,
  last_activity             bigint,
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

create sequence rooms_seq;

create sequence users_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists rooms;

drop table if exists users;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists rooms_seq;

drop sequence if exists users_seq;

