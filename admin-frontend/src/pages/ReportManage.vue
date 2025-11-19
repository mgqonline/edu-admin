<template>
  <div class="report-manage">
    <a-page-header title="报表管理" sub-title="教学与财务统计总览" />

    <a-row :gutter="16">
      <a-col :span="12">
        <a-card bordered title="招生趋势（最近6个月）" :loading="loading.enroll">
          <a-table :data-source="enroll.points" :columns="enrollColumns" rowKey="month" size="small" :pagination="false" />
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card bordered title="渠道占比" :loading="loading.channel">
          <a-table :data-source="channelShare" :columns="channelColumns" rowKey="channel" size="small" :pagination="false" />
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-top:16px">
      <a-col :span="12">
        <a-card bordered title="班级出勤率 Top5" :loading="loading.classRank">
          <a-table :data-source="classRank" :columns="classRankColumns" rowKey="classId" size="small" :pagination="false" />
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card bordered title="教师课时统计（时间范围可选）" :loading="loading.teacherHours">
          <a-space style="margin-bottom:8px">
            <CompatDatePicker v-model="filters.startDate" placeholder="开始日期" />
            <CompatDatePicker v-model="filters.endDate" placeholder="结束日期" />
            <a-button size="small" @click="loadTeacherHours">刷新</a-button>
          </a-space>
          <a-table :data-source="teacherHours" :columns="teacherColumns" rowKey="teacherId" size="small" :pagination="false" />
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-top:16px">
      <a-col :span="12">
        <a-card bordered title="营收趋势（最近6个月）" :loading="loading.revenue">
          <a-table :data-source="revenue.points" :columns="revenueColumns" rowKey="month" size="small" :pagination="false" />
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card bordered title="净利润（区间）" :loading="loading.netProfit">
          <a-space style="margin-bottom:8px">
            <CompatDatePicker v-model="filters.pStart" placeholder="开始日期" />
            <CompatDatePicker v-model="filters.pEnd" placeholder="结束日期" />
            <a-button size="small" type="primary" @click="loadNetProfit">计算</a-button>
          </a-space>
          <a-descriptions bordered size="small" :column="1">
            <a-descriptions-item label="总营收">￥{{fmt(netProfit.revenue)}}</a-descriptions-item>
            <a-descriptions-item label="退费">￥{{fmt(netProfit.refunds)}}</a-descriptions-item>
            <a-descriptions-item label="教师课时费">￥{{fmt(netProfit.teacherFees)}}</a-descriptions-item>
            <a-descriptions-item label="净利润">￥{{fmt(netProfit.netProfit)}}</a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-top:16px">
      <a-col :span="12">
        <a-card bordered title="课程收入占比" :loading="loading.courseShare">
          <a-table :data-source="courseShare" :columns="courseShareColumns" rowKey="course" size="small" :pagination="false" />
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card bordered title="退费统计" :loading="loading.refundStats">
          <a-space style="margin-bottom:8px">
            <CompatDatePicker v-model="refundFilters.startDate" placeholder="开始日期" />
            <CompatDatePicker v-model="refundFilters.endDate" placeholder="结束日期" />
            <a-button size="small" @click="loadRefundStats">刷新</a-button>
          </a-space>
          <a-table :data-source="refundStats.points" :columns="refundStatsColumns" rowKey="date" size="small" :pagination="false" />
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-top:16px">
      <a-col :span="24">
        <a-card bordered title="试听转化趋势（最近6个月）" :loading="loading.trialConv">
          <a-table :data-source="trialConv.points" :columns="trialConvColumns" rowKey="month" size="small" :pagination="false" />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script>
