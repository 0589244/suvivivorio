package com.example.survivorio.controller;

import com.example.survivorio.entity.AppUser;
import com.example.survivorio.entity.Character;
import com.example.survivorio.entity.Monster;
import com.example.survivorio.service.AuthService;
import com.example.survivorio.service.CharacterService;
import com.example.survivorio.service.MonsterService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class SheetController {

    private final CharacterService characterService;
    private final MonsterService monsterService;
    private final AuthService authService;

    public SheetController(CharacterService characterService, MonsterService monsterService, AuthService authService) {
        this.characterService = characterService;
        this.monsterService = monsterService;
        this.authService = authService;
    }

    @GetMapping("/characters")
    public List<Character> getCharacters(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        AppUser user = authService.requireUser(authorizationHeader);
        return characterService.getAll(user);
    }

    @PostMapping("/characters")
    public Character createCharacter(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody Character character
    ) {
        AppUser user = authService.requireUser(authorizationHeader);
        return characterService.create(user, character);
    }

    @PutMapping("/characters/{id}")
    public Character updateCharacter(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable Long id,
            @RequestBody Character character
    ) {
        AppUser user = authService.requireUser(authorizationHeader);
        return characterService.update(user, id, character);
    }

    @DeleteMapping("/characters/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCharacter(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable Long id
    ) {
        AppUser user = authService.requireUser(authorizationHeader);
        characterService.delete(user, id);
    }

    @GetMapping("/monsters")
    public List<Monster> getMonsters(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        AppUser user = authService.requireUser(authorizationHeader);
        return monsterService.getAll(user);
    }

    @PostMapping("/monsters")
    public Monster createMonster(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody Monster monster
    ) {
        AppUser user = authService.requireUser(authorizationHeader);
        return monsterService.create(user, monster);
    }

    @PutMapping("/monsters/{id}")
    public Monster updateMonster(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable Long id,
            @RequestBody Monster monster
    ) {
        AppUser user = authService.requireUser(authorizationHeader);
        return monsterService.update(user, id, monster);
    }

    @DeleteMapping("/monsters/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMonster(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable Long id
    ) {
        AppUser user = authService.requireUser(authorizationHeader);
        monsterService.delete(user, id);
    }
}
