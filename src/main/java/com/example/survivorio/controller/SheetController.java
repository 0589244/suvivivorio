package com.example.survivorio.controller;

import com.example.survivorio.entity.Character;
import com.example.survivorio.entity.Monster;
import com.example.survivorio.service.CharacterService;
import com.example.survivorio.service.MonsterService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class SheetController {

    private final CharacterService characterService;
    private final MonsterService monsterService;

    public SheetController(CharacterService characterService, MonsterService monsterService) {
        this.characterService = characterService;
        this.monsterService = monsterService;
    }

    @GetMapping("/characters")
    public List<Character> getCharacters() {
        return characterService.getAll();
    }

    @PostMapping("/characters")
    public Character createCharacter(@RequestBody Character character) {
        return characterService.create(character);
    }

    @GetMapping("/monsters")
    public List<Monster> getMonsters() {
        return monsterService.getAll();
    }

    @PostMapping("/monsters")
    public Monster createMonster(@RequestBody Monster monster) {
        return monsterService.create(monster);
    }

    @GetMapping("/characters/{id}")
    public Character getCharacter(@PathVariable Long id) {
        return characterService.getById(id);
    }

    @PatchMapping("/characters/{id}")
    public Character patchCharacter(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return characterService.patch(id, updates);
    }

    @GetMapping("/monsters/{id}")
    public Monster getMonster(@PathVariable Long id) {
        return monsterService.getById(id);
    }

    @PatchMapping("/monsters/{id}")
    public Monster patchMonster(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return monsterService.patch(id, updates);
    }
}

