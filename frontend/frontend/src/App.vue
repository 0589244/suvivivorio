<script setup lang="ts">
import { computed, ref } from 'vue'
import SheetList from './components/SheetList.vue'
import type { CharacterSheet, MonsterSheet, SheetRecord, SheetType } from './types/sheets'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

const isLoggedIn = ref(false)
const loginUsername = ref('')
const loginPassword = ref('')
const loginError = ref<string | null>(null)

const showRegister = ref(false)
const regUsername = ref('')
const regPassword = ref('')
const regPasswordRepeat = ref('')
const regError = ref<string | null>(null)
const regSuccess = ref<string | null>(null)

const sheets = ref<SheetRecord[]>([])
const loadError = ref<string | null>(null)
const saveError = ref<string | null>(null)
const saveSuccess = ref<string | null>(null)
const isLoadingSheets = ref(false)
const isSaving = ref(false)

const activeSheet = ref<SheetRecord | null>(null)
const filterType = ref<'all' | SheetType>('all')
const sortKey = ref<'createdAt' | 'name'>('createdAt')
const sortDir = ref<'asc' | 'desc'>('desc')

const filteredAndSortedSheets = computed(() => {
  let list = [...sheets.value]

  if (filterType.value !== 'all') {
    list = list.filter((sheet) => sheet.sheetType === filterType.value)
  }

  list.sort((a, b) => {
    const aValue = sortKey.value === 'name' ? a.name : a.createdAt || ''
    const bValue = sortKey.value === 'name' ? b.name : b.createdAt || ''

    if (sortKey.value === 'createdAt') {
      const aTime = new Date(aValue).getTime() || 0
      const bTime = new Date(bValue).getTime() || 0
      return sortDir.value === 'asc' ? aTime - bTime : bTime - aTime
    }

    const compare = aValue.localeCompare(bValue)
    return sortDir.value === 'asc' ? compare : -compare
  })

  return list
})

function emptyCharacter(): CharacterSheet {
  return {
    sheetType: 'character',
    name: '',
    ancestry: '',
    className: '',
    level: 1,
    xp: 0,
    str: 10,
    dex: 10,
    con: 10,
    intel: 10,
    wis: 10,
    cha: 10,
    hp: 10,
    ac: 10,
    title: '',
    alignment: '',
    background: '',
    deity: '',
    talentsSpells: '',
    attacks: '',
    gear: '',
    gp: 0,
    sp: 0,
    cp: 0,
  }
}

function emptyMonster(): MonsterSheet {
  return {
    sheetType: 'monster',
    name: '',
    type: '',
    armorClass: 10,
    hitPoints: 10,
    challenge: '',
    notes: '',
    str: 10,
    dex: 10,
    con: 10,
    intel: 10,
    wis: 10,
    cha: 10,
    attacks: '',
    gear: '',
    gp: 0,
    sp: 0,
    cp: 0,
  }
}

function normalizeCharacter(data: Partial<CharacterSheet>): CharacterSheet {
  return {
    ...emptyCharacter(),
    ...data,
    sheetType: 'character',
    str: numberOrDefault(data.str, 10),
    dex: numberOrDefault(data.dex, 10),
    con: numberOrDefault(data.con, 10),
    intel: numberOrDefault(data.intel, 10),
    wis: numberOrDefault(data.wis, 10),
    cha: numberOrDefault(data.cha, 10),
    hp: numberOrDefault(data.hp, 10),
    ac: numberOrDefault(data.ac, 10),
    level: numberOrDefault(data.level, 1),
    xp: numberOrDefault(data.xp, 0),
    gp: numberOrDefault(data.gp, 0),
    sp: numberOrDefault(data.sp, 0),
    cp: numberOrDefault(data.cp, 0),
  }
}

function normalizeMonster(data: Partial<MonsterSheet>): MonsterSheet {
  return {
    ...emptyMonster(),
    ...data,
    sheetType: 'monster',
    str: numberOrDefault(data.str, 10),
    dex: numberOrDefault(data.dex, 10),
    con: numberOrDefault(data.con, 10),
    intel: numberOrDefault(data.intel, 10),
    wis: numberOrDefault(data.wis, 10),
    cha: numberOrDefault(data.cha, 10),
    armorClass: numberOrDefault(data.armorClass, 10),
    hitPoints: numberOrDefault(data.hitPoints, 10),
    gp: numberOrDefault(data.gp, 0),
    sp: numberOrDefault(data.sp, 0),
    cp: numberOrDefault(data.cp, 0),
  }
}

