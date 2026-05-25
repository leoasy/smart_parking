package com.ruoyi.biz.controller;

import com.ruoyi.biz.service.ParkingDetectService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/biz/parking/detect")
@Slf4j
@RequiredArgsConstructor
public class ParkingDetectController {
    private final ParkingDetectService parkingDetectService;

    /**
     * 单帧检测（RuoYi → FastAPI）
     */
    @PostMapping("/image")
    public AjaxResult detectImage(
            @RequestParam(value = "parking_lot_id", required = false) String parkingLotId,
            @RequestParam(value = "camera_id", required = false) Integer cameraId,
            @RequestParam(value = "imagePath", required = false) String imagePath
    ) {
        if (parkingLotId == null || parkingLotId.isBlank()) {
            return AjaxResult.error("parking_lot_id 不能为空");
        }
        if (cameraId == null) {
            return AjaxResult.error("camera_id 不能为空");
        }
        if (imagePath == null || imagePath.isBlank()) {
            return AjaxResult.error("imagePath 不能为空");
        }

        try {
            Map<String, Object> responseData = parkingDetectService.detectAndPersist(parkingLotId, cameraId, imagePath);
            return AjaxResult.success(responseData);
        } catch (ServiceException ex) {
            return AjaxResult.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("停车检测失败 parkingLotId={} cameraId={} imagePath={}", parkingLotId, cameraId, imagePath, ex);
            return AjaxResult.error("停车检测处理失败");
        }
    }
}