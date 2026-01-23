package com.ruoyi.biz.service.impl;

import lombok.RequiredArgsConstructor;

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
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.ruoyi.biz.mapper.ParkingRoiMapper;
import com.ruoyi.biz.domain.ParkingRoi;
import com.ruoyi.biz.service.IParkingRoiService;

import jakarta.validation.Validator;

/**
 * 车位ROI标注Service业务层处理
 *
 * @author aoshiyue
 * @date 2026-01-08
 */
@Service
@RequiredArgsConstructor
public class ParkingRoiServiceImpl extends ServiceImpl<ParkingRoiMapper,ParkingRoi> implements IParkingRoiService {
    private final ParkingRoiMapper parkingRoiMapper;
    protected final Validator validator;

    @Override
    public String importParkingRoi(
    List<ParkingRoi> list, int titleNum, Boolean
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
            ParkingRoi parkingRoi =list.get(i);
            try {
                QueryWrapper<ParkingRoi> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("camera_name", parkingRoi.getCameraName());
                queryWrapper.eq("slot_code", parkingRoi.getSlotCode());

                List<ParkingRoi> checkList = parkingRoiMapper.selectList(queryWrapper);

                if (checkList.isEmpty()) {
                    BeanValidators.validateWithException(validator, parkingRoi);
                    insertParkingRoi(parkingRoi);
                    successNum++;
                } else if (isUpdateSupport) {
                    BeanValidators.validateWithException(validator, parkingRoi);
                    parkingRoi.setParkingRoiId(checkList.get(0).getParkingRoiId());
                    updateParkingRoi(parkingRoi);
                    successNum++;
                } else {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、记录"
                            + (i + titleNum + 2) + " 已存在");
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
    public IPage<ParkingRoi> pageParkingRoi(Page<ParkingRoi> page, QueryWrapper<ParkingRoi> queryWrapper) {
        return parkingRoiMapper.selectPage(page, queryWrapper);
    }

    /**
     * 查询车位ROI标注
     *
     * @param parkingRoiId 车位ROI标注主键
     * @return 车位ROI标注
     */
    @Override
    public ParkingRoi selectParkingRoiByParkingRoiId(Long parkingRoiId) {
        return parkingRoiMapper.selectById(parkingRoiId);
    }

    /**
     * 新增车位ROI标注
     *
     * @param parkingRoi 车位ROI标注
     * @return 结果
     */
    @Override
    public int insertParkingRoi(ParkingRoi parkingRoi) {
                parkingRoi.setUserId(SecurityUtils.getUserId());
                parkingRoi.setDeptId(SecurityUtils.getDeptId());
                parkingRoi.setCreateBy(SecurityUtils.getUsername());
                parkingRoi.setCreateTime(DateUtils.getNowDate());
            return parkingRoiMapper.insert(parkingRoi);
    }

    /**
     * 修改车位ROI标注
     *
     * @param parkingRoi 车位ROI标注
     * @return 结果
     */
    @Override
    public int updateParkingRoi(ParkingRoi parkingRoi) {
                parkingRoi.setUpdateTime(DateUtils.getNowDate());
                parkingRoi.setUpdateBy(SecurityUtils.getUsername());
        return parkingRoiMapper.updateById(parkingRoi);
    }

    /**
     * 批量删除车位ROI标注
     *
     * @param parkingRoiIds 需要删除的车位ROI标注主键
     * @return 结果
     */
    @Override
    public int deleteParkingRoiByParkingRoiIds(Long[] parkingRoiIds) {
        return parkingRoiMapper.deleteByIds(Arrays.asList(parkingRoiIds));
    }

    /**
     * 删除车位ROI标注信息
     *
     * @param parkingRoiId 车位ROI标注主键
     * @return 结果
     */
    @Override
    public int deleteParkingRoiByParkingRoiId(Long parkingRoiId) {
        return parkingRoiMapper.deleteById(parkingRoiId);
    }

    @Override
    public IPage<ParkingRoi> pageParkingRoiWithRelation(
            Page<ParkingRoi> page,
            ParkingRoi parkingRoi
    ) {
        return parkingRoiMapper.selectPageWithRelation(page, parkingRoi);
    }

}