function cloneSheet(sheet: SheetRecord): SheetRecord {
  return JSON.parse(JSON.stringify(sheet))
}

function numberOrDefault(value: unknown, fallback: number) {
  const numeric = Number(value)
  return Number.isFinite(numeric) ? numeric : fallback
}

async function loadSheets() {
  isLoadingSheets.value = true
  loadError.value = null

  try {
    const [characterResponse, monsterResponse] = await Promise.all([
      fetch(`${API_BASE_URL}/api/characters`),
      fetch(`${API_BASE_URL}/api/monsters`),
    ])

    if (!characterResponse.ok) {
      throw new Error(`Characters HTTP ${characterResponse.status}`)
    }
    if (!monsterResponse.ok) {
      throw new Error(`Monsters HTTP ${monsterResponse.status}`)
    }

    const characters = (await characterResponse.json()) as Partial<CharacterSheet>[]
    const monsters = (await monsterResponse.json()) as Partial<MonsterSheet>[]

    sheets.value = [
      ...characters.map((character) => normalizeCharacter(character)),
      ...monsters.map((monster) => normalizeMonster(monster)),
    ]
  } catch (error) {
    console.error(error)
    loadError.value =
      error instanceof Error ? error.message : 'Fehler beim Laden der Sheets'
  } finally {
    isLoadingSheets.value = false
  }
}

function handleLogin() {
  loginError.value = null

  if (!loginUsername.value || !loginPassword.value) {
    loginError.value = 'Bitte Username und Passwort eingeben.'
    return
  }

  isLoggedIn.value = true
  loginPassword.value = ''
  loadSheets()
}

function handleRegister() {
  regError.value = null
  regSuccess.value = null

  if (!regUsername.value || !regPassword.value || !regPasswordRepeat.value) {
    regError.value = 'Bitte alle Felder ausfuellen.'
    return
  }

  if (regPassword.value !== regPasswordRepeat.value) {
    regError.value = 'Passwoerter stimmen nicht ueberein.'
    return
  }

  loginUsername.value = regUsername.value
  regSuccess.value = 'Account lokal vorbereitet. Du kannst dich jetzt einloggen.'
  regPassword.value = ''
  regPasswordRepeat.value = ''
}

function openNewCharacter() {
  saveError.value = null
  saveSuccess.value = null
  activeSheet.value = emptyCharacter()
}

function openNewMonster() {
  saveError.value = null
  saveSuccess.value = null
  activeSheet.value = emptyMonster()
}

function openExistingSheet(sheet: SheetRecord) {
  saveError.value = null
  saveSuccess.value = null
  activeSheet.value = cloneSheet(sheet)
}

function closeSheetEditor() {
  activeSheet.value = null
}

async function saveActiveSheet() {
  if (!activeSheet.value) {
    return
  }

  if (!activeSheet.value.name.trim()) {
    saveError.value = 'Bitte mindestens einen Namen eintragen.'
    return
  }

  isSaving.value = true
  saveError.value = null
  saveSuccess.value = null

  const sheet = activeSheet.value
  const endpoint = sheet.sheetType === 'character' ? 'characters' : 'monsters'
  const method = sheet.id ? 'PUT' : 'POST'
  const url = `${API_BASE_URL}/api/${endpoint}${sheet.id ? `/${sheet.id}` : ''}`
  const payload = toBackendPayload(sheet)

  try {
    const response = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    })

    if (!response.ok) {
      throw new Error(`Speichern fehlgeschlagen: HTTP ${response.status}`)
    }

    const savedData = await response.json()
    const savedSheet =
      sheet.sheetType === 'character'
        ? normalizeCharacter(savedData)
        : normalizeMonster(savedData)

    upsertSheet(savedSheet)
    activeSheet.value = cloneSheet(savedSheet)
    saveSuccess.value = 'Gespeichert.'
  } catch (error) {
    console.error(error)
    saveError.value =
      error instanceof Error ? error.message : 'Fehler beim Speichern'
  } finally {
    isSaving.value = false
  }
}

