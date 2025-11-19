<template>
  <div class="schedule-view-container">
    <div class="page-title">课表查询</div>
    <a-tabs v-model="activeTab">
      <a-tab-pane key="class" tab="班级课表">
        <a-space direction="vertical" style="width:100%">
          <a-row :gutter="12">
            <a-col :sm="8" :xs="24">
              <a-select v-model="filters.classId" style="width:100%" placeholder="选择班级">
                <a-select-option v-for="c in classOptions" :key="c.id" :value="c.id">{{ c.name || ('班级#'+c.id) }}</a-select-option>
              </a-select>
            </a-col>
            <a-col :sm="8" :xs="24">
              <V2SimpleDate v-model="filters.startDate" format="YYYY-MM-DD" style="width:100%" placeholder="开始日期" />
            </a-col>
            <a-col :sm="8" :xs="24">
              <V2SimpleDate v-model="filters.endDate" format="YYYY-MM-DD" style="width:100%" placeholder="结束日期" />
            </a-col>
          </a-row>
          <a-space>
            <a-button type="primary" @click="loadClassSchedule">查询</a-button>
            <a-button @click="exportXls">导出 Excel(xls)</a-button>
            <a-button @click="exportPdf">导出 PDF</a-button>
            <a-button @click="exportCsv">导出Excel(CSV)</a-button>
            <a-button @click="printTable">打印</a-button>
            <a-button @click="shareSchedule">分享</a-button>
          </a-space>
          <a-table :data-source="list" :columns="columns" rowKey="id" :pagination="false" />
        </a-space>
      </a-tab-pane>
      <a-tab-pane key="teacher" tab="教师课表">
        <a-space direction="vertical" style="width:100%">
          <a-row :gutter="12">
            <a-col :sm="8" :xs="24">
              <a-select v-model="filters.teacherId" style="width:100%" placeholder="选择教师">
                <a-select-option v-for="t in teacherOptions" :key="t.id" :value="t.id">{{ t.name || ('老师#'+t.id) }}</a-select-option>
              </a-select>
            </a-col>
            <a-col :sm="8" :xs="24"><V2SimpleDate v-model="filters.startDate" format="YYYY-MM-DD" style="width:100%" placeholder="开始日期" /></a-col>
            <a-col :sm="8" :xs="24"><V2SimpleDate v-model="filters.endDate" format="YYYY-MM-DD" style="width:100%" placeholder="结束日期" /></a-col>
          </a-row>
          <a-space>
            <a-button type="primary" @click="loadTeacherSchedule">查询</a-button>
            <a-button @click="exportXls">导出 Excel(xls)</a-button>
            <a-button @click="exportPdf">导出 PDF</a-button>
            <a-button @click="exportCsv">导出Excel(CSV)</a-button>
            <a-button @click="printTable">打印</a-button>
            <a-button @click="shareSchedule">分享</a-button>
          </a-space>
          <a-table :data-source="list" :columns="columns" rowKey="id" :pagination="false" />
        </a-space>
      </a-tab-pane>
      <a-tab-pane key="student" tab="学员课表">
        <a-space direction="vertical" style="width:100%">
          <a-row :gutter="12">
            <a-col :sm="8" :xs="24">
              <a-select v-model="filters.studentId" style="width:100%" placeholder="选择学员" allowClear show-search :filterOption="true">
                <a-select-option v-for="s in studentOptions" :key="s.id" :value="s.id">{{ s.name || ('学员#'+s.id) }}</a-select-option>
              </a-select>
            </a-col>
            <a-col :sm="8" :xs="24"><V2SimpleDate v-model="filters.startDate" format="YYYY-MM-DD" style="width:100%" placeholder="开始日期" /></a-col>
            <a-col :sm="8" :xs="24"><V2SimpleDate v-model="filters.endDate" format="YYYY-MM-DD" style="width:100%" placeholder="结束日期" /></a-col>
          </a-row>
          <a-space>
            <a-button type="primary" @click="loadStudentSchedule">查询</a-button>
            <a-button @click="exportXls">导出 Excel(xls)</a-button>
            <a-button @click="exportPdf">导出 PDF</a-button>
            <a-button @click="exportCsv">导出Excel(CSV)</a-button>
            <a-button @click="printTable">打印</a-button>
            <a-button @click="shareSchedule">分享</a-button>
          </a-space>
          <a-table :data-source="list" :columns="columns" rowKey="id" :pagination="false" />
        </a-space>
      </a-tab-pane>
      <a-tab-pane key="room" tab="教室课表">
        <a-space direction="vertical" style="width:100%">
          <a-row :gutter="12">
            <a-col :sm="8" :xs="24">
              <a-select v-model="filters.campusId" style="width:100%" placeholder="选择校区" allowClear show-search :filterOption="true" @change="onCampusChange">
                <a-select-option v-for="c in campusOptions" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
              </a-select>
            </a-col>
            <a-col :sm="8" :xs="24">
              <a-select v-model="filters.room" style="width:100%" placeholder="选择教室" allowClear show-search :filterOption="true">
                <a-select-option v-for="r in roomOptions" :key="r.id || r.name" :value="r.name">{{ r.name }}<template v-if="r.usableSeats">（可用座位：{{ r.usableSeats }}）</template></a-select-option>
              </a-select>
            </a-col>
            <a-col :sm="8" :xs="24"><V2SimpleDate v-model="filters.startDate" format="YYYY-MM-DD" style="width:100%" placeholder="开始日期" /></a-col>
            <a-col :sm="8" :xs="24"><V2SimpleDate v-model="filters.endDate" format="YYYY-MM-DD" style="width:100%" placeholder="结束日期" /></a-col>
          </a-row>
          <a-space>
            <a-button type="primary" @click="loadRoomSchedule">查询</a-button>
            <a-button @click="exportXls">导出 Excel(xls)</a-button>
            <a-button @click="exportPdf">导出 PDF</a-button>
            <a-button @click="exportCsv">导出Excel(CSV)</a-button>
            <a-button @click="printTable">打印</a-button>
            <a-button @click="shareSchedule">分享</a-button>
          </a-space>
          <a-table :data-source="list" :columns="columns" rowKey="id" :pagination="false" />
        </a-space>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script>
