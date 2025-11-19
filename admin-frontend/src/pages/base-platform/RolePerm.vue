<template>
  <div class="roleperm-page">
    <a-page-header title="角色与权限管理" sub-title="维护角色信息与权限分配" />
    <a-space direction="vertical" style="width:100%">
      <a-space>
        <a-button type="primary" v-perm="'base:role:add'" @click="openRoleModal">新增角色</a-button>
        <a-button @click="loadRoles" :loading="loadingRoles">刷新</a-button>
      </a-space>

      <a-table :data-source="roleList" :columns="columns" rowKey="id" :pagination="false">
        <template slot="status" slot-scope="text, record">
          <a-tag color="green" v-if="record.status === 'enabled'">启用</a-tag>
          <a-tag color="red" v-else>停用</a-tag>
        </template>
        <template slot="action" slot-scope="text, record">
          <a-space>
            <a-button size="small" v-perm="'base:role:edit'" @click="editRole(record)">编辑</a-button>
            <a-button size="small" v-perm="'base:role:perm'" @click="openPermModal(record)">权限</a-button>
            <a-button size="small" type="danger" v-perm="'base:role:toggle'" @click="toggleRole(record)">{{ record.status === 'enabled' ? '禁用' : '启用' }}</a-button>
          </a-space>
        </template>
      </a-table>
    </a-space>

    <!-- 角色信息弹窗 -->
    <a-modal :visible="roleModal.visible" title="角色信息" @ok="saveRole" @cancel="closeRoleModal">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="角色名称">
          <a-input v-model="roleModal.form.name" placeholder="例如：教务老师" />
        </a-form-item>
        <a-form-item label="角色编码">
          <a-input v-model="roleModal.form.code" placeholder="例如：edu_teacher" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 权限分配弹窗 -->
    <a-modal :visible="permModal.visible" title="权限分配" @ok="savePerms" @cancel="closePermModal" :width="720">
      <a-alert type="info" show-icon message="为当前角色勾选可用的功能权限。勾选'全部权限'将赋予'*'。" style="margin-bottom: 12px" />
      <a-checkbox v-model="permModal.allPerms">全部权限</a-checkbox>
      <a-row :gutter="16" style="margin-top: 12px">
        <a-col :span="12">
          <a-card title="菜单权限">
            <a-checkbox-group v-model="permModal.menuPerms" :options="permCatalog.menus" />
          </a-card>
        </a-col>
        <a-col :span="12">
          <a-card title="按钮权限">
            <a-checkbox-group v-model="permModal.buttonPerms" :options="permCatalog.buttons" />
          </a-card>
        </a-col>
      </a-row>
      <a-row :gutter="16" style="margin-top: 12px">
        <a-col :span="24">
          <a-card title="数据范围">
            <a-checkbox-group v-model="permModal.dataPerms" :options="permCatalog.dataScopes" />
          </a-card>
        </a-col>
      </a-row>
    </a-modal>
  </div>
</template>

