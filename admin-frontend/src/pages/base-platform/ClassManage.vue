<template>
  <div class="class-dict-page">
    <a-page-header title="班级管理" sub-title="维护班级全量信息（标识、属性、人员、资源、状态）" />

    <a-card bordered>
      <a-space direction="vertical" style="margin-bottom: 12px; width: 100%">
       
        <a-space wrap>
          <a-input-search v-model="filters.q" placeholder="按名称关键词搜索" style="width: 220px" @search="loadData" allowClear />
          <a-select v-model="filters.campusId" placeholder="学校筛选" style="width: 200px" @change="onFilterCampusChange" allowClear>
            <a-select-option v-for="c in campusOptions" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
          </a-select>
          <a-select v-model="filters.gradeId" placeholder="年级筛选" style="width: 180px" @change="loadData" allowClear>
            <a-select-option v-for="g in gradeOptions" :key="g.id" :value="g.id">{{ g.name }}</a-select-option>
          </a-select>
          <a-select v-model="filters.status" placeholder="状态筛选" style="width: 160px" @change="loadData" allowClear>
            <a-select-option value="enabled">启用</a-select-option>
            <a-select-option value="disabled">禁用</a-select-option>
          </a-select>
          <a-select v-model="filters.subject" placeholder="科目筛选" style="width: 180px" @change="loadData" allowClear>
            <a-select-option v-for="s in subjectOptions" :key="s.id" :value="s.id">{{ s.name }}</a-select-option>
          </a-select>
          <a-button @click="loadData" :loading="loading">刷新</a-button>
        </a-space>
        <br></br>
        <a-space wrap>
          <a-button type="primary" @click="openAdd">新增班级</a-button>
          <a-button @click="exportXlsx" :loading="loadingExport">导出xlsx</a-button>
        </a-space>
      </a-space>

      <a-table :data-source="list" :columns="columns" :rowKey="rowKeyMain" :pagination="pagination" @change="onTableChange" :locale="{ emptyText: emptyText }" :scroll="{ x: 'max-content' }" size="small">
        <template slot="campus" slot-scope="text, record, index">
          {{ (record && record.campusName) || '-' }}
        </template>
        <template slot="grade" slot-scope="text, record, index">
          {{ (record && record.gradeName) || '-' }}
        </template>
        <template slot="subject" slot-scope="text, record, index">
          {{ (record && record.subjectName) || '-' }}
        </template>
        <!-- 开课日期格式化显示 -->
        <template slot="startDate" slot-scope="text, record, index">
          {{ formatDate(text) }}
        </template>
        <!-- 结课日期格式化显示 -->
        <template slot="endDate" slot-scope="text, record, index">
          {{ formatDate(text) }}
        </template>
        <template slot="state" slot-scope="text, record, index">
          <a-tag :color="stateTagColor(text)">
            {{ stateLabel(text) }}
          </a-tag>
        </template>
        <template slot="tags" slot-scope="text, record, index">
          <a-space wrap>
            <a-tag v-for="t in normalizeTags(text)" :key="t">{{ t }}</a-tag>
            <span v-if="!normalizeTags(text).length">-</span>
          </a-space>
        </template>
        <template slot="status" slot-scope="text, record, index">
          <a-tag :color="text === 'enabled' ? 'green' : 'red'">
            {{ text === 'enabled' ? '启用' : '禁用' }}
          </a-tag>
        </template>
        <template slot="action" slot-scope="text, record, index">
          <div class="action-cell-wrap">
            <!-- 主按钮始终可见，悬停显示更多 -->
            <a-dropdown placement="bottomRight" :trigger="['hover']">
              <a-button size="small" :disabled="!record" @click="openEdit(record)">编辑</a-button>
              <a-menu slot="overlay">
                <a-menu-item>
                  <a @click="openDetail(record)">详情</a>
                </a-menu-item>
                <a-menu-item :disabled="!canArrange(record)">
                  <a :class="['action-link', { 'action-link-disabled': !canArrange(record) }]" @click="canArrange(record) && goArrange(record)">排课</a>
                </a-menu-item>
                <a-menu-item :disabled="!canReschedule(record)">
                  <a :class="['action-link', { 'action-link-disabled': !canReschedule(record) }]" @click="canReschedule(record) && goArrange(record, true)">重新排课</a>
                </a-menu-item>
                <a-menu-item>
                  <a @click="goSchedule(record)">课表</a>
                </a-menu-item>
                <a-menu-item>
                  <a @click="viewStudents(record)">学生</a>
                </a-menu-item>
                <a-menu-item :disabled="!canDelete(record)">
                  <template v-if="canDelete(record)">
                    <a-popconfirm title="确认删除该班级？" @confirm="deleteItem(record)">
                      <a class="action-delete">删除</a>
                    </a-popconfirm>
                  </template>
                  <template v-else>
                    <a class="action-delete action-delete-disabled">删除</a>
                  </template>
                </a-menu-item>
                <a-menu-item>
                  <a @click="toggleStatus(record)">{{ record && record.status === 'enabled' ? '禁用' : '启用' }}</a>
                </a-menu-item>
              </a-menu>
            </a-dropdown>
          </div>
        </template>
      </a-table>
    </a-card>
    <a-drawer :visible="detail.visible" title="班级详情" width="520" @close="detail.visible=false">
      <a-descriptions bordered size="small" :column="1">
        <a-descriptions-item label="班级ID">{{ detail.item.id || '-' }}</a-descriptions-item>
        <a-descriptions-item label="名称">{{ detail.item.name || '-' }}</a-descriptions-item>
        <a-descriptions-item label="学校/校区">{{ campusNameById(detail.item.campusId) || '-' }}</a-descriptions-item>
        <a-descriptions-item label="年级">{{ gradeNameById(detail.item.gradeId) || '-' }}</a-descriptions-item>
        <a-descriptions-item label="授课模式">{{ detail.item.mode || '-' }}</a-descriptions-item>
        <a-descriptions-item label="开班/结课">{{ (detail.item.startDate||'-') + ' / ' + (detail.item.endDate||'-') }}</a-descriptions-item>
        <a-descriptions-item label="当前学期">{{ detail.item.term || '-' }}</a-descriptions-item>
        <a-descriptions-item label="班级状态">{{ stateLabel(detail.item.state) }}</a-descriptions-item>
        <a-descriptions-item label="排课状态">
          <a-tag :color="scheduleTagColor(detail.item)">{{ scheduleStatusLabel(detail.item) }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="班主任">{{ detail.item.headTeacherNames || '-' }}</a-descriptions-item>
        <a-descriptions-item label="主教室">{{ detail.item.classroom || '-' }}</a-descriptions-item>
        <a-descriptions-item label="班额/在读">{{ (detail.item.capacityLimit||'-') + ' / ' + (detail.item.currentCount||'-') }}</a-descriptions-item>
        <a-descriptions-item label="收费">{{ detail.item.feeStandard || '-' }}（{{ detail.item.feeStatus || '未设置' }}）</a-descriptions-item>
        <a-descriptions-item label="标签">{{ Array.isArray(detail.item.tags) ? detail.item.tags.join('、') : (detail.item.tags||'-') }}</a-descriptions-item>
        <a-descriptions-item label="家长群">{{ detail.item.parentGroup || '-' }}</a-descriptions-item>
        <a-descriptions-item label="联系人">{{ detail.item.contacts || '-' }}</a-descriptions-item>
        <a-descriptions-item label="备注">{{ detail.item.note || '-' }}</a-descriptions-item>
      </a-descriptions>
    </a-drawer>
    <a-drawer :visible="schedule.visible" title="班级课表" width="720" @close="schedule.visible=false">
      <a-descriptions size="small" :column="3" style="margin-bottom: 8px">
        <a-descriptions-item label="班级">{{ schedule.meta.className || '-' }}</a-descriptions-item>
        <a-descriptions-item label="班级ID">{{ schedule.meta.classId || '-' }}</a-descriptions-item>
        <a-descriptions-item label="校区/年级">{{ (schedule.meta.campus || '-') + ' / ' + (schedule.meta.grade || '-') }}</a-descriptions-item>
      </a-descriptions>
      <a-table :data-source="schedule.items" :pagination="false" :rowKey="rowKeySchedule" :columns="[
        { title: '星期', dataIndex: 'day', key: 'day' },
        { title: '时间', dataIndex: 'time', key: 'time' },
        { title: '课程', dataIndex: 'course', key: 'course' },
        { title: '任课老师', dataIndex: 'teacher', key: 'teacher' },
        { title: '教室', dataIndex: 'room', key: 'room' }
      ]" />
    </a-drawer>
    <a-drawer :visible="students.visible" title="班级学生" width="720" @close="students.visible=false">
      <a-descriptions size="small" :column="2" style="margin-bottom: 8px">
        <a-descriptions-item label="班级">{{ students.meta.className || '-' }}</a-descriptions-item>
        <a-descriptions-item label="班级ID">{{ students.meta.classId || '-' }}</a-descriptions-item>
      </a-descriptions>
      <a-table :data-source="students.items" :pagination="false" :rowKey="rowKeyStudent" :columns="[
        { title: '序号', dataIndex: 'no', key: 'no' },
        { title: '姓名', dataIndex: 'name', key: 'name' },
        { title: '性别', dataIndex: 'gender', key: 'gender' },
        { title: '状态', dataIndex: 'status', key: 'status' },
        { title: '入班日期', dataIndex: 'joinDate', key: 'joinDate' },
        { title: '联系方式', dataIndex: 'contact', key: 'contact' }
      ]" />
    </a-drawer>
    <a-modal :visible="modal.visible" :title="modalTitle" @ok="saveModal" @cancel="closeModal" :width="800">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="学校/校区">
          <a-select v-model="modal.form.campusId" placeholder="选择学校/校区" @change="onModalCampusChange">
            <a-select-option v-for="c in campusOptions" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="年级">
          <a-select v-model="modal.form.gradeId" placeholder="选择年级" @change="onModalGradeChange">
            <a-select-option v-for="g in gradeOptions" :key="g.id" :value="g.id">{{ g.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="当前学期">
          <a-input v-model="modal.form.term" placeholder="如 2024-2025 第一学期" @focus="onTermFocus" />
        </a-form-item>
        <a-form-item label="科目">
          <a-select v-model="modal.form.subject" placeholder="请选择科目">
            <a-select-option v-for="s in subjectOptions" :key="s.id" :value="s.id">{{ s.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="名称">
          <a-input v-model="modal.form.name" placeholder="请输入班级名称" @focus="onNameFocus" />
        </a-form-item>
        <a-form-item label="授课模式">
          <a-select v-model="modal.form.mode" placeholder="线上/线下">
            <a-select-option value="线上">线上</a-select-option>
            <a-select-option value="线下">线下</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="开班日期">
          <V2SimpleDate v-model="modal.form.startDate" format="YYYY-MM-DD" />
        </a-form-item>
        <a-form-item label="结课日期">
          <V2SimpleDate v-model="modal.form.endDate" format="YYYY-MM-DD" />
        </a-form-item>
       
        <a-form-item label="班级状态">
          <a-select v-model="modal.form.state" placeholder="选择班级状态">
            <a-select-option value="normal">正常</a-select-option>
            <a-select-option value="preparing">筹备中</a-select-option>
            <a-select-option value="paused">暂停</a-select-option>
            <a-select-option value="finished">已结课</a-select-option>
            <a-select-option value="dissolved">已解散</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="班主任">
          <a-select v-model="modal.form.headTeacherIds" mode="multiple" show-search placeholder="选择班主任">
            <a-select-option v-for="t in staffList" :key="t.id" :value="t.id">{{ t.name || t.title || ('#'+t.id) }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="主教室">
          <div style="display:flex; gap:8px">
            <a-select v-model="modal.form.classroomId" show-search allow-clear style="flex:1" :filterOption="true" placeholder="选择教室" @change="onClassroomChange">
              <a-select-option v-for="r in classroomOptions" :key="r.id" :value="r.id">{{ r.name }}（可用座位：{{ r.usableSeats }}）</a-select-option>
            </a-select>
            <a-button @click="$router.push('/base/classroom')">管理教室</a-button>
          </div>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model="modal.form.status">
            <a-select-option value="enabled">启用</a-select-option>
            <a-select-option value="disabled">禁用</a-select-option>
          </a-select>
        </a-form-item>
        
        <a-form-item label="班额限制">
          <a-input-number v-model="modal.form.capacityLimit" :min="1" style="width:100%" />
        </a-form-item>
        <a-form-item label="学费标准">
          <a-input v-model="modal.form.feeStandard" placeholder="如 5000元/学期 或 200元/课时" />
        </a-form-item>
        <a-form-item label="收费状态">
          <a-select v-model="modal.form.feeStatus" placeholder="选择收费状态">
            <a-select-option value="统一收费">已统一收费</a-select-option>
            <a-select-option value="按课时收费">按课时收费</a-select-option>
            <a-select-option value="未设置">未设置</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="班型标签">
          <a-select v-model="modal.form.tags" mode="tags" placeholder="输入或选择标签" style="width:100%">
            <a-select-option value="实验班">实验班</a-select-option>
            <a-select-option value="平行班">平行班</a-select-option>
            <a-select-option value="一对一">一对一</a-select-option>
            <a-select-option value="小班课">小班课</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model="modal.form.note" :rows="3" placeholder="特殊说明" />
        </a-form-item>
        <a-form-item label="家长群信息">
          <a-input v-model="modal.form.parentGroup" placeholder="群号或二维码说明" />
        </a-form-item>
        <a-form-item label="联系人信息">
          <a-input v-model="modal.form.contacts" placeholder="如 家委成员姓名/电话" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
export default {
  name: 'ClassDictPage',
  components: { V2SimpleDate: () => import('../../components/V2SimpleDate.vue') },
      data() {
        return {
      loading: false,
      loadingExport: false,
      list: [],
      pagination: { current: 1, pageSize: 10, total: 0, showSizeChanger: true, pageSizeOptions: ['10','20','50','100'] },
      campusOptions: [],
      staffList: [],
      classroomOptions: [],
      gradeOptions: [],
      subjectOptions: [],
      detail: { visible: false, item: {} },
      schedule: { visible: false, items: [], meta: {} },
      students: { visible: false, items: [], meta: {} },
      filters: { status: '', campusId: null, gradeId: null, q: '', subject: null },
      modal: { visible: false, mode: 'add', form: { id: null, campusId: null, gradeId: null, name: '', mode: '', startDate: null, endDate: null, term: '', state: 'normal', headTeacherIds: [], classroom: '', status: 'enabled', sortOrder: 0, capacityLimit: null, feeStandard: '', feeStatus: '', tags: [], note: '', parentGroup: '', contacts: '' } },
      columns: [
        { title: '学校/校区', dataIndex: 'campusName', key: 'campusName', scopedSlots: { customRender: 'campus' } },
        { title: '年级', dataIndex: 'gradeName', key: 'gradeName', scopedSlots: { customRender: 'grade' } },
        { title: '名称', dataIndex: 'name', key: 'name' },
        { title: '授课模式', dataIndex: 'mode', key: 'mode' },
        { title: '科目', dataIndex: 'subjectName', key: 'subjectName', scopedSlots: { customRender: 'subject' } },
        { title: '主教室', dataIndex: 'classroom', key: 'classroom' },
        { title: '班主任', dataIndex: 'headTeacherNames', key: 'headTeacherNames' },
        { title: '开课日期', dataIndex: 'startDate', key: 'startDate', scopedSlots: { customRender: 'startDate' } },
        { title: '结课日期', dataIndex: 'endDate', key: 'endDate', scopedSlots: { customRender: 'endDate' } },
        { title: '当前学期', dataIndex: 'term', key: 'term' },
        { title: '报班人数', dataIndex: 'currentCount', key: 'currentCount' },
        { title: '班额', dataIndex: 'capacityLimit', key: 'capacityLimit' },
        { title: '班级状态', dataIndex: 'state', key: 'state', scopedSlots: { customRender: 'state' } },
        { title: '学费', dataIndex: 'feeStandard', key: 'feeStandard' },
        { title: '收费状态', dataIndex: 'feeStatus', key: 'feeStatus' },
        { title: '班型', dataIndex: 'tags', key: 'tags', scopedSlots: { customRender: 'tags' } },
        { title: '启用', dataIndex: 'status', key: 'status', scopedSlots: { customRender: 'status' } },
        
        { title: '操作', key: 'action', width: 140, fixed: 'right', className: 'action-cell', scopedSlots: { customRender: 'action' } }
      ]
        }
      },
  computed: {
    modalTitle() { return this.modal.mode === 'edit' ? '编辑班级' : '新增班级' },
    emptyText() {
      const f = this.filters || {}
      const hasFilter = !!(f.q || f.campusId || f.gradeId || f.status)
      return hasFilter ? '没有匹配的班级' : '暂无班级数据'
    }
  },
  created() {
    this.loadCampusOptions().then(async () => {
      await Promise.all([this.loadGradeOptions(), this.loadStaffOptions()])
      await this.loadSubjectOptions()
      this.loadData()
    })
  },
  methods: {
    rowKeySchedule(r, i) { return i },
    rowKeyStudent(r, i) { return i },
    rowKeyMain(row) {
      if (!row) return Math.random().toString(36).slice(2)
      return row.id || (String(row.campusId||'') + '-' + String(row.gradeId||'') + '-' + String(row.name||''))
    },
    onNameFocus() {
      const f = this.modal && this.modal.form ? this.modal.form : null
      if (!f) return
      if (!f.subject && Array.isArray(this.subjectOptions) && this.subjectOptions.length) {
        this.modal.form.subject = this.subjectOptions[0].id
      }
      this.suggestClassName()
    },
    async exportXlsx(){
      this.loadingExport = true
      try {
        const p = new URLSearchParams()
        if (this.filters.status) p.append('status', this.filters.status)
        if (this.filters.campusId) p.append('campusId', String(this.filters.campusId))
        if (this.filters.gradeId) p.append('gradeId', String(this.filters.gradeId))
        if (this.filters.q) p.append('q', this.filters.q)
        const url = '/api/dict/class/export' + (p.toString() ? ('?' + p.toString()) : '')
        const res = await fetch(url, { method: 'GET' })
        if (!res.ok) { this.$message.error('导出失败：' + res.status); return }
        const blob = await res.blob()
        const dispo = res.headers.get('Content-Disposition') || ''
        let filename = 'class_info.xlsx'
        const m = dispo.match(/filename\s*=\s*([^;]+)/i)
        if (m && m[1]) filename = decodeURIComponent(m[1].replace(/"/g, ''))
        const objUrl = window.URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = objUrl
        a.download = filename
        document.body.appendChild(a)
        a.click()
        document.body.removeChild(a)
        window.URL.revokeObjectURL(objUrl)
      } catch (e) {
        console.error(e)
        this.$message.error('导出出现异常')
      } finally { this.loadingExport = false }
    },
    campusNameById(id) {
      const found = this.campusOptions.find(c => String((c && c.id) != null ? c.id : c.campusId) === String(id))
      return found ? found.name : ''
    },
    gradeNameById(id) {
      const arr = Array.isArray(this.gradeOptions) ? this.gradeOptions : []
      const found = arr.find(g => String(((g && g.id) != null ? g.id : g.gradeId)) === String(id))
      return found ? found.name : ''
    },
    stateLabel(s) {
      const map = { normal: '正常', preparing: '筹备中', finished: '已结课', dissolved: '已解散', paused: '暂停' }
      return map[s] || '—'
    },
    stateTagColor(s) {
      const map = { normal: 'green', preparing: 'orange', finished: 'blue', dissolved: 'red', paused: 'gold' }
      return map[s] || 'default'
    },
    formatDate(d) {
      if (!d) return '-'
      try {
        const dt = new Date(d)
        if (isNaN(dt.getTime())) return String(d)
        const y = dt.getFullYear()
        const m = String(dt.getMonth() + 1).padStart(2, '0')
        const day = String(dt.getDate()).padStart(2, '0')
        return `${y}-${m}-${day}`
      } catch (e) { return String(d) }
    },
    normalizeTags(text) {
      if (Array.isArray(text)) return text.filter(Boolean)
      if (typeof text === 'string' && text.trim()) return [text.trim()]
      return []
    },
    async loadCampusOptions() {
      try {
        // 主数据源：基础平台校区列表
        const res = await fetch('/api/base/campus/list')
        const json = await res.json()
        let items = (json && json.code === 200) ? (json.data || []) : []
        // 兜底：系统模块校园列表（campusId）
        if (!items || !items.length) {
          const res2 = await fetch('/api/campus/list')
          const json2 = await res2.json()
          if (json2 && json2.code === 200) {
            const raw = json2.data || []
            items = raw.map(it => ({ id: it.id != null ? it.id : it.campusId, name: it.name }))
          }
        }
        this.campusOptions = items
      } catch (e) { this.campusOptions = [] }
    },
    async loadGradeOptions() {
      try {
        const params = new URLSearchParams()
        const campusId = (this.modal && this.modal.form && this.modal.form.campusId) || this.filters.campusId
        if (campusId) params.append('campusId', campusId)
        // 显示映射需要包含所有状态的年级，移除 status 过滤
        const url = `/api/dict/grade/list${params.toString() ? '?' + params.toString() : ''}`
        const res = await fetch(url)
        const json = await res.json()
        if (json && json.code === 200) {
          const root = json.data || {}
          const arr = Array.isArray(root.items) ? root.items : (Array.isArray(json.data) ? json.data : [])
          this.gradeOptions = arr
        } else {
          this.gradeOptions = []
        }
        // 新增：在弹窗中加载年级后，若未选择年级则默认选第一项，并尝试自动生成名称
        if (this.modal && this.modal.visible && this.modal.form) {
          if (!this.modal.form.gradeId && this.gradeOptions && this.gradeOptions.length) {
            this.modal.form.gradeId = this.gradeOptions[0].id
          }
          if (!this.modal.form.name) {
            this.suggestClassName()
          }
        }
      } catch (e) { this.gradeOptions = [] }
    },
    async loadStaffOptions() {
      try {
        const res = await fetch('/api/base/staff/list?page=1&size=100')
        const json = await res.json()
        if (json && json.code === 200) {
          const root = json.data || {}
          this.staffList = Array.isArray(root.items) ? root.items : (Array.isArray(json.data) ? json.data : [])
        } else { this.staffList = [] }
      } catch (e) { this.staffList = [] }
    },
    async loadSubjectOptions(){
      try {
        const r = await fetch('/api/dict/subject/list')
        const j = await r.json()
        this.subjectOptions = (j && j.code===200) ? (j.data||[]) : []
        if (this.modal && this.modal.visible && this.modal.form) {
          if (!this.modal.form.subject && this.subjectOptions && this.subjectOptions.length) {
            this.modal.form.subject = this.subjectOptions[0].id
          }
        }
      } catch(e){ this.subjectOptions = [] }
    },
    async openDetail(record) {
      const r = record || {}
      // 若该记录尚未补充排课状态，则查一次接口
      if (!r.scheduleStatus) {
        await this.ensureRecordScheduleStatus(r)
      }
      this.detail.item = r
      this.detail.visible = true
    },
    goArrange(record, reschedule = false) {
      if (!record || !record.id) { this.$message.error('无法排课：缺少班级信息'); return }
      const q = {
        classId: String(record.id),
        className: record.name || '',
        mode: record.mode || '',
        // 默认班级类型：班课；若后续有 record.classType 可覆盖
        classType: (record.classType || '').trim() || '班课',
        // 科目：优先班级绑定课程/科目字段，如无则为空
        subject: (record.course || record.subject || '').trim()
      }
      // 若有班主任，作为可能的默认老师传递
      if (record.headTeacherId) q.teacherId = String(record.headTeacherId)
      if (reschedule) q.reschedule = 'true'
      this.$router.push({ path: '/course/schedule', query: q })
    },
    goSchedule(record) {
      const subjects = ['语文','数学','英语','科学','音乐','美术','体育','信息']
      const rooms = ['教学楼201','教学楼302','多媒体101','操场','实验室201']
      const days = ['周一','周二','周三','周四','周五']
      const times = ['08:00-08:45','09:00-09:45','10:00-10:45','11:00-11:45','14:00-14:45','15:00-15:45']
      const pick = (arr) => arr[Math.floor(Math.random() * arr.length)]
      const teacherName = (id) => {
        const t = this.staffList.find(s => String(s.id) === String(id))
        return t ? (t.name || t.title || ('#'+t.id)) : '任课老师'
      }
      const heads = Array.isArray(record.headTeacherIds) ? record.headTeacherIds : []
      const items = []
      for (let d of days) {
        for (let i = 0; i < 4; i++) {
          items.push({
            day: d,
            time: pick(times),
            course: pick(subjects),
            teacher: teacherName(pick(heads.length ? heads : [record.headTeacherId, ''])),
            room: pick(rooms)
          })
        }
      }
      this.schedule.meta = { className: record.name, classId: record.id, campus: this.campusNameById(record.campusId), grade: this.gradeNameById(record.gradeId) }
      this.schedule.items = items
      this.schedule.visible = true
    },
    viewStudents(record) {
      const surnames = ['张','李','王','刘','陈','杨','黄','赵','周','吴','徐','孙']
      const names = ['一','二','三','四','五','六','七','八','九','十','嘉','晨','宇','涵','玥','珺','泽','诺','洋','琪']
      const pick = (arr) => arr[Math.floor(Math.random() * arr.length)]
      const genName = () => pick(surnames) + pick(names) + (Math.random()>0.5?pick(names):'')
      const size = Math.max(12, Math.min(36, Number(record.capacityLimit || 24)))
      const items = Array.from({ length: size }).map((_, idx) => ({
        no: idx + 1,
        name: genName(),
        gender: Math.random() > 0.5 ? '男' : '女',
        status: '在读',
        joinDate: new Date(Date.now() - Math.floor(Math.random()*200)*86400000).toISOString().slice(0,10),
        contact: '1' + String(Math.floor(Math.random()*9)+3) + String(Math.floor(100000000+Math.random()*899999999))
      }))
      this.students.meta = { className: record.name, classId: record.id }
      this.students.items = items
      this.students.visible = true
    },
    openBatchImport() {
      console.debug('openBatchImport')
      this.$message && this.$message.info('批量导入：功能占位，待接入')
    },
    openBatchAdjustGrade() {
      console.debug('openBatchAdjustGrade')
      this.$message && this.$message.info('批量年级调整：功能占位，待接入')
    },
    openBatchAdjustStatus() {
      console.debug('openBatchAdjustStatus')
      this.$message && this.$message.info('批量状态调整：功能占位，待接入')
    },
    onModalCampusChange() { this.loadGradeOptions(); this.loadClassroomOptions() },
    async loadClassroomOptions(){
      try {
        const p = new URLSearchParams(); const campusId = (this.modal&&this.modal.form&&this.modal.form.campusId) || this.filters.campusId; if (campusId) p.append('campusId', campusId)
        const r = await fetch('/api/base/classroom/list' + (p.toString()?('?'+p.toString()):''))
        const j = await r.json()
        if (j && j.code===200) {
          const root = j.data || {}
          const arr = Array.isArray(root.items) ? root.items : (Array.isArray(j.data) ? j.data : [])
          this.classroomOptions = arr
          if (this.modal && this.modal.visible && this.modal.form) {
            const hasId = !!this.modal.form.classroomId
            const hasText = !!(this.modal.form.classroom && String(this.modal.form.classroom).trim())
            if (!hasId && hasText) {
              const found = (this.classroomOptions||[]).find(it => String(it.name) === String(this.modal.form.classroom))
              if (found) this.modal.form.classroomId = found.id
            }
          }
        } else {
          this.classroomOptions = []
        }
      } catch(e){ this.classroomOptions = [] }
    },
    onClassroomChange(id){
      const found = (this.classroomOptions||[]).find(it => String(it.id)===String(id))
      if(found){
        // 将教室名称写入原有字符串字段，保持后端兼容
        this.modal.form.classroom = found.name
        // 若班额未设置，默认等于可用座位，避免超过教室承载
        if(!this.modal.form.capacityLimit || Number(this.modal.form.capacityLimit) <= 0){
          this.modal.form.capacityLimit = Number(found.usableSeats || found.capacity || 0)
        }
      }
    },
    onFilterCampusChange() { this.loadGradeOptions(); this.loadData() },
    async loadData() {
      this.loading = true
      try {
        const params = new URLSearchParams()
        if (this.filters.campusId) params.append('campusId', this.filters.campusId)
        if (this.filters.gradeId) params.append('gradeId', this.filters.gradeId)
        if (this.filters.status) params.append('status', this.filters.status)
        if (this.filters.q) params.append('q', this.filters.q)
        if (this.filters.subject) params.append('subject', this.filters.subject)
        params.append('page', String(this.pagination.current))
        params.append('size', String(this.pagination.pageSize))
        const res = await fetch(`/api/class/list${'?' + params.toString()}`)
        const json = await res.json()
        if (json && json.code === 200) {
          const root = json.data || {}
          const raw = Array.isArray(root.items) ? root.items : (Array.isArray(json.data) ? json.data : [])
          this.list = raw.map(it => {
            console.log(it)
            const heads = Array.isArray(it.headTeacherIds) ? it.headTeacherIds : []
            const headTeacherNames = heads.map(id => {
              const staffArr = Array.isArray(this.staffList) ? this.staffList : []
              const t = staffArr.find(s => String(s.id) === String(id))
              return t ? (t.name || t.title || ('#'+t.id)) : ('#'+id)
            }).join('、')
            const currentCount = Array.isArray(it.students) ? it.students.filter(s => s && s.status !== '退学').length : (it.currentCount || 0)
            const campusName = it.campusName || this.campusNameById(it.campusId)
            const gradeName = it.gradeName || this.gradeNameById(it.gradeId)
            const subjectName = it.subjectName || (((this.subjectOptions||[]).find(s => String(s.id)===String(it.subject))||{}).name) || ''
            return { ...it, headTeacherNames, currentCount, campusName, gradeName, subjectName }
          })
          this.pagination = { ...this.pagination, total: Number(root.total || raw.length || 0) }
          await this.enrichScheduleStatuses()
        } else {
          this.list = []
        }
      } catch (e) {
        this.list = []
      } finally {
        this.loading = false
      }
    },
    async ensureRecordScheduleStatus(r) {
      if (!r || !r.id) return
      try {
        const res = await fetch(`/api/schedule/query/class?classId=${r.id}`)
        const json = await res.json()
        const items = (json && json.code === 200) ? (json.data || []) : []
        const hasSchedule = items.length > 0
        const now = new Date()
        const ed = r.endDate ? new Date(r.endDate) : null
        const ended = ed && !isNaN(ed.getTime()) && now > ed
        r.scheduleStatus = ended ? '已结课' : (hasSchedule ? '已排课' : '未排课')
      } catch (e) { r.scheduleStatus = '未排课' }
    },
    async enrichScheduleStatuses() {
      const now = new Date()
      const tasks = (this.list || []).map(async (r) => {
        try {
          const res = await fetch(`/api/schedule/query/class?classId=${r.id}`)
          const json = await res.json()
          const items = (json && json.code === 200) ? (json.data || []) : []
          const hasSchedule = items.length > 0
          let status = '未排课'
          const ed = r.endDate ? new Date(r.endDate) : null
          const ended = ed && !isNaN(ed.getTime()) && now > ed
          if (ended) status = '已结课'
          else if (hasSchedule) status = '已排课'
          r.scheduleStatus = status
        } catch (e) {
          r.scheduleStatus = '未排课'
        }
      })
      await Promise.all(tasks)
    },
    scheduleStatusLabel(record) {
      const r = record || {}
      if (r.scheduleStatus) return r.scheduleStatus
      // 兜底：仅根据结课日期推断
      try {
        const now = new Date()
        const ed = r.endDate ? new Date(r.endDate) : null
        if (ed && !isNaN(ed.getTime()) && now > ed) return '已结课'
      } catch (e) {}
      return '未排课'
    },
    scheduleTagColor(record) {
      const st = this.scheduleStatusLabel(record)
      const map = { '未排课': 'default', '已排课': 'green', '已结课': 'blue' }
      return map[st] || 'default'
    },
    canArrange(record) {
      return this.scheduleStatusLabel(record) === '未排课'
    },
    canReschedule(record) {
      return this.scheduleStatusLabel(record) === '已排课'
    },
    canDelete(record) {
      // 仅未排课允许删除；已排课和已结课均禁用删除
      return this.scheduleStatusLabel(record) === '未排课'
    },
    openAdd() {
      this.modal.mode = 'add'
      this.modal.form = { id: null, campusId: this.filters.campusId || (this.campusOptions[0] && this.campusOptions[0].id) || null, gradeId: this.filters.gradeId || null, name: '', mode: '', startDate: null, endDate: null, term: this.autoTerm(), state: 'normal', headTeacherIds: [], classroom: '', status: 'enabled', sortOrder: 0, capacityLimit: null, feeStandard: '', feeStatus: '', tags: [], note: '', parentGroup: '', contacts: '', subject: null }
      this.modal.visible = true
      this.loadGradeOptions()
      this.loadStaffOptions()
      this.loadClassroomOptions()
      this.loadSubjectOptions().then(() => {
        if (!this.modal.form.subject && Array.isArray(this.subjectOptions) && this.subjectOptions.length) {
          this.modal.form.subject = this.subjectOptions[0].id
        }
        if (!this.modal.form.name || !String(this.modal.form.name).trim()) {
          this.suggestClassName()
        }
      })
    },
    openEdit(record) {
      console.log(!record,!record.id)
      if (!record || !record.id) {
        console.warn('[openEdit] invalid record:', record)
        this.$message && this.$message.warning('当前行数据无效，无法编辑')
        return
      }
      this.modal.mode = 'edit'
      this.modal.form = { id: record.id, campusId: record.campusId, gradeId: record.gradeId, name: record.name, mode: record.mode || '', startDate: record.startDate || null, endDate: record.endDate || null, term: record.term || '', state: record.state || 'normal', headTeacherIds: Array.isArray(record.headTeacherIds) ? record.headTeacherIds : [], classroom: record.classroom || '', classroomId: record.classroomId || null, status: record.status || 'enabled', capacityLimit: record.capacityLimit || null, feeStandard: record.feeStandard || '', feeStatus: record.feeStatus || '', tags: Array.isArray(record.tags) ? record.tags : [], note: record.note || '', parentGroup: record.parentGroup || '', contacts: record.contacts || '', subject: record.subject || null }
      this.loadGradeOptions()
      this.loadStaffOptions()
      this.loadClassroomOptions()
      this.loadSubjectOptions()
      this.modal.visible = true
    },
    closeModal() { this.modal.visible = false },
    async saveModal() {
      const f = this.modal.form
      if (!f.campusId) { this.$message && this.$message.warning('请选择学校/校区'); return }
      if (!f.gradeId) { this.$message && this.$message.warning('请选择年级'); return }
      if (!f.name) { this.$message && this.$message.warning('请输入班级名称'); return }
      // 不再维护自定义班级编号，统一使用系统生成的 id
      // 日期顺序校验
      if (f.startDate && f.endDate) {
        const sd = new Date(f.startDate).getTime()
        const ed = new Date(f.endDate).getTime()
        if (!isNaN(sd) && !isNaN(ed) && sd > ed) { this.$message && this.$message.warning('结课日期不能早于开班日期'); return }
      }
      // 容量校验
      if (f.capacityLimit != null) {
        const cap = Number(f.capacityLimit)
        if (isNaN(cap) || cap <= 0) { this.$message && this.$message.warning('班额限制必须为正数'); return }
      }
      // 选定教室时，班额不得超过教室可用座位数
      if (f.classroomId) {
        const found = (this.classroomOptions||[]).find(it => String(it.id)===String(f.classroomId))
        if (found) {
          const seats = Number(found.usableSeats || found.capacity || 0)
          if (f.capacityLimit != null && Number(f.capacityLimit) > seats) {
            this.$message && this.$message.warning(`班额(${f.capacityLimit})不能大于教室可用座位(${seats})`)
            return
          }
        }
      }
      try {
        if (this.modal.mode === 'add') {
          const res = await fetch('/api/dict/class/save', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ campusId: f.campusId, gradeId: f.gradeId, name: f.name, mode: f.mode, startDate: f.startDate, endDate: f.endDate, term: f.term, state: f.state, headTeacherIds: f.headTeacherIds, classroom: f.classroom, status: f.status, capacityLimit: f.capacityLimit, feeStandard: f.feeStandard, feeStatus: f.feeStatus, tags: f.tags, note: f.note, parentGroup: f.parentGroup, contacts: f.contacts, classroomId: f.classroomId || null, subject: f.subject || null }) })
          const json = await res.json()
          if (json && json.code === 200) {
            this.$message && this.$message.success('新增成功')
            this.modal.visible = false
            this.loadData()
          } else {
            const msg = (json && json.msg) ? json.msg : '新增失败'
            throw new Error(msg)
          }
        } else {
          const res = await fetch(`/api/dict/class/update/${f.id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ campusId: f.campusId, gradeId: f.gradeId, name: f.name, mode: f.mode, startDate: f.startDate, endDate: f.endDate, term: f.term, state: f.state, headTeacherIds: f.headTeacherIds, classroom: f.classroom, status: f.status, capacityLimit: f.capacityLimit, feeStandard: f.feeStandard, feeStatus: f.feeStatus, tags: f.tags, note: f.note, parentGroup: f.parentGroup, contacts: f.contacts, classroomId: f.classroomId || null, subject: f.subject || null }) })
          const json = await res.json()
          if (json && json.code === 200) {
            this.$message && this.$message.success('更新成功')
            this.modal.visible = false
            this.loadData()
          } else {
            if (json && json.code === 409) {
              const campus = this.campusNameById(f.campusId) || `#${f.campusId}`
              const grade = this.gradeNameById(f.gradeId) || `#${f.gradeId}`
              const msg = `同校同年级已存在该班级名称（${campus} / ${grade}：${f.name}）。请更换名称。`
              this.$message && this.$message.error(msg)
            } else {
              const msg = (json && json.msg) ? json.msg : '更新失败'
              this.$message && this.$message.error(msg)
            }
          }
        }
      } catch (e) { this.$message && this.$message.error(e.message || '操作失败') }
    },
    async deleteItem(record) {
      if (!record || record.id == null) { this.$message && this.$message.warning('当前行数据无效，无法删除'); return }
      try {
        const res = await fetch(`/api/dict/class/delete/${record.id}`, { method: 'DELETE' })
        const json = await res.json()
        if (json && json.code === 200) {
          this.$message && this.$message.success('删除成功')
          this.loadData()
        } else {
          const msg = (json && json.msg) ? json.msg : '删除失败'
          throw new Error(msg)
        }
      } catch (e) { this.$message && this.$message.error(e.message || '删除失败') }
    },
    async toggleStatus(record) {
      if (!record || record.id == null) { this.$message && this.$message.warning('当前行数据无效，无法更新状态'); return }
      try {
        const newStatus = record.status === 'enabled' ? 'disabled' : 'enabled'
        const res = await fetch(`/api/dict/class/update/${record.id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ campusId: record.campusId, gradeId: record.gradeId, name: record.name, mode: record.mode, startDate: record.startDate, endDate: record.endDate, term: record.term, headTeacherIds: record.headTeacherIds, classroom: record.classroom, status: newStatus, sortOrder: Number(record.sortOrder || 0), capacityLimit: record.capacityLimit, feeStandard: record.feeStandard, feeStatus: record.feeStatus, tags: record.tags, note: record.note, parentGroup: record.parentGroup, contacts: record.contacts }) })
        const json = await res.json()
        if (json && json.code === 200) {
          this.$message && this.$message.success('状态已更新')
          this.loadData()
        } else {
          const msg = (json && json.msg) ? json.msg : '更新失败'
          throw new Error(msg)
        }
      } catch (e) {
        this.$message && this.$message.error(e.message || '更新失败')
      }
    },
    async updateSort(record) {
      if (!record || record.id == null) { this.$message && this.$message.warning('当前行数据无效，无法更新排序'); return }
      try {
        const res = await fetch(`/api/dict/class/update/${record.id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ campusId: record.campusId, gradeId: record.gradeId, name: record.name, mode: record.mode, startDate: record.startDate, endDate: record.endDate, term: record.term, headTeacherIds: record.headTeacherIds, classroom: record.classroom, status: record.status || 'enabled', sortOrder: Number(record.sortOrder || 0), capacityLimit: record.capacityLimit, feeStandard: record.feeStandard, feeStatus: record.feeStatus, tags: record.tags, note: record.note, parentGroup: record.parentGroup, contacts: record.contacts }) })
        const json = await res.json()
        if (json && json.code === 200) {
          this.$message && this.$message.success('排序已更新')
          this.loadData()
        } else {
          const msg = (json && json.msg) ? json.msg : '更新失败'
          throw new Error(msg)
        }
      } catch (e) {
        this.$message && this.$message.error(e.message || '更新失败')
      }
    },
    
    suggestClassName() {
      const f = (this.modal && this.modal.form) || {}
      const gname = this.gradeNameById(f.gradeId) || ''
      const sname = (() => {
        const arr = Array.isArray(this.subjectOptions) ? this.subjectOptions : []
        const it = arr.find(x => String(x.id) === String(f.subject))
        return it ? it.name : ''
      })()
      const term = f.term && String(f.term).trim() ? String(f.term).trim() : this.autoTerm()
      const parts = []
      if (gname) parts.push(gname)
      if (sname) parts.push(sname)
      if (term) parts.push(term)
      const name = (parts.join('') + '班').trim()
      this.modal.form.name = name
    },
    autoTerm(){
      const d = new Date()
      const y = d.getFullYear()
      const m = d.getMonth() + 1
      if (m >= 8) return `${y}-${y+1}上学期`
      return `${y}春季`
    },
    onTermFocus(){
      const f = (this.modal && this.modal.form) || {}
      if (!f.term || !String(f.term).trim()) {
        this.modal.form.term = this.autoTerm()
      }
      if (!f.name || !String(f.name).trim()) {
        this.suggestClassName()
      }
    },
    onModalGradeChange() {
      // 仅在名称未填写时自动生成，避免覆盖用户输入
      if (!this.modal.form.name) {
        this.suggestClassName()
      }
    },
    // 计算中文序号后缀：基于现有列表，选取未占用的“一班”“二班”…
    computeNextOrdinalSuffix(campusId, gradeId) {
      const numerals = ['一','二','三','四','五','六','七','八','九','十','十一','十二','十三','十四','十五','十六','十七','十八','十九','二十']
      const gname = this.gradeNameById(gradeId) || ''
      const sameGroup = (this.list || []).filter(it => String(it.campusId) === String(campusId) && String(it.gradeId) === String(gradeId))
      const used = new Set()
      for (const it of sameGroup) {
        const nm = String(it.name || '')
        if (gname && nm.startsWith(gname)) {
          const suffix = nm.slice(gname.length)
          // 规范后缀形如 “一班”“二班”
          if (suffix && /^(一|二|三|四|五|六|七|八|九|十(一|二|三|四|五|六|七|八|九)?|二十)班$/.test(suffix)) {
            used.add(suffix)
          }
        }
      }
      // 找到第一个未使用的后缀
      for (const n of numerals) {
        const candidate = `${n}班`
        if (!used.has(candidate)) return candidate
      }
      // 超出预设范围时回退为数字计数
      return `${(sameGroup.length + 1)}班`
    },
    
    onTableChange(pager) {
      this.pagination = { ...this.pagination, current: pager.current, pageSize: pager.pageSize }
      this.loadData()
    }
  }
}
</script>

<style>
.class-dict-page { background: #fff; padding: 16px; }
.class-dict-page .ant-page-header { padding: 0 0 12px 0; }
.class-dict-page .ant-card { border-radius: 8px; }
.class-dict-page .ant-table { margin-top: 8px; }
/* 操作列固定右侧，只显示主按钮，悬停展示下拉菜单 */
.class-dict-page .action-cell { width: 140px; white-space: nowrap; }
.class-dict-page .action-cell-wrap { display: flex; align-items: center; }
.class-dict-page .ant-table-small td { padding-top: 8px; padding-bottom: 8px; }
/* 操作菜单禁用态灰色显示 */
.class-dict-page .action-link { color: #1890ff; }
.class-dict-page .action-link-disabled { color: #bfbfbf; cursor: not-allowed; pointer-events: none; }
/* 删除操作颜色与其它按钮区分；禁用态为灰色 */
.class-dict-page .action-delete { color: #ff4d4f; }
.class-dict-page .action-delete-disabled { color: #bfbfbf; cursor: not-allowed; pointer-events: none; }
</style>