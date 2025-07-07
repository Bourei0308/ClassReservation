package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Relation;

@Repository
public interface RelationRepository extends MongoRepository<Relation, String> {
    Optional<Relation> findByTeacherIdAndStudentId(String teacherId, String studentId);
    List<Relation> findByTeacherId(String teacherId);
    List<Relation> findByStudentId(String studentId);
}


