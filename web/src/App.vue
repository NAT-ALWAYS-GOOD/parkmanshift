<script setup lang="ts">
import { ref, onMounted } from 'vue'

const message = ref('En attente du backend...')

onMounted(async () => {
  try {
    const res = await fetch('/api/hello')
    const data = await res.json()
    message.value = data.message
  } catch (e) {
    message.value = 'Erreur de connexion au backend !'
  }
})
</script>

<template>
  <main>
    <h1>🚗 ParkManShift</h1>
    <div class="card">
      <h2>Test du Walking Skeleton</h2>
      <p class="status">{{ message }}</p>
    </div>
  </main>
</template>

<style scoped>
main {
  text-align: center;
  padding: 50px;
  font-family: Arial, sans-serif;
  color: #333;
}
.card {
  border: 1px solid #ccc;
  border-radius: 8px;
  padding: 20px;
  display: inline-block;
  background-color: #f9f9f9;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}
.status {
  font-weight: bold;
  color: #08427b;
}
</style>
