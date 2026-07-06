<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import SheetList from './components/SheetList.vue'
import type { CharacterSheet, MonsterSheet, SheetRecord, SheetType } from './types/sheets'

const API_BASE_URL = normalizeApiBaseUrl(import.meta.env.VITE_API_URL || 'http://localhost:8080')
const TOKEN_STORAGE_KEY = 'dnd-sheet-token'
const USERNAME_STORAGE_KEY = 'dnd-sheet-username'
const REQUEST_TIMEOUT_MS = 18000
const RETRY_DELAYS_MS = [1800, 4200, 7000]
const PROFILE_IMAGE_MAX_BYTES = 1_500_000
const DEFAULT_PROFILE_IMAGES = {
  male: '/portrait-male.png',
  female: '/portrait-female.png',
  monster: '/portrait-monster.png',
}

type CharacterGender = CharacterSheet['gender']

type AuthResponse = {
  token: string
  username: string
}

const authToken = ref(localStorage.getItem(TOKEN_STORAGE_KEY) || '')
const currentUsername = ref(localStorage.getItem(USERNAME_STORAGE_KEY) || '')
const isLoggedIn = computed(() => Boolean(authToken.value && currentUsername.value))
const isAuthChecking = ref(Boolean(authToken.value))
const isAuthBusy = ref(false)
const isBackendWarming = ref(false)
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
const isDeleting = ref(false)

const activeSheet = ref<SheetRecord | null>(null)
const activeSheetTab = ref<'sheet' | 'journal'>('sheet')
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
    gender: 'male',
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
    journal: '',
    attacks: '',
    gear: '',
    profileImage: defaultCharacterPortrait('male'),
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
    profileImage: DEFAULT_PROFILE_IMAGES.monster,
    gp: 0,
    sp: 0,
    cp: 0,
  }
}

function normalizeCharacter(data: Partial<CharacterSheet>): CharacterSheet {
  const gender = data.gender === 'female' ? 'female' : 'male'

  return {
    ...emptyCharacter(),
    ...data,
    sheetType: 'character',
    gender,
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
    journal: data.journal ?? '',
    profileImage: data.profileImage || defaultCharacterPortrait(gender),
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
    profileImage: data.profileImage || DEFAULT_PROFILE_IMAGES.monster,
  }
}

function cloneSheet(sheet: SheetRecord): SheetRecord {
  return JSON.parse(JSON.stringify(sheet))
}

function numberOrDefault(value: unknown, fallback: number) {
  const numeric = Number(value)
  return Number.isFinite(numeric) ? numeric : fallback
}

function defaultCharacterPortrait(gender: CharacterGender) {
  return gender === 'female' ? DEFAULT_PROFILE_IMAGES.female : DEFAULT_PROFILE_IMAGES.male
}

function defaultSheetPortrait(sheet: SheetRecord) {
  if (sheet.sheetType === 'monster') {
    return DEFAULT_PROFILE_IMAGES.monster
  }

  return defaultCharacterPortrait(sheet.gender)
}

function isDefaultProfileImage(value: string) {
  return Object.values(DEFAULT_PROFILE_IMAGES).some((image) => image === value)
}

function normalizeApiBaseUrl(url: string) {
  return url.replace(/\/+$/, '')
}

onMounted(async () => {
  warmUpBackend()

  if (!authToken.value) {
    isAuthChecking.value = false
    return
  }

  try {
    const auth = await apiFetch<AuthResponse>('/api/auth/me')
    setAuth(auth)
    await loadSheets()
  } catch (error) {
    console.error(error)
    clearAuth()
  } finally {
    isAuthChecking.value = false
  }
})

async function warmUpBackend() {
  isBackendWarming.value = true

  try {
    await apiFetch<{ status: string }>('/api/health', {
      skipAuth: true,
      retry: false,
      timeoutMs: 25000,
    })
  } catch (error) {
    console.warn('Backend warmup failed', error)
  } finally {
    isBackendWarming.value = false
  }
}

type ApiFetchOptions = RequestInit & {
  retry?: boolean
  skipAuth?: boolean
  timeoutMs?: number
}

