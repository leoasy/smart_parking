package com.ruoyi.biz.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;

/**
 * 摄像头设备对象 dev_camera
 *
 * @author 姜彦汐
 * @site https://www.undsky.com
 * @date 2026-01-08
 */
@Data
@TableName("dev_camera" )
public class DevCamera extends BaseEntity
        {
private static final long serialVersionUID = 1L;

            /** 摄像头ID */
            @JsonSerialize(using = ToStringSerializer.class)
            @TableId(value = "camera_id", type = IdType.ASSIGN_ID)
        private Long cameraId;
        public static final String CAMERAID = "camera_id";

            /** 摄像头名称 */
                @Excel(name = "摄像头名称" )
            @TableField("camera_name" )
            @NotBlank(message = "摄像头名称不能为空")
            @Size(min = 0, max = 100, message = "摄像头名称长度不能超过100个字符")
        private String cameraName;
        public static final String CAMERANAME = "camera_name";

            /** RTSP地址 */
            @TableField("rtsp_url" )
            @NotBlank(message = "RTSP地址不能为空")
            @Size(min = 0, max = 255, message = "RTSP地址长度不能超过255个字符")
        private String rtspUrl;
        public static final String RTSPURL = "rtsp_url";

            /** 设备状态(ONLINE/OFFLINE) */
                @Excel(name = "设备状态(ONLINE/OFFLINE)" , dictType = "device_status" )
            @TableField("camera_status" )
            @NotBlank(message = "设备状态(ONLINE/OFFLINE)不能为空")
            @Size(min = 0, max = 20, message = "设备状态(ONLINE/OFFLINE)长度不能超过20个字符")
        private String cameraStatus;
        public static final String CAMERASTATUS = "camera_status";

            /** 最后心跳时间 */
                @JsonFormat(pattern = "yyyy-MM-dd" )
                @Excel(name = "最后心跳时间" , width = 30, dateFormat = "yyyy-MM-dd" )
            @TableField("last_heartbeat" )
        private Date lastHeartbeat;
        public static final String LASTHEARTBEAT = "last_heartbeat";

            /** 安装位置 */
                @Excel(name = "安装位置" )
            @TableField("location" )
            @Size(min = 0, max = 255, message = "安装位置长度不能超过255个字符")
        private String location;
        public static final String LOCATION = "location";

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
