<template>
  <div class="teacher-page">
    <a-page-header title="教师管理" sub-title="科目、班级与课时费设置" />

    <a-card bordered>
      <a-space style="margin-bottom: 12px" wrap>
        <a-button type="primary" v-perm="'base:teacher:add'" @click="openTeacherModal">新增教师</a-button>
        <a-upload :showUploadList="false" :beforeUpload="beforeUploadTeacherExcel">
          <a-button>导入Excel</a-button>
        </a-upload>
        <a-input-search v-model="filters.q" placeholder="按姓名搜索" style="width: 220px" @search="onSearch" allowClear />
        <a-select v-model="filters.status" placeholder="状态筛选" style="width: 160px" @change="onSearch" allowClear>
          <a-select-option value="enabled">启用</a-select-option>
          <a-select-option value="disabled">停用</a-select-option>
        </a-select>
        <a-button @click="loadTeachers" :loading="loading">查询</a-button>
      </a-space>

      <a-table :data-source="list" :columns="columns" :rowKey="rowKeyOf" :pagination="pagination" @change="onTableChange">
        <template slot="status" slot-scope="text, record, index">
          <a-tag :color="(record && record.status) === 'enabled' ? 'green' : 'red'">
            {{ (record && record.status) === 'enabled' ? '启用' : '停用' }}
          </a-tag>
        </template>

        <template slot="campus" slot-scope="text, record">
          {{ record.campusName  }}
        </template>
        <template slot="dept" slot-scope="text, record">
          {{ record.deptName  }}
        </template>

        <template slot="action" slot-scope="text, record, index">
          <a-space>
            <a-button size="small" v-perm="'base:teacher:edit'" @click="editTeacher(record)">编辑</a-button>
            <a-button size="small" type="danger" v-perm="'base:teacher:toggle'" :disabled="!record || typeof record.id !== 'number'" @click="record && toggleStatus(record)">{{ (record && record.status) === 'enabled' ? '停用' : '启用' }}</a-button>
            <a-button size="small" type="danger" v-perm="'base:teacher:edit'" :disabled="!record || typeof record.id !== 'number'" @click="removeTeacher(record)" ghost>删除</a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- 新增/编辑教师 -->
    <a-modal :visible="teacherModal.visible" title="教师信息" @ok="saveTeacher" @cancel="closeTeacherModal">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="姓名"><a-input v-model="teacherModal.form.name" placeholder="请输入姓名" /></a-form-item>
        <a-form-item label="性别">
          <a-select v-model="teacherModal.form.gender" placeholder="选择性别" allowClear>
            <a-select-option value="男">男</a-select-option>
            <a-select-option value="女">女</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="手机号"><a-input v-model="teacherModal.form.mobile" placeholder="请输入手机号" /></a-form-item>
        <a-form-item label="入职日期"><V2SimpleDate v-model="teacherModal.form.joinDate" format="YYYY-MM-DD" style="width: 100%" /></a-form-item>
        <a-form-item label="所属校区">
          <a-select v-model="teacherModal.form.campusId" placeholder="选择校区" style="width: 100%" allowClear @change="onCampusChange">
            <a-select-option v-for="c in campusOptions" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="所属部门">
          <a-select v-model="teacherModal.form.deptId" placeholder="选择部门" allowClear :disabled="!flatDepts.length">
            <a-select-option v-for="d in flatDepts" :key="d.id" :value="d.id">{{ d.title }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="教师等级">
          <a-select v-model="teacherModal.form.level" placeholder="选择等级" allowClear>
            <a-select-option value="初级">初级</a-select-option>
            <a-select-option value="中级">中级</a-select-option>
            <a-select-option value="高级">高级</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="资格证号"><a-input v-model="teacherModal.form.qualificationCertNo" placeholder="请输入教师资格证号" /></a-form-item>
        <a-form-item label="培训经历"><a-input v-model="teacherModal.form.trainingExperience" placeholder="请输入培训经历" /></a-form-item>
        <a-divider />
        <a-form-item label="关联员工">
          <a-select v-model="teacherModal.form.staffId" placeholder="选择员工" allowClear @change="onStaffChange">
            <a-select-option v-for="s in staffList" :key="s.id" :value="s.id">{{ s.name }}（{{ s.deptName || '-' }}）</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="科目">
          <a-select
            v-model="subjectsTemp"
            mode="tags"
            style="width: 100%"
            :token-separators="[',','，',';','；',' ']"
            :options="subjectOptions.map(s => ({ value: s.name, label: s.name }))"
            placeholder="选择或输入科目，回车或分隔符新增"
            @change="onSubjectsChange"
          />
        </a-form-item>
        <a-form-item label="班级">
          <a-select
            v-model="classesTemp"
            mode="tags"
            style="width: 100%"
            :token-separators="[',','，',';','；',' ']"
            :options="classOptions.map(s => ({ value: s.name, label: s.name }))"
            placeholder="选择或输入班级，回车或分隔符新增"
            @change="onClassesChange"
          />
        </a-form-item>
        <a-form-item label="课时费">
          <a-input-number v-model="teacherModal.form.hourlyRate" :min="0" style="width: 100%" placeholder="根据校区与等级自动建议，可手动改" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 科目设置 -->
    <a-modal :visible="subjectsModal.visible" title="设置科目" @ok="saveSubjects" @cancel="closeSubjectsModal">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="科目">
          <a-select
            v-model="subjectsModal.form.subjectsArr"
            mode="tags"
            style="width: 100%"
            :token-separators="[',','，',';','；',' ']"
            :options="subjectOptions.map(s => ({ value: s.name, label: s.name }))"
            placeholder="选择或输入科目，回车或分隔符新增"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 班级设置 -->
    <a-modal :visible="classesModal.visible" title="设置班级" @ok="saveClasses" @cancel="closeClassesModal">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="班级">
          <a-select
            v-model="classesModal.form.classesArr"
            mode="tags"
            style="width: 100%"
            :token-separators="[',','，',';','；',' ']"
            :options="classOptions.map(s => ({ value: s.name, label: s.name }))"
            placeholder="选择或输入班级，回车或分隔符新增"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 课时费设置 -->
    <a-modal :visible="rateModal.visible" title="设置课时费单价" @ok="saveRate" @cancel="closeRateModal">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="课时费">
          <a-input-number v-model="rateModal.form.hourlyRate" :min="0" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
export default {
  name: 'TeacherPage',
  components: { V2SimpleDate: () => import('../../components/V2SimpleDate.vue') },
  data() {
    return {
      loading: false,
      list: [],
      staffList: [],
      campusOptions: [],
      subjectOptions: [],
      classOptions: [],
      filters: { q: '', status: '' },
      columns: [
        { title: '姓名', dataIndex: 'name', key: 'name' },
        { title: '性别', dataIndex: 'gender', key: 'gender' },
        { title: '手机号', dataIndex: 'mobile', key: 'mobile' },
        { title: '部门', dataIndex: 'dept', key: 'dept', scopedSlots: { customRender: 'dept' } },
        { title: '校区', dataIndex: 'campus', key: 'campus', scopedSlots: { customRender: 'campus' } },
        { title: '等级', dataIndex: 'level', key: 'level' },
        { title: '科目', dataIndex: 'subjects', key: 'subjects' },
        { title: '班级', dataIndex: 'classes', key: 'classes' },
        { title: '课时费', dataIndex: 'hourlyRate', key: 'hourlyRate' },
        { title: '状态', dataIndex: 'status', key: 'status', scopedSlots: { customRender: 'status' } },
        { title: '操作', key: 'action', scopedSlots: { customRender: 'action' } }
      ],
      teacherModal: { visible: false, form: { id: '', name: '', gender: '', mobile: '', joinDate: null, campusId: null, deptId: null, level: '', qualificationCertNo: '', trainingExperience: '', staffId: null, subjects: '', classes: '', hourlyRate: null } },
      subjectsTemp: [],
      classesTemp: [],
      subjectsModal: { visible: false, form: { id: '', subjectsArr: [] } },
      classesModal: { visible: false, form: { id: '', classesArr: [] } },
      rateModal: { visible: false, form: { id: '', hourlyRate: null } },
      deptTree: [],
      flatDepts: [],
      pagination: { current: 1, pageSize: 10, total: 0, showSizeChanger: true, pageSizeOptions: ['10','20','50','100'] }
    }
  },
  created() {
    this.loadCampusOptions()
    this.loadDeptOptions()
    this.loadStaff()
    this.loadSubjectOptions()
    this.loadClassOptions()
    this.loadTeachers()
  },
  watch: {
    'teacherModal.form.level'(val) { this.suggestHourlyRate() },
    'teacherModal.form.campusId'(val) { this.suggestHourlyRate(); this.loadClassOptions(val); this.loadDeptOptions(val) }
  },
  methods: {
    noop() {},
    async loadCampusOptions() {
      try {
        const res = await fetch('/api/base/campus/list')
        const json = await res.json()
        this.campusOptions = (json && json.code === 200) ? ((json.data || []).map(c => ({ id: c.id, name: c.name }))) : []
      } catch (e) { this.campusOptions = [] }
    },
    async loadTeachers() {
      this.loading = true
      try {
        const params = new URLSearchParams()
        if (this.filters.q) params.append('q', this.filters.q)
        if (this.filters.status) params.append('status', this.filters.status)
        params.append('page', String(this.pagination.current || 1))
        params.append('size', String(this.pagination.pageSize || 10))
        const res = await fetch(`/api/teacher/list?${params.toString()}`)
        const json = await res.json()
        if (json && json.code === 200) {
          const payload = json.data || {}
          const arr = Array.isArray(payload) ? payload : (payload.items || [])
          this.list = arr.map(it => ({
            id: (it.id != null && !isNaN(Number(it.id))) ? Number(it.id) : null,
            name: it.name,
            staffId: it.staffId || null,
            gender: it.gender || '',
            mobile: it.mobile || '',
            joinDate: it.joinDate || null,
            campusId: it.campusId || null,
            campusName: it.campusName || '',
            deptId: it.deptId || null,
            deptName: it.deptName || '',
            level: it.level || '',
            qualificationCertNo: it.qualificationCertNo || '',
            trainingExperience: it.trainingExperience || '',
            subjects: it.subjects || '',
            classes: it.classes || '',
            hourlyRate: it.hourlyRate || 0,
            status: it.status || 'enabled'
          }))
          this.list = this.list.map(r => ({
            ...r,
            campusName: r.campusName || this.campusNameByRecord(r),
            deptName: r.deptName || this.deptNameByRecord(r)
          }))
          console.log("老师数据：",this.list)
          this.pagination.total = Number(payload.total || arr.length)
        } else {
          this.useFallback()
        }
      } catch (e) {
        this.useFallback()
      } finally {
        this.loading = false
      }
    },
    rowKeyOf(rec){
      if(!rec) return Math.random().toString(36).slice(2)
      if(typeof rec.id === 'number') return rec.id
      const a = String(rec.name||'')
      const b = String(rec.mobile||'')
      const c = String(rec.joinDate||'')
      return [a,b,c].filter(Boolean).join('#') || Math.random().toString(36).slice(2)
    },
    onTableChange(pager) {
      this.pagination.current = pager.current
      this.pagination.pageSize = pager.pageSize
      this.loadTeachers()
    },
    async loadStaff() {
      try {
        const res = await fetch('/api/base/staff/list')
        const json = await res.json()
        if (json && json.code === 200) {
          const arr = json.data || []
          this.staffList = arr.map(it => {
            const primary = it.id != null ? Number(it.id) : null
            const fallback = it.staffId != null ? Number(it.staffId) : null
            const sid = !isNaN(primary) && primary != null ? primary : (!isNaN(fallback) && fallback != null ? fallback : null)
            return {
              id: sid,
              name: it.name,
              deptId: it.deptId || null,
              deptName: it.deptName || '',
              campusId: it.campusId || null,
              campusName: it.campusName || ''
            }
          })
        } else {
          this.staffList = []
        }
      } catch (e) {
        this.staffList = []
      }
    },
    campusNameById(id) {
      const found = this.campusOptions.find(c => String(c.id) === String(id))
      return found ? found.name : ''
    },
    campusNameByRecord(rec) {
      const id = rec && rec.campusId
      const name = this.campusNameById(id)
      return name || '-'
    },
    deptNameByRecord(rec) {
      const id = rec && rec.deptId
      if (id != null) {
        const d = this.flatDepts.find(x => String(x.id) === String(id))
        if (d) return d.title
      }
      return '-'
    },
    onSearch() {
      this.loadTeachers()
    },
    useFallback() {
      this.list = [
        { id: 't1', name: '王老师', gender: '男', mobile: '13800000000', joinDate: '2024-09-01', campusId: 1, level: '中级', qualificationCertNo: 'CERT-001', trainingExperience: '2023 培训', subjects: '语文,数学', classes: '一年级一班', hourlyRate: 150, status: 'enabled' },
        { id: 't2', name: '赵老师', gender: '女', mobile: '13900000000', joinDate: '2024-06-01', campusId: 2, level: '初级', qualificationCertNo: 'CERT-002', trainingExperience: '2022 培训', subjects: '英语', classes: '一年级二班', hourlyRate: 120, status: 'disabled' }
      ]
    },
    openTeacherModal() {
      this.teacherModal.visible = true
      this.teacherModal.form = { id: '', name: '', gender: '', mobile: '', joinDate: null, campusId: null, deptId: null, level: '', qualificationCertNo: '', trainingExperience: '', staffId: null, subjects: '', classes: '', hourlyRate: null }
      this.subjectsTemp = []
      this.classesTemp = []
      // 初次打开时加载部门（若已有校区）
      if (this.teacherModal.form.campusId) this.loadDeptOptions(this.teacherModal.form.campusId)
    },
    async editTeacher(record) {
      if (!record || typeof record.id !== 'number') { this.$message && this.$message.warning('无效记录'); return }
      try {
        const res = await fetch(`/api/teacher/detail/${record.id}`)
        const json = await res.json()
        const recRaw = (json && json.code === 200) ? (json.data || {}) : (this.normalizeTeacherRecord(record) || {})
        const rec = this.normalizeTeacherRecord(recRaw)
        this.teacherModal.visible = true
        this.teacherModal.form = { id: rec.id, name: rec.name, gender: rec.gender || '', mobile: rec.mobile || '', joinDate: rec.joinDate || null, campusId: rec.campusId || null, deptId: rec.deptId || null, level: rec.level || '', qualificationCertNo: rec.qualificationCertNo || '', trainingExperience: rec.trainingExperience || '', staffId: rec.staffId || null, subjects: rec.subjects || '', classes: rec.classes || '', hourlyRate: rec.hourlyRate != null ? Number(rec.hourlyRate) : null }
        this.subjectsTemp = this.splitStrToArr(rec.subjects)
        this.classesTemp = this.splitStrToArr(rec.classes)
        if (this.teacherModal.form.campusId) this.loadDeptOptions(this.teacherModal.form.campusId)
      } catch (e) {
        const rec = this.normalizeTeacherRecord(record)
        this.teacherModal.visible = true
        this.teacherModal.form = { id: rec.id, name: rec.name, gender: rec.gender || '', mobile: rec.mobile || '', joinDate: rec.joinDate || null, campusId: rec.campusId || null, deptId: rec.deptId || null, level: rec.level || '', qualificationCertNo: rec.qualificationCertNo || '', trainingExperience: rec.trainingExperience || '', staffId: rec.staffId || null, subjects: rec.subjects || '', classes: rec.classes || '', hourlyRate: rec.hourlyRate != null ? Number(rec.hourlyRate) : null }
        this.subjectsTemp = this.splitStrToArr(rec.subjects)
        this.classesTemp = this.splitStrToArr(rec.classes)
        if (this.teacherModal.form.campusId) this.loadDeptOptions(this.teacherModal.form.campusId)
      }
    },
    closeTeacherModal() { this.teacherModal.visible = false },
    async saveTeacher() {
      const payload = { ...this.teacherModal.form, subjects: (this.subjectsTemp || []).join(','), classes: (this.classesTemp || []).join(',') }
      try {
        const url = payload.id ? `/api/teacher/update/${payload.id}` : '/api/teacher/save'
        const method = payload.id ? 'PUT' : 'POST'
        const res = await fetch(url, { method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
        const json = await res.json()
        if (json && json.code === 200) {
          this.closeTeacherModal()
          this.loadTeachers()
          if (this.$message) this.$message.success('教师信息已保存')
        } else {
          throw new Error('保存失败')
        }
      } catch (e) {
        this.closeTeacherModal()
        this.loadTeachers()
        if (this.$message) this.$message.error('保存失败，已回滚至列表')
      }
    },

    onCampusChange() {
      // 校区变更时联动班级与部门列表
      const campusId = this.teacherModal.form.campusId
      this.loadClassOptions(campusId)
      this.loadDeptOptions(campusId)
      // 校区切换后，若当前部门不在新校区的选项中，清空部门选择
      if (this.teacherModal.form.deptId) {
        const exist = this.flatDepts.find(d => String(d.id) === String(this.teacherModal.form.deptId))
        if (!exist) this.teacherModal.form.deptId = null
      }
    },
    onStaffChange(val) {
      // 选择员工后自动修复映射：填充校区与部门
      const staff = this.staffList.find(s => String(s.id) === String(val))
      if (staff) {
        if (staff.campusId != null) {
          this.teacherModal.form.campusId = staff.campusId
          this.loadDeptOptions(staff.campusId)
        }
        if (staff.deptId != null) this.teacherModal.form.deptId = staff.deptId
      }
    },
    onSubjectsChange(val) {
      this.subjectsTemp = Array.isArray(val) ? val : []
    },
    onClassesChange(val) {
      this.classesTemp = Array.isArray(val) ? val : []
    },

    openSubjectsModal(record) {
      this.subjectsModal.visible = true
      this.subjectsModal.form = { id: record.id, subjectsArr: this.splitStrToArr(record.subjects) }
    },
    closeSubjectsModal() { this.subjectsModal.visible = false },
    async saveSubjects() {
      const { id, subjectsArr } = this.subjectsModal.form
      const subjects = (subjectsArr || []).join(',')
      try {
        await this.ensureSubjectsInDict(subjectsArr || [])
        const res = await fetch(`/api/teacher/update/${id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ subjects }) })
        const json = await res.json()
        if (json && json.code === 200) {
          this.closeSubjectsModal()
          this.loadTeachers()
          if (this.$message) this.$message.success('科目已更新')
        } else { throw new Error('更新失败') }
      } catch (e) {
        this.closeSubjectsModal(); this.loadTeachers(); if (this.$message) this.$message.error('更新失败，已回滚至列表')
      }
    },

    openClassesModal(record) {
      this.classesModal.visible = true
      this.classesModal.form = { id: record.id, classesArr: this.splitStrToArr(record.classes) }
    },
    closeClassesModal() { this.classesModal.visible = false },
    async saveClasses() {
      const { id, classesArr } = this.classesModal.form
      const classes = (classesArr || []).join(',')
      try {
        await this.ensureClassesInDict(classesArr || [])
        const res = await fetch(`/api/teacher/update/${id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ classes }) })
        const json = await res.json()
        if (json && json.code === 200) {
          this.closeClassesModal(); this.loadTeachers(); if (this.$message) this.$message.success('班级已更新')
        } else { throw new Error('更新失败') }
      } catch (e) {
        this.closeClassesModal(); this.loadTeachers(); if (this.$message) this.$message.error('更新失败，已回滚至列表')
      }
    },

    // 字典与工具方法（此前误放在 <style> 之后，现归位）
    async loadSubjectOptions() {
      try {
        const res = await fetch('/api/dict/subject/list?status=enabled')
        const json = await res.json()
        this.subjectOptions = (json && json.code === 200) ? (json.data || []) : []
      } catch (e) { this.subjectOptions = [] }
    },
    async loadClassOptions(campusId) {
      try {
        const params = new URLSearchParams()
        params.append('status', 'enabled')
        if (campusId) params.append('campusId', campusId)
        const res = await fetch(`/api/class/info/list?${params.toString()}`)
        const json = await res.json()
        let data = []
        if (json && json.code === 200) {
          const d = json.data
          if (Array.isArray(d)) data = d
          else if (d && Array.isArray(d.items)) data = d.items
        }
        this.classOptions = data
      } catch (e) { this.classOptions = [] }
    },
    splitStrToArr(str) {
      if (!str) return []
      const arr = String(str).split(/[,，;；\s]+/).map(s => s.trim()).filter(Boolean)
      return Array.from(new Set(arr))
    },
    async ensureSubjectsInDict(arr) {
      const existing = new Set(this.subjectOptions.map(s => s.name))
      for (const name of arr) {
        if (!existing.has(name)) {
          try {
            await fetch('/api/dict/subject/save', {
              method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ name, status: 'enabled' })
            })
          } catch (e) {}
        }
      }
      await this.loadSubjectOptions()
    },
    async ensureClassesInDict(arr) {
      const existing = new Set(this.classOptions.map(s => s.name))
      for (const name of arr) {
        if (!existing.has(name)) {
          try {
            await fetch('/api/class/save', {
              method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ name, status: 'enabled' })
            })
          } catch (e) {}
        }
      }
      await this.loadClassOptions()
    },
    async loadDeptOptions(campusId) {
      try {
        const qs = campusId ? `?campusId=${campusId}` : ''
        const res = await fetch(`/api/base/dept/tree${qs}`)
        const json = await res.json()
        if (json && json.code === 200) {
          const tree = json.data || []
          this.deptTree = tree
          // 平铺用于下拉选择
          const flat = []
          const walk = nodes => nodes.forEach(n => { flat.push({ id: n.id, title: n.title }); if (n.children && n.children.length) walk(n.children) })
          walk(tree)
          this.flatDepts = flat
        } else {
          this.deptTree = []
          this.flatDepts = []
        }
      } catch (e) {
        this.deptTree = []
        this.flatDepts = []
      }
    },

    // 统一规范后端返回或表格记录的字段与类型，避免选择框与日期控件不显示
    normalizeTeacherRecord(record) {
      const r = { ...record }
      // 校区 ID 统一为数字
      if (r.campusId != null) {
        const n = Number(r.campusId)
        r.campusId = Number.isNaN(n) ? r.campusId : n
      }
      // 课时费统一为数字
      if (r.hourlyRate != null) {
        const n = Number(r.hourlyRate)
        r.hourlyRate = Number.isNaN(n) ? r.hourlyRate : n
      }
      // 入职日期：保持字符串 YYYY-MM-DD，若为时间戳则转换
      if (r.joinDate != null) {
        if (typeof r.joinDate === 'number') {
          try {
            const d = new Date(r.joinDate)
            const y = d.getFullYear()
            const m = String(d.getMonth() + 1).padStart(2, '0')
            const day = String(d.getDate()).padStart(2, '0')
            r.joinDate = `${y}-${m}-${day}`
          } catch (e) {}
        }
      }
      return r
    },

    async suggestHourlyRate() {
      try {
        const level = this.teacherModal.form.level
        if (!level) return
        const campusId = this.teacherModal.form.campusId
        const params = new URLSearchParams()
        params.append('teacherLevel', level)
        if (campusId) params.append('campusId', campusId)
        const res = await fetch(`/api/teacher/fee/rule/suggest?${params.toString()}`)
        const json = await res.json()
        if (json && json.code === 200 && json.data && json.data.baseFeePerLesson != null) {
          // 若用户尚未填写或为 0，则自动带出建议值
          const current = this.teacherModal.form.hourlyRate
          if (current == null || Number(current) === 0) {
            this.teacherModal.form.hourlyRate = Number(json.data.baseFeePerLesson)
          }
        }
      } catch (e) {
        // 兜底映射
        const map = { '初级': 100, '中级': 120, '高级': 150 }
        const current = this.teacherModal.form.hourlyRate
        if (current == null || Number(current) === 0) {
          this.teacherModal.form.hourlyRate = map[this.teacherModal.form.level] || 120
        }
      }
    },

    openRateModal(record) {
      this.rateModal.visible = true
      this.rateModal.form = { id: record.id, hourlyRate: record.hourlyRate }
    },
    closeRateModal() { this.rateModal.visible = false },
    async saveRate() {
      const { id, hourlyRate } = this.rateModal.form
      try {
        const res = await fetch(`/api/teacher/update/${id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ hourlyRate }) })
        const json = await res.json()
        if (json && json.code === 200) {
          this.closeRateModal(); this.loadTeachers(); if (this.$message) this.$message.success('课时费已更新')
        } else { throw new Error('更新失败') }
      } catch (e) {
        this.closeRateModal(); this.loadTeachers(); if (this.$message) this.$message.error('更新失败，已回滚至列表')
      }
    },

    async removeTeacher(record) {
      if (!record || typeof record.id !== 'number') { this.$message && this.$message.error('删除失败或已取消'); return }
      const doDelete = async () => {
        const res = await fetch(`/api/teacher/delete/${record.id}`, { method: 'DELETE' })
        const json = await res.json()
        if (json && json.code === 200) {
          this.$message && this.$message.success('已删除')
          await this.loadTeachers()
        } else {
          throw new Error('删除失败')
        }
      }
      this.$confirm({
        title: '确认删除该教师？',
        content: `删除后不可恢复：${record.name}`,
        onOk: () => new Promise(async (resolve) => { try { await doDelete(); resolve(); } catch (e) { this.$message && this.$message.error('删除失败或已取消'); resolve(); } })
      })
    },

    async toggleStatus(record) {
      if (!record || typeof record.id !== 'number' || typeof record.status === 'undefined') {
        if (this.$message) this.$message.warning('当前行数据无效，无法切换状态')
        return
      }
      const next = record.status === 'enabled' ? 'disabled' : 'enabled'
      try {
        const res = await fetch(`/api/teacher/update/${record.id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ status: next }) })
        const json = await res.json()
        if (json && json.code === 200) {
          record.status = next
          if (this.$message) this.$message.success('状态已更新')
        } else {
          record.status = next
          if (this.$message) this.$message.warning('状态更新接口不可用，已在前端切换')
        }
      } catch (e) {
        record.status = next
        if (this.$message) this.$message.error('状态更新失败，已在前端切换')
      }
    }
    , beforeUploadTeacherExcel(file) {
      const fd = new FormData()
      fd.append('file', file)
      fetch('/api/teacher/import/excel', { method: 'POST', body: fd }).then(r=>r.json()).then(json => {
        if (json && json.code === 200) {
          this.$message && this.$message.success(`导入成功 ${json.data.success} 条，失败 ${json.data.failed} 条`)
          if (json.data.errors && json.data.errors.length) {
            console.warn('导入错误', json.data.errors)
          }
          this.loadTeachers()
        } else {
          this.$message && this.$message.error('导入失败')
        }
      }).catch(()=>{ this.$message && this.$message.error('导入请求失败') })
      return false
    }
    , async enableAll(){
      if (!Array.isArray(this.list) || !this.list.length) return
      try {
        // 优先调用后端批量启用；若不可用则逐条更新
        let ok = false
        try {
          const r = await fetch('/api/teacher/status/enable-all', { method: 'POST' })
          const j = await r.json().catch(()=>null)
          ok = !!(j && (j.code===200 || j.status===200))
        } catch(e) { ok = false }
        if (!ok) {
          const tasks = this.list.filter(t => t && t.id!=null && t.status!=='enabled')
            .map(t => fetch(`/api/teacher/update/${t.id}`, { method: 'PUT', headers: { 'Content-Type':'application/json' }, body: JSON.stringify({ status: 'enabled' }) }))
          await Promise.all(tasks)
        }
        this.$message && this.$message.success('已将全部教师设置为启用')
        await this.loadTeachers()
      } catch(e){ this.$message && this.$message.error('批量启用失败') }
    }
  }
}
</script>

<style>
.teacher-page { background: #fff; padding: 16px; }
</style>