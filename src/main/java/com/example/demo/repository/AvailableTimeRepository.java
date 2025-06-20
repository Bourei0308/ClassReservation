package com.example.demo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.entity.AvailableTime;

public interface AvailableTimeRepository extends MongoRepository<AvailableTime, String> {
	List<AvailableTime> findByTeacherId(String teacherId);
    List<AvailableTime> findByTeacherIdAndYearAndMonth(String teacherId, String year, String month);
}