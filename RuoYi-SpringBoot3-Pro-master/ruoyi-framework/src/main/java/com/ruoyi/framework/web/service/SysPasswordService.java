package com.ruoyi.framework.web.service;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.user.UserPasswordNotMatchException;
import com.ruoyi.common.exception.user.UserPasswordRetryLimitExceedException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.framework.security.context.AuthenticationContextHolder;

/**
 * 登录密码方法
 * 
 * 安全加固版本：
 * 1. 启用密码错误次数限制（防暴力破解）
 * 2. 添加连续失败检测
 * 3. 增强密码匹配安全性
 * 
 * @author ruoyi
 */
@Component
public class SysPasswordService
{
    private static final Logger log = LoggerFactory.getLogger(SysPasswordService.class);

    @Autowired
    private RedisCache redisCache;

    // 最大重试次数（默认5次）
    @Value("${user.password.maxRetryCount:5}")
    private int maxRetryCount;

    // 锁定时间（分钟，默认10分钟）
    @Value("${user.password.lockTime:10}")
    private int lockTime;

    // 是否启用密码重试限制
    @Value("${user.password.enableRetryLimit:true}")
    private boolean enableRetryLimit;

    /**
     * 登录账户密码错误次数缓存键名
     *
     * @param username 用户名
     * @return 缓存键key
     */
    private String getCacheKey(String username)
    {
        return CacheConstants.PWD_ERR_CNT_KEY + username;
    }

    /**
     * 连续错误次数缓存键名
     */
    private String getConsecutiveErrKey(String username)
    {
        return CacheConstants.PWD_ERR_CNT_KEY + username + ":consecutive";
    }

    /**
     * 验证密码
     * 
     * @param user 用户信息
     */
    public void validate(SysUser user)
    {
        Authentication usernamePasswordAuthenticationToken = AuthenticationContextHolder.getContext();
        String username = usernamePasswordAuthenticationToken.getName();
        String password = usernamePasswordAuthenticationToken.getCredentials().toString();

        // ========== 安全加固: 检查是否启用密码重试限制 ==========
        if (enableRetryLimit) {
            Integer retryCount = redisCache.getCacheObject(getCacheKey(username));

            if (retryCount == null)
            {
                retryCount = 0;
            }

            if (retryCount >= maxRetryCount)
            {
                // 获取锁定剩余时间
                Long expireTime = redisCache.getExpire(getCacheKey(username));
                int remainingMinutes = expireTime != null ? (int) Math.min(Integer.MAX_VALUE, expireTime / 60) : lockTime;
                throw new UserPasswordRetryLimitExceedException(maxRetryCount, remainingMinutes);
            }

            if (!matches(user, password))
            {
                // 增加错误次数
                retryCount = retryCount + 1;
                redisCache.setCacheObject(getCacheKey(username), retryCount, lockTime, TimeUnit.MINUTES);
                
                // 更新连续错误标记
                updateConsecutiveError(username);
                
                // 计算剩余尝试次数
                int remainingAttempts = maxRetryCount - retryCount;
                if (remainingAttempts <= 2) {
                    // 剩余尝试次数过少时给出提示
                    log.warn("用户{}密码错误，剩余{}次尝试机会", username, remainingAttempts);
                }
                
                throw new UserPasswordNotMatchException();
            }
            else
            {
                clearLoginRecordCache(username);
            }
        } else {
            // 未启用限制时，直接验证
            if (!matches(user, password))
            {
                throw new UserPasswordNotMatchException();
            }
        }
    }

    /**
     * 更新连续错误状态
     */
    private void updateConsecutiveError(String username) {
        String consecutiveKey = getConsecutiveErrKey(username);
        Long consecutiveCount = redisCache.getCacheObject(consecutiveKey);
        if (consecutiveCount == null) {
            consecutiveCount = 1L;
        } else {
            consecutiveCount = consecutiveCount + 1;
        }
        redisCache.setCacheObject(consecutiveKey, consecutiveCount, 24, TimeUnit.HOURS);
        
        // 如果连续错误超过3次，记录安全警告
        if (consecutiveCount >= 3) {
            log.warn("用户{}连续{}次输入错误密码", username, consecutiveCount);
        }
    }

    /**
     * 清除登录记录缓存
     * 
     * @param loginName 登录名
     */
    public void clearLoginRecordCache(String loginName)
    {
        if (redisCache.hasKey(getCacheKey(loginName)))
        {
            redisCache.deleteObject(getCacheKey(loginName));
        }
        if (redisCache.hasKey(getConsecutiveErrKey(loginName))) {
            redisCache.deleteObject(getConsecutiveErrKey(loginName));
        }
    }

    /**
     * 判断密码是否匹配
     * 
     * @param user  用户信息
     * @param rawPassword 原始密码
     * @return 是否匹配
     */
    public boolean matches(SysUser user, String rawPassword)
    {
        // ========== 安全加固: 添加密码强度检查 ==========
        if (rawPassword == null || rawPassword.length() < 6) {
            return false;
        }
        
        // 使用SecurityUtils的密码匹配方法
        return SecurityUtils.matchesPassword(rawPassword, user.getPassword());
    }

    /**
     * 获取用户连续错误登录的次数
     * 
     * @param username 用户名
     * @return 连续错误次数
     */
    public int getConsecutiveErrorCount(String username) {
        Long count = redisCache.getCacheObject(getConsecutiveErrKey(username));
        return count != null ? count.intValue() : 0;
    }

    /**
     * 检查用户是否被锁定
     * 
     * @param username 用户名
     * @return true表示被锁定
     */
    public boolean isLocked(String username) {
        if (!enableRetryLimit) {
            return false;
        }
        Integer retryCount = redisCache.getCacheObject(getCacheKey(username));
        return retryCount != null && retryCount >= maxRetryCount;
    }
}
