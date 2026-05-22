package com.ytclass.ytclassifier.repository;

import com.ytclass.ytclassifier.model.User;
import com.ytclass.ytclassifier.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, String> {
    @Query("SELECT v FROM Video v JOIN v.users u WHERE u = :user")
    List<Video> findByUser(@Param("user") User user);
}
