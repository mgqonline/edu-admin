<template>
  <div style="background:#fff; padding:16px">
    <h2>试听转化报表</h2>
    <a-space style="margin-bottom:12px">
      <a-button type="primary" @click="loadTrend" :loading="loading">加载6个月转化趋势</a-button>
      <a-button @click="exportTrend" :disabled="loading">导出趋势CSV</a-button>
    </a-space>
    <a-table :data-source="points" :columns="columns" :pagination="false" rowKey="month" />
  </div>
</template>

<script>
export default {
  name: 'ReportAudition',
  data(){ return { loading:false, points: [] } },
  computed:{
    columns(){ return [ { title:'月份', dataIndex:'month', key:'month' }, { title:'完成试听', dataIndex:'finished', key:'finished' }, { title:'30日内转化', dataIndex:'converted', key:'converted' }, { title:'转化率(%)', dataIndex:'rate', key:'rate' } ] }
  },
  methods:{
    async loadTrend(){ this.loading = true; try { const r = await fetch('/api/report/student/trial-conversion-trend'); const j=await r.json(); this.points = (j && j.code===200) ? ((j.data && j.data.points) || []) : [] } finally { this.loading=false } },
    exportTrend(){ window.open('/api/report/export/trial-conversion-trend.csv', '_blank') }
  },
  mounted(){ this.loadTrend() }
}
</script>

<style scoped>
</style>