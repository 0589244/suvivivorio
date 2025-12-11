<script setup lang="ts">
import { ref, computed } from 'vue'

type SheetType = 'character' | 'monster'

// was wir im Frontend anzeigen
type SheetEntry = {
  id: number
  type: SheetType
  name: string
  createdAt: string // ISO-Datum als String
}

// wie die Daten ungefähr vom Backend kommen
type CharacterResponse = {
  id: number
  name: string
  createdAt: string
  // alle anderen Felder vom Character-Entity sind hier egal
}

type MonsterResponse = {
  id: number
  name: string
  createdAt: string
  // andere Monster-Felder sind hier egal
}

// 🔹 Lokales Backend – für Render später ändern!
const BACKEND_URL = 'http://localhost:8080'

// --- Auth-Status (vorerst nur im Frontend) ---
const isLoggedIn = ref(false)

// --- Login-Form ---
const loginUsername = ref('')
const loginPassword = ref('')
const loginError = ref<string | null>(null)

// --- Register-Form ---
const showRegister = ref(false)
const regUsername = ref('')
const regPassword = ref('')
const regPasswordRepeat = ref('')
const regError = ref<string | null>(null)
const regSuccess = ref<string | null>(null)

// --- Sheets (Charaktere & Monster) ---
const sheets = ref<SheetEntry[]>([])
const loadError = ref<string | null>(null)
const isLoadingSheets = ref(false)

// Filter & Sortierung
const filterType = ref<'all' | SheetType>('all')
const sortKey = ref<'createdAt' | 'name'>('createdAt')
const sortDir = ref<'asc' | 'desc'>('desc')

const filteredAndSortedSheets = computed(() => {
  let list = [...sheets.value]

  if (filterType.value !== 'all') {
    list = list.filter((s) => s.type === filterType.value)
  }

  list.sort((a, b) => {
    let vA = a[sortKey.value]
    let vB = b[sortKey.value]

    if (sortKey.value === 'createdAt') {
      const tA = new Date(vA).getTime()
      const tB = new Date(vB).getTime()
      return sortDir.value === 'asc' ? tA - tB : tB - tA
    } else {
      const cmp = vA.localeCompare(vB)
      return sortDir.value === 'asc' ? cmp : -cmp
    }
  })

  return list
})

// 🔹 Sheets vom Backend laden
async function loadSheets() {
  isLoadingSheets.value = true
  loadError.value = null

  try {
    // Characters holen
    const charRes = await fetch(`${BACKEND_URL}/api/characters`)
    if (!charRes.ok) throw new Error(`Characters HTTP ${charRes.status}`)
    const chars: CharacterResponse[] = await charRes.json()

    // Monster holen
    const monRes = await fetch(`${BACKEND_URL}/api/monsters`)
    if (!monRes.ok) throw new Error(`Monsters HTTP ${monRes.status}`)
    const mons: MonsterResponse[] = await monRes.json()

    // In gemeinsame Liste (für rechte Seite) mappen
    const charEntries: SheetEntry[] = chars.map((c) => ({
      id: c.id,
      name: c.name,
      type: 'character',
      createdAt: c.createdAt,
    }))

    const monEntries: SheetEntry[] = mons.map((m) => ({
      id: m.id,
      name: m.name,
      type: 'monster',
      createdAt: m.createdAt,
    }))

    sheets.value = [...charEntries, ...monEntries]
  } catch (err: any) {
    console.error(err)
    loadError.value = err.message ?? 'Fehler beim Laden der Sheets'
  } finally {
    isLoadingSheets.value = false
  }
}

// --- Auth-Logik (noch ohne echtes Backend) ---
function handleLogin() {
  loginError.value = null
  if (!loginUsername.value || !loginPassword.value) {
    loginError.value = 'Bitte Username und Passwort eingeben.'
    return
  }
  // Fake-Login
  isLoggedIn.value = true
  loginPassword.value = ''

  // 🔹 Nach Login sofort Sheets vom Backend laden
  loadSheets()
}

function handleRegister() {
  regError.value = null
  regSuccess.value = null

  if (!regUsername.value || !regPassword.value || !regPasswordRepeat.value) {
    regError.value = 'Bitte alle Felder ausfüllen.'
    return
  }
  if (regPassword.value !== regPasswordRepeat.value) {
    regError.value = 'Passwörter stimmen nicht überein.'
    return
  }

  // hier später: POST ans Backend
  regSuccess.value = 'Registrierung erfolgreich! Du kannst dich jetzt einloggen.'
  regPassword.value = ''
  regPasswordRepeat.value = ''
}

