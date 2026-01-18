package com.example.survivorio.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "monsters")
public class Monster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String type;

    @Column(name = "armor_class")
    private int armorClass;

    @Column(name = "hit_points")
    private int hitPoints;

    private String challenge;

    @Column(length = 2000)
    private String attacks;

    @Column(length = 4000)
    private String talents;

    @Column(length = 4000)
    private String spells;

    @Column(length = 6000)
    private String gear;

    @Column(length = 6000)
    private String notes;


    private String createdAt;

    // 🔹 JPA braucht leeren Konstruktor
    public Monster() {
    }

    // 🔹 Komfort-Konstruktor
    public Monster(
            String name,
            String type,
            int armorClass,
            int hitPoints,
            String challenge,
            String notes,
            String createdAt
    ) {
        this.name = name;
        this.type = type;
        this.armorClass = armorClass;
        this.hitPoints = hitPoints;
        this.challenge = challenge;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    // 🔹 Getter & Setter

    public Long getId() { return id; }

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

    public String getAttacks() { return attacks; }
    public void setAttacks(String attacks) { this.attacks = attacks; }

    public String getTalents() { return talents; }
    public void setTalents(String talents) { this.talents = talents; }

    public String getSpells() { return spells; }
    public void setSpells(String spells) { this.spells = spells; }

    public String getGear() { return gear; }
    public void setGear(String gear) { this.gear = gear; }
}
