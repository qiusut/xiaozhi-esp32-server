package xiaozhi.common.redis;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import xiaozhi.modules.sys.dao.SysUserDao;
import xiaozhi.modules.sys.entity.SysUserEntity;
import xiaozhi.modules.sys.service.SysUserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 公共缓存Cache
 *
 * @author qiust
 */
@Component
@DependsOn("systemInitConfig")
public class SysCommonCache {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private SysUserDao sysUserDao;

    private static SysCommonCache sysCommonCache;
    private static RedisTemplate<String, Object> staticRedisTemplate;
    private static SysUserDao staticSysUserDao;


    // 在启动时执行
    @PostConstruct
    public void init() {
        sysCommonCache = this;
        staticRedisTemplate = redisTemplate;
        staticSysUserDao = sysUserDao;
        reload();
    }
    /**
     * 重新加载
     */
    @Async
    public void reload() {
        redisTemplate.opsForValue().set("aes", RandomUtil.randomString(16));

        List<SysUserEntity> list = staticSysUserDao.selectList(null);
        Map<String, String> cacheMap = new HashMap<>();
        if(CollectionUtil.isNotEmpty(list)){
            for (SysUserEntity e : list){
                cacheMap.put(String.valueOf(e.getId()), e.getUsername());
            }
            redisTemplate.opsForHash().putAll("sys:user", cacheMap);
        }
    }

    /**
     * 静态方法：根据用户ID获取用户名
     */
    public static String getUserNameById(String userId) {
        if (staticRedisTemplate == null || userId == null) {
            return null;
        }
        return (String) staticRedisTemplate.opsForHash().get("sys:user", userId);
    }

    /**
     * 静态方法：从缓存或数据库获取用户名（缓存无时回查 DB）
     */
    public static String getUserNameWithFallback(String userId) {
        String username = "";
        if(StrUtil.isNotBlank(userId)){
            username = getUserNameById(userId);
            if (username == null && staticSysUserDao != null) {
                SysUserEntity user = staticSysUserDao.selectById(Long.valueOf(userId));
                if (user != null) {
                    username = user.getUsername();
                    staticRedisTemplate.opsForHash().put("sys:user", userId, username);
                }
            }
        }

        return username;
    }


}