package com.example.application.repository;

import com.example.application.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);
}
