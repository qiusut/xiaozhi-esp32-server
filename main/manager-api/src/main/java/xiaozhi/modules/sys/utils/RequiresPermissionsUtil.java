package xiaozhi.modules.sys.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Shiro @RequiresPermissions 注解 权限标识
 */
public class RequiresPermissionsUtil {

    /**
     * 获取 @RequiresPermissionsList 注解的权限标识列表
     *
     * @return 权限标识列表
     */
    public static List<String> getRequiresPermissionsList() {
        List<String> authorities = new ArrayList<>();
        ApplicationContext context = SpringUtil.getApplicationContext();
        String[] beanNames = context.getBeanNamesForAnnotation(Controller.class);
        if(ObjectUtil.isNotEmpty(beanNames)){
            for (String beanName : beanNames) {
                Object bean = context.getBean(beanName);
                Method[] methods = bean.getClass().getDeclaredMethods();
                for (Method method : methods) {
                    RequiresPermissions requiresPermissions = AnnotationUtils.findAnnotation(method, RequiresPermissions.class);
                    if (requiresPermissions != null) {
                        String[] value = requiresPermissions.value();
                        if(ObjectUtil.isNotEmpty(value)){
                            authorities.addAll(CollUtil.toList(value));
                        }
                    }
                }
            }
        }

        return authorities;
    }

}