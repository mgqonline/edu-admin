<template>
  <div style="background:#fff; padding:16px">
    <h2>教学报表</h2>
    <a-space style="margin-bottom:12px">
      <V2SimpleDate v-model="startDate" :type="'date'" format="YYYY-MM-DD" placeholder="开始日期" />
      <V2SimpleDate v-model="endDate" :type="'date'" format="YYYY-MM-DD" placeholder="结束日期" />
      <a-input-number v-model="top" :min="1" :max="20" style="width:120px" placeholder="TopN" />
      <a-button type="primary" @click="loadClassRank" :loading="loadingRank">加载班级出勤排行</a-button>
      <a-button @click="exportClassRank" :disabled="loadingRank">导出排行CSV</a-button>
    </a-space>
    <a-table :data-source="classRank" :columns="columnsRank" :pagination="false" rowKey="classId" />

    <h3 style="margin-top:24px">教师课时统计</h3>
    <a-space style="margin-bottom:8px">
      <V2SimpleDate v-model="tStart" :type="'date'" format="YYYY-MM-DD" placeholder="开始日期" />
      <V2SimpleDate v-model="tEnd" :type="'date'" format="YYYY-MM-DD" placeholder="结束日期" />
      <a-button type="primary" @click="loadTeacherHours" :loading="loadingHours">加载教师课时</a-button>
      <a-button @click="exportTeacherHours" :disabled="loadingHours">导出课时CSV</a-button>
    </a-space>
    <a-table :data-source="teacherHours" :columns="columnsHours" :pagination="false" rowKey="teacherId" />
  </div>
</template>

<script>
function fmt(d){
  if (!d) return ''
  // 若已是 YYYY-MM-DD 字符串，直接返回
  if (typeof d === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(d)) return d
  const pad = n => String(n).padStart(2,'0')
  const dt = new Date(d)
  return dt.getFullYear()+'-'+pad(dt.getMonth()+1)+'-'+pad(dt.getDate())
}
export default {
  name: 'ReportTeaching',
  components: { V2SimpleDate: () => import('../../components/V2SimpleDate.vue') },
  data(){ return { startDate: '', endDate: '', top: 5, loadingRank:false, classRank: [], tStart:'', tEnd:'', loadingHours:false, teacherHours: [] } },
  computed:{
    columnsRank(){ return [
      { title:'班级', dataIndex:'className', key:'className' },
      { title:'平均出勤率(%)', dataIndex:'avgAttendanceRate', key:'avgAttendanceRate' }
    ] },
    columnsHours(){ return [
      { title:'教师ID', dataIndex:'teacherId', key:'teacherId' },
      { title:'总课时(小时)', dataIndex:'totalHours', key:'totalHours' }
    ] }
  },
  methods:{
    async loadClassRank(){
      this.loadingRank = true
      try {
        const p = new URLSearchParams()
        if (this.startDate) p.append('startDate', fmt(this.startDate))
        if (this.endDate) p.append('endDate', fmt(this.endDate))
        if (this.top) p.append('top', String(this.top))
        const r = await fetch('/api/report/teaching/class-attendance-rank?' + p.toString())
        const j = await r.json(); this.classRank = (j && j.code===200) ? (j.data||[]) : []
      } finally { this.loadingRank = false }
    },
    async loadTeacherHours(){
      this.loadingHours = true
      try {
        const p = new URLSearchParams()
        if (this.tStart) p.append('startDate', fmt(this.tStart))
        if (this.tEnd) p.append('endDate', fmt(this.tEnd))
        const r = await fetch('/api/report/teaching/teacher-hours?' + p.toString())
        const j = await r.json(); this.teacherHours = (j && j.code===200) ? (j.data||[]) : []
      } finally { this.loadingHours = false }
    },
    exportClassRank(){
      const p = new URLSearchParams()
      if (this.startDate) p.append('startDate', fmt(this.startDate))
      if (this.endDate) p.append('endDate', fmt(this.endDate))
      if (this.top) p.append('top', String(this.top))
      window.open('/api/report/export/class-attendance-rank.csv?' + p.toString(), '_blank')
    },
    exportTeacherHours(){
      const p = new URLSearchParams()
      if (this.tStart) p.append('startDate', fmt(this.tStart))
      if (this.tEnd) p.append('endDate', fmt(this.tEnd))
      window.open('/api/report/export/teacher-hours.csv?' + p.toString(), '_blank')
    }
  },
  mounted(){ this.loadClassRank(); this.loadTeacherHours() }
}
</script>

<style scoped>
</style>