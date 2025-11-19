<template>
  <div class="attendance-container">
    <a-space direction="vertical" style="width:100%">
      <!-- 课时选择器 -->
      <a-select v-model="scheduleId" style="width: 360px" @change="loadSignData" placeholder="选择课时">
        <a-select-option v-for="s in scheduleList" :key="s.id" :value="s.id">
          {{ s.className }} {{ s.date }} {{ s.startTime }}
        </a-select-option>
      </a-select>

      <!-- 操作工具栏：发起签到码、教师批量标记、一对一开始与确认、补签申请与审批 -->
      <a-space style="margin: 8px 0;">
        <a-button type="primary" @click="startSignCode">发起签到码</a-button>
        <a-button @click="teacherAllPresent">全到</a-button>
        <a-button @click="teacherAllAbsent">全缺勤</a-button>
        <a-button type="dashed" @click="o2oStart">一对一开始授课</a-button>
        <a-button type="dashed" @click="o2oStudentConfirm">学员确认</a-button>
        <a-button @click="checkO2OStatus">查看一对一状态</a-button>
        <a-divider type="vertical" />
        <a-button @click="previewPreclass" :disabled="!scheduleId">课前提醒预览</a-button>
        <a-button @click="previewAbsent" :disabled="!scheduleId">缺勤通知预览</a-button>
      </a-space>
      <a-space style="margin: 6px 0;">
        <span>状态筛选：</span>
        <a-select v-model="filterSignType" style="width: 160px" @change="applyFilter">
          <a-select-option :value="0">全部</a-select-option>
          <a-select-option :value="1">已签到</a-select-option>
          <a-select-option :value="2">迟到</a-select-option>
          <a-select-option :value="4">缺勤</a-select-option>
        </a-select>
      </a-space>
      <div v-if="lastSignCode.code" style="margin-top:4px">当前签到码：{{ lastSignCode.code }}（有效至：{{ fmtDateTime(lastSignCode.expireAt) }}）</div>
      <div v-if="o2oStatus.status" style="margin-top:4px">一对一状态：{{ o2oStatus.status }}（剩余：{{ o2oStatus.remainMs || 0 }}ms）</div>

      <!-- 学员签到列表 -->
      <a-table :data-source="studentSignList" :columns="columns" rowKey="studentId" :pagination="false">
        <template v-slot="{ text, record, column }">
          <span v-if="column.dataIndex === 'statusLabel'">
            <a-tag color="green" v-if="record.signType === 1">已签到</a-tag>
            <a-tag color="orange" v-else-if="record.signType === 2">迟到</a-tag>
            <a-tag color="red" v-else-if="record.signType === 4">缺勤</a-tag>
            <a-tag v-else>未签到</a-tag>
          </span>
          <span v-else>{{ text }}</span>
        </template>
      </a-table>

      <!-- 统计卡片 -->
      <a-row :gutter="16" style="margin-top: 20px;">
        <a-col :span="6">
          <a-statistic title="应到人数" :value="totalStudents" />
        </a-col>
        <a-col :span="6">
          <a-statistic title="实到人数" :value="arrivedStudents" />
        </a-col>
        <a-col :span="6">
          <a-statistic title="出勤率" :value="attendanceRate" suffix="%" />
        </a-col>
      </a-row>

      <!-- 通知预览弹窗 -->
      <a-modal v-model="notifyPreviewVisible" :title="notifyPreviewTitle" :footer="null">
        <div style="white-space:pre-wrap">{{ notifyPreviewContent }}</div>
      </a-modal>

      <!-- 补签申请与审批（简版） -->
      <a-divider />
      <div style="font-weight:600">补签管理</div>
      <a-space>
        <a-input v-model="makeupStudentId" style="width:160px" placeholder="学员ID" />
        <a-input v-model="makeupReason" style="width:260px" placeholder="补签原因" />
        <a-button type="primary" @click="applyMakeup">申请补签</a-button>
      </a-space>
      <a-space style="margin-top:8px">
        <a-input v-model="approveApplyId" style="width:160px" placeholder="申请ID" />
        <a-select v-model="approvePass" style="width:140px">
          <a-select-option :value="true">通过</a-select-option>
          <a-select-option :value="false">拒绝</a-select-option>
        </a-select>
        <a-button @click="approveMakeup">提交审批</a-button>
      </a-space>
    </a-space>
  </div>
</template>

