<template>
  <div style="background:#fff; padding:16px">
    <h3>班级列表（联调用）</h3>
    <a-space style="margin-bottom:8px">
      <a-input v-model="keyword" placeholder="按名称搜索" style="width:220px" />
      <a-button @click="load">查询</a-button>
    </a-space>
    <a-table :columns="columns" :data-source="data" rowKey="id" :pagination="false" />
  </div>
</template>

<script>
export default {
  name: 'ClassList',
  data() {
    return { keyword: '', data: [] }
  },
  computed: {
    columns() {
      return [
        { title: '班级名称', dataIndex: 'name' },
        { title: '学费', dataIndex: 'fee' },
        { title: '关联课程ID', dataIndex: 'courseId' }
      ]
    }
  },
  created() { this.load() },
  methods: {
    async load() {
      const url = '/api/class/list' + (this.keyword ? ('?keyword=' + encodeURIComponent(this.keyword)) : '')
      const res = await fetch(url)
      const json = await res.json()
      this.data = (json && json.code === 200) ? (json.data || []) : []
    }
  }
}
</script>

<style scoped>
</style>