function toBackendPayload(sheet: SheetRecord) {
  const { sheetType, ...payload } = sheet
  const numberFields = [
    'level',
    'xp',
    'str',
    'dex',
    'con',
    'intel',
    'wis',
    'cha',
    'hp',
    'ac',
    'armorClass',
    'hitPoints',
    'gp',
    'sp',
    'cp',
  ]

  for (const field of numberFields) {
    if (field in payload) {
      const record = payload as Record<string, unknown>
      record[field] = numberOrDefault(record[field], field === 'level' ? 1 : 0)
    }
  }

  return payload
}

function upsertSheet(sheet: SheetRecord) {
  const index = sheets.value.findIndex(
    (entry) => entry.sheetType === sheet.sheetType && entry.id === sheet.id,
  )

  if (index >= 0) {
    sheets.value[index] = sheet
    return
  }

  sheets.value = [sheet, ...sheets.value]
}
</script>

<template>
  <div class="app-root">
    <section v-if="!isLoggedIn" class="auth-screen">
      <div class="auth-backdrop"></div>
      <div class="auth-card">
        <p class="kicker">Campaign sheets</p>
        <h1>DnD Sheet Manager</h1>

        <div v-if="!showRegister" class="auth-panel">
          <label>
            Username
            <input v-model="loginUsername" type="text" autocomplete="username" />
          </label>

          <label>
            Password
            <input v-model="loginPassword" type="password" autocomplete="current-password" />
          </label>

          <p v-if="loginError" class="error-msg">{{ loginError }}</p>

          <div class="auth-actions">
            <button class="primary" type="button" @click="handleLogin">Einloggen</button>
            <button class="secondary" type="button" @click="showRegister = true">
              Registrieren
            </button>
          </div>
        </div>

        <div v-else class="auth-panel">
          <label>
            Username
            <input v-model="regUsername" type="text" autocomplete="username" />
          </label>

          <label>
            Password
            <input v-model="regPassword" type="password" autocomplete="new-password" />
          </label>

          <label>
            Repeat password
            <input
              v-model="regPasswordRepeat"
              type="password"
              autocomplete="new-password"
            />
          </label>

          <p v-if="regError" class="error-msg">{{ regError }}</p>
          <p v-if="regSuccess" class="success-msg">{{ regSuccess }}</p>

          <div class="auth-actions">
            <button class="primary" type="button" @click="handleRegister">Erstellen</button>
            <button class="secondary" type="button" @click="showRegister = false">
              Zurueck
            </button>
          </div>
        </div>
      </div>
    </section>

    <section v-else class="workspace">
      <header class="topbar">
        <div>
          <p class="kicker">Dungeon table</p>
          <h1>DnD Sheet Manager</h1>
        </div>
        <div class="topbar-actions">
          <span>{{ loginUsername || regUsername || 'User' }}</span>
          <button class="secondary" type="button" @click="loadSheets">Neu laden</button>
        </div>
      </header>

      <main class="dashboard">
        <aside class="actions-panel">
          <button class="create-tile" type="button" @click="openNewCharacter">
            <strong>Charakter erstellen</strong>
            <span>Spielerwerte, Ausruestung und Notizen speichern.</span>
          </button>

          <button class="create-tile monster" type="button" @click="openNewMonster">
            <strong>Monster erstellen</strong>
            <span>Stats, Angriffe und Loot fuer den Dungeonmaster.</span>
          </button>
        </aside>

        <section class="list-panel">
          <div class="list-toolbar">
            <h2>Deine Sheets</h2>
            <div class="list-controls">
              <select v-model="filterType" aria-label="Filter">
                <option value="all">Alle</option>
                <option value="character">Charaktere</option>
                <option value="monster">Monster</option>
              </select>

              <select v-model="sortKey" aria-label="Sortierung">
                <option value="createdAt">Datum</option>
                <option value="name">Name</option>
              </select>

              <button
                class="icon-button"
                type="button"
                :title="sortDir === 'asc' ? 'Aufsteigend' : 'Absteigend'"
                @click="sortDir = sortDir === 'asc' ? 'desc' : 'asc'"
              >
                {{ sortDir === 'asc' ? 'A-Z' : 'Z-A' }}
              </button>
            </div>
          </div>

          <SheetList
            :entries="filteredAndSortedSheets"
            :is-loading="isLoadingSheets"
            :error="loadError"
            @select="openExistingSheet"
          />
        </section>
      </main>
    </section>

    <div v-if="activeSheet" class="modal-overlay" @click.self="closeSheetEditor">
      <form class="sheet-window" @submit.prevent="saveActiveSheet">
        <header class="sheet-header">
          <h2>{{ activeSheet.sheetType === 'character' ? 'Charakter' : 'Monster' }}</h2>
          <div class="sheet-actions">
            <span v-if="saveSuccess" class="success-msg">{{ saveSuccess }}</span>
            <span v-if="saveError" class="error-msg">{{ saveError }}</span>
            <button class="primary dark" type="submit" :disabled="isSaving">
              {{ isSaving ? 'Speichert...' : 'Speichern' }}
            </button>
            <button class="secondary dark" type="button" @click="closeSheetEditor">
              Schliessen
            </button>
          </div>
        </header>

        <div class="sheet-grid">
          <section class="stats-column">
            <label class="stat-box">
              STR
              <input v-model.number="activeSheet.str" type="number" />
            </label>
            <label class="stat-box">
              INT
              <input v-model.number="activeSheet.intel" type="number" />
            </label>
            <label class="stat-box">
              DEX
              <input v-model.number="activeSheet.dex" type="number" />
            </label>
            <label class="stat-box">
              WIS
              <input v-model.number="activeSheet.wis" type="number" />
            </label>
            <label class="stat-box">
              CON
              <input v-model.number="activeSheet.con" type="number" />
            </label>
            <label class="stat-box">
              CHA
              <input v-model.number="activeSheet.cha" type="number" />
            </label>

            <label v-if="activeSheet.sheetType === 'character'" class="big-stat">
              HP
              <input v-model.number="activeSheet.hp" type="number" />
            </label>
            <label v-if="activeSheet.sheetType === 'character'" class="big-stat">
              AC
              <input v-model.number="activeSheet.ac" type="number" />
            </label>

            <label v-if="activeSheet.sheetType === 'monster'" class="big-stat">
              HP
              <input v-model.number="activeSheet.hitPoints" type="number" />
            </label>
            <label v-if="activeSheet.sheetType === 'monster'" class="big-stat">
              AC
              <input v-model.number="activeSheet.armorClass" type="number" />
            </label>

            <label class="wide-field attacks">
              Attacks
              <textarea v-model="activeSheet.attacks"></textarea>
            </label>
          </section>

          <section class="identity-column">
            <label class="wide-field">
              Name
              <input v-model="activeSheet.name" type="text" />
            </label>

            <template v-if="activeSheet.sheetType === 'character'">
              <label class="wide-field">
                Ancestry
                <input v-model="activeSheet.ancestry" type="text" />
              </label>
              <label class="wide-field">
                Class
                <input v-model="activeSheet.className" type="text" />
              </label>
              <div class="two-fields">
                <label class="wide-field">
                  Level
                  <input v-model.number="activeSheet.level" type="number" min="1" />
                </label>
                <label class="wide-field">
                  XP
                  <input v-model.number="activeSheet.xp" type="number" min="0" />
                </label>
              </div>
              <label class="wide-field">
                Title
                <input v-model="activeSheet.title" type="text" />
              </label>
              <label class="wide-field">
                Alignment
                <input v-model="activeSheet.alignment" type="text" />
              </label>
              <label class="wide-field">
                Background
                <input v-model="activeSheet.background" type="text" />
              </label>
              <label class="wide-field">
                Deity
                <input v-model="activeSheet.deity" type="text" />
              </label>
            </template>

            <template v-else>
              <label class="wide-field">
                Type
                <input v-model="activeSheet.type" type="text" />
              </label>
              <label class="wide-field">
                Challenge
                <input v-model="activeSheet.challenge" type="text" />
              </label>
              <label class="wide-field monster-notes">
                Notes
                <textarea v-model="activeSheet.notes"></textarea>
              </label>
            </template>
          </section>

          <section class="notes-column">
            <label v-if="activeSheet.sheetType === 'character'" class="wide-field tall">
              Talents / Spells
              <textarea v-model="activeSheet.talentsSpells"></textarea>
            </label>
            <label v-else class="wide-field tall">
              Special traits
              <textarea v-model="activeSheet.notes"></textarea>
            </label>

            <div class="coin-row">
              <label>
                GP
                <input v-model.number="activeSheet.gp" type="number" min="0" />
              </label>
              <label>
                SP
                <input v-model.number="activeSheet.sp" type="number" min="0" />
              </label>
              <label>
                CP
                <input v-model.number="activeSheet.cp" type="number" min="0" />
              </label>
            </div>

            <label class="wide-field gear-field">
              Gear
              <textarea v-model="activeSheet.gear"></textarea>
            </label>
          </section>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.app-root {
  min-height: 100vh;
  color: #f7f2e7;
  background: #151212;
  font-family:
    Inter,
    ui-sans-serif,
    system-ui,
    -apple-system,
    BlinkMacSystemFont,
    'Segoe UI',
    sans-serif;
}

