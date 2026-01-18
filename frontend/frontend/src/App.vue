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

type CharacterCreate = {
  name: string
  createdAt?: string
}

type MonsterCreate = {
  name: string
  createdAt?: string
}

const showCreate = ref<null | SheetType>(null)

const newCharacter = ref<CharacterCreate>({ name: '' })
const newMonster = ref<MonsterCreate>({ name: '' })

const createError = ref<string | null>(null)
const isCreating = ref(false)

// 🔹 Lokales Backend – für Render später ändern!
const BACKEND_URL = 'http://localhost:8080'

async function postCharacter(payload: CharacterCreate) {
  const res = await fetch(`${BACKEND_URL}/api/characters`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (!res.ok) throw new Error(`POST /characters HTTP ${res.status}`)
  return res.json()
}

async function postMonster(payload: MonsterCreate) {
  const res = await fetch(`${BACKEND_URL}/api/monsters`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (!res.ok) throw new Error(`POST /monsters HTTP ${res.status}`)
  return res.json()
}

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

// --- Editor Panel (ausgewähltes Sheet) ---
const selected = ref<{ type: SheetType; id: number } | null>(null)
const selectedCharacter = ref<any | null>(null)
const selectedMonster = ref<any | null>(null)

const saveStatus = ref<'idle' | 'saving' | 'saved' | 'error'>('idle')
let saveTimer: number | null = null

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

async function openSheet(type: SheetType, id: number) {
  selected.value = { type, id }
  saveStatus.value = 'idle'

  try {
    if (type === 'character') {
      const res = await fetch(`${BACKEND_URL}/api/characters/${id}`)
      if (!res.ok) throw new Error(`GET character ${id} HTTP ${res.status}`)
      selectedCharacter.value = await res.json()
      selectedMonster.value = null
    } else {
      const res = await fetch(`${BACKEND_URL}/api/monsters/${id}`)
      if (!res.ok) throw new Error(`GET monster ${id} HTTP ${res.status}`)
      selectedMonster.value = await res.json()
      selectedCharacter.value = null
    }
  } catch (e) {
    console.error(e)
  }
}

function scheduleSave(patch: Record<string, any>) {
  const sel = selected.value
  if (!sel) return

  saveStatus.value = 'saving'

  if (saveTimer) window.clearTimeout(saveTimer)
  saveTimer = window.setTimeout(async () => {
    try {
      const url =
        sel.type === 'character'
          ? `${BACKEND_URL}/api/characters/${sel.id}`
          : `${BACKEND_URL}/api/monsters/${sel.id}`

      const res = await fetch(url, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(patch),
      })
      if (!res.ok) throw new Error(`PATCH HTTP ${res.status}`)

      const updated = await res.json()
      if (sel.type === 'character') selectedCharacter.value = updated
      else selectedMonster.value = updated

      saveStatus.value = 'saved'
      await loadSheets()
    } catch (e) {
      console.error(e)
      saveStatus.value = 'error'
    }
  }, 600)
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

function createCharacter() {
  showCreate.value = 'character'
  newCharacter.value = { name: '' }
  createError.value = null
}

function createMonster() {
  showCreate.value = 'monster'
  newMonster.value = { name: '' }
  createError.value = null
}

async function submitCreate() {
  createError.value = null

  try {
    isCreating.value = true

    if (showCreate.value === 'character') {
      const name = newCharacter.value.name.trim()
      if (!name) {
        createError.value = 'Name darf nicht leer sein.'
        return
      }

      const created = await postCharacter({ name })
      showCreate.value = null
      await loadSheets()
      await openSheet('character', created.id)
      return
    }

    if (showCreate.value === 'monster') {
      const name = newMonster.value.name.trim()
      if (!name) {
        createError.value = 'Name darf nicht leer sein.'
        return
      }

      const created = await postMonster({ name })
      showCreate.value = null
      await loadSheets()
      await openSheet('monster', created.id)
      return
    }
  } catch (err: any) {
    console.error(err)
    createError.value = err.message ?? 'Fehler beim Erstellen'
  } finally {
    isCreating.value = false
  }
}

function closeCreate() {
  showCreate.value = null
  createError.value = null
  // optional: Felder resetten
  newCharacter.value = { name: '' }
  newMonster.value = { name: '' }
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
            <!-- EDITOR PANEL -->
            <div v-if="selected" class="editor-panel">
              <div class="editor-header">
                <div class="editor-title">
                  {{ selected.type === 'character' ? 'Held' : 'Monster' }}
                </div>

                <div class="editor-status">
                  <span v-if="saveStatus === 'idle'"> </span>
                  <span v-else-if="saveStatus === 'saving'">Speichere…</span>
                  <span v-else-if="saveStatus === 'saved'">Gespeichert ✓</span>
                  <span v-else>Fehler beim Speichern</span>
                </div>
              </div>

              <!-- CHARACTER PANEL (Shadowdark Layout v1) -->
              <div v-if="selected.type === 'character' && selectedCharacter" class="sd-sheet">
                <!-- Header -->
                <div class="sd-head">
                  <div class="sd-brand">HELD</div>

                  <div class="sd-head-main">
                    <label class="sd-label">Name</label>
                    <input
                      class="sd-input sd-title"
                      :value="selectedCharacter.name"
                      @input="(e:any) => { selectedCharacter.name = e.target.value; scheduleSave({ name: selectedCharacter.name }) }"
                      type="text"
                      placeholder="Name"
                    />
                  </div>

                  <div class="sd-head-meta">
                    <div class="sd-mini">
                      <label class="sd-label">Level</label>
                      <input
                        class="sd-input"
                        :value="selectedCharacter.level"
                        @input="(e:any) => { selectedCharacter.level = Number(e.target.value); scheduleSave({ level: selectedCharacter.level }) }"
                        type="number"
                        min="0"
                      />
                    </div>
                    <div class="sd-mini">
                      <label class="sd-label">XP</label>
                      <input
                        class="sd-input"
                        :value="selectedCharacter.xp"
                        @input="(e:any) => { selectedCharacter.xp = Number(e.target.value); scheduleSave({ xp: selectedCharacter.xp }) }"
                        type="number"
                        min="0"
                      />
                    </div>
                  </div>
                </div>

                <!-- Body: 3 columns -->
                <div class="sd-body">
                  <!-- Left: Stats -->
                  <div class="sd-col sd-stats">
                    <div class="sd-stat">
                      <div class="sd-stat-name">STR</div>
                      <input class="sd-stat-val"
                             :value="selectedCharacter.str"
                             @input="(e:any) => { selectedCharacter.str = Number(e.target.value); scheduleSave({ str: selectedCharacter.str }) }"
                             type="number"
                      />
                    </div>

                    <div class="sd-stat">
                      <div class="sd-stat-name">DEX</div>
                      <input class="sd-stat-val"
                             :value="selectedCharacter.dex"
                             @input="(e:any) => { selectedCharacter.dex = Number(e.target.value); scheduleSave({ dex: selectedCharacter.dex }) }"
                             type="number"
                      />
                    </div>

                    <div class="sd-stat">
                      <div class="sd-stat-name">CON</div>
                      <input class="sd-stat-val"
                             :value="selectedCharacter.con"
                             @input="(e:any) => { selectedCharacter.con = Number(e.target.value); scheduleSave({ con: selectedCharacter.con }) }"
                             type="number"
                      />
                    </div>

                    <div class="sd-stat">
                      <div class="sd-stat-name">INT</div>
                      <input class="sd-stat-val"
                             :value="selectedCharacter.intel"
                             @input="(e:any) => { selectedCharacter.intel = Number(e.target.value); scheduleSave({ intel: selectedCharacter.intel }) }"
                             type="number"
                      />
                    </div>

                    <div class="sd-stat">
                      <div class="sd-stat-name">WIS</div>
                      <input class="sd-stat-val"
                             :value="selectedCharacter.wis"
                             @input="(e:any) => { selectedCharacter.wis = Number(e.target.value); scheduleSave({ wis: selectedCharacter.wis }) }"
                             type="number"
                      />
                    </div>

                    <div class="sd-stat">
                      <div class="sd-stat-name">CHA</div>
                      <input class="sd-stat-val"
                             :value="selectedCharacter.cha"
                             @input="(e:any) => { selectedCharacter.cha = Number(e.target.value); scheduleSave({ cha: selectedCharacter.cha }) }"
                             type="number"
                      />
                    </div>
                  </div>

                  <!-- Middle: Core -->
                  <div class="sd-col sd-core">
                    <div class="sd-row">
                      <div class="sd-field">
                        <label class="sd-label">Ancestry</label>
                        <input
                          class="sd-input"
                          :value="selectedCharacter.ancestry"
                          @input="(e:any) => { selectedCharacter.ancestry = e.target.value; scheduleSave({ ancestry: selectedCharacter.ancestry }) }"
                          type="text"
                          placeholder="z.B. Human, Elf..."
                        />
                      </div>

                      <div class="sd-field">
                        <label class="sd-label">Class</label>
                        <input
                          class="sd-input"
                          :value="selectedCharacter.className"
                          @input="(e:any) => { selectedCharacter.className = e.target.value; scheduleSave({ className: selectedCharacter.className }) }"
                          type="text"
                          placeholder="z.B. Fighter..."
                        />
                      </div>
                    </div>

                    <div class="sd-row sd-badges">
                      <div class="sd-badge">
                        <div class="sd-badge-name">HP</div>
                        <input
                          class="sd-badge-val"
                          :value="selectedCharacter.hp"
                          @input="(e:any) => { selectedCharacter.hp = Number(e.target.value); scheduleSave({ hp: selectedCharacter.hp }) }"
                          type="number"
                          min="0"
                        />
                      </div>

                      <div class="sd-badge">
                        <div class="sd-badge-name">AC</div>
                        <input
                          class="sd-badge-val"
                          :value="selectedCharacter.ac"
                          @input="(e:any) => { selectedCharacter.ac = Number(e.target.value); scheduleSave({ ac: selectedCharacter.ac }) }"
                          type="number"
                          min="0"
                        />
                      </div>
                    </div>
                  </div>

                  <!-- Right: Flavour -->
                  <div class="sd-col sd-flavour">
                    <div class="sd-field">
                      <label class="sd-label">Title</label>
                      <input
                        class="sd-input"
                        :value="selectedCharacter.title"
                        @input="(e:any) => { selectedCharacter.title = e.target.value; scheduleSave({ title: selectedCharacter.title }) }"
                        type="text"
                        placeholder="z.B. The Brave"
                      />
                    </div>

                    <div class="sd-field">
                      <label class="sd-label">Alignment</label>
                      <input
                        class="sd-input"
                        :value="selectedCharacter.alignment"
                        @input="(e:any) => { selectedCharacter.alignment = e.target.value; scheduleSave({ alignment: selectedCharacter.alignment }) }"
                        type="text"
                        placeholder="z.B. Lawful"
                      />
                    </div>

                    <div class="sd-field">
                      <label class="sd-label">Background</label>
                      <input
                        class="sd-input"
                        :value="selectedCharacter.background"
                        @input="(e:any) => { selectedCharacter.background = e.target.value; scheduleSave({ background: selectedCharacter.background }) }"
                        type="text"
                        placeholder="z.B. Outcast"
                      />
                    </div>

                    <div class="sd-field">
                      <label class="sd-label">Deity</label>
                      <input
                        class="sd-input"
                        :value="selectedCharacter.deity"
                        @input="(e:any) => { selectedCharacter.deity = e.target.value; scheduleSave({ deity: selectedCharacter.deity }) }"
                        type="text"
                        placeholder="z.B. None"
                      />
                    </div>
                    <div class="sd-field">
                      <label class="sd-label">Attacks</label>
                      <textarea
                        class="sd-textarea"
                        :value="selectedCharacter.attacks ?? ''"
                        @input="(e:any) => { selectedCharacter.attacks = e.target.value; scheduleSave({ attacks: selectedCharacter.attacks }) }"
                        rows="3"
                        placeholder="Weapon, to hit, damage…"
                      />
                    </div>

                    <div class="sd-field">
                      <label class="sd-label">Talents</label>
                      <textarea
                        class="sd-textarea"
                        :value="selectedCharacter.talents ?? ''"
                        @input="(e:any) => { selectedCharacter.talents = e.target.value; scheduleSave({ talents: selectedCharacter.talents }) }"
                        rows="4"
                        placeholder="Talents / Features…"
                      />
                    </div>

                    <div class="sd-field">
                      <label class="sd-label">Spells</label>
                      <textarea
                        class="sd-textarea"
                        :value="selectedCharacter.spells ?? ''"
                        @input="(e:any) => { selectedCharacter.spells = e.target.value; scheduleSave({ spells: selectedCharacter.spells }) }"
                        rows="4"
                        placeholder="Spells, slots, notes…"
                      />
                    </div>

                    <div class="sd-field">
                      <label class="sd-label">Gear</label>
                      <textarea
                        class="sd-textarea"
                        :value="selectedCharacter.gear ?? ''"
                        @input="(e:any) => { selectedCharacter.gear = e.target.value; scheduleSave({ gear: selectedCharacter.gear }) }"
                        rows="5"
                        placeholder="Inventory / Items…"
                      />
                    </div>

                    <div class="sd-field">
                      <label class="sd-label">Notes</label>
                      <textarea
                        class="sd-textarea"
                        :value="selectedCharacter.notes ?? ''"
                        @input="(e:any) => { selectedCharacter.notes = e.target.value; scheduleSave({ notes: selectedCharacter.notes }) }"
                        rows="5"
                        placeholder="Session notes…"
                      />
                    </div>
                  </div>
                </div>
              </div>


              <!-- MONSTER PANEL (minimal) -->
              <div v-if="selected.type === 'monster' && selectedMonster" class="editor-grid">
                <div class="editor-field">
                  <label>Name</label>
                  <input
                    :value="selectedMonster.name"
                    @input="(e:any) => { selectedMonster.name = e.target.value; scheduleSave({ name: selectedMonster.name }) }"
                    type="text"
                  />
                </div>

                <div class="editor-field">
                  <label>Hit Points</label>
                  <input
                    :value="selectedMonster.hitPoints"
                    @input="(e:any) => { selectedMonster.hitPoints = Number(e.target.value); scheduleSave({ hitPoints: selectedMonster.hitPoints }) }"
                    type="number"
                  />
                </div>

                <div class="editor-field">
                  <label>Armor Class</label>
                  <input
                    :value="selectedMonster.armorClass"
                    @input="(e:any) => { selectedMonster.armorClass = Number(e.target.value); scheduleSave({ armorClass: selectedMonster.armorClass }) }"
                    type="number"
                  />
                </div>
                <div class="editor-field" style="grid-column: 1 / -1;">
                  <label>Attacks</label>
                  <textarea
                    class="sd-textarea"
                    :value="selectedMonster.attacks ?? ''"
                    @input="(e:any) => { selectedMonster.attacks = e.target.value; scheduleSave({ attacks: selectedMonster.attacks }) }"
                    rows="3"
                    placeholder="Attack lines…"
                  />
                </div>

                <div class="editor-field" style="grid-column: 1 / -1;">
                  <label>Talents</label>
                  <textarea
                    class="sd-textarea"
                    :value="selectedMonster.talents ?? ''"
                    @input="(e:any) => { selectedMonster.talents = e.target.value; scheduleSave({ talents: selectedMonster.talents }) }"
                    rows="4"
                    placeholder="Talents / Features…"
                  />
                </div>

                <div class="editor-field" style="grid-column: 1 / -1;">
                  <label>Spells</label>
                  <textarea
                    class="sd-textarea"
                    :value="selectedMonster.spells ?? ''"
                    @input="(e:any) => { selectedMonster.spells = e.target.value; scheduleSave({ spells: selectedMonster.spells }) }"
                    rows="4"
                    placeholder="Spells…"
                  />
                </div>

                <div class="editor-field" style="grid-column: 1 / -1;">
                  <label>Gear</label>
                  <textarea
                    class="sd-textarea"
                    :value="selectedMonster.gear ?? ''"
                    @input="(e:any) => { selectedMonster.gear = e.target.value; scheduleSave({ gear: selectedMonster.gear }) }"
                    rows="4"
                    placeholder="Inventory…"
                  />
                </div>

                <div class="editor-field" style="grid-column: 1 / -1;">
                  <label>Notes</label>
                  <textarea
                    class="sd-textarea"
                    :value="selectedMonster.notes ?? ''"
                    @input="(e:any) => { selectedMonster.notes = e.target.value; scheduleSave({ notes: selectedMonster.notes }) }"
                    rows="5"
                    placeholder="Notes…"
                  />
                </div>
              </div>
            </div>

            <!-- Hint, wenn nichts ausgewählt -->
            <p v-else class="empty-hint">
              Klick rechts auf ein Sheet, um es zu bearbeiten.
            </p>



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
              @click="openSheet(entry.type, entry.id)"
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
  <!-- CREATE MODAL -->
  <div v-if="showCreate" class="modal-backdrop" @click.self="closeCreate">
    <div class="modal-card">
      <h2 style="margin:0 0 10px;">
        {{ showCreate === 'character' ? 'Charakter erstellen' : 'Monster erstellen' }}
      </h2>

      <div class="field">
        <label>Name</label>

        <input
          v-if="showCreate === 'character'"
          v-model="newCharacter.name"
          type="text"
          placeholder="z.B. Okan der Krieger"
        />

        <input
          v-else
          v-model="newMonster.name"
          type="text"
          placeholder="z.B. Goblin"
        />
      </div>

      <p v-if="createError" class="error-msg" style="margin-top:8px;">
        {{ createError }}
      </p>

      <div class="auth-actions" style="margin-top: 12px;">
        <button class="btn primary" @click="submitCreate" :disabled="isCreating">
          {{ isCreating ? 'Speichere...' : 'Speichern (POST)' }}
        </button>

        <button class="btn ghost" @click="closeCreate" :disabled="isCreating">
          Abbrechen
        </button>
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

.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  z-index: 50;
}

.modal-card {
  width: min(420px, 100%);
  background: rgba(10, 16, 32, 0.98);
  border-radius: 16px;
  padding: 16px 16px 14px;
  border: 1px solid rgba(255, 255, 255, 0.10);
  box-shadow: 0 24px 80px rgba(0,0,0,0.7);
}

.editor-panel {
  background: rgba(7, 12, 26, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 14px;
  padding: 12px;
  margin-bottom: 10px;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.editor-title {
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-size: 12px;
  opacity: 0.9;
}

.editor-status {
  font-size: 12px;
  opacity: 0.8;
}

.editor-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.editor-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
}

.editor-field label {
  opacity: 0.8;
}

.editor-field input {
  padding: 8px 10px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(9, 14, 30, 0.9);
  color: #f5f5f5;
  outline: none;
}
.editor-field input:focus {
  border-color: #4ea3ff;
}

/* Shadowdark-ish sheet layout */
.sd-sheet {
  display: grid;
  gap: 12px;
}

.sd-head {
  display: grid;
  grid-template-columns: 110px minmax(0, 1fr) 180px;
  gap: 10px;
  align-items: end;
}

.sd-brand {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  border: 1px solid rgba(255,255,255,0.14);
  background: rgba(0,0,0,0.22);
  font-weight: 900;
  letter-spacing: 0.12em;
  font-size: 12px;
  text-transform: uppercase;
}

.sd-head-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sd-head-meta {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.sd-label {
  font-size: 11px;
  opacity: 0.8;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.sd-input {
  width: 100%;
  padding: 8px 10px;
  border-radius: 10px;
  border: 1px solid rgba(255,255,255,0.14);
  background: rgba(9,14,30,0.9);
  color: #f5f5f5;
  outline: none;
}

.sd-input:focus {
  border-color: rgba(104, 174, 255, 0.9);
}

.sd-title {
  font-size: 14px;
  font-weight: 700;
}

.sd-body {
  display: grid;
  grid-template-columns: 190px minmax(0, 1fr) 260px;
  gap: 12px;
}

.sd-col {
  border: 1px solid rgba(255,255,255,0.10);
  background: rgba(5, 10, 24, 0.55);
  border-radius: 14px;
  padding: 10px;
}

.sd-stats {
  display: grid;
  gap: 10px;
}

.sd-stat {
  display: grid;
  grid-template-columns: 48px 1fr;
  gap: 8px;
  align-items: center;
  padding: 8px 10px;
  border-radius: 12px;
  border: 1px solid rgba(255,255,255,0.12);
  background: rgba(7, 12, 26, 0.75);
}

.sd-stat-name {
  font-weight: 900;
  letter-spacing: 0.12em;
  opacity: 0.9;
  font-size: 12px;
}

.sd-stat-val {
  width: 100%;
  text-align: right;
  padding: 8px 10px;
  border-radius: 10px;
  border: 1px solid rgba(255,255,255,0.12);
  background: rgba(9,14,30,0.9);
  color: #f5f5f5;
  outline: none;
}

.sd-core {
  display: grid;
  gap: 12px;
}

.sd-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.sd-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sd-badges {
  grid-template-columns: 1fr 1fr;
}

.sd-badge {
  border: 1px solid rgba(255,255,255,0.12);
  background: rgba(7, 12, 26, 0.75);
  border-radius: 14px;
  padding: 10px;
  display: grid;
  gap: 6px;
}

.sd-badge-name {
  font-size: 11px;
  opacity: 0.8;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-weight: 800;
}

.sd-badge-val {
  width: 100%;
  text-align: right;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid rgba(255,255,255,0.12);
  background: rgba(9,14,30,0.9);
  color: #f5f5f5;
  font-size: 16px;
  font-weight: 800;
  outline: none;
}

.sd-flavour {
  display: grid;
  gap: 10px;
}

/* Responsive */
@media (max-width: 1100px) {
  .sd-head {
    grid-template-columns: 110px minmax(0, 1fr);
  }
  .sd-head-meta {
    grid-column: 1 / -1;
    grid-template-columns: 1fr 1fr;
  }
  .sd-body {
    grid-template-columns: 1fr;
  }

  /* Shadowdark-ish sheet layout */
  .sd-sheet {
    display: grid;
    gap: 12px;
  }

  .sd-head {
    display: grid;
    grid-template-columns: 110px minmax(0, 1fr) 180px;
    gap: 10px;
    align-items: end;
  }

  .sd-brand {
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 10px;
    border: 1px solid rgba(255,255,255,0.14);
    background: rgba(0,0,0,0.22);
    font-weight: 900;
    letter-spacing: 0.12em;
    font-size: 12px;
    text-transform: uppercase;
  }

  .sd-head-main {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .sd-head-meta {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 8px;
  }

  .sd-label {
    font-size: 11px;
    opacity: 0.8;
    text-transform: uppercase;
    letter-spacing: 0.08em;
  }

  .sd-input {
    width: 100%;
    padding: 8px 10px;
    border-radius: 10px;
    border: 1px solid rgba(255,255,255,0.14);
    background: rgba(9,14,30,0.9);
    color: #f5f5f5;
    outline: none;
  }

  .sd-input:focus {
    border-color: rgba(104, 174, 255, 0.9);
  }

  .sd-title {
    font-size: 14px;
    font-weight: 700;
  }

  .sd-body {
    display: grid;
    grid-template-columns: 190px minmax(0, 1fr) 260px;
    gap: 12px;
  }

  .sd-col {
    border: 1px solid rgba(255,255,255,0.10);
    background: rgba(5, 10, 24, 0.55);
    border-radius: 14px;
    padding: 10px;
  }

  .sd-stats {
    display: grid;
    gap: 10px;
  }

  .sd-stat {
    display: grid;
    grid-template-columns: 48px 1fr;
    gap: 8px;
    align-items: center;
    padding: 8px 10px;
    border-radius: 12px;
    border: 1px solid rgba(255,255,255,0.12);
    background: rgba(7, 12, 26, 0.75);
  }

  .sd-stat-name {
    font-weight: 900;
    letter-spacing: 0.12em;
    opacity: 0.9;
    font-size: 12px;
  }

  .sd-stat-val {
    width: 100%;
    text-align: right;
    padding: 8px 10px;
    border-radius: 10px;
    border: 1px solid rgba(255,255,255,0.12);
    background: rgba(9,14,30,0.9);
    color: #f5f5f5;
    outline: none;
  }

  .sd-core {
    display: grid;
    gap: 12px;
  }

  .sd-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 10px;
  }

  .sd-field {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .sd-badges {
    grid-template-columns: 1fr 1fr;
  }

  .sd-badge {
    border: 1px solid rgba(255,255,255,0.12);
    background: rgba(7, 12, 26, 0.75);
    border-radius: 14px;
    padding: 10px;
    display: grid;
    gap: 6px;
  }

  .sd-badge-name {
    font-size: 11px;
    opacity: 0.8;
    text-transform: uppercase;
    letter-spacing: 0.08em;
    font-weight: 800;
  }

  .sd-badge-val {
    width: 100%;
    text-align: right;
    padding: 10px 12px;
    border-radius: 12px;
    border: 1px solid rgba(255,255,255,0.12);
    background: rgba(9,14,30,0.9);
    color: #f5f5f5;
    font-size: 16px;
    font-weight: 800;
    outline: none;
  }

  .sd-flavour {
    display: grid;
    gap: 10px;
  }

  /* Responsive */
  @media (max-width: 1100px) {
    .sd-head {
      grid-template-columns: 110px minmax(0, 1fr);
    }
    .sd-head-meta {
      grid-column: 1 / -1;
      grid-template-columns: 1fr 1fr;
    }
    .sd-body {
      grid-template-columns: 1fr;
    }
  }
}


</style>