async function apiFetch<T>(path: string, options: ApiFetchOptions = {}): Promise<T> {
  const headers = new Headers(options.headers)

  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  if (authToken.value && !(options as ApiFetchOptions).skipAuth) {
    headers.set('Authorization', `Bearer ${authToken.value}`)
  }

  let response: Response
  try {
    response = await fetchWithRetry(`${API_BASE_URL}${path}`, {
      ...options,
      headers,
    })
  } catch (error) {
    throw new Error(
      `Das Backend startet gerade oder ist kurz nicht erreichbar (${API_BASE_URL}). Bitte warte einen Moment und versuche es erneut.`,
    )
  }

  if (!response.ok) {
    const message = await readErrorMessage(response)
    if (response.status === 401 && !path.startsWith('/api/auth/login')) {
      clearAuth()
    }
    throw new Error(message)
  }

  if (response.status === 204) {
    return null as T
  }

  return (await response.json()) as T
}

async function fetchWithRetry(url: string, options: ApiFetchOptions) {
  const retry = options.retry !== false
  const timeoutMs = options.timeoutMs ?? REQUEST_TIMEOUT_MS
  const delays = retry ? RETRY_DELAYS_MS : []
  let lastError: unknown

  for (let attempt = 0; attempt <= delays.length; attempt++) {
    try {
      const response = await fetchWithTimeout(url, options, timeoutMs)

      if (!retry || !isTemporaryBackendStatus(response.status) || attempt === delays.length) {
        return response
      }

      await wait(delays[attempt] ?? 0)
    } catch (error) {
      lastError = error

      if (!retry || attempt === delays.length) {
        throw error
      }

      await wait(delays[attempt] ?? 0)
    }
  }

  throw lastError
}

async function fetchWithTimeout(url: string, options: ApiFetchOptions, timeoutMs: number) {
  const controller = new AbortController()
  const timeoutId = window.setTimeout(() => controller.abort(), timeoutMs)
  const { retry, skipAuth, timeoutMs: _timeoutMs, signal, ...fetchOptions } = options

  try {
    return await fetch(url, {
      ...fetchOptions,
      signal: signal ?? controller.signal,
    })
  } finally {
    window.clearTimeout(timeoutId)
  }
}

function isTemporaryBackendStatus(status: number) {
  return [408, 425, 429, 500, 502, 503, 504].includes(status)
}

function wait(ms: number) {
  return new Promise((resolve) => window.setTimeout(resolve, ms))
}

async function readErrorMessage(response: Response) {
  const fallback = `HTTP ${response.status}`
  const text = await response.text()

  if (!text) {
    return fallback
  }

  try {
    const data = JSON.parse(text)
    return data.message || data.error || fallback
  } catch {
    return text
  }
}

function setAuth(auth: AuthResponse) {
  authToken.value = auth.token
  currentUsername.value = auth.username
  localStorage.setItem(TOKEN_STORAGE_KEY, auth.token)
  localStorage.setItem(USERNAME_STORAGE_KEY, auth.username)
}

function clearAuth() {
  authToken.value = ''
  currentUsername.value = ''
  sheets.value = []
  activeSheet.value = null
  localStorage.removeItem(TOKEN_STORAGE_KEY)
  localStorage.removeItem(USERNAME_STORAGE_KEY)
}

async function loadSheets() {
  isLoadingSheets.value = true
  loadError.value = null

  try {
    const [characters, monsters] = await Promise.all([
      apiFetch<Partial<CharacterSheet>[]>('/api/characters'),
      apiFetch<Partial<MonsterSheet>[]>('/api/monsters'),
    ])

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

async function handleLogin() {
  loginError.value = null

  if (!loginUsername.value || !loginPassword.value) {
    loginError.value = 'Bitte Username und Passwort eingeben.'
    return
  }

  isAuthBusy.value = true

  try {
    const auth = await apiFetch<AuthResponse>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({
        username: loginUsername.value,
        password: loginPassword.value,
      }),
    })

    setAuth(auth)
    loginPassword.value = ''
    await loadSheets()
  } catch (error) {
    loginError.value =
      error instanceof Error ? error.message : 'Login fehlgeschlagen.'
  } finally {
    isAuthBusy.value = false
  }
}

