package com.example.demo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.AvailableTime;

public interface AvailableTimeRepository extends MongoRepository<AvailableTime, String> {
	List<AvailableTime> findByTeacherId(String teacherId);
    @Query("SELECT a FROM AvailableTime a WHERE a.teacherId = :teacherId AND FUNCTION('YEAR', a.date) = :year AND FUNCTION('MONTH', a.date) = :month")
    List<AvailableTime> findByTeacherIdAndYearAndMonth(
            @Param("teacherId") String teacherId,
            @Param("year") String year,
            @Param("month") String month
    );
}