package com.ruoyi.framework.filter;

import com.ruoyi.common.utils.StringUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * XSS过滤器
 * 
 * 对请求参数进行XSS过滤，防止XSS攻击
 * 
 * @author ruoyi
 */
@Component
public class XssFilter implements Filter {

    // 排除的URL（不做XSS过滤）
    private static final String[] EXCLUDE_URLS = {
        "/biz/parking/detect/",  // 停车场检测接口可能需要原始图片数据
    };

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestUri = httpRequest.getRequestURI();
        
        // 检查是否需要排除
        if (isExcludeUrl(requestUri)) {
            chain.doFilter(request, response);
            return;
        }
        
        // 包装请求进行XSS过滤
        XssRequestWrapper xssRequest = new XssRequestWrapper(httpRequest);
        chain.doFilter(xssRequest, response);
    }

    @Override
    public void destroy() {
        // 销毁
    }

    /**
     * 判断URL是否需要排除
     */
    private boolean isExcludeUrl(String url) {
        if (StringUtils.isEmpty(url)) {
            return false;
        }
        for (String excludeUrl : EXCLUDE_URLS) {
            if (url.contains(excludeUrl)) {
                return true;
            }
        }
        return false;
    }
}
