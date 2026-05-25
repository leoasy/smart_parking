package com.ruoyi.framework.filter;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.xss.XssUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.HashMap;
import java.util.Map;

/**
 * XSS请求包装器
 * 
 * 对请求参数进行XSS过滤，保护应用免受XSS攻击
 * 
 */
public class XssRequestWrapper extends HttpServletRequestWrapper {

    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }
        int length = values.length;
        String[] encodedValues = new String[length];
        for (int i = 0; i < length; i++) {
            encodedValues[i] = XssUtil.filter(values[i]);
        }
        return encodedValues;
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        return XssUtil.filter(value);
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        // 对Header也进行XSS过滤
        return XssUtil.filter(value);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> params = super.getParameterMap();
        if (params == null) {
            return null;
        }
        Map<String, String[]> result = new HashMap<>();
        for (String key : params.keySet()) {
            String[] values = params.get(key);
            if (values != null) {
                String[] encodedValues = new String[values.length];
                for (int i = 0; i < values.length; i++) {
                    encodedValues[i] = XssUtil.filter(values[i]);
                }
                result.put(key, encodedValues);
            }
        }
        return result;
    }
}
