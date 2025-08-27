package xiaozhi.modules.sys.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.errors.*;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.dromara.x.file.storage.core.platform.FileStorage;
import org.dromara.x.file.storage.core.platform.MinioFileStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xiaozhi.modules.sys.service.SysFileService;
import xiaozhi.modules.sys.service.SysParamsService;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@AllArgsConstructor
@Service
public class SysFileServiceImpl implements SysFileService {

    @Resource
    private FileStorageService fileStorageService;//注入实列


    @Override
    public void deleteFile(String path) {
        String defaultPlatform = SpringUtil.getProperty("dromara.x-file-storage.default-platform");

        // 截取最后一个分隔符之前的字符串
        String beforeLastSlash = StringUtils.substringBeforeLast(path, "/");  // "folder/subfolder"

        // 截取最后一个分隔符之后的字符串
        String afterLastSlash = StringUtils.substringAfterLast(path, "/");    // "filename.txt"
        //手动构造文件信息，可用于其它操作
        FileInfo fileInfo = new FileInfo()
                .setPlatform(defaultPlatform)
                //.setBasePath(basePath)
                .setPath(beforeLastSlash+"/")
                .setFilename(afterLastSlash)
                .setThFilename("image.png.min.jpg");
        //判断文件是否存在
        boolean exists = fileStorageService.exists(fileInfo);
        Assert.isTrue(exists,"文件不存在");
        //删除
        fileStorageService.delete(fileInfo);
        /*try {
            if(defaultPlatform.contains("minio")){
                MinioFileStorage minioFileStorage = fileStorageService.getFileStorage();
                MinioClient minioClient = minioFileStorage.getClient();
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(minioFileStorage.getBucketName())
                                .object("/2025/08/22/68a7c350305d88fd41ddfd6a.jpeg")
                                .build());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/
    }

    public String getStorageValue(String filePlatform,String key) {
        // 获取 Environment
        String defaultPlatform = SpringUtil.getProperty("dromara.x-file-storage.default-platform");

        if (defaultPlatform == null) {
            return null;
        }
        if(StrUtil.isBlank(filePlatform)){
            filePlatform = StringUtils.substringBeforeLast(filePlatform, "-");;
        }

        // 遍历查找匹配的平台配置
        for (int i = 0; ; i++) {
            String platform = SpringUtil.getProperty("dromara.x-file-storage.minio[" + i + "].platform");

            // 如果没有更多配置项，跳出循环
            if (platform == null) {
                break;
            }

            // 如果找到匹配的平台
            if (defaultPlatform.equals(platform)) {
                // 返回对应的 bucket-name
                return SpringUtil.getProperty("dromara.x-file-storage.minio[" + i + "]."+key);
            }
        }

        return null; // 未找到匹配项
    }


}
