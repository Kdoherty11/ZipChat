# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table room (
  id                        varchar(255) not null,
  name                      varchar(255),
  constraint pk_room primary key (id))
;

create sequence room_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists room;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists room_seq;

