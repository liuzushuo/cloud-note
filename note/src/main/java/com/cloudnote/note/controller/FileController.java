package com.cloudnote.note.controller;

import cn.hutool.core.lang.Validator;
import com.cloudnote.common.constants.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/file")
public class FileController {


    /**
     * 富文本编辑器——图像上传
     *
     * @param file 图像文件
     * @return
     */
    @PostMapping("/upload/pic")
    public Map uploadPic(@RequestParam("upload") MultipartFile file, HttpServletRequest request) {
        Map<String, Object> responseData = new HashMap<>();
        Map<String, String> responseDataInfo = new HashMap<>();
        // 1.判断文件是否为空
        if (file.isEmpty()) {
            responseDataInfo.put("message", "未找到图片源！");
            responseData.put("error", responseDataInfo);
            return responseData;
        }
        // 2. 构造文件存储路径
        // 上传到哪个磁盘文件夹下
        String fileMkdirsPath = "E:" + File.separator + "ck-file" + File.separator + "image";
        // 上传到哪个磁盘文件夹下的虚拟路径地址
        String urlImagePath = request.getScheme() + "://" + request.getLocalAddr() + ":" + request.getServerPort() + request.getContextPath() + "/image/";

        // 获取上传文件的名称
        String originalFilename = file.getOriginalFilename();
        if (Validator.isEmpty(originalFilename)) {
            responseDataInfo.put("message", "未找到图片源！");
            responseData.put("error", responseDataInfo);
            return responseData;
        }
        // 获取上传文件的后缀名
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 上传后的文件名称
        String fileName = System.currentTimeMillis() + extension;

        // 判断上传到的文件夹是否存在
        File temp = new File(fileMkdirsPath);
        if (!temp.exists()) {
            // 不存在则创建文件夹
            temp.mkdirs();
        }

        // 最终上传的文件对象
        File localFile = new File(fileMkdirsPath + File.separator + fileName);
        try {
            // 3.上传文件
            file.transferTo(localFile);
        } catch (IOException e) {
            e.printStackTrace();
            responseData.put("code", HttpStatus.ERROR);
            responseDataInfo.put("message", "上传失败！");
            responseData.put("error", responseDataInfo);
            return responseData;
        }

        // 4.构造返回值
        responseData.put("code", HttpStatus.SUCCESS);
        responseData.put("url", urlImagePath + fileName);
        return responseData;
    }

}
