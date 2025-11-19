<template>
  <div class="orgstaff-page">
    <a-page-header title="组织与教职工管理" sub-title="部门树与教职工列表" />
    <a-row :gutter="16">
      <a-col :span="6">
        <a-card title="部门结构" bordered>
          <a-tree
            :treeData="deptTree"
            :defaultExpandAll="true"
            @select="onDeptSelect"
          />
        </a-card>
        <a-card title="过滤" style="margin-top: 12px">
          <a-select v-model="filters.campusId" style="width: 100%" placeholder="选择校区" @change="onCampusChange">
            <a-select-option v-for="c in campuses" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
          </a-select>
        </a-card>
      </a-col>

      <a-col :span="18">
        <a-card :title="staffCardTitle" bordered>
          <a-space style="margin-bottom: 12px">
            <a-button type="primary" @click="openStaffModal">新增员工</a-button>
            <a-upload :showUploadList="false" :beforeUpload="beforeUploadUserExcel">
              <a-button>导入用户Excel</a-button>
            </a-upload>
            <a-button @click="genInitialAccounts">生成初始账号</a-button>
            <a-button @click="loadStaff" :loading="loadingStaff">刷新</a-button>
          </a-space>

          <a-table :data-source="staffList" :columns="columns" rowKey="id" :pagination="pagination" @change="onTableChange">
            <template slot="status" slot-scope="text, record, index">
              <a-tag color="green" v-if="text === 'enabled'">启用</a-tag>
              <a-tag color="red" v-else>停用</a-tag>
            </template>

            <template slot="roles" slot-scope="text, record">
              <a-space wrap>
                <a-tag v-for="(nm, i) in (record.roleNames||[])" :key="nm+'_'+i" color="blue">{{ nm }}</a-tag>
              </a-space>
            </template>

            <template slot="action" slot-scope="text, record, index">
              <a-space>
                <a-button size="small" @click="editStaff(record)">编辑</a-button>
                <a-button size="small" @click="resetPassword(record)">重置密码</a-button>
                <a-button size="small" type="danger" @click="toggleStatus(record)">{{ (record && record.status) === 'enabled' ? '停用' : '启用' }}</a-button>
              </a-space>
            </template>
          </a-table>
        </a-card>
      </a-col>
    </a-row>

    <a-modal :visible="staffModal.visible" title="员工信息" @ok="saveStaff" @cancel="closeStaffModal">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="姓名">
          <a-input v-model="staffModal.form.name" placeholder="请输入姓名" />
        </a-form-item>
        <a-form-item label="角色">
          <a-select v-model="staffModal.form.roleIds" mode="multiple" placeholder="选择角色">
            <a-select-option v-for="(r, idx) in roles" :key="'role-'+String(r.id)" :value="String(r.id)">{{ r.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="手机号码">
          <a-input v-model="staffModal.form.mobile" placeholder="用于联系或作默认账号" />
        </a-form-item>
        <a-form-item label="所属部门">
          <a-select v-model="staffModal.form.deptId" placeholder="选择部门">
            <a-select-option v-for="d in flatDepts" :key="d.id" :value="d.id">{{ d.title }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="所属校区">
          <a-select v-model="staffModal.form.campusId" placeholder="选择校区">
            <a-select-option v-for="c in campuses" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="账号">
          <a-input v-model="staffModal.form.username" placeholder="用于登录的账号名" />
        </a-form-item>
        <a-form-item label="初始密码">
          <a-input v-model="staffModal.form.password" placeholder="留空则使用默认密码" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
export default {
  name: 'OrgStaffPage',
  data() {
    return {
      loadingDept: false,
      loadingStaff: false,
      deptTree: [],
      flatDepts: [],
      selectedDeptId: '',
      campuses: [],
      roles: [],
      filters: {
        campusId: ''
      },
      staffList: [],
      pagination: { current: 1, pageSize: 10, total: 0, showSizeChanger: true, pageSizeOptions: ['10','20','50','100'] },
      columns: [
        { title: '姓名', dataIndex: 'name' },
        { title: '账号', dataIndex: 'username' },
        { title: '手机', dataIndex: 'mobile' },
        { title: '角色', dataIndex: 'roleNames', scopedSlots: { customRender: 'roles' } },
        { title: '部门', dataIndex: 'deptName' },
        { title: '校区', dataIndex: 'campusName' },
        { title: '状态', dataIndex: 'status' },
        { title: '操作', scopedSlots: { customRender: 'action' } }
      ],
      staffModal: {
        visible: false,
        form: {
          id: '', name: '', username: '', password: '', mobile: '', roleIds: [], deptId: '', campusId: ''
        }
      }
    }
  },
  computed: {
    staffCardTitle() {
      const dept = this.flatDepts.find(d => d.id === this.selectedDeptId)
      return dept ? `员工列表 - ${dept.title}` : '员工列表'
    }
  },
  created() {
    Promise.all([this.loadCampuses(), this.loadRoles()]).then(() => {
      this.loadDepts()
    })
  },
  methods: {
    async loadDepts() {
      this.loadingDept = true
      try {
        const qs = this.filters.campusId ? `?campusId=${this.filters.campusId}` : ''
        const res = await fetch(`/api/base/dept/tree${qs}`)
        const json = await res.json()
        if (json && json.code === 200) {
          const tree = (json.data || [])
          this.deptTree = tree
          this.flatDepts = this.flattenTree(tree)
          if (!this.selectedDeptId && this.flatDepts.length) this.selectedDeptId = this.flatDepts[0].id
          this.loadStaff()
        } else {
          this.useDeptFallback()
        }
      } catch (e) {
        this.useDeptFallback()
      } finally {
        this.loadingDept = false
      }
    },
    useDeptFallback() {
      const tree = [
        { id: 'd1', title: '总部', children: [ { id: 'd1-1', title: '教务部' }, { id: 'd1-2', title: '招生部' } ] },
        { id: 'd2', title: '南校区', children: [ { id: 'd2-1', title: '教学组' } ] }
      ]
      this.deptTree = tree
      this.flatDepts = this.flattenTree(tree)
      this.selectedDeptId = this.flatDepts[0].id
      this.loadStaff()
    },
    flattenTree(nodes, acc = []) {
      nodes.forEach(n => {
        acc.push({ id: n.id, title: n.title })
        if (n.children && n.children.length) this.flattenTree(n.children, acc)
      })
      return acc
    },
    async loadCampuses() {
      try {
        const res = await fetch('/api/base/campus/list')
        const json = await res.json()
        if (json && json.code === 200) {
          this.campuses = json.data || []
          if (!this.filters.campusId && this.campuses.length) {
            this.filters.campusId = this.campuses[0].id
          }
        } else {
          this.campuses = [ { id: 'c1', name: '总部' }, { id: 'c2', name: '南校区' } ]
        }
      } catch (e) {
        this.campuses = [ { id: 'c1', name: '总部' }, { id: 'c2', name: '南校区' } ]
      }
    },
    onCampusChange() {
      this.loadDepts()
      this.loadStaff()
    },
    async loadRoles() {
      try {
        const res = await fetch('/api/base/role/list')
        const json = await res.json()
        if (json && json.code === 200) {
          const arr = Array.isArray(json.data) ? json.data : []
          const seen = new Set()
          this.roles = arr.filter(r => {
            const id = String(r && r.id)
            if (!id || seen.has(id)) return false
            seen.add(id)
            return true
          })
        } else {
          this.roles = [ { id: 1, name: '教务老师' }, { id: 2, name: '教师' } ]
        }
      } catch (e) {
        this.roles = [ { id: 1, name: '教务老师' }, { id: 2, name: '教师' } ]
      }
    },
    beforeUploadUserExcel(file) {
      const fd = new FormData(); fd.append('file', file)
      fetch('/api/auth/user/import/excel', { method: 'POST', body: fd }).then(r=>r.json()).then(json => {
        if (json && json.code===200) {
          this.$message && this.$message.success(`导入成功 ${json.data.success} 条，失败 ${json.data.failed} 条`)
          this.loadStaff()
        } else { this.$message && this.$message.error('导入失败') }
      }).catch(()=>{ this.$message && this.$message.error('导入请求失败') })
      return false
    },
    genInitialAccounts(){
      fetch('/api/auth/user/init-accounts', { method: 'POST', headers: { 'Content-Type':'application/json' }, body: JSON.stringify({ target: 'staff' }) })
        .then(res => res.json()).then(json => {
          if (json && json.code===200) {
            this.$message && this.$message.success(`已更新 ${json.data.updated} 个账号`)
            this.loadStaff()
          } else { this.$message && this.$message.error('生成失败') }
        }).catch(() => { this.$message && this.$message.error('生成请求失败') })
    },
    loadStaff() {
      this.loadingStaff = true
      const params = new URLSearchParams()
      if (this.selectedDeptId && /^\d+$/.test(String(this.selectedDeptId))) params.append('deptId', this.selectedDeptId)
      if (this.filters.campusId && /^\d+$/.test(String(this.filters.campusId))) params.append('campusId', this.filters.campusId)
      params.append('page', String(this.pagination.current || 1))
      params.append('size', String(this.pagination.pageSize || 10))
      fetch(`/api/base/staff/list?${params.toString()}`).then(res => res.json()).then(json => {
        if (json && json.code === 200) {
          const payload = json.data || {}
          const list = Array.isArray(payload) ? payload : (payload.items || [])
          const roleNameById = (id) => {
            const r = this.roles.find(x => String(x.id) === String(id))
            return r ? r.name : ''
          }
          const campusNameById = (id) => {
            const c = this.campuses.find(x => String(x.id) === String(id))
            return c ? c.name : ''
          }
          const deptNameById = (id) => {
            const d = this.flatDepts.find(x => String(x.id) === String(id))
            return d ? d.title : ''
          }
          this.staffList = list.map(it => {
            const idsStr = String(it.roleIds || it.roleId || '').trim()
            const idsArr = idsStr ? idsStr.split(',').filter(Boolean) : []
            const namesArr = idsArr.map(id => roleNameById(id)).filter(Boolean)
            return {
              id: it.id || it.staffId || Math.random().toString(36).slice(2),
              name: it.name,
              username: it.username || '',
              mobile: it.mobile || '',
              roleIds: idsArr,
              roleNames: namesArr,
              roleId: it.roleId || '',
              deptId: it.deptId || '',
              deptName: deptNameById(it.deptId),
              campusId: it.campusId || '',
              campusName: campusNameById(it.campusId),
              status: it.status || 'enabled'
            }
          })
          this.pagination.total = (payload.total || list.length)
        } else {
          this.useStaffFallback()
        }
        this.loadingStaff = false
      }).catch(() => { this.useStaffFallback(); this.loadingStaff = false })
    },
    onTableChange(pager) {
      this.pagination.current = pager.current
      this.pagination.pageSize = pager.pageSize
      this.loadStaff()
    },
    useStaffFallback() {
      this.staffList = [
        { id: 's1', name: '张三', roleId: 1, roleName: '教务老师', deptName: '教务部', campusName: '总部', status: 'enabled' },
        { id: 's2', name: '李四', roleId: 2, roleName: '教师', deptName: '教学组', campusName: '南校区', status: 'disabled' }
      ]
    },
    onDeptSelect(keys) {
      if (keys && keys.length) {
        this.selectedDeptId = keys[0]
        this.loadStaff()
      }
    },
    openStaffModal() {
      this.staffModal.visible = true
      this.staffModal.form = { id: '', name: '', username: '', password: '', mobile: '', roleIds: [], deptId: this.selectedDeptId, campusId: this.filters.campusId }
    },
    closeStaffModal() {
      this.staffModal.visible = false
    },
    editStaff(record) {
      this.staffModal.visible = true
      const ids = Array.isArray(record.roleIds) ? record.roleIds.map(x=>String(x)) : []
      const uniq = Array.from(new Set(ids))
      this.staffModal.form = { id: record.id, name: record.name, username: record.username || '', password: '', mobile: record.mobile || '', roleIds: uniq, deptId: record.deptId || this.selectedDeptId, campusId: record.campusId || this.filters.campusId }
    },
    saveStaff() {
      const payload = { ...this.staffModal.form }
      if (Array.isArray(payload.roleIds)) payload.roleIds = Array.from(new Set(payload.roleIds.map(x=>String(x))))
      const url = payload.id ? `/api/base/staff/update/${payload.id}` : '/api/base/staff/save'
      const method = payload.id ? 'PUT' : 'POST'
      fetch(url, { method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
        .then(res => res.json()).then(json => {
          if (json && json.code === 200) {
            this.closeStaffModal()
            this.loadStaff()
            if (this.$message) this.$message.success('员工信息已保存')
          } else {
            this.closeStaffModal()
            this.loadStaff()
            if (this.$message) this.$message.error('保存失败，已回滚至列表')
          }
        }).catch(() => { this.closeStaffModal(); this.loadStaff(); this.$message && this.$message.error('保存失败，已回滚至列表') })
    },
    toggleStatus(record) {
      const next = record.status === 'enabled' ? 'disabled' : 'enabled'
      fetch(`/api/base/staff/status/${record.id}?status=${next}`, { method: 'POST' }).then(res => res.json()).then(json => {
        if (json && json.code === 200) {
          record.status = next
          if (this.$message) this.$message.success('状态已更新')
        } else {
          record.status = next
          if (this.$message) this.$message.warning('状态更新接口不可用，已在前端切换')
        }
      }).catch(() => { record.status = next; this.$message && this.$message.error('状态更新失败，已在前端切换') })
    }
    , resetPassword(record) {
      if (!record || !record.id) return
      fetch(`/api/base/staff/reset-password/${record.id}`, { method: 'POST' }).then(res=>res.json()).then(json => {
        if (json && json.code===200) {
          this.$message && this.$message.success('已初始化密码为 123456')
        } else {
          this.$message && this.$message.error('初始化失败')
        }
      }).catch(()=>{ this.$message && this.$message.error('初始化请求失败') })
    }
  }
}
</script>

<style>
.orgstaff-page { background: #fff; padding: 16px; }
</style>