<template>
  <div class="audition-manage">
    <a-page-header title="试听管理" sub-title="预约、反馈与转化" />

    <a-card bordered style="margin-bottom: 12px" title="预约试听">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-form-item label="学员">
              <template v-if="students.length">
                <a-select v-model="form.studentId" show-search allowClear placeholder="选择学员" style="width:100%">
                  <a-select-option
                    v-for="s in students"
                    :key="s._studentId"
                    :value="s._studentId"
                  >{{ s.name || ('学员#'+s._studentId) }}</a-select-option>
                </a-select>
              </template>
              <template v-else>
                <a-input-number v-model="form.studentId" :min="1" style="width:100%" placeholder="学员ID" />
              </template>
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="课程">
              <template v-if="courses.length">
                <a-select v-model="form.courseId" show-search allowClear placeholder="选择课程" style="width:100%">
                  <a-select-option
                    v-for="c in courses"
                    :key="c._courseId"
                    :value="c._courseId"
                    :disabled="c.status && c.status !== 'published'"
                  >{{ (c.name || ('课程#'+c._courseId)) + (c.status ? (' ['+ (c.status==='published' ? '已发布' : c.status==='draft' ? '草稿' : '已下架') +']') : '') }}</a-select-option>
                </a-select>
              </template>
              <template v-else>
                <a-input-number v-model="form.courseId" :min="1" style="width:100%" placeholder="课程ID" />
              </template>
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="教师">
              <template v-if="teachers.length">
                <a-select v-model="form.teacherId" show-search allowClear placeholder="选择教师" style="width:100%">
                  <a-select-option
                    v-for="t in teachers"
                    :key="t._teacherId"
                    :value="t._teacherId"
                  >{{ t.name || ('教师#'+t._teacherId) }}</a-select-option>
                </a-select>
              </template>
              <template v-else>
                <a-input-number v-model="form.teacherId" :min="1" style="width:100%" placeholder="教师ID" />
              </template>
            </a-form-item>
          </a-col>
            <a-col :span="6"><a-form-item label="时间"><V2SimpleDate v-model="form.time" @change="onTimeChange" :type="'datetime'" format="YYYY-MM-DD HH:mm:ss" :valueType="'format'" style="width:100%" /></a-form-item></a-col>
        </a-row>
        <a-form-item :wrapper-col="{ span: 16, offset: 6 }">
          <a-space>
            <a-button type="primary" @click="book">预约试听</a-button>
            <a-button @click="loadList">加载列表</a-button>
            <a-button @click="cleanupInvalid" type="dashed">清理空记录</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card bordered title="试听列表">
      <a-table :data-source="list" :columns="columns" rowKey="id" :pagination="false">
        <template v-slot:status="slotProps">
          <a-tag color="blue" v-if="slotProps && slotProps.record && slotProps.record.status==='booked'">已预约</a-tag>
          <a-tag color="green" v-else-if="slotProps && slotProps.record && slotProps.record.status==='finished'">已完成</a-tag>
          <a-tag v-else>未知</a-tag>
        </template>
        <template v-slot:action="slotProps">
          <a-space v-if="slotProps && slotProps.record">
            <a-input size="small" v-model="slotProps.record._feedback" placeholder="评价" style="width:160px" />
            <a-select size="small" v-model="slotProps.record._result" style="width:140px">
              <a-select-option value="适合报班">适合报班</a-select-option>
              <a-select-option value="需再评估">需再评估</a-select-option>
            </a-select>
            <a-button size="small" @click="submitFeedback(slotProps.record)" :disabled="slotProps.record.status==='finished'">提交反馈</a-button>
            <a-button size="small" type="primary" @click="convert(slotProps.record)">转为正式学员</a-button>
          </a-space>
          <span v-else>—</span>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'AuditionManage',
  components: { V2SimpleDate: () => import('../../components/V2SimpleDate.vue') },
  data() {
    return {
      form: { studentId: null, courseId: null, teacherId: null, time: '' },
      list: [],
      courses: [],
      students: [],
      teachers: [],
      columns: [
        { title: 'ID', dataIndex: 'id', key: 'id' },
        { title: '学员', dataIndex: 'studentName', key: 'studentName' },
        { title: '课程', dataIndex: 'courseName', key: 'courseName' },
        { title: '教师', dataIndex: 'teacherName', key: 'teacherName' },
        { title: '时间', dataIndex: 'time', key: 'time' },
        { title: '状态', dataIndex: 'status', key: 'status', scopedSlots: { customRender: 'status' } },
        { title: '操作', key: 'action', scopedSlots: { customRender: 'action' } }
      ]
    }
  },
  methods: {
    noop() {},
    onTimeChange(dateString) { this.form.time = (dateString || '').toString() },
    isValidId(n) { return Number.isFinite(Number(n)) && Number(n) > 0 },
    validateForm() {
      const { studentId, courseId, teacherId, time } = this.form
      if (!this.isValidId(studentId)) { this.$message && this.$message.error('请填写有效的学员ID'); return false }
      if (!this.isValidId(courseId)) { this.$message && this.$message.error('请填写或选择有效的课程'); return false }
      if (!this.isValidId(teacherId)) { this.$message && this.$message.error('请填写或选择有效的教师'); return false }
      if (!time || String(time).trim().length === 0) { this.$message && this.$message.error('请选择试听时间'); return false }
      return true
    },
    async book() {
      if (!this.validateForm()) return
      const payload = {
        studentId: Number(this.form.studentId),
        courseId: Number(this.form.courseId),
        teacherId: Number(this.form.teacherId),
        time: this.form.time
      }
      const res = await fetch('/api/audition/book', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(payload) })
      const json = await res.json()
      if (json.code === 200) { this.$message && this.$message.success('预约成功'); this.loadList() } else { this.$message && this.$message.error('预约失败：'+(json.msg||'未知错误')) }
    },
    studentNameById(id){
      const sid = String(id)
      const s = (this.students||[]).find(it => String(it._studentId)===sid)
      return s ? (s.name || ('学员#'+sid)) : ('学员#'+sid)
    },
    courseNameById(id){
      const cid = String(id)
      const c = (this.courses||[]).find(it => String(it._courseId)===cid)
      return c ? (c.name || ('课程#'+cid)) : ('课程#'+cid)
    },
    teacherNameById(id){
      const tid = String(id)
      const t = (this.teachers||[]).find(it => String(it._teacherId)===tid)
      return t ? (t.name || ('教师#'+tid)) : ('教师#'+tid)
    },
    async loadList() {
      const res = await fetch('/api/audition/list')
      const json = await res.json()
      this.list = (json.data||[]).map(x=>({
        ...x,
        studentName: this.studentNameById(x.studentId),
        courseName: this.courseNameById(x.courseId),
        teacherName: this.teacherNameById(x.teacherId),
        _feedback:'', _result:'适合报班'
      }))
    },
    async cleanupInvalid() {
      try {
        if (!Array.isArray(this.list) || this.list.length === 0) { await this.loadList() }
        const invalids = (this.list||[]).filter(x => !this.isValidId(x.studentId) || !this.isValidId(x.courseId) || !this.isValidId(x.teacherId) || !x.time)
        if (invalids.length === 0) { this.$message && this.$message.info('没有需要清理的空记录'); return }
        let ok = 0, fail = 0
        for (const it of invalids) {
          try {
            const res = await fetch('/api/audition/delete', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ id: it.id }) })
            const j = await res.json()
            if (j && j.code === 200) ok++; else fail++
          } catch(_) { fail++ }
        }
        if (ok > 0) {
          this.$message && this.$message.success(`清理完成：成功 ${ok} 条，失败 ${fail} 条`)
          await this.loadList()
        } else {
          // 后端暂不支持删除，前端先隐藏无效记录，避免干扰使用
          const before = this.list.length
          this.list = (this.list||[]).filter(x => this.isValidId(x.studentId) && this.isValidId(x.courseId) && this.isValidId(x.teacherId) && !!x.time)
          const removed = before - this.list.length
          this.$message && this.$message.info(`后端未提供删除接口，已从列表隐藏 ${removed} 条无效记录`)
        }
      } catch(e) {
        this.$message && this.$message.error('清理失败：网络或服务异常')
      }
    },
    async loadOptions() {
      try {
        const [cr, tr, sr] = await Promise.all([
          fetch('/api/course/list'),
          fetch('/api/teacher/list'),
          fetch('/api/student/list')
        ])
        const cj = await cr.json().catch(()=>({}))
        const tj = await tr.json().catch(()=>({}))
        const sj = await sr.json().catch(()=>({}))
        // 解析课程数据，兼容不同返回结构，并仅保留“已发布”的课程
        const rawCourses = (cj && cj.code === 200 && Array.isArray(cj.data))
          ? cj.data
          : (Array.isArray(cj) ? cj : (Array.isArray(cj.items) ? cj.items : []))
        const isPublished = (c) => {
          const s = c.status != null ? c.status : (c.state != null ? c.state : (c.publishStatus != null ? c.publishStatus : null))
          if (typeof s === 'string') {
            const v = s.toLowerCase()
            return v === 'published' || v === '已发布' || v === 'online' || v === 'enabled'
          }
          return s === true || s === 1 || s === '1'
        }
        const publishedCourses = rawCourses.filter(c => isPublished(c))
        let parsedCourses = publishedCourses
          .map(c => {
            const cid = Number(c.courseId)
            const iid = Number(c.id)
            const resolved = (Number.isFinite(cid) && cid > 0) ? cid : ((Number.isFinite(iid) && iid > 0) ? iid : null)
            return Object.assign({}, c, { _courseId: resolved })
          })
          .filter(c => Number.isFinite(c._courseId) && c._courseId > 0)
        // 若解析后为空，做一次宽松回退：不过滤状态，仅保留有数值 ID 的课程
        if (!parsedCourses.length) {
          parsedCourses = rawCourses
            .map(c => {
              const cid = Number(c.courseId)
              const iid = Number(c.id)
              const resolved = (Number.isFinite(cid) && cid > 0) ? cid : ((Number.isFinite(iid) && iid > 0) ? iid : null)
              return Object.assign({}, c, { _courseId: resolved })
            })
            .filter(c => Number.isFinite(c._courseId) && c._courseId > 0)
        }
        this.courses = parsedCourses
        // 教师解析：兼容 id/teacherId，保留数值型 ID 用于选择与名称映射
        const traw = Array.isArray(tj.data) ? tj.data : []
        this.teachers = traw
          .map(t => {
            const tid1 = Number(t.teacherId)
            const tid2 = Number(t.id)
            const resolved = (Number.isFinite(tid1) && tid1 > 0) ? tid1 : ((Number.isFinite(tid2) && tid2 > 0) ? tid2 : null)
            return Object.assign({}, t, { _teacherId: resolved })
          })
          .filter(t => Number.isFinite(t._teacherId) && t._teacherId > 0)
        // 学员解析：兼容 id/studentId，保留数值型 ID 用于选择
        const sraw = Array.isArray(sj.data) ? sj.data : []
        this.students = sraw
          .map(s => {
            const sid = Number(s.id)
            const sid2 = Number(s.studentId)
            const resolved = (Number.isFinite(sid) && sid > 0) ? sid : ((Number.isFinite(sid2) && sid2 > 0) ? sid2 : null)
            return Object.assign({}, s, { _studentId: resolved })
          })
          .filter(s => Number.isFinite(s._studentId) && s._studentId > 0)
      } catch(e) {
        // 保持空数组作为降级，使用数字输入
        this.courses = []
        this.students = []
        this.teachers = []
      }
    },
    async submitFeedback(a) {
      const payload = { id: a.id, feedback: a._feedback, result: a._result }
      const res = await fetch('/api/audition/feedback', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(payload) })
      const json = await res.json()
      if (json.code === 200) { this.$message && this.$message.success('反馈已提交'); this.loadList() } else { this.$message && this.$message.error('失败：'+(json.msg||'未知错误')) }
    },
    async convert(a) {
      const res = await fetch('/api/audition/convert', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ auditionId: a.id }) })
      const json = await res.json()
      if (json.code === 200) {
        this.$message && this.$message.success('可转为正式学员，已为你跳转到报名页');
        const recommend = (a._result || a.result || '适合报班')
        this.$router && this.$router.push({ path: '/student/enroll', query: { studentId: a.studentId, auditionId: a.id, recommend } })
      } else {
        this.$message && this.$message.error('转化失败：'+(json.msg||'未知错误'))
      }
    }
  },
  async mounted() { await this.loadOptions(); await this.loadList() }
}
</script>

<style scoped>
.audition-manage { }
</style>