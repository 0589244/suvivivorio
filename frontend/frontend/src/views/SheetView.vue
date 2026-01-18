<script setup lang="ts">
import { onMounted, ref } from "vue";

type Character = {
  id?: number;
  name: string;
  ancestry?: string;
  className?: string;
  level?: number;
  xp?: number;
  createdAt?: string;
};

const API_BASE = "http://localhost:8080/api";

const characters = ref<Character[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);

const form = ref<Character>({
  name: "",
  ancestry: "",
  className: "",
  level: 1,
  xp: 0,
});

async function loadCharacters() {
  loading.value = true;
  error.value = null;
  try {
    const res = await fetch(`${API_BASE}/characters`);
    if (!res.ok) throw new Error(`GET failed: ${res.status}`);
    characters.value = await res.json();
  } catch (e: any) {
    error.value = e?.message ?? "Unknown error";
  } finally {
    loading.value = false;
  }
}

async function createCharacter() {
  error.value = null;

  if (!form.value.name.trim()) {
    error.value = "Name darf nicht leer sein.";
    return;
  }

  try {
    const res = await fetch(`${API_BASE}/characters`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(form.value),
    });
    if (!res.ok) throw new Error(`POST failed: ${res.status}`);
    await res.json();

    // Form reset (optional)
    form.value = { name: "", ancestry: "", className: "", level: 1, xp: 0 };

    // Nach POST neu laden -> Nachweis für M4
    await loadCharacters();
  } catch (e: any) {
    error.value = e?.message ?? "Unknown error";
  }
}

onMounted(loadCharacters);
</script>

<template>
  <main style="max-width: 800px; margin: 24px auto; padding: 16px;">
    <h1>Sheet (M4 Test)</h1>

    <section style="margin: 16px 0; padding: 12px; border: 1px solid #ddd; border-radius: 8px;">
      <h2>Create Character</h2>

      <div style="display: grid; gap: 8px; grid-template-columns: 1fr 1fr;">
        <label>
          Name*
          <input v-model="form.name" type="text" style="width: 100%;" />
        </label>

        <label>
          Ancestry
          <input v-model="form.ancestry" type="text" style="width: 100%;" />
        </label>

        <label>
          Class
          <input v-model="form.className" type="text" style="width: 100%;" />
        </label>

        <label>
          Level
          <input v-model.number="form.level" type="number" min="1" style="width: 100%;" />
        </label>
      </div>

      <button @click="createCharacter" style="margin-top: 12px;">
        Create (POST /api/characters)
      </button>

      <p v-if="error" style="color: red; margin-top: 8px;">
        {{ error }}
      </p>
    </section>

    <section style="margin: 16px 0;">
      <h2>Characters</h2>
      <p v-if="loading">Loading...</p>

      <ul v-else>
        <li v-for="c in characters" :key="c.id">
          <b>#{{ c.id }}</b> {{ c.name }} — lvl {{ c.level ?? "?" }}
          <small v-if="c.createdAt"> ({{ c.createdAt }})</small>
        </li>
      </ul>
    </section>
  </main>
</template>
