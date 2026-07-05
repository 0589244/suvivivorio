package com.example.survivorio.repository;

import com.example.survivorio.entity.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthSessionRepository extends JpaRepository<AuthSession, Long> {
    Optional<AuthSession> findByToken(String token);

    void deleteByToken(String token);
}