<script>
export default {
  name: 'RolePermPage',
  data() {
    return {
      loadingRoles: false,
      roleList: [],
      columns: [
        { title: '角色名称', dataIndex: 'name' },
        { title: '角色编码', dataIndex: 'code' },
        { title: '状态', dataIndex: 'status', scopedSlots: { customRender: 'status' } },
        { title: '操作', scopedSlots: { customRender: 'action' } }
      ],
      roleModal: {
        visible: false,
        form: { id: '', name: '', code: '' }
      },
      permModal: {
        visible: false,
        roleId: '',
        allPerms: false,
        menuPerms: [],
        buttonPerms: [],
        dataPerms: []
      },
      permCatalog: { menus: [], buttons: [], dataScopes: [] }
    }
  },
  created() {
    this.loadRoles()
    this.loadPermCatalog()
  },
  methods: {
    async loadPermCatalog() {
      try {
        const res = await fetch('/api/base/perm/catalog')
        const json = await res.json()
        if (json && json.code === 200) {
          const data = json.data || {}
          this.permCatalog = {
            menus: data.menus || [],
            buttons: data.buttons || [],
            dataScopes: data.dataScopes || []
          }
          if (this.$message) this.$message.success('权限目录已加载')
        } else {
          this.usePermCatalogFallback()
          if (this.$message) this.$message.warning('权限目录接口不可用，使用本地数据')
        }
      } catch (e) {
        this.usePermCatalogFallback()
        if (this.$message) this.$message.error('权限目录加载失败，使用本地数据')
      }
    },
    usePermCatalogFallback() {
      this.permCatalog = {
        menus: [
          { label: 'attendance', value: 'menu:attendance' },
          { label: 'base', value: 'menu:base' },
          { label: 'scheduling', value: 'menu:scheduling' },
          { label: 'student', value: 'menu:student' }
        ],
        buttons: [
          { label: '排课编辑', value: 'btn:scheduling:edit' },
          { label: '退款审批', value: 'btn:refund:approve' }
        ],
        dataScopes: [
          { label: '本校区数据', value: 'data:campus:own' },
          { label: '本班级数据', value: 'data:class:own' }
        ]
      }
    },
    async loadRoles() {
      this.loadingRoles = true
      try {
        const res = await fetch('/api/base/role/list')
        const json = await res.json()
        if (json && json.code === 200) {
          this.roleList = (json.data || [])
          if (this.$message) this.$message.success('角色列表已加载')
        } else {
          this.useRoleFallback()
          if (this.$message) this.$message.warning('角色接口不可用，使用本地数据')
        }
      } catch (e) {
        this.useRoleFallback()
        if (this.$message) this.$message.error('角色列表加载失败，使用本地数据')
      } finally {
        this.loadingRoles = false
      }
    },
    useRoleFallback() {
      this.roleList = [
        { id: 'r1', name: '超级管理员', code: 'super_admin', status: 'enabled' },
        { id: 'r2', name: '校区管理员', code: 'campus_admin', status: 'enabled' },
        { id: 'r3', name: '教务老师', code: 'edu_teacher', status: 'enabled' }
      ]
    },
    openRoleModal() {
      this.roleModal.visible = true
      this.roleModal.form = { id: '', name: '', code: '' }
    },
    closeRoleModal() { this.roleModal.visible = false },
    editRole(record) {
      this.roleModal.visible = true
      this.roleModal.form = { id: record.id, name: record.name, code: record.code }
    },
    async saveRole() {
      const payload = { ...this.roleModal.form }
      try {
        const url = payload.id ? `/api/base/role/update/${payload.id}` : '/api/base/role/save'
        const method = payload.id ? 'PUT' : 'POST'
        const res = await fetch(url, { method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
        const json = await res.json()
        if (json && json.code === 200) {
          this.closeRoleModal()
          this.loadRoles()
          if (this.$message) this.$message.success('角色信息已保存')
        } else {
          throw new Error('保存失败')
        }
      } catch (e) {
        this.closeRoleModal()
        this.loadRoles()
        if (this.$message) this.$message.error('保存角色失败，已回滚至列表')
      }
    },
    async toggleRole(record) {
      const next = record.status === 'enabled' ? 'disabled' : 'enabled'
      try {
        const res = await fetch(`/api/base/role/disable/${record.id}`, { method: 'POST' })
        const json = await res.json()
        if (json && json.code === 200) {
          record.status = next
          if (this.$message) this.$message.success('角色状态已更新')
        } else {
          record.status = next
          if (this.$message) this.$message.warning('状态更新接口不可用，已在前端切换')
        }
      } catch (e) {
        record.status = next
        if (this.$message) this.$message.error('状态更新失败，已在前端切换')
      }
    },
    async openPermModal(record) {
      this.permModal.visible = true
      this.permModal.roleId = record.id
      this.permModal.allPerms = false
      this.permModal.menuPerms = []
      this.permModal.buttonPerms = []
      this.permModal.dataPerms = []
      try {
        const res = await fetch(`/api/base/role/perms/${record.id}`)
        const json = await res.json()
        if (json && json.code === 200) {
          const perms = new Set(json.data || [])
          if (perms.has('*')) {
            this.permModal.allPerms = true
          }
          const menus = (this.permCatalog.menus || []).map(m => m.value)
          const buttons = (this.permCatalog.buttons || []).map(b => b.value)
          const datas = (this.permCatalog.dataScopes || []).map(d => d.value)
          this.permModal.menuPerms = [...perms].filter(p => menus.includes(p))
          this.permModal.buttonPerms = [...perms].filter(p => buttons.includes(p))
          this.permModal.dataPerms = [...perms].filter(p => datas.includes(p))
        } else {
          // 保持空，允许用户重新勾选
        }
      } catch (e) {
        // 保持空，允许用户重新勾选
      }
    },
    closePermModal() { this.permModal.visible = false },
    async savePerms() {
      const all = this.permModal.allPerms
      const set = new Set()
      if (all) { set.add('*') }
      this.permModal.menuPerms.forEach(p => set.add(p))
      this.permModal.buttonPerms.forEach(p => set.add(p))
      this.permModal.dataPerms.forEach(p => set.add(p))
      try {
        const res = await fetch(`/api/base/role/perms/${this.permModal.roleId}`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(Array.from(set))
        })
        const json = await res.json()
        if (json && json.code === 200) {
          this.closePermModal()
          if (this.$message) this.$message.success('权限已保存')
        } else {
          this.closePermModal()
          if (this.$message) this.$message.warning('保存失败，请稍后重试')
        }
      } catch (e) {
        this.closePermModal()
        if (this.$message) this.$message.error('保存权限时发生错误')
      }
    }
  }
}
</script>

<style>
.roleperm-page { background: #fff; padding: 16px; }
</style>