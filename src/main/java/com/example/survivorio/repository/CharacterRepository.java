package com.example.survivorio.repository;

import com.example.survivorio.entity.Character;
import com.example.survivorio.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CharacterRepository extends JpaRepository<Character, Long> {
    List<Character> findAllByOwnerOrderByCreatedAtDesc(AppUser owner);

    Optional<Character> findByIdAndOwner(Long id, AppUser owner);
}
