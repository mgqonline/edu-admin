<template>
  <div class="grade-manage">
    <a-page-header title="学生成绩管理" sub-title="新增、导入、导出与查询" />
    <a-card bordered style="margin-bottom:12px">
      <a-form layout="inline" class="filter-inline">
        <a-form-item label="学员">
          <a-select v-model="query.studentId" show-search allowClear placeholder="选择学员" style="width: 220px" @change="loadList">
            <a-select-option v-for="s in studentOptions" :key="s.id" :value="s.id">{{ s.name }}（#{{ s.id }}）</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="科目"><a-input v-model="query.subject" placeholder="如 数学" style="width: 160px" @pressEnter="loadList" /></a-form-item>
        <a-form-item label="学期"><a-input v-model="query.term" placeholder="如 2025春季" style="width: 160px" @pressEnter="loadList" /></a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadList">查询</a-button>
            <a-button @click="openAdd">新增</a-button>
            <a-upload :before-upload="beforeImport" :show-upload-list="false">
              <a-button>导入CSV</a-button>
            </a-upload>
            <a-button @click="doExport">导出CSV</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card bordered>
      <a-table :data-source="list" :columns="columns" rowKey="id" :pagination="pagination" @change="onTableChange">
        <template slot="ops" slot-scope="text, record">
          <a-space>
            <a-button size="small" @click="openEdit(record)">编辑</a-button>
            <a-popconfirm title="确认删除？" @confirm="doDelete(record)"><a-button size="small" danger>删除</a-button></a-popconfirm>
          </a-space>
        </template>
      </a-table>
      <div class="empty" v-if="list.length===0">暂无数据</div>
    </a-card>

    <a-modal v-model="editVisible" :title="editMode==='add'?'新增成绩':'编辑成绩'" :confirm-loading="saving" @ok="doSave" @cancel="()=>{editVisible=false}" :width="520" :destroyOnClose="true" :maskClosable="false">
      <a-form v-if="editVisible" layout="horizontal" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="学员"><a-select v-model="form.studentId" show-search placeholder="选择学员"><a-select-option v-for="s in studentOptions" :key="s.id" :value="s.id">{{ s.name }}（#{{ s.id }}）</a-select-option></a-select></a-form-item>
        <a-form-item label="科目"><a-input v-model="form.subject" /></a-form-item>
        <a-form-item label="学期"><a-input v-model="form.term" /></a-form-item>
        <a-form-item label="分数"><a-input-number v-model="form.score" :min="0" :max="100" :precision="2" /></a-form-item>
        <a-form-item label="考试日期"><V2SimpleDate v-model="form.examDate" format="YYYY-MM-DD" valueType="format" /></a-form-item>
        <a-form-item label="状态"><a-select v-model="form.status"><a-select-option value="passed">及格</a-select-option><a-select-option value="failed">不及格</a-select-option><a-select-option value="unknown">未知</a-select-option></a-select></a-form-item>
        <a-form-item label="备注"><a-input v-model="form.remark" /></a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
