package com.ruoyi.biz.service.impl;

import com.ruoyi.biz.domain.Alarm;
import com.ruoyi.biz.domain.DevCamera;
import com.ruoyi.biz.domain.ParkingSlot;
import com.ruoyi.biz.kafka.KafkaProducerService;
import com.ruoyi.biz.mapper.AlarmMapper;
import com.ruoyi.biz.mapper.DevCameraMapper;
import com.ruoyi.biz.mapper.ParkingSlotMapper;
import com.ruoyi.biz.service.IAlarmService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
import com.ruoyi.biz.mapper.AiEventMapper;
import com.ruoyi.biz.domain.AiEvent;
import com.ruoyi.biz.service.IAiEventService;
import com.ruoyi.common.constant.AlarmConstants;

import jakarta.validation.Validator;

/**
 * AI推理事件Service业务层处理
 *
 * @date 2026-01-08
 */
@Service
@RequiredArgsConstructor
public class AiEventServiceImpl extends ServiceImpl<AiEventMapper,AiEvent> implements IAiEventService {
    private final IAlarmService alarmService;
    private final AiEventMapper aiEventMapper;
    private final ParkingSlotMapper parkingSlotMapper;
    private final AlarmMapper alarmMapper;
    private final DevCameraMapper devCameraMapper;
    protected final Validator validator;
    private final KafkaProducerService kafkaProducerService;

