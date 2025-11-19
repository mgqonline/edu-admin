<template>
  <div class="renew-manage">
    <a-page-header title="续费录入" sub-title="为已报班学员续费并入库" />

    <a-card bordered title="选择学员与班级">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 12 }">
        <a-form-item label="学员">
          <a-select v-model="form.studentId" show-search placeholder="选择学员" @change="onStudentChange">
            <a-select-option v-for="s in studentOptions" :key="s.id" :value="s.id">{{ s.name }}（#{{ s.id }}）</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="已报班级">
          <a-select v-model="form.classId" placeholder="选择已报班级" @change="onClassChange">
            <a-select-option v-for="e in enrollOptions" :key="e.classId" :value="e.classId">{{ e.className || ('班级#' + e.classId) }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="教室">
          <a-select v-model="form.classroomId" placeholder="选择教室" allowClear @change="onClassroomChange">
            <a-select-option v-for="r in classroomOptions" :key="r.id" :value="r.id">{{ r.name }}（可用座位：{{ Number(r.usableSeats||r.capacity||0) }}）</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="座位">
          <a-space>
            <a-select v-model="form.seatNo" placeholder="选择座位" style="width:300px" :disabled="!form.classroomId || seatOptions.length===0" allowClear @dropdownVisibleChange="onSeatDropdownOpen">
              <a-select-option v-for="n in seatOptions" :key="n" :value="n" :disabled="!!occupiedMap[n]">
                {{ n }} 号 - {{ seatLabelMap[n] || '未知' }}
                <template v-if="occupiedMap[n]">（已占用：{{ occupiedMap[n] }}）</template>
              </a-select-option>
            </a-select>
            <a-button :disabled="!form.classroomId" @click="openSeatPreview">预览座位</a-button>
          </a-space>
        </a-form-item>
        <a-form-item label="单价(元/课时)"><a-input-number v-model="form.unitPrice" :min="0" :precision="2" style="width: 160px" /></a-form-item>
        <a-form-item label="续费课时"><a-input-number v-model="form.hours" :min="1" :precision="2" style="width: 160px" /></a-form-item>
        <a-form-item label="教材费"><a-input-number v-model="form.textbookFee" :min="0" :precision="2" style="width: 160px" /></a-form-item>
        <a-form-item label="优惠"><a-input-number v-model="form.discount" :min="0" :precision="2" style="width: 160px" /></a-form-item>
        <a-form-item label="应付总额">
          <a-space>
            <span>¥ {{ form.totalFee.toFixed(2) }}</span>
            <a-button size="small" @click="recalc">重新计算</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card bordered title="支付与票据" style="margin-top: 12px">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 12 }">
        <a-form-item label="支付方式">
          <a-select v-model="form.method" placeholder="选择支付方式">
            <a-select-option value="cash">现金</a-select-option>
            <a-select-option value="wechat">微信</a-select-option>
            <a-select-option value="alipay">支付宝</a-select-option>
            <a-select-option value="bankcard">银行卡</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="收据编号"><a-input v-model="form.receiptNo" placeholder="自动生成，可覆盖" /></a-form-item>
        <a-form-item label="领取人"><a-input v-model="form.receiver" placeholder="填写领取人" /></a-form-item>
        <a-form-item label="凭证URL"><a-input v-model="form.voucherUrl" placeholder="可粘贴上传后的地址（演示）" /></a-form-item>
        <a-form-item :wrapper-col="{ span: 12, offset: 6 }">
          <a-space>
            <a-button type="primary" @click="saveRenew" :loading="saving">保存续费记录</a-button>
            <a-button @click="loadRecent">加载该学员近期缴费</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card bordered title="近期缴费记录" style="margin-top: 12px">
      <a-table :data-source="recentList" :columns="columns" rowKey="id" :pagination="false">
        <template v-slot:opType="slotProps">
          <a-tag color="geekblue" v-if="slotProps.text==='renew'">续费</a-tag>
          <a-tag color="blue" v-else>报名</a-tag>
        </template>
        <template v-slot:status="slotProps">
          <a-tag color="green" v-if="slotProps.text==='paid'">已支付</a-tag>
          <a-tag color="red" v-else>未缴清</a-tag>
        </template>
      </a-table>
    </a-card>
    <a-modal v-model="seatPreviewVisible" title="座位预览与选择" :footer="null" width="720px">
      <div v-if="seatLoading">加载座位中...</div>
      <div v-else class="seat-grid">
        <div v-for="(row, rIdx) in seatGrid" :key="'row-'+rIdx" class="seat-row">
          <div v-for="seat in row" :key="seat.label" class="seat-cell" :class="{ disabled: !seat.usable, occupied: !!seat.occupiedName, selected: form.seatNo === seat.seatNo }" @click="onPickSeat(seat)">
            <div class="seat-label">{{ seat.label }}</div>
            <div v-if="seat.occupiedName" class="seat-name">{{ seat.occupiedName }}</div>
            <div v-else class="seat-no">#{{ seat.seatNo }}</div>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
  
