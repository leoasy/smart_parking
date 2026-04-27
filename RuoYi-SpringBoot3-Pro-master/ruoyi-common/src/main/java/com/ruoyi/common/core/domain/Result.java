package com.ruoyi.common.core.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应封装
 *
 * @author ruoyi
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 状态码 */
    private int code;

    /** 消息 */
    private String msg;

    /** 数据 */
    private T data;

    /** 时间戳 */
    private long timestamp;

    public Result()
    {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(int code, String msg)
    {
        this();
        this.code = code;
        this.msg = msg;
    }

    public Result(int code, String msg, T data)
    {
        this();
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> ok()
    {
        return ok(null);
    }

    public static <T> Result<T> ok(T data)
    {
        return new Result<>(200, "操作成功", data);
    }

    public static <T> Result<T> ok(T data, String msg)
    {
        return new Result<>(200, msg, data);
    }

    public static <T> Result<T> fail()
    {
        return fail("操作失败");
    }

    public static <T> Result<T> fail(String msg)
    {
        return new Result<>(500, msg, null);
    }

    public static <T> Result<T> fail(int code, String msg)
    {
        return new Result<>(code, msg, null);
    }

    public static <T> Result<T> fail(T data, String msg)
    {
        return new Result<>(500, msg, data);
    }

    public boolean isSuccess()
    {
        return this.code == 200;
    }
}