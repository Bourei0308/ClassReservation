package com.example.demo.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 初始化 demo 数据
    @PostMapping("/init")
    @Operation(summary = "（テスト）ユーザデータ初期化")
    public String initDemoUsers() {
        userRepository.deleteAll();

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        userRepository.save(new User("0", "admin", "admin@gmail.com", encoder.encode("admin123"), 0,"admin123"));
        userRepository.save(new User("1", "student", "student@gmail.com", encoder.encode("student123"), 1,"student123"));
        userRepository.save(new User("2", "teacher", "teacher@gmail.com", encoder.encode("teacher123"), 2,"teacher123"));

        return "ユーザデータ初期化しました";
    }
    
    @GetMapping("")
    @Operation(summary = "获取所有用户")
    public List<User> getAllUsers() {
    	return userRepository.findAll();
    }


    @PostMapping("/register")
    @Operation(summary = "ユーザ作成")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        boolean accountExists = userRepository.findAll().stream()
            .anyMatch(u -> u.getAccount().equals(user.getAccount()));
        boolean nameExists = userRepository.findAll().stream()
            .anyMatch(u -> u.getName().equals(user.getName()));

        if (accountExists) {
            return ResponseEntity.badRequest().body("account-exists");
        }
        if (nameExists) {
            return ResponseEntity.badRequest().body("name-exists");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/{id}")
    @Operation(summary = "ユーザーをIDで取得")
    public User getUserById(@PathVariable String id) {
        return userRepository.findById(id).orElse(null);
    }

    @PutMapping("/{id}")
    @Operation(summary = "ユーザ情報更新（パスワード以外）")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody User user) {
    	user.setId(id);

        boolean nameExists = userRepository.findAll().stream()
            .anyMatch(u -> !u.getId().equals(id) && u.getName().equals(user.getName()));

        if (nameExists) {
            return ResponseEntity.badRequest().body("name-exists");
        }

        return ResponseEntity.ok(userRepository.save(user));
    }
    


    @DeleteMapping("/{id}")
    @Operation(summary = "ユーザ削除")
    public String deleteUser(@PathVariable String id) {
        userRepository.deleteById(id);
        return "ユーザを削除しました";
    }
    
    @PutMapping("/{id}/password")
    @Operation(summary = "パスワード変更（旧パスワード不要）")
    public ResponseEntity<?> updatePassword(@PathVariable String id, @RequestBody NewPasswordRequest request) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (!optionalUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user-not-found");
        }

        User user = optionalUser.get();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("password-updated");
    }

    
    
    //パスワード変更用DTO
    static class NewPasswordRequest {
        private String newPassword;

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

    
//  @PutMapping("/{id}/remove")
//  @Operation(summary = "ユーザ論理削除")
//  public ResponseEntity<String> softDeleteUser(@PathVariable String id) {
//      Optional<User> optional = userRepository.findById(id);
//      if (optional.isPresent()) {
//          User user = optional.get();
//          user.setRemoveFlag(true);
//          userRepository.save(user);
//          return ResponseEntity.ok("ユーザーを削除済みにしました。");
//      }
//      return ResponseEntity.notFound().build();
//  }
//  
//  @PutMapping("/{id}/restore")
//  @Operation(summary = "ユーザ復元")
//  public ResponseEntity<String> restoreUser(@PathVariable String id) {
//      Optional<User> optional = userRepository.findById(id);
//      if (optional.isPresent()) {
//          User user = optional.get();
//          user.setRemoveFlag(false);
//          userRepository.save(user);
//          return ResponseEntity.ok("ユーザーを復元しました。");
//      }
//      return ResponseEntity.notFound().build();
//  }
    
//    @PostMapping("/{id}/icon")
//    @Operation(summary = "アイコン設定")
//    public ResponseEntity<?> uploadIcon(
//        @PathVariable String id,
//        @RequestParam("file") MultipartFile file,
//        @RequestParam("cropped") MultipartFile croppedFile
//    ) {
//        try {
//        	// 絶対パス
//        	Path uploadDir = Paths.get("C:/pleiades/2025-03/workspace/MinecraftReceiptBrowser/uploads/icons");
//        	Path croppedDir = Paths.get("C:/pleiades/2025-03/workspace/MinecraftReceiptBrowser/uploads/icons/cropped");
//        	
//        	// Directory
//        	Files.createDirectories(uploadDir);
//        	Files.createDirectories(croppedDir);
//        	
//            String rawFilename = UUID.randomUUID() + "_" + file.getOriginalFilename();
//            String croppedFilename = UUID.randomUUID() + "_" + croppedFile.getOriginalFilename();
//
//            Path rawPath = uploadDir.resolve(rawFilename);
//            Path croppedPath = croppedDir.resolve(croppedFilename);
//
//            Files.createDirectories(rawPath.getParent());
//            Files.createDirectories(croppedPath.getParent());
//            file.transferTo(rawPath.toFile());
//            croppedFile.transferTo(croppedPath.toFile());
//
//            User user = userRepository.findById(id).orElseThrow();
//            user.setIcon( croppedFilename);
//            user.setIconRaw( rawFilename);
//            userRepository.save(user);
//
//            return ResponseEntity.ok(Map.of(
//            	    "icon", croppedFilename,
//            	    "iconRaw", rawFilename
//            	));
//        } catch (Exception e) {
//        	e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("upload-error");
//        }
//    }

}

