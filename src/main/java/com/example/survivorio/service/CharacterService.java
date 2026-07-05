package com.example.survivorio.service;

import com.example.survivorio.entity.AppUser;
import com.example.survivorio.entity.Character;
import com.example.survivorio.repository.CharacterRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class CharacterService {

    private final CharacterRepository repo;

    public CharacterService(CharacterRepository repo) {
        this.repo = repo;
    }

    public List<Character> getAll(AppUser owner) {
        return repo.findAllByOwnerOrderByCreatedAtDesc(owner);
    }

    public Character create(AppUser owner, Character character) {
        character.setId(null);
        character.setOwner(owner);
        if (character.getCreatedAt() == null || character.getCreatedAt().isBlank()) {
            character.setCreatedAt(Instant.now().toString());
        }

        return repo.save(character);
    }

    public Character update(AppUser owner, Long id, Character data) {
        Character existing = repo.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Character not found"));

        existing.setName(data.getName());
        existing.setAncestry(data.getAncestry());
        existing.setClassName(data.getClassName());
        existing.setLevel(data.getLevel());
        existing.setXp(data.getXp());
        existing.setStr(data.getStr());
        existing.setDex(data.getDex());
        existing.setCon(data.getCon());
        existing.setIntel(data.getIntel());
        existing.setWis(data.getWis());
        existing.setCha(data.getCha());
        existing.setHp(data.getHp());
        existing.setAc(data.getAc());
        existing.setTitle(data.getTitle());
        existing.setAlignment(data.getAlignment());
        existing.setBackground(data.getBackground());
        existing.setDeity(data.getDeity());
        existing.setTalentsSpells(data.getTalentsSpells());
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
        Character existing = repo.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Character not found"));

        repo.delete(existing);
    }
}

