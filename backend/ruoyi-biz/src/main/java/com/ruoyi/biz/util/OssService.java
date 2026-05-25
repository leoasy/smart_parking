package com.ruoyi.biz.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class OssService {
    @Value("${aliyun.oss.endpoint:}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId:}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret:}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName:}")
    private String bucketName;

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
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(size);
            ossClient.putObject(bucketName, objectName, inputStream, metadata);
            return "https://" + bucketName + "." + endpoint + "/" + objectName;
        } finally {
            ossClient.shutdown();
        }
    }
}
