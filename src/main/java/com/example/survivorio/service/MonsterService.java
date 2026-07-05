package com.example.survivorio.service;

import com.example.survivorio.entity.AppUser;
import com.example.survivorio.entity.Monster;
import com.example.survivorio.repository.MonsterRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class MonsterService {

    private final MonsterRepository repo;

    public MonsterService(MonsterRepository repo) {
        this.repo = repo;
    }

    public List<Monster> getAll(AppUser owner) {
        return repo.findAllByOwnerOrderByCreatedAtDesc(owner);
    }

    public Monster create(AppUser owner, Monster monster) {
        monster.setId(null);
        monster.setOwner(owner);
        if (monster.getCreatedAt() == null || monster.getCreatedAt().isBlank()) {
            monster.setCreatedAt(Instant.now().toString());
        }
        return repo.save(monster);
    }

    public Monster update(AppUser owner, Long id, Monster data) {
        Monster existing = repo.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monster not found"));

        existing.setName(data.getName());
        existing.setType(data.getType());
        existing.setArmorClass(data.getArmorClass());
        existing.setHitPoints(data.getHitPoints());
        existing.setChallenge(data.getChallenge());
        existing.setNotes(data.getNotes());
        existing.setStr(data.getStr());
        existing.setDex(data.getDex());
        existing.setCon(data.getCon());
        existing.setIntel(data.getIntel());
        existing.setWis(data.getWis());
        existing.setCha(data.getCha());
        existing.setAttacks(data.getAttacks());
        existing.setGear(data.getGear());
        existing.setGp(data.getGp());
        existing.setSp(data.getSp());
        existing.setCp(data.getCp());

        if (data.getCreatedAt() != null && !data.getCreatedAt().isBlank()) {
            existing.setCreatedAt(data.getCreatedAt());
        }

        return repo.save(existing);
    }

    public void delete(AppUser owner, Long id) {
        Monster existing = repo.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Monster not found"));

        repo.delete(existing);
    }
}
