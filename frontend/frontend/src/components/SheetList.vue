<script setup lang="ts">
import type { SheetRecord } from '../types/sheets'

defineProps<{
  entries: SheetRecord[]
  isLoading: boolean
  error: string | null
}>()

const emit = defineEmits<{
  select: [sheet: SheetRecord]
  remove: [sheet: SheetRecord]
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

    <article
      v-for="entry in entries"
      :key="`${entry.sheetType}-${entry.id ?? entry.name}`"
      class="sheet-card"
    >
      <button class="sheet-card-main" type="button" @click="emit('select', entry)">
        <span class="type-tag" :data-type="entry.sheetType">
          {{ entry.sheetType === 'character' ? 'Charakter' : 'Monster' }}
        </span>
        <strong>{{ entry.name || 'Unbenanntes Sheet' }}</strong>
        <small>{{ formatDate(entry.createdAt) }}</small>
      </button>

      <button
        class="sheet-delete"
        type="button"
        :aria-label="`${entry.name || 'Unbenanntes Sheet'} loeschen`"
        @click="emit('remove', entry)"
      >
        Loeschen
      </button>
    </article>
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
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  align-items: stretch;
  width: 100%;
  border: 1px solid rgba(244, 223, 178, 0.2);
  border-radius: 8px;
  background: rgba(255, 250, 238, 0.92);
  color: #18110c;
  overflow: hidden;
}

.sheet-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 16px 28px rgba(0, 0, 0, 0.24);
}

.sheet-card-main {
  display: grid;
  gap: 4px;
  min-width: 0;
  padding: 13px 14px;
  border: 0;
  background: transparent;
  color: inherit;
  text-align: left;
}

.sheet-card-main:hover {
  background: rgba(125, 85, 35, 0.08);
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

.sheet-delete {
  align-self: stretch;
  min-width: 88px;
  padding: 0 12px;
  border: 0;
  border-left: 1px solid rgba(92, 42, 28, 0.18);
  background: #7d1f16;
  color: #fff7e8;
  font-size: 12px;
  font-weight: 800;
}

.sheet-delete:hover {
  background: #9b2b20;
}
</style>
