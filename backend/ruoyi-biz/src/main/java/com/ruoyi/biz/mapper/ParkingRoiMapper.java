package com.ruoyi.biz.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.biz.domain.ParkingRoi;
import org.apache.ibatis.annotations.Param;

/**
 * 车位ROI标注Mapper接口
 *
 * @date 2026-01-08
 */
public interface ParkingRoiMapper extends BaseMapper<ParkingRoi> {
    IPage<ParkingRoi> selectPageWithRelation(
            Page<ParkingRoi> page,
            @Param("roi") ParkingRoi parkingRoi
    );

}