export default {
  name: 'ScheduleView',
  components: { V2SimpleDate: () => import('../../components/V2SimpleDate.vue') },
  data() {
    return {
      activeTab: 'class',
      list: [],
      columns: [
        { title: '日期', dataIndex: 'date' },
        { title: '开始', dataIndex: 'start' },
        { title: '结束', dataIndex: 'end' },
        { title: '班级', dataIndex: 'classId' },
        { title: '教师', dataIndex: 'teacherId' },
        { title: '教室', dataIndex: 'room' },
        { title: '状态', dataIndex: 'status' }
      ],
      filters: {
        classId: null,
        teacherId: null,
        studentId: '',
        campusId: null,
        room: '',
        startDate: null,
        endDate: null
      },
      classOptions: [],
      teacherOptions: [],
      studentOptions: [],
      campusOptions: [],
      roomOptions: []
    }
  },
  created() {
    this.loadClassOptions(); this.loadTeacherOptions(); this.loadStudentOptions(); this.loadCampusOptions(); this.applyRouteFilters();
  },
  methods: {
    noop() {},
    async applyRouteFilters() {
      try {
        const q = (this.$route && this.$route.query) || {}
        if (q.mode === 'upcoming') {
          let days = Number.parseInt(q.days || '0', 10)
          if (!days || Number.isNaN(days)) {
            const sd = q.startDate, ed = q.endDate
            if (sd && ed) {
              const start = new Date(sd)
              const end = new Date(ed)
              const diff = Math.round((end - start) / (24*3600*1000)) + 1
              days = diff > 0 ? diff : 3
            } else {
              days = 3
            }
          }
          await this.loadUpcoming(days)
          return
        }
        const dim = q.dim
        const sd = q.startDate
        const ed = q.endDate
        if (sd) this.filters.startDate = sd
        if (ed) this.filters.endDate = ed
        if (dim === 'class' && q.classId) {
          this.activeTab = 'class'
          this.filters.classId = Number(q.classId)
          await this.loadClassSchedule()
          return
        }
        if (dim === 'teacher' && q.teacherId) {
          this.activeTab = 'teacher'
          this.filters.teacherId = Number(q.teacherId)
          await this.loadTeacherSchedule()
          return
        }
        if (dim === 'student' && q.studentId) {
          this.activeTab = 'student'
          this.filters.studentId = String(q.studentId)
          await this.loadStudentSchedule()
          return
        }
        if (dim === 'room' && q.campusId && q.room) {
          this.activeTab = 'room'
          this.filters.campusId = Number(q.campusId)
          this.filters.room = String(q.room)
          await this.loadRoomSchedule()
          return
        }
      } catch (e) { /* ignore */ }
    },
    async loadUpcoming(days = 3) {
      try {
        const p = new URLSearchParams({ days: String(days), limit: '100' })
        const r = await fetch('/api/schedule/upcoming?' + p.toString())
        const j = await r.json()
        if (j && j.code === 200) {
          this.list = Array.isArray(j.data) ? j.data : []
          this.activeTab = 'class'
        }
      } catch (e) { /* ignore */ }
    },
    async loadClassOptions() {
      try { const r = await fetch('/api/class/list'); const j = await r.json(); if (j && j.code === 200) this.classOptions = (j.data||[]).map(it=>({id:it.id,name:it.name})) } catch(e){}
    },
    async loadTeacherOptions() {
      try { const r = await fetch('/api/teacher/list'); const j = await r.json(); if (j && j.code === 200) this.teacherOptions = (j.data||[]).map(it=>({id:it.id,name:it.name})) } catch(e){}
    },
    async loadStudentOptions() {
      try { const r = await fetch('/api/student/list'); const j = await r.json(); if (j && j.code === 200) this.studentOptions = (j.data||[]).map(it=>({id:it.id || it.studentId || it.uid, name: it.name || it.realName || it.nickname})) } catch(e){}
    },
    async loadCampusOptions() {
      try { const r = await fetch('/api/base/campus/list'); const j = await r.json(); if (j && j.code === 200) this.campusOptions = (j.data||[]); } catch(e){}
    },
    async loadRoomOptions(campusId) {
      if (!campusId) { this.roomOptions = []; return }
      try { const r = await fetch('/api/base/classroom/list?campusId=' + encodeURIComponent(campusId)); const j = await r.json(); if (j && j.code === 200) this.roomOptions = (j.data||[]).map(it=>({id:it.id || it.roomId || it.roomCode, name: it.name || it.roomName || it.code, usableSeats: it.usableSeats, campusId: it.campusId})) } catch(e){}
    },
    onCampusChange(v) { this.filters.campusId = v; this.filters.room = ''; this.loadRoomOptions(v) },
    fmtDate(d) { if (!d) return ''; if (typeof d === 'string') return d; try { return (d && d.toISOString) ? d.toISOString().slice(0,10) : '' } catch(e){ return '' } },
    async loadClassSchedule() {
      if (!this.filters.classId) { this.$message.warning('请选择班级'); return }
      const p = new URLSearchParams({ classId: this.filters.classId })
      const sd = this.fmtDate(this.filters.startDate), ed = this.fmtDate(this.filters.endDate)
      if (sd && ed) { p.append('startDate', sd); p.append('endDate', ed) }
      const r = await fetch('/api/schedule/query/class?' + p.toString()); const j = await r.json(); this.list = (j && j.code===200) ? (j.data||[]) : []
    },
    async loadTeacherSchedule() {
      if (!this.filters.teacherId) { this.$message.warning('请选择教师'); return }
      const p = new URLSearchParams({ teacherId: this.filters.teacherId })
      const sd = this.fmtDate(this.filters.startDate), ed = this.fmtDate(this.filters.endDate)
      if (sd && ed) { p.append('startDate', sd); p.append('endDate', ed) }
      const r = await fetch('/api/schedule/query/teacher?' + p.toString()); const j = await r.json(); this.list = (j && j.code===200) ? (j.data||[]) : []
    },
    async loadStudentSchedule() {
      if (!this.filters.studentId) { this.$message.warning('请选择学员'); return }
      const p = new URLSearchParams({ studentId: this.filters.studentId })
      const sd = this.fmtDate(this.filters.startDate), ed = this.fmtDate(this.filters.endDate)
      if (sd && ed) { p.append('startDate', sd); p.append('endDate', ed) }
      const r = await fetch('/api/schedule/query/student?' + p.toString()); const j = await r.json(); this.list = (j && j.code===200) ? (j.data||[]) : []
    },
    async loadRoomSchedule() {
      if (!this.filters.campusId || !this.filters.room) { this.$message.warning('请选择校区与教室'); return }
      const p = new URLSearchParams({ campusId: this.filters.campusId, room: this.filters.room })
      const sd = this.fmtDate(this.filters.startDate), ed = this.fmtDate(this.filters.endDate)
      if (sd && ed) { p.append('startDate', sd); p.append('endDate', ed) }
      const r = await fetch('/api/schedule/query/room?' + p.toString()); const j = await r.json(); this.list = (j && j.code===200) ? (j.data||[]) : []
    },
    buildExportTitle() {
      const dimMap = { class: '班级', teacher: '教师', student: '学员', room: '教室' }
      const range = (this.fmtDate(this.filters.startDate) || '') + (this.filters.endDate ? ('~' + this.fmtDate(this.filters.endDate)) : '')
      const idText = this.activeTab === 'class' ? this.filters.classId
        : this.activeTab === 'teacher' ? this.filters.teacherId
        : this.activeTab === 'student' ? this.filters.studentId
        : (this.filters.campusId + '#' + this.filters.room)
      return `课表_${dimMap[this.activeTab] || this.activeTab}_${idText || '全部'}_${range || '全部'}`
    },
    buildTableHtml() {
      const header = this.columns.map(c => `<th>${c.title}</th>`).join('')
      const rows = this.list.map(r => `<tr>${this.columns.map(c => `<td>${r[c.dataIndex] !== undefined && r[c.dataIndex] !== null ? String(r[c.dataIndex]) : ''}</td>`).join('')}</tr>`).join('')
      const style = '<style>table{border-collapse:collapse;width:100%}th,td{border:1px solid #ccc;padding:8px;text-align:left}</style>'
      return `<!DOCTYPE html><html><head><meta charset="UTF-8">${style}</head><body><table><thead><tr>${header}</tr></thead><tbody>${rows}</tbody></table></body></html>`
    },
    exportXls() {
      if (!this.list.length) { this.$message.info('暂无数据可导出'); return }
      const html = this.buildTableHtml()
      const blob = new Blob([html], { type: 'application/vnd.ms-excel' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a'); a.href = url; a.download = this.buildExportTitle() + '.xls'; a.click(); URL.revokeObjectURL(url)
    },
    exportCsv() {
      if (!this.list.length) { this.$message.info('暂无数据可导出'); return }
      const cols = this.columns.map(c => c.dataIndex)
      const header = cols.join(',')
      const rows = this.list.map(r => cols.map(k => (r[k]!==undefined && r[k]!==null) ? String(r[k]).replace(/,/g,';') : '').join(','))
      const csv = [header, ...rows].join('\n')
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a'); a.href = url; a.download = '课表导出.csv'; a.click(); URL.revokeObjectURL(url)
    },
    exportPdf() {
      if (!this.list.length) { this.$message.info('暂无数据可导出'); return }
      // 打开独立打印窗口，用户可选择“保存为 PDF”
      const html = this.buildTableHtml()
      const w = window.open('', '_blank')
      if (!w) { this.$message.error('无法打开打印窗口'); return }
      w.document.open(); w.document.write(`<html><head><title>${this.buildExportTitle()}</title></head><body>${html}</body></html>`); w.document.close()
      w.focus(); w.print();
    },
    printTable() { window.print() },
    async shareSchedule() {
      const payload = { dimension: this.activeTab, targetId: this.activeTab==='class'?this.filters.classId: this.activeTab==='teacher'?this.filters.teacherId: this.activeTab==='student'?this.filters.studentId: (this.filters.campusId+'#'+this.filters.room), range: (this.fmtDate(this.filters.startDate)||'') + '~' + (this.fmtDate(this.filters.endDate)||'') }
      const r = await fetch('/api/schedule/share', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(payload) })
      const j = await r.json(); if (j && j.code===200) this.$message.success('分享已入队'); else this.$message.error('分享失败')
    }
  },
  watch: {
    '$route'(to, from) { this.applyRouteFilters() }
  }
}
</script>

<style>
.schedule-view-container { background:#fff; padding:16px; }
.page-title { font-size:18px; font-weight:600; margin-bottom:12px; }
@media print {
  .schedule-view-container .ant-space, .schedule-view-container .ant-row { display: none; }
  .schedule-view-container .ant-table { display: block; }
}
</style>