</template>

<script>
export default {
  name: 'RenewManage',
  data() {
    return {
      studentOptions: [],
      classOptions: [],
      classroomOptions: [],
      enrollOptions: [],
      recentList: [],
      saving: false,
      form: {
        studentId: null,
        classId: null,
        classroomId: null,
        seatNo: null,
        unitPrice: 0,
        hours: 1,
        textbookFee: 0,
        discount: 0,
        totalFee: 0,
        method: 'cash',
        receiptNo: '',
        receiver: '',
        voucherUrl: ''
      },
      seatPreviewVisible: false,
      seatLoading: false,
      seatGrid: [],
      seatLabelMap: {},
      occupiedMap: {}
    }
  },
  computed: {
    columns() {
      return [
        { title: 'ID', dataIndex: 'id' },
        { title: '类型', dataIndex: 'opType', scopedSlots: { customRender: 'opType' } },
        { title: '班级', dataIndex: 'classId' },
        { title: '教室', dataIndex: 'classroomId' },
        { title: '座位', dataIndex: 'seatNo' },
        { title: '单价', dataIndex: 'unitPrice' },
        { title: '课时', dataIndex: 'hours' },
        { title: '教材费', dataIndex: 'textbookFee' },
        { title: '优惠', dataIndex: 'discount' },
        { title: '总额', dataIndex: 'totalFee' },
        { title: '方式', dataIndex: 'method' },
        { title: '状态', dataIndex: 'status', scopedSlots: { customRender: 'status' } }
      ]
    },
    seatOptions() {
      if (!this.form.classroomId) return []
      const found = (this.classroomOptions||[]).find(r => r.id === this.form.classroomId)
      const seats = Number(found && (found.usableSeats != null ? found.usableSeats : found.capacity)) || 0
      const arr = []
      for (let i = 1; i <= seats; i++) arr.push(i)
      return arr
    }
  },
  methods: {
    async loadStudents() {
      try { const res = await fetch('/api/student/list'); const json = await res.json(); this.studentOptions = Array.isArray(json.data) ? json.data.map(it => ({ id: it.id, name: it.name })) : [] } catch(e) { this.studentOptions = [] }
    },
    async loadClasses() {
      try { const res = await fetch('/api/class/list'); const json = await res.json(); this.classOptions = Array.isArray(json.data) ? json.data : [] } catch(e) { this.classOptions = [] }
    },
    async loadClassrooms() {
      try { const res = await fetch('/api/base/classroom/list'); const json = await res.json(); this.classroomOptions = Array.isArray(json.data) ? json.data : [] } catch(e) { this.classroomOptions = [] }
    },
    async onStudentChange(val) {
      this.form.classId = null
      this.enrollOptions = []
      if (!val) return
      try {
        const res = await fetch(`/api/enroll/list?studentId=${val}`)
        const json = await res.json()
        const arr = Array.isArray(json.data) ? json.data : []
        // 仅展示状态有效的报名，附带班级中文名（尝试从 classOptions 反查）
        this.enrollOptions = arr.map(e => {
          const c = this.classOptions.find(it => it.id === e.classId)
          return { classId: e.classId, className: c ? (c.title || c.name || ('班级#' + c.id)) : ('班级#' + e.classId), status: e.status }
        })
      } catch(e) { this.enrollOptions = [] }
    },
    onClassChange(val) {
      const sel = this.classOptions.find(c => c.id === val)
      if (sel && sel.fee != null) this.form.unitPrice = Number(sel.fee) || 0
      // 切换班级时重置座位并刷新占用标记
      this.form.seatNo = null
      this.occupiedMap = {}
      this.loadOccupiedSeats()
      this.recalc()
    },
    onClassroomChange() {
      // 切换教室时重置座位并刷新占用与标签映射
      this.form.seatNo = null
      this.seatLabelMap = {}
      this.occupiedMap = {}
      this.loadOccupiedSeats()
      this.loadSeatLabels()
    },
    async onSeatDropdownOpen(open) {
      if (open) {
        if (!this.seatLabelMap || Object.keys(this.seatLabelMap).length === 0) {
          await this.loadSeatLabels()
        }
        await this.loadOccupiedSeats()
      }
    },
    async loadSeatLabels() {
      this.seatLabelMap = {}
      if (!this.form.classroomId) return
      try {
        const res = await fetch(`/api/base/classroom/seats/${this.form.classroomId}`)
        const json = await res.json()
        const arr = Array.isArray(json.data) ? json.data : []
        const map = {}
        let seq = 0
        for (const s of arr) {
          const usable = !!s.usable
          if (!usable) continue
          let no = s.seatNo != null ? Number(s.seatNo) : null
          if (!Number.isFinite(no) || no <= 0) { seq += 1; no = seq }
          map[no] = String(s.label || '')
        }
        this.seatLabelMap = map
      } catch(e) { /* 忽略座位标签加载错误 */ }
    },
    async loadOccupiedSeats() {
      this.occupiedMap = {}
      if (!this.form.classroomId || !this.form.classId) return
      try {
        const res = await fetch(`/api/finance/settlement/occupied-seats?classroomId=${this.form.classroomId}&classId=${this.form.classId}`)
        const json = await res.json(); const arr = Array.isArray(json.data) ? json.data : []
        const map = {}
        arr.forEach(it => { const no = Number(it.seatNo); if (Number.isFinite(no) && no>0) map[no] = String(it.studentName||'已占用') })
        this.occupiedMap = map
      } catch(e) { /* 忽略占用加载错误 */ }
    },
    async openSeatPreview() {
      if (!this.form.classroomId) { this.$message && this.$message.warning('请先选择教室'); return }
      this.seatPreviewVisible = true; this.seatLoading = true; this.seatGrid = []
      try {
        const res = await fetch(`/api/base/classroom/seats/${this.form.classroomId}`)
        const json = await res.json()
        const arr = Array.isArray(json.data) ? json.data : []
        const maxRow = arr.reduce((m, s) => Math.max(m, Number(s.rowIndex||0)), 0)
        const maxCol = arr.reduce((m, s) => Math.max(m, Number(s.colIndex||0)), 0)
        const rows = maxRow + 1; const cols = maxCol + 1
        const grid = []
        let seq = 0
        for (let r=0; r<rows; r++) {
          const row = []
          for (let c=0; c<cols; c++) {
            const s = arr.find(x => Number(x.rowIndex)===r && Number(x.colIndex)===c) || { usable: false, label: `${String.fromCharCode(65+r)}${c+1}` }
            const usable = !!s.usable
            let no = s.seatNo != null ? Number(s.seatNo) : null
            if (!Number.isFinite(no) || no <= 0) { seq += 1; no = seq }
            const occupiedName = this.occupiedMap[no] || ''
            row.push({ label: String(s.label||`${String.fromCharCode(65+r)}${c+1}`), usable, seatNo: no, occupiedName })
          }
          grid.push(row)
        }
        this.seatGrid = grid
      } catch(e) { /* 忽略座位预览加载错误 */ }
      finally { this.seatLoading = false }
    },
    onPickSeat(seat) {
      if (!seat || !seat.usable || seat.occupiedName) return
      this.form.seatNo = seat.seatNo
      this.seatPreviewVisible = false
    },
    async recalc() {
      try {
        const payload = { opType: 'renew', unitPrice: this.form.unitPrice, hours: this.form.hours, textbookFee: this.form.textbookFee, discount: this.form.discount }
        const res = await fetch('/api/finance/settlement/calculate', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
        const json = await res.json()
        if (json && json.code === 200) this.form.totalFee = Number((json.data && json.data.totalFee) || 0)
      } catch(e) { /* no-op */ }
    },
    genReceiptNo() {
      const ts = new Date()
      const yyyy = ts.getFullYear(); const mm = String(ts.getMonth()+1).padStart(2,'0'); const dd = String(ts.getDate()).padStart(2,'0')
      const hh = String(ts.getHours()).padStart(2,'0'); const mi = String(ts.getMinutes()).padStart(2,'0'); const ss = String(ts.getSeconds()).padStart(2,'0')
      return `RC-${yyyy}${mm}${dd}-${hh}${mi}${ss}`
    },
    async saveRenew() {
      if (!this.form.studentId || !this.form.classId) { this.$message && this.$message.error('请先选择学员与已报班级'); return }
      if (!this.form.classroomId || !this.form.seatNo) { this.$message && this.$message.warning('请填写教室与座位'); return }
      this.saving = true
      try {
        const payload = { opType: 'renew', studentId: Number(this.form.studentId), classId: Number(this.form.classId), classroomId: Number(this.form.classroomId), seatNo: Number(this.form.seatNo), unitPrice: Number(this.form.unitPrice), hours: Number(this.form.hours), textbookFee: Number(this.form.textbookFee), discount: Number(this.form.discount), totalFee: Number(this.form.totalFee), method: this.form.method, receiptNo: this.form.receiptNo || this.genReceiptNo(), receiver: this.form.receiver, voucherUrl: this.form.voucherUrl, status: 'paid' }
        const res = await fetch('/api/finance/settlement/save', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
        const json = await res.json()
        if (json && json.code === 200) { this.$message && this.$message.success('续费记录已保存'); this.loadRecent() }
        else if (json && json.code === 409) { this.$message && this.$message.error('该座位已被占用，请重新选择'); }
        else { throw new Error('保存失败') }
      } catch(e) { this.$message && this.$message.error(e.message || '保存失败') }
      this.saving = false
    },
    async loadRecent() {
      try { const res = await fetch('/api/finance/settlement/list'); const json = await res.json(); const arr = Array.isArray(json.data) ? json.data : []; const sid = this.form.studentId; this.recentList = arr.filter(r => !sid || r.studentId === sid) } catch(e) { this.recentList = [] }
    }
  },
  created() {
    this.loadStudents();
    this.loadClasses();
    this.loadClassrooms();
    this.recalc();
    this.loadRecent();
    // 路由参数预选学员并加载其报班及近期缴费
    try {
      const q = (this.$route && this.$route.query) ? this.$route.query : {}
      if (q && q.studentId) {
        const sid = Number(q.studentId) || null
        this.form.studentId = sid
        // 预加载该学员的已报班与占用座位信息
        this.onStudentChange(sid)
        this.loadRecent()
      }
    } catch(e) { /* no-op */ }
  }
}
</script>

<style scoped>
.renew-manage { background: #fff; padding: 16px; }
.seat-grid { display: flex; flex-direction: column; gap: 8px; }
.seat-row { display: flex; gap: 8px; }
.seat-cell { width: 80px; height: 60px; border: 1px solid #ddd; border-radius: 6px; display: flex; flex-direction: column; align-items: center; justify-content: center; cursor: pointer; background: #fafafa; }
.seat-cell.disabled { cursor: not-allowed; opacity: 0.5; background: #f5f5f5; }
.seat-cell.occupied { border-color: #ff7875; background: #fff1f0; }
.seat-cell.selected { border-color: #52c41a; box-shadow: 0 0 0 2px rgba(82,196,26,0.2); }
.seat-label { font-weight: 600; }
.seat-no { font-size: 12px; color: #888; }
.seat-name { font-size: 12px; color: #cf1322; }
</style>