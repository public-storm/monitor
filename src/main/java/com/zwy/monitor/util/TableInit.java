package com.zwy.monitor.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zwy
 * @date 2023/6/26 16:47
 */
public class TableInit {
    private TableInit() {
        throw new IllegalStateException("Utility class");
    }

    private static final List<String> SQL_LIST = new ArrayList<>();
    private static final String USER_TABLE_CREATE_SQL = "create table if not exists user\n" +
            "(\n" +
            "    id          varchar(32)                           not null comment '用户id'\n" +
            "        primary key,\n" +
            "    user_name   varchar(100)                          not null comment '用户名',\n" +
            "    password    varchar(50)                           not null comment '密码',\n" +
            "    role_id     varchar(32) default '1'               not null comment '角色id',\n" +
            "    update_time timestamp   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',\n" +
            "    create_time timestamp   default CURRENT_TIMESTAMP not null comment '创建时间',\n" +
            "    constraint user_user_name_uindex\n" +
            "        unique (user_name)\n" +
            ")\n" +
            "    comment '用户表';";
    private static final String USER_FILE_TABLE_CREATE_SQL = "create table if not exists user_file\n" +
            "(\n" +
            "    id          varchar(32)                           not null comment '主键id'\n" +
            "        primary key,\n" +
            "    name        varchar(100)                          not null comment '文件名',\n" +
            "    path        varchar(200)                          null comment '路径',\n" +
            "    file        int         default 0                 not null comment '是否为文件 0：不是 1：是',\n" +
            "    super_id    varchar(32) default '-1'              not null comment '上级文件id 默认 -1:根目录',\n" +
            "    user_id     varchar(32)                           not null comment '用户id',\n" +
            "    size        bigint      default 0                 not null comment '文件大小',\n" +
            "    favorite    int         default 0                 null comment '是否收藏 0:否 1:是',\n" +
            "    suffix      varchar(50)                           null comment '文件后缀',\n" +
            "    status      int         default 1                 not null comment '文件状态 默认 1',\n" +
            "    create_time timestamp   default CURRENT_TIMESTAMP not null comment '创建时间',\n" +
            "    update_time timestamp   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',\n" +
            "    constraint index_user_id_name_super_id\n" +
            "        unique (name, user_id, super_id)\n" +
            ")\n" +
            "    comment '用户文件表';";
    private static final String ACTION_TABLE_CREATE_SQL = "create table if not exists action\n" +
            "(\n" +
            "    id          varchar(32)                         not null comment '主键id'\n" +
            "        primary key,\n" +
            "    action_name varchar(100)                        not null comment '用户操作行为名称',\n" +
            "    create_time timestamp default CURRENT_TIMESTAMP not null comment '创建时间'\n" +
            ")\n" +
            "    comment '用户操作行为表';";

    private static final String USER_ACTION_RECORD_TABLE_CREATE_SQL = "create table if not exists user_action_record\n" +
            "(\n" +
            "    id                 bigint auto_increment comment '主键id'\n" +
            "        primary key,\n" +
            "    user_id            varchar(36)                         not null comment '用户id',\n" +
            "    user_name          varchar(100)                        not null comment '用户名',\n" +
            "    login_ip           varchar(20)                         not null comment '登录ip地址',\n" +
            "    action_id          varchar(32)                         not null comment '用户操作行为类型id',\n" +
            "    action_params      varchar(2000)                       null comment '用户操作行为参数',\n" +
            "    create_time        timestamp default CURRENT_TIMESTAMP not null comment '创建时间'\n" +
            ")\n" +
            "    comment '用户登录记录表';";

    static {
        SQL_LIST.add(USER_TABLE_CREATE_SQL);
        SQL_LIST.add(USER_FILE_TABLE_CREATE_SQL);
        SQL_LIST.add(ACTION_TABLE_CREATE_SQL);
        SQL_LIST.add(USER_ACTION_RECORD_TABLE_CREATE_SQL);
    }

    public static List<String> findSqlList() {
        return SQL_LIST;
    }
}
