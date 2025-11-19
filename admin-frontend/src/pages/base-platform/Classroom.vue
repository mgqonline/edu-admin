<template>
  <div class="classroom-page">
    <a-page-header title="教室管理" sub-title="维护教室信息与容量" />
    <a-card>
      <div style="margin-bottom:8px; display:flex; gap:8px; align-items:center;">
        <a-select v-model="filters.campusId" placeholder="选择校区" style="width:220px" @change="loadData">
          <a-select-option v-for="c in campusOptions" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
        </a-select>
        <a-input v-model="filters.q" placeholder="按名称/编号搜索" style="width:240px" @pressEnter="loadData" />
        <a-button type="primary" icon="plus" @click="openAdd">新增教室</a-button>
        <a-button @click="loadData">查询</a-button>
      </div>
      <a-table :data-source="list" :loading="loading" :pagination="pagination" @change="onTableChange" :rowKey="row => row.id || row.roomCode" :columns="columns">
        <template slot="status" slot-scope="text"> 
          <a-tag :color="text==='enabled'?'green':'default'">{{ text==='enabled'?'启用':'停用' }}</a-tag>
        </template>
        <template slot="seatDims" slot-scope="text, record">
          <span>{{ record.seatRows || '-' }}×{{ record.seatCols || '-' }}</span>
        </template>
        <template slot="layout" slot-scope="text, record">
          <a-tag :color="record.seatMap ? 'blue' : 'default'">{{ record.seatMap ? '有' : '无' }}</a-tag>
          <a-button size="small" @click="openPreview(record)" style="margin-left:8px">预览</a-button>
        </template>
        <template slot="action" slot-scope="text, record">
          <a-button size="small" @click="openEdit(record)">编辑</a-button>
          <a-divider type="vertical" />
          <a-button size="small" @click="toggleStatus(record)">{{ record.status==='enabled' ? '停用' : '启用' }}</a-button>
          <a-divider type="vertical" />
          <a-popconfirm title="确认删除该教室？" @confirm="deleteItem(record)"><a-button size="small" type="danger">删除</a-button></a-popconfirm>
        </template>
      </a-table>
    </a-card>

    <a-modal :visible="modal.visible" :title="modalTitle" @ok="saveModal" @cancel="closeModal" :width="720">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="校区">
          <a-select v-model="modal.form.campusId" placeholder="选择校区" @change="onCampusChange">
            <a-select-option v-for="c in campusOptions" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="教室名称">
          <a-input v-model="modal.form.name" placeholder="如 教学楼302" />
        </a-form-item>
        <a-form-item label="教室编号">
          <a-input v-model="modal.form.roomCode" placeholder="如 A-302 或 ZB-01" />
        </a-form-item>
        <a-form-item label="容纳人数">
          <a-input-number v-model="modal.form.capacity" :min="0" :max="999" style="width:100%" @change="onCapacityChange" />
        </a-form-item>
        <a-form-item label="可用座位数">
          <a-input-number v-model="modal.form.usableSeats" :min="0" :max="999" style="width:100%" :disabled="seatGrid && seatGrid.length" />
          <div class="help">应小于或等于容纳人数</div>
        </a-form-item>
        <a-form-item label="座位行列">
          <a-space>
            <a-input-number v-model="modal.form.seatRows" :min="1" :max="50" placeholder="行数" @change="onSeatDimChange" />
            <span>×</span>
            <a-input-number v-model="modal.form.seatCols" :min="1" :max="50" placeholder="列数" @change="onSeatDimChange" />
            <a-button size="small" @click="resetSeatGrid">重置布局</a-button>
          </a-space>
          <div class="help">根据行列自动生成座位网格，点击方格可禁用/启用。</div>
        </a-form-item>
        <a-form-item label="座位布局">
          <div style="margin-bottom:8px">
            <a-space>
              <a-button size="small" @click="enableAllSeats" :disabled="!seatGrid || !seatGrid.length">全部启用</a-button>
              <a-button size="small" @click="disableAllSeats" :disabled="!seatGrid || !seatGrid.length">全部禁用</a-button>
            </a-space>
          </div>
          <div class="seat-grid" v-if="seatGrid && seatGrid.length">
            <div v-for="(row, rIdx) in seatGrid" :key="'r'+rIdx" class="seat-row">
              <div v-for="(cell, cIdx) in row" :key="'c'+cIdx" class="seat-cell" :class="cell? 'seat-ok':'seat-off'" @click="toggleSeat(rIdx, cIdx)">
                {{ rowLabel(rIdx) }}{{ cIdx+1 }}
              </div>
            </div>
          </div>
          <div v-else class="help">请输入行列生成座位布局</div>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model="modal.form.status">
            <a-select-option value="enabled">启用</a-select-option>
            <a-select-option value="disabled">停用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model="modal.form.note" :rows="3" placeholder="特殊说明" />
        </a-form-item>
      </a-form>
    </a-modal>
    <a-modal :visible="preview.visible" :title="preview.title" @ok="closePreview" @cancel="closePreview" :width="640">
      <div class="seat-grid" v-if="preview.grid && preview.grid.length">
        <div v-for="(row, rIdx) in preview.grid" :key="'pr'+rIdx" class="seat-row">
          <div v-for="(cell, cIdx) in row" :key="'pc'+cIdx" class="seat-cell" :class="cell? 'seat-ok':'seat-off'">
            {{ rowLabel(rIdx) }}{{ cIdx+1 }}
          </div>
        </div>
      </div>
      <div v-else class="help">暂无座位布局数据</div>
    </a-modal>
  </div>
  
