package com.ruoyi.framework.security.filter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.service.TokenService;

/**
 * token过滤器 验证token有效性
 * 
 * 安全加固版本：
 * 1. 添加Token黑名单检查
 * 2. 添加Token使用次数限制（防重放攻击）
 * 3. 添加异常时清除认证信息
 * 
 * @author ruoyi
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter
{
    @Autowired
    private TokenService tokenService;

    @Autowired
    private RedisCache redisCache;

    // Token黑名单前缀
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    // Token使用次数限制前缀（防重放）
    private static final String TOKEN_USAGE_PREFIX = "token:usage:";

    // 最大使用次数（默认10000次）
    @Value("${token.maxUsage:10000}")
    private int maxTokenUsage;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException
    {
        try {
            LoginUser loginUser = tokenService.getLoginUser(request);
            if (StringUtils.isNotNull(loginUser) && StringUtils.isNull(SecurityUtils.getAuthentication()))
            {
                // ========== 安全加固1: 检查Token是否在黑名单中 ==========
                String token = getToken(request);
                if (isTokenBlacklisted(token)) {
                    SecurityContextHolder.clearContext();
                    chain.doFilter(request, response);
                    return;
                }

                // ========== 安全加固2: 检查Token使用次数（防重放攻击）==========
                if (!checkTokenUsage(token, loginUser)) {
                    // 使用次数超限，将Token加入黑名单
                    blacklistToken(token);
                    SecurityContextHolder.clearContext();
                    chain.doFilter(request, response);
                    return;
                }

                // ========== 安全加固3: 验证Token ==========
                tokenService.verifyToken(loginUser);
                UsernamePasswordAuthenticationToken authenticationToken = 
                    new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } catch (Exception e) {
            // 异常时清除认证信息，防止污染后续请求
            SecurityContextHolder.clearContext();
        }
        chain.doFilter(request, response);
    }

    /**
     * 检查Token是否在黑名单中
     */
    private boolean isTokenBlacklisted(String token) {
        if (StringUtils.isEmpty(token)) {
            return false;
        }
        return redisCache.hasKey(TOKEN_BLACKLIST_PREFIX + token);
    }

    /**
     * 将Token加入黑名单
     */
    public void blacklistToken(String token) {
        if (StringUtils.isNotEmpty(token)) {
            // 黑名单有效期应该长于Token剩余有效期
            Long ttl = redisCache.getExpire(TOKEN_BLACKLIST_PREFIX + token);
            if (ttl == null || ttl < 0) {
                // 默认24小时黑名单
                redisCache.setCacheObject(TOKEN_BLACKLIST_PREFIX + token, "1", 24, TimeUnit.HOURS);
            }
        }
    }

    /**
     * 检查Token使用次数，防止重放攻击
     */
    private boolean checkTokenUsage(String token, LoginUser loginUser) {
        if (StringUtils.isEmpty(token)) {
            return true;
        }
        String usageKey = TOKEN_USAGE_PREFIX + token;
        Number usageCount = redisCache.getCacheObject(usageKey);
        
        if (usageCount == null) {
            // 首次使用，初始化计数器
            redisCache.setCacheObject(usageKey, 1L, 30, TimeUnit.DAYS);
            return true;
        }
        
        if (usageCount.longValue() >= maxTokenUsage) {
            return false;
        }
        
        // 增加使用计数
        redisCache.setCacheObject(usageKey, usageCount.longValue() + 1L, 30, TimeUnit.DAYS);
        return true;
    }

    /**
     * 获取请求token
     */
    private String getToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (StringUtils.isNotEmpty(token) && token.startsWith("Bearer ")) {
            token = token.replace("Bearer ", "");
        }
        return token;
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }
}
