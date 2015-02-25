# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table requests (
  id                        bigint not null,
  to_user_id                varchar(255),
  from_user_id              varchar(255),
  status                    integer,
  message                   varchar(255),
  constraint ck_requests_status check (status in (0,1,2)),
  constraint pk_requests primary key (id))
;

create sequence requests_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists requests;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists requests_seq;

