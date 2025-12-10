package com.example.survivorio.controller;

import com.example.survivorio.entity.Player;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlayerController {

    @GetMapping("/player")
    public Player getPlayer() {
        return new Player(1L, "Hero", 5);
    }
}


