CREATE TABLE `action` (
                          `id` varchar(32) NOT NULL COMMENT '主键id',
                          `action_name` varchar(100) NOT NULL COMMENT '用户操作行为名称',
                          `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='用户操作行为表';

INSERT INTO `my_file`.`action` (`id`, `action_name`, `create_time`) VALUES ('f8c396b2b7324d7f8048582ff1e64e38', '登录', '2023-07-18 17:42:02');


CREATE TABLE `user` (
                        `id` varchar(32) NOT NULL COMMENT '用户id',
                        `user_name` varchar(100) NOT NULL COMMENT '用户名',
                        `password` varchar(50) NOT NULL COMMENT '密码',
                        `role_id` varchar(32) NOT NULL DEFAULT '1' COMMENT '角色id',
                        `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `user_user_name_uindex` (`user_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='用户表';

INSERT INTO `my_file`.`user` (`id`, `user_name`, `password`, `role_id`, `update_time`, `create_time`) VALUES ('6afb389dcc2a45479eb1677d9d8c9294', '123', '54d936f5463121c82c82120458fc53b05d91a8205f42d71d', '1', '2023-05-31 11:25:52', '2023-05-31 11:25:52');
INSERT INTO `my_file`.`user` (`id`, `user_name`, `password`, `role_id`, `update_time`, `create_time`) VALUES ('ec98dd0c49d741ae898378eaa349b610', '张三', '44a83f55513821322f72b20f08915bd00c96e8295f32f91f', '1', '2023-05-28 10:35:30', '2023-05-28 10:35:30');


CREATE TABLE `user_action_record` (
                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                      `user_id` varchar(36) NOT NULL COMMENT '用户id',
                                      `user_name` varchar(100) NOT NULL COMMENT '用户名',
                                      `login_ip` varchar(20) NOT NULL COMMENT '登录ip地址',
                                      `action_id` varchar(32) NOT NULL COMMENT '用户操作行为类型id',
                                      `action_params` varchar(2000) DEFAULT NULL COMMENT '用户操作行为参数',
                                      `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=400 DEFAULT CHARSET=utf8mb3 COMMENT='用户登录记录表';

CREATE TABLE `user_file` (
                             `id` varchar(32) NOT NULL COMMENT '主键id',
                             `name` varchar(2000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '文件名',
                             `file` int NOT NULL DEFAULT '0' COMMENT '是否为文件 0：不是 1：是',
                             `super_id` varchar(32) NOT NULL DEFAULT '-1' COMMENT '上级文件id 默认 -1:根目录',
                             `user_id` varchar(32) NOT NULL COMMENT '用户id',
                             `size` bigint NOT NULL DEFAULT '0' COMMENT '文件大小',
                             `suffix` varchar(50) DEFAULT NULL COMMENT '文件后缀',
                             `status` int NOT NULL DEFAULT '0' COMMENT '文件状态 默认 1',
                             `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='用户文件表';

CREATE TABLE `user_file_history` (
                                     `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
                                     `name` varchar(2000) NOT NULL COMMENT '文件名',
                                     `size` bigint NOT NULL COMMENT '文件大小',
                                     `suffix` varchar(50) NOT NULL COMMENT '文件后缀',
                                     `user_id` varchar(32) NOT NULL COMMENT '用户id',
                                     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                     `file_type` int NOT NULL COMMENT '文件类型',
                                     PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1693447259068735491 DEFAULT CHARSET=utf8mb3;