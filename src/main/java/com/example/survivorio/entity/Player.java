package com.example.survivorio.entity;

public class Player {
    private Long id;
    private String name;
    private int level;

    public Player(Long id, String name, int level) {
        this.id = id;
        this.name = name;
        this.level = level;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public int getLevel() { return level; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLevel(int level) { this.level = level; }
}
