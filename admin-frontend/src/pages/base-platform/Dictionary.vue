<template>
  <div class="dict-page">
    <a-page-header title="字典配置" sub-title="课程分类、班级类型、学员状态、通用状态" />
    <a-space direction="vertical" style="width: 100%">
      <a-space>
        <a-button type="primary" @click="loadDicts" :loading="loading">刷新</a-button>
      </a-space>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-card title="课程分类" bordered>
            <a-space :size="8" wrap>
              <a-tag v-for="item in dicts.courseCategory" :key="item.id" :closable="true" @close="onDelete(item)" @click="openEdit('courseCategory', item)">{{ item.name }}</a-tag>
              <a-button size="small" type="dashed" @click="openAdd('courseCategory')">新增</a-button>
            </a-space>
          </a-card>
        </a-col>
        <a-col :span="12">
          <a-card title="班级类型" bordered>
            <a-space :size="8" wrap>
              <a-tag v-for="item in dicts.classType" :key="item.id" :closable="true" @close="onDelete(item)" @click="openEdit('classType', item)">{{ item.name }}</a-tag>
              <a-button size="small" type="dashed" @click="openAdd('classType')">新增</a-button>
            </a-space>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-card title="学员状态" bordered>
            <a-space :size="8" wrap>
              <a-tag v-for="item in dicts.studentStatus" :key="item.id" :closable="true" @close="onDelete(item)" @click="openEdit('studentStatus', item)">{{ item.name }}</a-tag>
              <a-button size="small" type="dashed" @click="openAdd('studentStatus')">新增</a-button>
            </a-space>
          </a-card>
        </a-col>
        <a-col :span="12">
          <a-card title="通用状态" bordered>
            <a-space :size="8" wrap>
              <a-tag v-for="item in dicts.commonStatus" :key="item.id" :closable="true" @close="onDelete(item)" @click="openEdit('commonStatus', item)">{{ item.name }}</a-tag>
              <a-button size="small" type="dashed" @click="openAdd('commonStatus')">新增</a-button>
            </a-space>
          </a-card>
        </a-col>
      </a-row>

      <a-modal :visible="itemModal.visible" title="字典项" @ok="saveItem" @cancel="itemModal.visible=false" :confirmLoading="itemModal.submitting">
        <a-form layout="vertical">
          <a-form-item label="编码"><a-input v-model="itemModal.form.code" /></a-form-item>
          <a-form-item label="名称"><a-input v-model="itemModal.form.name" /></a-form-item>
          <a-form-item label="排序"><a-input type="number" v-model="itemModal.form.sortOrder" /></a-form-item>
          <a-form-item label="状态">
            <a-select v-model="itemModal.form.status">
              <a-select-option value="enabled">启用</a-select-option>
              <a-select-option value="disabled">禁用</a-select-option>
            </a-select>
          </a-form-item>
        </a-form>
      </a-modal>
    </a-space>
  </div>
</template>

<script>
export default {
  name: 'DictionaryPage',
  data() {
    return {
      loading: false,
      dicts: {
        courseCategory: [],
        classType: [],
        studentStatus: [],
        commonStatus: []
      },
      itemModal: {
        visible: false,
        type: '',
        form: { id: '', code: '', name: '', sortOrder: 0, status: 'enabled' },
        submitting: false
      }
    }
  },
  created() {
    this.loadDicts()
  },
  methods: {
    async loadDicts() {
      this.loading = true
      try {
        const res = await fetch('/api/base/dict/all')
        const json = await res.json()
        if (json && json.code === 200) {
          const data = json.data || {}
          // 兼容旧字段名与新结构
          this.dicts.courseCategory = data.courseCategory || data.courseCategories || []
          this.dicts.classType = data.classType || data.classTypes || []
          this.dicts.studentStatus = data.studentStatus || []
          this.dicts.commonStatus = data.commonStatus || []
          if (this.$message) this.$message.success('字典数据已加载')
        } else {
          this.useFallback()
          if (this.$message) this.$message.warning('字典接口不可用，使用本地数据')
        }
      } catch(e) {
        this.useFallback()
        if (this.$message) this.$message.error('字典数据加载失败，使用本地数据')
      } finally {
        this.loading = false
      }
    },
    useFallback() {
      this.dicts = {
        courseCategory: [ { id:'c1', code:'math', name:'数学' }, { id:'c2', code:'eng', name:'英语' } ],
        classType: [ { id:'t1', code:'1v1', name:'一对一' }, { id:'t2', code:'small', name:'小班' } ],
        studentStatus: [ { id:'s1', code:'studying', name:'在读' }, { id:'s2', code:'quit', name:'退学' } ],
        commonStatus: [ { id:'cs1', code:'enabled', name:'启用' }, { id:'cs2', code:'disabled', name:'停用' } ]
      }
    },
    openAdd(type) {
      this.itemModal.visible = true
      this.itemModal.type = type
      this.itemModal.form = { id: '', code: '', name: '', sortOrder: 0, status: 'enabled' }
    },
    openEdit(type, item) {
      this.itemModal.visible = true
      this.itemModal.type = type
      this.itemModal.form = { id: item.id, code: item.code, name: item.name, sortOrder: item.sortOrder || 0, status: item.status || 'enabled' }
    },
    async onDelete(item) {
      if (!item.id) { return }
      try {
        const res = await fetch(`/api/base/dict/delete/${item.id}`, { method:'DELETE' })
        const json = await res.json()
        if (json && json.code === 200) {
          if (this.$message) this.$message.success('删除成功')
          this.loadDicts()
        } else {
          if (this.$message) this.$message.error(json.message || '删除失败')
        }
      } catch (e) {
        if (this.$message) this.$message.error('删除失败')
      }
    },
    async saveItem() {
      this.itemModal.submitting = true
      const payload = { ...this.itemModal.form, type: this.itemModal.type }
      try {
        const url = payload.id ? `/api/base/dict/update/${payload.id}` : '/api/base/dict/save'
        const method = payload.id ? 'PUT' : 'POST'
        const res = await fetch(url, { method, headers: { 'Content-Type':'application/json' }, body: JSON.stringify(payload) })
        const json = await res.json()
        if (json && json.code === 200) {
          if (this.$message) this.$message.success('保存成功')
          this.itemModal.visible = false
          this.loadDicts()
        } else {
          if (this.$message) this.$message.error(json.message || '保存失败')
        }
      } catch(e) {
        if (this.$message) this.$message.error('保存失败')
      } finally {
        this.itemModal.submitting = false
      }
    }
  }
}
</script>

<style>
.dict-page { background: #fff; padding: 16px; }
</style>