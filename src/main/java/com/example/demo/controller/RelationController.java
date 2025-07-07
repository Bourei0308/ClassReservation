package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Relation;
import com.example.demo.repository.RelationRepository;

@RestController
@RequestMapping("/api/relations")
public class RelationController {

    @Autowired
    private RelationRepository relationRepository;

    // 1. 创建 Relation（自动生成 id + 避免重复）
    @PostMapping
    public ResponseEntity<?> createRelation(@RequestBody Relation relation) {
        // 构造 id：teacherId-studentId
        String id = relation.getTeacherId() + "-" + relation.getStudentId();
        boolean exists = relationRepository.existsById(id);
        if (exists) {
            return ResponseEntity.badRequest().body("该关系已存在，无法重复插入。");
        }

        relation.setId(id); // 设置复合 id
        Relation saved = relationRepository.save(relation);
        return ResponseEntity.ok(saved);
    }

    // 2. 查询所有 Relation
    @GetMapping
    public List<Relation> getAllRelations() {
        return relationRepository.findAll();
    }

    // 3. 根据 ID 删除 Relation
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRelation(@PathVariable("id") String id) {
        if (relationRepository.existsById(id)) {
            relationRepository.deleteById(id);
            return ResponseEntity.ok("已删除");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 4. 查询某教师的所有 studentId
    @GetMapping("/teacher/{teacherId}")
    public List<String> getStudentsByTeacherId(@PathVariable("teacherId") String teacherId) {
        return relationRepository.findByTeacherId(teacherId).stream()
                .map(Relation::getStudentId)
                .collect(Collectors.toList());
    }

    // 5. 查询某学生的所有 teacherId
    @GetMapping("/student/{studentId}")
    public List<String> getTeachersByStudentId(@PathVariable("studentId") String studentId) {
        return relationRepository.findByStudentId(studentId).stream()
                .map(Relation::getTeacherId)
                .collect(Collectors.toList());
    }
}
