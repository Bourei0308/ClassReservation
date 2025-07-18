package com.example.demo.controller;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.repository.ImageRepository;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/images")
public class ImageController {

    @Autowired
    private ImageRepository imageRepository;

    // イメージアップロードAPI
    @PostMapping("/upload")
    @Operation(summary = "画像アプロード")
    public String uploadImage(@RequestParam("file") MultipartFile file) throws Exception {
        ObjectId id = imageRepository.saveImage(file);
        return id.toHexString();
    }

    // イメージ獲得
    @Operation(summary = "画像取得")
    @GetMapping("/{id}")
    public ResponseEntity<InputStreamResource> getImage(@PathVariable("id") String id) throws Exception {
        GridFsResource resource = imageRepository.getImageById(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(resource.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(new InputStreamResource(resource.getInputStream()));
    }
}

