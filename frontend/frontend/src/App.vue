<script setup>
import { ref, onMounted } from 'vue'
import EnemyList from './components/EnemyList.vue'

// -----------------------------------------
// Gegnerliste aus M2 (NICHT verändert) ✔
// -----------------------------------------
const enemies = ref([
  { id: 1, type: 'Slime',   hp: 10, level: 1, x: 120, y: 200 },
  { id: 2, type: 'Bat',     hp:  6, level: 2, x: 260, y: 140 },
  { id: 3, type: 'Goblin',  hp: 20, level: 3, x: 420, y: 260 }
])

function spawn() {
  const id = Date.now()
  enemies.value.push({
    id, type: 'Skeleton', hp: 30, level: 4,
    x: Math.floor(Math.random()*500),
    y: Math.floor(Math.random()*300)
  })
}

// -----------------------------------------
// Backend URL automatisch wählen ✔
// -----------------------------------------

const BACKEND =
  window.location.hostname === "localhost"
    ? "http://localhost:8080" // Lokale Entwicklung
    : "https://suvivivoriobackend.onrender.com" // Render Backend

// -----------------------------------------
// Player aus Backend laden ✔
// -----------------------------------------

const player = ref(null)
const isLoadingPlayer = ref(false)
const playerError = ref(null)

async function loadPlayer() {
  isLoadingPlayer.value = true
  playerError.value = null

  try {
    const res = await fetch(`${BACKEND}/player`)
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    player.value = await res.json()
  } catch (err) {
    playerError.value = err.message
  } finally {
    isLoadingPlayer.value = false
  }
}

onMounted(() => {
  loadPlayer()
})
</script>

<template>
  <main style="display:grid;gap:12px;max-width:520px;margin:auto;padding:16px">
    <h1>Survivorio Frontend — M2 + Backend (M3)</h1>

    <!-- Unterkomponente aus M2 -->
    <EnemyList :enemies="enemies" />

    <button @click="spawn"
            style="background:#335bff;color:#fff;border:0;border-radius:10px;padding:.6rem 1rem;cursor:pointer">
      Gegner spawnen
    </button>

    <small style="opacity:.6">Mind. 1 Unterkomponente mit v-for ✅</small>


    <!-- Player Anzeige -->
    <section style="padding:12px;background:#1a1f37;border-radius:10px;color:#fff">
      <h2>Player aus Backend (GET /player)</h2>

      <button @click="loadPlayer"
              style="background:#44aa55;color:#fff;border:0;border-radius:8px;padding:.4rem 1rem;cursor:pointer;margin-bottom:6px">
        {{ isLoadingPlayer ? "Lade..." : "Player neu laden" }}
      </button>

      <div v-if="player">
        <p><strong>ID:</strong> {{ player.id }}</p>
        <p><strong>Name:</strong> {{ player.name }}</p>
        <p><strong>Level:</strong> {{ player.level }}</p>
      </div>

      <p v-else>Kein Player geladen...</p>
      <p v-if="playerError" style="color:#ff7777">Fehler: {{ playerError }}</p>
    </section>
  </main>
</template>
