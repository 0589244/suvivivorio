<script setup>
import { ref } from 'vue'
import EnemyList from './components/EnemyList.vue'

// Dummy-Daten für M2 (Array nötig für v-for)
const enemies = ref([
  { id: 1, type: 'Slime',   hp: 10, level: 1, x: 120, y: 200 },
  { id: 2, type: 'Bat',     hp:  6, level: 2, x: 260, y: 140 },
  { id: 3, type: 'Goblin',  hp: 20, level: 3, x: 420, y: 260 }
])

function spawn() {
  const id = Date.now()
  enemies.value.push({
    id, type: 'Skeleton', hp: 30, level: 4,
    x: Math.floor(Math.random()*500), y: Math.floor(Math.random()*300)
  })
}
</script>

<template>
  <main style="display:grid;gap:12px;max-width:520px;margin:auto;padding:16px">
    <h1>Survivorio Frontend — M2</h1>

    <!-- Unterkomponente mit v-for -->
    <EnemyList :enemies="enemies" />

    <button @click="spawn"
            style="background:#335bff;color:#fff;border:0;border-radius:10px;padding:.6rem 1rem;cursor:pointer">
      Gegner spawnen
    </button>

    <small style="opacity:.6">Mind. 1 Unterkomponente mit v-for ✅</small>
  </main>
</template>
