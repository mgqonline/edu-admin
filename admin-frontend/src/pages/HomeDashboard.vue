<template>
  <div class="home-dashboard">
    <a-page-header title="首页" sub-title="数据驾驶舱 + 快捷操作 + 消息中心" />

    <!-- 顶部核心数据卡片 -->
    <a-row :gutter="16">
      <a-col :xl="6" :lg="8" :md="12" :xs="24">
        <a-card>
          <div class="metric-card">
            <div class="metric-title">今日续费金额</div>
            <div class="metric-value">¥ {{ metrics.todayRenewAmount.toLocaleString() }}</div>
            <div class="metric-delta" :class="{ up: metrics.deltaRenewAmount >= 0, down: metrics.deltaRenewAmount < 0 }">
              {{ metrics.deltaRenewAmount >= 0 ? '↑' : '↓' }}{{ Math.abs(metrics.deltaRenewAmount) }}%
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xl="6" :lg="8" :md="12" :xs="24">
        <a-card>
          <div class="metric-card">
            <div class="metric-title">今日续费人数</div>
            <div class="metric-value">{{ metrics.todayRenewCount }} 人</div>
            <div class="metric-delta" :class="{ up: metrics.deltaRenewCount >= 0, down: metrics.deltaRenewCount < 0 }">
              {{ metrics.deltaRenewCount >= 0 ? '↑' : '↓' }}{{ Math.abs(metrics.deltaRenewCount) }}%
            </div>
          </div>
        </a-card>
      </a-col>
       <a-col :xl="6" :lg="8" :md="12" :xs="24">
        <a-card>
          <div class="metric-card">
            <div class="metric-title">今日报名人数</div>
            <div class="metric-value">{{ metrics.todayEnrollCount }} 人</div>
            <div class="metric-delta" :class="{ up: metrics.deltaEnrollCount >= 0, down: metrics.deltaEnrollCount < 0 }">
              {{ metrics.deltaEnrollCount >= 0 ? '↑' : '↓' }}{{ Math.abs(metrics.deltaEnrollCount) }}%
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xl="6" :lg="8" :md="12" :xs="24">
        <a-card>
          <div class="metric-card">
            <div class="metric-title">班型报名分布</div>
            <div class="metric-sub">一对一：{{ pie.oneToOne }}% | 班课：{{ pie.smallClass }}%</div>
          </div>
        </a-card>
      </a-col>
     
    </a-row>

    <!-- 快捷操作区 -->
    <a-card style="margin-top: 16px">
      <a-space wrap>
        <a-button type="primary" @click="goToEnroll">新增报名</a-button>
        <a-button type="default" @click="goToRenew">新增续费</a-button>
        <a-button @click="exportToday">导出今日数据</a-button>
        <a-button @click="goToFollowRenew">待续费学员跟进</a-button>
      </a-space>
    </a-card>

    <!-- 中部趋势图表与右侧消息区 -->
    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :xl="18" :lg="16" :md="24" :xs="24">
        <a-card title="未来七天排课提醒">
          <div v-if="urgentTip" style="margin-bottom:12px">
            <a-alert type="warning" showIcon :message="'近期排课提醒'" :description="urgentTip" closable />
          </div>
          <a-list>
            <a-list-item v-for="item in upcoming" :key="item.id">
              <a-space>
                <a-tag :color="item.urgent ? 'red' : 'blue'">{{ item.urgent ? '紧急' : '提醒' }}</a-tag>
                <span>{{ (item.date || '') + ' ' + (item.start || '') + (item.end ? ('~'+item.end) : '') }}</span>
                <span>{{ item.className || ('班级#' + (item.classId || '')) }}</span>
                <span>{{ item.room || '' }}</span>
                <a-button type="link" @click="goToSchedule">查看详情</a-button>
              </a-space>
            </a-list-item>
          </a-list>
        </a-card>
        <a-card title="班型报名占比" style="margin-top: 16px">
          <div class="pie-placeholder">
            <div class="pie-item">
              <span>一对一</span>
              <a-progress :percent="pie.oneToOne" status="active" />
            </div>
            <div class="pie-item">
              <span>班课</span>
              <a-progress :percent="pie.smallClass" status="active" />
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xl="6" :lg="8" :md="24" :xs="24">
        <a-card :title="msgSidebarTitle">
          <template slot="extra">
            <a-space>
              <a-button size="small" @click="toggleSidebar">折叠</a-button>
            </a-space>
          </template>
          <a-list>
            <a-list-item v-for="item in messages" :key="item.id">
              <a-space>
                <a-tag :color="item.type === '预警' ? 'orange' : (item.type === '待办' ? 'red' : 'blue')">{{ item.type }}</a-tag>
                <span :style="{ color: item.read ? '#999' : '#333' }">{{ item.text }}</span>
                <a-button v-if="!item.read" size="small" @click="markRead(item)">已读</a-button>
              </a-space>
            </a-list-item>
          </a-list>
          <div style="margin-top:8px;text-align:right">
            <a-button size="small" @click="markAllRead">全部已读</a-button>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 底部明细列表 -->
    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :md="12" :xs="24">
        <a-card title="今日续费记录">
          <template slot="extra">
            <a-space>
              <a-button type="link" @click="viewAll('renew')">查看全部</a-button>
            </a-space>
          </template>
          <a-table :data-source="renewList" :columns="renewColumns" :pagination="false" rowKey="id" />
        </a-card>
      </a-col>
      <a-col :md="12" :xs="24">
        <a-card title="今日报名记录">
          <template slot="extra">
            <a-space>
              <a-button type="link" @click="viewAll('enroll')">查看全部</a-button>
            </a-space>
          </template>
          <a-table :data-source="enrollList" :columns="enrollColumns" :pagination="false" rowKey="id" />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script>
