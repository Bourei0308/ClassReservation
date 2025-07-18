package com.example.demo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "relations")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Relation {
	@Id
    private String id;
	private String teacherId;
	private String studentId;
}
