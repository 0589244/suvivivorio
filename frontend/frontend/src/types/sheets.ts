export type SheetType = 'character' | 'monster'

export type CharacterSheet = {
  id?: number
  sheetType: 'character'
  name: string
  gender: 'male' | 'female'
  ancestry: string
  className: string
  level: number
  xp: number
  str: number
  dex: number
  con: number
  intel: number
  wis: number
  cha: number
  hp: number
  ac: number
  title: string
  alignment: string
  background: string
  deity: string
  talentsSpells: string
  journal: string
  attacks: string
  gear: string
  profileImage: string
  gp: number
  sp: number
  cp: number
  createdAt?: string
}

export type MonsterSheet = {
  id?: number
  sheetType: 'monster'
  name: string
  type: string
  armorClass: number
  hitPoints: number
  challenge: string
  notes: string
  str: number
  dex: number
  con: number
  intel: number
  wis: number
  cha: number
  attacks: string
  gear: string
  profileImage: string
  gp: number
  sp: number
  cp: number
  createdAt?: string
}

export type SheetRecord = CharacterSheet | MonsterSheet
