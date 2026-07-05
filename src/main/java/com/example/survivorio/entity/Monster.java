package com.example.survivorio.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "monsters")
public class Monster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String type;
    private int armorClass;
    private int hitPoints;
    private String challenge;
    private String notes;

    private int str;
    private int dex;
    private int con;
    private int intel;
    private int wis;
    private int cha;

    private String attacks;
    private String gear;
    private int gp;
    private int sp;
    private int cp;

    private String createdAt;

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
            int str,
            int dex,
            int con,
            int intel,
            int wis,
            int cha,
            String attacks,
            String gear,
            int gp,
            int sp,
            int cp,
            String createdAt
    ) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.armorClass = armorClass;
        this.hitPoints = hitPoints;
        this.challenge = challenge;
        this.notes = notes;
        this.str = str;
        this.dex = dex;
        this.con = con;
        this.intel = intel;
        this.wis = wis;
        this.cha = cha;
        this.attacks = attacks;
        this.gear = gear;
        this.gp = gp;
        this.sp = sp;
        this.cp = cp;
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

    public int getStr() { return str; }
    public void setStr(int str) { this.str = str; }

    public int getDex() { return dex; }
    public void setDex(int dex) { this.dex = dex; }

    public int getCon() { return con; }
    public void setCon(int con) { this.con = con; }

    public int getIntel() { return intel; }
    public void setIntel(int intel) { this.intel = intel; }

    public int getWis() { return wis; }
    public void setWis(int wis) { this.wis = wis; }

    public int getCha() { return cha; }
    public void setCha(int cha) { this.cha = cha; }

    public String getAttacks() { return attacks; }
    public void setAttacks(String attacks) { this.attacks = attacks; }

    public String getGear() { return gear; }
    public void setGear(String gear) { this.gear = gear; }

    public int getGp() { return gp; }
    public void setGp(int gp) { this.gp = gp; }

    public int getSp() { return sp; }
    public void setSp(int sp) { this.sp = sp; }

    public int getCp() { return cp; }
    public void setCp(int cp) { this.cp = cp; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
