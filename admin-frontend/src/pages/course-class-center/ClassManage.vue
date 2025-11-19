<template>
  <div style="background:#fff; padding:16px">
    <h3>班级管理</h3>
    <a-tabs>
      <a-tab-pane key="group" tab="班课班级创建">
        <a-form layout="vertical">
          <a-row :gutter="16">
            <a-col :span="6"><a-form-item label="班号"><a-input v-model="groupForm.classNo" /></a-form-item></a-col>
            <a-col :span="8"><a-form-item label="班级名称"><a-input v-model="groupForm.name" /></a-form-item></a-col>
            <a-col :span="10"><a-form-item label="课程ID"><a-input v-model="groupForm.courseId" placeholder="绑定课程ID" /></a-form-item></a-col>
          </a-row>
          <a-row :gutter="16">
            <a-col :span="6"><a-form-item label="主教师ID"><a-input v-model="groupForm.teacherMainId" /></a-form-item></a-col>
            <a-col :span="6"><a-form-item label="副教师ID"><a-input v-model="groupForm.teacherAssistantId" /></a-form-item></a-col>
            <a-col :span="6"><a-form-item label="校区ID"><a-input v-model="groupForm.campusId" /></a-form-item></a-col>
            <a-col :span="6"><a-form-item label="教室"><a-input v-model="groupForm.room" /></a-form-item></a-col>
          </a-row>
          <a-row :gutter="16">
            <a-col :span="6"><a-form-item label="开课日期"><CompatDatePicker @change="onGroupStartDateChange" style="width:100%" /></a-form-item></a-col>
            <a-col :span="6"><a-form-item label="结课日期"><CompatDatePicker @change="onGroupEndDateChange" style="width:100%" /></a-form-item></a-col>
            <a-col :span="6"><a-form-item label="每周上课日"><a-select v-model="groupForm.weekDays" mode="multiple" style="width:100%">
              <a-select-option value="Mon">周一</a-select-option>
              <a-select-option value="Tue">周二</a-select-option>
              <a-select-option value="Wed">周三</a-select-option>
              <a-select-option value="Thu">周四</a-select-option>
              <a-select-option value="Fri">周五</a-select-option>
              <a-select-option value="Sat">周六</a-select-option>
              <a-select-option value="Sun">周日</a-select-option>
            </a-select></a-form-item></a-col>
            <a-col :span="6"><a-form-item label="课时时长(分钟)"><a-input-number v-model="groupForm.durationMinutes" :min="30" :step="15" style="width:100%" /></a-form-item></a-col>
          </a-row>
          <a-row :gutter="16">
            <a-col :span="6"><a-form-item label="最大人数"><a-input-number v-model="groupForm.maxSize" :min="1" style="width:100%" /></a-form-item></a-col>
            <a-col :span="6"><a-form-item label="学费"><a-input-number v-model="groupForm.fee" :min="0" :step="10" style="width:100%" /></a-form-item></a-col>
          </a-row>
          <a-space>
            <a-button type="primary" @click="createGroupClass">创建班课班级</a-button>
            <a-button @click="resetGroup">重置</a-button>
          </a-space>
        </a-form>
      </a-tab-pane>
      <a-tab-pane key="one2one" tab="一对一班级创建">
        <a-form layout="vertical">
          <a-row :gutter="16">
            <a-col :span="6"><a-form-item label="班号"><a-input v-model="oneForm.classNo" /></a-form-item></a-col>
            <a-col :span="10"><a-form-item label="班级名称"><a-input v-model="oneForm.name" placeholder="例如 一对一-张老师" /></a-form-item></a-col>
            <a-col :span="8"><a-form-item label="课程ID"><a-input v-model="oneForm.courseId" /></a-form-item></a-col>
          </a-row>
          <a-row :gutter="16">
            <a-col :span="6"><a-form-item label="固定教师ID"><a-input v-model="oneForm.fixedTeacherId" /></a-form-item></a-col>
            <a-col :span="6"><a-form-item label="灵活匹配教师"><a-switch v-model="oneForm.flexibleTeacher" /></a-form-item></a-col>
            <a-col :span="6"><a-form-item label="专属学员ID"><a-input v-model="oneForm.exclusiveStudentId" /></a-form-item></a-col>
            <a-col :span="6"><a-form-item label="无固定周期"><a-switch v-model="oneForm.noFixedSchedule" /></a-form-item></a-col>
          </a-row>
          <a-row :gutter="16">
            <a-col :span="6"><a-form-item label="校区ID"><a-input v-model="oneForm.campusId" /></a-form-item></a-col>
            <a-col :span="6"><a-form-item label="教室"><a-input v-model="oneForm.room" /></a-form-item></a-col>
            <a-col :span="6"><a-form-item label="课时时长"><a-input-number v-model="oneForm.durationMinutes" :min="30" :step="15" style="width:100%" /></a-form-item></a-col>
            <a-col :span="6"><a-form-item label="学费"><a-input-number v-model="oneForm.fee" :min="0" :step="10" style="width:100%" /></a-form-item></a-col>
          </a-row>
          <a-space>
            <a-button type="primary" @click="createOneClass">创建一对一班级</a-button>
            <a-button @click="resetOne">重置</a-button>
          </a-space>
        </a-form>
      </a-tab-pane>
    </a-tabs>

    <a-divider />
    <h4>班级列表与管理</h4>
    <a-space style="margin-bottom:8px" wrap>
      <a-input v-model="keyword" placeholder="按名称关键字" style="width:220px" />
      <a-input v-model="filters.courseId" placeholder="课程ID" style="width:220px" />
      <a-select v-model="filters.teacherId" placeholder="教师（主教师/固定教师）" style="width:240px" allowClear>
        <a-select-option v-for="t in teacherOptions" :key="t.id" :value="t.id">{{ t.name || t.title || ('#'+t.id) }}</a-select-option>
      </a-select>
      <a-select v-model="filters.campusId" placeholder="校区" style="width:200px" allowClear>
        <a-select-option v-for="c in campusOptions" :key="c.id" :value="c.id">{{ c.title || c.name || ('#'+c.id) }}</a-select-option>
      </a-select>
      <a-select v-model="filters.status" placeholder="状态筛选" style="width:160px" allowClear>
        <a-select-option value="not_started">未开课</a-select-option>
        <a-select-option value="ongoing">进行中</a-select-option>
        <a-select-option value="ended">已结课</a-select-option>
        <a-select-option value="suspended">临时停课</a-select-option>
      </a-select>
      <a-button type="primary" @click="loadClasses">查询</a-button>
      <a-button @click="resetFilters">清空筛选</a-button>
    </a-space>
    <a-table :columns="columns" :data-source="classes" rowKey="id" :pagination="false">
      <span slot="status" slot-scope="t, r">
        <a-tag v-if="r.status==='suspended'" color="orange">停课</a-tag>
        <span v-else>{{ statusText(r.status) }}</span>
      </span>
      <span slot="actions" slot-scope="t, r">
        <a-space>
          <a-dropdown>
            <a class="ant-dropdown-link">状态<i class="anticon anticon-down" /></a>
            <a-menu slot="overlay">
              <a-menu-item @click="changeStatus(r, 'not_started')">未开课</a-menu-item>
              <a-menu-item @click="changeStatus(r, 'ongoing')">进行中</a-menu-item>
              <a-menu-item @click="changeStatus(r, 'ended')">已结课</a-menu-item>
              <a-menu-item @click="suspend(r)">临时停课</a-menu-item>
            </a-menu>
          </a-dropdown>
          <a @click="openManualEnroll(r)">手动分班</a>
          <a @click="openSelectApps(r)">选班申请</a>
        </a-space>
      </span>
    </a-table>

    <a-modal :visible="manualVisible" title="手动分班" @ok="doManualEnroll" @cancel="manualVisible=false">
      <a-form layout="vertical">
        <a-form-item label="学员ID"><a-input v-model="manual.studentId" /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal :visible="suspendVisible" title="临时停课" @ok="doSuspend" @cancel="suspendVisible=false">
      <a-form layout="vertical">
        <a-form-item label="原因"><a-input-textarea v-model="suspendReason" :rows="3" /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal :visible="selectVisible" title="选班申请审核" @cancel="selectVisible=false" footer="null">
      <a-table :columns="selectColumns" :data-source="selectApps" rowKey="id" :pagination="false">
        <span slot="ops" slot-scope="t, row">
          <a-space>
            <a @click="approve(row)">通过</a>
            <a @click="reject(row)">驳回</a>
          </a-space>
        </span>
      </a-table>
    </a-modal>
  </div>
