package com.example.survivorio.service;

import com.example.survivorio.entity.Monster;
import com.example.survivorio.repository.MonsterRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class MonsterService {

    private final MonsterRepository repo;

    public MonsterService(MonsterRepository repo) {
        this.repo = repo;
    }

    public List<Monster> getAll() {
        return repo.findAll();
    }

    public Monster create(Monster monster) {
        if (monster.getCreatedAt() == null) {
            monster.setCreatedAt(Instant.now().toString());
        }
        return repo.save(monster);
    }
}

