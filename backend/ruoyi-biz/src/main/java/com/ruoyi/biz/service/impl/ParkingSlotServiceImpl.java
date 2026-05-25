package com.ruoyi.biz.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.BeanValidators;
import com.ruoyi.common.exception.ServiceException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.mapper.ParkingSlotMapper;
import com.ruoyi.biz.domain.ParkingSlot;
import com.ruoyi.biz.service.IParkingSlotService;

import jakarta.validation.Validator;

/**
 * 车位信息Service业务层处理
 *
 * @date 2026-01-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingSlotServiceImpl extends ServiceImpl<ParkingSlotMapper,ParkingSlot> implements IParkingSlotService {
    private final ParkingSlotMapper parkingSlotMapper;
    protected final Validator validator;

    @Override
    public String importParkingSlot(
    List<ParkingSlot> list, int titleNum, Boolean
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
            ParkingSlot parkingSlot =list.get(i);
            try {
                QueryWrapper<ParkingSlot> queryWrapper = new QueryWrapper<>();
                List<ParkingSlot> checkList = new ArrayList<>(); //parkingSlotMapper.selectList(queryWrapper);
                if (checkList.size() == 0) {
                    BeanValidators.validateWithException(validator, parkingSlot);
                    insertParkingSlot(parkingSlot);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、记录" + (i + titleNum + 2) + "：" + parkingSlot.getParkingSlotId() + " 导入成功")
                    ;
                } else if (isUpdateSupport) {
                    BeanValidators.validateWithException(validator, parkingSlot);
                    parkingSlot.setParkingSlotId(checkList.get(0).getParkingSlotId());
                    updateParkingSlot(parkingSlot);
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
    public IPage<ParkingSlot> pageParkingSlot(Page<ParkingSlot> page, QueryWrapper<ParkingSlot> queryWrapper) {
        return parkingSlotMapper.selectPage(page, queryWrapper);
    }


    /**
     * 查询车位信息
     *
     * @param parkingSlotId 车位信息主键
     * @return 车位信息
     */
    @Override
    public ParkingSlot selectParkingSlotByParkingSlotId(Long parkingSlotId) {
        return parkingSlotMapper.selectById(parkingSlotId);
    }

    /**
     * 新增车位信息
     *
     * @param parkingSlot 车位信息
     * @return 结果
     */
    @Override
    public int insertParkingSlot(ParkingSlot parkingSlot) {
                parkingSlot.setUserId(SecurityUtils.getUserId());
                parkingSlot.setDeptId(SecurityUtils.getDeptId());
                parkingSlot.setCreateBy(SecurityUtils.getUsername());
                parkingSlot.setCreateTime(DateUtils.getNowDate());
            return parkingSlotMapper.insert(parkingSlot);
    }

    /**
     * 修改车位信息
     *
     * @param parkingSlot 车位信息
     * @return 结果
     */
//    @Override
//    public int updateParkingSlot(ParkingSlot parkingSlot) {
//                parkingSlot.setUpdateTime(DateUtils.getNowDate());
//                parkingSlot.setUpdateBy(SecurityUtils.getUsername());
//        return parkingSlotMapper.updateById(parkingSlot);
//    }

    @Override
    public int updateParkingSlot(ParkingSlot parkingSlot) {
        parkingSlot.setUpdateTime(DateUtils.getNowDate());

        Long userId;
        try {
            userId = SecurityUtils.getUserId();
        } catch (Exception e) {
            // 兜底逻辑：如果是内部调用或未登录，可设置为默认值（如 -1L 或 null）
            userId = -1L;
            // 或者记录日志
            // log.warn("获取用户ID失败，采用默认值", e);
        }

        // 2. 获取用户名 (如果需要)
        String username;
        try {
            username = SecurityUtils.getUsername();
        } catch (Exception e) {
            username = "system";
        }

        parkingSlot.setUpdateBy(username);
        return parkingSlotMapper.updateById(parkingSlot);
    }

    /**
     * 批量删除车位信息
     *
     * @param parkingSlotIds 需要删除的车位信息主键
     * @return 结果
     */
    @Override
    public int deleteParkingSlotByParkingSlotIds(Long[] parkingSlotIds) {
        return parkingSlotMapper.deleteByIds(Arrays.asList(parkingSlotIds));
    }

    /**
     * 删除车位信息信息
     *
     * @param parkingSlotId 车位信息主键
     * @return 结果
     */
    @Override
    public int deleteParkingSlotByParkingSlotId(Long parkingSlotId) {
        return parkingSlotMapper.deleteById(parkingSlotId);
    }

    @Override
    public ParkingSlot selectByCameraAndSlotCode(Long cameraId, String slotCode) {
        return parkingSlotMapper.selectByCameraAndSlotCode(cameraId, slotCode);
    }

}
