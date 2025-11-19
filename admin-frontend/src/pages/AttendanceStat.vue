<template>
  <div class="attendance-stat-container">
    <div class="page-title">签到与考勤统计</div>
    <a-tabs v-model="activeTab">
      <a-tab-pane key="student" tab="学员统计">
        <a-space direction="vertical" style="width:100%">
          <a-row :gutter="12">
            <a-col :sm="8" :xs="24"><a-input v-model="studentId" placeholder="学员ID" /></a-col>
            <a-col :sm="8" :xs="24"><CompatDatePicker v-model="startDate" style="width:100%" placeholder="开始日期" /></a-col>
            <a-col :sm="8" :xs="24"><CompatDatePicker v-model="endDate" style="width:100%" placeholder="结束日期" /></a-col>
          </a-row>
          <a-space>
            <a-button type="primary" @click="loadStudentStats">查询</a-button>
          </a-space>
          <a-row :gutter="16" style="margin-top: 12px;">
            <a-col :span="6"><a-statistic title="总课时" :value="studentStats.totalHours || 0" /></a-col>
            <a-col :span="6"><a-statistic title="完成课时" :value="studentStats.completedHours || 0" /></a-col>
            <a-col :span="6"><a-statistic title="缺勤次数" :value="studentStats.absent || 0" /></a-col>
            <a-col :span="6"><a-statistic title="迟到次数" :value="studentStats.late || 0" /></a-col>
          </a-row>
          <a-row :gutter="16" style="margin-top: 12px;">
            <a-col :span="12"><a-statistic title="报名课时" :value="studentStats.enrolledHours || 0" /></a-col>
            <a-col :span="12"><a-statistic title="剩余课时" :value="studentStats.remainingHours || 0" /></a-col>
          </a-row>
        </a-space>
      </a-tab-pane>
      <a-tab-pane key="class" tab="班级统计">
        <a-space direction="vertical" style="width:100%">
          <a-row :gutter="12">
            <a-col :sm="12" :xs="24"><a-input v-model="classId" placeholder="班级ID" /></a-col>
            <a-col :sm="12" :xs="24"><a-input v-model="month" placeholder="月份(YYYY-MM)" /></a-col>
          </a-row>
          <a-button type="primary" @click="loadClassStats">查询</a-button>
          <a-row :gutter="16" style="margin-top: 12px;">
            <a-col :span="8"><a-statistic title="平均出勤率" :value="classStats.avgAttendanceRate || 0" suffix="%" /></a-col>
            <a-col :span="16">
              <div>缺勤TOP3：{{ (classStats.absentTop3 || []).join('，') }}</div>
            </a-col>
          </a-row>
          <a-divider />
          <a-row :gutter="12">
            <a-col :sm="8" :xs="24">
              <a-select v-model="classTrendRange" style="width:100%">
                <a-select-option value="week">周度趋势(最近8周)</a-select-option>
                <a-select-option value="month">月度趋势(当月)</a-select-option>
              </a-select>
            </a-col>
            <a-col :sm="8" :xs="24" v-if="classTrendRange==='month'">
              <a-input v-model="month" placeholder="月份(YYYY-MM)" />
            </a-col>
            <a-col :sm="8" :xs="24"><a-button @click="loadClassTrend">加载趋势</a-button></a-col>
          </a-row>
          <a-table :data-source="classTrendPoints" :columns="classTrendColumns" rowKey="key" :pagination="false" style="margin-top:8px" />
        </a-space>
      </a-tab-pane>
      <a-tab-pane key="teacher" tab="教师统计">
        <a-space direction="vertical" style="width:100%">
          <a-row :gutter="12">
            <a-col :sm="8" :xs="24"><a-input v-model="teacherId" placeholder="教师ID" /></a-col>
            <a-col :sm="8" :xs="24"><CompatDatePicker v-model="startDateT" style="width:100%" placeholder="开始日期" /></a-col>
            <a-col :sm="8" :xs="24"><CompatDatePicker v-model="endDateT" style="width:100%" placeholder="结束日期" /></a-col>
          </a-row>
          <a-button type="primary" @click="loadTeacherStats">查询</a-button>
          <a-row :gutter="16" style="margin-top: 12px;">
            <a-col :span="8"><a-statistic title="整体出勤率" :value="teacherStats.overallAttendanceRate || 0" suffix="%" /></a-col>
            <a-col :span="8"><a-statistic title="签到发起及时率" :value="teacherStats.signStartTimelyRate || 0" suffix="%" /></a-col>
            <a-col :span="8"><a-statistic title="平均发起延迟" :value="teacherStats.signStartAvgDelayMinutes || 0" suffix="分钟" /></a-col>
          </a-row>
        </a-space>
      </a-tab-pane>
      <a-tab-pane key="overview" tab="周报概览">
        <a-space direction="vertical" style="width:100%">
          <a-button @click="loadWeekOverview">加载周出勤概览</a-button>
          <div style="margin-top:8px">周范围：{{ weekReport.week || '-' }}；平均出勤率：{{ weekReport.avgRate || '-' }}%</div>
          <a-space style="margin-top:8px">
            <a-button @click="previewWeeklyNotify">周报通知预览</a-button>
          </a-space>
        </a-space>
      </a-tab-pane>
    </a-tabs>
    <a-modal v-model="weeklyPreviewVisible" :title="weeklyPreviewTitle" :footer="null">
      <div style="white-space:pre-wrap">{{ weeklyPreviewContent }}</div>
    </a-modal>
  </div>
