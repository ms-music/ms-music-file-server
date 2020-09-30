package com.ms.music.file.server.file_info.service;

import com.alibaba.fastjson.JSONObject;
import com.ms.music.file.server.file_info.entity.FileInfo;
import com.ms.music.file.server.file_info.repository.FileInfoRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
public class FileInfoService {

    private static final Logger logger = LoggerFactory.getLogger(FileInfoService.class);

    private final FileInfoRepository fileInfoRepository;
    private boolean initStatus = true;
    private final String rootDirectory;


    @Autowired
    public FileInfoService(FileInfoRepository fileInfoRepository, @Value("${file-server.root-directory}") String rootDirectory) {
        this.fileInfoRepository = fileInfoRepository;
        this.rootDirectory = rootDirectory;
        File fileDir = new File(rootDirectory);
        if (!fileDir.exists()) {
            initStatus = fileDir.mkdirs();
        }
        if (initStatus) {
            initStatus = fileDir.isDirectory();
        }
    }

    public JSONObject upload(MultipartFile file, String type) {
        if (file != null && initStatus) {
            String nowDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
            StringBuilder currentDirectoryBuilder = new StringBuilder();
            currentDirectoryBuilder.append(rootDirectory);
            currentDirectoryBuilder.append("/");
            currentDirectoryBuilder.append(nowDate);
            currentDirectoryBuilder.append("/");
            currentDirectoryBuilder.append(type);
            currentDirectoryBuilder.append("/");
            File currentDirectory = new File(new String(currentDirectoryBuilder));
            boolean isSuccess = true;
            if (!currentDirectory.exists()) {
                isSuccess = currentDirectory.mkdirs();
            }
            if (!isSuccess) {
                logger.info("创建文件：" + currentDirectoryBuilder + "成功");
            }
            String fileName = file.getOriginalFilename();
            String fileType = file.getContentType();
            if (!StringUtils.isEmpty(fileType)) {
                fileType = fileType.substring(fileType.indexOf("/") + 1);
            }
            long fileSize = file.getSize();
            if (!StringUtils.isEmpty(fileName) && fileSize > 0) {
                String fileId = UUID.randomUUID().toString();
                String path = nowDate + "/" + type + "/" + fileId + "." + fileType;
                FileInfo fileInfo = new FileInfo(fileId, fileName, fileSize, path, type, new Date());
                File saveFile = new File(new String(currentDirectoryBuilder), fileId + "." + fileType);
                try (InputStream inputStream = file.getInputStream(); FileOutputStream outputStream = new FileOutputStream(saveFile)) {
                    FileCopyUtils.copy(inputStream, outputStream);
                    fileInfoRepository.save(fileInfo);
                    return new JSONObject().fluentPut("status", "success").fluentPut("id", fileId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return new JSONObject().fluentPut("status", "error");
    }

    public void download(String id, HttpServletResponse response) {
        if (!StringUtils.isEmpty(id)) {
            FileInfo fileInfo = fileInfoRepository.findById(id).orElse(null);
            if (fileInfo != null) {
                String[] ids = fileInfo.getPath().split("/");
                if (ids.length == 2) {
                    File file = new File(rootDirectory + "/" + ids[0] + "/" + fileInfo.getType() + "/", ids[1]);
                    if (file.exists() && file.isFile()) {
                        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
                        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileInfo.getName());
                        try (FileInputStream inputStream = new FileInputStream(file); ServletOutputStream outputStream = response.getOutputStream()) {
                            FileCopyUtils.copy(inputStream, outputStream);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public FileInfo getVideoUrl(String id) {
        if (!StringUtils.isEmpty(id)) {
            return fileInfoRepository.findById(id).orElse(null);
//            FileInfo fileInfo = fileInfoRepository.findById(id).orElse(null);
//            String[] ids = fileInfo.getPath().split("/");
//            return rootDirectory + ids[0] + "/" + fileInfo.getType() + "/" + ids[1];
        }
        return null;
    }

}
