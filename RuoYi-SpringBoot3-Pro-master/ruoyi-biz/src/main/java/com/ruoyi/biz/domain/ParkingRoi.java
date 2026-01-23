package com.ruoyi.biz.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serial;

/**
 * 车位ROI标注对象 biz_parking_roi
 *
 * @author aoshiyue
 * @date 2026-01-08
 */
@Data
@TableName("biz_parking_roi" )
public class ParkingRoi extends BaseEntity
        {
@Serial
private static final long serialVersionUID = 1L;

    /** ROI标注ID（主键） */
    @TableId(value = "parking_roi_id", type = IdType.AUTO)
    private Long parkingRoiId;
    public static final String PARKINGROIID = "parking_roi_id";

    /** 车位ID */
    @TableField("slot_id" )
    private Long slotId;
    public static final String SLOTID = "slot_id";

    /** 车位编号（来自 biz_parking_slot） */
    @Excel(name = "车位编号")
    @TableField("slot_code")
    private String slotCode;

    /** 摄像头ID（来自 biz_parking_slot） */
    @TableField(exist = false)
    private Long cameraId;

    /** 摄像头名称（来自 dev_camera） */
    @Excel(name = "摄像头名称")
    @TableField("camera_name")
    private String cameraName;

    /** 图片宽度 */
    @Excel(name = "图片宽度" )
    @TableField("image_width" )
    private Integer imageWidth;
    public static final String IMAGEWIDTH = "image_width";

    /** 图片高度 */
    @Excel(name = "图片高度" )
    @TableField("image_height" )
    private Integer imageHeight;
    public static final String IMAGEHEIGHT = "image_height";

    /** ROI多边形坐标(JSON) */
    @Excel(name = "ROI多边形坐标")
    @TableField("roi_polygon" )
    private String roiPolygon;
    public static final String ROIPOLYGON = "roi_polygon";

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
