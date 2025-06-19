package com.example.demo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "users")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {
	@Id
	private String id;
	private String name;
	private String email;
	private String password;
	private int role; // 0 = admin, 1 = student, 2 = teacher
	private String account;
}
