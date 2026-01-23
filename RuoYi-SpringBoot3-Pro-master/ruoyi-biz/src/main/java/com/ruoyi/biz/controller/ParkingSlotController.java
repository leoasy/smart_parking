package com.ruoyi.biz.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.ruoyi.common.constant.HttpStatus;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.springframework.validation.annotation.Validated;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.common.annotation.RepeatSubmit;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.biz.domain.ParkingSlot;
import com.ruoyi.biz.service.IParkingSlotService;
import com.ruoyi.biz.mapper.ParkingSlotMapper;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 车位信息Controller
 *
 * @author aoshiyue
 * @date 2026-01-07
 */
@RestController
@RequestMapping("/biz/ParkingSlot")
@RequiredArgsConstructor
public class ParkingSlotController extends BaseController {
    private final IParkingSlotService parkingSlotService;
    // private final ParkingSlotMapper parkingSlotMapper;

/**
 * 查询车位信息列表
 */
@PreAuthorize("@ss.hasPermi('biz:ParkingSlot:list')")
@GetMapping("/list")
public TableDataInfo list(
        ParkingSlot parkingSlot, // 用于绑定普通查询参数
        @RequestParam Map<String, Object> allRequestParams) { // 用于接收所有请求参数

    LambdaQueryWrapper<ParkingSlot> qw = new LambdaQueryWrapper<>();

    // 处理普通字段（RuoYi 自动绑定到 parkingSlot）
    if (StringUtils.isNotBlank(parkingSlot.getSlotCode())) {
        qw.like(ParkingSlot::getSlotCode, parkingSlot.getSlotCode());
    }
    if (StringUtils.isNotBlank(parkingSlot.getSlotStatus())) {
        qw.eq(ParkingSlot::getSlotStatus, parkingSlot.getSlotStatus());
    }
    if (parkingSlot.getAreaId() != null) {
        qw.eq(ParkingSlot::getAreaId, parkingSlot.getAreaId());
    }
    if (parkingSlot.getCameraId() != null) {
        qw.eq(ParkingSlot::getCameraId, parkingSlot.getCameraId());
    }

    // 处理时间范围（从 allRequestParams 中提取）
    String beginCreateTime = (String) allRequestParams.get("params[begincreate_time]");
    String endCreateTime = (String) allRequestParams.get("params[endcreate_time]");

    if (StringUtils.isNotBlank(beginCreateTime)) {
        qw.ge(ParkingSlot::getCreateTime, beginCreateTime + " 00:00:00");
    }
    if (StringUtils.isNotBlank(endCreateTime)) {
        qw.le(ParkingSlot::getCreateTime, endCreateTime + " 23:59:59");
    }

    // 分页查询
    IPage<ParkingSlot> page = parkingSlotService.page(getPage(), qw);

    // 手动构造返回（兼容旧版 RuoYi）
    TableDataInfo rspData = new TableDataInfo();
    rspData.setCode(HttpStatus.SUCCESS);
    rspData.setMsg("查询成功");
    rspData.setRows(page.getRecords());
    rspData.setTotal(page.getTotal());

    return rspData;
}

    /**
     * 导出车位信息列表
     */
    @PreAuthorize("@ss.hasPermi('biz:ParkingSlot:export')")
    @Log(title = "车位信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ParkingSlot parkingSlot) {
        QueryWrapper<ParkingSlot> queryWrapper = getQueryWrapper(ParkingSlot.class);
        IPage<ParkingSlot> page = parkingSlotService.pageParkingSlot(getPage(), queryWrapper);
        List<ParkingSlot> list = page.getRecords();
        ExcelUtil<ParkingSlot> util = new ExcelUtil<ParkingSlot>(ParkingSlot. class);
        util.exportExcel(response, list, "车位信息");
    }

    /**
     * 导入车位信息数据
     */
    @PreAuthorize("@ss.hasPermi('biz:ParkingSlot:import')")
    @Log(title = "车位信息", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception {
        int titleNum = 0;
        ExcelUtil<ParkingSlot> util = new ExcelUtil<ParkingSlot>(ParkingSlot. class);
        List<ParkingSlot> list = util.importExcel(file.getInputStream(), titleNum);
        String operName = getUsername();
        String message = parkingSlotService.importParkingSlot(list, titleNum, updateSupport, operName);
        return success(message);
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response, ParkingSlot parkingSlot) {
        List<ParkingSlot> list = new ArrayList<>();
        ExcelUtil<ParkingSlot> util = new ExcelUtil<ParkingSlot>(ParkingSlot. class);
        util.exportExcel(response, list, "车位信息");
    }

    /**
     * 获取车位信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('biz:ParkingSlot:query')")
    @GetMapping(value = "/{parkingSlotId}")
    public AjaxResult getInfo(@PathVariable("parkingSlotId") Long parkingSlotId) {
        return success(parkingSlotService.selectParkingSlotByParkingSlotId(parkingSlotId));
    }

    /**
     * 新增车位信息
     */
    @PreAuthorize("@ss.hasPermi('biz:ParkingSlot:add')")
    @Log(title = "车位信息", businessType = BusinessType.INSERT)
    @PostMapping
    @RepeatSubmit
    public AjaxResult add(@Validated @RequestBody ParkingSlot parkingSlot) {
            parkingSlotService.insertParkingSlot(parkingSlot);
        JSONObject jsonObject = new JSONObject();
        jsonObject.putOpt("id", parkingSlot.getParkingSlotId());
        return success(jsonObject);
    }

    /**
     * 修改车位信息
     */
    @PreAuthorize("@ss.hasPermi('biz:ParkingSlot:edit')")
    @Log(title = "车位信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ParkingSlot parkingSlot) {
        return toAjax(parkingSlotService.updateParkingSlot(parkingSlot));
    }

    /**
     * 删除车位信息
     */
    @PreAuthorize("@ss.hasPermi('biz:ParkingSlot:remove')")
    @Log(title = "车位信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{parkingSlotIds}")
    public AjaxResult remove(@PathVariable Long[] parkingSlotIds) {
        return toAjax(parkingSlotService.deleteParkingSlotByParkingSlotIds(parkingSlotIds));
    }
}