</template>

<script>
export default {
  name: 'ClassManage',
  components: { CompatDatePicker: () => import('../../components/CompatDatePicker.vue') },
  data() {
    return {
      groupForm: { classNo: '', name: '', courseId: '', teacherMainId: '', teacherAssistantId: '', campusId: '', room: '', startDate: null, endDate: null, weekDays: [], durationMinutes: 90, maxSize: 30, fee: 0 },
      oneForm: { classNo: '', name: '', courseId: '', fixedTeacherId: '', flexibleTeacher: false, exclusiveStudentId: '', noFixedSchedule: true, campusId: '', room: '', durationMinutes: 60, fee: 0 },
      keyword: '',
      filters: { courseId: '', teacherId: '', campusId: '', status: '' },
      teacherOptions: [],
      campusOptions: [],
      classes: [],
      manualVisible: false,
      manual: { classId: null, studentId: '' },
      suspendVisible: false,
      suspendClassId: null,
      suspendReason: '',
      selectVisible: false,
      selectClassId: null,
      selectApps: []
    }
  },
  computed: {
    columns() {
      return [
        { title: '班号', dataIndex: 'classNo' },
        { title: '班级名称', dataIndex: 'name' },
        { title: '课程ID', dataIndex: 'courseId' },
        { title: '主教师', dataIndex: 'teacherMainId' },
        { title: '校区', dataIndex: 'campusId' },
        { title: '教室', dataIndex: 'room' },
        { title: '最大人数', dataIndex: 'maxSize' },
        { title: '已报名', dataIndex: 'enrolledCount' },
        { title: '剩余名额', dataIndex: 'remaining' },
        { title: '状态', dataIndex: 'status', scopedSlots: { customRender: 'status' } },
        { title: '操作', key: 'actions', scopedSlots: { customRender: 'actions' } }
      ]
    },
    selectColumns() {
      return [
        { title: '申请ID', dataIndex: 'id' },
        { title: '学员ID', dataIndex: 'studentId' },
        { title: '状态', dataIndex: 'status' },
        { title: '备注', dataIndex: 'note' },
        { title: '操作', key: 'ops', scopedSlots: { customRender: 'ops' } }
      ]
    }
  },
  created() {
    this.loadClasses(); this.loadTeacherOptions(); this.loadCampusOptions()
  },
  methods: {
    noop() {},
    onGroupStartDateChange(dateString) { this.groupForm.startDate = dateString || '' },
    onGroupEndDateChange(dateString) { this.groupForm.endDate = dateString || '' },
    statusText(s) {
      return s === 'not_started' ? '未开课' : s === 'ongoing' ? '进行中' : s === 'ended' ? '已结课' : s === 'suspended' ? '临时停课' : s
    },
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
    async createGroupClass() {
      const payload = Object.assign({}, this.groupForm)
      // 日期序列化
      if (typeof payload.startDate === 'object' && payload.startDate && payload.startDate._d) payload.startDate = payload.startDate._d
      if (typeof payload.endDate === 'object' && payload.endDate && payload.endDate._d) payload.endDate = payload.endDate._d
      const res = await fetch('/api/class/create-group', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
      const json = await res.json()
      if (json && json.code === 200) { this.$message.success('创建成功'); this.loadClasses() } else { this.$message.error('创建失败') }
    },
    resetGroup() { this.groupForm = { classNo: '', name: '', courseId: '', teacherMainId: '', teacherAssistantId: '', campusId: '', room: '', startDate: null, endDate: null, weekDays: [], durationMinutes: 90, maxSize: 30, fee: 0 } },
    async createOneClass() {
      const payload = Object.assign({}, this.oneForm)
      const res = await fetch('/api/class/create-one2one', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
      const json = await res.json()
      if (json && json.code === 200) { this.$message.success('创建成功'); this.loadClasses() } else { this.$message.error('创建失败') }
    },
    resetOne() { this.oneForm = { classNo: '', name: '', courseId: '', fixedTeacherId: '', flexibleTeacher: false, exclusiveStudentId: '', noFixedSchedule: true, campusId: '', room: '', durationMinutes: 60, fee: 0 } },
    async loadClasses() {
      const params = []
      if (this.keyword) params.push('keyword=' + encodeURIComponent(this.keyword))
      if (this.filters.courseId) params.push('courseId=' + encodeURIComponent(this.filters.courseId))
      if (this.filters.teacherId) params.push('teacherId=' + encodeURIComponent(this.filters.teacherId))
      if (this.filters.campusId) params.push('campusId=' + encodeURIComponent(this.filters.campusId))
      if (this.filters.status) params.push('status=' + encodeURIComponent(this.filters.status))
      const q = params.length ? ('?' + params.join('&')) : ''
      const res = await fetch('/api/class/list' + q)
      const json = await res.json()
      this.classes = (json && json.code === 200) ? (json.data || []) : []
    },
    resetFilters() { this.keyword=''; this.filters={ courseId:'', teacherId:'', campusId:'', status:'' } },
    async changeStatus(row, status) {
      const res = await fetch(`/api/class/status/${row.id}?status=${status}`, { method: 'POST' })
      const json = await res.json()
      if (json && json.code === 200) { this.$message.success('状态已更新'); this.loadClasses() }
    },
    suspend(row) { this.suspendClassId = row.id; this.suspendReason=''; this.suspendVisible = true },
    async doSuspend() {
      const res = await fetch(`/api/class/status/${this.suspendClassId}?status=suspended&reason=${encodeURIComponent(this.suspendReason||'')}`, { method: 'POST' })
      const json = await res.json()
      if (json && json.code === 200) { this.$message.success('已临时停课'); this.suspendVisible=false; this.loadClasses() }
    },
    openManualEnroll(row) { this.manual = { classId: row.id, studentId: '' }; this.manualVisible = true },
    async doManualEnroll() {
      const payload = { classId: this.manual.classId, studentId: Number(this.manual.studentId) }
      const res = await fetch('/api/class/enroll-manual', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
      const json = await res.json()
      if (json && json.code === 200) { this.$message.success('分班成功'); this.manualVisible=false; this.loadClasses() }
    },
    openSelectApps(row) { this.selectClassId = row.id; this.loadSelectApps(); this.selectVisible = true },
    async loadSelectApps() {
      const res = await fetch(`/api/class/select/list?classId=${this.selectClassId}`)
      const json = await res.json()
      this.selectApps = (json && json.code === 200) ? (json.data || []) : []
    },
    async approve(row) {
      const payload = { applicationId: row.id, action: 'approve' }
      const res = await fetch('/api/class/select/approve', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
      const json = await res.json()
      if (json && json.code === 200) { this.$message.success('已通过'); this.loadClasses(); this.loadSelectApps() }
    },
    async reject(row) {
      const reason = window.prompt('请输入驳回原因', '') || ''
      const payload = { applicationId: row.id, action: 'reject', reason }
      const res = await fetch('/api/class/select/approve', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
      const json = await res.json()
      if (json && json.code === 200) { this.$message.success('已驳回'); this.loadSelectApps() }
    }
  }
}
</script>

<style scoped>
</style>