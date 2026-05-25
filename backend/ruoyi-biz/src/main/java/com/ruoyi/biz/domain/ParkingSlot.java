package com.ruoyi.biz.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;


/**
 * 车位信息对象 biz_parking_slot
 *
 * @date 2026-01-07
 */
@Data
@TableName("biz_parking_slot" )
public class ParkingSlot extends BaseEntity
        {
private static final long serialVersionUID = 1L;

        /** 车位ID */
        @TableId(value = "parking_slot_id", type = IdType.AUTO)
        private Long parkingSlotId;
        public static final String PARKINGSLOTID = "parking_slot_id";

        /** 停车区域ID */
        @Excel(name = "停车区域ID" )
        @TableField("area_id" )
        @NotNull(message = "停车区域ID不能为空")
        private Long areaId;
        public static final String AREAID = "area_id";

        /** 车位编号 */
        @Excel(name = "车位编号" )
        @TableField("slot_code" )
        @NotBlank(message = "车位编号不能为空")
        @Size(min = 0, max = 50, message = "车位编号长度不能超过50个字符")
        private String slotCode;
        public static final String SLOTCODE = "slot_code";

        /** 车位状态 */
        @Excel(name = "车位状态" , dictType = "slot_status" )
        @TableField("slot_status" )
        @Size(min = 0, max = 20, message = "车位状态长度不能超过20个字符")
        private String slotStatus;
        public static final String SLOTSTATUS = "slot_status";

        /** 绑定摄像头ID*/
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @Excel(name = "摄像头ID" )
        @TableField("camera_id" )
        private Long cameraId;
        public static final String CAMERAID = "camera_id";


        /** 删除标志(0存在 2删除) */
        @TableField("del_flag" )
        @TableLogic
        private String delFlag;
        public static final String DELFLAG = "del_flag";
        public static final String CREATETIME = "create_time";

        /** 用户ID */
        @TableField(exist = false)
        private Long userId;
        public static final String USERID = "user_id";

            /** 部门ID */
            @TableField(exist = false)
        private Long deptId;
        public static final String DEPTID = "dept_id";

}
