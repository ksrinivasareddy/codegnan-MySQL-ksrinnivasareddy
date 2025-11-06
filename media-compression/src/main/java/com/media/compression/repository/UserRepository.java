package com.media.compression.repository;

import com.media.compression.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    // Extra custom queries (optional)
}
