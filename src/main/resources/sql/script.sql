create database bio_info;
use bio_info;

drop table if exists users;
create table users (
   id int primary key,
   email varchar(255) unique not null COMMENT '用户邮箱',
   password text not null COMMENT '用户密码',
   role varchar(10) default 'user' COMMENT '用户角色',
   available int default 1 COMMENT '逻辑删除字段',
   create_time datetime COMMENT '创建时间',
   update_time datetime COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;