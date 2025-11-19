<template>
  <div class="perm-catalog">
    <a-page-header title="权限目录管理" sub-title="维护菜单/按钮/数据权限，支持初始化与角色关联查看" />
    <a-card bordered style="margin-bottom:12px">
      <a-space>
        <a-select v-model="query.type" placeholder="选择类型" style="width:160px" allowClear @change="loadList">
          <a-select-option value="menu">menu</a-select-option>
          <a-select-option value="button">button</a-select-option>
          <a-select-option value="data">data</a-select-option>
        </a-select>
        <a-input v-model="query.keyword" placeholder="按标签或值搜索" style="width:240px" @pressEnter="loadList" />
        <a-button type="primary" @click="openAdd">新增</a-button>
        <a-button @click="loadList">刷新</a-button>
        <a-divider type="vertical" />
        <a-button @click="initPermissions" :loading="initLoading">初始化权限目录</a-button>
      </a-space>
    </a-card>

    <a-card bordered>
      <a-table :data-source="list" :columns="columns" rowKey="id" :loading="loading">
        <template slot="actions" slot-scope="text, record">
          <a-space>
            <a-button size="small" @click="openEdit(record)">编辑</a-button>
            <a-button size="small" @click="viewRoles(record)">关联角色</a-button>
            <a-popconfirm title="确认删除？删除前需确保未关联任何角色" @confirm="doDelete(record)">
              <a-button size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model="editVisible" :title="editMode==='add'?'新增权限项':'编辑权限项'" :confirm-loading="saving" @ok="doSave" @cancel="()=>{editVisible=false}" :width="520">
      <a-form layout="horizontal" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="类型"><a-select v-model="form.type"><a-select-option value="menu">menu</a-select-option><a-select-option value="button">button</a-select-option><a-select-option value="data">data</a-select-option></a-select></a-form-item>
        <a-form-item label="标签"><a-input v-model="form.label" /></a-form-item>
        <a-form-item label="值"><a-input v-model="form.value" placeholder="如 menu:finance:settlement:fee 或 base:campus:add" /></a-form-item>
        <a-form-item label="排序"><a-input-number v-model="form.sortOrder" :min="0" /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model="rolesVisible" title="关联角色" :footer="null" :width="520">
      <a-list :data-source="roles" bordered>
        <a-list-item v-for="r in roles" :key="r.id">{{ r.name }}（{{ r.code }}）</a-list-item>
      </a-list>
      <div v-if="!roles || roles.length===0" style="color:#999; padding:8px 0">暂无关联角色</div>
    </a-modal>
  </div>
</template>

<script>
export default {
  name: 'PermCatalog',
  data() {
    return {
      query: { type: 'menu', keyword: '' },
      loading: false,
      list: [],
      columns: [
        { title: '类型', dataIndex: 'type' },
        { title: '标签', dataIndex: 'label' },
        { title: '值', dataIndex: 'value' },
        { title: '排序', dataIndex: 'sortOrder' },
        { title: '操作', key: 'actions', scopedSlots: { customRender: 'actions' } }
      ],
      editVisible: false,
      editMode: 'add',
      saving: false,
      form: { id: null, type: 'menu', label: '', value: '', sortOrder: 0 },
      rolesVisible: false,
      roles: [],
      initLoading: false
    }
  },
  created() { this.loadList() },
  methods: {
    async loadList() {
      this.loading = true
      const qs = this.query.type ? ('?type=' + encodeURIComponent(this.query.type)) : ''
      try {
        const res = await fetch('/api/base/perm/catalog/items' + qs)
        const json = await res.json()
        let arr = Array.isArray(json.data) ? json.data : []
        if (this.query.keyword) {
          const kw = String(this.query.keyword).toLowerCase()
          arr = arr.filter(it => String(it.label||'').toLowerCase().includes(kw) || String(it.value||'').toLowerCase().includes(kw))
        }
        this.list = arr
      } catch(e) { this.list = [] }
      this.loading = false
    },
    openAdd() { this.editMode='add'; this.form = { id:null, type:'menu', label:'', value:'', sortOrder:0 }; this.editVisible=true },
    openEdit(rec) { if (!rec) return; this.editMode='edit'; this.form = { id: rec.id, type: rec.type, label: rec.label, value: rec.value, sortOrder: rec.sortOrder }; this.editVisible=true },
    async doSave() {
      if (!this.form.type || !this.form.label || !this.form.value) { this.$message && this.$message.warning('请填写完整信息'); return }
      this.saving = true
      try {
        if (this.editMode === 'add') {
          const res = await fetch('/api/base/perm/catalog/save', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(this.form) })
          const json = await res.json(); if (json && json.code === 200) { this.$message && this.$message.success('已新增'); this.editVisible=false; this.loadList() } else { throw new Error(json.message || '新增失败') }
        } else {
          const res = await fetch('/api/base/perm/catalog/update/' + this.form.id, { method:'PUT', headers:{'Content-Type':'application/json'}, body: JSON.stringify(this.form) })
          const json = await res.json(); if (json && json.code === 200) { this.$message && this.$message.success('已更新'); this.editVisible=false; this.loadList() } else { throw new Error(json.message || '更新失败') }
        }
      } catch(e) { this.$message && this.$message.error(e.message || '保存失败') }
      this.saving = false
    },
    async doDelete(rec) {
      if (!rec || !rec.id) return
      try {
        const res = await fetch('/api/base/perm/catalog/delete/' + rec.id, { method:'DELETE' })
        const json = await res.json()
        if (json && json.code === 200) { this.$message && this.$message.success('已删除'); this.loadList() }
        else { this.$message && this.$message.error(json.message || '删除失败'); }
      } catch(e) { this.$message && this.$message.error('删除失败') }
    },
    async viewRoles(rec) {
      if (!rec || !rec.value) { this.rolesVisible = true; this.roles = []; return }
      try {
        const res = await fetch('/api/base/perm/catalog/roles?value=' + encodeURIComponent(rec.value))
        const json = await res.json(); this.roles = Array.isArray(json.data) ? json.data : []
      } catch(e) { this.roles = [] }
      this.rolesVisible = true
    },
    async initPermissions() {
      this.initLoading = true
      try {
        const res = await fetch('/api/system/init/permissions', { method:'POST' })
        const json = await res.json(); if (json && json.code === 200) { this.$message && this.$message.success('权限目录已初始化'); this.loadList() } else { throw new Error(json.message || '初始化失败') }
      } catch(e) { this.$message && this.$message.error(e.message || '初始化失败') }
      this.initLoading = false
    }
  }
}
</script>

<style scoped>
.perm-catalog { background:#fff; padding: 16px; }
</style>