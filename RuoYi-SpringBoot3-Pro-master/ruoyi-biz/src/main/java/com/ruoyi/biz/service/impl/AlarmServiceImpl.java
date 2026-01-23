package com.ruoyi.biz.service.impl;

import com.ruoyi.biz.vo.AlarmVo;
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
import com.ruoyi.biz.mapper.AlarmMapper;
import com.ruoyi.biz.domain.Alarm;
import com.ruoyi.biz.service.IAlarmService;

import jakarta.validation.Validator;

/**
 * 告警记录Service业务层处理
 *
 * @author aoshiyue
 * @date 2026-01-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmServiceImpl extends ServiceImpl<AlarmMapper,Alarm> implements IAlarmService {
    private final AlarmMapper alarmMapper;
    protected final Validator validator;

    @Override
    public String importAlarm(
    List<Alarm> list, int titleNum, Boolean
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
            Alarm alarm =list.get(i);
            try {
                QueryWrapper<Alarm> queryWrapper = new QueryWrapper<>();
                List<Alarm> checkList = new ArrayList<>(); //alarmMapper.selectList(queryWrapper);
                if (checkList.size() == 0) {
                    BeanValidators.validateWithException(validator, alarm);
                    insertAlarm(alarm);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、记录" + (i + titleNum + 2) + "：" + alarm.getEventId() + " 导入成功")
                    ;
                } else if (isUpdateSupport) {
                    BeanValidators.validateWithException(validator, alarm);
                    alarm.setEventId(checkList.get(0).getEventId());
                    updateAlarm(alarm);
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
    public IPage<AlarmVo> selectPageAlarmListWithDetails(Page<AlarmVo> page, AlarmVo params) {
        // 调用 Mapper 层的分页查询方法
        // params 会被 MyBatis 作为 #{} 参数传递给 XML 中的 <if test="params.xxx != null"> 条件判断
        return alarmMapper.selectPageAlarmListWithDetails(page, params);
    }
    @Override
    public IPage<Alarm> pageAlarm(Page<Alarm> page, QueryWrapper<Alarm> queryWrapper) {
        return alarmMapper.selectPage(page, queryWrapper);
    }

    /**
     * 查询告警记录
     *
     * @param alarmId 告警记录主键
     * @return 告警记录
     */
    @Override
    public Alarm selectAlarmByAlarmId(Long alarmId) {
        return alarmMapper.selectById(alarmId);
    }

    /**
     * 新增告警记录
     *
     * @param alarm 告警记录
     * @return 结果
     */
//    @Override
//    public int insertAlarm(Alarm alarm) {
//                alarm.setUserId(SecurityUtils.getUserId());
//                alarm.setDeptId(SecurityUtils.getDeptId());
//                alarm.setCreateBy(SecurityUtils.getUsername());
//                alarm.setCreateTime(DateUtils.getNowDate());
//            return alarmMapper.insert(alarm);
//    }
    @Override
    public int insertAlarm(Alarm alarm) {

        Long userId;
        Long deptId;
        String username;

        try {
            userId = SecurityUtils.getUserId();
            deptId = SecurityUtils.getDeptId();
            username = SecurityUtils.getUsername();
        } catch (Exception e) {
            // ⭐ 无登录态兜底（非常关键）
            userId = 1L;
            deptId = 1L;
            username = "system";
        }

        alarm.setUserId(userId);
        alarm.setDeptId(deptId);
        alarm.setCreateBy(username);
        alarm.setCreateTime(DateUtils.getNowDate());

        return alarmMapper.insert(alarm);
    }




    /**
     * 修改告警记录
     *
     * @param alarm 告警记录
     * @return 结果
     */
    @Override
    public int updateAlarm(Alarm alarm) {
                alarm.setUpdateTime(DateUtils.getNowDate());
                alarm.setUpdateBy(SecurityUtils.getUsername());
        return alarmMapper.updateById(alarm);
    }

    /**
     * 批量删除告警记录
     *
     * @param alarmIds 需要删除的告警记录主键
     * @return 结果
     */
    @Override
    public int deleteAlarmByAlarmIds(Long[] alarmIds) {
        return alarmMapper.deleteByIds(Arrays.asList(alarmIds));
    }

    /**
     * 删除告警记录信息
     *
     * @param alarmId 告警记录主键
     * @return 结果
     */
    @Override
    public int deleteAlarmByAlarmId(Long alarmId) {
        return alarmMapper.deleteById(alarmId);
    }

    @Override
    public Alarm selectUnhandledByEventId(Long eventId) {
        return alarmMapper.selectOne(
                new QueryWrapper<Alarm>()
                        .eq("event_id", eventId)
                        .eq("alarm_status", "UNHANDLED")
                        .eq("del_flag", "0")
                        .last("limit 1")
        );
    }

    @Override
    public int autoClearByEventId(Long eventId) {
        Alarm alarm = selectUnhandledByEventId(eventId);
        if (alarm == null) {
            return 0;
        }
        alarm.setAlarmStatus("AUTO_CLEARED");
        alarm.setUpdateBy(SecurityUtils.getUsername());
        alarm.setUpdateTime(DateUtils.getNowDate());
        return alarmMapper.updateById(alarm);
    }


}