// Buttons links – aktuell nur Platzhalter
function createCharacter() {
  alert('Charakter erstellen (hier kommt später das Formular + POST-Route).')
}

function createMonster() {
  alert('Monster erstellen (hier kommt später das Formular + POST-Route).')
}
</script>

<template>
  <div class="app-root">
    <!-- Hintergrund (später auch für Darkmode nutzbar) -->
    <div class="bg-gradient"></div>

    <div class="app-shell" v-if="!isLoggedIn">
      <!-- LOGIN / REGISTER VIEW -->
      <div class="auth-card">
        <h1 class="logo-title">DnD Sheet Manager</h1>

        <div v-if="!showRegister" class="auth-panel">
          <h2 class="auth-heading">Sign in</h2>
          <div class="field">
            <label>Username</label>
            <input v-model="loginUsername" type="text" placeholder="Username" />
          </div>
          <div class="field">
            <label>Password</label>
            <input v-model="loginPassword" type="password" placeholder="Password" />
          </div>
          <p v-if="loginError" class="error-msg">{{ loginError }}</p>

          <div class="auth-actions">
            <button class="btn primary" @click="handleLogin">Sign in</button>
            <button class="btn ghost" @click="showRegister = true">Register</button>
          </div>
        </div>

        <div v-else class="auth-panel">
          <h2 class="auth-heading">Register</h2>
          <div class="field">
            <label>Username</label>
            <input v-model="regUsername" type="text" placeholder="Wunschname" />
          </div>
          <div class="field">
            <label>Password</label>
            <input v-model="regPassword" type="password" placeholder="Passwort" />
          </div>
          <div class="field">
            <label>Repeat Password</label>
            <input
              v-model="regPasswordRepeat"
              type="password"
              placeholder="Passwort wiederholen"
            />
          </div>

          <p v-if="regError" class="error-msg">{{ regError }}</p>
          <p v-if="regSuccess" class="success-msg">{{ regSuccess }}</p>

          <div class="auth-actions">
            <button class="btn primary" @click="handleRegister">Create account</button>
            <button class="btn ghost" @click="showRegister = false">Back to login</button>
          </div>
        </div>
      </div>
    </div>

    <!-- MAIN APP (nach Login) -->
    <div v-else class="main-layout">
      <header class="main-header">
        <h1>DnD Sheet Manager</h1>
        <div class="header-right">
          <!-- Darkmode-Toggle kommt hier später hin -->
          <span class="user-pill">Angemeldet als {{ loginUsername || regUsername || 'User' }}</span>
        </div>
      </header>

      <div class="main-grid">
        <!-- Linke Seite: große Kacheln -->
        <section class="left-panel">
          <div class="big-card" @click="createCharacter">
            <h2>Charakter erstellen +</h2>
            <p>Lege ein neues Character-Sheet an.</p>
          </div>

          <div class="big-card" @click="createMonster">
            <h2>Monster erstellen +</h2>
            <p>Erstelle ein Monster-Sheet für deine Kampagnen.</p>
          </div>
        </section>

        <!-- Rechte Seite: Liste der vorhandenen Sheets -->
        <section class="right-panel">
          <div class="list-header">
            <h2>Deine Sheets</h2>
            <div class="list-controls">
              <select v-model="filterType">
                <option value="all">Alle</option>
                <option value="character">Nur Charaktere</option>
                <option value="monster">Nur Monster</option>
              </select>

              <select v-model="sortKey">
                <option value="createdAt">Sortieren nach: Erstelldatum</option>
                <option value="name">Sortieren nach: Name</option>
              </select>

              <button class="btn tiny" @click="sortDir = sortDir === 'asc' ? 'desc' : 'asc'">
                {{ sortDir === 'asc' ? '↑' : '↓' }}
              </button>
            </div>
          </div>

          <div class="sheet-list">
            <p v-if="isLoadingSheets" class="empty-hint">
              Lade Sheets vom Backend...
            </p>
            <p v-else-if="loadError" class="error-msg">
              {{ loadError }}
            </p>

            <div
              v-for="entry in filteredAndSortedSheets"
              :key="entry.id"
              class="sheet-card"
            >
              <div class="sheet-type" :data-type="entry.type">
                {{ entry.type === 'character' ? 'Charakter' : 'Monster' }}
              </div>
              <div class="sheet-name">{{ entry.name }}</div>
              <div class="sheet-meta">
      <span>
        Erstellt:
        {{ new Date(entry.createdAt).toLocaleDateString() }}
      </span>
              </div>
            </div>

            <p
              v-if="!isLoadingSheets && !loadError && filteredAndSortedSheets.length === 0"
              class="empty-hint"
            >
              Noch keine Einträge. Erstelle links einen Charakter oder ein Monster.
            </p>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<style scoped>
