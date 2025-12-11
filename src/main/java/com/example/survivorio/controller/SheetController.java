package com.example.survivorio.controller;

import com.example.survivorio.entity.Character;
import com.example.survivorio.entity.Monster;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*") // später für Render-Frontend anpassen
@RestController
@RequestMapping("/api")
public class SheetController {

    @GetMapping("/characters")
    public List<Character> getCharacters() {
        Character c1 = new Character(
                1L,
                "Aria the Swift",
                "Human",
                "Rogue",
                3,
                900,
                14, // STR
                18, // DEX
                12, // CON
                13, // INT
                10, // WIS
                16, // CHA
                18, // HP
                15, // AC
                "Shadowblade",
                "Chaotic Good",
                "Street Urchin",
                "The Trickster",
                "2025-12-01T10:00:00Z"
        );

        Character c2 = new Character(
                2L,
                "Thorgar Ironfist",
                "Dwarf",
                "Fighter",
                5,
                4500,
                18,
                10,
                16,
                9,
                12,
                8,
                42,
                18,
                "Defender of the Halls",
                "Lawful Neutral",
                "Clan Warrior",
                "Moradin",
                "2025-12-05T14:20:00Z"
        );

        return List.of(c1, c2);
    }

    @GetMapping("/monsters")
    public List<Monster> getMonsters() {
        Monster m1 = new Monster(
                1L,
                "Goblin Boss",
                "Humanoid (goblin)",
                15,
                30,
                "1",
                "Multiattack, Nimble Escape",
                "2025-12-03T18:30:00Z"
        );

        Monster m2 = new Monster(
                2L,
                "Ancient Shadow Drake",
                "Dragon",
                19,
                210,
                "16",
                "Legendary Actions, Shadow Breath",
                "2025-12-06T20:15:00Z"
        );

        return List.of(m1, m2);
    }
}
