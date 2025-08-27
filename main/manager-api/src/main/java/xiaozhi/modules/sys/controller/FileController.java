package xiaozhi.modules.sys.controller;

import cn.hutool.core.date.DateUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import xiaozhi.common.annotation.LogOperation;
import xiaozhi.common.utils.Result;
import xiaozhi.common.utils.ResultUtils;
import xiaozhi.modules.sys.service.SysFileService;
import xiaozhi.modules.sys.service.SysParamsService;

@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;//注入实列

    @Resource
    private SysParamsService sysParamsService;

    @Resource
    private SysFileService sysFileService;


    private String getCurrentPath() {
        return DateUtil.today().replace("-", "/") + "/";
    }

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    @LogOperation("上传文件")
    public FileInfo upload(MultipartFile file) {
        FileInfo fileInfo = fileStorageService.of(file)
                .setPath(getCurrentPath())
                .upload();

        return fileInfo;
    }
    
    /**
     * 上传文件，成功返回文件 url
     */
    //@PostMapping("/upload2")
    /*public String upload2(MultipartFile file) {
        FileInfo fileInfo = fileStorageService.of(file)
                .setPath("upload/") //保存到相对路径下，为了方便管理，不需要可以不写
                .setSaveFilename("image.jpg") //设置保存的文件名，不需要可以不写，会随机生成
                .setObjectId("0")   //关联对象id，为了方便管理，不需要可以不写
                .setObjectType("0") //关联对象类型，为了方便管理，不需要可以不写
                .putAttr("role","admin") //保存一些属性，可以在切面、保存上传记录、自定义存储平台等地方获取使用，不需要可以不写
                .upload();  //将文件上传到对应地方
        return fileInfo == null ? "上传失败！" : fileInfo.getUrl();
    }*/

    /**
     * 上传图片，成功返回文件信息
     * 图片处理使用的是 https://github.com/coobird/thumbnailator
     */
    @PostMapping("/upload-image")
    @Operation(summary = "上传图片")
    @LogOperation("上传图片")
    public FileInfo uploadImage(MultipartFile file) {
        FileInfo fileInfo =  fileStorageService.of(file)
                .setPath(getCurrentPath())
                .image(img -> img.size(1000,1000))  //将图片大小调整到 1000*1000
                .thumbnail(th -> th.size(200,200))  //再生成一张 200*200 的缩略图
                .upload();

        return fileInfo;
    }

    /**
     * 上传文件到指定存储平台，成功返回文件信息
     */
    /*@PostMapping("/upload-platform")
    public FileInfo uploadPlatform(MultipartFile file) {
        return fileStorageService.of(file)
                .setPlatform("aliyun-oss-1")    //使用指定的存储平台
                .upload();
    }*/

    /**
     * 直接读取 HttpServletRequest 中的文件进行上传，成功返回文件信息
     * 使用这种方式有些注意事项，请查看文档 基础功能-上传 章节
     */
    @PostMapping("/upload-request")
    @Operation(summary = "上传")
    public FileInfo uploadPlatform(HttpServletRequest request) {
        return fileStorageService.of(request).setPath(getCurrentPath()).upload();
    }

    @PostMapping("delete")
    @Operation(summary = "删除文件")
    @LogOperation("文件删除")
    public Result<String> delete(@RequestParam("path") String path) throws Exception {
        sysFileService.deleteFile(path);
        return ResultUtils.success("删除成功");
    }
}