.auth-screen {
  position: relative;
  min-height: 100vh;
  display: grid;
  place-items: center;
  overflow: hidden;
  padding: 24px;
}

.auth-screen::before {
  position: absolute;
  inset: 0;
  content: '';
  background:
    linear-gradient(90deg, rgba(12, 8, 6, 0.88), rgba(20, 14, 12, 0.48)),
    url('/dnd-background.avif') center / cover;
  transform: scale(1.02);
}

.auth-backdrop {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 50% 20%, rgba(255, 220, 150, 0.24), transparent 38%);
}

.auth-card {
  position: relative;
  width: min(420px, 100%);
  padding: 28px;
  border: 1px solid rgba(244, 223, 178, 0.28);
  border-radius: 8px;
  background: rgba(18, 13, 11, 0.86);
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(6px);
}

.kicker {
  margin: 0 0 6px;
  color: #e6bf75;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

h1,
h2 {
  margin: 0;
  letter-spacing: 0;
}

.auth-card h1 {
  margin-bottom: 22px;
  font-family: Georgia, 'Times New Roman', serif;
  font-size: 34px;
}

.auth-panel,
.auth-panel label {
  display: grid;
  gap: 10px;
}

.auth-panel {
  gap: 14px;
}

label {
  color: inherit;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

input,
textarea,
select {
  width: 100%;
  border: 1px solid rgba(244, 223, 178, 0.35);
  border-radius: 4px;
  background: rgba(255, 249, 238, 0.95);
  color: #14110f;
  font: inherit;
  letter-spacing: 0;
  outline: none;
}

input,
select {
  height: 38px;
  padding: 8px 10px;
}

textarea {
  min-height: 88px;
  padding: 9px 10px;
  resize: vertical;
}

input:focus,
textarea:focus,
select:focus {
  border-color: #d69a38;
  box-shadow: 0 0 0 3px rgba(214, 154, 56, 0.22);
}

.auth-actions,
.topbar-actions,
.sheet-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

button {
  border: 0;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 800;
  letter-spacing: 0;
}

button:disabled {
  cursor: wait;
  opacity: 0.68;
}

.primary,
.secondary {
  min-height: 38px;
  padding: 9px 14px;
}

.primary {
  background: #d69a38;
  color: #130f0b;
}

.primary:hover {
  background: #efb44e;
}

.secondary {
  border: 1px solid rgba(244, 223, 178, 0.28);
  background: rgba(255, 255, 255, 0.06);
  color: #f7f2e7;
}

.secondary:hover {
  background: rgba(255, 255, 255, 0.12);
}

.error-msg,
.success-msg {
  margin: 0;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0;
}

.error-msg {
  color: #ff8d8d;
}

.success-msg {
  color: #8ee596;
}

.workspace {
  min-height: 100vh;
  padding: 24px;
  background:
    radial-gradient(circle at 20% 0%, rgba(214, 154, 56, 0.2), transparent 28%),
    linear-gradient(135deg, #16120f, #27221d 52%, #141211);
}

.topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
  margin: 0 auto 22px;
  width: min(1180px, 100%);
}

.topbar h1 {
  font-family: Georgia, 'Times New Roman', serif;
  font-size: clamp(28px, 4vw, 46px);
}

.topbar-actions span {
  padding: 8px 12px;
  border: 1px solid rgba(244, 223, 178, 0.22);
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.06);
}

.dashboard {
  display: grid;
  grid-template-columns: minmax(260px, 0.75fr) minmax(0, 1.55fr);
  gap: 18px;
  width: min(1180px, 100%);
  margin: 0 auto;
}

.actions-panel,
.list-panel {
  border: 1px solid rgba(244, 223, 178, 0.18);
  border-radius: 8px;
  background: rgba(20, 15, 12, 0.78);
  box-shadow: 0 18px 50px rgba(0, 0, 0, 0.28);
}

.actions-panel {
  display: grid;
  gap: 14px;
  align-content: start;
  padding: 16px;
}

.create-tile {
  display: grid;
  gap: 7px;
  width: 100%;
  padding: 18px;
  border: 1px solid rgba(244, 223, 178, 0.22);
  border-radius: 8px;
  background: #f4dfb2;
  color: #17110d;
  text-align: left;
}

.create-tile.monster {
  background: #dabf85;
}

.create-tile:hover {
  transform: translateY(-1px);
  box-shadow: 0 16px 34px rgba(0, 0, 0, 0.24);
}

.create-tile strong {
  font-family: Georgia, 'Times New Roman', serif;
  font-size: 21px;
}

.create-tile span {
  font-size: 13px;
  font-weight: 600;
}

.list-panel {
  padding: 16px;
}

.list-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.list-toolbar h2 {
  font-size: 22px;
}

.list-controls {
  display: flex;
  gap: 8px;
  align-items: center;
}

.list-controls select {
  width: auto;
  min-width: 116px;
  background: #fff8e8;
}

.icon-button {
  min-width: 48px;
  height: 38px;
  padding: 0 10px;
  background: #fff8e8;
  color: #19120d;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 20;
  display: grid;
  place-items: center;
  padding: 24px;
  overflow: auto;
  background: rgba(0, 0, 0, 0.76);
}

.sheet-window {
  width: min(1120px, 100%);
  max-height: calc(100vh - 48px);
  overflow: auto;
  border: 3px solid #111;
  border-radius: 4px;
  background:
    linear-gradient(rgba(255, 255, 255, 0.92), rgba(255, 255, 255, 0.92)),
    repeating-linear-gradient(0deg, #f7f0df, #f7f0df 22px, #efe2c5 23px);
  color: #111;
  box-shadow: 0 30px 120px rgba(0, 0, 0, 0.62);
}

.sheet-header {
  position: sticky;
  top: 0;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px 12px;
  border-bottom: 3px solid #111;
  background: #fbf6e9;
}

.sheet-header h2 {
  font-family: Georgia, 'Times New Roman', serif;
  font-size: clamp(34px, 7vw, 64px);
  font-weight: 900;
  line-height: 0.95;
}

.sheet-actions {
  justify-content: flex-end;
}

.primary.dark {
  background: #111;
  color: #fffaf0;
}

.primary.dark:hover {
  background: #2b2119;
}

.secondary.dark {
  border-color: #111;
  background: #fffaf0;
  color: #111;
}

.sheet-grid {
  display: grid;
  grid-template-columns: 1fr 1.05fr 1.55fr;
  gap: 14px;
  padding: 18px 20px 22px;
}

.stats-column {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  align-content: start;
}

.identity-column,
.notes-column {
  display: grid;
  gap: 10px;
  align-content: start;
}

.stat-box,
.big-stat,
.wide-field,
.coin-row label {
  position: relative;
  display: grid;
  gap: 5px;
  border: 2px solid #111;
  border-radius: 2px;
  background: rgba(255, 255, 255, 0.68);
  color: #111;
  padding: 7px;
  font-size: 12px;
}

.stat-box input,
.big-stat input,
.wide-field input,
.wide-field textarea,
.coin-row input {
  border: 0;
  border-bottom: 2px solid #111;
  border-radius: 0;
  background: transparent;
  box-shadow: none;
  padding: 2px 0 4px;
  min-height: 30px;
}

.wide-field textarea {
  min-height: 72px;
}

.big-stat {
  min-height: 82px;
}

.attacks {
  grid-column: 1 / -1;
}

.two-fields,
.coin-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.coin-row {
  grid-template-columns: repeat(3, 1fr);
}

.tall textarea {
  min-height: 190px;
}

.gear-field textarea {
  min-height: 150px;
}

.monster-notes textarea {
  min-height: 120px;
}

@media (max-width: 900px) {
  .dashboard,
  .sheet-grid {
    grid-template-columns: 1fr;
  }

  .topbar,
  .list-toolbar,
  .sheet-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .sheet-actions,
  .list-controls {
    width: 100%;
  }

  .list-controls {
    flex-wrap: wrap;
  }
}

@media (max-width: 560px) {
  .workspace,
  .auth-screen,
  .modal-overlay {
    padding: 14px;
  }

  .stats-column,
  .two-fields,
  .coin-row {
    grid-template-columns: 1fr;
  }
}
</style>
