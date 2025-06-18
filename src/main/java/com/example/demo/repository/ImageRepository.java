package com.example.demo.repository;

import static org.springframework.data.mongodb.core.query.Criteria.*;
import static org.springframework.data.mongodb.core.query.Query.*;

import java.io.IOException;
import java.io.InputStream;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.client.gridfs.model.GridFSFile;

@Repository
public class ImageRepository {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    // イメージアプロード
    public ObjectId saveImage(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            return gridFsTemplate.store(inputStream, file.getOriginalFilename(), file.getContentType());
        }
    }

    // イメージ取得
    public GridFsResource getImageById(String id) throws IllegalStateException {
        GridFSFile file = gridFsTemplate.findOne(query(where("_id").is(id)));
        if (file == null) {
            throw new IllegalStateException("File not found");
        }
        return gridFsTemplate.getResource(file);
    }
}
