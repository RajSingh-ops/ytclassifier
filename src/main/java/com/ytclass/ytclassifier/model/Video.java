package com.ytclass.ytclassifier.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "videos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Video {

    @Id
    private String videoId;

    private String title;
    
    private String channelName;

    private int credibilityScore;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String explanation;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_videos",
        joinColumns = @JoinColumn(name = "video_id"),
        inverseJoinColumns = @JoinColumn(name = "user_email")
    )
    private java.util.List<User> users = new java.util.ArrayList<>();
}