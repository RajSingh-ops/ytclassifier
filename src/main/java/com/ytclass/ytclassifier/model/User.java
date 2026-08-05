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
    private String googleId;       
    @Column(unique = true)
    private String yid;            
    private String pictureUrl;
    private String provider;       
    private LocalDateTime createdAt;   
    private LocalDateTime lastLogin;   
}
