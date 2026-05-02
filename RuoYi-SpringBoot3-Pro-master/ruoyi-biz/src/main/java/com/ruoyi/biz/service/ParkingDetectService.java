package com.ruoyi.biz.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ruoyi.biz.domain.AiEvent;
import com.ruoyi.biz.domain.DevCamera;
import com.ruoyi.biz.domain.ParkingSlot;
import com.ruoyi.biz.service.impl.DevCameraServiceImpl;
import com.ruoyi.biz.util.OssService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParkingDetectService {
    private static final String SLOT_STATUS_OCCUPIED = "OCCUPIED";
    private static final String SLOT_STATUS_FREE = "FREE";

    @Value("${ai.fastapi.url}")
    private String fastApiUrl;

    private final RestTemplate restTemplate;
    private final IParkingSlotService parkingSlotService;
    private final OssService ossService;
    private final IAiEventService aiEventService;
    private final DevCameraServiceImpl devCameraServiceImpl;

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> detectAndPersist(String parkingLotId, Integer cameraId, String imagePath) {
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            throw new ServiceException("图片不存在: " + imagePath);
        }

        Map<String, Object> responseData = invokeFastApi(parkingLotId, cameraId, imageFile);
        List<Map<String, Object>> slots = extractSlots(responseData);
        String cameraName = extractCameraName(responseData);

        DevCamera devCamera = devCameraServiceImpl.getOne(
                new QueryWrapper<DevCamera>().eq("camera_name", cameraName).eq("del_flag", "0")
        );
        if (devCamera == null) {
            throw new ServiceException("未找到摄像头：" + cameraName);
        }
        Long realCameraId = devCamera.getCameraId();

        String imgUrl = uploadImage(imageFile);
        String username = resolveUsername();

        for (Map<String, Object> slot : slots) {
            Object slotCodeValue = slot.get("slot_code");
            if (slotCodeValue == null) {
                continue;
            }
            String slotCode = slotCodeValue.toString();
            boolean occupied = Boolean.parseBoolean(String.valueOf(slot.get("occupied")));
            updateSlotAndPersistEvent(realCameraId, cameraName, imagePath, imgUrl, username, slotCode, occupied);
        }

        return responseData;
    }

    @CircuitBreaker(name = "aiService", fallbackMethod = "invokeFastApiFallback")
    @Retry(name = "aiService")
    private Map<String, Object> invokeFastApi(String parkingLotId, Integer cameraId, File imageFile) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("parking_lot_id", parkingLotId);
        body.add("camera_id", cameraId);
        body.add("file", new FileSystemResource(imageFile));
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    fastApiUrl + "/parking/detect/image",
                    request,
                    Map.class
            );
            Map<String, Object> responseData = response.getBody();
            if (responseData == null) {
                throw new ServiceException("FastAPI 返回空响应");
            }
            return responseData;
        } catch (HttpClientErrorException ex) {
            // 4xx 客户端错误 - 请求格式错误、权限问题等
            log.error("AI 服务客户端错误: status={}, body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new ServiceException("AI 服务请求失败: " + ex.getStatusCode());
        } catch (HttpServerErrorException ex) {
            // 5xx 服务器错误 - FastAPI 内部故障
            log.error("AI 服务端错误: status={}, body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new ServiceException("AI 服务暂时不可用: " + ex.getStatusCode());
        } catch (RestClientException ex) {
            // 网络连接失败、超时等
            log.error("AI 服务连接失败: {}", ex.getMessage());
            throw new ServiceException("AI 服务连接失败: " + ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> invokeFastApiFallback(String parkingLotId, Integer cameraId, File imageFile, Exception ex) {
        log.error("AI 服务熔断触发，降级处理: parkingLotId={}, cameraId={}, error={}", parkingLotId, cameraId, ex.getMessage());
        throw new ServiceException("AI 服务暂时不可用，请稍后重试");
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractSlots(Map<String, Object> responseData) {
        Object slots = responseData.get("slots");
        if (!(slots instanceof List)) {
            throw new ServiceException("FastAPI 返回数据格式错误: slots 缺失");
        }
        return (List<Map<String, Object>>) slots;
    }

    private String extractCameraName(Map<String, Object> responseData) {
        Object cameraName = responseData.get("camera_name");
        if (cameraName == null || cameraName.toString().isBlank()) {
            throw new ServiceException("FastAPI 未返回 camera_name");
        }
        return cameraName.toString();
    }

    private String uploadImage(File imageFile) {
        // 流式上传，避免将整个图片加载到内存
        String objectName = "alarm/" + java.time.LocalDate.now() + "/" + UUID.randomUUID() + ".jpg";
        try (InputStream inputStream = new FileInputStream(imageFile)) {
            return ossService.uploadStream(inputStream, imageFile.length(), objectName);
        } catch (IOException ex) {
            log.error("读取图片文件失败: {}", ex.getMessage());
            throw new ServiceException("上传图片失败: " + ex.getMessage());
        }
    }

    private String resolveUsername() {
        try {
            return SecurityUtils.getUsername();
        } catch (Exception ex) {
            return "system";
        }
    }

    private void updateSlotAndPersistEvent(
            Long cameraId,
            String cameraName,
            String imagePath,
            String imageUrl,
            String username,
            String slotCode,
            boolean occupied
    ) {
        ParkingSlot parkingSlot = parkingSlotService.selectByCameraAndSlotCode(cameraId, slotCode);
        if (parkingSlot == null) {
            log.debug("未找到对应车位记录, 跳过更新: cameraId={}, slotCode={}", cameraId, slotCode);
            return;
        }

        String oldStatus = parkingSlot.getSlotStatus();
        String newStatus = occupied ? SLOT_STATUS_OCCUPIED : SLOT_STATUS_FREE;
        if (newStatus.equals(oldStatus)) {
            return;
        }

        parkingSlot.setSlotStatus(newStatus);
        parkingSlotService.updateParkingSlot(parkingSlot);

        AiEvent aiEvent = new AiEvent();
        aiEvent.setCameraId(cameraId);
        aiEvent.setCameraName(cameraName);
        aiEvent.setSlotId(parkingSlot.getParkingSlotId());
        aiEvent.setSlotCode(slotCode);
        aiEvent.setOldStatus(oldStatus);
        aiEvent.setNewStatus(newStatus);
        aiEvent.setEventTime(new Date());
        aiEvent.setImageUrl(imageUrl);
        aiEvent.setFramePath(imagePath);
        aiEvent.setCreateBy(username);
        aiEventService.insertAiEvent(aiEvent);
    }
}
