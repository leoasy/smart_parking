package com.ruoyi.biz.service;

import java.util.List;
import java.util.function.Consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.biz.domain .DevCamera;

/**
 * 摄像头设备Service接口
 *
 * @date 2026-01-08
 */
public interface IDevCameraService extends IService<DevCamera> {
    IPage<DevCamera> pageDevCamera(IPage<DevCamera> page, Consumer<LambdaQueryWrapper<DevCamera>> consumer);

    /**
     * 查询摄像头设备
     *
     * @param cameraId 摄像头设备主键
     * @return 摄像头设备
     */
    public DevCamera selectDevCameraByCameraId(Long cameraId);

    /**
     * 新增摄像头设备
     *
     * @param devCamera 摄像头设备
     * @return 结果
     */
    public int insertDevCamera(DevCamera devCamera);

    /**
     * 修改摄像头设备
     *
     * @param devCamera 摄像头设备
     * @return 结果
     */
    public int updateDevCamera(DevCamera devCamera);

    /**
     * 批量删除摄像头设备
     *
     * @param cameraIds 需要删除的摄像头设备主键集合
     * @return 结果
     */
    public int deleteDevCameraByCameraIds(Long[] cameraIds);

    /**
     * 删除摄像头设备信息
     *
     * @param cameraId 摄像头设备主键
     * @return 结果
     */
    public int deleteDevCameraByCameraId(Long cameraId);

    /**
     * 导入摄像头设备数据
     *
     * @param list            数据列表
     * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
     * @param operName        操作用户
     * @return 结果
     */
    public String importDevCamera(
    List<DevCamera> list, int titleNum, Boolean
    isUpdateSupport,
    String operName);
}
