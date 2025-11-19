<template>
  <div style="background:#fff; padding:16px">
    <a-page-header title="节假日管理" sub-title="维护法定节假日，供排课跳过" />

    <a-card bordered style="margin-bottom: 16px" title="查询与新增">
      <a-form layout="inline">
        <a-form-item label="年份">
          <a-select v-model="query.year" style="width:120px" allowClear placeholder="选择年份">
            <a-select-option v-for="y in yearOptions" :key="y" :value="y">{{ y }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="日期范围">
          <a-range-picker :value="query.range" @change="onRangeChange" style="width:240px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" :loading="loading" @click="loadList">查询</a-button>
            <a-button @click="resetQuery">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-divider />

      <a-space>
        <a-button type="primary" v-perm="'base:holiday:add'" @click="openCreate">新增节假日</a-button>
        <a-button @click="loadList" :loading="loading">刷新</a-button>
      </a-space>
    </a-card>

    <a-table :data-source="list" :columns="columns" rowKey="id" :loading="loading" />

    <a-modal :visible="createVisible" title="新增节假日" @ok="submitCreate" @cancel="createVisible=false" :confirmLoading="submitting">
      <a-form layout="vertical">
        <a-form-item label="日期"><CompatDatePicker v-model="createForm.date" style="width:100%" /></a-form-item>
        <a-form-item label="名称"><a-input v-model="createForm.name" placeholder="如：国庆节" /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal :visible="editVisible" title="编辑节假日" @ok="submitEdit" @cancel="editVisible=false" :confirmLoading="submitting">
      <a-form layout="vertical">
        <a-form-item label="日期"><CompatDatePicker v-model="editForm.date" style="width:100%" /></a-form-item>
        <a-form-item label="名称"><a-input v-model="editForm.name" /></a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
export default {
  name: 'HolidayManage',
  components: { CompatDatePicker: () => import('../../components/CompatDatePicker.vue') },
  data() {
    const now = new Date()
    const currentYear = now.getFullYear()
    return {
      loading: false,
      submitting: false,
      list: [],
      yearOptions: [currentYear - 1, currentYear, currentYear + 1, currentYear + 2],
      query: { year: currentYear, range: null },
      createVisible: false,
      editVisible: false,
      createForm: { date: null, name: '' },
      editForm: { id: null, date: null, name: '' },
      columns: [
        { title: '日期', dataIndex: 'dateOnly', customRender: (text) => this.formatDate(text) },
        { title: '名称', dataIndex: 'name' },
        { title: '操作', key: 'action', customRender: (text, record) => {
            return this.$createElement('span', [
              this.$createElement('a', { directives: [{ name: 'perm', value: 'base:holiday:edit' }], on: { click: () => this.openEdit(record) } }, '编辑'),
              this.$createElement('span', { style: { margin: '0 8px' } }, '|'),
              this.$createElement('a', { directives: [{ name: 'perm', value: 'base:holiday:delete' }], on: { click: () => this.remove(record) } }, '删除')
            ])
          } }
      ]
    }
  },
  created() {
    this.loadList()
  },
  methods: {
    noop() {},
    onRangeChange(dates, dateStrings) {
      this.query.range = (Array.isArray(dateStrings) && dateStrings[0] && dateStrings[1]) ? dateStrings : null
    },
    formatDate(d) {
      try {
        const dt = new Date(d)
        return dt.toISOString().slice(0,10)
      } catch (e) { return d }
    },
    ymd(d) {
      if (!d) return ''
      if (typeof d === 'string') return d
      if (d && d.format) return d.format('YYYY-MM-DD')
      try { return d.toISOString().slice(0,10) } catch(e) { return '' }
    },
    async loadList() {
      this.loading = true
      try {
        let url = '/api/base/holiday/list'
        const params = []
        if (this.query.year) params.push('year=' + this.query.year)
        if (this.query.range && this.query.range.length === 2) {
          params.push('start=' + this.ymd(this.query.range[0]))
          params.push('end=' + this.ymd(this.query.range[1]))
        }
        if (params.length) url += '?' + params.join('&')
        const res = await fetch(url, { headers: { 'Authorization': 'Bearer ' + (localStorage.getItem('AUTH_TOKEN') || '') } })
        const json = await res.json()
        if (json && json.code === 200) {
          this.list = json.data || []
        } else {
          this.list = []
          this.$message.error(json.message || '加载失败')
        }
      } catch (e) {
        this.$message.error('加载失败')
      } finally {
        this.loading = false
      }
    },
    resetQuery() { this.query = { year: new Date().getFullYear(), range: null }; this.loadList() },
    openCreate() { this.createVisible = true; this.createForm = { date: null, name: '' } },
    openEdit(rec) { this.editVisible = true; this.editForm = { id: rec.id, date: new Date(rec.dateOnly), name: rec.name } },
    async submitCreate() {
      this.submitting = true
      try {
        const body = { date: this.ymd(this.createForm.date), name: this.createForm.name }
        const res = await fetch('/api/base/holiday/create', { method:'POST', headers:{ 'Content-Type':'application/json', 'Authorization': 'Bearer ' + (localStorage.getItem('AUTH_TOKEN') || '') }, body: JSON.stringify(body) })
        const json = await res.json()
        if (!(json && json.code === 200)) throw new Error(json.message || '新增失败')
        this.createVisible = false
        this.$message.success('新增成功')
        this.loadList()
      } catch (e) {
        this.$message.error(e.message || '新增失败')
      } finally { this.submitting = false }
    },
    async submitEdit() {
      this.submitting = true
      try {
        const body = { date: this.ymd(this.editForm.date), name: this.editForm.name }
        const res = await fetch(`/api/base/holiday/update/${this.editForm.id}`, { method:'PUT', headers:{ 'Content-Type':'application/json', 'Authorization': 'Bearer ' + (localStorage.getItem('AUTH_TOKEN') || '') }, body: JSON.stringify(body) })
        const json = await res.json()
        if (!(json && json.code === 200)) throw new Error(json.message || '更新失败')
        this.editVisible = false
        this.$message.success('更新成功')
        this.loadList()
      } catch (e) {
        this.$message.error(e.message || '更新失败')
      } finally { this.submitting = false }
    },
    async remove(rec) {
      try {
        const res = await fetch(`/api/base/holiday/delete/${rec.id}`, { method:'DELETE', headers:{ 'Authorization': 'Bearer ' + (localStorage.getItem('AUTH_TOKEN') || '') } })
        const json = await res.json()
        if (json && json.code === 200) {
          this.$message.success('删除成功')
          this.loadList()
        } else {
          this.$message.error(json.message || '删除失败')
        }
      } catch (e) { this.$message.error('删除失败') }
    }
  }
}
</script>

<style scoped>
</style>