<template>
  <div style="background:#fff; padding:16px">
    <h3>学员选班申请</h3>
    <a-space style="margin-bottom:12px" wrap>
      <a-input v-model="filters.keyword" placeholder="按班级名称关键字" style="width:220px" />
      <a-input v-model="filters.courseId" placeholder="课程ID（可选）" style="width:180px" />
      <a-select v-model="filters.teacherId" placeholder="教师（可选）" style="width:220px" allowClear>
        <a-select-option v-for="t in teacherOptions" :key="t.id" :value="t.id">{{ t.name || t.title || ('#'+t.id) }}</a-select-option>
      </a-select>
      <a-select v-model="filters.campusId" placeholder="校区（可选）" style="width:200px" allowClear>
        <a-select-option v-for="c in campusOptions" :key="c.id" :value="c.id">{{ c.title || c.name || ('#'+c.id) }}</a-select-option>
      </a-select>
      <a-select v-model="filters.status" placeholder="状态筛选" style="width:160px" allowClear>
        <a-select-option value="not_started">未开课</a-select-option>
        <a-select-option value="ongoing">进行中</a-select-option>
        <a-select-option value="ended">已结课</a-select-option>
        <a-select-option value="suspended">临时停课</a-select-option>
      </a-select>
    </a-space>
    <a-space style="margin:8px 0 12px" wrap>
      <a-input v-model="studentId" placeholder="学员ID" style="width:160px" />
      <a-button @click="loadRecords">查看分班记录</a-button>
       <a-button type="primary" @click="loadClasses">查询班级</a-button>
      <a-button @click="resetFilters">清空筛选</a-button>
    </a-space>

    <a-table :columns="columns" :data-source="classes" rowKey="id" :pagination="pagination" @change="onTableChange">
      <span slot="apply" slot-scope="t, r">
        <a-space>
          <a-button type="link" @click="applySelect(r)">申请选班</a-button>
          <a-button type="link" @click="openApps(r)">查看该班申请</a-button>
        </a-space>
      </span>
    </a-table>

    <a-divider />
    <h4>学员分班记录</h4>
    <a-table :columns="recordColumns" :data-source="records" rowKey="time" :pagination="false" />

    <a-modal :visible="appsVisible" title="班级选班申请" @cancel="appsVisible=false" footer="null">
      <a-table :columns="appsColumns" :data-source="apps" rowKey="id" :pagination="false" />
    </a-modal>

    <a-modal :visible="applyVisible" title="提交选班申请" @ok="doApplySelect" @cancel="closeApply">
      <a-form layout="vertical">
        <a-form-item label="确认课程ID" :validate-status="applyErrors.courseId && 'error'" :help="applyErrors.courseId">
          <a-input v-model="applyForm.confirmCourseId" placeholder="请输入当前班级的课程ID以确认" />
        </a-form-item>
        <a-form-item label="备注（可选）">
          <a-input v-model="applyForm.note" placeholder="可填写备注信息" />
        </a-form-item>
        <a-alert type="info" show-icon message="当前班级课程ID：" :description="String(applyClass && applyClass.courseId || '')" />
      </a-form>
    </a-modal>
  </div>
</template>

