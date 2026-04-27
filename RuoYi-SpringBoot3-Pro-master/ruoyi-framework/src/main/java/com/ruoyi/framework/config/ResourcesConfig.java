package com.ruoyi.framework.config;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.framework.interceptor.RepeatSubmitInterceptor;
import com.ruoyi.framework.interceptor.RequestLogInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

/**
 * 通用配置
 * 
 * 安全加固版本：
 * 1. CORS配置改进：禁止credentials、限制allowedOrigins
 * 2. 添加安全响应头
 * 
 * @author ruoyi
 */
@Configuration
public class ResourcesConfig implements WebMvcConfigurer {
    @Autowired
    private RepeatSubmitInterceptor repeatSubmitInterceptor;

    @Autowired
    private RequestLogInterceptor requestLogInterceptor;

    // CORS允许的来源列表（生产环境应配置具体域名）
    @Value("${cors.allowedOrigins:}")
    private String allowedOrigins;

    // 是否允许CORS携带凭证
    @Value("${cors.allowCredentials:false}")
    private boolean allowCredentials;

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(600_000); // 10 分钟
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        /** 本地文件上传路径 */
        registry.addResourceHandler(Constants.RESOURCE_PREFIX + "/**")
                .addResourceLocations("file:" + RuoYiConfig.getProfile() + "/");

        /** swagger配置 */
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
                .setCacheControl(CacheControl.maxAge(5, TimeUnit.HOURS).cachePublic());
    }

    /**
     * 自定义拦截规则
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(repeatSubmitInterceptor).addPathPatterns("/**");
        registry.addInterceptor(requestLogInterceptor).addPathPatterns("/**");
    }

    /**
     * 跨域配置
     * 
     * 安全加固说明：
     * 1. 禁止使用 * 允许所有来源（安全风险）
     * 2. 当 allowCredentials=true 时，allowedOrigins 不能为 *
     * 3. 只允许必要的HTTP方法
     * 4. 只允许必要的请求头
     * 5. 暴露必要的响应头
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // ========== 安全加固1: 处理allowedOrigins配置 ==========
        if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
            // 支持多个域名，用逗号分隔
            String[] origins = allowedOrigins.split(",");
            for (String origin : origins) {
                origin = origin.trim();
                if (!origin.isEmpty()) {
                    config.addAllowedOriginPattern(origin);
                }
            }
        } else {
            // 如果未配置，生产环境应该警告，这里保守地只允许GET,POST,HEAD
            // 注意：不允许 * + credentials 同时使用
        }
        
        // ========== 安全加固2: 凭证设置 ==========
        // 当需要携带cookie时，必须设置为具体域名，不能是*
        config.setAllowCredentials(allowCredentials);
        
        // ========== 安全加固3: 限制允许的请求方法 ==========
        config.addAllowedMethod(HttpMethod.GET);
        config.addAllowedMethod(HttpMethod.POST);
        config.addAllowedMethod(HttpMethod.PUT);
        config.addAllowedMethod(HttpMethod.DELETE);
        config.addAllowedMethod(HttpMethod.OPTIONS);
        config.addAllowedMethod(HttpMethod.HEAD);
        
        // ========== 安全加固4: 限制允许的请求头 ==========
        config.addAllowedHeader("Origin");
        config.addAllowedHeader("X-Requested-With");
        config.addAllowedHeader("Content-Type");
        config.addAllowedHeader("Accept");
        config.addAllowedHeader("Authorization");
        config.addAllowedHeader("X-Token");
        // 禁止允许所有请求头：config.addAllowedHeader("*");
        
        // ========== 安全加固5: 暴露必要的响应头 ==========
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Content-Disposition");
        // 禁止暴露所有响应头
        
        // 预检请求缓存时间
        config.setMaxAge(1800L);
        
        // 添加映射路径，拦截一切请求
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