.app-root {
  min-height: 100vh;
  position: relative;
  color: #f5f5f5;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

.bg-gradient {
  position: fixed;
  inset: 0;
  background: radial-gradient(circle at top left, #4b6cb7, #182848);
  filter: blur(0px);
  z-index: -1;
}

.app-shell {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.auth-card {
  background: rgba(10, 16, 32, 0.95);
  border-radius: 16px;
  padding: 24px 28px 28px;
  width: 360px;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.65);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.logo-title {
  margin: 0 0 18px;
  text-align: center;
  font-size: 20px;
  letter-spacing: 0.06em;
}

.auth-heading {
  margin: 0 0 12px;
  font-size: 18px;
}

.auth-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
}

.field label {
  opacity: 0.8;
}

.field input {
  padding: 8px 10px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(9, 14, 30, 0.9);
  color: #f5f5f5;
  outline: none;
}
.field input:focus {
  border-color: #4ea3ff;
}

.auth-actions {
  display: flex;
  gap: 10px;
  margin-top: 8px;
}

.btn {
  border-radius: 8px;
  border: none;
  padding: 8px 14px;
  font-size: 14px;
  cursor: pointer;
  font-weight: 600;
}
.btn.primary {
  background: #3ea5ff;
  color: #0b1220;
}
.btn.primary:hover {
  background: #6bb7ff;
}
.btn.ghost {
  background: transparent;
  color: #f5f5f5;
  border: 1px solid rgba(255, 255, 255, 0.25);
}
.btn.ghost:hover {
  background: rgba(255, 255, 255, 0.06);
}
.btn.tiny {
  padding: 4px 8px;
  font-size: 12px;
}

.error-msg {
  color: #ff6b81;
  font-size: 12px;
}
.success-msg {
  color: #8ee58f;
  font-size: 12px;
}

/* MAIN LAYOUT */

.main-layout {
  min-height: 100vh;
  padding: 16px 20px 24px;
  display: flex;
  flex-direction: column;
  backdrop-filter: blur(6px);
}

.main-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.main-header h1 {
  font-size: 22px;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-pill {
  font-size: 13px;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.main-grid {
  display: grid;
  grid-template-columns: minmax(0, 2.2fr) minmax(0, 3fr);
  gap: 18px;
  align-items: flex-start;
}

.left-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.big-card {
  background: rgba(12, 20, 40, 0.95);
  border-radius: 16px;
  padding: 18px 18px 16px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  cursor: pointer;
  transition: transform 0.12s ease, box-shadow 0.12s ease, border-color 0.12s ease;
}
.big-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 18px 60px rgba(0, 0, 0, 0.6);
  border-color: rgba(104, 174, 255, 0.8);
}
.big-card h2 {
  margin: 0 0 6px;
  font-size: 18px;
}
.big-card p {
  margin: 0;
  font-size: 13px;
  opacity: 0.8;
}

.right-panel {
  background: rgba(10, 16, 32, 0.96);
  border-radius: 16px;
  padding: 14px 14px 16px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.list-header h2 {
  margin: 0;
  font-size: 16px;
}

.list-controls {
  display: flex;
  gap: 8px;
  align-items: center;
}
.list-controls select {
  font-size: 12px;
  padding: 4px 6px;
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: rgba(5, 10, 24, 0.9);
  color: #f5f5f5;
}

.sheet-list {
  display: grid;
  gap: 8px;
}

.sheet-card {
  background: rgba(7, 12, 26, 0.95);
  border-radius: 10px;
  padding: 8px 10px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  font-size: 13px;
}

.sheet-type {
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  opacity: 0.8;
}
.sheet-type[data-type='character'] {
  color: #8bd5ff;
}
.sheet-type[data-type='monster'] {
  color: #ffb36b;
}
.sheet-name {
  font-weight: 600;
}
.sheet-meta {
  font-size: 11px;
  opacity: 0.75;
}

.empty-hint {
  font-size: 12px;
  opacity: 0.7;
}
</style>
