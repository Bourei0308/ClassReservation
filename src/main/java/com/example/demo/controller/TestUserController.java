package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.TestUser;
import com.example.demo.repository.TestUserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/testusers")
@Tag(name = "TestUser API", description = "テストユーザーAPI")
public class TestUserController {

    @Autowired
    private TestUserRepository userRepository;

    @GetMapping
    @Operation(summary = "ユーザー獲得")
    public List<TestUser> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping
    @Operation(summary = "ユーザー追加")
    public TestUser createUser(@RequestBody TestUser user) {
        return userRepository.save(user);
    }

    @GetMapping("/{id}")
    @Operation(summary = "IDでユーザー獲得")
    public TestUser getUserById(@PathVariable String id) {
        return userRepository.findById(id).orElse(null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "IDでユーザー削除")
    public void deleteUser(@PathVariable String id) {
        userRepository.deleteById(id);
    }
}