<script>
export default {
  name: 'AttendancePage',
  data() {
    return {
      scheduleId: '',
      scheduleList: [
        { id: 'sch-001', className: '数学提高班', date: '2025-11-05', startTime: '19:00' },
        { id: 'sch-002', className: '英语听力班', date: '2025-11-06', startTime: '14:00' }
      ],
      rawStudentSignList: [],
      studentSignList: [],
      filterSignType: 0,
      totalStudents: 0,
      arrivedStudents: 0,
      attendanceRate: 0,
      lastSignCode: {},
      o2oStatus: {},
      makeupStudentId: '',
      makeupReason: '',
      approveApplyId: '',
      approvePass: true,
      notifyPreviewVisible: false,
      notifyPreviewTitle: '',
      notifyPreviewContent: '',
      columns: [
        { title: '学员ID', dataIndex: 'studentId' },
        { title: '姓名', dataIndex: 'name' },
        { title: '签到时间', dataIndex: 'signTimeText' },
        { title: '状态', dataIndex: 'statusLabel' }
      ]
    }
  },
  created() {
    this.loadScheduleList()
  },
  methods: {
    async loadScheduleList(){
      try {
        const r = await fetch('/api/attendance/dev/schedules')
        const j = await r.json()
        if (j && j.code===200) {
          this.scheduleList = (j.data||[]).map(s => ({ id: String(s.id), className: `班级${s.classId||''}`, date: (s.date||''), startTime: (s.startTime||'') }))
          if (this.scheduleList.length>0) { this.scheduleId = this.scheduleList[0].id; this.loadSignData(this.scheduleId) }
          return
        }
        throw new Error('load schedules failed')
      } catch(e) {
        // 保持演示数据
        this.scheduleId = this.scheduleList[0].id
        this.loadSignData(this.scheduleId)
      }
    },
    async loadSignData(id) {
      this.scheduleId = id
      // 优先尝试后端接口，失败则使用本地演示数据
      try {
        const res = await fetch(`/api/attendance/student/detail?scheduleId=${id}`)
        const json = await res.json()
        if (json && json.code === 200) {
          this.rawStudentSignList = (json.data || []).map(it => ({
            studentId: it.studentId,
            name: it.name,
            signType: it.signType,
            signTime: it.signTime,
            signTimeText: this.fmtDateTime(it.signTime),
            statusLabel: ''
          }))
          this.applyFilter()
          return
        }
        throw new Error('接口返回异常')
      } catch (e) {
        // 演示数据
        this.rawStudentSignList = [
          { studentId: 'stu-001', name: '张三', signType: 1, signTime: new Date(), signTimeText: this.fmtDateTime(new Date()), statusLabel: '' },
          { studentId: 'stu-002', name: '李四', signType: 4, signTime: null, signTimeText: '', statusLabel: '' },
          { studentId: 'stu-003', name: '王五', signType: 2, signTime: new Date(), signTimeText: this.fmtDateTime(new Date()), statusLabel: '' }
        ]
        this.applyFilter()
      }
    },
    applyFilter(){
      const list = Array.isArray(this.rawStudentSignList) ? this.rawStudentSignList.slice() : []
      const type = Number(this.filterSignType||0)
      this.studentSignList = type ? list.filter(it => Number(it.signType||0) === type) : list
      this.computeStats()
    },
    computeStats() {
      this.totalStudents = this.studentSignList.length
      this.arrivedStudents = this.studentSignList.filter(s => s.signType === 1 || s.signType === 2).length
      this.attendanceRate = this.totalStudents ? Number(((this.arrivedStudents / this.totalStudents) * 100).toFixed(1)) : 0
    },
    fmtDateTime(v){ try { const d = v && (typeof v==='string'? new Date(v) : v); return d ? (new Date(d)).toLocaleString() : '' } catch(e){ return '' } },
    async startSignCode(){
      if (!this.scheduleId) { this.$message && this.$message.warning('请选择课时'); return }
      const body = { scheduleId: Number(this.scheduleId) }
      const r = await fetch('/api/attendance/code/start', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(body) })
      const j = await r.json()
      if (j && j.code===200) { this.lastSignCode = j.data || {}; this.$message && this.$message.success('已生成签到码') } else { this.$message && this.$message.error(j.message || '生成失败') }
    },
    async teacherAllPresent(){ await this._teacherBulk('all_present') },
    async teacherAllAbsent(){ await this._teacherBulk('all_absent') },
    async _teacherBulk(op){
      if (!this.scheduleId) { this.$message && this.$message.warning('请选择课时'); return }
      const body = { scheduleId: Number(this.scheduleId), op }
      const r = await fetch('/api/attendance/teacher/mark', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(body) })
      const j = await r.json(); if (j && j.code===200) { this.$message && this.$message.success(`已标记：${(j.data||{}).affected||0}`); this.loadSignData(this.scheduleId) } else { this.$message && this.$message.error(j.message||'操作失败') }
    },
    async o2oStart(){
      if (!this.scheduleId) { this.$message && this.$message.warning('请选择课时'); return }
      const r = await fetch('/api/attendance/o2o/start', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ scheduleId: Number(this.scheduleId) }) })
      const j = await r.json(); if (j && j.code===200) { this.o2oStatus = j.data || {}; this.$message && this.$message.success('已开始一对一授课'); } else { this.$message && this.$message.error(j.message||'操作失败') }
    },
    async o2oStudentConfirm(){
      if (!this.scheduleId) { this.$message && this.$message.warning('请选择课时'); return }
      const sid = (this.studentSignList[0] && this.studentSignList[0].studentId) || this.makeupStudentId || ''
      if (!sid) { this.$message && this.$message.warning('请先加载并选择学员'); return }
      const r = await fetch('/api/attendance/o2o/student-confirm', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ scheduleId: Number(this.scheduleId), studentId: Number(sid) }) })
      const j = await r.json(); if (j && j.code===200) { this.$message && this.$message.success('学员已确认'); this.loadSignData(this.scheduleId) } else { this.$message && this.$message.error(j.message||'操作失败') }
    },
    async checkO2OStatus(){
      if (!this.scheduleId) { this.$message && this.$message.warning('请选择课时'); return }
      const r = await fetch(`/api/attendance/o2o/status?scheduleId=${this.scheduleId}`)
      const j = await r.json(); this.o2oStatus = (j && j.code===200) ? (j.data||{}) : {}; if (!(j && j.code===200)) this.$message && this.$message.error(j.message||'查询失败')
    },
    async applyMakeup(){
      if (!this.scheduleId || !this.makeupStudentId) { this.$message && this.$message.warning('请选择课时并填写学员ID'); return }
      const body = { scheduleId: Number(this.scheduleId), studentId: Number(this.makeupStudentId), reason: this.makeupReason }
      const r = await fetch('/api/attendance/makeup/apply', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(body) })
      const j = await r.json(); if (j && j.code===200) { this.$message && this.$message.success('已提交补签申请'); this.approveApplyId = (j.data||{}).applyId || '' } else { this.$message && this.$message.error(j.message||'申请失败') }
    },
    async approveMakeup(){
      if (!this.approveApplyId) { this.$message && this.$message.warning('请填写申请ID'); return }
      const body = { applyId: Number(this.approveApplyId), approved: !!this.approvePass }
      const r = await fetch('/api/attendance/makeup/approve', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(body) })
      const j = await r.json(); if (j && j.code===200) { this.$message && this.$message.success('已处理补签审批'); this.loadSignData(this.scheduleId) } else { this.$message && this.$message.error(j.message||'审批失败') }
    },
    async previewPreclass(){
      if (!this.scheduleId) { this.$message && this.$message.warning('请选择课时'); return }
      try {
        const p = new URLSearchParams({ scheduleId: this.scheduleId })
        const r = await fetch('/api/notify/preview/preclass?' + p.toString())
        const j = await r.json()
        if (j && j.code === 200) {
          const d = j.data || {}
          this.notifyPreviewTitle = d.title || '课前提醒预览'
          this.notifyPreviewContent = d.content || ''
          this.notifyPreviewVisible = true
        } else { throw new Error(j && j.message ? j.message : '接口异常') }
      } catch(e){ this.$message && this.$message.error('预览失败：' + (e && e.message ? e.message : e)) }
    },
    async previewAbsent(){
      if (!this.scheduleId) { this.$message && this.$message.warning('请选择课时'); return }
      try {
        const p = new URLSearchParams({ scheduleId: this.scheduleId })
        const r = await fetch('/api/notify/preview/absent?' + p.toString())
        const j = await r.json()
        if (j && j.code === 200) {
          const d = j.data || {}
          this.notifyPreviewTitle = d.title || '缺勤通知预览'
          this.notifyPreviewContent = d.content || ''
          this.notifyPreviewVisible = true
        } else { throw new Error(j && j.message ? j.message : '接口异常') }
      } catch(e){ this.$message && this.$message.error('预览失败：' + (e && e.message ? e.message : e)) }
    }
  }
}
</script>

<style>
.attendance-container { background: #fff; padding: 16px; }
</style>