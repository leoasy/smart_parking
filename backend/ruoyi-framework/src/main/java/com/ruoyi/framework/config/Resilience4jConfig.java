package com.ruoyi.framework.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Resilience4j 熔断降级配置
 *
 */
@Configuration
public class Resilience4jConfig {

    /**
     * 配置熔断器
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                // 滑动窗口大小，熔断器在最近 N 次调用中计算失败率
                .slidingWindowSize(10)
                // 熔断器打开的最小调用次数
                .minimumNumberOfCalls(5)
                // 失败率阈值，超过此比例则熔断器打开
                .failureRateThreshold(50)
                // 熔断器打开持续时间
                .waitDurationInOpenState(Duration.ofSeconds(30))
                // 半开状态允许的调用次数
                .permittedNumberOfCallsInHalfOpenState(3)
                // 自动从打开变为半开
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();

        return CircuitBreakerRegistry.of(config);
    }

    /**
     * 配置重试
     */
    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
                // 最大重试次数
                .maxAttempts(3)
                // 重试间隔
                .waitDuration(Duration.ofMillis(500))
                // 只对特定异常进行重试
                .retryExceptions(Exception.class)
                .build();

        return RetryRegistry.of(config);
    }
}