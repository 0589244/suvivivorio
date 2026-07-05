package com.example.survivorio.repository;

import com.example.survivorio.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    boolean existsByUsernameIgnoreCase(String username);

    Optional<AppUser> findByUsernameIgnoreCase(String username);
}