</template>

<script>
import moment from 'moment'
export default {
  name: 'AttendanceStat',
  components: { CompatDatePicker: () => import('../components/CompatDatePicker.vue') },
  data() {
    return {
      activeTab: 'student',
      studentId: '', startDate: null, endDate: null, studentStats: {},
      classId: '', month: '', classStats: {},
      classTrendRange: 'week', classTrendPoints: [], classTrendColumns: [
        { title: '日期/周', dataIndex: 'label' },
        { title: '出勤率(%)', dataIndex: 'rate' }
      ],
      teacherId: '', startDateT: null, endDateT: null, teacherStats: {},
      weekReport: {},
      weeklyPreviewVisible: false,
      weeklyPreviewTitle: '',
      weeklyPreviewContent: ''
    }
  },
  methods: {
    noop() {},
    fmtDate(d) { if (!d) return ''; if (typeof d === 'string') return d; try { return d.format ? d.format('YYYY-MM-DD') : moment(d).format('YYYY-MM-DD') } catch(e){ return '' } },
    async loadStudentStats() {
      if (!this.studentId) { this.$message.warning('请输入学员ID'); return }
      const p = new URLSearchParams({ studentId: this.studentId, startDate: this.fmtDate(this.startDate), endDate: this.fmtDate(this.endDate) })
      const r = await fetch('/api/attendance/student/stats?' + p.toString()); const j = await r.json(); this.studentStats = (j && j.code===200) ? (j.data||{}) : {}
    },
    async loadClassStats() {
      if (!this.classId || !this.month) { this.$message.warning('请输入班级ID与月份'); return }
      const p = new URLSearchParams({ classId: this.classId, month: this.month })
      const r = await fetch('/api/attendance/class/stats?' + p.toString()); const j = await r.json(); this.classStats = (j && j.code===200) ? (j.data||{}) : {}
    },
    async loadClassTrend(){
      if (!this.classId) { this.$message.warning('请输入班级ID'); return }
      const p = new URLSearchParams({ classId: this.classId, range: this.classTrendRange, month: this.month })
      const r = await fetch('/api/attendance/class/trend?' + p.toString()); const j = await r.json(); const d = (j && j.code===200) ? (j.data||{}) : {}
      const pts = Array.isArray(d.points) ? d.points : []
      this.classTrendPoints = pts.map((it, idx) => ({ key: idx, label: (it.date||it.week||'-'), rate: it.rate||0 }))
    },
    async loadTeacherStats() {
      if (!this.teacherId) { this.$message.warning('请输入教师ID'); return }
      const p = new URLSearchParams({ teacherId: this.teacherId, startDate: this.fmtDate(this.startDateT), endDate: this.fmtDate(this.endDateT) })
      const r = await fetch('/api/attendance/teacher/stats?' + p.toString()); const j = await r.json(); this.teacherStats = (j && j.code===200) ? (j.data||{}) : {}
    },
    async loadWeekOverview() {
      const r = await fetch('/api/report/attendance/week'); const j = await r.json(); this.weekReport = (j && j.code===200) ? (j.data||{}) : {}
    },
    async previewWeeklyNotify() {
      try {
        const p = new URLSearchParams({})
        const r = await fetch('/api/notify/preview/weekly?' + p.toString())
        const j = await r.json()
        const d = (j && j.code===200) ? (j.data||{}) : {}
        this.weeklyPreviewTitle = d.title || '周报通知预览'
        this.weeklyPreviewContent = d.content || ''
        this.weeklyPreviewVisible = true
      } catch(e){ this.$message && this.$message.error('预览失败') }
    }
  }
}
</script>

<style>
.attendance-stat-container { background:#fff; padding:16px; }
.page-title { font-size:18px; font-weight:600; margin-bottom:12px; }
</style>