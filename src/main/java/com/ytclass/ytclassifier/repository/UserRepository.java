package com.ytclass.ytclassifier.repository;

import com.ytclass.ytclassifier.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findFirstByOrderByCreatedAtAsc();
    Optional<User> findByYid(String yid);
}
