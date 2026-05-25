package com.ruoyi.biz.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ruoyi.biz.domain.AiEvent;
import com.ruoyi.biz.service.impl.AiEventServiceImpl;
import com.ruoyi.common.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.ArrayList;

import org.springframework.validation.annotation.Validated;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.common.annotation.RepeatSubmit;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.biz.vo.AlarmVo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.biz.domain.Alarm;
import com.ruoyi.biz.service.IAlarmService;
import com.ruoyi.biz.mapper.AlarmMapper;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
/**
 * 告警记录Controller
 *
 * @date 2026-01-08
 */
@RestController
@RequestMapping("/biz/Alarm")
@RequiredArgsConstructor
public class AlarmController extends BaseController {
    private final IAlarmService alarmService;
    private final AiEventServiceImpl aiEventServiceImpl;
    // private final AlarmMapper alarmMapper;

/**
 * 查询告警记录列表
 */
@PreAuthorize("@ss.hasPermi('biz:Alarm:list')")
@GetMapping("/list")
public TableDataInfo list(AlarmVo alarmVo) { // 参数改为 AlarmVo
    IPage<AlarmVo> page = alarmService.selectPageAlarmListWithDetails(getPage(), alarmVo);
    return getDataTableByPage(page); // 返回包含 slotCode, cameraName 等的 VO 列表
}

    /**
     * 导出告警记录列表
     */
    @PreAuthorize("@ss.hasPermi('biz:Alarm:export')")
    @Log(title = "告警记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Alarm alarm) {
        QueryWrapper<Alarm> queryWrapper = getQueryWrapper(Alarm.class);
        IPage<Alarm> page = alarmService.pageAlarm(getPage(), queryWrapper);
        List<Alarm> list = page.getRecords();
        ExcelUtil<Alarm> util = new ExcelUtil<Alarm>(Alarm. class);
        util.exportExcel(response, list, "告警记录");
    }

    /**
     * 导入告警记录数据
     */
    @PreAuthorize("@ss.hasPermi('biz:Alarm:import')")
    @Log(title = "告警记录", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception {
        int titleNum = 0;
        ExcelUtil<Alarm> util = new ExcelUtil<Alarm>(Alarm. class);
        List<Alarm> list = util.importExcel(file.getInputStream(), titleNum);
        String operName = getUsername();
        String message = alarmService.importAlarm(list, titleNum, updateSupport, operName);
        return success(message);
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response, Alarm alarm) {
        List<Alarm> list = new ArrayList<>();
        ExcelUtil<Alarm> util = new ExcelUtil<Alarm>(Alarm. class);
        util.exportExcel(response, list, "告警记录");
    }

    /**
     * 获取告警记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('biz:Alarm:query')")
    @GetMapping(value = "/{alarmId}")
    public AjaxResult getInfo(@PathVariable("alarmId") Long alarmId) {
        return success(alarmService.selectAlarmByAlarmId(alarmId));
    }

    /**
     * 新增告警记录
     */
    @PreAuthorize("@ss.hasPermi('biz:Alarm:add')")
    @Log(title = "告警记录", businessType = BusinessType.INSERT)
    @PostMapping
    @RepeatSubmit
    public AjaxResult add(@Validated @RequestBody Alarm alarm) {
        // 验证 eventId 是否存在
        if (alarm.getEventId() == null) {
            throw new ServiceException("关联事件ID不能为空");
        }

        // 检查事件是否存在
        AiEvent event = aiEventServiceImpl.selectAiEventByEventId(alarm.getEventId());
        if (event == null) {
            throw new ServiceException("关联的事件不存在");
        }

        alarmService.insertAlarm(alarm);
        JSONObject jsonObject = new JSONObject();
        jsonObject.putOpt("id", alarm.getAlarmId());  // 应该返回 alarmId
        return success(jsonObject);
    }

    /**
     * 修改告警记录
     */
    @PreAuthorize("@ss.hasPermi('biz:Alarm:edit')")
    @Log(title = "告警记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody Alarm alarm) {
        return toAjax(alarmService.updateAlarm(alarm));
    }

    /**
     * 删除告警记录
     */
    @PreAuthorize("@ss.hasPermi('biz:Alarm:remove')")
    @Log(title = "告警记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{alarmIds}")
    public AjaxResult remove(@PathVariable Long[] alarmIds) {
        return toAjax(alarmService.deleteAlarmByAlarmIds(alarmIds));
    }
}
