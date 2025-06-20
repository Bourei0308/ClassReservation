package com.example.demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.entity.User;

public interface UserRepository extends MongoRepository<User, String> {
	// 例：emailで検索するなど追加メソッドも可能
	User findByEmail(String email);
}