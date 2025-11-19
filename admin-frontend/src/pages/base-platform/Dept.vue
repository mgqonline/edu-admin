<template>
  <div class="dept-page">
    <a-page-header title="组织信息维护" sub-title="按校区维护部门结构" />
    <a-row :gutter="16">
      <a-col :span="6">
        <a-card title="校区选择" bordered>
          <a-select v-model="filters.campusId" style="width: 100%" placeholder="选择校区" @change="onCampusChange">
            <a-select-option v-for="c in campuses" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
          </a-select>
        </a-card>
        <a-card title="部门结构" style="margin-top: 12px" bordered>
          <a-tree :treeData="deptTree" :defaultExpandAll="true" @select="onDeptSelect" />
        </a-card>
      </a-col>

      <a-col :span="18">
        <a-card :title="cardTitle" bordered>
          <a-space style="margin-bottom: 12px">
            <a-button type="primary" @click="openAddRoot">新增根部门</a-button>
            <a-button @click="openAddChild" :disabled="!selectedDept">在选中部门下新增</a-button>
            <a-button @click="openEdit" :disabled="!selectedDept">编辑选中部门</a-button>
            <a-button type="danger" @click="disableSelected" :disabled="!selectedDept">禁用选中部门</a-button>
            <a-button @click="loadDepts" :loading="loadingDept">刷新</a-button>
          </a-space>

          <div v-if="selectedDept">
            <p><b>当前选中：</b>{{ selectedDept.title }}（ID: {{ selectedDept.id }}）</p>
          </div>
          <div v-else>
            <p>请选择左侧树中的部门进行维护。</p>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-modal :visible="modal.visible" :title="modalTitle" @ok="saveDept" @cancel="closeModal">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="部门名称">
          <a-input v-model="modal.form.title" placeholder="请输入部门名称" />
        </a-form-item>
        <a-form-item label="父部门">
          <a-select v-model="modal.form.parentId" placeholder="选择父部门，为空则为根">
            <a-select-option :value="0">(无)</a-select-option>
            <a-select-option v-for="d in flatDepts" :key="d.id" :value="d.id">{{ d.title }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model="modal.form.sortOrder" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model="modal.form.status">
            <a-select-option value="enabled">启用</a-select-option>
            <a-select-option value="disabled">禁用</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
export default {
  name: 'DeptPage',
  data() {
    return {
      campuses: [],
      deptTree: [],
      flatDepts: [],
      selectedDept: null,
      loadingDept: false,
      filters: { campusId: '' },
      modal: {
        visible: false,
        mode: 'add',
        form: { id: '', title: '', parentId: 0, campusId: '', sortOrder: 0, status: 'enabled' }
      }
    }
  },
  computed: {
    cardTitle() {
      const campus = this.campuses.find(c => String(c.id) === String(this.filters.campusId))
      return campus ? `部门结构 - ${campus.name}` : '部门结构'
    },
    modalTitle() {
      return this.modal.mode === 'edit' ? '编辑部门' : '新增部门'
    }
  },
  created() {
    // 如果路由带有 campusId，则优先使用它
    if (this.$route && this.$route.query && this.$route.query.campusId) {
      const qid = this.$route.query.campusId
      this.filters.campusId = (!isNaN(Number(qid)) ? Number(qid) : qid)
      // 直接加载部门树（校园列表用于展示，不阻塞业务）
      this.loadDepts()
      this.loadCampuses()
    } else {
      this.loadCampuses()
    }
  },
  methods: {
    async loadCampuses() {
      try {
        const res = await fetch('/api/base/campus/list')
        const json = await res.json()
        if (json && json.code === 200) {
          this.campuses = json.data || []
          if (!this.filters.campusId && this.campuses.length) {
            this.filters.campusId = this.campuses[0].id
            this.loadDepts()
          }
        }
      } catch (e) {
        this.campuses = []
      }
    },
    async loadDepts() {
      this.loadingDept = true
      try {
        const res = await fetch(`/api/base/dept/tree?campusId=${this.filters.campusId || ''}`)
        const json = await res.json()
        if (json && json.code === 200) {
          const tree = json.data || []
          this.deptTree = tree.map(this.mapToTreeNode)
          this.flatDepts = this.flattenTree(tree)
          this.selectedDept = null
        } else {
          this.deptTree = []
          this.flatDepts = []
        }
      } catch (e) {
        this.deptTree = []
        this.flatDepts = []
      } finally {
        this.loadingDept = false
      }
    },
    mapToTreeNode(n) {
      const node = { key: n.id, title: n.title, id: n.id }
      if (n.children && n.children.length) node.children = n.children.map(this.mapToTreeNode)
      return node
    },
    flattenTree(nodes, acc = []) {
      nodes.forEach(n => {
        acc.push({ id: n.id, title: n.title })
        if (n.children && n.children.length) this.flattenTree(n.children, acc)
      })
      return acc
    },
    onCampusChange() {
      this.loadDepts()
    },
    onDeptSelect(keys) {
      if (keys && keys.length) {
        const id = keys[0]
        const flat = this.flatDepts.find(d => String(d.id) === String(id))
        this.selectedDept = flat || null
      } else {
        this.selectedDept = null
      }
    },
    openAddRoot() {
      this.modal.visible = true
      this.modal.mode = 'add'
      this.modal.form = { id: '', title: '', parentId: 0, campusId: this.filters.campusId, sortOrder: 0, status: 'enabled' }
    },
    openAddChild() {
      if (!this.selectedDept) return
      this.modal.visible = true
      this.modal.mode = 'add'
      this.modal.form = { id: '', title: '', parentId: this.selectedDept.id, campusId: this.filters.campusId, sortOrder: 0, status: 'enabled' }
    },
    openEdit() {
      if (!this.selectedDept) return
      this.modal.visible = true
      this.modal.mode = 'edit'
      this.modal.form = { id: this.selectedDept.id, title: this.selectedDept.title, parentId: 0, campusId: this.filters.campusId, sortOrder: 0, status: 'enabled' }
    },
    closeModal() {
      this.modal.visible = false
    },
    async saveDept() {
      const payload = { ...this.modal.form }
      payload.parentId = payload.parentId === 0 ? null : payload.parentId
      try {
        const url = payload.id ? `/api/base/dept/update/${payload.id}` : '/api/base/dept/save'
        const method = payload.id ? 'PUT' : 'POST'
        const res = await fetch(url, { method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
        const json = await res.json()
        if (json && json.code === 200) {
          this.closeModal()
          this.loadDepts()
          if (this.$message) this.$message.success('部门信息已保存')
        } else {
          throw new Error('保存失败')
        }
      } catch (e) {
        this.closeModal()
        this.loadDepts()
        if (this.$message) this.$message.error('保存失败，已尝试刷新数据')
      }
    },
    async disableSelected() {
      if (!this.selectedDept) return
      try {
        const res = await fetch(`/api/base/dept/disable/${this.selectedDept.id}`, { method: 'POST' })
        const json = await res.json()
        if (json && json.code === 200) {
          if (this.$message) this.$message.success('已禁用')
          this.loadDepts()
        } else {
          throw new Error('禁用失败')
        }
      } catch (e) {
        if (this.$message) this.$message.error('禁用失败')
      }
    }
  }
}
</script>

<style>
.dept-page { background: #fff; padding: 16px; }
</style>