async function handleRegister() {
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

  isAuthBusy.value = true

  try {
    const auth = await apiFetch<AuthResponse>('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify({
        username: regUsername.value,
        password: regPassword.value,
      }),
    })

    setAuth(auth)
    loginUsername.value = auth.username
    regPassword.value = ''
    regPasswordRepeat.value = ''
    showRegister.value = false
    await loadSheets()
  } catch (error) {
    regError.value =
      error instanceof Error ? error.message : 'Registrierung fehlgeschlagen.'
  } finally {
    isAuthBusy.value = false
  }
}

async function handleLogout() {
  try {
    if (authToken.value) {
      await apiFetch<null>('/api/auth/logout', { method: 'POST' })
    }
  } catch (error) {
    console.error(error)
  } finally {
    clearAuth()
    loginPassword.value = ''
    regPassword.value = ''
    regPasswordRepeat.value = ''
    showRegister.value = false
  }
}

function openNewCharacter() {
  saveError.value = null
  saveSuccess.value = null
  activeSheet.value = emptyCharacter()
  activeSheetTab.value = 'sheet'
}

function openNewMonster() {
  saveError.value = null
  saveSuccess.value = null
  activeSheet.value = emptyMonster()
  activeSheetTab.value = 'sheet'
}

function openExistingSheet(sheet: SheetRecord) {
  saveError.value = null
  saveSuccess.value = null
  activeSheet.value = cloneSheet(sheet)
  activeSheetTab.value = 'sheet'
}

function closeSheetEditor() {
  activeSheet.value = null
  activeSheetTab.value = 'sheet'
}

function handleCharacterGenderChange(gender: CharacterGender) {
  if (!activeSheet.value || activeSheet.value.sheetType !== 'character') {
    return
  }

  const currentImage = activeSheet.value.profileImage
  activeSheet.value.gender = gender

  if (!currentImage || isDefaultProfileImage(currentImage)) {
    activeSheet.value.profileImage = defaultCharacterPortrait(gender)
  }

  saveSuccess.value = null
  saveError.value = null
}

async function handleProfileImageUpload(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]

  if (!activeSheet.value || !file) {
    return
  }

  saveSuccess.value = null
  saveError.value = null

  if (!file.type.startsWith('image/')) {
    saveError.value = 'Bitte eine Bilddatei auswaehlen.'
    input.value = ''
    return
  }

  if (file.size > PROFILE_IMAGE_MAX_BYTES) {
    saveError.value = 'Das Bild ist zu gross. Bitte unter 1,5 MB bleiben.'
    input.value = ''
    return
  }

  try {
    activeSheet.value.profileImage = await readFileAsDataUrl(file)
  } catch (error) {
    console.error(error)
    saveError.value = 'Bild konnte nicht geladen werden.'
  } finally {
    input.value = ''
  }
}

function removeProfileImage() {
  if (!activeSheet.value) {
    return
  }

  activeSheet.value.profileImage = defaultSheetPortrait(activeSheet.value)
  saveSuccess.value = null
  saveError.value = null
}

function readFileAsDataUrl(file: File) {
  return new Promise<string>((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result || ''))
    reader.onerror = () => reject(reader.error)
    reader.readAsDataURL(file)
  })
}

