<script setup lang="ts">
defineProps<{ open: boolean; title?: string }>()
defineEmits<{ (e: 'close'): void }>()
</script>

<template>
  <Teleport to="body">
    <Transition name="backdrop">
      <div v-if="open" class="backdrop" @click="$emit('close')" />
    </Transition>
    <div :class="['drawer', { 'drawer--open': open }]" role="dialog">
      <div class="drawer-header">
        <span v-if="title" class="drawer-title">{{ title }}</span>
        <button class="close-btn" @click="$emit('close')">✕</button>
      </div>
      <div class="drawer-body">
        <slot />
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 40;
}

.backdrop-enter-active,
.backdrop-leave-active {
  transition: opacity 0.2s ease;
}
.backdrop-enter-from,
.backdrop-leave-to {
  opacity: 0;
}

.drawer {
  position: fixed;
  top: 0;
  right: 0;
  height: 100dvh;
  width: 50vw;
  background: var(--bg);
  border-left: 1px solid var(--border);
  z-index: 50;
  display: flex;
  flex-direction: column;
  transform: translateX(100%);
  transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: -4px 0 24px rgba(0, 0, 0, 0.1);
}

.drawer--open {
  transform: translateX(0);
}

@media (max-width: 768px) {
  .drawer {
    width: 100vw;
    border-left: none;
  }
}

.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
}

.drawer-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-h);
  font-family: var(--mono);
}

.close-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 14px;
  color: var(--text);
  padding: 4px 8px;
  border-radius: 5px;
  line-height: 1;
}

.close-btn:hover {
  background: var(--code-bg);
  color: var(--text-h);
}

.drawer-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
