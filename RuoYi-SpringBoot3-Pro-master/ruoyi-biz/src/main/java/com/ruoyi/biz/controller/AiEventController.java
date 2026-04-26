package com.ruoyi.biz.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.biz.domain.AiEvent;
import com.ruoyi.biz.service.IAiEventService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * AI推理事件Controller
 *
 * @author aoshiyue
 * @date 2026-01-08
 */
@RestController
@RequestMapping("/biz/AiEvent")
@RequiredArgsConstructor
public class AiEventController extends BaseController {
    private final IAiEventService aiEventService;

/**
 * 查询AI推理事件列表
 */
@PreAuthorize("@ss.hasPermi('biz:AiEvent:list')")
@GetMapping("/list")
    public TableDataInfo list(AiEvent aiEvent) {
        QueryWrapper<AiEvent> queryWrapper = getQueryWrapper(AiEvent.class);
        IPage<AiEvent> page = aiEventService.pageAiEvent(getPage(), queryWrapper);
        return getDataTableByPage(page);
    }

    /**
     * 导出AI推理事件列表
     */
    @PreAuthorize("@ss.hasPermi('biz:AiEvent:export')")
    @Log(title = "AI推理事件", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AiEvent aiEvent) {
        QueryWrapper<AiEvent> queryWrapper = getQueryWrapper(AiEvent.class);
        IPage<AiEvent> page = aiEventService.pageAiEvent(getPage(), queryWrapper);
        List<AiEvent> list = page.getRecords();
        ExcelUtil<AiEvent> util = new ExcelUtil<AiEvent>(AiEvent. class);
        util.exportExcel(response, list, "AI推理事件");
    }

    /**
     * 导入AI推理事件数据
     */
    @PreAuthorize("@ss.hasPermi('biz:AiEvent:import')")
    @Log(title = "AI推理事件", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception {
        int titleNum = 0;
        ExcelUtil<AiEvent> util = new ExcelUtil<AiEvent>(AiEvent. class);
        List<AiEvent> list = util.importExcel(file.getInputStream(), titleNum);
        String operName = getUsername();
        String message = aiEventService.importAiEvent(list, titleNum, updateSupport, operName);
        return success(message);
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response, AiEvent aiEvent) {
        List<AiEvent> list = new ArrayList<>();
        ExcelUtil<AiEvent> util = new ExcelUtil<AiEvent>(AiEvent. class);
        util.exportExcel(response, list, "AI推理事件");
    }

    /**
     * 获取AI推理事件详细信息
     */
    @PreAuthorize("@ss.hasPermi('biz:AiEvent:query')")
    @GetMapping(value = "/{eventId}")
    public AjaxResult getInfo(@PathVariable("eventId") Long eventId) {
        return success(aiEventService.selectAiEventByEventId(eventId));
    }

    /**
     * 新增AI推理事件
     */
    @PreAuthorize("@ss.hasPermi('biz:AiEvent:add')")
    @Log(title = "AI推理事件", businessType = BusinessType.INSERT)
    @PostMapping
    @RepeatSubmit
    public AjaxResult add(@Validated @RequestBody AiEvent aiEvent) {
            aiEventService.insertAiEvent(aiEvent);
        JSONObject jsonObject = new JSONObject();
        jsonObject.putOpt("id", aiEvent.getEventId());
        return success(jsonObject);
    }

    /**
     * 修改AI推理事件
     */
    @PreAuthorize("@ss.hasPermi('biz:AiEvent:edit')")
    @Log(title = "AI推理事件", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AiEvent aiEvent) {
        return toAjax(aiEventService.updateAiEvent(aiEvent));
    }

    /**
     * 删除AI推理事件
     */
    @PreAuthorize("@ss.hasPermi('biz:AiEvent:remove')")
    @Log(title = "AI推理事件", businessType = BusinessType.DELETE)
    @DeleteMapping("/{eventIds}")
    public AjaxResult remove(@PathVariable Long[] eventIds) {
        return toAjax(aiEventService.deleteAiEventByEventIds(eventIds));
    }
}
