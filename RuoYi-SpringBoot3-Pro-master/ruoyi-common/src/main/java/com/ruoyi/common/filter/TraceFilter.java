package com.ruoyi.common.filter;

import java.io.IOException;
import java.util.UUID;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * TraceFilter - 请求链路追踪过滤器
 * 
 * 将 traceId/spanId 加入 MDC 日志上下文，支持结构化日志输出
 * 同时设置到 response header 供前端或网关获取
 *
 * @author ruoyi
 */
@Component
public class TraceFilter implements Filter {

    /** traceId 对应的 MDC key */
    private static final String TRACE_ID = "traceId";
    /** spanId 对应的 MDC key */
    private static final String SPAN_ID = "spanId";
    /** traceId response header 名称 */
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 无需初始化
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // 获取或生成 traceId
        String traceId = httpRequest.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString(true);
        }

        // 生成 spanId（取 UUID 缩短前8位）
        String spanId = UUID.randomUUID().toString(true).substring(0, 8);

        try {
            // 放入 MDC，供 logback JSON 格式读取
            MDC.put(TRACE_ID, traceId);
            MDC.put(SPAN_ID, spanId);

            // 设置到响应头，便于调用方追踪
            if (response instanceof jakarta.servlet.http.HttpServletResponse httpResponse) {
                httpResponse.setHeader(TRACE_ID_HEADER, traceId);
            }

            chain.doFilter(request, response);
        } finally {
            // 清理 MDC，避免线程污染
            MDC.remove(TRACE_ID);
            MDC.remove(SPAN_ID);
        }
    }

    @Override
    public void destroy() {
        // 无需清理
    }
}
