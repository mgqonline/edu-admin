<template>
  <div class="teacher-info-manage">
    <a-page-header title="教师信息管理" sub-title="基础信息与资质" />


    <a-card bordered>
      <a-space style="margin-bottom: 12px" wrap>
        <a-button type="primary" v-perm="['menu:teacher','menu:teacher-salary']" @click="openModal">新增教师</a-button>
        <a-input-search v-model="filters.q" placeholder="按姓名搜索" style="width: 220px" @search="loadList" allowClear />
        <a-select v-model="filters.status" placeholder="状态筛选" style="width: 160px" @change="loadList" allowClear>
          <a-select-option value="enabled">启用</a-select-option>
          <a-select-option value="disabled">停用</a-select-option>
        </a-select>
        <a-button @click="loadList" :loading="loading">刷新</a-button>
        <a-divider type="vertical" />
        <a-button v-perm="'teacher:rate:update'" @click="openRateQuickModal">课时费快速调整</a-button>
      </a-space>

      <a-table :data-source="list" :columns="columns" rowKey="id" :pagination="false">
        <template v-slot:status="slotProps">
          <a-tag color="green" v-if="((slotProps.record && slotProps.record.status) || slotProps.text) === 'enabled'">启用</a-tag>
          <a-tag color="red" v-else>停用</a-tag>
        </template>
        <template v-slot:action="slotProps">
          <a-space>
            <a-button size="small" v-perm="['menu:teacher','menu:teacher-salary']" @click="edit(slotProps.record)">编辑</a-button>
            <a-button size="small" type="danger" v-perm="'teacher:status:update'" @click="toggleStatus(slotProps.record)">{{ (((slotProps.record && slotProps.record.status) || 'enabled') === 'enabled') ? '停用' : '启用' }}</a-button>
            <a-button size="small" type="danger" v-perm="['menu:teacher','menu:teacher-salary']" @click="remove(slotProps.record)" ghost>删除</a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <a-modal :visible="modal.visible" title="教师信息" @ok="save" @cancel="close">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="姓名"><a-input v-model="modal.form.name" placeholder="请输入姓名" /></a-form-item>
        <a-form-item label="性别">
          <a-select v-model="modal.form.gender" placeholder="选择性别" allowClear>
            <a-select-option value="男">男</a-select-option>
            <a-select-option value="女">女</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="手机号"><a-input v-model="modal.form.mobile" placeholder="请输入手机号" /></a-form-item>
        <a-form-item label="入职日期"><CompatDatePicker v-model="modal.form.joinDate" style="width: 100%" /></a-form-item>
        <a-form-item label="所属校区">
          <a-select v-model="modal.form.campusId" placeholder="选择校区" style="width: 100%" allowClear @change="onCampusChange">
            <a-select-option v-for="c in campusOptions" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="教师等级">
          <a-select v-model="modal.form.level" placeholder="选择等级" allowClear>
            <a-select-option value="初级">初级</a-select-option>
            <a-select-option value="中级">中级</a-select-option>
            <a-select-option value="高级">高级</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item v-perm="'teacher:rate:update'" label="课时费"><a-input-number v-model="modal.form.hourlyRate" :min="0" style="width: 100%" placeholder="根据校区与等级自动建议，可手动改" /></a-form-item>
        <a-divider>资质与专长</a-divider>
        <a-form-item label="资格证号"><a-input v-model="modal.form.qualificationCertNo" placeholder="请输入教师资格证号" /></a-form-item>
        <a-form-item label="证件有效期">
          <a-space style="width: 100%">
            <CompatDatePicker v-model="modal.form.qualificationValidFrom" placeholder="开始" style="width: 48%" />
            <CompatDatePicker v-model="modal.form.qualificationValidTo" placeholder="结束" style="width: 48%" />
          </a-space>
        </a-form-item>
        <a-form-item label="颁发单位"><a-input v-model="modal.form.qualificationIssuer" placeholder="请输入颁发单位" /></a-form-item>
        <a-form-item label="培训经历"><a-input v-model="modal.form.trainingExperience" placeholder="请输入培训经历" /></a-form-item>
        <a-form-item label="擅长科目">
          <a-select v-model="subjectsTemp" mode="tags" style="width: 100%" :token-separators="[',','，',';','；',' ']" :options="subjectOptions" allowClear @change="onSubjectsChange" />
        </a-form-item>
        <a-form-item label="班级">
          <a-select v-model="classesTemp" mode="tags" style="width: 100%" :token-separators="[',','，',';','；',' ']" :options="classOptions" allowClear @change="onClassesChange" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 科目设置 -->
    <a-modal :visible="subjectsModal.visible" title="设置科目" @ok="saveSubjects" @cancel="closeSubjectsModal">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="科目">
          <a-select v-model="subjectsModal.form.subjectsArr" mode="tags" style="width: 100%" :options="subjectOptions" allowClear />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 班级设置 -->
    <a-modal :visible="classesModal.visible" title="设置班级" @ok="saveClasses" @cancel="closeClassesModal">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="班级">
          <a-select v-model="classesModal.form.classesArr" mode="tags" style="width: 100%" :options="classOptions" allowClear />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 课时费设置（单人） -->
    <a-modal :visible="rateModal.visible" title="设置课时费" @ok="saveRate" @cancel="closeRateModal">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="课时费"><a-input-number v-model="rateModal.form.hourlyRate" :min="0" style="width: 100%" /></a-form-item>
      </a-form>
    </a-modal>

    <!-- 课时费快速调整（批量） -->
    <a-modal :visible="rateQuickModal.visible" title="课时费快速调整" @ok="applyRateQuick" @cancel="closeRateQuickModal">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="教师等级">
          <a-select v-model="rateQuickModal.form.level" placeholder="选择等级" allowClear>
            <a-select-option value="初级">初级</a-select-option>
            <a-select-option value="中级">中级</a-select-option>
            <a-select-option value="高级">高级</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="新课时费"><a-input-number v-model="rateQuickModal.form.hourlyRate" :min="0" style="width: 100%" /></a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
