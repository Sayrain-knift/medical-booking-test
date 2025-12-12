<script setup lang="ts">
// 导入路由组件
import { RouterView } from 'vue-router'
</script>

<template>
  <!-- 路由出口，用于渲染当前路由匹配的组件 -->
  <RouterView />
</template>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  font-size: 1rem;
  line-height: 1.5;
  color: #212529;
  background-color: #fff;
}
</style>
