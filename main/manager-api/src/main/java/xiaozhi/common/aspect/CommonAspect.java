package xiaozhi.common.aspect;

import cn.hutool.core.lang.Assert;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import xiaozhi.common.annotation.RateLimit;

import java.lang.reflect.Method;
import java.time.Duration;

@Aspect
@Component
@AllArgsConstructor
public class CommonAspect {

    private final RedissonClient redissonClient;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint pjp, RateLimit rateLimit) throws Throwable {
        //Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        Object[] args = pjp.getArgs();

        String key = "rate_limit:"+rateLimit.key_pre() +":"+ DigestUtil.sha256Hex(JSONUtil.toJsonStr(args));

        // 分布式限流器
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        boolean firstTime = rateLimiter.trySetRate(RateType.OVERALL, 1, Duration.ofSeconds(rateLimit.seconds()));

        if(firstTime){
            rateLimiter.expire(Duration.ofSeconds(rateLimit.seconds())); // 设置过期时间
        }

        // 拦截重复请求
        Assert.isTrue(rateLimiter.tryAcquire()&&firstTime, "限定时间内重复请求，已忽略");

        // 继续执行原方法
        return pjp.proceed();
    }
}
