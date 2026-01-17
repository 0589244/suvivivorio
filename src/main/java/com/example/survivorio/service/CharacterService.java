package com.example.survivorio.service;

import com.example.survivorio.entity.Character;
import com.example.survivorio.repository.CharacterRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class CharacterService {

    private final CharacterRepository repo;

    public CharacterService(CharacterRepository repo) {
        this.repo = repo;
    }

    public List<Character> getAll() {
        return repo.findAll();
    }

    public Character create(Character character) {
        // createdAt nur setzen, wenn es fehlt (Entity hat bei dir String createdAt)
        if (character.getCreatedAt() == null || character.getCreatedAt().isBlank()) {
            character.setCreatedAt(Instant.now().toString());
        }

        // id sollte bei POST am besten null sein (JPA generiert sie), aber save() klappt auch so.
        return repo.save(character);
    }
}
