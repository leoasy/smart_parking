package com.ruoyi.biz.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ruoyi.biz.domain.DevCamera;
import com.ruoyi.biz.service.impl.DevCameraServiceImpl;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.FileSystemResource;
import java.io.File;
import java.util.List;
import java.util.Map;
import com.ruoyi.biz.domain.ParkingSlot;
import com.ruoyi.biz.service.IParkingSlotService;
import com.ruoyi.biz.domain.AiEvent;
import com.ruoyi.biz.service.IAiEventService;

@RestController
@RequestMapping("/biz/parking/detect")
public class ParkingDetectController {

    @Value("${ai.fastapi.url}")
    private String fastApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // ✅ 关键修改1：使用 @Autowired 注入服务（替代 SpringUtils）
    @Autowired
    private IParkingSlotService parkingSlotService;

    @Autowired
    private IAiEventService aiEventService;
    @Autowired
    private DevCameraServiceImpl devCameraServiceImpl;

    /**
     * 单帧检测（RuoYi → FastAPI）
     */
    @PostMapping("/image")
    public AjaxResult detectImage(
            @RequestParam(value = "parking_lot_id", required = false) String parkingLotId,
            @RequestParam(value = "camera_id", required = false) Integer cameraId,
            @RequestParam(value = "imagePath", required = false) String imagePath
    ) {
        System.out.println(
                "[DEBUG] parking_lot_id= " + parkingLotId +
                        ", camera_id= " + cameraId +
                        ", imagePath= " + imagePath
        );

        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            return AjaxResult.error("图片不存在: " + imagePath);
        }

        // ---------- 1️⃣ multipart ----------
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("parking_lot_id", parkingLotId);
        body.add("camera_id", cameraId);
        body.add("file", new FileSystemResource(imageFile));

        HttpEntity<MultiValueMap<String, Object>> request =
                new HttpEntity<>(body, headers);

        // ---------- 2️⃣ 调 FastAPI ----------
        ResponseEntity<Map> response;
        try {
            response = restTemplate.postForEntity(
                    fastApiUrl + "/parking/detect/image",
                    request,
                    Map.class
            );
        } catch (Exception e) {
            return AjaxResult.error("AI 服务调用失败： " + e.getMessage());
        }



        // ---------- 3️⃣ 处理 FastAPI 返回的数据 ----------
        Map<String, Object> responseData = response.getBody();
        if (responseData == null || !responseData.containsKey("slots")) {
            return AjaxResult.error("FastAPI 返回数据格式错误");
        }

        List<Map<String, Object>> slots =
                (List<Map<String, Object>>) responseData.get("slots");

        String cameraName = (String) responseData.get("camera_name");
        if (cameraName == null) {
            return AjaxResult.error("FastAPI 未返回 camera_name");
        }

        DevCamera devCamera = devCameraServiceImpl.getOne(
                new QueryWrapper<DevCamera>()
                        .eq("camera_name", cameraName)
                        .eq("del_flag", "0")
        );

        if (devCamera == null) {
            return AjaxResult.error("未找到摄像头：" + cameraName);
        }

        Long realCameraId = devCamera.getCameraId();

        String username;
        try {
            username = SecurityUtils.getUsername();
        } catch (Exception e) {
            // 本地 / 无登录态测试兜底
            username = "system";
        }


        // 更新车位状态
        for (Map<String, Object> slot : slots) {
            String slotCode = slot.get("slot_code").toString();
            Boolean occupied = (Boolean) slot.get("occupied");

        // 通过 slot_code + camera_id 查找车位
            ParkingSlot parkingSlot =
                    parkingSlotService.selectByCameraAndSlotCode(
                            realCameraId,
                            slotCode
                    );
            if (parkingSlot == null) {
                // ROI 里有，但系统里没这个车位，直接跳过
                continue;
            }
            String oldStatus = parkingSlot.getSlotStatus();
            // 更新车位状态
            String newStatus = occupied ? "OCCUPIED" : "FREE";
            if (!newStatus.equals(oldStatus)) {

                // 1️⃣ 先更新车位
                parkingSlot.setSlotStatus(newStatus);
                parkingSlotService.updateParkingSlot(parkingSlot);

                // 2️⃣ 再记录 AI 事件
                AiEvent aiEvent = new AiEvent();
                aiEvent.setCameraId(realCameraId);
                aiEvent.setCameraName(cameraName);
                aiEvent.setSlotId(parkingSlot.getParkingSlotId());
                aiEvent.setSlotCode(slotCode);
                aiEvent.setOldStatus(oldStatus);
                aiEvent.setNewStatus(newStatus);
                aiEvent.setEventTime(new java.util.Date());
                aiEvent.setFramePath(imagePath);
                aiEvent.setCreateBy(username);

                aiEventService.insertAiEvent(aiEvent);
            }
        }

        // ---------- 4️⃣ 返回 AI 结果 ----------
        return AjaxResult.success(responseData);
    }
}