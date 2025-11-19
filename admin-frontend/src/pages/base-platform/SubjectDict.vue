<template>
  <div class="subject-dict-page">
    <a-page-header title="科目维护" sub-title="维护科目字典（名称、状态、排序）" />

    <a-card bordered>
      <a-space style="margin-bottom: 12px" wrap>
        <a-input v-model="form.name" placeholder="科目名称" style="width: 200px" />
        <a-select v-model="form.status" style="width: 140px">
          <a-select-option value="enabled">启用</a-select-option>
          <a-select-option value="disabled">禁用</a-select-option>
        </a-select>
        <a-input-number v-model="form.sortOrder" :min="0" :max="999" style="width: 120px" />
        <a-button type="primary" @click="submit" :loading="submitting">保存</a-button>
        <a-select v-model="filters.status" placeholder="状态筛选" style="width: 160px" @change="loadData" allowClear>
          <a-select-option value="enabled">启用</a-select-option>
          <a-select-option value="disabled">禁用</a-select-option>
        </a-select>
        <a-button @click="loadData" :loading="loading">刷新</a-button>
      </a-space>

      <a-table :data-source="list" :columns="columns" rowKey="id" :pagination="false">
        <template slot="status" slot-scope="text">
          <a-tag :color="text === 'enabled' ? 'green' : 'red'">
            {{ text === 'enabled' ? '启用' : '禁用' }}
          </a-tag>
        </template>
        <template slot="action" slot-scope="text, record">
          <a-space>
            <a-button size="small" @click="toggleStatus(record)">
              {{ record && record.status === 'enabled' ? '禁用' : '启用' }}
            </a-button>
            <a-popconfirm title="更新排序？" @confirm="updateSort(record)">
              <a-button size="small">更新排序</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'SubjectDictPage',
  data() {
    return {
      loading: false,
      submitting: false,
      list: [],
      filters: { status: '' },
      form: { name: '', status: 'enabled', sortOrder: 0 },
      columns: [
        { title: '名称', dataIndex: 'name', key: 'name' },
        { title: '状态', dataIndex: 'status', key: 'status', scopedSlots: { customRender: 'status' } },
        { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder' },
        { title: '操作', key: 'action', scopedSlots: { customRender: 'action' } }
      ]
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const params = new URLSearchParams()
        if (this.filters.status) params.append('status', this.filters.status)
        const res = await fetch(`/api/dict/subject/list${params.toString() ? '?' + params.toString() : ''}`)
        const json = await res.json()
        if (json && json.code === 200) {
          this.list = json.data || []
        } else {
          this.list = []
        }
      } catch (e) {
        this.list = []
      } finally {
        this.loading = false
      }
    },
    async submit() {
      if (!this.form.name) {
        this.$message && this.$message.warning('请输入科目名称')
        return
      }
      this.submitting = true
      try {
        const res = await fetch('/api/dict/subject/save', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ name: this.form.name, status: this.form.status, sortOrder: this.form.sortOrder })
        })
        const json = await res.json()
        if (json && json.code === 200) {
          this.$message && this.$message.success('保存成功')
          this.form = { name: '', status: 'enabled', sortOrder: 0 }
          this.loadData()
        } else {
          this.$message && this.$message.error(json.message || '保存失败')
        }
      } catch (e) {
        this.$message && this.$message.error('保存失败')
      } finally {
        this.submitting = false
      }
    },
    async toggleStatus(record) {
      if (!record || !record.name) return
      try {
        const newStatus = record.status === 'enabled' ? 'disabled' : 'enabled'
        const res = await fetch('/api/dict/subject/save', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ name: record.name, status: newStatus, sortOrder: record.sortOrder || 0 })
        })
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
        const res = await fetch('/api/dict/subject/save', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ name: record.name, status: record.status || 'enabled', sortOrder: Number(record.sortOrder || 0) })
        })
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
.subject-dict-page { background: #fff; padding: 16px; }
</style>