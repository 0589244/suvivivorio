package com.example.survivorio.service;

import com.example.survivorio.entity.Character;
import com.example.survivorio.repository.CharacterRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;


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

    public Character getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Character not found: " + id));
    }

    public Character patch(Long id, Map<String, Object> updates) {
        Character c = getById(id);

        if (updates.containsKey("name")) c.setName((String) updates.get("name"));
        if (updates.containsKey("ancestry")) c.setAncestry((String) updates.get("ancestry"));
        if (updates.containsKey("className")) c.setClassName((String) updates.get("className"));
        if (updates.containsKey("level")) c.setLevel(((Number) updates.get("level")).intValue());
        if (updates.containsKey("xp")) c.setXp(((Number) updates.get("xp")).intValue());

        if (updates.containsKey("str")) c.setStr(((Number) updates.get("str")).intValue());
        if (updates.containsKey("dex")) c.setDex(((Number) updates.get("dex")).intValue());
        if (updates.containsKey("con")) c.setCon(((Number) updates.get("con")).intValue());
        if (updates.containsKey("intel")) c.setIntel(((Number) updates.get("intel")).intValue());
        if (updates.containsKey("wis")) c.setWis(((Number) updates.get("wis")).intValue());
        if (updates.containsKey("cha")) c.setCha(((Number) updates.get("cha")).intValue());

        if (updates.containsKey("hp")) c.setHp(((Number) updates.get("hp")).intValue());
        if (updates.containsKey("ac")) c.setAc(((Number) updates.get("ac")).intValue());

        if (updates.containsKey("title")) c.setTitle((String) updates.get("title"));
        if (updates.containsKey("alignment")) c.setAlignment((String) updates.get("alignment"));
        if (updates.containsKey("background")) c.setBackground((String) updates.get("background"));
        if (updates.containsKey("deity")) c.setDeity((String) updates.get("deity"));

        if (updates.containsKey("attacks"))
            c.setAttacks((String) updates.get("attacks"));

        if (updates.containsKey("talents"))
            c.setTalents((String) updates.get("talents"));

        if (updates.containsKey("spells"))
            c.setSpells((String) updates.get("spells"));

        if (updates.containsKey("gear"))
            c.setGear((String) updates.get("gear"));

        if (updates.containsKey("notes"))
            c.setNotes((String) updates.get("notes"));

        if (updates.containsKey("title")) c.setTitle((String) updates.getOrDefault("title", ""));


        // createdAt nicht patchen (optional), eher beim Create setzen
        return repo.save(c);
    }

}
