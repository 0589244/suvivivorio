package com.example.survivorio.repository;

import com.example.survivorio.entity.AppUser;
import com.example.survivorio.entity.Monster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MonsterRepository extends JpaRepository<Monster, Long> {
    List<Monster> findAllByOwnerOrderByCreatedAtDesc(AppUser owner);

    Optional<Monster> findByIdAndOwner(Long id, AppUser owner);
}