<script>
export default {
  name: 'StudentClassSelect',
  data() {
    return {
      filters: { keyword: '', courseId: '', teacherId: '', campusId: '', status: '' },
      teacherOptions: [],
      campusOptions: [],
      classes: [],
      pagination: { current: 1, pageSize: 10, total: 0 },
      studentId: '',
      records: [],
      appsVisible: false,
      appsClassId: null,
      apps: [],
      applyVisible: false,
      applyClass: null,
      applyForm: { confirmCourseId: '', note: '' },
      applyErrors: { courseId: '' }
    }
  },
  computed: {
    columns() {
      return [
        { title: '班号', dataIndex: 'classNo' },
        { title: '班级名称', dataIndex: 'name' },
        { title: '课程ID', dataIndex: 'courseId' },
        { title: '校区', dataIndex: 'campusId' },
        { title: '教室', dataIndex: 'room' },
        { title: '最大人数', dataIndex: 'maxSize' },
        { title: '剩余名额', dataIndex: 'remaining' },
        { title: '操作', key: 'apply', scopedSlots: { customRender: 'apply' } }
      ]
    },
    recordColumns() {
      return [
        { title: '类型', dataIndex: 'type' },
        { title: '来源班级', dataIndex: 'fromClassId' },
        { title: '目标班级', dataIndex: 'toClassId' },
        { title: '时间', dataIndex: 'time' },
        { title: '原因', dataIndex: 'reason' }
      ]
    },
    appsColumns() {
      return [
        { title: '申请ID', dataIndex: 'id' },
        { title: '学员ID', dataIndex: 'studentId' },
        { title: '状态', dataIndex: 'status' },
        { title: '备注', dataIndex: 'note' }
      ]
    }
  },
  created() {
    this.loadClasses(); this.loadTeacherOptions(); this.loadCampusOptions()
  },
  methods: {
    async loadTeacherOptions() {
      try {
        const res = await fetch('/api/teacher/list')
        const json = await res.json()
        this.teacherOptions = (json && json.code === 200) ? (json.data || []) : []
      } catch(e) { this.teacherOptions = [] }
    },
    async loadCampusOptions() {
      try {
        const res = await fetch('/api/base/campus/list')
        const json = await res.json()
        this.campusOptions = (json && json.code === 200) ? (json.data || []) : []
      } catch(e) { this.campusOptions = [] }
    },
    async loadClasses() {
      const params = []
      if (this.filters.keyword) params.push('q=' + encodeURIComponent(this.filters.keyword))
      if (this.filters.courseId) params.push('courseId=' + encodeURIComponent(this.filters.courseId))
      if (this.filters.teacherId) params.push('teacherId=' + encodeURIComponent(this.filters.teacherId))
      if (this.filters.campusId) params.push('campusId=' + encodeURIComponent(this.filters.campusId))
      if (this.filters.status) params.push('status=' + encodeURIComponent(this.filters.status))
      params.push('page=' + encodeURIComponent(this.pagination.current))
      params.push('size=' + encodeURIComponent(this.pagination.pageSize))
      const q = params.length ? ('?' + params.join('&')) : ''
      const res = await fetch('/api/class/list' + q)
      const json = await res.json()
      const dataRoot = (json && json.code === 200) ? (json.data || {}) : {}
      this.classes = Array.isArray(dataRoot.items) ? dataRoot.items : (Array.isArray(json.data) ? json.data : [])
      this.pagination = { ...this.pagination, total: Number(dataRoot.total || 0) }
    },
    onTableChange(pager) { this.pagination = { ...this.pagination, current: pager.current, pageSize: pager.pageSize }; this.loadClasses() },
    resetFilters(){ this.filters={ keyword:'', courseId:'', teacherId:'', campusId:'', status:'' } },
    applySelect(row) {
      if (!this.studentId) { this.$message.error('请先填写学员ID'); return }
      const sid = Number(this.studentId)
      if (!Number.isFinite(sid) || sid <= 0) { this.$message.error('学员ID无效，应为正整数'); return }
      this.applyClass = row
      this.applyForm = { confirmCourseId: '', note: '' }
      this.applyErrors = { courseId: '' }
      this.applyVisible = true
    },
    closeApply(){ this.applyVisible=false; this.applyClass=null },
    async doApplySelect(){
      // 表单级校验
      const confirm = String(this.applyForm.confirmCourseId||'').trim()
      if (!confirm) { this.applyErrors.courseId = '课程ID不能为空'; return }
      if (String(confirm) !== String(this.applyClass.courseId)) { this.applyErrors.courseId = '课程ID不匹配，请确认'; return }
      this.applyErrors.courseId = ''
      const sid = Number(this.studentId)
      const payload = { classId: this.applyClass.id, studentId: sid, note: this.applyForm.note||'' }
      const res = await fetch('/api/class/select/apply', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
      const json = await res.json()
      if (json && json.code === 200) {
        this.$message.success('已提交选班申请')
        this.applyVisible=false
        this.openApps(this.applyClass)
      } else {
        this.$message.error('提交失败')
      }
    },
    async loadRecords() {
      if (!this.studentId) { this.$message.error('请填写学员ID'); return }
      const res = await fetch(`/api/class/records?studentId=${encodeURIComponent(this.studentId)}`)
      const json = await res.json()
      this.records = (json && json.code === 200) ? (json.data || []) : []
    },
    async openApps(row) {
      this.appsClassId = row.id
      const res = await fetch(`/api/class/select/list?classId=${row.id}`)
      const json = await res.json()
      this.apps = (json && json.code === 200) ? (json.data || []) : []
      this.appsVisible = true
    }
  }
}
</script>

<style scoped>
</style>