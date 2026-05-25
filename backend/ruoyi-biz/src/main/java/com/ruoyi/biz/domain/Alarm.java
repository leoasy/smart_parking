package com.ruoyi.biz.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 告警记录对象 biz_alarm
 *
 * @date 2026-01-08
 */
@Data
@TableName("biz_alarm" )
public class Alarm extends BaseEntity
        {
private static final long serialVersionUID = 1L;

        /** 告警ID */
        @TableId(value = "alarm_id", type = IdType.AUTO)
        private Long alarmId;
        public static final String ALARMID = "alarm_id";

        /** 关联事件ID */
        @Excel(name = "关联事件ID" )
        @TableField("event_id" )
        private Long eventId;
        public static final String EVENTID = "event_id";

        /** 摄像头ID（内部关联） */
        @TableField("camera_id")
        @NotNull(message = "摄像头ID不能为空")
        private Long cameraId;

        /** 车位ID（内部关联） */
        @TableField("slot_id")
        @NotNull(message = "车位ID不能为空")
        private Long slotId;

        /** 告警等级 */
        @Excel(name = "告警等级" , dictType = "alarm_level" )
        @TableField("alarm_level" )
        @NotBlank(message = "告警等级不能为空")
        @Size(min = 0, max = 20, message = "告警等级长度不能超过20个字符")
        private String alarmLevel;
        public static final String ALARMLEVEL = "alarm_level";

        /** 告警类型 */
        @Excel(name = "告警类型" ,dictType = "alarm_type")
        @TableField("alarm_type" )
        @Size(min = 0, max = 50, message = "告警类型长度不能超过50个字符")
        private String alarmType;
        public static final String ALARMTYPE = "alarm_type";

        /** 告警状态*/
        @Excel(name = "告警状态" , dictType = "alarm_status" )
        @TableField("alarm_status" )
        @NotBlank(message = "告警状态不能为空")
        @Size(min = 0, max = 20, message = "告警状态长度不能超过20个字符")
        private String alarmStatus;
        public static final String ALARMSTATUS = "alarm_status";

        /** 触发时间 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss" )
        @Excel(name = "触发时间" , width = 30, dateFormat = "yyyy-MM-dd" )
        @TableField("trigger_time" )
        private Date triggerTime;
        public static final String TRIGGERTIME = "trigger_time";

        /** 删除标志(0存在 2删除) */
        @TableField("del_flag" )
        private String delFlag;
        public static final String DELFLAG = "del_flag";

        @TableField("image_url" )
        private String imageUrl;
        public static final String IMAGEURL = "image_url";

        /** 用户ID */
        @TableField(exist = false)
        private Long userId;
        public static final String USERID = "user_id";

        /** 部门ID */
        @TableField(exist = false)
        private Long deptId;
        public static final String DEPTID = "dept_id";

}
