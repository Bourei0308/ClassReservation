package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.entity.AvailableTime;

public interface AvailableTimeRepository extends MongoRepository<AvailableTime, String> {
	List<AvailableTime> findByTeacherId(String teacherId);

	// ✅ 年月の範囲検索（MongoDB向け）
	List<AvailableTime> findByTeacherIdAndStartTimeBetween(
			String teacherId,
			LocalDateTime start,
			LocalDateTime end);
}
