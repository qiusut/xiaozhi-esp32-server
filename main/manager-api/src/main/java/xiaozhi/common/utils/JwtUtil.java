package xiaozhi.common.utils;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import org.springframework.data.redis.core.RedisTemplate;
import xiaozhi.modules.sys.service.SysParamsService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class JwtUtil {
    private static final String TOKEN_KEY = "sys:token:";
    private static final long ACCESS_EXPIRE = 1000 * 60 * 60; // 60分钟

    @SuppressWarnings("unchecked")
    private static final RedisTemplate<String, String> redisTemplate = SpringUtil.getBean("redisTemplate", RedisTemplate.class);
    private static final SysParamsService sysParamsService = SpringUtil.getBean(SysParamsService.class);

    /**
     * 获取密钥（可选，我这里做的是动态配置的，可以根据需要写死就行）
     * @return 密钥
     */
    private static byte[] getRefreshJwtSecret() {
        String jwtSecret = RandomUtil.randomString(5);
        return jwtSecret.getBytes();
    }

    private static byte[] getAccessSecret(Long userId,String deviceType) {

        String redis_refreshToken = redisTemplate.opsForValue().get(TOKEN_KEY + userId+":"+deviceType);
        Assert.isTrue(StrUtil.isNotBlank(redis_refreshToken), "Token已过期");
        String last5Chars = redis_refreshToken.substring(redis_refreshToken.length() - 5);
        return last5Chars.getBytes();
    }

    /**
     * 获取刷新token过期时间-单位天（可选，我这里做的是动态配置的，可以根据需要写死就行）
     * @return 过期时间-单位天
     */
    private static int getRefreshExp() {
        String refreshExp = sysParamsService.getValue("jwt.exp", true);
        return Integer.parseInt(refreshExp);
    }

    // 生成双Token
    public static Map<String, String> generateTokens(Long userId,String username,String deviceType) {
        Map<String, String> tokens = new HashMap<>();
        // Refresh Token
        tokens.put("refreshToken", createRefreshToken(userId,deviceType));
        // Access Token
        tokens.put("accessToken", createToken(userId,username,deviceType));

        return tokens;
    }

    public static String createToken(Long userId,String username,String deviceType) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("username", username);
        payload.put("type", "access");
        payload.put("deviceType", deviceType);
        payload.put("exp", (System.currentTimeMillis() + ACCESS_EXPIRE)/1000);

        return JWTUtil.createToken(payload, getAccessSecret(userId,deviceType));
    }

    public static String createRefreshToken(Long userId,String deviceType) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("type", "refresh");
        payload.put("deviceType", deviceType);
        String refreshToken = JWTUtil.createToken(payload, getRefreshJwtSecret());
        redisTemplate.opsForValue().set(TOKEN_KEY+userId+":"+deviceType, refreshToken, getRefreshExp(), TimeUnit.DAYS);
        //redisTemplate.opsForValue().set(TOKEN_KEY+userId,refreshToken, 2, TimeUnit.MINUTES);//测试使用
        return refreshToken;
    }

    // 刷新Token
    public static void refreshAccessToken(String refreshToken) {

        //Assert.isTrue(JWTUtil.verify(refreshToken, getRefreshJwtSecret()), "非法Token错误");
        JWT jwt = JWTUtil.parseToken(refreshToken);
        Assert.isTrue(ObjectUtil.equals("refresh", jwt.getPayload("type")), "非法Token错误");

        Long userId = Convert.toLong(jwt.getPayload("userId"));
        String deviceType = jwt.getPayload("deviceType").toString();

        String redis_refreshToken = redisTemplate.opsForValue().get(TOKEN_KEY + userId+":"+deviceType);
        Assert.isTrue(ObjectUtil.equals(redis_refreshToken, refreshToken), "Token已过期");

        //可选 用于延长缓存时间
        /*long expire = redisTemplate.getExpire(TOKEN_KEY + userId+":"+deviceType, TimeUnit.DAYS);
        if(expire == 0){
            redisTemplate.expire(TOKEN_KEY + userId+":"+deviceType, 7, TimeUnit.DAYS);
        }*/

        redisTemplate.expire(TOKEN_KEY + userId+":"+deviceType, getRefreshExp(), TimeUnit.DAYS);
        //redisTemplate.expire(TOKEN_KEY+userId,refreshToken, 2, TimeUnit.MINUTES);//测试使用

    }

    /**
     * 从Token中获取用户Id
     * @param refreshToken JWT Token字符串
     * @return 用户Id
     */
    public static Map<String, Object> getValueFromRefreshToken(String refreshToken) {
        Map<String, Object> result = new HashMap<>();
        JWT jwt = JWTUtil.parseToken(refreshToken);
        Assert.isTrue(ObjectUtil.equals("refresh", jwt.getPayload("type")), "非法Token错误");
        Long userId = Convert.toLong(jwt.getPayload("userId"));
        String deviceType = jwt.getPayload("deviceType").toString();
        result.put("userId", userId);
        result.put("deviceType", deviceType);

        long expire = redisTemplate.getExpire(TOKEN_KEY + userId+":"+deviceType, TimeUnit.DAYS);
        Assert.isTrue(expire >= 0, "Token已过期");

        String redis_refreshToken = redisTemplate.opsForValue().get(TOKEN_KEY + userId+":"+deviceType);
        Assert.isTrue(ObjectUtil.equals(redis_refreshToken, refreshToken), "Token已过期");

        return result;

    }

    /**
     * 从Token中获取用户Id
     * @param refreshToken JWT Token字符串
     * @return 用户Id
     */
    public static Long getUserIdFromRefreshToken(String refreshToken) {
        //Assert.isTrue(JWTUtil.verify(refreshToken, getRefreshJwtSecret()), "非法Token错误");
        JWT jwt = JWTUtil.parseToken(refreshToken);
        Assert.isTrue(ObjectUtil.equals("refresh", jwt.getPayload("type")), "非法Token错误");
        Long userId = Convert.toLong(jwt.getPayload("userId"));
        String deviceType = jwt.getPayload("deviceType").toString();

        long expire = redisTemplate.getExpire(TOKEN_KEY + userId+":"+deviceType, TimeUnit.DAYS);
        Assert.isTrue(expire >= 0, "Token已过期");

        String redis_refreshToken = redisTemplate.opsForValue().get(TOKEN_KEY + userId+":"+deviceType);
        Assert.isTrue(ObjectUtil.equals(redis_refreshToken, refreshToken), "Token已过期");

        return userId;

    }

    /**
     * 从Token中获取用户Id
     * @param token JWT Token字符串
     * @return 用户Id
     */
    public static Long getUserIdFromToken(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        Long userId = Convert.toLong(jwt.getPayload("userId"));
        String deviceType = jwt.getPayload("deviceType").toString();
        byte[] key = getAccessSecret(userId,deviceType);
        Assert.isTrue(JWTUtil.verify(token, key), "该账号已在其它设备登入");
        Assert.isTrue(ObjectUtil.equals(jwt.getPayload("type"),"access"), "非法Token错误");
        Assert.isTrue(jwt.getPayload("exp")!=null && jwt.setKey(key).validate(0),"token已失效");
        return userId;
    }

    /**
     * 从Token中获取用户名
     * @param token JWT Token字符串
     * @return 用户名
     */
    public static String getUsernameFromToken(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        Long userId = Convert.toLong(jwt.getPayload("userId"));
        String deviceType = jwt.getPayload("deviceType").toString();
        byte[] key = getAccessSecret(userId,deviceType);
        Assert.isTrue(JWTUtil.verify(token, key), "非法Token错误");
        Assert.isTrue(jwt.getPayload("exp")!=null && jwt.setKey(key).validate(0),"token已失效");
        return jwt.getPayload("username").toString();

    }


    public static void main(String[] args) {
        System.out.println(createToken(1L,"admin","app"));
    }

}