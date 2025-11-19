<template>
  <div style="background:#fff; padding:10px">
    <div style="font-size:16px; font-weight:600; margin-bottom:6px">排课管理</div>
    <div v-if="fixedClassMode" class="fixed-class-summary" style="margin-bottom:6px">
      <a-descriptions size="small" :column="3">
        <a-descriptions-item label="班级">{{ fixedClassMeta.className || ('班级#'+fixedClassMeta.classId) }}</a-descriptions-item>
        <a-descriptions-item label="班级ID">{{ fixedClassMeta.classId }}</a-descriptions-item>
        <a-descriptions-item label="授课模式/类型">{{ (fixedClassMeta.mode || '-') + ' / ' + fmtClassType(calendarForm.classType) }}</a-descriptions-item>
        <a-descriptions-item label="科目"><a-tag color="blue">{{ calendarForm.subject || '-' }}</a-tag></a-descriptions-item>
      </a-descriptions>
      <input type="hidden" :value="calendarForm.classId" />
    </div>
    <a-form layout="horizontal" class="compact-form" :labelCol="{ span: 6 }" :wrapperCol="{ span: 18 }">
      <a-row :gutter="[8, 6]" class="compact-row">
        <a-col v-if="!fixedClassMode" :xs="24" :sm="12" :md="8" :lg="8">
          <a-form-item label="班级">
            <a-select v-model="calendarForm.classId" allowClear placeholder="选择班级" style="width:100%" size="small" @change="onClassChange">
              <a-select-option v-for="c in classOptions" :key="c.id" :value="c.id">{{ c.name || ('班级#'+c.id) }}</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col v-if="!fixedClassMode" :xs="24" :sm="12" :md="8" :lg="8">
          <a-form-item label="班级类型">
            <a-select v-model="calendarForm.classType" allowClear placeholder="选择班级类型" style="width:100%" size="small" :disabled="!!calendarForm.classId">
              <a-select-option value="1">班课</a-select-option>
              <a-select-option value="2">一对一</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col v-if="!fixedClassMode" :xs="24" :sm="12" :md="8" :lg="8">
          <a-form-item label="科目">
            <a-select v-model="calendarForm.subject" allowClear placeholder="选择科目" style="width:100%" size="small">
              <a-select-option v-for="s in subjectOptions" :key="s.id || s.name" :value="s.name">{{ s.name }}</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="[8, 6]" class="compact-row" style="margin-top:4px">
        <a-col :xs="24" :sm="12" :md="8" :lg="8">
          <a-form-item label="目标月份">
            <a-space :size="6">
              <a-input v-model="calendarMonthStr" placeholder="YYYY-MM" style="width:110px" size="small" @change="onCalendarMonthInputChange" />
              <a-button size="small" @click="prevMonth">上个月</a-button>
              <a-button size="small" @click="nextMonth">下个月</a-button>
            </a-space>
          </a-form-item>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="8">
          <a-form-item label="上课时间：" class="time-range">
            <a-input v-model="calendarForm.startTime" placeholder="HH:mm" style="width:96px" size="small" />
            <span style="margin:0 8px">-</span>
            <a-input v-model="calendarForm.endTime" placeholder="HH:mm" style="width:96px" size="small" />
          </a-form-item>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="8">
          <a-form-item label="课时">
            <a-select v-model="calendarForm.durationMinutes" style="width:100%" size="small">
              <a-select-option :value="60">1小时</a-select-option>
              <a-select-option :value="120">2小时</a-select-option>
              <a-select-option :value="180">3小时</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="[8, 6]" class="compact-row" style="margin-top:4px">
        <a-col :xs="24" :sm="12" :md="8" :lg="8">
          <a-form-item label="课时单价(元)">
            <a-input-number v-model="calendarForm.fee" :min="0" :step="10" style="width:100%" :disabled="true" size="small" />
          </a-form-item>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="8">
          <a-form-item label="老师">
            <a-select v-model="calendarForm.teacherId" allowClear placeholder="选择老师" style="width:100%" size="small">
              <a-select-option v-for="t in teacherOptions" :key="t.id" :value="t.id">{{ t.name || ('老师#'+t.id) }}</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8" :lg="8">
          <a-form-item label="教室">
            <a-select v-model="calendarForm.classroomId" allowClear show-search placeholder="选择教室" style="width:100%" size="small">
              <a-select-option v-for="r in classroomOptions" :key="r.id" :value="r.id">{{ r.name }}（可用座位：{{ r.usableSeats }}）</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>
    </a-form>
      <div class="schedule-block">
        <table class="form-actions-table">
        <tbody>
          <tr>
            <td>
              <div class="form-actions">
                <a-space :size="6">
                  <a-button size="small" type="primary" :loading="loading" @click="generateCalendarList">生成排课列表</a-button>
                  <a-button size="small" @click="checkConflicts" :disabled="!calendarSchedules.length">检查冲突</a-button>
                  <a-button size="small" type="dashed" @click="persistSchedules" :disabled="!calendarSchedules.length">提交到后台</a-button>
                  <a-button size="small" @click="clearSelections">清空日期选择</a-button>
                </a-space>
              </div>
            </td>
          </tr>
        </tbody>
        </table>
        <div class="schedule-content">
          <div class="schedule-calendar">
            <div class="calendar-grid">
              <div class="calendar-header">
                <div class="cell">周一</div>
              <div class="cell">周二</div>
              <div class="cell">周三</div>
              <div class="cell">周四</div>
              <div class="cell">周五</div>
              <div class="cell">周六</div>
              <div class="cell">周日</div>
            </div>
            <div class="calendar-body">
              <div class="row" v-for="(wk, wi) in calendarWeeks" :key="wi">
                <div class="cell" v-for="(d, di) in wk" :key="d.dateStr || (wi+'-'+di)" :class="{disabled: !d.inMonth, selected: !!d.selected}" @click="onCalendarCellClick(d)">
                  <span class="day-number">{{ d.dayNum || '' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="schedule-list" v-if="calendarSchedules.length">
          <a-alert type="info" :message="'已选择 '+selectedDates.length+' 天，生成 '+calendarSchedules.length+' 条排课'" show-icon style="margin-bottom:8px" />
          <a-table :data-source="calendarSchedules" :columns="calendarColumns" rowKey="key" :pagination="false" size="small">
            <template slot="classType" slot-scope="text, record">
              <span>{{ fmtClassType(record.classType) }}</span>
            </template>
            <template slot="teacher" slot-scope="text, record">
              <a-select v-model="record.teacherId" style="width:100%" size="small" @change="onScheduleTeacherChange(record)">
                <a-select-option v-for="t in teacherOptions" :key="t.id" :value="t.id">{{ t.name || ('老师#'+t.id) }}</a-select-option>
              </a-select>
            </template>
            <template slot="durationMinutes" slot-scope="text, record">
              <span>{{ Number(text || record.durationMinutes || 0) / 60 }} 小时</span>
            </template>
            <template slot="fee" slot-scope="text, record">
              <span>￥{{ Number(text || record.fee || 0) }}</span>
            </template>
            <template slot="subject" slot-scope="text, record">
              <a-select v-model="record.subject" style="width:100%" size="small" @change="onScheduleSubjectChange(record)">
                <a-select-option v-for="s in subjectOptions" :key="s.id || s.name" :value="s.name">{{ s.name }}</a-select-option>
              </a-select>
            </template>
            <template slot="classroom" slot-scope="text, record">
              <a-select v-model="record.classroomId" style="width:100%" size="small">
                <a-select-option v-for="r in classroomOptions" :key="r.id" :value="r.id">{{ r.name }}（可用座位：{{ r.usableSeats }}）</a-select-option>
              </a-select>
            </template>
            <template slot="timePoint" slot-scope="text, record">
              <a-select :value="record.timePoint || (record.dateTime && record.dateTime.split(' ')[1])" style="width:100%" size="small" @change="val => onTimePointChange(record, val)">
                <a-select-option v-for="tp in getTimePointOptions()" :key="tp" :value="tp">{{ tp }}</a-select-option>
              </a-select>
            </template>
            <template slot="ops" slot-scope="text, record">
              <a-space :size="6">
                <a-popconfirm title="确认提交保存？" ok-text="提交" cancel-text="取消" @confirm="persistSchedules">
                  <a-button size="small" type="primary">保存</a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </a-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import moment from 'moment'
export default {
  name: 'ScheduleManage',
  data() {
    return {
      fixedClassMode: false,
      fixedClassMeta: {},
      loading: false,
      classOptions: [],
      teacherOptions: [],
      subjectOptions: [],
      classroomOptions: [],
      hasUnsavedSchedules: false,
      weekDayOptions: [
        { label: '周一', value: 1 }, { label: '周二', value: 2 }, { label: '周三', value: 3 },
        { label: '周四', value: 4 }, { label: '周五', value: 5 }, { label: '周六', value: 6 }, { label: '周日', value: 7 }
      ],
      // 日历排课
      calendarMonth: null, // MonthPicker 绑定用（moment|null）
      calendarMonthStr: '', // 内部生成日历用（YYYY-MM）
      calendarWeeks: [],
      selectedDates: [], // YYYY-MM-DD
      calendarForm: { 
        classId: null,
        classType: '1',
        teacherId: null, 
        classroomId: null,
        subject: '', 
        durationMinutes: 60, 
        // 使用字符串时间以规避 a-time-picker 的兼容性问题
        startTime: '09:00', 
        endTime: '10:30', 
        fee: 100 
      },
      calendarSchedules: [],
      calendarColumns: [
        { title: '班级类型', dataIndex: 'classType', key: 'classType', scopedSlots: { customRender: 'classType' } },
        { title: '单课课时', dataIndex: 'durationMinutes', key: 'durationMinutes', scopedSlots: { customRender: 'durationMinutes' } },
        { title: '课时单价', dataIndex: 'fee', key: 'fee', scopedSlots: { customRender: 'fee' } },
        { title: '日期+时间', dataIndex: 'dateTime', key: 'dateTime' },
        { title: '老师', dataIndex: 'teacherId', key: 'teacherId', scopedSlots: { customRender: 'teacher' } },
        { title: '科目', dataIndex: 'subject', key: 'subject', scopedSlots: { customRender: 'subject' } },
        { title: '教室', dataIndex: 'classroomId', key: 'classroomId', scopedSlots: { customRender: 'classroom' } },
        { title: '时间点', dataIndex: 'timePoint', key: 'timePoint', scopedSlots: { customRender: 'timePoint' } },
        { title: '操作', key: 'ops', scopedSlots: { customRender: 'ops' } }
      ],
      editVisible: false,
      editForm: {},
      editingRecord: null,
      
    }
  },
  created() {
    this.loadClassOptions(); this.loadTeacherOptions(); this.loadSubjectOptions(); this.loadClassroomOptions(); this.initCalendar()
    // 从路由接收固定班级参数：隐藏班级/类型/科目字段
    const q = (this.$route && this.$route.query) || {}
    const cid = q.classId != null ? Number(q.classId) : null
    if (cid) {
      this.fixedClassMode = true
      this.fixedClassMeta = { classId: cid, className: q.className || '', mode: q.mode || '' }
      this.calendarForm.classId = cid
      // 班级类型：优先取传入的 classType，其次根据授课模式推断，默认班课
      const qType = String(q.classType || '').trim()
      const mode = String(q.mode || '').trim()
      this.calendarForm.classType = qType ? qType : ((mode === '一对一') ? '2' : '1')
      // 若有传入默认老师
      if (q.teacherId) this.calendarForm.teacherId = Number(q.teacherId)
      // 若有传入科目
      if (q.subject) this.calendarForm.subject = String(q.subject)
    }
    // 初始化时同步一次单价与课时
    this.syncFeeWithDuration()
  },
  watch: {
    'calendarForm.durationMinutes'(nv) {
      this.syncFeeWithDuration()
    }
  },
  methods: {
    async loadClassroomOptions() {
      try {
        const r = await fetch('/api/base/classroom/list')
        const j = await r.json()
        this.classroomOptions = (j && j.code === 200) ? (j.data || []) : []
      } catch (e) { this.classroomOptions = [] }
    },
    async loadClassOptions() {
      try {
        const token = localStorage.getItem('AUTH_TOKEN') || ''
        const res = await fetch('/api/class/list', { headers: token ? { 'Authorization': 'Bearer ' + token } : undefined })
        const json = await res.json()
        if (json && json.code === 200) {
          this.classOptions = (json.data || []).map(it => ({ id: it.id, name: it.name, mode: it.mode }))
        }
      } catch (e) {}
    },
    async loadTeacherOptions() {
      try {
        const token = localStorage.getItem('AUTH_TOKEN') || ''
        const res = await fetch('/api/teacher/list', { headers: { 'Authorization': 'Bearer ' + token } })
        const json = await res.json()
        const arr = (json && json.code === 200) ? (json.data || []) : []
        this.teacherOptions = Array.isArray(arr) ? arr.map(it => ({ id: it.id, name: it.name || it.teacherName || ('老师#'+(it.id||'')) })) : []
        // 默认选择第一个老师（若未选择且有数据）
        if (!this.calendarForm.teacherId && this.teacherOptions.length) {
          this.calendarForm.teacherId = this.teacherOptions[0].id
        }
      } catch(e) { this.teacherOptions = [] }
    },
    onClassChange(val) {
      // 选中班级后，联动设置班级类型并禁用编辑
      const opt = this.classOptions.find(c => c.id === val)
      if (opt) {
        const mode = String(opt.mode || '').trim()
        // 后端 mode 可能为 "一对一" / "线上" / "线下"
        this.calendarForm.classType = (mode === '一对一') ? '2' : '1'
      } else {
        // 清空班级后允许编辑，保持原值或默认班课
        if (!this.calendarForm.classType) this.calendarForm.classType = '1'
      }
    },
    async loadSubjectOptions() {
      try {
        const res = await fetch('/api/dict/subject/list')
        const json = await res.json()
        if (json && json.code === 200) {
          const arr = (json.data || []).map(it => ({ id: it.id, name: it.name || it.subjectName || '' })).filter(it => it.name)
          this.subjectOptions = arr.length ? arr : []
        }
      } catch (e) {}
      // 后端不可用或账号没有科目时，使用内置备选
      if (!Array.isArray(this.subjectOptions) || !this.subjectOptions.length) {
        this.subjectOptions = [
          { name: '数学' }, { name: '英语' }, { name: '语文' }, { name: '物理' }, { name: '化学' }
        ]
      }
      // 若尚未选择科目，默认选择第一个
      if (!this.calendarForm.subject && this.subjectOptions.length) {
        this.calendarForm.subject = this.subjectOptions[0].name
      }
    },
    initCalendar() {
      // 默认当前月，仅用于渲染网格；MonthPicker由用户选择（保持为 moment 或 null）
      const d = new Date()
      const y = d.getFullYear(), m = d.getMonth() + 1
      const s = `${y}-${String(m).padStart(2,'0')}`
      this.calendarMonthStr = s
      // 默认填充输入框为当前月，提升可用性
      this.calendarMonth = moment(s, 'YYYY-MM')
      this.buildCalendarWeeks(s)
    },
    onCalendarMonthChange(val, dateString) {
      const s = dateString || ((val && typeof val.format === 'function') ? val.format('YYYY-MM') : '')
      this.calendarMonth = val
      this.calendarMonthStr = s
      this.selectedDates = []
      this.buildCalendarWeeks(s)
    },
    onCalendarMonthInputChange() {
      const s = this.calendarMonthStr
      this.selectedDates = []
      this.buildCalendarWeeks(s)
    },
    prevMonth() {
      const [yStr,mStr] = (this.calendarMonthStr||'').split('-')
      let y = Number(yStr)||new Date().getFullYear()
      let m = Number(mStr)||new Date().getMonth()+1
      m -= 1
      if (m < 1) { m = 12; y -= 1 }
      this.calendarMonthStr = `${y}-${String(m).padStart(2,'0')}`
      this.onCalendarMonthInputChange()
    },
    nextMonth() {
      const [yStr,mStr] = (this.calendarMonthStr||'').split('-')
      let y = Number(yStr)||new Date().getFullYear()
      let m = Number(mStr)||new Date().getMonth()+1
      m += 1
      if (m > 12) { m = 1; y += 1 }
      this.calendarMonthStr = `${y}-${String(m).padStart(2,'0')}`
      this.onCalendarMonthInputChange()
    },
    buildCalendarWeeks(ym) {
      if (!ym) { this.calendarWeeks = []; return }
      const [yStr,mStr] = ym.split('-')
      const y = Number(yStr), m = Number(mStr)
      const first = new Date(y, m-1, 1)
      const last = new Date(y, m, 0)
      const daysInMonth = last.getDate()
      // 1=Mon..7=Sun
      const firstDow = ((first.getDay() === 0 ? 7 : first.getDay()))
      const cells = []
      // leading blanks
      for (let i=1;i<firstDow;i++) cells.push({ inMonth:false })
      for (let d=1; d<=daysInMonth; d++) {
        const date = new Date(y, m-1, d)
        const ds = `${y}-${String(m).padStart(2,'0')}-${String(d).padStart(2,'0')}`
        cells.push({ inMonth:true, dayNum:d, dateStr:ds, selected: this.selectedDates.includes(ds) })
      }
      // trailing to complete weeks (total cells to multiple of 7)
      while (cells.length % 7 !== 0) cells.push({ inMonth:false })
      const weeks = []
      for (let i=0;i<cells.length;i+=7) weeks.push(cells.slice(i,i+7))
      this.calendarWeeks = weeks
    },
    onCalendarCellClick(cell) {
      if (!cell || !cell.inMonth || !cell.dateStr) return
      const ds = cell.dateStr
      // 选择规则：
      // - 未处于连续选择时：点击未选日期 -> 添加；点击已选日期 -> 取消。
      // - 处于连续选择时：第二次点击会选中[start..end]的连续区间，并与已有选择合并。
      if (!this._rangeStart) {
        if (this.selectedDates.includes(ds)) {
          this.selectedDates = this.selectedDates.filter(d => d !== ds)
        } else {
          this._rangeStart = ds
          this.selectedDates = [...this.selectedDates, ds]
        }
      } else {
        const start = new Date(this._rangeStart)
        const end = new Date(ds)
        const a = start <= end ? start : end
        const b = start <= end ? end : start
        const sel = []
        const cur = new Date(a)
        while (cur <= b) { sel.push(`${cur.getFullYear()}-${String(cur.getMonth()+1).padStart(2,'0')}-${String(cur.getDate()).padStart(2,'0')}`); cur.setDate(cur.getDate()+1) }
        const merged = Array.from(new Set([ ...this.selectedDates, ...sel ]))
        // 排序
        merged.sort((d1,d2) => new Date(d1).getTime() - new Date(d2).getTime())
        this.selectedDates = merged
        this._rangeStart = null
      }
      // 更新格子选中态
      this.buildCalendarWeeks(this.calendarMonthStr)
    },
    clearSelections() {
      this.selectedDates = []
      this._rangeStart = null
      // 若存在未提交的排课数据，则一并清空
      if (this.calendarSchedules && this.calendarSchedules.length && this.hasUnsavedSchedules) {
        this.calendarSchedules = []
        this.hasUnsavedSchedules = false
        this.$message.success('已清空日期选择并移除未提交的排课数据')
      }
      this.buildCalendarWeeks(this.calendarMonthStr)
    },
    fmtHHmm(d) {
      if (!d) return ''
      if (typeof d === 'string') {
        const m = d.trim()
        const m2 = /^\d{1,2}:\d{2}$/.test(m) ? m : ''
        if (!m2) return ''
        const [h, mi] = m2.split(':')
        return `${String(Number(h)).padStart(2,'0')}:${mi}`
      }
      if (typeof d.format === 'function') { try { return d.format('HH:mm') } catch(e){} }
      try { const h=(d.getHours&&d.getHours())||0, m=(d.getMinutes&&d.getMinutes())||0; return `${String(h).padStart(2,'0')}:${String(m).padStart(2,'0')}` } catch(e){ return '' }
    },
    generateCalendarList() {
      // 更精确的校验与提示：逐项检查并汇总缺失字段
      const missing = []
      const typeVal = String(this.calendarForm.classType === undefined || this.calendarForm.classType === null ? '' : this.calendarForm.classType).trim()
      if (!typeVal) missing.push('班级类型')
      if (!this.selectedDates.length) missing.push('日期')
      if (!this.calendarForm.startTime || !this.calendarForm.endTime) missing.push('上课时间')
      if (missing.length) { this.$message.error('请完整选择：' + missing.join('、')); return }
      const st = this.fmtHHmm(this.calendarForm.startTime)
      const et = this.fmtHHmm(this.calendarForm.endTime)
      if (!st || !et) { this.$message.error('请输入有效的起止时间（HH:mm）'); return }
      // 保证起止时间顺序正确
      const [sh, sm] = st.split(':').map(n => Number(n))
      const [eh, em] = et.split(':').map(n => Number(n))
      const startMin = sh * 60 + sm
      const endMin = eh * 60 + em
      if (endMin <= startMin) { this.$message.error('结束时间必须晚于开始时间'); return }
      const range = `${st}-${et}`
      const durationMins = Number(this.calendarForm.durationMinutes) || 60
      const feeCalc = Math.round(durationMins / 60) * 100
      // 老师、科目兜底默认值
      const teacherId = this.calendarForm.teacherId || (this.teacherOptions[0] && this.teacherOptions[0].id) || null
      const subject = this.calendarForm.subject || (this.subjectOptions[0] && this.subjectOptions[0].name) || ''
      this.calendarSchedules = this.selectedDates.map((ds, idx) => ({
        key: ds + '-' + idx,
        classType: this.calendarForm.classType,
        classId: this.calendarForm.classId || null,
        teacherId,
        classroomId: this.calendarForm.classroomId || null,
        durationMinutes: durationMins,
        fee: feeCalc,
        subject,
        date: ds,
        timeRange: range,
        dateTime: `${ds} ${st}`,
        timePoint: st,
        conflict: false,
        status: '正常'
      }))
      this.hasUnsavedSchedules = !!this.calendarSchedules.length
      console.log(this.calendarSchedules)
      this.$message.success('已生成排课列表，请检查或提交')
    },
    getTimePointOptions() {
      const st = this.fmtHHmm(this.calendarForm.startTime) || '09:00'
      const et = this.fmtHHmm(this.calendarForm.endTime) || '20:00'
      const [sh, sm] = st.split(':').map(n => Number(n))
      const [eh, em] = et.split(':').map(n => Number(n))
      let s = sh * 60 + sm, e = eh * 60 + em
      if (e <= s) { e = s + Math.max(30, Number(this.calendarForm.durationMinutes) || 60) }
      const opts = []
      while (s <= e) { const h = Math.floor(s/60), m = s % 60; opts.push(`${String(h).padStart(2,'0')}:${String(m).padStart(2,'0')}`); s += 15 }
      return opts
    },
    onTimePointChange(row, val) {
      const tp = this.fmtHHmm(val)
      if (!tp) return
      row.timePoint = tp
      // 更新 dateTime 与 timeRange 的开始时间，结束时间按时长自动推算
      row.dateTime = `${row.date} ${tp}`
      const mins = Number(row.durationMinutes) || Number(this.calendarForm.durationMinutes) || 60
      const [h, m] = tp.split(':').map(n => Number(n))
      const startTotal = h*60 + m
      const endTotal = startTotal + mins
      const eh = Math.floor(endTotal/60), em = endTotal % 60
      const endText = `${String(eh).padStart(2,'0')}:${String(em).padStart(2,'0')}`
      row.timeRange = `${tp}-${endText}`
    },
    async persistSchedules() {
      if (!this.calendarSchedules.length) { this.$message.warning('请先生成排课列表'); return }
      // 数据完整性校验：班级类型、老师、日期、开始/结束时间
      const invalid = []
      for (const it of this.calendarSchedules) {
        const hasType = !!it.classType
        const type = this.fmtClassType(it.classType)
        const hasTeacher = !!it.teacherId
        const hasDate = !!it.date
        const st = (it.timePoint || (it.dateTime && it.dateTime.split(' ')[1]) || this.fmtHHmm(this.calendarForm.startTime) || '')
        const et = (it.timeRange && it.timeRange.split('-')[1]) || ''
        if (!hasType || !hasTeacher || !hasDate || !st) invalid.push(it)
      }
      if (invalid.length) { this.$message.error('排课数据不完整，请检查班级类型、班级/老师、日期与时间'); return }
      try {
        const token = localStorage.getItem('AUTH_TOKEN') || ''
        const payload = {
          items: this.calendarSchedules.map(it => ({
            classId: it.classId || this.calendarForm.classId || null,
            classType: it.classType,
            teacherId: it.teacherId,
            classroomId: it.classroomId || this.calendarForm.classroomId || null,
            date: it.date,
            start: (it.timePoint || (it.dateTime && it.dateTime.split(' ')[1]) || (this.fmtHHmm(this.calendarForm.startTime) || '09:00')),
            end: (it.timeRange && it.timeRange.split('-')[1]) || null,
            durationMinutes: it.durationMinutes,
            fee: it.fee
          }))
        }
        // 按行没有明确 end，则使用开始时间+时长推算
        payload.items = payload.items.map(x => {
          if (!x.end) {
            const [h,m] = x.start.split(':').map(n=>Number(n)); const st=h*60+m; const et=st+(x.durationMinutes||60);
            const eh=Math.floor(et/60), em=et%60; x.end=`${String(eh).padStart(2,'0')}:${String(em).padStart(2,'0')}`
          }
          return x
        })
        const res = await fetch('/api/schedule/manual/save', { method: 'POST', headers: { 'Content-Type': 'application/json', 'Authorization': token ? ('Bearer '+token) : undefined }, body: JSON.stringify(payload) })
        const json = await res.json()
        if (json && json.code === 200) {
          this.$message.success(`提交成功：已保存 ${json.data && json.data.savedCount || 0} 条`) 
          this.hasUnsavedSchedules = false
        } else {
          this.$message.error(`提交失败：${json && json.message || '未知错误'}`)
        }
      } catch (e) {
        this.$message.error('提交异常：' + e.message)
      }
    },
    fmtClassType(val) {
      const v = val === undefined || val === null ? '' : String(val)
      if (v === '1') return '班课'
      if (v === '2') return '一对一'
      if (v === '班课' || v === '一对一') return v
      return v || ''
    },
    async checkConflicts() {
      if (!this.calendarSchedules.length) return
      this.loading = true
      try {
        for (const item of this.calendarSchedules) {
          const p = new URLSearchParams({ date: item.date, start: item.timeRange.split('-')[0], end: item.timeRange.split('-')[1] })
          const type = this.fmtClassType(item.classType)
          if (type === '班课' && item.classId) p.append('classId', String(item.classId))
          else if (type === '一对一' && item.teacherId) p.append('teacherId', String(item.teacherId))
          else if (item.classId) p.append('classId', String(item.classId))
          // 若选择了教室，则根据教室选项追加 room/campusId 参与冲突校验
          const selectedRoomId = item.classroomId || this.calendarForm.classroomId
          if (selectedRoomId) {
            const roomObj = (this.classroomOptions || []).find(r => String(r.id) === String(selectedRoomId))
            if (roomObj) {
              if (roomObj.name) p.append('room', roomObj.name)
              if (roomObj.campusId) p.append('campusId', String(roomObj.campusId))
            }
          }
          // 若有教师信息，补充教师冲突校验参数（不影响后端缺省处理）
          if (item.teacherId) p.append('teacherId', String(item.teacherId))
          const token = localStorage.getItem('AUTH_TOKEN') || ''
          const res = await fetch('/api/schedule/conflict/check?' + p.toString(), { headers: token ? { 'Authorization': 'Bearer ' + token } : undefined })
          const json = await res.json()
          item.conflict = !(json && json.code === 200 && json.data && json.data.ok)
          item.status = item.conflict ? '冲突' : '正常'
        }
      } catch(e) {
        this.$message.error('冲突检查失败: ' + e.message)
      } finally { this.loading = false }
    },
    onScheduleTeacherChange(row) { /* 保持双向数据即可 */ },
    onScheduleSubjectChange(row) { /* 保持双向数据即可 */ },
    openEdit(record) {
      this.editingRecord = record
      this.editForm = Object.assign({}, record)
      this.syncEditFeeWithDuration()
      this.editVisible = true
    },
    closeEdit() { this.editVisible = false; this.editingRecord = null; this.editForm = {} },
    saveEdit() {
      if (!this.editingRecord) return
      const src = this.editingRecord
      const f = this.editForm
      src.teacherId = f.teacherId
      src.subject = f.subject
      src.durationMinutes = f.durationMinutes
      src.fee = Math.round((Number(src.durationMinutes) || 60) / 60) * 100
      src.startTime = this.fmtHHmm(f.startTime)
      src.endTime = this.fmtHHmm(f.endTime)
      src.timeRange = `${src.startTime}-${src.endTime}`
      src.dateTime = `${src.date} ${src.startTime}`
      this.editVisible = false
      this.$message.success('已应用微调')
    },
    syncFeeWithDuration() {
      const minutes = Number(this.calendarForm.durationMinutes) || 60
      this.calendarForm.fee = Math.round(minutes / 60) * 100
    },
    onEditDurationChange() { this.syncEditFeeWithDuration() },
    syncEditFeeWithDuration() {
      if (!this.editForm) return
      const minutes = Number(this.editForm.durationMinutes) || 60
      this.editForm.fee = Math.round(minutes / 60) * 100
    },
    async submitCalendarSchedules() { /* deprecated: 改为 persistSchedules 使用 /api/schedule/manual/save */ },
    fmtHHmm(d) {
      if (!d) return ''
      if (typeof d.format === 'function') {
        // moment/dayjs 对象
        try { return d.format('HH:mm') } catch (e) {}
      }
      // 字符串格式 HH:mm 直接返回标准化
      if (typeof d === 'string') {
        const m = d.trim().match(/^([0-2]?\d):([0-5]\d)$/)
        if (m) {
          const h = String(Math.min(23, Number(m[1]))).padStart(2, '0')
          const mm = m[2]
          return `${h}:${mm}`
        }
        return ''
      }
      try {
        const h = (d.getHours && d.getHours()) || 0
        const m = (d.getMinutes && d.getMinutes()) || 0
        return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
      } catch (e) {
        return ''
      }
    },
    
    
  }
}
</script>

<style>
.schedule-layout { display:flex; flex-direction:column; gap:8px; }
.schedule-content { display:flex; flex-direction:column; gap:12px; align-items:stretch; }
.schedule-calendar { min-width:320px; }
.schedule-list { min-width:360px; }
/* 上下布局恒定，不再使用双列网格 */
.compact-form { margin-bottom:30px; }
.compact-form .ant-form-item { margin-bottom: 0; }
.compact-form .ant-form-item-label > label { height:30px; line-height:30px; }
.compact-row { height:30px; align-items:center; flex-wrap:wrap; }
.form-actions { margin-bottom:6px; display:flex; align-items:center; gap:6px; flex-wrap:wrap; }
.time-range { white-space:nowrap; }
.form-actions-table { width:100%; border-collapse:collapse; margin-bottom:6px; }
.form-actions-table td { padding:0; }
.calendar-grid { border:1px solid #eee; }
.calendar-header, .calendar-body .row { display:grid; grid-template-columns: repeat(7, 1fr); }
.calendar-header { background:#fafafa; border-bottom:1px solid #eee; }
.calendar-header .cell { padding:4px; text-align:center; font-weight:500; }
.calendar-body .cell { height:36px; border-right:1px solid #f0f0f0; border-bottom:1px solid #f0f0f0; padding:3px; cursor:pointer; }
.calendar-body .cell.disabled { background:#fafafa; color:#999; cursor:not-allowed; }
.calendar-body .cell.selected { background:#e6f7ff; border-color:#91d5ff; }
.calendar-body .day-number { font-size:10px; }
</style>