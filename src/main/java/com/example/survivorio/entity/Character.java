package com.example.survivorio.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "characters")
public class Character {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String ancestry;
    private String className;
    private int level;
    private int xp;

    private int str;
    private int dex;
    private int con;
    private int intel;
    private int wis;
    private int cha;

    private int hp;
    private int ac;

    private String title;
    private String alignment;
    private String background;
    private String deity;

    private String createdAt;

    public Character() {}

    // Getter/Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAncestry() { return ancestry; }
    public void setAncestry(String ancestry) { this.ancestry = ancestry; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }

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

    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }

    public int getAc() { return ac; }
    public void setAc(int ac) { this.ac = ac; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAlignment() { return alignment; }
    public void setAlignment(String alignment) { this.alignment = alignment; }

    public String getBackground() { return background; }
    public void setBackground(String background) { this.background = background; }

    public String getDeity() { return deity; }
    public void setDeity(String deity) { this.deity = deity; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
