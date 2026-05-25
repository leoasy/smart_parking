package com.ruoyi.biz.service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.biz.domain .ParkingRoi;

/**
 * 车位ROI标注Service接口
 *
 * @date 2026-01-08
 */
public interface IParkingRoiService extends IService<ParkingRoi> {
    public IPage<ParkingRoi> pageParkingRoi(Page<ParkingRoi> page, QueryWrapper<ParkingRoi> queryWrapper);

    /**
     * 查询车位ROI标注
     *
     * @param parkingRoiId 车位ROI标注主键
     * @return 车位ROI标注
     */
    public ParkingRoi selectParkingRoiByParkingRoiId(Long parkingRoiId);

    /**
     * 新增车位ROI标注
     *
     * @param parkingRoi 车位ROI标注
     * @return 结果
     */
    public int insertParkingRoi(ParkingRoi parkingRoi);

    /**
     * 修改车位ROI标注
     *
     * @param parkingRoi 车位ROI标注
     * @return 结果
     */
    public int updateParkingRoi(ParkingRoi parkingRoi);

    /**
     * 批量删除车位ROI标注
     *
     * @param parkingRoiIds 需要删除的车位ROI标注主键集合
     * @return 结果
     */
    public int deleteParkingRoiByParkingRoiIds(Long[] parkingRoiIds);

    /**
     * 删除车位ROI标注信息
     *
     * @param parkingRoiId 车位ROI标注主键
     * @return 结果
     */
    public int deleteParkingRoiByParkingRoiId(Long parkingRoiId);

    /**
     * 导入车位ROI标注数据
     *
     * @param list            数据列表
     * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
     * @param operName        操作用户
     * @return 结果
     */
    public String importParkingRoi(
    List<ParkingRoi> list, int titleNum, Boolean
    isUpdateSupport,
    String operName);

    /**
     * 查询车位ROI标注（含车位编号、摄像头信息）
     */
    IPage<ParkingRoi> pageParkingRoiWithRelation(
            Page<ParkingRoi> page,
            ParkingRoi parkingRoi
    );

}
