package com.ruoyi.common.constant;

/**
 * 告警相关常量
 */
public class AlarmConstants {

    /**
     * =========================
     * 1. 告警级别（Alarm Level）
     * =========================
     */
    public static class Level {
        public static final String LOW = "LOW";         // 低风险
        public static final String MEDIUM = "MEDIUM";   // 中风险
        public static final String HIGH = "HIGH";       // 高风险
        public static final String URGENT = "URGENT";   // 紧急
    }

    /**
     * =========================
     * 2. 告警类型（Alarm Type）
     * =========================
     */
    public static class Type {
        public static final String PARKING_OCCUPIED = "PARKING_OCCUPIED";     // 车位被占用
        public static final String PARKING_FREE = "PARKING_FREE";             // 车位释放
        public static final String ILLEGAL_PARKING = "ILLEGAL_PARKING";       // 非法停车
        public static final String LONG_TIME_OCCUPIED = "LONG_TIME_OCCUPIED"; // 超时占用
        public static final String SLOT_BLOCKED = "SLOT_BLOCKED";             // 车位遮挡
        public static final String CAMERA_OFFLINE = "CAMERA_OFFLINE";         // 摄像头离线
        public static final String CAMERA_ONLINE = "CAMERA_ONLINE";           // 摄像头恢复
    }

    /**
     * =========================
     * 3. 告警状态（Alarm Status）
     * =========================
     */
    public static class Status {
        public static final String UNHANDLED = "UNHANDLED"; // 未处理
        public static final String HANDLED = "HANDLED";     // 已处理
        public static final String IGNORED = "IGNORED";     // 已忽略
        public static final String RECOVERED = "RECOVERED"; // 自动恢复
    }

}
