<script setup lang="ts">
import type { SheetRecord } from '../types/sheets'

defineProps<{
  entries: SheetRecord[]
  isLoading: boolean
  error: string | null
}>()

const emit = defineEmits<{
  select: [sheet: SheetRecord]
}>()

function formatDate(value?: string) {
  if (!value) {
    return 'Noch nicht gespeichert'
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }

  return date.toLocaleDateString()
}
</script>

<template>
  <div class="sheet-list">
    <p v-if="isLoading" class="state-text">Lade Sheets vom Backend...</p>
    <p v-else-if="error" class="state-text error">{{ error }}</p>
    <p v-else-if="entries.length === 0" class="state-text">
      Noch keine Eintraege. Erstelle links einen Charakter oder ein Monster.
    </p>

    <button
      v-for="entry in entries"
      :key="`${entry.sheetType}-${entry.id ?? entry.name}`"
      class="sheet-card"
      type="button"
      @click="emit('select', entry)"
    >
      <span class="type-tag" :data-type="entry.sheetType">
        {{ entry.sheetType === 'character' ? 'Charakter' : 'Monster' }}
      </span>
      <strong>{{ entry.name || 'Unbenanntes Sheet' }}</strong>
      <small>{{ formatDate(entry.createdAt) }}</small>
    </button>
  </div>
</template>

<style scoped>
.sheet-list {
  display: grid;
  gap: 10px;
}

.state-text {
  margin: 0;
  padding: 18px;
  border: 1px dashed rgba(244, 223, 178, 0.35);
  border-radius: 8px;
  color: rgba(255, 250, 238, 0.78);
}

.state-text.error {
  color: #ff9a9a;
}

.sheet-card {
  display: grid;
  gap: 4px;
  width: 100%;
  padding: 13px 14px;
  border: 1px solid rgba(244, 223, 178, 0.2);
  border-radius: 8px;
  background: rgba(255, 250, 238, 0.92);
  color: #18110c;
  text-align: left;
}

.sheet-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 16px 28px rgba(0, 0, 0, 0.24);
}

.sheet-card strong {
  font-family: Georgia, 'Times New Roman', serif;
  font-size: 20px;
}

.sheet-card small {
  color: #6f5941;
  font-size: 12px;
}

.type-tag {
  width: fit-content;
  padding: 3px 7px;
  border-radius: 4px;
  background: #1c1510;
  color: #f8e4b9;
  font-size: 11px;
  font-weight: 900;
  letter-spacing: 0.09em;
  text-transform: uppercase;
}

.type-tag[data-type='monster'] {
  background: #6c2d23;
}
</style>
