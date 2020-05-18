package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import org.apache.commons.io.FilenameUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

/**
 * @author Administrator
 * @create 2020-05-13 22:42
 */
@RestController
@RequestMapping("/admin/product")
public class FileUploadController {
    @Value("${fileServer.url}")
    private String fileServerUrl;
    @RequestMapping("/fileUpload")
    public Result fileUpload(MultipartFile file) throws IOException, MyException {
        //初始化配置文件
        String path = ClassUtils.getDefaultClassLoader().getResource("tracker.conf").getPath();
        ClientGlobal.init(path);
        //连接跟踪点
        TrackerClient trackerClient = new TrackerClient();
        //连接
        TrackerServer trackerServer = trackerClient.getConnection();
        //连接到指定的存储节点
        StorageClient1 storageClient1 = new StorageClient1(trackerServer, null);
        //上传
        //获取上传文件的后缀
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        String fileId = storageClient1.upload_file1(file.getBytes(), extension, null);
        return Result.ok(fileServerUrl + fileId);
    }
}
