package com.ms.music.file.server.file_info.controller;

import com.alibaba.fastjson.JSONObject;
import com.ms.music.file.server.file_info.entity.FileInfo;
import com.ms.music.file.server.file_info.service.FileInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@RestController
public class FileInfoController {

    private final FileInfoService fileInfoService;

    @Autowired
    public FileInfoController(FileInfoService fileInfoService) {
        this.fileInfoService = fileInfoService;
    }

    @PostMapping("/upload")
    public JSONObject uploadImage(@RequestParam("file") MultipartFile file, @RequestParam("file_type") String type) {
        return fileInfoService.upload(file, type);
    }

    @GetMapping("/download/{id}")
    public void download(@PathVariable String id, HttpServletResponse response) {
        fileInfoService.download(id, response);
    }

    @GetMapping("/getVideoUrl")
    public FileInfo getVideoUrl(@RequestParam String id) {
        return fileInfoService.getVideoUrl(id);
    }

}
