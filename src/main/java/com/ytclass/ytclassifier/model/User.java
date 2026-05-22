package com.ytclass.ytclassifier.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private String email;

    private String name;

    @Column(unique = true)
    private String googleId;       // OAuth2 subject ID ("sub" claim)

    @Column(unique = true)
    private String yid;            // Dynamic 6-character alphabetic identifier

    private String pictureUrl;

    private String provider;       // e.g. "google"

    private LocalDateTime createdAt;   // First registration time

    private LocalDateTime lastLogin;   // Most recent login
}
