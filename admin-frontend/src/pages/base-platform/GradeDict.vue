<template>
  <div class="grade-dict-page">
    <a-page-header title="年级维护" sub-title="维护年级字典（名称、状态、排序）" />

    <a-card bordered>
      <a-space style="margin-bottom: 12px" wrap>
        <a-button type="primary" @click="openAdd">新增年级</a-button>
        <a-input-search v-model="filters.q" placeholder="按名称关键词搜索" style="width: 220px" @search="loadData" allowClear />
        <a-select v-model="filters.campusId" placeholder="学校筛选" style="width: 200px" @change="loadData" allowClear>
          <a-select-option v-for="c in campusOptions" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
        </a-select>
        <a-select v-model="filters.status" placeholder="状态筛选" style="width: 160px" @change="loadData" allowClear>
          <a-select-option value="enabled">启用</a-select-option>
          <a-select-option value="disabled">禁用</a-select-option>
        </a-select>
        <a-button @click="loadData" :loading="loading">刷新</a-button>
      </a-space>

      <a-table :data-source="list" :columns="columns" rowKey="id" :pagination="pagination" @change="onTableChange">
        <template slot="campus" slot-scope="text, record">
          {{ (record && record.campusName) || campusNameById(text || (record && record.campusId)) || '-' }}
        </template>
        <template slot="status" slot-scope="text">
          <a-tag :color="text === 'enabled' ? 'green' : 'red'">{{ text === 'enabled' ? '启用' : '禁用' }}</a-tag>
        </template>
        <template slot="action" slot-scope="text, record">
          <a-space>
            <a-button size="small" :disabled="!record || record.id==null" @click="record && openEdit(record)">编辑</a-button>
            <a-popconfirm title="确认删除该年级？" v-if="record && record.id!=null" @confirm="deleteItem(record)">
              <a-button size="small" type="danger">删除</a-button>
            </a-popconfirm>
            <a-button size="small" :disabled="!record || record.id==null" @click="record && toggleStatus(record)">
              {{ (record && record.status) === 'enabled' ? '禁用' : '启用' }}
            </a-button>
            <a-popconfirm title="更新排序？" v-if="record && record.id!=null" @confirm="updateSort(record)">
              <a-button size="small">更新排序</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </a-card>
    <a-modal :visible="modal.visible" :title="modalTitle" @ok="saveModal" @cancel="closeModal">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="学校/校区">
          <a-select v-model="modal.form.campusId" placeholder="选择学校/校区">
            <a-select-option v-for="c in campusOptions" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="名称">
          <a-input v-model="modal.form.name" placeholder="请输入年级名称" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model="modal.form.status">
            <a-select-option value="enabled">启用</a-select-option>
            <a-select-option value="disabled">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model="modal.form.sortOrder" :min="0" :max="999" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
