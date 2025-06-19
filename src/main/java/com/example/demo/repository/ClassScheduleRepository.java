package com.example.demo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.entity.ClassSchedule;

public interface ClassScheduleRepository extends MongoRepository<ClassSchedule, String> {
	List<ClassSchedule> findByStudentId(String studentId);

	List<ClassSchedule> findByTeacherId(String teacherId);
}