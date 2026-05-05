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
import com.ruoyi.biz.domain.ParkingRoi;
import com.ruoyi.biz.service.IParkingRoiService;
import com.ruoyi.biz.mapper.ParkingRoiMapper;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 车位ROI标注Controller
 *
 * @author aoshiyue
 * @date 2026-01-08
 */
@RestController
@RequestMapping("/biz/ParkingRoi")
@RequiredArgsConstructor
public class ParkingRoiController extends BaseController {
    private final IParkingRoiService parkingRoiService;

/**
 * 查询车位ROI标注列表
 */
@PreAuthorize("@ss.hasPermi('biz:ParkingRoi:list')")
@GetMapping("/list")
    public TableDataInfo list(ParkingRoi parkingRoi) {
        IPage<ParkingRoi> page = parkingRoiService.pageParkingRoiWithRelation(getPage(), parkingRoi);
        return getDataTableByPage(page);
    }

    /**
     * 导出车位ROI标注列表
     */
    @PreAuthorize("@ss.hasPermi('biz:ParkingRoi:export')")
    @Log(title = "车位ROI标注", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ParkingRoi parkingRoi) {
        QueryWrapper<ParkingRoi> queryWrapper = getQueryWrapper(ParkingRoi.class);
        IPage<ParkingRoi> page = parkingRoiService.pageParkingRoi(getPage(), queryWrapper);
        List<ParkingRoi> list = page.getRecords();
        ExcelUtil<ParkingRoi> util = new ExcelUtil<ParkingRoi>(ParkingRoi. class);
        util.exportExcel(response, list, "车位ROI标注");
    }

    /**
     * 导入车位ROI标注数据
     */
    @PreAuthorize("@ss.hasPermi('biz:ParkingRoi:import')")
    @Log(title = "车位ROI标注", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception {
        int titleNum = 0;
        ExcelUtil<ParkingRoi> util = new ExcelUtil<ParkingRoi>(ParkingRoi. class);
        List<ParkingRoi> list = util.importExcel(file.getInputStream(), titleNum);
        String operName = getUsername();
        String message = parkingRoiService.importParkingRoi(list, titleNum, updateSupport, operName);
        return success(message);
    }


    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response, ParkingRoi parkingRoi) {
        List<ParkingRoi> list = new ArrayList<>();
        ExcelUtil<ParkingRoi> util = new ExcelUtil<ParkingRoi>(ParkingRoi. class);
        util.exportExcel(response, list, "车位ROI标注");
    }

    /**
     * 获取车位ROI标注详细信息
     */
    @PreAuthorize("@ss.hasPermi('biz:ParkingRoi:query')")
    @GetMapping(value = "/{parkingRoiId}")
    public AjaxResult getInfo(@PathVariable("parkingRoiId") Long parkingRoiId) {
        return success(parkingRoiService.selectParkingRoiByParkingRoiId(parkingRoiId));
    }

    /**
     * 新增车位ROI标注
     */
    @PreAuthorize("@ss.hasPermi('biz:ParkingRoi:add')")
    @Log(title = "车位ROI标注", businessType = BusinessType.INSERT)
    @PostMapping
    @RepeatSubmit
    public AjaxResult add(@Validated @RequestBody ParkingRoi parkingRoi) {
            parkingRoiService.insertParkingRoi(parkingRoi);
        JSONObject jsonObject = new JSONObject();
        jsonObject.putOpt("id", parkingRoi.getParkingRoiId());
        return success(jsonObject);
    }

    /**
     * 修改车位ROI标注
     */
    @PreAuthorize("@ss.hasPermi('biz:ParkingRoi:edit')")
    @Log(title = "车位ROI标注", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ParkingRoi parkingRoi) {
        return toAjax(parkingRoiService.updateParkingRoi(parkingRoi));
    }

    /**
     * 删除车位ROI标注
     */
    @PreAuthorize("@ss.hasPermi('biz:ParkingRoi:remove')")
    @Log(title = "车位ROI标注", businessType = BusinessType.DELETE)
    @DeleteMapping("/{parkingRoiIds}")
    public AjaxResult remove(@PathVariable Long[] parkingRoiIds) {
        return toAjax(parkingRoiService.deleteParkingRoiByParkingRoiIds(parkingRoiIds));
    }
}
