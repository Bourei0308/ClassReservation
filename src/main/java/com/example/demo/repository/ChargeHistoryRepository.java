package com.example.demo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.entity.ChargeHistory;

public interface ChargeHistoryRepository extends MongoRepository<ChargeHistory, String> {
	List<ChargeHistory> findByStudentId(String studentId);
}
