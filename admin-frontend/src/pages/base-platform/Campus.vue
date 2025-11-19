<template>
  <div>
    <!-- 操作区域 -->
    <a-card bordered style="margin-bottom: 12px">
      <a-space>
        <a-button type="primary" @click="openForm()">新增校区</a-button>
      </a-space>
    </a-card>

    <!-- 查询条件 -->
    <a-card bordered style="margin-bottom: 12px">
      <a-form :layout="'inline'">
        <a-form-item label="学校/校区">
          <a-input v-model="filters.name" placeholder="按名称搜索" allowClear />
        </a-form-item>
        <a-form-item label="电话">
          <a-input v-model="filters.phone" placeholder="联系电话" allowClear />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="searchCampus">查询</a-button>
            <a-button @click="resetFilters">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-table :data-source="list" :columns="columns" rowKey="id" />

    <a-modal :visible="visible" title="校区信息" @ok="submit" @cancel="visible=false" :confirmLoading="submitting">
      <a-form layout="vertical">
        <a-form-item label="名称"><a-input v-model="form.name" /></a-form-item>
        <a-form-item label="地址"><a-input v-model="form.address" /></a-form-item>
        <a-form-item label="联系电话"><a-input v-model="form.phone" /></a-form-item>
        <a-form-item label="负责人"><a-input v-model="form.manager" /></a-form-item>
        <a-form-item label="所属区域"><a-input v-model="form.region" /></a-form-item>
        <a-form-item label="默认签到规则"><a-select v-model="form.config.signRule">
          <a-select-option value="qrcode">二维码</a-select-option>
          <a-select-option value="manual">人工</a-select-option>
        </a-select>
        </a-form-item>
        <a-form-item label="冲突校验级别"><a-select v-model="form.config.conflictCheckLevel">
          <a-select-option value="strict">严格</a-select-option>
          <a-select-option value="normal">一般</a-select-option>
          <a-select-option value="loose">宽松</a-select-option>
        </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
export default {
  name: 'Campus',
  data() {
    return {
      list: [],
      rawList: [],
      visible: false,
      loading: false,
      submitting: false,
      editingId: null,
      filters: { name: '', phone: '' },
      form: { name:'', address:'', phone:'', manager:'', region:'', config:{ signRule:'qrcode', conflictCheckLevel:'strict' } },
      columns: [
        { title:'名称', dataIndex:'name' },
        { title:'地址', dataIndex:'address' },
        { title:'电话', dataIndex:'phone' },
        { title:'负责人', dataIndex:'manager' },
        { title:'所属区域', dataIndex:'region' },
        { title:'状态', dataIndex:'status' },
        { title:'操作', key:'action', customRender: (text, record) => {
            return this.$createElement('span', [
              this.$createElement('a', { on: { click: () => this.edit(record) } }, '编辑'),
              this.$createElement('span', { style: { margin: '0 8px' } }, '|'),
              this.$createElement('a', { on: { click: () => this.disable(record) } }, '禁用'),
              this.$createElement('span', { style: { margin: '0 8px' } }, '|'),
              this.$createElement('a', { on: { click: () => this.goDept(record.id) } }, '维护部门')
            ])
          } }
      ]
    }
  },
  created() { this.loadData() },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await fetch('/api/base/campus/list')
        const json = await res.json()
        if (json && json.code === 200) {
          this.rawList = json.data || []
          this.list = this.rawList
          if (this.$message) this.$message.success('校区数据已加载')
        } else {
          this.rawList = []
          this.list = []
          if (this.$message) this.$message.error(json.message || '校区数据加载失败')
        }
      } catch (e) {
        if (this.$message) this.$message.error('校区数据加载失败')
      } finally {
        this.loading = false
      }
    },
    searchCampus() {
      const name = (this.filters.name || '').trim()
      const phone = (this.filters.phone || '').trim()
      const hasFilter = !!(name || phone)
      if (!hasFilter) { this.list = this.rawList; return }
      const nameLower = name.toLowerCase()
      this.list = (this.rawList || []).filter(it => {
        const matchName = name ? String(it.name || '').toLowerCase().includes(nameLower) : true
        const matchPhone = phone ? String(it.phone || '').includes(phone) : true
        return matchName && matchPhone
      })
    },
    resetFilters() {
      this.filters = { name: '', phone: '' }
      this.list = this.rawList
    },
    openForm() { this.visible = true; this.editingId = null; this.form = { name:'', address:'', phone:'', manager:'', region:'', config:{ signRule:'qrcode', conflictCheckLevel:'strict' } } },
    edit(rec) { this.visible = true; this.editingId = rec.id; this.form = JSON.parse(JSON.stringify(rec)); },
    async submit() {
      this.submitting = true
      try {
        if (this.editingId) {
          const res = await fetch(`/api/base/campus/update/${this.editingId}`, { method:'PUT', headers:{'Content-Type':'application/json'}, body: JSON.stringify(this.form) })
          const json = await res.json()
          if (!(json && json.code === 200)) throw new Error(json && json.message ? json.message : '更新失败')
        } else {
          const res = await fetch('/api/base/campus/save', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(this.form) })
          const json = await res.json()
          if (!(json && json.code === 200)) throw new Error(json && json.message ? json.message : '保存失败')
        }
        this.visible = false
        if (this.$message) this.$message.success('保存成功')
        this.loadData()
      } catch (e) {
        if (this.$message) this.$message.error(e.message || '保存失败')
      } finally {
        this.submitting = false
      }
    },
    async disable(rec) {
      try {
        const res = await fetch(`/api/base/campus/disable/${rec.id}`, { method:'POST' })
        const json = await res.json()
        if (json && json.code === 200) {
          if (this.$message) this.$message.success('状态已更新')
          this.loadData()
        } else {
          if (this.$message) this.$message.error(json.message || '操作失败')
        }
      } catch (e) {
        if (this.$message) this.$message.error('操作失败')
      }
    },
    goDept(campusId) {
      this.$router.push({ path: '/base/dept', query: { campusId } })
    }
  }
}
</script>

<style scoped>
</style>