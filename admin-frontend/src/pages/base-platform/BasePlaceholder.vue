<template>
  <div class="base-placeholder">
    <a-page-header :title="pageTitle" :sub-title="subTitle" />
    <a-card bordered>
      <div class="content">
        该功能页面尚未实现或正在建设中。
      </div>
      <div class="tips">权限值：{{ permValue }} · 路由：/base/{{ code }}</div>
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'BasePlaceholder',
  data() {
    return { code: (this.$route && this.$route.params && this.$route.params.code) || '', label: '' }
  },
  computed: {
    pageTitle() { return (this.label || this.code || '功能') + '（占位页）' },
    subTitle() { return '请在前端路由和页面目录中实现该功能页面' },
    permValue() { return 'menu:base:' + String(this.code || '') }
  },
  created() {
    this.fetchLabel()
  },
  methods: {
    async fetchLabel() {
      try {
        const res = await fetch('/api/base/perm/catalog/items?type=menu')
        const json = await res.json(); const arr = Array.isArray(json.data) ? json.data : []
        const found = arr.find(it => String(it.value||'') === this.permValue)
        this.label = found ? (found.label || '') : ''
      } catch(e) {}
    }
  }
}
</script>

<style scoped>
.base-placeholder { background:#fff; padding:16px; }
.content { padding: 12px 0; color:#2f3b4c; }
.tips { margin-top:8px; color:#9aa4b5; font-size:12px; }
</style>