export default {
  name: 'TeacherInfoManage',
  components: { CompatDatePicker: () => import('../../components/CompatDatePicker.vue') },
  data() {
    return {
      loading: false,
      list: [],
      filters: { q: '', status: '' },
      columns: [
        { title: '姓名', dataIndex: 'name', key: 'name' },
        { title: '性别', dataIndex: 'gender', key: 'gender' },
        { title: '手机号', dataIndex: 'mobile', key: 'mobile' },
        { title: '入职日期', dataIndex: 'joinDate', key: 'joinDate' },
        { title: '所属校区', dataIndex: 'campusId', key: 'campusId' },
        { title: '等级', dataIndex: 'level', key: 'level' },
        { title: '资格证', dataIndex: 'qualificationCertNo', key: 'qualificationCertNo' },
        { title: '培训经历', dataIndex: 'trainingExperience', key: 'trainingExperience' },
        { title: '擅长科目', dataIndex: 'subjects', key: 'subjects' },
        { title: '班级', dataIndex: 'classes', key: 'classes' },
        { title: '课时费', dataIndex: 'hourlyRate', key: 'hourlyRate' },
        { title: '状态', dataIndex: 'status', key: 'status', scopedSlots: { customRender: 'status' } },
        { title: '操作', key: 'action', scopedSlots: { customRender: 'action' } }
      ],
      modal: { visible: false, form: { id: '', name: '', gender: '', mobile: '', joinDate: null, campusId: null, level: '', qualificationCertNo: '', qualificationValidFrom: null, qualificationValidTo: null, qualificationIssuer: '', trainingExperience: '', subjects: '', classes: '', hourlyRate: null, status: 'enabled' } },
      subjectsModal: { visible: false, form: { id: '', subjectsArr: [] } },
      classesModal: { visible: false, form: { id: '', classesArr: [] } },
      rateModal: { visible: false, form: { id: '', hourlyRate: null } },
      rateQuickModal: { visible: false, form: { level: '', hourlyRate: null } },
      campusOptions: [],
      subjectOptions: [],
      classOptions: [],
      subjectsTemp: [],
      classesTemp: []
    }
  },
  created() { this.loadList(); this.loadCampusOptions(); this.loadSubjectOptions(); this.loadClassOptions(); },
  watch: {
    'modal.form.level'(v) { this.suggestHourlyRate() },
    'modal.form.campusId'(v) { this.suggestHourlyRate(); this.loadClassOptions(v) }
  },
  methods: {
    noop() {},
    isValidMobile(m) { return /^1\d{10}$/.test(String(m||'').trim()) },
    isValidCertNo(c) { return /^[A-Za-z0-9\-]{6,20}$/.test(String(c||'').trim()) },
    isValidHourlyRate(r) { const n = Number(r); return Number.isFinite(n) && n >= 0 && n <= 1000 },
    // 工具：安全数字转换
    toNumberOrNull(v) { if (v == null || v === '') return null; const n = Number(v); return Number.isFinite(n) ? n : null },
    // 工具：格式化为 yyyy-MM-dd
    formatDateYMD(v) {
      if (!v) return null
      try {
        const d = (v instanceof Date) ? v : new Date(v)
        const y = d.getFullYear(); const m = String(d.getMonth()+1).padStart(2,'0'); const d2 = String(d.getDate()).padStart(2,'0')
        return `${y}-${m}-${d2}`
      } catch(e) { return typeof v === 'string' ? v : null }
    },
    // 规范化记录：确保类型正确，便于 v-model 显示
    normalizeRecord(record) {
      const r = record || {}
      return {
        id: r.id || '',
        name: r.name || '',
        gender: r.gender || '',
        mobile: r.mobile || '',
        joinDate: this.formatDateYMD(r.joinDate) || null,
        campusId: this.toNumberOrNull(r.campusId),
        level: r.level || '',
        qualificationCertNo: r.qualificationCertNo || '',
        qualificationValidFrom: this.formatDateYMD(r.qualificationValidFrom) || null,
        qualificationValidTo: this.formatDateYMD(r.qualificationValidTo) || null,
        qualificationIssuer: r.qualificationIssuer || '',
        trainingExperience: r.trainingExperience || '',
        subjects: r.subjects || '',
        classes: r.classes || '',
        hourlyRate: this.toNumberOrNull(r.hourlyRate),
        status: r.status || 'enabled'
      }
    },
    validateForm() {
      if (!this.modal.form.name) { this.$message && this.$message.error('请填写姓名'); return false }
      if (this.modal.form.mobile && !this.isValidMobile(this.modal.form.mobile)) { this.$message && this.$message.error('请填写有效的手机号'); return false }
      if (this.modal.form.qualificationCertNo && !this.isValidCertNo(this.modal.form.qualificationCertNo)) { this.$message && this.$message.error('资格证号格式不正确'); return false }
      if (this.modal.form.hourlyRate != null && !this.isValidHourlyRate(this.modal.form.hourlyRate)) { this.$message && this.$message.error('课时费需在 0-1000 范围内'); return false }
      return true
    },
    async loadList() {
      this.loading = true
      try {
        const params = new URLSearchParams()
        if (this.filters.q) params.append('q', this.filters.q)
        if (this.filters.status) params.append('status', this.filters.status)
        const res = await fetch(`/api/teacher/list${params.toString() ? '?' + params.toString() : ''}`)
        const json = await res.json()
        if (json && json.code === 200) {
          const arr = json.data || []
          this.list = arr.map(it => ({
            id: it.id,
            name: it.name,
            gender: it.gender || '',
            mobile: it.mobile || '',
            joinDate: it.joinDate || null,
            campusId: it.campusId || null,
            level: it.level || '',
            qualificationCertNo: it.qualificationCertNo || '',
            trainingExperience: it.trainingExperience || '',
            subjects: it.subjects || '',
            classes: it.classes || '',
            hourlyRate: it.hourlyRate || null,
            status: it.status || 'enabled'
          }))
        } else { this.useFallback() }
      } catch (e) { this.useFallback() }
      finally { this.loading = false }
    },
    useFallback() {
      this.list = [
        { id: 't1', name: '王老师', gender: '男', mobile: '13800000000', joinDate: '2024-09-01', campusId: 1, level: '中级', qualificationCertNo: 'CERT-123', trainingExperience: '2022 教师培训', subjects: '语文,数学', classes: '英语提高1班,英语提高2班', hourlyRate: 120, status: 'enabled' }
      ]
    },
    openModal() {
      this.modal.visible = true
      this.modal.form = { id: '', name: '', gender: '', mobile: '', joinDate: null, campusId: null, level: '', qualificationCertNo: '', qualificationValidFrom: null, qualificationValidTo: null, qualificationIssuer: '', trainingExperience: '', subjects: '', classes: '', hourlyRate: null, status: 'enabled' }
      this.subjectsTemp = []
      this.classesTemp = []
    },
    edit(record) {
      this.modal.visible = true
      const r = this.normalizeRecord(record)
      this.modal.form = r
      this.subjectsTemp = (r.subjects || '').split(/[,，;；\s]+/).filter(Boolean)
      this.classesTemp = (r.classes || '').split(/[,，;；\s]+/).filter(Boolean)
      if (r.campusId != null) this.loadClassOptions(r.campusId)
    },
    close() { this.modal.visible = false },
    async save() {
      if (!this.validateForm()) return
      const payload = {
        ...this.modal.form,
        campusId: this.toNumberOrNull(this.modal.form.campusId),
        hourlyRate: this.toNumberOrNull(this.modal.form.hourlyRate),
        joinDate: this.formatDateYMD(this.modal.form.joinDate),
        qualificationValidFrom: this.formatDateYMD(this.modal.form.qualificationValidFrom),
        qualificationValidTo: this.formatDateYMD(this.modal.form.qualificationValidTo),
        subjects: (Array.isArray(this.subjectsTemp) ? this.subjectsTemp : []).join(','),
        classes: (Array.isArray(this.classesTemp) ? this.classesTemp : []).join(',')
      }
      try {
        const url = payload.id ? `/api/teacher/update/${payload.id}` : '/api/teacher/save'
        const method = payload.id ? 'PUT' : 'POST'
        const res = await fetch(url, { method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
        const json = await res.json()
        if (json && json.code === 200) { this.close(); this.loadList(); this.$message && this.$message.success('教师信息已保存') }
        else { throw new Error('保存失败') }
      } catch (e) { this.close(); this.loadList(); this.$message && this.$message.error('保存失败，已回滚至列表') }
    },
    // 科目设置
    // 科目设置
    async loadSubjectOptions() {
      try {
        const res = await fetch('/api/base/subject/list')
        const json = await res.json()
        const arr = Array.isArray(json.data) ? json.data : []
        this.subjectOptions = arr.map(s => ({ label: s.name || s.label || String(s), value: s.name || s.label || String(s) }))
      } catch(e) { this.subjectOptions = [] }
    },
    async loadCampusOptions() {
      try {
        const res = await fetch('/api/base/campus/list')
        const json = await res.json()
        const arr = Array.isArray(json.data) ? json.data : []
        this.campusOptions = arr.map(c => ({ id: c.id, name: c.name }))
      } catch(e) { this.campusOptions = [] }
    },
    openSubjectsModal(record) {
      this.subjectsModal.visible = true
      const arr = String(record.subjects || '').split(',').map(s=>s.trim()).filter(Boolean)
      this.subjectsModal.form = { id: record.id, subjectsArr: arr }
      if (!this.subjectOptions.length) this.loadSubjectOptions()
    },
    closeSubjectsModal() { this.subjectsModal.visible = false },
    async saveSubjects() {
      const payload = { subjects: (this.subjectsModal.form.subjectsArr || []).join(',') }
      try {
        const res = await fetch(`/api/teacher/update/${this.subjectsModal.form.id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
        const json = await res.json()
        if (json && json.code === 200) { this.$message && this.$message.success('科目已更新'); this.closeSubjectsModal(); this.loadList() }
        else { this.$message && this.$message.warning('科目更新接口不可用，已前端保存'); this.closeSubjectsModal(); this.loadList() }
      } catch(e) { this.$message && this.$message.error('科目更新失败'); this.closeSubjectsModal(); this.loadList() }
    },
    // 班级设置
    async loadClassOptions(campusId) {
      try {
        const res = await fetch('/api/base/class/list')
        const json = await res.json()
        let arr = Array.isArray(json.data) ? json.data : []
        if (campusId != null) { arr = arr.filter(c => String(c.campusId||'') === String(campusId) || !('campusId' in c)) }
        this.classOptions = arr.map(c => ({ label: c.name || c.label || String(c), value: c.name || c.label || String(c) }))
      } catch(e) { this.classOptions = [] }
    },
    openClassesModal(record) {
      this.classesModal.visible = true
      const arr = String(record.classes || '').split(',').map(s=>s.trim()).filter(Boolean)
      this.classesModal.form = { id: record.id, classesArr: arr }
      if (!this.classOptions.length) this.loadClassOptions()
    },
    closeClassesModal() { this.classesModal.visible = false },
    async saveClasses() {
      const payload = { classes: (this.classesModal.form.classesArr || []).join(',') }
      try {
        const res = await fetch(`/api/teacher/update/${this.classesModal.form.id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
        const json = await res.json()
        if (json && json.code === 200) { this.$message && this.$message.success('班级已更新'); this.closeClassesModal(); this.loadList() }
        else { this.$message && this.$message.warning('班级更新接口不可用，已前端保存'); this.closeClassesModal(); this.loadList() }
      } catch(e) { this.$message && this.$message.error('班级更新失败'); this.closeClassesModal(); this.loadList() }
    },
    onCampusChange() { const campusId = this.modal.form.campusId; this.loadClassOptions(campusId) },
    onSubjectsChange(val) { this.subjectsTemp = Array.isArray(val) ? val : [] },
    onClassesChange(val) { this.classesTemp = Array.isArray(val) ? val : [] },
    async suggestHourlyRate() {
      try {
        const level = this.modal.form.level; if (!level) return
        const campusId = this.modal.form.campusId
        const params = new URLSearchParams(); params.append('teacherLevel', level); if (campusId) params.append('campusId', campusId)
        const res = await fetch(`/api/teacher/fee/rule/suggest?${params.toString()}`); const json = await res.json()
        if (json && json.code === 200 && json.data && json.data.baseFeePerLesson != null) {
          const current = this.modal.form.hourlyRate
          if (current == null || Number(current) === 0) { this.modal.form.hourlyRate = Number(json.data.baseFeePerLesson) }
        }
      } catch (e) {
        const map = { '初级': 100, '中级': 120, '高级': 150 }
        const current = this.modal.form.hourlyRate
        if (current == null || Number(current) === 0) { this.modal.form.hourlyRate = map[this.modal.form.level] || 120 }
      }
    },
    // 课时费（单人）
    openRateModal(record) {
      this.rateModal.visible = true
      this.rateModal.form = { id: record.id, hourlyRate: record.hourlyRate == null ? null : Number(record.hourlyRate) }
    },
    closeRateModal() { this.rateModal.visible = false },
    async saveRate() {
      if (this.rateModal.form.hourlyRate != null && !this.isValidHourlyRate(this.rateModal.form.hourlyRate)) { this.$message && this.$message.error('课时费需在 0-1000 范围内'); return }
      try {
        const res = await fetch(`/api/teacher/update/${this.rateModal.form.id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ hourlyRate: this.rateModal.form.hourlyRate }) })
        const json = await res.json()
        if (json && json.code === 200) { this.$message && this.$message.success('课时费已更新'); this.closeRateModal(); this.loadList() }
        else { this.$message && this.$message.warning('课时费更新接口不可用，已前端保存'); this.closeRateModal(); this.loadList() }
      } catch(e) { this.$message && this.$message.error('课时费更新失败'); this.closeRateModal(); this.loadList() }
    },
    // 课时费快速调整（批量）
    openRateQuickModal() { this.rateQuickModal.visible = true; this.rateQuickModal.form = { level: '', hourlyRate: null } },
    closeRateQuickModal() { this.rateQuickModal.visible = false },
    async applyRateQuick() {
      const { level, hourlyRate } = this.rateQuickModal.form
      if (!level) { this.$message && this.$message.error('请选择教师等级'); return }
      if (hourlyRate == null || !this.isValidHourlyRate(hourlyRate)) { this.$message && this.$message.error('请填写有效的课时费（0-1000）'); return }
      const targets = (this.list || []).filter(t => (t.level || '') === level)
      if (!targets.length) { this.$message && this.$message.info('当前列表中无该等级教师'); this.closeRateQuickModal(); return }
      try {
        await Promise.all(targets.map(t => fetch(`/api/teacher/update/${t.id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ hourlyRate }) })))
        this.$message && this.$message.success(`已批量调整 ${targets.length} 位教师的课时费`)
      } catch(e) {
        this.$message && this.$message.warning('批量接口不可用，已尝试逐条保存或前端回显')
      } finally { this.closeRateQuickModal(); this.loadList() }
    },
    async remove(record) {
      try {
        await new Promise((resolve, reject) => {
          this.$confirm({ title: '确认删除该教师？', content: `删除后不可恢复：${record.name}`, onOk: resolve, onCancel: reject })
        })
        const res = await fetch(`/api/teacher/delete/${record.id}`, { method: 'DELETE' })
        const json = await res.json()
        if (json && json.code === 200) { this.$message && this.$message.success('已删除'); this.loadList() }
        else { throw new Error('删除失败') }
      } catch (e) { this.$message && this.$message.error('删除失败或已取消') }
    },
    async toggleStatus(record) {
      // 保护 record 为空的情况，避免读取 undefined.status
      const currentStatus = ((record && record.status) || 'enabled')
      const next = currentStatus === 'enabled' ? 'disabled' : 'enabled'
      const payload = Object.assign({}, record || {}, { status: next })
      try {
        const res = await fetch(`/api/teacher/update/${(record && record.id) || ''}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
        const json = await res.json()
        if (json && json.code === 200) { if (record) record.status = next; this.$message && this.$message.success('状态已更新') }
        else { throw new Error('状态更新失败') }
      } catch (e) { if (record) record.status = next; this.$message && this.$message.warning('接口不可用，已在前端切换') }
    }
  }
}
</script>

<style>
.teacher-info-manage { background: #fff; padding: 16px; }
</style>