import V2SimpleDate from '../../components/V2SimpleDate.vue'
export default {
  name: 'GradeManage',
  components: { V2SimpleDate },
  data() {
    return {
      query: { studentId: null, subject: '', term: '' },
      list: [],
      pagination: { current: 1, pageSize: 10, total: 0 },
      studentOptions: [],
      columns: [
        { title: 'ID', dataIndex: 'id' },
        { title: '学员ID', dataIndex: 'studentId' },
        { title: '科目', dataIndex: 'subject' },
        { title: '学期', dataIndex: 'term' },
        { title: '分数', dataIndex: 'score' },
        { title: '考试日期', dataIndex: 'examDate' },
        { title: '状态', dataIndex: 'status' },
        { title: '备注', dataIndex: 'remark' },
        { title: '操作', key: 'ops', scopedSlots: { customRender: 'ops' } }
      ],
      editVisible: false,
      editMode: 'add',
      saving: false,
      form: { id: null, studentId: null, subject: '', term: '', score: null, examDate: null, status: 'unknown', remark: '' }
    }
  },
  created() {
    this.loadStudents();
    this.loadList();
  },
  methods: {
    async loadStudents() {
      try { const res = await fetch('/api/student/list'); const json = await res.json(); this.studentOptions = Array.isArray(json.data) ? json.data.map(it => ({ id: it.id, name: it.name })) : [] } catch(e) { this.studentOptions = [] }
    },
    async loadList() {
      const params = []
      if (this.query.studentId) params.push('studentId='+encodeURIComponent(this.query.studentId))
      if (this.query.subject) params.push('subject='+encodeURIComponent(this.query.subject))
      if (this.query.term) params.push('term='+encodeURIComponent(this.query.term))
      params.push('page='+encodeURIComponent(this.pagination.current))
      params.push('size='+encodeURIComponent(this.pagination.pageSize))
      const qs = params.length ? ('?'+params.join('&')) : ''
      try {
        const res = await fetch('/api/student/grade/list'+qs)
        const json = await res.json()
        const dataRoot = json && json.data ? json.data : {}
        const arr = Array.isArray(dataRoot.items) ? dataRoot.items : (Array.isArray(json.data) ? json.data : [])
        const total = Number(dataRoot.total || 0)
        const fmt = (d) => {
          if (!d) return ''
          try {
            if (typeof d === 'number') {
              const dt = new Date(d); const y=dt.getFullYear(); const m=(dt.getMonth()+1+'').padStart(2,'0'); const day=(dt.getDate()+'').padStart(2,'0'); return `${y}-${m}-${day}`
            }
            const s = String(d)
            if (/^\d{4}-\d{2}-\d{2}/.test(s)) return s.slice(0,10)
            const dt = new Date(s); const y=dt.getFullYear(); const m=(dt.getMonth()+1+'').padStart(2,'0'); const day=(dt.getDate()+'').padStart(2,'0'); return `${y}-${m}-${day}`
          } catch(e) { return '' }
        }
        this.list = arr.map(g => ({...g, examDate: fmt(g.examDate)}))
        this.pagination = { ...this.pagination, total: total }
      } catch(e) { this.list = [] }
    },
    onTableChange(pager) {
      this.pagination = { ...this.pagination, current: pager.current, pageSize: pager.pageSize }
      this.loadList()
    },
    openAdd() { this.editMode='add'; this.form = { id:null, studentId:null, subject:'', term:'', score:null, examDate:null, status:'unknown', remark:'' }; this.editVisible=true },
    openEdit(rec) {
      if (!rec) return
      this.editMode='edit'
      const f = { ...rec }
      if (f.examDate) {
        try {
          const d = new Date(f.examDate)
          if (!isNaN(d.getTime())) {
            const y=d.getFullYear(); const m=String(d.getMonth()+1).padStart(2,'0'); const day=String(d.getDate()).padStart(2,'0');
            f.examDate = `${y}-${m}-${day}`
          } else {
            const s = String(f.examDate)
            f.examDate = /^\d{4}-\d{2}-\d{2}/.test(s) ? s.slice(0,10) : ''
          }
        } catch(e) { f.examDate = '' }
      }
      this.form = f
      this.editVisible=true
    },
    async doSave() {
      if (!this.form.studentId || !this.form.subject) { this.$message && this.$message.warning('请填写学员与科目'); return }
      this.saving = true
      try {
        const payload = { ...this.form }
        if (payload.examDate instanceof Date) {
          const y = payload.examDate.getFullYear(); const m = String(payload.examDate.getMonth()+1).padStart(2,'0'); const d = String(payload.examDate.getDate()).padStart(2,'0');
          payload.examDate = `${y}-${m}-${d}`
        } else if (typeof payload.examDate === 'string') {
          const s = payload.examDate
          if (/^\d{4}-\d{2}-\d{2}$/.test(s)) {
            payload.examDate = s
          } else {
            const d = new Date(s)
            if (!isNaN(d.getTime())) {
              const y = d.getFullYear(); const m = String(d.getMonth()+1).padStart(2,'0'); const dd = String(d.getDate()).padStart(2,'0');
              payload.examDate = `${y}-${m}-${dd}`
            } else {
              payload.examDate = ''
            }
          }
        }
        const url = this.editMode==='add' ? '/api/student/grade/save' : ('/api/student/grade/update/'+this.form.id)
        const method = this.editMode==='add' ? 'POST' : 'PUT'
        const res = await fetch(url, { method, headers: { 'Content-Type':'application/json' }, body: JSON.stringify(payload) })
        const json = await res.json(); if (json && json.code === 200) { this.$message && this.$message.success('已保存'); this.editVisible=false; this.loadList() } else { throw new Error(json.message || '保存失败') }
      } catch(e) { this.$message && this.$message.error(e.message || '保存失败') }
      this.saving = false
    },
    async doDelete(rec) {
      if (!rec || !rec.id) return
      try { const res = await fetch('/api/student/grade/delete/'+rec.id, { method:'DELETE' }); const json = await res.json(); if (json && json.code === 200) { this.$message && this.$message.success('已删除'); this.loadList() } else { throw new Error(json.message || '删除失败') } } catch(e) { this.$message && this.$message.error('删除失败') }
    },
    beforeImport(file) {
      const reader = new FileReader()
      reader.onload = async () => {
        try {
          const text = reader.result
          const rows = String(text).trim().split(/\r?\n/)
          const header = rows[0].split(',')
          const idx = (name) => header.findIndex(h => h.trim()===name)
          const out = []
          for (let i=1;i<rows.length;i++) {
            const cols = rows[i].split(',')
            out.push({
              studentId: Number(cols[idx('studentId')]),
              subject: cols[idx('subject')],
              term: cols[idx('term')],
              score: cols[idx('score')] ? Number(cols[idx('score')]) : null,
              examDate: cols[idx('examDate')] || '',
              status: cols[idx('status')] || 'unknown',
              remark: cols[idx('remark')] || ''
            })
          }
          const res = await fetch('/api/student/grade/import', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(out) })
          const json = await res.json(); if (json && json.code === 200) { this.$message && this.$message.success('导入完成'); this.loadList() } else { throw new Error(json.message || '导入失败') }
        } catch(e) { this.$message && this.$message.error('导入失败') }
      }
      reader.readAsText(file)
      return false
    },
    async doExport() {
      const params = []
      if (this.query.studentId) params.push('studentId='+encodeURIComponent(this.query.studentId))
      if (this.query.subject) params.push('subject='+encodeURIComponent(this.query.subject))
      if (this.query.term) params.push('term='+encodeURIComponent(this.query.term))
      const qs = params.length ? ('?'+params.join('&')) : ''
      try {
        const res = await fetch('/api/student/grade/export'+qs)
        const text = await res.text()
        const blob = new Blob([text], { type: 'text/csv;charset=utf-8;' })
        const url = URL.createObjectURL(blob)
        const a = document.createElement('a'); a.href = url; a.download = 'grades.csv'; a.click(); URL.revokeObjectURL(url)
      } catch(e) { this.$message && this.$message.error('导出失败') }
    }
  }
}
</script>

<style scoped>
.grade-manage { background:#fff; padding:16px; }
.empty { padding: 8px; color:#9aa4b5; }
</style>