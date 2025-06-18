package com.example.demo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "test_users")
@Schema(description = "テストユーザー")
public class TestUser {

    @Id
    @Schema(description = "ユーザーID", example = "64a123abc456")
    private String id;

    @Schema(description = "ユーザー名", example = "test_user")
    private String username;

    @Schema(description = "メール", example = "user@example.com")
    private String email;
}
