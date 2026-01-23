package com.ruoyi.biz.service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.biz.domain .ParkingSlot;

/**
 * 车位信息Service接口
 *
 * @author aoshiyue
 * @date 2026-01-07
 */
public interface IParkingSlotService extends IService<ParkingSlot> {
    public IPage<ParkingSlot> pageParkingSlot(Page<ParkingSlot> page, QueryWrapper<ParkingSlot> queryWrapper);

    /**
     * 查询车位信息
     *
     * @param parkingSlotId 车位信息主键
     * @return 车位信息
     */
    public ParkingSlot selectParkingSlotByParkingSlotId(Long parkingSlotId);

    /**
     * 新增车位信息
     *
     * @param parkingSlot 车位信息
     * @return 结果
     */
    public int insertParkingSlot(ParkingSlot parkingSlot);

    /**
     * 修改车位信息
     *
     * @param parkingSlot 车位信息
     * @return 结果
     */
    public int updateParkingSlot(ParkingSlot parkingSlot);

    /**
     * 批量删除车位信息
     *
     * @param parkingSlotIds 需要删除的车位信息主键集合
     * @return 结果
     */
    public int deleteParkingSlotByParkingSlotIds(Long[] parkingSlotIds);

    /**
     * 删除车位信息信息
     *
     * @param parkingSlotId 车位信息主键
     * @return 结果
     */
    public int deleteParkingSlotByParkingSlotId(Long parkingSlotId);

    /**
     * 导入车位信息数据
     *
     * @param list            数据列表
     * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
     * @param operName        操作用户
     * @return 结果
     */
    public String importParkingSlot(
    List<ParkingSlot> list, int titleNum, Boolean
    isUpdateSupport,
    String operName);

    ParkingSlot selectByCameraAndSlotCode(Long cameraId, String slotCode);

}