export default {
  name: 'ReportManage',
  components: { CompatDatePicker: () => import('../components/CompatDatePicker.vue') },
  data() {
    return {
      loading: { enroll: false, channel: false, classRank: false, teacherHours: false, revenue: false, netProfit: false, courseShare: false, refundStats: false, trialConv: false },
      enroll: { points: [] },
      channelShare: [],
      classRank: [],
      teacherHours: [],
      revenue: { points: [] },
      netProfit: { revenue: 0, refunds: 0, teacherFees: 0, netProfit: 0 },
      courseShare: [],
      refundStats: { points: [] },
      trialConv: { points: [] },
      refundFilters: { startDate: '', endDate: '' },
      filters: { startDate: '', endDate: '', pStart: '', pEnd: '' },
      enrollColumns: [ { title: '月份', dataIndex: 'month', key: 'month' }, { title: '新增报名', dataIndex: 'count', key: 'count' } ],
      channelColumns: [ { title: '渠道', dataIndex: 'channel', key: 'channel' }, { title: '人数', dataIndex: 'count', key: 'count' }, { title: '占比(%)', dataIndex: 'ratio', key: 'ratio' } ],
      classRankColumns: [ { title: '班级', dataIndex: 'className', key: 'className' }, { title: '平均出勤率(%)', dataIndex: 'avgAttendanceRate', key: 'avgAttendanceRate' } ],
      teacherColumns: [ { title: '教师ID', dataIndex: 'teacherId', key: 'teacherId' }, { title: '总课时', dataIndex: 'totalHours', key: 'totalHours' } ],
      revenueColumns: [ { title: '月份', dataIndex: 'month', key: 'month' }, { title: '金额(￥)', dataIndex: 'amount', key: 'amount' } ],
      courseShareColumns: [ { title: '课程', dataIndex: 'course', key: 'course' }, { title: '收入(￥)', dataIndex: 'amount', key: 'amount' }, { title: '占比(%)', dataIndex: 'ratio', key: 'ratio' } ],
      refundStatsColumns: [ { title: '日期', dataIndex: 'date', key: 'date' }, { title: '退费金额(￥)', dataIndex: 'amount', key: 'amount' }, { title: '笔数', dataIndex: 'count', key: 'count' } ],
      trialConvColumns: [ { title: '月份', dataIndex: 'month', key: 'month' }, { title: '完成数', dataIndex: 'finished', key: 'finished' }, { title: '转化数', dataIndex: 'converted', key: 'converted' }, { title: '转化率(%)', dataIndex: 'rate', key: 'rate' } ]
    }
  },
  methods: {
    fmt(n) { const x = Number(n||0); return x.toFixed(2) },
    async loadEnroll() {
      this.loading.enroll = true
      try { const r = await fetch('/api/report/enroll/new?range=month'); const j = await r.json(); this.enroll = j.data || { points: [] } } finally { this.loading.enroll = false }
    },
    async loadChannel() {
      this.loading.channel = true
      try { const r = await fetch('/api/report/enroll/channel-share'); const j = await r.json(); this.channelShare = j.data || [] } finally { this.loading.channel = false }
    },
    async loadClassRank() {
      this.loading.classRank = true
      try { const r = await fetch('/api/report/teaching/class-attendance-rank?top=5'); const j = await r.json(); this.classRank = j.data || [] } finally { this.loading.classRank = false }
    },
    async loadTeacherHours() {
      this.loading.teacherHours = true
      try {
        const qs = new URLSearchParams()
        if (this.filters.startDate) qs.append('startDate', this.filters.startDate)
        if (this.filters.endDate) qs.append('endDate', this.filters.endDate)
        const r = await fetch('/api/report/teaching/teacher-hours' + (qs.toString()?`?${qs.toString()}`:''))
        const j = await r.json(); this.teacherHours = j.data || []
      } finally { this.loading.teacherHours = false }
    },
    async loadRevenue() {
      this.loading.revenue = true
      try { const r = await fetch('/api/report/finance/revenue-trend?range=month'); const j = await r.json(); this.revenue = j.data || { points: [] } } finally { this.loading.revenue = false }
    },
    async loadNetProfit() {
      this.loading.netProfit = true
      try {
        const qs = new URLSearchParams()
        if (this.filters.pStart) qs.append('startDate', this.filters.pStart)
        if (this.filters.pEnd) qs.append('endDate', this.filters.pEnd)
        const r = await fetch('/api/report/finance/net-profit' + (qs.toString()?`?${qs.toString()}`:''))
        const j = await r.json(); this.netProfit = j.data || { revenue:0, refunds:0, teacherFees:0, netProfit:0 }
      } finally { this.loading.netProfit = false }
    }
    ,async loadCourseShare() {
      this.loading.courseShare = true
      try {
        const r = await fetch('/api/report/finance/course-income-share')
        const j = await r.json(); this.courseShare = j.data || []
      } finally { this.loading.courseShare = false }
    }
    ,async loadRefundStats() {
      this.loading.refundStats = true
      try {
        const qs = new URLSearchParams()
        if (this.refundFilters.startDate) qs.append('startDate', this.refundFilters.startDate)
        if (this.refundFilters.endDate) qs.append('endDate', this.refundFilters.endDate)
        const r = await fetch('/api/report/finance/refund-stats' + (qs.toString()?`?${qs.toString()}`:''))
        const j = await r.json(); this.refundStats = j.data || { points: [] }
      } finally { this.loading.refundStats = false }
    }
    ,async loadTrialConv() {
      this.loading.trialConv = true
      try {
        const r = await fetch('/api/report/student/trial-conversion-trend')
        const j = await r.json(); this.trialConv = j.data || { points: [] }
      } finally { this.loading.trialConv = false }
    }
  },
  mounted() {
    this.loadEnroll(); this.loadChannel(); this.loadClassRank(); this.loadTeacherHours(); this.loadRevenue(); this.loadNetProfit();
    this.loadCourseShare(); this.loadRefundStats(); this.loadTrialConv();
  }
}
</script>

<style scoped>
.report-manage {}
</style>