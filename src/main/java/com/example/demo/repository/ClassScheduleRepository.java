package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.entity.ClassSchedule;

public interface ClassScheduleRepository extends MongoRepository<ClassSchedule, String> {
	List<ClassSchedule> findByStudentId(String studentId);

	List<ClassSchedule> findByTeacherId(String teacherId);

	// 日付の範囲で取得（メール当日分）
	List<ClassSchedule> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

	// ClassScheduleRepository.java

	List<ClassSchedule> findByStartTimeBetweenAndIsRemindedFalse(LocalDateTime start, LocalDateTime end);

	List<ClassSchedule> findByAvailableTimeId(String availableTimeId);

}