package com.ruoyi.biz.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 告警记录视图对象 biz_alarm
 *
 * @author aoshiyue
 * @date 2026-01-08
 */
public class AlarmVo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 告警记录主键 */
    private Long alarmId;

    /** 事件ID */
    private Long eventId;

    /** 告警级别 */
    @Excel(name = "告警级别")
    private String alarmLevel;

    /** 告警类型 */
    @Excel(name = "告警类型")
    private String alarmType;

    /** 告警状态 */
    @Excel(name = "告警状态")
    private String alarmStatus;

    /** 触发时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "触发时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date triggerTime;

    // --- 添加用于查询条件的字段 ---
    /** 开始触发时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date beginTriggerTime;

    /** 结束触发时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTriggerTime;
    // --- 添加结束 ---

    // --- 关联查询字段 ---
    /** 车位编号 */
    private String slotCode;

    /** 摄像头名称 */
    private String cameraName;

    /** 车位ID */
    private Long slotId;

    /** 摄像头ID */
    private Long cameraId;
    // --- 关联查询字段结束 ---

    // --- Getters and Setters ---
    public Long getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(Long alarmId) {
        this.alarmId = alarmId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarmLevel(String alarmLevel) {
        this.alarmLevel = alarmLevel;
    }

    public String getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(String alarmType) {
        this.alarmType = alarmType;
    }

    public String getAlarmStatus() {
        return alarmStatus;
    }

    public void setAlarmStatus(String alarmStatus) {
        this.alarmStatus = alarmStatus;
    }

    public Date getTriggerTime() {
        return triggerTime;
    }

    public void setTriggerTime(Date triggerTime) {
        this.triggerTime = triggerTime;
    }

    // --- Getter and Setter for beginTriggerTime and endTriggerTime ---
    public Date getBeginTriggerTime() {
        return beginTriggerTime;
    }

    public void setBeginTriggerTime(Date beginTriggerTime) {
        this.beginTriggerTime = beginTriggerTime;
    }

    public Date getEndTriggerTime() {
        return endTriggerTime;
    }

    public void setEndTriggerTime(Date endTriggerTime) {
        this.endTriggerTime = endTriggerTime;
    }
    // --- End of Getter and Setter for time range ---

    // --- Getter and Setter for关联查询字段 ---
    public String getSlotCode() {
        return slotCode;
    }

    public void setSlotCode(String slotCode) {
        this.slotCode = slotCode;
    }

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public Long getCameraId() {
        return cameraId;
    }

    public void setCameraId(Long cameraId) {
        this.cameraId = cameraId;
    }
    // --- End of Getter and Setter for关联查询字段 ---

}