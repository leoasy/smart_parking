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
import com.ruoyi.biz.domain.DevCamera;
import com.ruoyi.biz.service.IDevCameraService;
import com.ruoyi.biz.mapper.DevCameraMapper;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 摄像头设备Controller
 *
 * @date 2026-01-08
 */
@RestController
@RequestMapping("/biz/DevCamera")
@RequiredArgsConstructor
public class DevCameraController extends BaseController {
    private final IDevCameraService devCameraService;
    // private final DevCameraMapper devCameraMapper;

    /**
     * 查询摄像头设备列表
     */
    @PreAuthorize("@ss.hasPermi('biz:DevCamera:list')")
    @GetMapping("/list")
    public TableDataInfo list(
            DevCamera devCamera, // 自动绑定普通字段：cameraName, cameraStatus, location 等
            @RequestParam Map<String, Object> allRequestParams) { // 用于获取 params[xxx] 时间参数

        LambdaQueryWrapper<DevCamera> qw = new LambdaQueryWrapper<>();

        // 普通字段查询（由 Spring MVC 自动绑定到 devCamera）
        if (StringUtils.isNotBlank(devCamera.getCameraName())) {
            qw.like(DevCamera::getCameraName, devCamera.getCameraName());
        }
        if (StringUtils.isNotBlank(devCamera.getCameraStatus())) {
            qw.eq(DevCamera::getCameraStatus, devCamera.getCameraStatus());
        }
        if (StringUtils.isNotBlank(devCamera.getLocation())) {
            qw.like(DevCamera::getLocation, devCamera.getLocation());
        }

        // ⏰ 时间范围查询：从 allRequestParams 中提取 Ruoyi 标准格式的 params[begincreate_time]
        String beginCreateTime = (String) allRequestParams.get("params[begincreate_time]");
        String endCreateTime = (String) allRequestParams.get("params[endcreate_time]");

        if (StringUtils.isNotBlank(beginCreateTime)) {
            qw.ge(DevCamera::getCreateTime, beginCreateTime + " 00:00:00");
        }
        if (StringUtils.isNotBlank(endCreateTime)) {
            qw.le(DevCamera::getCreateTime, endCreateTime + " 23:59:59");
        }

        // 执行分页查询
        IPage<DevCamera> page = devCameraService.page(getPage(), qw);

        // 构造返回结果（兼容 RuoYi）
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("查询成功");
        rspData.setRows(page.getRecords());
        rspData.setTotal(page.getTotal());

        return rspData;
    }

    @PostMapping("/export")
    @PreAuthorize("@ss.hasPermi('biz:DevCamera:export')")
    @Log(title = "摄像头设备", businessType = BusinessType.EXPORT)
    public void export(HttpServletResponse response, DevCamera devCamera) {
        // 使用 LambdaQueryWrapper 手动构建条件（与 list 方法逻辑一致）
        LambdaQueryWrapper<DevCamera> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(devCamera.getCameraName())) {
            wrapper.like(DevCamera::getCameraName, devCamera.getCameraName());
        }
        if (devCamera.getCameraStatus() != null) {
            wrapper.eq(DevCamera::getCameraStatus, devCamera.getCameraStatus());
        }
        if (StringUtils.isNotBlank(devCamera.getLocation())) {
            wrapper.like(DevCamera::getLocation, devCamera.getLocation());
        }

        // 注意：导出通常不分页，应查全部数据！
        List<DevCamera> list = devCameraService.list(wrapper);

        ExcelUtil<DevCamera> util = new ExcelUtil<>(DevCamera.class);
        util.exportExcel(response, list, "摄像头设备");
    }

    /**
     * 导入摄像头设备数据
     */
    @PreAuthorize("@ss.hasPermi('biz:DevCamera:import')")
    @Log(title = "摄像头设备", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception {
        int titleNum = 0;
        ExcelUtil<DevCamera> util = new ExcelUtil<DevCamera>(DevCamera. class);
        List<DevCamera> list = util.importExcel(file.getInputStream(), titleNum);
        String operName = getUsername();
        String message = devCameraService.importDevCamera(list, titleNum, updateSupport, operName);
        return success(message);
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response, DevCamera devCamera) {
        List<DevCamera> list = new ArrayList<>();
        ExcelUtil<DevCamera> util = new ExcelUtil<DevCamera>(DevCamera. class);
        util.exportExcel(response, list, "摄像头设备");
    }

    /**
     * 获取摄像头设备详细信息
     */
    @PreAuthorize("@ss.hasPermi('biz:DevCamera:query')")
    @GetMapping(value = "/{cameraId}")
    public AjaxResult getInfo(@PathVariable("cameraId") Long cameraId) {
        return success(devCameraService.selectDevCameraByCameraId(cameraId));
    }

    /**
     * 新增摄像头设备
     */
    @PreAuthorize("@ss.hasPermi('biz:DevCamera:add')")
    @Log(title = "摄像头设备", businessType = BusinessType.INSERT)
    @PostMapping
    @RepeatSubmit
    public AjaxResult add(@Validated @RequestBody DevCamera devCamera) {
            devCameraService.insertDevCamera(devCamera);
        JSONObject jsonObject = new JSONObject();
        jsonObject.putOpt("id", devCamera.getCameraId());
        return success(jsonObject);
    }

    /**
     * 修改摄像头设备
     */
    @PreAuthorize("@ss.hasPermi('biz:DevCamera:edit')")
    @Log(title = "摄像头设备", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody DevCamera devCamera) {
        return toAjax(devCameraService.updateDevCamera(devCamera));
    }

    /**
     * 删除摄像头设备
     */
    @PreAuthorize("@ss.hasPermi('biz:DevCamera:remove')")
    @Log(title = "摄像头设备", businessType = BusinessType.DELETE)
    @DeleteMapping("/{cameraIds}")
    public AjaxResult remove(@PathVariable Long[] cameraIds) {
        return toAjax(devCameraService.deleteDevCameraByCameraIds(cameraIds));
    }
}