async function deleteSheet(sheet: SheetRecord) {
  if (!sheet.id) {
    return
  }

  const label = sheet.sheetType === 'character' ? 'Charakter' : 'Monster'
  const name = sheet.name || 'Unbenanntes Sheet'

  if (!window.confirm(`${label} "${name}" wirklich loeschen?`)) {
    return
  }

  const endpoint = sheet.sheetType === 'character' ? 'characters' : 'monsters'
  isDeleting.value = true
  saveError.value = null
  saveSuccess.value = null
  loadError.value = null

  try {
    await apiFetch<null>(`/api/${endpoint}/${sheet.id}`, { method: 'DELETE' })
    sheets.value = sheets.value.filter(
      (entry) => !(entry.sheetType === sheet.sheetType && entry.id === sheet.id),
    )

    if (activeSheet.value?.sheetType === sheet.sheetType && activeSheet.value.id === sheet.id) {
      activeSheet.value = null
    }
  } catch (error) {
    console.error(error)
    saveError.value = error instanceof Error ? error.message : 'Fehler beim Loeschen'
    loadError.value = saveError.value
  } finally {
    isDeleting.value = false
  }
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
  const path = `/api/${endpoint}${sheet.id ? `/${sheet.id}` : ''}`
  const payload = toBackendPayload(sheet)

  try {
    const savedData = await apiFetch<Partial<SheetRecord>>(path, {
      method,
      body: JSON.stringify(payload),
    })
    const savedSheet =
      sheet.sheetType === 'character'
        ? normalizeCharacter(savedData as Partial<CharacterSheet>)
        : normalizeMonster(savedData as Partial<MonsterSheet>)

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
    <section v-if="isAuthChecking" class="auth-screen">
      <div class="auth-backdrop"></div>
      <div class="auth-card">
        <p class="kicker">Campaign sheets</p>
        <h1>DnD Sheet Manager</h1>
        <p class="success-msg">Session wird geprueft...</p>
      </div>
    </section>

    <section v-else-if="!isLoggedIn" class="auth-screen">
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
          <p v-else-if="isBackendWarming" class="success-msg">
            Backend wird vorbereitet. Der erste Login kann kurz dauern.
          </p>

          <div class="auth-actions">
            <button class="primary" type="button" :disabled="isAuthBusy" @click="handleLogin">
              {{ isAuthBusy ? 'Prueft...' : 'Einloggen' }}
            </button>
            <button class="secondary" type="button" :disabled="isAuthBusy" @click="showRegister = true">
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
          <p v-else-if="isBackendWarming" class="success-msg">
            Backend wird vorbereitet. Das erste Erstellen kann kurz dauern.
          </p>

          <div class="auth-actions">
            <button class="primary" type="button" :disabled="isAuthBusy" @click="handleRegister">
              {{ isAuthBusy ? 'Erstellt...' : 'Erstellen' }}
            </button>
            <button class="secondary" type="button" :disabled="isAuthBusy" @click="showRegister = false">
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
          <span>{{ currentUsername || 'User' }}</span>
          <button class="secondary" type="button" @click="loadSheets">Neu laden</button>
          <button class="secondary" type="button" @click="handleLogout">Logout</button>
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
            <span>Stats, Waffen und Loot fuer den Dungeonmaster.</span>
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
        <div class="sheet-nav">
          <button
            class="sheet-nav-item"
            :class="{ active: activeSheetTab === 'sheet' }"
            type="button"
            @click="activeSheetTab = 'sheet'"
          >
            Sheet
          </button>
          <button
            v-if="activeSheet.sheetType === 'character'"
            class="sheet-nav-item"
            :class="{ active: activeSheetTab === 'journal' }"
            type="button"
            @click="activeSheetTab = 'journal'"
          >
            Journal
          </button>
        </div>

        <div class="sheet-layout">
          <aside class="sheet-profile-panel">
            <div class="profile-frame">
              <div class="profile-preview">
                <img v-if="activeSheet.profileImage" :src="activeSheet.profileImage" alt="" />
                <span v-else>Bild</span>
              </div>
            </div>

            <div class="profile-name-card">
              <strong>{{ activeSheet.name || (activeSheet.sheetType === 'character' ? 'Neuer Charakter' : 'Neues Monster') }}</strong>
              <span v-if="activeSheet.sheetType === 'character'">
                Stufe {{ activeSheet.level || 1 }} {{ activeSheet.className || 'Klasse' }}
              </span>
              <span v-else>
                Stufe {{ activeSheet.challenge || '-' }} {{ activeSheet.type || 'Monsterart' }}
              </span>
            </div>

            <div class="profile-quick-stats">
              <span>
                TP
                <strong>{{ activeSheet.sheetType === 'character' ? activeSheet.hp : activeSheet.hitPoints }}</strong>
              </span>
              <span>
                RK
                <strong>{{ activeSheet.sheetType === 'character' ? activeSheet.ac : activeSheet.armorClass }}</strong>
              </span>
            </div>

            <div v-if="activeSheet.sheetType === 'character'" class="gender-picker">
              <p>Geschlecht</p>
              <div class="gender-options">
                <button
                  class="gender-option"
                  :class="{ active: activeSheet.gender === 'male' }"
                  type="button"
                  @click="handleCharacterGenderChange('male')"
                >
                  Maennlich
                </button>
                <button
                  class="gender-option"
                  :class="{ active: activeSheet.gender === 'female' }"
                  type="button"
                  @click="handleCharacterGenderChange('female')"
                >
                  Weiblich
                </button>
              </div>
            </div>

            <div class="profile-buttons">
              <label class="profile-upload">
                Portrait hochladen
                <input type="file" accept="image/*" @change="handleProfileImageUpload" />
              </label>
              <button
                v-if="activeSheet.profileImage && !isDefaultProfileImage(activeSheet.profileImage)"
                class="profile-remove"
                type="button"
                @click="removeProfileImage"
              >
                Standardbild nutzen
              </button>
            </div>
          </aside>

          <section class="sheet-paper">
            <header class="sheet-header">
              <div class="sheet-title-area">
                <p class="sheet-kicker">{{ activeSheet.sheetType === 'character' ? 'Spieler-Sheet' : 'Dungeonmaster-Sheet' }}</p>
                <h2>{{ activeSheet.sheetType === 'character' ? 'Charakter' : 'Monster' }}</h2>
                <p class="sheet-subtitle">
                  {{ activeSheet.name || (activeSheet.sheetType === 'character' ? 'Neues Charakterblatt' : 'Neues Monsterblatt') }}
                </p>
              </div>

              <div class="sheet-actions">
                <span v-if="saveSuccess" class="success-msg">{{ saveSuccess }}</span>
                <span v-if="saveError" class="error-msg">{{ saveError }}</span>
                <button class="primary dark" type="submit" :disabled="isSaving || isDeleting">
                  {{ isSaving ? 'Speichert...' : 'Speichern' }}
                </button>
                <button
                  v-if="activeSheet.id"
                  class="danger dark"
                  type="button"
                  :disabled="isSaving || isDeleting"
                  @click="deleteSheet(activeSheet)"
                >
                  {{ isDeleting ? 'Loescht...' : 'Loeschen' }}
                </button>
                <button class="secondary dark" type="button" @click="closeSheetEditor">
                  Schliessen
                </button>
              </div>
            </header>

            <div v-if="activeSheetTab === 'sheet'" class="sheet-grid">
              <section class="stats-column">
                <p class="section-heading">Attribute</p>
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
                  Trefferpunkte
                  <input v-model.number="activeSheet.hp" type="number" min="0" />
                </label>
                <label v-if="activeSheet.sheetType === 'character'" class="big-stat">
                  Ruestungsklasse
                  <input v-model.number="activeSheet.ac" type="number" min="0" />
                </label>

                <label v-if="activeSheet.sheetType === 'monster'" class="big-stat">
                  Trefferpunkte
                  <input v-model.number="activeSheet.hitPoints" type="number" min="0" />
                </label>
                <label v-if="activeSheet.sheetType === 'monster'" class="big-stat">
                  Ruestungsklasse
                  <input v-model.number="activeSheet.armorClass" type="number" min="0" />
                </label>

                <label class="wide-field weapons-field">
                  Waffen
                  <textarea v-model="activeSheet.attacks"></textarea>
                </label>
              </section>

              <section class="identity-column">
                <p class="section-heading">Profil</p>
                <label class="wide-field">
                  Name
                  <input v-model="activeSheet.name" type="text" />
                </label>

                <template v-if="activeSheet.sheetType === 'character'">
                  <label class="wide-field">
                    Herkunft
                    <input v-model="activeSheet.ancestry" type="text" />
                  </label>
                  <label class="wide-field">
                    Klasse
                    <input v-model="activeSheet.className" type="text" />
                  </label>
                  <div class="two-fields">
                    <label class="wide-field">
                      Stufe
                      <input v-model.number="activeSheet.level" type="number" min="1" />
                    </label>
                    <label class="wide-field">
                      XP
                      <input v-model.number="activeSheet.xp" type="number" min="0" />
                    </label>
                  </div>
                  <label class="wide-field">
                    Titel
                    <input v-model="activeSheet.title" type="text" />
                  </label>
                  <label class="wide-field">
                    Gesinnung
                    <input v-model="activeSheet.alignment" type="text" />
                  </label>
                  <label class="wide-field">
                    Hintergrund
                    <input v-model="activeSheet.background" type="text" />
                  </label>
                  <label class="wide-field">
                    Gottheit
                    <input v-model="activeSheet.deity" type="text" />
                  </label>
                </template>

                <template v-else>
                  <label class="wide-field">
                    Art
                    <input v-model="activeSheet.type" type="text" />
                  </label>
                  <label class="wide-field">
                    Stufe
                    <input v-model="activeSheet.challenge" type="text" />
                  </label>
                </template>
              </section>

              <section class="notes-column">
                <p class="section-heading">
                  {{ activeSheet.sheetType === 'character' ? 'Zauber/Talente und Ausruestung' : 'Besonderheiten/Notizen und Ausruestung' }}
                </p>
                <label v-if="activeSheet.sheetType === 'character'" class="wide-field tall">
                  Zauber / Talente
                  <textarea v-model="activeSheet.talentsSpells"></textarea>
                </label>
                <label v-else class="wide-field tall">
                  Besonderheiten / Notizen
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
                  Ausruestung
                  <textarea v-model="activeSheet.gear"></textarea>
                </label>
              </section>
            </div>

            <div v-else-if="activeSheet.sheetType === 'character'" class="journal-page">
              <section class="journal-panel">
                <p class="section-heading">Journal</p>
                <label class="wide-field journal-field">
                  Geschehnisse / Notizen
                  <textarea v-model="activeSheet.journal"></textarea>
                </label>
              </section>
            </div>
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
  width: min(480px, 100%);
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
.secondary,
.danger {
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
    linear-gradient(rgba(12, 8, 6, 0.72), rgba(12, 8, 6, 0.78)),
    url('/camping.jpg') center / cover;
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
  padding: 18px;
  overflow: auto;
  background:
    radial-gradient(circle at 50% 0%, rgba(195, 154, 69, 0.16), transparent 36%),
    rgba(0, 0, 0, 0.82);
}

.sheet-window {
  width: min(1220px, 100%);
  max-height: calc(100vh - 36px);
  overflow: auto;
  border: 1px solid rgba(223, 183, 102, 0.68);
  border-radius: 8px;
  background:
    linear-gradient(135deg, rgba(13, 24, 34, 0.98), rgba(34, 24, 18, 0.98)),
    #152638;
  color: #1e1510;
  box-shadow: 0 30px 120px rgba(0, 0, 0, 0.72);
}

.sheet-nav {
  display: flex;
  align-items: center;
  min-height: 46px;
  border-bottom: 1px solid rgba(223, 183, 102, 0.38);
  background: #20394f;
}

.sheet-nav-item {
  display: grid;
  place-items: center;
  min-width: 96px;
  min-height: 46px;
  padding: 0 18px;
  border-radius: 0;
  background: transparent;
  color: #e6d1a6;
  cursor: pointer;
  font-family: inherit;
  font-size: 13px;
  font-weight: 900;
}

.sheet-nav-item.active {
  background: #7f2b22;
  color: #fff8e8;
}

.sheet-layout {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 18px;
  align-items: stretch;
  padding: 18px;
}

.sheet-profile-panel {
  display: grid;
  gap: 12px;
  align-content: start;
}

.profile-frame {
  padding: 10px;
  border: 3px double #d1ab5f;
  border-radius: 8px;
  background:
    linear-gradient(rgba(8, 18, 29, 0.82), rgba(8, 18, 29, 0.94)),
    radial-gradient(circle at 50% 22%, rgba(209, 171, 95, 0.26), transparent 58%);
  box-shadow:
    inset 0 0 0 1px rgba(255, 248, 232, 0.12),
    0 18px 32px rgba(0, 0, 0, 0.34);
}

.profile-name-card {
  display: grid;
  gap: 6px;
  padding: 18px;
  border: 1px solid rgba(209, 171, 95, 0.4);
  border-top: 4px solid #7f2b22;
  border-radius: 6px;
  background:
    linear-gradient(rgba(255, 251, 240, 0.92), rgba(238, 223, 191, 0.94)),
    #f6ecd5;
  color: #221712;
  text-align: center;
  box-shadow: 0 14px 26px rgba(0, 0, 0, 0.22);
}

.profile-name-card strong {
  font-family: Georgia, 'Times New Roman', serif;
  font-size: 26px;
  line-height: 1.05;
}

.profile-name-card span {
  color: #6b5740;
  font-size: 13px;
  font-weight: 800;
}

.profile-quick-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.profile-quick-stats span {
  display: grid;
  gap: 5px;
  min-height: 62px;
  place-items: center;
  border: 1px solid rgba(209, 171, 95, 0.42);
  border-radius: 6px;
  background: rgba(255, 248, 232, 0.9);
  color: #5f4733;
  font-size: 12px;
  font-weight: 900;
}

.profile-quick-stats strong {
  color: #7f2b22;
  font-family: Georgia, 'Times New Roman', serif;
  font-size: 26px;
  line-height: 1;
}

.gender-picker {
  display: grid;
  gap: 8px;
  padding: 12px;
  border: 1px solid rgba(209, 171, 95, 0.42);
  border-radius: 6px;
  background: rgba(255, 248, 232, 0.9);
}

.gender-picker p {
  margin: 0;
  color: #5f4733;
  font-size: 12px;
  font-weight: 900;
  text-align: center;
  text-transform: uppercase;
}

.gender-options {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.gender-option {
  min-height: 36px;
  border: 1px solid rgba(127, 43, 34, 0.28);
  border-radius: 5px;
  background: rgba(246, 236, 213, 0.8);
  color: #221712;
  font-size: 12px;
  font-weight: 900;
}

.gender-option.active {
  background: #7f2b22;
  color: #fff8e8;
}

.sheet-paper {
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
  border: 1px solid #9c3d31;
  border-radius: 6px;
  background:
    linear-gradient(rgba(250, 238, 210, 0.88), rgba(250, 238, 210, 0.95)),
    radial-gradient(circle at 22% 10%, rgba(255, 255, 255, 0.46), transparent 34%),
    repeating-linear-gradient(0deg, #f6ecd5, #f6ecd5 20px, #eadab8 21px);
  box-shadow:
    inset 0 0 0 1px rgba(255, 250, 238, 0.65),
    0 22px 44px rgba(0, 0, 0, 0.3);
}

.sheet-header {
  position: sticky;
  top: 0;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 24px 16px;
  border-bottom: 2px solid #7b2b22;
  background:
    linear-gradient(90deg, rgba(246, 236, 213, 0.98), rgba(235, 216, 178, 0.98)),
    #f6ecd5;
}

.sheet-title-area {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.sheet-header h2 {
  margin: 0;
  font-family: Georgia, 'Times New Roman', serif;
  font-size: clamp(44px, 6vw, 76px);
  font-weight: 900;
  line-height: 0.95;
  color: #7b2b22;
}

.sheet-kicker {
  margin: 0 0 6px;
  color: #5f4733;
  font-size: 12px;
  font-weight: 900;
  text-transform: uppercase;
}

.sheet-subtitle {
  margin: 0;
  color: #5f4733;
  font-size: 14px;
  font-weight: 800;
}

.profile-control {
  display: flex;
  align-items: center;
  gap: 10px;
}

.profile-preview {
  display: grid;
  place-items: center;
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  border: 1px solid rgba(195, 154, 69, 0.75);
  border-radius: 4px;
  background:
    linear-gradient(rgba(15, 31, 45, 0.9), rgba(15, 31, 45, 0.98)),
    #132333;
  color: #f4dfb2;
  font-size: 13px;
  font-weight: 900;
  text-transform: uppercase;
}

.profile-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-buttons {
  display: grid;
  gap: 6px;
}

.profile-upload,
.profile-remove {
  min-height: 40px;
  padding: 9px 12px;
  border: 1px solid rgba(209, 171, 95, 0.55);
  border-radius: 5px;
  background: #f6ecd5;
  color: #19120d;
  cursor: pointer;
  font-size: 12px;
  font-weight: 900;
  text-align: center;
}

.profile-upload input {
  display: none;
}

.profile-remove {
  background: #7d1f16;
  color: #fffaf0;
}

.sheet-actions {
  justify-content: flex-end;
}

.primary.dark {
  background: #14100d;
  color: #fffaf0;
}

.primary.dark:hover {
  background: #2b2119;
}

.secondary.dark {
  border-color: rgba(34, 23, 18, 0.5);
  background: rgba(255, 250, 238, 0.88);
  color: #19120d;
}

.danger.dark {
  border-color: #7d1f16;
  background: #7d1f16;
  color: #fffaf0;
}

.danger.dark:hover {
  background: #9b2b20;
}

.sheet-grid {
  display: grid;
  grid-template-columns: 0.9fr 1.12fr 1.38fr;
  gap: 12px;
  padding: 22px 24px 26px;
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

.section-heading {
  margin: 0;
  padding: 10px 12px;
  border: 1px solid #152637;
  border-radius: 5px;
  background: #1f3448;
  color: #fffaf0;
  font-size: 12px;
  font-weight: 900;
  text-transform: uppercase;
}

.journal-page {
  display: grid;
  flex: 1;
  min-height: 420px;
  padding: 22px 24px 26px;
}

.journal-panel {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  gap: 12px;
  min-height: 0;
}

.journal-field {
  grid-template-rows: auto minmax(0, 1fr);
  min-height: 0;
}

.journal-field textarea {
  height: 100%;
  min-height: min(64vh, 620px);
}

.stats-column .section-heading {
  grid-column: 1 / -1;
}

.stat-box,
.big-stat,
.wide-field,
.coin-row label {
  position: relative;
  display: grid;
  gap: 7px;
  border: 1px solid rgba(92, 48, 38, 0.42);
  border-radius: 5px;
  background:
    linear-gradient(rgba(255, 252, 244, 0.82), rgba(242, 226, 193, 0.76)),
    rgba(255, 252, 244, 0.72);
  color: #221712;
  padding: 10px;
  font-size: 12px;
  font-weight: 900;
  text-transform: uppercase;
  box-shadow: inset 0 -1px 0 rgba(127, 43, 34, 0.14);
}

.stat-box input,
.big-stat input,
.wide-field input,
.wide-field textarea,
.coin-row input {
  border: 0;
  border-bottom: 2px solid rgba(92, 48, 38, 0.72);
  border-radius: 0;
  background: transparent;
  box-shadow: none;
  padding: 2px 0 4px;
  min-height: 30px;
  color: #1e1510;
  font-weight: 900;
}

.stat-box input,
.big-stat input {
  font-family: Georgia, 'Times New Roman', serif;
  font-size: 22px;
  line-height: 1;
}

.wide-field textarea {
  min-height: 72px;
}

.big-stat {
  min-height: 88px;
}

.weapons-field {
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

input[type='number'] {
  appearance: textfield;
}

input[type='number']::-webkit-outer-spin-button,
input[type='number']::-webkit-inner-spin-button {
  margin: 0;
  appearance: none;
}

@media (max-width: 900px) {
  .dashboard,
  .sheet-layout,
  .sheet-grid {
    grid-template-columns: 1fr;
  }

  .topbar,
  .list-toolbar,
  .sheet-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .sheet-profile-panel {
    grid-template-columns: minmax(170px, 230px) minmax(0, 1fr);
    align-items: start;
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
  .sheet-profile-panel,
  .coin-row {
    grid-template-columns: 1fr;
  }
}
</style>
