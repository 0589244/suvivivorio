<script setup>
defineProps({
  enemies: {
    type: Array,
    required: true
  }
})
</script>

<template>
  <section class="card">
    <h2>Gegner ({{ enemies.length }})</h2>

    <ul class="list">
      <li v-for="e in enemies" :key="e.id" class="item">
        <div class="row">
          <strong>{{ e.type }}</strong>
          <span>{{ e.hp }} HP</span>
        </div>
        <small>Lvl {{ e.level }} • x:{{ e.x }}, y:{{ e.y }}</small>
      </li>
    </ul>

    <p v-if="!enemies.length" class="empty">Keine Gegner</p>
  </section>
</template>

<style scoped>
.card { background:#1f1f1f; border:1px solid #333; border-radius:12px; padding:12px; }
.list { list-style:none; margin:0; padding:0; display:grid; gap:8px; }
.item { background:#151515; border:1px solid #2a2a2a; border-radius:10px; padding:10px; }
.row { display:flex; justify-content:space-between; }
.empty { opacity:.7; font-style:italic; }
</style>
