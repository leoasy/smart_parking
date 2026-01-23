package com.ruoyi.biz.domain;

import java.math.BigDecimal;
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
 * AI推理事件对象 ai_event
 *
 * @author aoshiyue
 * @date 2026-01-08
 */
@Data
@TableName("ai_event" )
public class AiEvent extends BaseEntity
        {
private static final long serialVersionUID = 1L;

        /** 事件ID */
        @TableId(value = "event_id", type = IdType.AUTO)
        private Long eventId;
        public static final String EVENTID = "event_id";

        /** 摄像头ID */
        @TableField("camera_id" )
        @NotNull(message = "摄像头ID不能为空")
        private Long cameraId;
        public static final String CAMERAID = "camera_id";

        /** 摄像头名称*/
        @Excel(name = "摄像头名称")
        @TableField("camera_name")
        @Size(max = 100, message = "摄像头名称长度不能超过100个字符")
        private String cameraName;

        /** 车位ID */
        @TableField("slot_id" )
        private Long slotId;
        public static final String SLOTID = "slot_id";

        /** 车位编号*/
        @Excel(name = "车位编号")
        @TableField("slot_code")
        @Size(max = 64, message = "车位编号长度不能超过64个字符")
        private String slotCode;

        /** 变更前状态 */
        @Excel(name = "变更前状态" , dictType = "ai_event_type" )
        @TableField("old_status" )
        @Size(min = 0, max = 20, message = "变更前状态长度不能超过20个字符")
        private String oldStatus;
        public static final String OLDSTATUS = "old_status";

        /** 变更后状态 */
        @Excel(name = "变更后状态" , dictType = "ai_event_type" )
        @TableField("new_status" )
        @Size(min = 0, max = 20, message = "变更后状态长度不能超过20个字符")
        private String newStatus;
        public static final String NEWSTATUS = "new_status";

        /** 置信度 */
        @Excel(name = "置信度" )
        @TableField("confidence" )
        private BigDecimal confidence;
        public static final String CONFIDENCE = "confidence";

        /** 事件发生时间 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // <--- 修改这里
        @Excel(name = "事件发生时间", width = 30, dateFormat = "yyyy-MM-dd")
        @TableField("event_time")
        @NotNull(message = "事件发生时间不能为空")
        private Date eventTime;
        public static final String EVENTTIME = "event_time";

        /** 关键帧图片路径 */
        @TableField("frame_path" )
        @Size(min = 0, max = 255, message = "关键帧图片路径长度不能超过255个字符")
        private String framePath;
        public static final String FRAMEPATH = "frame_path";

        /** 删除标志(0存在 2删除) */
        @TableField("del_flag" )
        private String delFlag;
        public static final String DELFLAG = "del_flag";

        /** 用户ID */
        @TableField(exist = false)
        private Long userId;
        public static final String USERID = "user_id";

        /** 部门ID */
        @TableField(exist = false)
        private Long deptId;
        public static final String DEPTID = "dept_id";

}
