package com.ruoyi.framework.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * RequestLogInterceptor 单元测试
 */
class RequestLogInterceptorTest {

    private RequestLogInterceptor interceptor;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        interceptor = new RequestLogInterceptor();
    }

    @Test
    @DisplayName("preHandle 应设置Trace-Id和Start-Time属性")
    void preHandle_shouldSetTraceIdAndStartTime() throws Exception
    {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getQueryString()).thenReturn(null);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        verify(request).setAttribute(eq("X-Trace-Id"), any(String.class));
        verify(request).setAttribute(eq("X-Start-Time"), any(Long.class));
        verify(response).setHeader(eq("X-Trace-Id"), any(String.class));
    }

    @Test
    @DisplayName("preHandle 应记录请求方法和URI")
    void preHandle_shouldLogMethodAndUri() throws Exception
    {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/parking");
        when(request.getQueryString()).thenReturn("id=1");

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        verify(request).getMethod();
        verify(request).getRequestURI();
    }

    @Test
    @DisplayName("afterCompletion 正常请求应记录日志")
    void afterCompletion_normalRequest_shouldLog() throws Exception
    {
        String traceId = "abc123";
        Long startTime = System.currentTimeMillis() - 100;

        when(request.getAttribute("X-Trace-Id")).thenReturn(traceId);
        when(request.getAttribute("X-Start-Time")).thenReturn(startTime);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(response.getStatus()).thenReturn(200);

        interceptor.afterCompletion(request, response, new Object(), null);

        verify(request).getAttribute("X-Trace-Id");
        verify(request).getAttribute("X-Start-Time");
    }

    @Test
    @DisplayName("afterCompletion 异常请求应记录错误日志")
    void afterCompletion_exceptionRequest_shouldLogError() throws Exception
    {
        String traceId = "abc123";
        Long startTime = System.currentTimeMillis() - 100;

        when(request.getAttribute("X-Trace-Id")).thenReturn(traceId);
        when(request.getAttribute("X-Start-Time")).thenReturn(startTime);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(response.getStatus()).thenReturn(500);

        Exception ex = new RuntimeException("Test exception");

        // 不应抛出异常
        assertDoesNotThrow(() -> {
            interceptor.afterCompletion(request, response, new Object(), ex);
        });
    }

    @Test
    @DisplayName("postHandle 空实现应正常执行")
    void postHandle_shouldDoNothing() throws Exception
    {
        assertDoesNotThrow(() -> {
            interceptor.postHandle(request, response, new Object(), null);
        });
    }
}