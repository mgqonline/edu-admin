<template>
  <div class="fee-manage">
    <a-page-header title="学员报班缴费" sub-title="选择班级、教室与座位后计算费用" />

    <a-card bordered title="报名与费用" style="margin-bottom: 12px">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 12 }">
        <a-form-item label="学员">
          <a-select v-model="calc.studentId" placeholder="选择学员" style="width:100%" allowClear>
            <a-select-option v-for="s in studentOptions" :key="s.id" :value="s.id">{{ s.name }}（ID: {{ s.id }}）</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="报班班级"><a-select v-model="calc.classId" placeholder="选择班级" style="width: 100%" @change="onClassChange">
          <a-select-option v-for="c in classOptions" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
        </a-select></a-form-item>
        <a-form-item label="教室">
          <a-select v-model="calc.classroomId" placeholder="选择教室" style="width:100%" allowClear @change="onClassroomChange">
            <a-select-option v-for="r in classroomOptions" :key="r.id" :value="r.id">{{ r.name }}（可用座位：{{ Number(r.usableSeats||r.capacity||0) }}）</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="座位">
          <a-space>
            <a-select v-model="calc.seatNo" placeholder="选择座位" style="width:300px" :disabled="!calc.classroomId || seatOptions.length===0" allowClear @dropdownVisibleChange="onSeatDropdownOpen">
              <a-select-option v-for="n in seatOptions" :key="n" :value="n" :disabled="!!occupiedMap[n]">
                {{ n }} 号 - {{ seatLabelMap[n] || '未知' }}
                <template v-if="occupiedMap[n]">（已占用：{{ occupiedMap[n] }}）</template>
              </a-select-option>
            </a-select>
            <a-button :disabled="!calc.classroomId" @click="openSeatPreview">预览座位</a-button>
          </a-space>
        </a-form-item>
        <a-form-item label="课程单价"><a-input-number v-model="calc.unitPrice" :min="0" style="width: 100%" /></a-form-item>
        <a-form-item label="课时数"><a-input-number v-model="calc.hours" :min="0" style="width: 100%" /></a-form-item>
        <a-form-item label="教材费"><a-input-number v-model="calc.textbookFee" :min="0" style="width: 100%" /></a-form-item>
        <a-form-item label="优惠活动"><a-input-number v-model="calc.discount" :min="0" style="width: 100%" /></a-form-item>
        <a-form-item :wrapper-col="{ span: 12, offset: 6 }">
          <a-space>
            <a-button type="primary" @click="calculate">计算费用</a-button>
            <span>总费用：<strong>{{ totalFeeDisplay }}</strong></span>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card bordered title="收费与票据" style="margin-bottom: 12px">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 12 }">
        <a-form-item label="支付方式">
          <a-select v-model="pay.method" placeholder="选择支付方式">
            <a-select-option value="cash">现金</a-select-option>
            <a-select-option value="wechat">微信</a-select-option>
            <a-select-option value="alipay">支付宝</a-select-option>
            <a-select-option value="bankcard">银行卡</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="支付凭证">
          <a-upload :before-upload="beforeUpload" :file-list="voucherList" list-type="picture">
            <a-button>上传凭证</a-button>
          </a-upload>
        </a-form-item>
        <a-form-item label="收据编号"><a-input v-model="pay.receiptNo" placeholder="自动生成，可覆盖" /></a-form-item>
        <a-form-item label="领取人"><a-input v-model="pay.receiver" placeholder="填写领取人" /></a-form-item>
        <a-form-item :wrapper-col="{ span: 12, offset: 6 }">
          <a-space>
            <a-button type="primary" @click="saveSettlement">保存收费记录</a-button>
            <a-button @click="loadList">刷新列表</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card bordered title="欠费管理">
      <a-table :data-source="list" :columns="columns" rowKey="id" :pagination="false">
        <template v-slot:opType="slotProps">
          <a-tag color="geekblue" v-if="((slotProps && slotProps.text !== undefined) ? slotProps.text : slotProps) === 'renew'">续费</a-tag>
          <a-tag color="blue" v-else>报名</a-tag>
        </template>
        <template v-slot:status="slotProps">
          <a-tag color="red" v-if="((slotProps && slotProps.text !== undefined) ? slotProps.text : slotProps) === 'unpaid'">未缴清</a-tag>
          <a-tag color="green" v-else>已支付</a-tag>
        </template>
        <template v-slot:action="slotProps">
          <a-space>
            <a-button size="small" @click="markArrears(slotProps && slotProps.record ? slotProps.record : null)" v-if="slotProps && slotProps.record && slotProps.record.status !== 'unpaid'">标记欠费</a-button>
            <a-button size="small" type="primary" @click="sendReminder(slotProps && slotProps.record ? slotProps.record : null)" v-if="slotProps && slotProps.record && slotProps.record.status === 'unpaid'">发送提醒</a-button>
            <a-button size="small" danger @click="releaseSeat(slotProps && slotProps.record ? slotProps.record : null)" v-if="slotProps && slotProps.record && slotProps.record.seatNo">释放座位</a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>
    <a-modal v-model="seatPreviewVisible" title="座位预览与选择" :footer="null" width="720px">
    <div v-if="seatLoading">加载座位中...</div>
    <div v-else class="seat-grid">
      <div v-for="(row, rIdx) in seatGrid" :key="'row-'+rIdx" class="seat-row">
        <div v-for="seat in row" :key="seat.label" class="seat-cell" :class="{ disabled: !seat.usable, occupied: !!seat.occupiedName, selected: calc.seatNo === seat.seatNo }" @click="onPickSeat(seat)">
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
  name: 'FeeManage',
  data() {
    return {
      classOptions: [],
      classroomOptions: [],
      studentOptions: [],
      calc: { studentId: null, classId: null, classroomId: null, seatNo: null, unitPrice: 0, hours: 0, textbookFee: 0, discount: 0 },
      totalFee: 0,
      pay: { method: '', receiptNo: '', receiver: '', voucherUrl: '' },
      voucherList: [],
      list: [],
      seatPreviewVisible: false,
      seatLoading: false,
      seatGrid: [],
      seatLabelMap: {},
      occupiedMap: {},
      columns: [
        { title: '学员ID', dataIndex: 'studentId', key: 'studentId' },
        { title: '类型', dataIndex: 'opType', key: 'opType', scopedSlots: { customRender: 'opType' } },
        { title: '班级ID', dataIndex: 'classId', key: 'classId' },
        { title: '教室ID', dataIndex: 'classroomId', key: 'classroomId' },
        { title: '座位号', dataIndex: 'seatNo', key: 'seatNo' },
        { title: '总费用', dataIndex: 'totalFee', key: 'totalFee' },
        { title: '支付方式', dataIndex: 'method', key: 'method' },
        { title: '收据编号', dataIndex: 'receiptNo', key: 'receiptNo' },
        { title: '领取人', dataIndex: 'receiver', key: 'receiver' },
        { title: '状态', dataIndex: 'status', key: 'status', scopedSlots: { customRender: 'status' } },
        { title: '操作', key: 'action', scopedSlots: { customRender: 'action' } }
      ]
    }
  },
  computed: {
    totalFeeDisplay() { return Number(this.totalFee || 0).toFixed(2) },
    seatOptions() {
      if (!this.calc.classroomId) return []
      const found = (this.classroomOptions||[]).find(r => r.id === this.calc.classroomId)
      const seats = Number(found && (found.usableSeats != null ? found.usableSeats : found.capacity)) || 0
      const arr = []
      for (let i = 1; i <= seats; i++) arr.push(i)
      return arr
    }
  },
  methods: {
    async loadStudentOptions() {
      try {
        const res = await fetch('/api/student/list')
        const json = await res.json()
        const arr = Array.isArray(json.data) ? json.data : []
        this.studentOptions = arr.map(it => ({ id: it.id, name: it.name }))
      } catch(e) { this.studentOptions = [] }
    },
    async loadClassOptions() {
      try { const res = await fetch('/api/class/list'); const json = await res.json(); this.classOptions = Array.isArray(json.data) ? json.data : [] } catch(e) { this.classOptions = [] }
    },
    async loadClassroomOptions() {
      try { const res = await fetch('/api/base/classroom/list'); const json = await res.json(); this.classroomOptions = Array.isArray(json.data) ? json.data : [] } catch(e) { this.classroomOptions = [] }
    },
    onClassChange(val) {
      const sel = this.classOptions.find(c => c.id === val)
      if (sel && sel.fee != null) this.calc.unitPrice = Number(sel.fee) || 0
      // 切换班级时重置座位并刷新占用标记
      this.calc.seatNo = null
      this.occupiedMap = {}
      this.loadOccupiedSeats()
    },
    onClassroomChange() {
      // 切换教室时重置座位并刷新占用与标签映射
      this.calc.seatNo = null
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
      if (!this.calc.classroomId) return
      try {
        const res = await fetch(`/api/base/classroom/seats/${this.calc.classroomId}`)
        const json = await res.json()
        const arr = Array.isArray(json.data) ? json.data : []
        // 构建 seatNo -> 标签 映射，优先使用后端 seatNo
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
      if (!this.calc.classroomId || !this.calc.classId) return
      try {
        const res = await fetch(`/api/finance/settlement/occupied-seats?classroomId=${this.calc.classroomId}&classId=${this.calc.classId}`)
        const json = await res.json(); const arr = Array.isArray(json.data) ? json.data : []
        const map = {}
        arr.forEach(it => { const no = Number(it.seatNo); if (Number.isFinite(no) && no>0) map[no] = String(it.studentName||'已占用') })
        this.occupiedMap = map
      } catch(e) { /* 忽略占用加载错误 */ }
    },
    async openSeatPreview() {
      if (!this.calc.classroomId) { this.$message && this.$message.warning('请先选择教室'); return }
      this.seatPreviewVisible = true; this.seatLoading = true; this.seatGrid = [];
      try {
        const res = await fetch(`/api/base/classroom/seats/${this.calc.classroomId}`)
        const json = await res.json()
        const arr = Array.isArray(json.data) ? json.data : []
        // 计算 row/col 维度
        const maxRow = arr.reduce((m, s) => Math.max(m, Number(s.rowIndex||0)), 0)
        const maxCol = arr.reduce((m, s) => Math.max(m, Number(s.colIndex||0)), 0)
        const rows = maxRow + 1; const cols = maxCol + 1
        // 按行优先扁平可用座位，生成 seatNo 映射
        const flat = []; const grid = []
        for (let r=0; r<rows; r++) { const row = []; for (let c=0; c<cols; c++) {
          const s = arr.find(x => Number(x.rowIndex)===r && Number(x.colIndex)===c) || { usable: false, label: `${String.fromCharCode(65+r)}${c+1}` }
          row.push({ rowIndex:r, colIndex:c, label: s.label, usable: !!s.usable, seatNo: (s && s.seatNo!=null ? Number(s.seatNo) : null), occupiedName: '' })
        } grid.push(row) }
        for (let r=0; r<grid.length; r++) { for (let c=0; c<grid[r].length; c++) { const cell = grid[r][c]; if (cell.usable) { flat.push({ r, c }) } } }
        // 若后端未返回 seatNo，则按行优先重新编号
        if (!arr.some(s => s && s.seatNo != null)) {
          flat.forEach((pos, idx) => { grid[pos.r][pos.c].seatNo = idx+1 })
        }
        // 叠加占用信息
        let occMap = {}
        if (this.calc.classId) {
          try {
            const occRes = await fetch(`/api/finance/settlement/occupied-seats?classroomId=${this.calc.classroomId}&classId=${this.calc.classId}`)
            const occJson = await occRes.json(); const occArr = Array.isArray(occJson.data) ? occJson.data : []
            occArr.forEach(it => { occMap[Number(it.seatNo)] = String(it.studentName||'已占用') })
            this.occupiedMap = occMap
          } catch (e) {}
        }
        for (let r=0; r<grid.length; r++) { for (let c=0; c<grid[r].length; c++) {
          const seat = grid[r][c]; if (seat.seatNo && occMap[seat.seatNo]) { seat.occupiedName = occMap[seat.seatNo]; seat.usable = false }
        } }
        this.seatGrid = grid
      } catch(e) { this.$message && this.$message.error('座位数据加载失败'); this.seatPreviewVisible = false }
      finally { this.seatLoading = false }
    },
    onPickSeat(seat) {
      if (!seat || !seat.usable || seat.occupiedName) return
      this.calc.seatNo = seat.seatNo
      this.seatPreviewVisible = false
    },
    async calculate() {
      try {
        const payload = { opType: 'enroll', unitPrice: Number(this.calc.unitPrice||0), hours: Number(this.calc.hours||0), textbookFee: Number(this.calc.textbookFee||0), discount: Number(this.calc.discount||0) }
        const res = await fetch('/api/finance/settlement/calculate', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
        const json = await res.json();
        if (json && json.code === 200) this.totalFee = Number((json.data && json.data.totalFee) || 0)
        else throw new Error('计算失败')
      } catch(e) {
        const { unitPrice, hours, textbookFee, discount } = this.calc
        const total = Number(unitPrice||0) * Number(hours||0) + Number(textbookFee||0) - Number(discount||0)
        this.totalFee = Math.max(0, Number.isFinite(total) ? total : 0)
      }
    },
    beforeUpload(file) {
      // 简化：不实际上传，生成本地预览 URL 并保存
      const url = URL.createObjectURL(file)
      this.pay.voucherUrl = url
      this.voucherList = [{ uid: file.uid || Date.now(), name: file.name, status: 'done', url }]
      return false
    },
    genReceiptNo() {
      const d = new Date(); const y = d.getFullYear(); const m = String(d.getMonth()+1).padStart(2,'0'); const day = String(d.getDate()).padStart(2,'0'); const h = String(d.getHours()).padStart(2,'0'); const mm = String(d.getMinutes()).padStart(2,'0'); const s = String(d.getSeconds()).padStart(2,'0'); const rnd = Math.random().toString(36).slice(2,6).toUpperCase();
      return `RC-${y}${m}${day}${h}${mm}${s}-${rnd}`
    },
    async saveSettlement() {
      if (!this.calc.studentId || !this.calc.classId) { this.$message && this.$message.error('请填写学员与班级'); return }
      if (!this.calc.classroomId || !this.calc.seatNo) { this.$message && this.$message.warning('请填写教室与座位'); return }
      if (!this.totalFee || Number(this.totalFee) <= 0) { this.calculate() }
      const payload = {
        opType: 'enroll',
        studentId: Number(this.calc.studentId), classId: Number(this.calc.classId), classroomId: Number(this.calc.classroomId), seatNo: Number(this.calc.seatNo),
        unitPrice: Number(this.calc.unitPrice||0), hours: Number(this.calc.hours||0), textbookFee: Number(this.calc.textbookFee||0), discount: Number(this.calc.discount||0),
        totalFee: Number(this.totalFee||0), method: this.pay.method || '', receiptNo: this.pay.receiptNo || this.genReceiptNo(), receiver: this.pay.receiver || '', voucherUrl: this.pay.voucherUrl || '', status: 'paid'
      }
      try {
        const res = await fetch('/api/finance/settlement/save', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
        const json = await res.json()
        if (json && json.code === 200) { this.$message && this.$message.success('收费记录已保存'); this.loadList() }
        else if (json && json.code === 409) { this.$message && this.$message.error('该座位已被占用，请重新选择'); }
        else { throw new Error('保存失败') }
      } catch(e) { this.$message && this.$message.error('保存失败：接口不可用或网络错误') }
    },
    async loadList() {
      try { const res = await fetch('/api/finance/settlement/list'); const json = await res.json(); this.list = Array.isArray(json.data) ? json.data : [] } catch(e) { this.list = [] }
    },
    async markArrears(rec) {
      try { const res = await fetch(`/api/finance/arrears/mark/${rec.id}`, { method: 'PUT' }); const json = await res.json(); if (json && json.code === 200) { this.$message && this.$message.success('已标记欠费'); this.loadList() } else { throw new Error('失败') } } catch(e) { this.$message && this.$message.error('标记失败') }
    },
    async sendReminder(rec) {
      const payload = { studentId: rec.studentId, amountDue: rec.totalFee, channels: ['sms','wechat'] }
      try { const res = await fetch('/api/finance/reminder/send', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) }); const json = await res.json(); if (json && json.code === 200) { this.$message && this.$message.success('提醒已发送') } else { throw new Error('失败') } } catch(e) { this.$message && this.$message.error('提醒失败') }
    },
    async releaseSeat(rec) {
      if (!rec || !rec.id) return
      try {
        const res = await fetch(`/api/finance/settlement/release-seat/${rec.id}`, { method: 'PUT' })
        const json = await res.json()
        if (json && json.code === 200) {
          this.$message && this.$message.success('座位已释放')
          await this.loadList()
          // 若当前筛选与记录相同班级/教室，刷新占用
          if (this.calc.classId && this.calc.classroomId && rec.classId === this.calc.classId && rec.classroomId === this.calc.classroomId) {
            await this.loadOccupiedSeats()
          }
        } else { throw new Error('失败') }
      } catch(e) { this.$message && this.$message.error('释放失败') }
    }
  },
  created() {
    this.loadStudentOptions();
    this.loadClassOptions();
    this.loadClassroomOptions();
    this.loadList();
    this.pay.receiptNo = this.genReceiptNo();
    // 路由参数预选学员
    try {
      const q = (this.$route && this.$route.query) ? this.$route.query : {}
      if (q && q.studentId) { this.calc.studentId = Number(q.studentId) || null }
    } catch(e) { /* no-op */ }
  }
}
</script>

<style scoped>
.fee-manage { background: #fff; padding: 16px; }
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