export default {
  name: 'GradeDictPage',
  data() {
    return {
      loading: false,
      list: [],
      campusOptions: [],
      filters: { status: '', campusId: null, q: '' },
      modal: { visible: false, mode: 'add', form: { id: null, campusId: null, name: '', status: 'enabled', sortOrder: 0 } },
      pagination: { current: 1, pageSize: 10, total: 0, showSizeChanger: true, pageSizeOptions: ['10','20','50','100'] },
      columns: [
        { title: '学校/校区', dataIndex: 'campusId', key: 'campusId', scopedSlots: { customRender: 'campus' } },
        { title: '名称', dataIndex: 'name', key: 'name' },
        { title: '状态', dataIndex: 'status', key: 'status', scopedSlots: { customRender: 'status' } },
        { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder' },
        { title: '操作', key: 'action', scopedSlots: { customRender: 'action' } }
      ]
    }
  },
  computed: {
    modalTitle() { return this.modal.mode === 'edit' ? '编辑年级' : '新增年级' }
  },
  created() {
    this.loadCampusOptions().then(() => this.loadData())
  },
  methods: {
    campusNameById(id) {
      const found = this.campusOptions.find(c => String((c && c.id) != null ? c.id : c.campusId) === String(id))
      return found ? found.name : ''
    },
    async loadCampusOptions() {
      try {
        // 主数据源：基础平台校区列表（包含 id/name）
        const res = await fetch('/api/base/campus/list')
        const json = await res.json()
        let items = (json && json.code === 200) ? (json.data || []) : []
        // 兜底：系统模块的校园列表（返回 campusId/name），做字段归一化
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
    async loadData() {
      this.loading = true
      try {
        const params = new URLSearchParams()
        if (this.filters.campusId) params.append('campusId', this.filters.campusId)
        if (this.filters.status) params.append('status', this.filters.status)
        if (this.filters.q) params.append('q', this.filters.q)
        params.append('page', String(this.pagination.current || 1))
        params.append('size', String(this.pagination.pageSize || 10))
        const res = await fetch(`/api/dict/grade/list${'?' + params.toString()}`)
        const json = await res.json()
        if (json && json.code === 200) {
          const root = json.data || {}
          const arr = Array.isArray(root.items) ? root.items : (Array.isArray(json.data) ? json.data : [])
          this.list = arr
          this.pagination = { ...this.pagination, total: Number(root.total || arr.length) }
        } else {
          this.list = []
        }
      } catch (e) {
        this.list = []
      }
      this.loading = false
    },
    onTableChange(pager) {
      this.pagination.current = pager.current
      this.pagination.pageSize = pager.pageSize
      this.loadData()
    },
    openAdd() {
      this.modal.mode = 'add'
      this.modal.form = { id: null, campusId: this.filters.campusId || (this.campusOptions[0] && this.campusOptions[0].id) || null, name: '', status: 'enabled', sortOrder: 0 }
      this.modal.visible = true
    },
    openEdit(record) {
      this.modal.mode = 'edit'
      this.modal.form = { id: record.id, campusId: record.campusId, name: record.name, status: record.status || 'enabled', sortOrder: Number(record.sortOrder || 0) }
      this.modal.visible = true
    },
    closeModal() { this.modal.visible = false },
    async saveModal() {
      const f = this.modal.form
      if (!f.campusId || !f.name) { this.$message && this.$message.warning('请填写学校与名称'); return }
      try {
        const campusIdNum = Number(f.campusId)
        if (!Number.isFinite(campusIdNum)) { this.$message && this.$message.error('无效的学校/校区ID'); return }
        if (this.modal.mode === 'add') {
          const res = await fetch('/api/dict/grade/save', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ campusId: campusIdNum, name: String(f.name).trim(), status: f.status, sortOrder: Number(f.sortOrder || 0) }) })
          const json = await res.json()
          if (json && json.code === 200) { this.$message && this.$message.success('新增成功'); this.modal.visible = false; this.loadData() } else { throw new Error((json && json.msg) ? json.msg : '新增失败') }
        } else {
          const res = await fetch(`/api/dict/grade/update/${f.id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ campusId: campusIdNum, name: String(f.name).trim(), status: f.status, sortOrder: Number(f.sortOrder || 0) }) })
          const json = await res.json()
          if (json && json.code === 200) { this.$message && this.$message.success('更新成功'); this.modal.visible = false; this.loadData() } else { throw new Error((json && json.msg) ? json.msg : '更新失败') }
        }
      } catch (e) { this.$message && this.$message.error(e.message || '操作失败'); console.warn('Grade save error:', e) }
    },
    async deleteItem(record) {
      try {
        const res = await fetch(`/api/dict/grade/delete/${record.id}`, { method: 'DELETE' })
        const json = await res.json()
        if (json && json.code === 200) { this.$message && this.$message.success('删除成功'); this.loadData() } else { throw new Error('删除失败') }
      } catch (e) { this.$message && this.$message.error('删除失败') }
    },
    async toggleStatus(record) {
      if (!record || !record.name) return
      try {
        const newStatus = record.status === 'enabled' ? 'disabled' : 'enabled'
        const res = await fetch(`/api/dict/grade/update/${record.id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ campusId: record.campusId, name: record.name, status: newStatus, sortOrder: Number(record.sortOrder || 0) }) })
        const json = await res.json()
        if (json && json.code === 200) {
          this.$message && this.$message.success('状态已更新')
          this.loadData()
        } else {
          throw new Error('更新失败')
        }
      } catch (e) {
        this.$message && this.$message.error('更新失败')
      }
    },
    async updateSort(record) {
      if (!record || !record.name) return
      try {
        const res = await fetch(`/api/dict/grade/update/${record.id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ campusId: record.campusId, name: record.name, status: record.status || 'enabled', sortOrder: Number(record.sortOrder || 0) }) })
        const json = await res.json()
        if (json && json.code === 200) {
          this.$message && this.$message.success('排序已更新')
          this.loadData()
        } else {
          throw new Error('更新失败')
        }
      } catch (e) {
        this.$message && this.$message.error('更新失败')
      }
    }
  }
}
</script>

<style>
.grade-dict-page { background: #fff; padding: 16px; }
.grade-dict-page .ant-page-header { padding: 0 0 12px 0; }
.grade-dict-page .ant-card { border-radius: 8px; }
.grade-dict-page .ant-table { margin-top: 8px; }
</style>