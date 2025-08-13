package xiaozhi.common.annotation;

import java.lang.annotation.*;

/**
 * 防止重复提交注解（基于 Redisson 的分布式限流）
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RateLimit {
    /**
     * 时间窗口（秒）
     */
    int seconds() default 60;

    /**
     * 时间窗口（秒）
     */
    String key_pre();
}
