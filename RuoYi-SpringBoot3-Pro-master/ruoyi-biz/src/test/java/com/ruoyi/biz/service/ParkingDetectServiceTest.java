package com.ruoyi.biz.service;

import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ParkingDetectServiceTest {

    @Test
    void detectAndPersist_shouldValidateImagePath() {
        ParkingDetectService service = new ParkingDetectService(
                null, null, null, null, null
        );
        assertThrows(ServiceException.class, () -> service.detectAndPersist("roi_1", 1, "not-exists.jpg"));
    }
}