export default {
  name: 'HomeDashboard',
  data() {
    return {
      metrics: {
        todayRenewAmount: 0,
        deltaRenewAmount: 0,
        todayRenewCount: 0,
        deltaRenewCount: 0,
        todayEnrollCount: 0,
        deltaEnrollCount: 0
      },
      trends: [],
      upcoming: [],
      urgentTip: '',
      pie: { oneToOne: 37, smallClass: 63 },
      messages: [
        { id: 1, type: '待办', text: '15 人待续费，建议今日跟进', read: false },
        { id: 2, type: '预警', text: '学员 张三 的课程 3 天后到期', read: false },
        { id: 3, type: '通知', text: '本周排课已完成审核', read: true }
      ],
      renewList: [
        { id: 101, studentName: '李雷', classType: '一对一', amount: 4800, time: '10:05', operator: '王老师' },
        { id: 102, studentName: '韩梅', classType: '小班课', amount: 3600, time: '10:16', operator: '赵老师' }
      ],
      enrollList: [
        { id: 201, studentName: '王芳', course: '数学提高班', amount: 5200, channel: '线上', status: '待确认' },
        { id: 202, studentName: '陈强', course: '英语口语', amount: 4200, channel: '转介绍', status: '已生效' }
      ]
    }
  },
  computed: {
    scale() {
      const maxAmount = this.trends.length ? Math.max(...this.trends.map(t => t.renewAmount)) : 0
      // 简化缩放，报名人数乘以10以便可视化
      const maxEnroll = this.trends.length ? Math.max(...this.trends.map(t => t.enrollCount)) : 0
      return Math.max(maxAmount, maxEnroll * 10)
    },
    msgSidebarTitle() {
      const unread = this.messages.filter(m => !m.read).length
      return `消息中心（未读 ${unread}）`
    },
    renewColumns() {
      return [
        { title: '学员姓名', dataIndex: 'studentName' },
        { title: '班型', dataIndex: 'classType' },
        { title: '续费金额', dataIndex: 'amount', customRender: (v) => `¥${v}` },
        { title: '续费时间', dataIndex: 'time' },
        { title: '操作人', dataIndex: 'operator' },
        { title: '操作', scopedSlots: { customRender: 'actionRenew' } }
      ]
    },
    enrollColumns() {
      return [
        { title: '学员姓名', dataIndex: 'studentName' },
        { title: '报名课程', dataIndex: 'course' },
        { title: '报名金额', dataIndex: 'amount', customRender: (v) => `¥${v}` },
        { title: '报名渠道', dataIndex: 'channel' },
        { title: '状态', dataIndex: 'status' },
        { title: '操作', scopedSlots: { customRender: 'actionEnroll' } }
      ]
    }
  },
  methods: {
    goToEnroll() { this.$router.push('/student/enroll') },
    goToRenew() { this.$router.push('/finance/settlement/renew') },
    exportToday() { this.$message && this.$message.success('已生成今日数据（示例）'); },
    goToFollowRenew() { this.$message && this.$message.info('待续费跟进页面待接入'); },
    markRead(item) { item.read = true },
    markAllRead() { this.messages.forEach(m => m.read = true) },
    toggleSidebar() { this.$message && this.$message.info('右侧消息侧边栏可按需折叠/展开（示例）') },
    viewAll(type) {
      if (type === 'enroll') this.$router.push('/student/enroll')
      else this.$message && this.$message.info('续费记录完整页待接入')
    },
    goToSchedule() { this.$router.push({ path: '/course/schedule-view', query: { mode: 'upcoming', days: '7' } }) }
  },
  created() {
    (async () => {
      try {
        const token = localStorage.getItem('AUTH_TOKEN') || ''
        const res = await fetch('/api/dashboard/home', { headers: token ? { 'Authorization': 'Bearer ' + token } : undefined })
        const json = await res.json()
        if (json && json.code === 200) {
          const d = json.data || {}
          this.metrics.todayRenewAmount = Math.round(((d.todayRenewAmount || 0) + Number.EPSILON) * 100) / 100
          this.metrics.todayRenewCount = d.todayRenewCount || 0
          this.metrics.todayEnrollCount = d.todayEnrollCount || 0
          const t = d.trend7Days || {}
          const dates = Array.isArray(t.dates) ? t.dates : []
          const renewAmounts = Array.isArray(t.renewAmounts) ? t.renewAmounts : []
          const enrollCounts = Array.isArray(t.enrollCounts) ? t.enrollCounts : []
          const len = Math.min(dates.length, renewAmounts.length, enrollCounts.length)
          this.trends = dates.map((dt, i) => ({
            date: dt,
            renewAmount: Math.round((((renewAmounts[i] || 0) + Number.EPSILON) * 100)) / 100,
            enrollCount: enrollCounts[i] || 0
          }))
          if (len >= 2) {
            const todayRenew = renewAmounts[len - 1] || 0
            const yesterdayRenew = renewAmounts[len - 2] || 0
            const todayEnroll = enrollCounts[len - 1] || 0
            const yesterdayEnroll = enrollCounts[len - 2] || 0
            this.metrics.deltaRenewAmount = Math.round(((todayRenew - yesterdayRenew) / (yesterdayRenew || 1)) * 100)
            this.metrics.deltaRenewCount = Math.round(((todayEnroll - yesterdayEnroll) / (yesterdayEnroll || 1)) * 100)
            this.metrics.deltaEnrollCount = this.metrics.deltaRenewCount
          } else {
            this.metrics.deltaRenewAmount = 0
            this.metrics.deltaRenewCount = 0
            this.metrics.deltaEnrollCount = 0
          }
          const pie = d.enrollPie || {}
          const labels = Array.isArray(pie.labels) ? pie.labels : []
          const values = Array.isArray(pie.values) ? pie.values : []
          const total = values.reduce((a, b) => a + b, 0) || 1
          const idxOne = labels.indexOf('一对一')
          const idxClass = labels.indexOf('班课')
          const oneCount = idxOne >= 0 ? values[idxOne] : 0
          const classCount = idxClass >= 0 ? values[idxClass] : 0
          this.pie.oneToOne = Math.round(oneCount * 100 / total)
          this.pie.smallClass = Math.round(classCount * 100 / total)
        }
        const r2 = await fetch('/api/schedule/upcoming?days=7', { headers: token ? { 'Authorization': 'Bearer ' + token } : undefined })
        const j2 = await r2.json()
        if (j2 && j2.code === 200) {
          this.upcoming = Array.isArray(j2.data) ? j2.data : []
          const firstUrgent = this.upcoming.find(it => it.urgent)
          if (firstUrgent) {
            this.urgentTip = `${firstUrgent.date || ''} ${firstUrgent.start || ''}${firstUrgent.end ? ('~' + firstUrgent.end) : ''}  ${firstUrgent.className || ('班级#' + (firstUrgent.classId || ''))}  ${firstUrgent.room || ''}`
          }
        }
      } catch (e) {}
    })()
  }
}
</script>

<style scoped>
.home-dashboard { }
.metric-card { display:flex; flex-direction:column; }
.metric-title { color:#666; font-size:14px; }
.metric-value { font-size:24px; font-weight:bold; margin-top:4px; }
.metric-delta { margin-top:4px; font-size:14px; }
.metric-delta.up { color: #3f8600; }
.metric-delta.down { color: #cf1322; }
.chart-placeholder { padding:8px; }
.chart-line { display:flex; align-items:center; margin:6px 0; }
.chart-line .label { width:60px; color:#888; }
.chart-line .bars { flex:1; display:flex; gap:8px; align-items:center; }
.bar { height:12px; border-radius:6px; }
.bar.renew { background:#69c0ff; }
.bar.enroll { background:#95de64; }
.pie-placeholder { display:flex; flex-direction:column; gap:8px; }
.pie-item { display:flex; align-items:center; gap:12px; }
</style>