<template>
  <div style="background:#fff; padding:16px">
    <h2>招生报表</h2>
    <a-space style="margin-bottom:12px">
      <a-select v-model="range" style="width:150px">
        <a-select-option value="day">最近14天</a-select-option>
        <a-select-option value="week">最近8周</a-select-option>
        <a-select-option value="month">最近6个月</a-select-option>
      </a-select>
      <a-button type="primary" @click="loadEnrollNew" :loading="loadingNew">加载趋势</a-button>
      <a-button @click="exportEnrollNew" :disabled="loadingNew">导出趋势CSV</a-button>
    </a-space>
    <a-table :data-source="points" :columns="columnsNew" :pagination="false" rowKey="_key" />

    <h3 style="margin-top:24px">渠道占比</h3>
    <a-space style="margin-bottom:8px">
      <a-button type="primary" @click="loadChannelShare" :loading="loadingShare">加载渠道占比</a-button>
      <a-button @click="exportChannelShare" :disabled="loadingShare">导出渠道占比CSV</a-button>
    </a-space>
    <a-table :data-source="channelShare" :columns="columnsShare" :pagination="false" rowKey="channel" />
  </div>
</template>

<script>
export default {
  name: 'ReportEnroll',
  data(){
    return {
      range: 'month',
      loadingNew: false,
      loadingShare: false,
      points: [],
      channelShare: []
    }
  },
  computed:{
    columnsNew(){
      return [
        { title: this.range==='day'?'日期':(this.range==='week'?'周':'月份'), dataIndex: this.range==='day'?'date':(this.range==='week'?'week':'month'), key: 'key' },
        { title: '新增报名数', dataIndex: 'count', key: 'count' }
      ]
    },
    columnsShare(){
      return [
        { title: '渠道', dataIndex: 'channel', key: 'channel' },
        { title: '人数', dataIndex: 'count', key: 'count' },
        { title: '占比(%)', dataIndex: 'ratio', key: 'ratio' }
      ]
    }
  },
  methods:{
    async loadEnrollNew(){
      this.loadingNew = true
      try {
        const r = await fetch(`/api/report/enroll/new?range=${this.range}`)
        const j = await r.json()
        const pts = (j && j.code===200) ? (j.data && j.data.points || []) : []
        this.points = pts.map((p,i)=>({ ...p, _key: i }))
      } finally { this.loadingNew = false }
    },
    async loadChannelShare(){
      this.loadingShare = true
      try {
        const r = await fetch('/api/report/enroll/channel-share')
        const j = await r.json()
        this.channelShare = (j && j.code===200) ? (j.data||[]) : []
      } finally { this.loadingShare = false }
    },
    async exportEnrollNew(){
      const url = `/api/report/export/enroll-new.csv?range=${this.range}`
      window.open(url, '_blank')
    },
    async exportChannelShare(){
      const url = '/api/report/export/channel-share.csv'
      window.open(url, '_blank')
    }
  },
  mounted(){ this.loadEnrollNew(); this.loadChannelShare() }
}
</script>

<style scoped>
</style>