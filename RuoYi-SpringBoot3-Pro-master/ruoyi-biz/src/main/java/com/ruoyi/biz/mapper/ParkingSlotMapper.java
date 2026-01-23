package com.ruoyi.biz.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.biz.domain.ParkingSlot;
import io.lettuce.core.dynamic.annotation.Param;

/**
 * 车位信息Mapper接口
 *
 * @author aoshiyue
 * @date 2026-01-07
 */
public interface ParkingSlotMapper extends BaseMapper<ParkingSlot> {
    /**
     * 根据 cameraId + slotCode 查询车位
     */
    ParkingSlot selectByCameraAndSlotCode(
            @Param("cameraId") Long cameraId,
            @Param("slotCode") String slotCode
    );
}
