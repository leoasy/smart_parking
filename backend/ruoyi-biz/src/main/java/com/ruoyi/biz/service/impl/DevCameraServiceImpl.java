package com.ruoyi.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.BeanValidators;
import com.ruoyi.common.exception.ServiceException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.mapper.DevCameraMapper;
import com.ruoyi.biz.domain.DevCamera;
import com.ruoyi.biz.service.IDevCameraService;

import jakarta.validation.Validator;

/**
 * 摄像头设备Service业务层处理
 *
 * @date 2026-01-08
 */
@Service
@RequiredArgsConstructor
public class DevCameraServiceImpl extends ServiceImpl<DevCameraMapper,DevCamera> implements IDevCameraService {
    private final DevCameraMapper devCameraMapper;
    protected final Validator validator;

    @Override
    public String importDevCamera(
    List<DevCamera> list, int titleNum, Boolean
    isUpdateSupport,
    String operName)

    {
        if (StringUtils.isNull(list) || list.size() == 0) {
            throw new ServiceException("导入数据不能为空！" );
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            DevCamera devCamera =list.get(i);
            try {
                QueryWrapper<DevCamera> queryWrapper = new QueryWrapper<>();
                List<DevCamera> checkList = new ArrayList<>(); //devCameraMapper.selectList(queryWrapper);
                if (checkList.size() == 0) {
                    BeanValidators.validateWithException(validator, devCamera);
                    insertDevCamera(devCamera);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、记录" + (i + titleNum + 2) + "：" + devCamera.getCameraId()
                            + " 导入成功")
                    ;
                } else if (isUpdateSupport) {
                    BeanValidators.validateWithException(validator, devCamera);
                    devCamera.setCameraId(checkList.get(0).getCameraId()
                    );
                    updateDevCamera(devCamera);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、记录" + (i + titleNum + 2) + " 更新成功")
                    ;
                } else {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、记录" + (i + titleNum + 2) + " 已存在")
                    ;
                }
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + "、记录" + (i + titleNum + 2) + " 导入失败：";
                failureMsg.append(msg + e.getMessage());
                log.error(msg, e);
            }
        }
        if (failureNum > 0) {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：" );
            throw new ServiceException(failureMsg.toString());
        } else {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：" );
        }
        return successMsg.toString();
    }

    @Override
    public IPage<DevCamera> pageDevCamera(IPage<DevCamera> page, Consumer<LambdaQueryWrapper<DevCamera>> consumer) {
        LambdaQueryWrapper<DevCamera> wrapper = new LambdaQueryWrapper<>();
        consumer.accept(wrapper);
        return this.page(page, wrapper);
    }

    /**
     * 查询摄像头设备
     *
     * @param cameraId 摄像头设备主键
     * @return 摄像头设备
     */
    @Override
    public DevCamera selectDevCameraByCameraId(Long cameraId) {
        return devCameraMapper.selectById(cameraId);
    }

    /**
     * 新增摄像头设备
     *
     * @param devCamera 摄像头设备
     * @return 结果
     */
    @Override
    public int insertDevCamera(DevCamera devCamera) {
                devCamera.setUserId(SecurityUtils.getUserId());
                devCamera.setDeptId(SecurityUtils.getDeptId());
                devCamera.setCreateBy(SecurityUtils.getUsername());
                devCamera.setCreateTime(DateUtils.getNowDate());
            return devCameraMapper.insert(devCamera);
    }

    /**
     * 修改摄像头设备
     *
     * @param devCamera 摄像头设备
     * @return 结果
     */
    @Override
    public int updateDevCamera(DevCamera devCamera) {
                devCamera.setUpdateTime(DateUtils.getNowDate());
                devCamera.setUpdateBy(SecurityUtils.getUsername());
        return devCameraMapper.updateById(devCamera);
    }

    /**
     * 批量删除摄像头设备
     *
     * @param cameraIds 需要删除的摄像头设备主键
     * @return 结果
     */
    @Override
    public int deleteDevCameraByCameraIds(Long[] cameraIds) {
        return devCameraMapper.deleteByIds(Arrays.asList(cameraIds));
    }

    /**
     * 删除摄像头设备信息
     *
     * @param cameraId 摄像头设备主键
     * @return 结果
     */
    @Override
    public int deleteDevCameraByCameraId(Long cameraId) {
        return devCameraMapper.deleteById(cameraId);
    }
}