</template>

<script>
export default {
  name: 'ClassroomManage',
  data(){
    return {
      loading: false,
      list: [],
      campusOptions: [],
      filters: { campusId: null, q: '' },
      pagination: { current: 1, pageSize: 10, total: 0, showSizeChanger: true, pageSizeOptions: ['10','20','50','100'] },
      modal: { visible: false, mode: 'add', form: { id: null, campusId: null, name: '', roomCode: '', capacity: 0, usableSeats: 0, seatRows: null, seatCols: null, seatMap: '', status: 'enabled', note: '' } },
      seatGrid: [],
      preview: { visible: false, title: '', grid: [] },
      columns: [
        { title: '校区', dataIndex: 'campusName', key: 'campusName' },
        { title: '教室名称', dataIndex: 'name', key: 'name' },
        { title: '教室编号', dataIndex: 'roomCode', key: 'roomCode' },
        { title: '容纳人数', dataIndex: 'capacity', key: 'capacity' },
        { title: '可用座位', dataIndex: 'usableSeats', key: 'usableSeats' },
        { title: '行×列', key: 'seatDims', scopedSlots: { customRender: 'seatDims' } },
        { title: '座位图', key: 'layout', scopedSlots: { customRender: 'layout' } },
        { title: '状态', dataIndex: 'status', key: 'status', scopedSlots: { customRender: 'status' } },
        { title: '操作', key: 'action', scopedSlots: { customRender: 'action' } }
      ]
    }
  },
  computed: {
    modalTitle(){ return this.modal.mode === 'edit' ? '编辑教室' : '新增教室' }
  },
  created(){ this.loadCampusOptions().then(() => this.loadData()) },
  methods: {
    async loadCampusOptions(){
      try {
        const r = await fetch('/api/base/campus/list')
        const j = await r.json()
        this.campusOptions = (j && j.code===200) ? (j.data||[]) : []
        if (!this.filters.campusId && this.campusOptions.length) this.filters.campusId = this.campusOptions[0].id
      } catch(e){ this.campusOptions = [] }
    },
    campusNameById(id){ const f = (this.campusOptions||[]).find(c => String(c.id)===String(id)); return f?f.name:'' },
    async loadData(){
      this.loading = true
      try {
        const p = new URLSearchParams(); if (this.filters.campusId) p.append('campusId', this.filters.campusId); if (this.filters.q) p.append('q', this.filters.q)
        p.append('page', String(this.pagination.current||1)); p.append('size', String(this.pagination.pageSize||10))
        const r = await fetch('/api/base/classroom/list' + (p.toString()?('?'+p.toString()):''))
        const j = await r.json()
        const payload = (j && j.code===200) ? (j.data||{}) : {}
        const raw = Array.isArray(payload) ? payload : (payload.items||[])
        this.list = raw.map(it => ({ ...it, campusName: this.campusNameById(it.campusId) }))
        this.pagination.total = Number(payload.total || raw.length)
      } catch(e){ this.list = [] } finally { this.loading = false }
    },
    onTableChange(pager){ this.pagination.current = pager.current; this.pagination.pageSize = pager.pageSize; this.loadData() },
    openAdd(){
      this.modal.mode='add';
      this.modal.form={ id:null, campusId: this.filters.campusId, name:'', roomCode:'', capacity:0, usableSeats:0, seatRows:null, seatCols:null, seatMap:'', status:'enabled', note:'' };
      this.seatGrid = [];
      this.modal.visible=true
    },
    openEdit(rec){
      if(!rec||rec.id==null){ this.$message&&this.$message.warning('无效数据'); return }
      this.modal.mode='edit';
      this.modal.form={ id:rec.id, campusId:rec.campusId, name:rec.name, roomCode:rec.roomCode, capacity:Number(rec.capacity||0), usableSeats:Number(rec.usableSeats||0), seatRows: rec.seatRows||null, seatCols: rec.seatCols||null, seatMap: rec.seatMap||'', status:rec.status||'enabled', note:rec.note||'' };
      this.initSeatGridFromForm();
      this.modal.visible=true
    },
    closeModal(){ this.modal.visible=false },
    onCampusChange(){ /* no-op; could filter by campus later */ },
    onCapacityChange(){
      const f = this.modal.form
      const cap = Number(f.capacity || 0)
      const rows = Number(f.seatRows || 0)
      const cols = Number(f.seatCols || 0)
      if (cap > 0 && (rows <= 0 || cols <= 0)) {
        const r = Math.max(1, Math.floor(Math.sqrt(cap)))
        const c = Math.max(1, Math.ceil(cap / r))
        f.seatRows = r
        f.seatCols = c
        this.onSeatDimChange()
      }
    },
    onSeatDimChange(){
      const f = this.modal.form
      const rows = Number(f.seatRows||0), cols = Number(f.seatCols||0)
      if(rows>0 && cols>0){
        // 容量与行列乘积对齐
        f.capacity = rows * cols
        // 重新生成网格
        this.seatGrid = Array.from({ length: rows }, () => Array.from({ length: cols }, () => true))
        // 可用座位自动与网格可用座位同步
        f.usableSeats = rows * cols
      }
    },
    resetSeatGrid(){ this.onSeatDimChange() },
    toggleSeat(r, c){
      if(!this.seatGrid || !this.seatGrid[r]) return
      this.$set(this.seatGrid[r], c, !this.seatGrid[r][c])
      this.syncUsableSeats()
    },
    rowLabel(r){ return r < 26 ? String.fromCharCode(65 + r) : (r+1) },
    enableAllSeats(){ if(!this.seatGrid || !this.seatGrid.length) return; this.seatGrid = this.seatGrid.map(row => row.map(() => true)); this.syncUsableSeats() },
    disableAllSeats(){ if(!this.seatGrid || !this.seatGrid.length) return; this.seatGrid = this.seatGrid.map(row => row.map(() => false)); this.syncUsableSeats() },
    syncUsableSeats(){
      const f = this.modal.form
      let cnt = 0
      for(const row of (this.seatGrid||[])) for(const cell of (row||[])) if(cell) cnt++
      f.usableSeats = cnt
    },
    initSeatGridFromForm(){
      const f = this.modal.form
      const rows = Number(f.seatRows||0), cols = Number(f.seatCols||0)
      if(rows>0 && cols>0){
        if(f.seatMap){
          try {
            const m = JSON.parse(f.seatMap)
            if(Array.isArray(m) && m.length===rows){ this.seatGrid = m.map(row => row.map(v => !!v)); this.syncUsableSeats(); return }
          } catch(e){ /* ignore parse error */ }
        }
        // 无 seatMap 时按行列生成全可用
        this.seatGrid = Array.from({ length: rows }, () => Array.from({ length: cols }, () => true))
        f.capacity = rows * cols
        this.syncUsableSeats()
      } else {
        this.seatGrid = []
      }
    },
    async saveModal(){
      const f = this.modal.form
      if (this.seatGrid && this.seatGrid.length) this.syncUsableSeats()
      if(!f.campusId){ this.$message&&this.$message.warning('请选择校区'); return }
      if(!f.name){ this.$message&&this.$message.warning('请输入教室名称'); return }
      const cap = Number(f.capacity||0), seats = Number(f.usableSeats||0)
      if(cap<0 || seats<0) { this.$message&&this.$message.warning('容量与座位不能为负数'); return }
      if(seats>cap) { this.$message&&this.$message.warning('可用座位数不能超过容纳人数'); return }
      // 行列校验与序列化 seatMap
      if(f.seatRows && f.seatCols){
        const rows = Number(f.seatRows), cols = Number(f.seatCols)
        if(rows*cols !== cap){ this.$message&&this.$message.warning('容量应等于座位行列乘积'); return }
        if(!this.seatGrid || this.seatGrid.length!==rows){ this.$message&&this.$message.warning('请先生成座位布局'); return }
        try { f.seatMap = JSON.stringify(this.seatGrid.map(row => row.map(v => v?1:0))) } catch(e){ f.seatMap = '' }
      } else {
        f.seatMap = ''
      }
      try {
        if(this.modal.mode==='add'){
          const r = await fetch('/api/base/classroom/save', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(f) })
          const j = await r.json(); if(j && j.code===200){ this.$message&&this.$message.success('新增成功'); this.modal.visible=false; this.loadData() } else { throw new Error((j&&j.msg)||'新增失败') }
        } else {
          const r = await fetch(`/api/base/classroom/update/${f.id}`, { method:'PUT', headers:{'Content-Type':'application/json'}, body: JSON.stringify(f) })
          const j = await r.json(); if(j && j.code===200){ this.$message&&this.$message.success('更新成功'); this.modal.visible=false; this.loadData() } else { throw new Error((j&&j.msg)||'更新失败') }
        }
      } catch(e){ this.$message&&this.$message.error(e.message||'操作失败') }
    },
    async openPreview(rec){
      if(!rec) return
      const rows = Number(rec.seatRows||0), cols = Number(rec.seatCols||0)
      let grid = []
      // 优先从座位表接口获取
      if (rec.id != null && rows>0 && cols>0) {
        try {
          const res = await fetch(`/api/base/classroom/seats/${rec.id}`)
          const json = await res.json()
          const items = Array.isArray(json && json.data) ? json.data : []
          if (items.length) {
            // 根据接口返回构造二维网格
            grid = Array.from({ length: rows }, () => Array.from({ length: cols }, () => true))
            for (const s of items) {
              const r = Number(s.rowIndex), c = Number(s.colIndex)
              if (r>=0 && r<rows && c>=0 && c<cols) grid[r][c] = !!s.usable
            }
          }
        } catch (e) { /* 接口不可用时回退到 seatMap */ }
      }
      // 回退：按 seatMap 或默认全启用
      if (!grid.length && rows>0 && cols>0) {
        if(rec.seatMap){
          try {
            const m = JSON.parse(rec.seatMap)
            if(Array.isArray(m) && m.length===rows) grid = m.map(row => row.map(v => !!v))
          } catch(e){ /* ignore */ }
        }
        if(!grid.length){ grid = Array.from({ length: rows }, () => Array.from({ length: cols }, () => true)) }
      }
      this.preview = { visible: true, title: `${rec.name||rec.roomCode||'座位布局预览'}`, grid }
    },
    closePreview(){ this.preview.visible = false },
    async deleteItem(rec){ if(!rec||rec.id==null){ this.$message&&this.$message.warning('无效数据'); return }
      try { const r = await fetch(`/api/base/classroom/delete/${rec.id}`, { method:'DELETE' }); const j = await r.json(); if(j && j.code===200){ this.$message&&this.$message.success('删除成功'); this.loadData() } else { throw new Error((j&&j.msg)||'删除失败') } } catch(e){ this.$message&&this.$message.error(e.message||'删除失败') }
    },
    async toggleStatus(rec){ if(!rec||rec.id==null){ this.$message&&this.$message.warning('无效数据'); return }
      try { const newStatus = rec.status==='enabled'?'disabled':'enabled'; const r = await fetch(`/api/base/classroom/update/${rec.id}`, { method:'PUT', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ ...rec, status:newStatus }) }); const j = await r.json(); if(j && j.code===200){ this.$message&&this.$message.success('状态已更新'); this.loadData() } else { throw new Error((j&&j.msg)||'更新失败') } } catch(e){ this.$message&&this.$message.error(e.message||'更新失败') }
    }
  }
}
</script>

<style>
.classroom-page { background:#fff; padding:16px; }
.classroom-page .ant-page-header { padding:0 0 12px 0; }
.classroom-page .help { font-size:12px; color:#888; margin-top:4px; }
.seat-grid { display: inline-block; border: 1px solid #f0f0f0; padding: 8px; background: #fafafa; }
.seat-row { display: flex; }
.seat-cell { width: 42px; height: 32px; margin: 4px; border-radius: 4px; display: flex; align-items: center; justify-content: center; font-size: 12px; cursor: pointer; user-select: none; border: 1px solid #d9d9d9; }
.seat-ok { background: #e6fffb; border-color: #87e8de; color: #006d75; }
.seat-off { background: #fff1f0; border-color: #ffa39e; color: #a8071a; }
</style>