    @Override
    public String importAiEvent(
    List<AiEvent> list, int titleNum, Boolean
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
        // 批量查询所有 event_id，一次查询避免 N+1
        List<Long> eventIds = list.stream().map(AiEvent::getEventId).collect(Collectors.toList());
        QueryWrapper<AiEvent> batchWrapper = new QueryWrapper<>();
        batchWrapper.in("event_id", eventIds).eq("del_flag", "0");
        List<AiEvent> existingList = aiEventMapper.selectList(batchWrapper);
        Set<Long> existingEventIds = existingList.stream().map(AiEvent::getEventId).collect(Collectors.toSet());
        Map<Long, AiEvent> existingEventMap = existingList.stream().collect(Collectors.toMap(AiEvent::getEventId, e -> e));

        for (int i = 0; i < list.size(); i++) {
            AiEvent aiEvent = list.get(i);
            try {
                // 检查是否已存在相同 event_id 的记录
                if (!existingEventIds.contains(aiEvent.getEventId())) {
                    BeanValidators.validateWithException(validator, aiEvent);
                    insertAiEvent(aiEvent);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、记录" + (i + titleNum + 2) + "：" + aiEvent.getEventId() + " 导入成功")
                    ;
                } else if (isUpdateSupport) {
                    BeanValidators.validateWithException(validator, aiEvent);
                    aiEvent.setEventId(existingEventMap.get(aiEvent.getEventId()).getEventId());
                    updateAiEvent(aiEvent);
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
    public IPage<AiEvent> pageAiEvent(Page<AiEvent> page, QueryWrapper<AiEvent> queryWrapper) {
        return aiEventMapper.selectPage(page, queryWrapper);
    }

    /**
     * 查询AI推理事件
     *
     * @param eventId AI推理事件主键
     * @return AI推理事件
     */
    @Override
    public AiEvent selectAiEventByEventId(Long eventId) {
        return aiEventMapper.selectById(eventId);
    }

    /**
     * 新增AI推理事件
     *
     * @param aiEvent AI推理事件
     * @return 结果
     */
    @Override
    public int insertAiEvent(AiEvent aiEvent) {
        aiEvent.setCreateTime(DateUtils.getNowDate());

        Long userId;
        Long deptId;
        String username;

        try {
            userId = SecurityUtils.getUserId();
            deptId = SecurityUtils.getDeptId();
            username = SecurityUtils.getUsername();
        } catch (Exception e) {
            userId = 1L;
            deptId = 1L;
            username = "system";
        }

        aiEvent.setUserId(userId);
        aiEvent.setDeptId(deptId);
        aiEvent.setCreateBy(username);

        int rows = aiEventMapper.insert(aiEvent);
        kafkaProducerService.sendAiEvent(aiEvent);

        return rows;
    }

    /**
     * 修改AI推理事件
     *
     * @param aiEvent AI推理事件
     * @return 结果
     */
    @Override
    public int updateAiEvent(AiEvent aiEvent) {
        aiEvent.setUpdateTime(DateUtils.getNowDate());
        aiEvent.setUpdateBy(SecurityUtils.getUsername());
        return aiEventMapper.updateById(aiEvent);
    }

    /**
     * 批量删除AI推理事件
     *
     * @param eventIds 需要删除的AI推理事件主键
     * @return 结果
     */
    @Override
    public int deleteAiEventByEventIds(Long[] eventIds) {
        return aiEventMapper.deleteByIds(Arrays.asList(eventIds));
    }

    /**
     * 删除AI推理事件信息
     *
     * @param eventId AI推理事件主键
     * @return 结果
     */
    @Override
    public int deleteAiEventByEventId(Long eventId) {
        return aiEventMapper.deleteById(eventId);
    }

    public Map<String, Object> dashboard() {
        Map<String, Object> map = new HashMap<>();

        long totalSlots = parkingSlotMapper.selectCount(new QueryWrapper<ParkingSlot>().eq("del_flag", "0"));
        long occupiedSlots = parkingSlotMapper.selectCount(
                new QueryWrapper<ParkingSlot>().eq("slot_status", "OCCUPIED").eq("del_flag", "0")
        );
        long freeSlots = totalSlots - occupiedSlots;

        LocalDate today = LocalDate.now();
        LocalDate statDate = resolveDashboardStatDate(today);
        boolean usingLatestDataDate = !today.equals(statDate);
        long todayAlarms = countAlarmsByDate(statDate);

        long cameraOnline = devCameraMapper.selectCount(
                new QueryWrapper<DevCamera>().eq("camera_status", "ONLINE")
        );
        long cameraOffline = devCameraMapper.selectCount(
                new QueryWrapper<DevCamera>().eq("camera_status", "OFFLINE")
        );

        List<AiEvent> events = aiEventMapper.selectList(
                new QueryWrapper<AiEvent>().eq("del_flag", "0").orderByDesc("event_time").last("limit 10")
        );
        map.put("totalSlots", totalSlots);
        map.put("occupiedSlots", occupiedSlots);
        map.put("freeSlots", freeSlots);
        map.put("todayAlarms", todayAlarms);
        map.put("statDate", statDate.toString());
        map.put("usingLatestDataDate", usingLatestDataDate);

        map.put("cameraOnline", cameraOnline);
        map.put("cameraOffline", cameraOffline);

        // 最近7天告警数据统计
        List<String> days = new ArrayList<>();
        List<Long> alarmCounts = new ArrayList<>();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = statDate.minusDays(i);
            days.add(date.format(dayFormatter));
            alarmCounts.add(countAlarmsByDate(date));
        }

        map.put("days", days);
        map.put("alarmCounts", alarmCounts);

        map.put("events", events);

        return map;
    }

    private LocalDate resolveDashboardStatDate(LocalDate today) {
        if (countAlarmsByDate(today) > 0) {
            return today;
        }
        Alarm latestAlarm = alarmMapper.selectOne(
                new QueryWrapper<Alarm>()
                        .eq("del_flag", "0")
                        .isNotNull("trigger_time")
                        .orderByDesc("trigger_time")
                        .last("limit 1")
        );
        if (latestAlarm == null || latestAlarm.getTriggerTime() == null) {
            return today;
        }
        return latestAlarm.getTriggerTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private Long countAlarmsByDate(LocalDate date) {
        return alarmMapper.selectCount(
                new QueryWrapper<Alarm>()
                        .ge("trigger_time", date.atStartOfDay())
                        .lt("trigger_time", date.plusDays(1).atStartOfDay())
                        .eq("del_flag", "0")
        );
    }
}
