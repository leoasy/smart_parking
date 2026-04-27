package com.ruoyi.common.core.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Result 统一响应封装单元测试
 */
class ResultTest {

    @Test
    @DisplayName("ok() 无参数应返回200状态码和默认消息")
    void ok_shouldReturnSuccessResult()
    {
        Result<String> result = Result.ok();
        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("ok(data) 带数据应返回正确的数据对象")
    void ok_withData_shouldReturnCorrectData()
    {
        String data = "test data";
        Result<String> result = Result.ok(data);
        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMsg());
        assertEquals(data, result.getData());
    }

    @Test
    @DisplayName("ok(data, msg) 带数据和个人消息应返回正确内容")
    void ok_withDataAndMsg_shouldReturnCorrectContent()
    {
        String data = "test data";
        String msg = "自定义成功消息";
        Result<String> result = Result.ok(data, msg);
        assertEquals(200, result.getCode());
        assertEquals(msg, result.getMsg());
        assertEquals(data, result.getData());
    }

    @Test
    @DisplayName("fail() 无参数应返回500状态码和默认失败消息")
    void fail_shouldReturnErrorResult()
    {
        Result<String> result = Result.fail();
        assertEquals(500, result.getCode());
        assertEquals("操作失败", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("fail(msg) 带消息应返回正确错误消息")
    void fail_withMsg_shouldReturnCorrectErrorMsg()
    {
        String msg = "自定义错误消息";
        Result<String> result = Result.fail(msg);
        assertEquals(500, result.getCode());
        assertEquals(msg, result.getMsg());
    }

    @Test
    @DisplayName("fail(code, msg) 带状态码和消息应返回正确内容")
    void fail_withCodeAndMsg_shouldReturnCorrectContent()
    {
        int code = 400;
        String msg = "参数错误";
        Result<String> result = Result.fail(code, msg);
        assertEquals(code, result.getCode());
        assertEquals(msg, result.getMsg());
    }

    @Test
    @DisplayName("fail(data, msg) 带数据和消息应返回正确内容")
    void fail_withDataAndMsg_shouldReturnCorrectContent()
    {
        String data = "error details";
        String msg = "操作失败";
        Result<String> result = Result.fail(data, msg);
        assertEquals(500, result.getCode());
        assertEquals(msg, result.getMsg());
        assertEquals(data, result.getData());
    }

    @Test
    @DisplayName("isSuccess() 成功状态应返回true")
    void isSuccess_onSuccessResult_shouldReturnTrue()
    {
        Result<String> result = Result.ok("data");
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("isSuccess() 失败状态应返回false")
    void isSuccess_onFailResult_shouldReturnFalse()
    {
        Result<String> result = Result.fail("error");
        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("timestamp 应自动设置")
    void timestamp_shouldBeAutoSet()
    {
        Result<String> result = Result.ok();
        assertTrue(result.getTimestamp() > 0);
    }

    @Test
    @DisplayName("支持泛型类型")
    void genericType_shouldWork()
    {
        Result<Integer> intResult = Result.ok(123);
        assertEquals(Integer.valueOf(123), intResult.getData());

        Result<Long> longResult = Result.ok(999L);
        assertEquals(Long.valueOf(999L), longResult.getData());
    }
}