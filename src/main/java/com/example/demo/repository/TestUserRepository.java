package com.example.demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.entity.TestUser;

public interface TestUserRepository extends MongoRepository<TestUser, String> {
    // 可以添加自定义查询，如：
    TestUser findByUsername(String username);
}
