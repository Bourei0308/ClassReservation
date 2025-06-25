// UserRepository.java
package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.entity.User;

public interface UserRepository extends MongoRepository<User, String> {
	List<User> findByRole(int role); // 生徒のみ取得

	Optional<User> findByEmail(String email);

	Optional<User> findByEmailAndName(String email, String name);
}
