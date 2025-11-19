<template>
  <div style="background:#fff; padding:16px">
    <h2>财务报表</h2>

    <h3>营收趋势</h3>
    <a-space style="margin-bottom:8px">
      <a-select v-model="revRange" style="width:160px">
        <a-select-option value="month">最近6个月</a-select-option>
        <a-select-option value="quarter">最近4个季度</a-select-option>
      </a-select>
      <a-button type="primary" @click="loadRevenue" :loading="loadingRev">加载营收趋势</a-button>
      <a-button @click="exportRevenue" :disabled="loadingRev">导出营收CSV</a-button>
    </a-space>
    <a-table :data-source="revPoints" :columns="columnsRev" :pagination="false" rowKey="_k" />

    <h3 style="margin-top:24px">课程收入占比</h3>
    <a-space style="margin-bottom:8px">
      <a-button type="primary" @click="loadCourseShare" :loading="loadingShare">加载占比</a-button>
      <a-button @click="exportCourseShare" :disabled="loadingShare">导出占比CSV</a-button>
    </a-space>
    <a-table :data-source="courseShare" :columns="columnsShare" :pagination="false" rowKey="courseId" />

    <h3 style="margin-top:24px">退费统计</h3>
    <a-space style="margin-bottom:8px">
      <V2SimpleDate v-model="rStart" :type="'date'" format="YYYY-MM-DD" placeholder="开始日期" />
      <V2SimpleDate v-model="rEnd" :type="'date'" format="YYYY-MM-DD" placeholder="结束日期" />
      <a-button type="primary" @click="loadRefund" :loading="loadingRefund">加载退费</a-button>
      <a-button @click="exportRefund" :disabled="loadingRefund">导出退费CSV</a-button>
    </a-space>
    <a-table :data-source="refundPoints" :columns="columnsRefund" :pagination="false" rowKey="month" />

    <h3 style="margin-top:24px">净利润</h3>
    <a-space style="margin-bottom:8px">
      <V2SimpleDate v-model="pStart" :type="'date'" format="YYYY-MM-DD" placeholder="开始日期" />
      <V2SimpleDate v-model="pEnd" :type="'date'" format="YYYY-MM-DD" placeholder="结束日期" />
      <a-button type="primary" @click="loadProfit" :loading="loadingProfit">计算净利润</a-button>
      <a-button @click="exportProfit" :disabled="loadingProfit">导出净利润CSV</a-button>
    </a-space>
    <a-table :data-source="[profit]" :columns="columnsProfit" :pagination="false" rowKey="_only" />
  </div>
</template>

<script>
function fmt(d){
  if(!d) return ''
  if (typeof d === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(d)) return d
  const pad=n=>String(n).padStart(2,'0'); const dt=new Date(d); return dt.getFullYear()+'-'+pad(dt.getMonth()+1)+'-'+pad(dt.getDate())
}
export default {
  name: 'ReportFinance',
  components: { V2SimpleDate: () => import('../../components/V2SimpleDate.vue') },
  data(){ return { revRange:'month', loadingRev:false, revPoints:[], loadingShare:false, courseShare:[], rStart:'', rEnd:'', loadingRefund:false, refundPoints:[], pStart:'', pEnd:'', loadingProfit:false, profit:{ _only:1, revenue:0, refunds:0, teacherFees:0, profit:0 } } },
  computed:{
    columnsRev(){ return [ { title: this.revRange==='quarter'?'季度':'月份', dataIndex: this.revRange==='quarter'?'quarter':'month', key:'k' }, { title:'金额', dataIndex:'amount', key:'amount' } ] },
    columnsShare(){ return [ { title:'课程', dataIndex:'courseName', key:'courseName' }, { title:'金额', dataIndex:'amount', key:'amount' }, { title:'占比(%)', dataIndex:'ratio', key:'ratio' } ] },
    columnsRefund(){ return [ { title:'月份', dataIndex:'month', key:'month' }, { title:'退费金额', dataIndex:'amount', key:'amount' } ] },
    columnsProfit(){ return [ { title:'总营收', dataIndex:'revenue', key:'revenue' }, { title:'退费', dataIndex:'refunds', key:'refunds' }, { title:'教师课时费', dataIndex:'teacherFees', key:'teacherFees' }, { title:'净利润', dataIndex:'profit', key:'profit' } ] }
  },
  methods:{
    async loadRevenue(){ this.loadingRev=true; try { const r=await fetch(`/api/report/finance/revenue-trend?range=${this.revRange}`); const j=await r.json(); const pts=(j&&j.code===200)?(j.data&&j.data.points||[]):[]; this.revPoints = pts.map((p,i)=>({ ...p, _k:i })) } finally { this.loadingRev=false } },
    async loadCourseShare(){ this.loadingShare=true; try { const r=await fetch('/api/report/finance/course-income-share'); const j=await r.json(); this.courseShare=(j&&j.code===200)?(j.data||[]):[] } finally { this.loadingShare=false } },
    async loadRefund(){ this.loadingRefund=true; try { const p=new URLSearchParams(); if(this.rStart) p.append('startDate', fmt(this.rStart)); if(this.rEnd) p.append('endDate', fmt(this.rEnd)); const r=await fetch('/api/report/finance/refund-stats?'+p.toString()); const j=await r.json(); const pts=(j&&j.code===200)?(j.data&&j.data.points||[]):[]; this.refundPoints=pts } finally { this.loadingRefund=false } },
    async loadProfit(){ this.loadingProfit=true; try { const p=new URLSearchParams(); if(this.pStart) p.append('startDate', fmt(this.pStart)); if(this.pEnd) p.append('endDate', fmt(this.pEnd)); const r=await fetch('/api/report/finance/net-profit?'+p.toString()); const j=await r.json(); if(j&&j.code===200){ const d=j.data||{}; this.profit={ _only:1, revenue:d.revenue||0, refunds:d.refunds||0, teacherFees:d.teacherFees||0, profit:d.profit||0 } } } finally { this.loadingProfit=false } },
    exportRevenue(){ window.open(`/api/report/export/revenue-trend.csv?range=${this.revRange}`, '_blank') },
    exportCourseShare(){ window.open('/api/report/export/course-income-share.csv', '_blank') },
    exportRefund(){ const p=new URLSearchParams(); if(this.rStart) p.append('startDate', fmt(this.rStart)); if(this.rEnd) p.append('endDate', fmt(this.rEnd)); window.open('/api/report/export/refund-stats.csv?'+p.toString(), '_blank') },
    exportProfit(){ const p=new URLSearchParams(); if(this.pStart) p.append('startDate', fmt(this.pStart)); if(this.pEnd) p.append('endDate', fmt(this.pEnd)); window.open('/api/report/export/net-profit.csv?'+p.toString(), '_blank') }
  },
  mounted(){ this.loadRevenue(); this.loadCourseShare(); this.loadRefund(); this.loadProfit() }
}
</script>

<style scoped>
</style>