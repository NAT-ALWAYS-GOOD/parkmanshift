<script setup lang="ts">
import NavBar from './components/NavBar.vue'
import ToastNotification from './components/ToastNotification.vue'
import { RouterView } from 'vue-router'
</script>

<template>
  <NavBar />
  <RouterView />
  <ToastNotification />
</template>