package com.example.demo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "users")
@Data
public class User {
	@Id
	private String id;
	private String name;
	private String email;
	private String password;
	private int role; // 例: 0 = student, 1 = teacher
	private String account;
}
