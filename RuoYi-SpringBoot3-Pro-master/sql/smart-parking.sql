-- ========================================= --
-- AI 视觉车位管理系统业务表（RuoYi-Vue3-Pro 兼容）
-- MySQL 8.x
-- ========================================= --

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1、车位信息表
-- ----------------------------
DROP TABLE IF EXISTS `biz_parking_slot`;
CREATE TABLE `biz_parking_slot` (
                                    `parking_slot_id` bigint NOT NULL AUTO_INCREMENT COMMENT '车位ID',
                                    `area_id` bigint NULL DEFAULT NULL COMMENT '停车区域ID',
                                    `slot_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '车位编号',
                                    `slot_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'FREE' COMMENT '车位状态(FREE/OCCUPIED)',
                                    `camera_id` bigint NULL DEFAULT NULL COMMENT '绑定摄像头ID',
                                    `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标志(0存在 2删除)',
                                    `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
                                    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
                                    `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
                                    PRIMARY KEY (`parking_slot_id`) USING BTREE,
                                    UNIQUE INDEX `uk_biz_parking_slot_area_code`(`area_id`, `slot_code` ASC) USING BTREE,
                                    INDEX `idx_biz_parking_slot_camera_id`(`camera_id` ASC) USING BTREE,
                                    INDEX `idx_biz_parking_slot_status`(`slot_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '车位信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 2、车位 ROI 标注表
-- ----------------------------
DROP TABLE IF EXISTS `biz_parking_roi`;
CREATE TABLE `biz_parking_roi` (
                                   `parking_roi_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ROI标注ID',
                                   `slot_id` bigint NOT NULL COMMENT '车位ID',
                                   `image_width` int NULL DEFAULT NULL COMMENT '图片宽度',
                                   `image_height` int NULL DEFAULT NULL COMMENT '图片高度',
                                   `roi_polygon` json NULL DEFAULT NULL COMMENT 'ROI多边形坐标(JSON)',
                                   `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标志(0存在 2删除)',
                                   `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
                                   `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
                                   `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
                                   PRIMARY KEY (`parking_roi_id`) USING BTREE,
                                   INDEX `idx_biz_parking_roi_slot_id`(`slot_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '车位ROI标注表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 3、摄像头设备表
-- ----------------------------
DROP TABLE IF EXISTS `dev_camera`;
CREATE TABLE `dev_camera` (
                              `camera_id` bigint NOT NULL AUTO_INCREMENT COMMENT '摄像头ID',
                              `camera_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '摄像头名称',
                              `rtsp_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'RTSP地址',
                              `camera_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备状态(ONLINE/OFFLINE)',
                              `last_heartbeat` datetime NULL DEFAULT NULL COMMENT '最后心跳时间',
                              `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '安装位置',
                              `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标志(0存在 2删除)',
                              `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
                              `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
                              `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
                              PRIMARY KEY (`camera_id`) USING BTREE,
                              INDEX `idx_dev_camera_status`(`camera_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '摄像头设备表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 4、AI 推理事件表
-- ----------------------------
DROP TABLE IF EXISTS `ai_event`;
CREATE TABLE `ai_event` (
                            `event_id` bigint NOT NULL AUTO_INCREMENT COMMENT '事件ID',
                            `camera_id` bigint NULL DEFAULT NULL COMMENT '摄像头ID',
                            `slot_id` bigint NULL DEFAULT NULL COMMENT '车位ID',
                            `old_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '变更前状态',
                            `new_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '变更后状态',
                            `confidence` decimal(5,2) NULL DEFAULT NULL COMMENT '置信度',
                            `event_time` datetime NOT NULL COMMENT '事件发生时间',
                            `frame_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关键帧图片路径',
                            `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标志(0存在 2删除)',
                            `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '记录创建人/来源',
                            `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
                            `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
                            `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
                            PRIMARY KEY (`event_id`) USING BTREE,
                            INDEX `idx_ai_event_slot_id`(`slot_id` ASC) USING BTREE,
                            INDEX `idx_ai_event_camera_id`(`camera_id` ASC) USING BTREE,
                            INDEX `idx_ai_event_time`(`event_time` ASC) USING BTREE,
                            INDEX `idx_ai_event_camera_time`(`camera_id` ASC, `event_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'AI推理事件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 5、告警记录表
-- ----------------------------
DROP TABLE IF EXISTS `biz_alarm`;
CREATE TABLE `biz_alarm` (
                             `alarm_id` bigint NOT NULL AUTO_INCREMENT COMMENT '告警ID',
                             `event_id` bigint NULL DEFAULT NULL COMMENT '关联事件ID',
                             `camera_id` bigint NULL DEFAULT NULL COMMENT '摄像头ID',
                             `slot_id` bigint NULL DEFAULT NULL COMMENT '车位ID',
                             `alarm_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '告警等级',
                             `alarm_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '告警类型',
                             `alarm_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '告警状态(UNHANDLED/HANDLED)',
                             `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '告警图片URL',
                             `trigger_time` datetime NULL DEFAULT NULL COMMENT '触发时间',
                             `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标志(0存在 2删除)',
                             `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
                             `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
                             `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
                             PRIMARY KEY (`alarm_id`) USING BTREE,
                             -- 防止同一事件的重复告警（UNHANDLED 唯一）
                             UNIQUE INDEX `uk_biz_alarm_event_status`(`event_id` ASC, `alarm_status` ASC) USING BTREE,
                             INDEX `idx_biz_alarm_event_id`(`event_id` ASC) USING BTREE,
                             INDEX `idx_biz_alarm_level`(`alarm_level` ASC) USING BTREE,
                             INDEX `idx_biz_alarm_status_time`(`alarm_status` ASC, `trigger_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '告警记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 增量脚本：为已存在的 biz_alarm 表添加唯一约束（防止重复告警）
-- ----------------------------
ALTER TABLE `biz_alarm` ADD UNIQUE INDEX `uk_biz_alarm_event_status`(`event_id`, `alarm_status`);

-- ----------------------------
-- 6、告警等级枚举表
-- ----------------------------
DROP TABLE IF EXISTS `enum_alarm_level`;
CREATE TABLE `enum_alarm_level` (
                                    `level_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '等级编码',
                                    `level_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '等级名称',
                                    `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
                                    PRIMARY KEY (`level_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '告警等级枚举' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 7、事件类型枚举表
-- ----------------------------
DROP TABLE IF EXISTS `enum_event_type`;
CREATE TABLE `enum_event_type` (
                                   `event_type_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '事件编码',
                                   `event_type_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '事件名称',
                                   `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
                                   PRIMARY KEY (`event_type_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '事件类型枚举' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 8、设备状态枚举表
-- ----------------------------
DROP TABLE IF EXISTS `enum_device_status`;
CREATE TABLE `enum_device_status` (
                                      `status_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态编码',
                                      `status_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '状态名称',
                                      `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
                                      PRIMARY KEY (`status_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '设备状态枚举' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of enum_alarm_level
-- ----------------------------
INSERT INTO `enum_alarm_level` (`level_code`, `level_name`, `remark`) VALUES ('LOW', '低等级', '一般提示');
INSERT INTO `enum_alarm_level` (`level_code`, `level_name`, `remark`) VALUES ('MEDIUM', '中等级', '需要关注');
INSERT INTO `enum_alarm_level` (`level_code`, `level_name`, `remark`) VALUES ('HIGH', '高等级', '紧急处理');

-- ----------------------------
-- Records of enum_event_type
-- ----------------------------
INSERT INTO `enum_event_type` (`event_type_code`, `event_type_name`, `remark`) VALUES ('OCCUPY', '车位占用', '车辆进入车位');
INSERT INTO `enum_event_type` (`event_type_code`, `event_type_name`, `remark`) VALUES ('RELEASE', '车位释放', '车辆离开车位');

-- ----------------------------
-- Records of enum_device_status
-- ----------------------------
INSERT INTO `enum_device_status` (`status_code`, `status_name`, `remark`) VALUES ('ONLINE', '在线', '设备正常');
INSERT INTO `enum_device_status` (`status_code`, `status_name`, `remark`) VALUES ('OFFLINE', '离线', '设备异常');

SET FOREIGN_KEY_CHECKS = 1;
