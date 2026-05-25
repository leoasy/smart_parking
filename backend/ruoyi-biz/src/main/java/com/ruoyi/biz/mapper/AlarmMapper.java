package com.ruoyi.biz.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.biz.domain.Alarm;
import com.ruoyi.biz.vo.AlarmVo;
import org.apache.ibatis.annotations.Param;

/**
 * 告警记录Mapper接口
 *
 * @date 2026-01-08
 */
public interface AlarmMapper extends BaseMapper<Alarm> {
    /**
     * 查询告警列表，关联事件、摄像头和车位信息
     * @return 告警视图对象列表
     */
    List<AlarmVo> selectAlarmList();
    IPage<AlarmVo> selectPageAlarmListWithDetails(Page<AlarmVo> page, @Param("params") AlarmVo params);

}
