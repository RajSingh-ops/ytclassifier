package com.ytclass.ytclassifier.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "videos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Video implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String videoId;

    private String title;
    
    private String channelName;

    private int credibilityScore;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = java.time.LocalDateTime.now();
        }
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_videos",
        joinColumns = @JoinColumn(name = "video_id"),
        inverseJoinColumns = @JoinColumn(name = "user_email")
    )
    private java.util.List<User> users = new java.util.ArrayList<>();
}