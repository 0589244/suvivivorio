package com.example.survivorio.entity;

public class Monster {
    private Long id;
    private String name;
    private String type;          // z. B. Beast, Undead, Dragon
    private int armorClass;
    private int hitPoints;
    private String challenge;     // z. B. "1/2", "3", etc.
    private String notes;         // Beschreibung / Angriffe
    private String createdAt;     // ISO-String

    public Monster() {
    }

    public Monster(
            Long id,
            String name,
            String type,
            int armorClass,
            int hitPoints,
            String challenge,
            String notes,
            String createdAt
    ) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.armorClass = armorClass;
        this.hitPoints = hitPoints;
        this.challenge = challenge;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getArmorClass() { return armorClass; }
    public void setArmorClass(int armorClass) { this.armorClass = armorClass; }

    public int getHitPoints() { return hitPoints; }
    public void setHitPoints(int hitPoints) { this.hitPoints = hitPoints; }

    public String getChallenge() { return challenge; }
    public void setChallenge(String challenge) { this.challenge = challenge; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
