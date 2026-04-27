package com.ruoyi.biz.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class OssService {
    private final String endpoint;
    private final String accessKeyId;
    private final String accessKeySecret;
    private final String bucketName;

    public String upload(byte[] bytes, String fileName) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        String objectName = "alarm/" + LocalDate.now() + "/" + fileName;
        ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        ossClient.shutdown();
        return "https://" + bucketName + "." + endpoint + "/" + objectName;
    }

    public String uploadStream(InputStream inputStream, long size, String objectName) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            ossClient.putObject(bucketName, objectName, inputStream, size);
            return "https://" + bucketName + "." + endpoint + "/" + objectName;
        } finally {
            ossClient.shutdown();
        }
    }
}