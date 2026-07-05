package com.example.survivorio.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
    private String talentsSpells;
    private String attacks;
    private String gear;
    private int gp;
    private int sp;
    private int cp;

    private String createdAt;

    public Character() {
    }

    public Character(
            Long id,
            String name,
            String ancestry,
            String className,
            int level,
            int xp,
            int str,
            int dex,
            int con,
            int intel,
            int wis,
            int cha,
            int hp,
            int ac,
            String title,
            String alignment,
            String background,
            String deity,
            String talentsSpells,
            String attacks,
            String gear,
            int gp,
            int sp,
            int cp,
            String createdAt
    ) {
        this.id = id;
        this.name = name;
        this.ancestry = ancestry;
        this.className = className;
        this.level = level;
        this.xp = xp;
        this.str = str;
        this.dex = dex;
        this.con = con;
        this.intel = intel;
        this.wis = wis;
        this.cha = cha;
        this.hp = hp;
        this.ac = ac;
        this.title = title;
        this.alignment = alignment;
        this.background = background;
        this.deity = deity;
        this.talentsSpells = talentsSpells;
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

    public String getTalentsSpells() { return talentsSpells; }
    public void setTalentsSpells(String talentsSpells) { this.talentsSpells = talentsSpells; }

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
