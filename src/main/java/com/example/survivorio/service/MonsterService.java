package com.example.survivorio.service;

import com.example.survivorio.entity.Monster;
import com.example.survivorio.repository.MonsterRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;


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

    public Monster getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Monster not found: " + id));
    }

    public Monster patch(Long id, Map<String, Object> updates) {
        Monster m = getById(id);

        if (updates.containsKey("name")) m.setName((String) updates.get("name"));
        if (updates.containsKey("type")) m.setType((String) updates.get("type"));
        if (updates.containsKey("armorClass")) m.setArmorClass(((Number) updates.get("armorClass")).intValue());
        if (updates.containsKey("hitPoints")) m.setHitPoints(((Number) updates.get("hitPoints")).intValue());
        if (updates.containsKey("challenge")) m.setChallenge((String) updates.get("challenge"));
        if (updates.containsKey("notes")) m.setNotes((String) updates.get("notes"));

        if (updates.containsKey("attacks"))
            m.setAttacks((String) updates.get("attacks"));

        if (updates.containsKey("talents"))
            m.setTalents((String) updates.get("talents"));

        if (updates.containsKey("spells"))
            m.setSpells((String) updates.get("spells"));

        if (updates.containsKey("gear"))
            m.setGear((String) updates.get("gear"));


        return repo.save(m);
    }

}

