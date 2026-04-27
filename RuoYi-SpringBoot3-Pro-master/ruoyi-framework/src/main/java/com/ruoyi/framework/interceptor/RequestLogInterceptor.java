package com.ruoyi.framework.interceptor;

import cn.hutool.core.util.StrUtil;
import com.ruoyi.common.utils.LogUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

/**
 * 请求日志拦截器
 *
 * @author ruoyi
 */
@Component
public class RequestLogInterceptor implements HandlerInterceptor
{
    private static final Logger log = LoggerFactory.getLogger(RequestLogInterceptor.class);

    private static final String TRACE_ID = "X-Trace-Id";
    private static final String START_TIME = "X-Start-Time";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        String traceId = UUID.randomUUID().toString().replace("-", "");
        request.setAttribute(TRACE_ID, traceId);
        request.setAttribute(START_TIME, System.currentTimeMillis());
        response.setHeader(TRACE_ID, traceId);

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();

        log.info("==> {} {} {}", method, uri, StrUtil.isNotEmpty(queryString) ? "?" + queryString : "");
        log.info("==> Trace-Id: {}", traceId);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception
    {
        // nothing to do
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception
    {
        String traceId = (String) request.getAttribute(TRACE_ID);
        Long startTime = (Long) request.getAttribute(START_TIME);
        long costTime = System.currentTimeMillis() - startTime;

        int status = response.getStatus();
        String method = request.getMethod();
        String uri = request.getRequestURI();

        if (ex != null)
        {
            log.error("<== {} {} {} - {}ms - [ERROR] {}", method, uri, status, costTime, traceId);
            log.error("Exception: ", ex);
        }
        else
        {
            log.info("<== {} {} {} - {}ms - {}", method, uri, status, costTime, traceId);
        }
    }
}