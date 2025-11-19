<template>
  <div style="background:#fff; padding:16px">
    <h3>课程管理</h3>
    <a-form layout="vertical">
      <a-row :gutter="16">
        <a-col :span="8">
          <a-form-item label="课程名称">
            <a-input v-model="form.name" placeholder="请输入课程名称" />
          </a-form-item>
        </a-col>
        <a-col :span="6">
          <a-form-item label="类别">
            <a-input v-model="form.category" placeholder="如 文化课/语言类" />
          </a-form-item>
        </a-col>
        <a-col :span="6">
          <a-form-item label="类型">
            <a-select v-model="form.type">
              <a-select-option value="班课">班课</a-select-option>
              <a-select-option value="一对一">一对一</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="6">
          <a-form-item label="适合年龄段">
            <a-input v-model="form.ageRange" placeholder="如 8-12" />
          </a-form-item>
        </a-col>
        <a-col :span="4">
          <a-form-item label="课时数">
            <a-input-number v-model="form.lessonCount" :min="1" style="width:100%" />
          </a-form-item>
        </a-col>
        <a-col :span="4">
          <a-form-item label="单价">
            <a-input-number v-model="form.unitPrice" :min="0" :step="10" style="width:100%" />
          </a-form-item>
        </a-col>
        <a-col :span="6">
          <a-form-item label="状态">
            <a-select v-model="form.status">
              <a-select-option value="draft">草稿</a-select-option>
              <a-select-option value="published">已发布</a-select-option>
              <a-select-option value="offline">已下架</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="课程大纲">
            <a-input-textarea v-model="form.outline" :rows="3" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="教学目标">
            <a-input-textarea v-model="form.objective" :rows="3" />
          </a-form-item>
        </a-col>
      </a-row>
      <a-space>
        <a-button type="primary" @click="saveCourse">保存课程</a-button>
        <a-button @click="resetForm">重置</a-button>
      </a-space>
    </a-form>

    <a-divider />
    <h4>课程列表</h4>
    <a-input-search v-model="keyword" placeholder="按名称关键字搜索" @search="loadCourses" style="max-width:320px; margin-bottom:8px" />
    <a-table :columns="courseColumns" :data-source="courses" rowKey="courseId" :pagination="false">
      <span slot="status" slot-scope="t">{{ statusText(t) }}</span>
      <span slot="actions" slot-scope="t, record">
        <a-space>
          <a @click="editCourse(record)">编辑</a>
          <a @click="openBindMaterials(record)">绑定教材</a>
          <a-dropdown>
            <a class="ant-dropdown-link">状态<i class="anticon anticon-down" /></a>
            <a-menu slot="overlay">
              <a-menu-item @click="changeStatus(record, 'draft')">草稿</a-menu-item>
              <a-menu-item @click="changeStatus(record, 'published')">已发布</a-menu-item>
              <a-menu-item @click="changeStatus(record, 'offline')">已下架</a-menu-item>
            </a-menu>
          </a-dropdown>
        </a-space>
      </span>
    </a-table>

    <a-modal :visible="bindVisible" title="绑定教材" @ok="doBindMaterials" @cancel="bindVisible=false">
      <a-select mode="multiple" style="width:100%" v-model="selectedMaterials" :placeholder="'选择教材'">
        <a-select-option v-for="tb in textbooks" :key="tb.textbookId" :value="tb.textbookId">
          {{ tb.name }}（¥{{ tb.unitPrice }} | 库存：{{ tb.stock }}）
        </a-select-option>
      </a-select>
    </a-modal>
  </div>
</template>

<script>
export default {
  name: 'CourseManage',
  data() {
    return {
      form: { name: '', category: '', type: '班课', ageRange: '', lessonCount: 32, unitPrice: 0, outline: '', objective: '', status: 'draft', courseId: '' },
      keyword: '',
      courses: [],
      textbooks: [],
      bindVisible: false,
      selectedCourseId: '',
      selectedMaterials: []
    }
  },
  computed: {
    courseColumns() {
      return [
        { title: '课程名称', dataIndex: 'name' },
        { title: '类别', dataIndex: 'category' },
        { title: '类型', dataIndex: 'type' },
        { title: '课时数', dataIndex: 'lessonCount' },
        { title: '单价', dataIndex: 'unitPrice' },
        { title: '状态', dataIndex: 'status', scopedSlots: { customRender: 'status' } },
        { title: '操作', key: 'actions', scopedSlots: { customRender: 'actions' } }
      ]
    }
  },
  created() {
    this.loadCourses()
    this.loadTextbooks()
  },
  methods: {
    statusText(s) {
      return s === 'draft' ? '草稿' : s === 'published' ? '已发布' : s === 'offline' ? '已下架' : s
    },
    async saveCourse() {
      const res = await fetch('/api/course/save', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(this.form) })
      const json = await res.json()
      if (json && json.code === 200) {
        this.$message.success('保存成功')
        this.form.courseId = json.data.courseId
        this.loadCourses()
      } else {
        this.$message.error('保存失败')
      }
    },
    resetForm() {
      this.form = { name: '', category: '', type: '班课', ageRange: '', lessonCount: 32, unitPrice: 0, outline: '', objective: '', status: 'draft', courseId: '' }
    },
    async loadCourses() {
      const url = '/api/course/list' + (this.keyword ? ('?keyword=' + encodeURIComponent(this.keyword)) : '')
      const res = await fetch(url)
      const json = await res.json()
      this.courses = (json && json.code === 200) ? (json.data || []) : []
    },
    editCourse(row) {
      this.form = Object.assign({}, row)
    },
    async changeStatus(row, status) {
      const res = await fetch(`/api/course/status/${row.courseId}?status=${status}`, { method: 'POST' })
      const json = await res.json()
      if (json && json.code === 200) {
        this.$message.success('状态已更新')
        this.loadCourses()
      }
    },
    async loadTextbooks() {
      const res = await fetch('/api/course/textbook/list')
      const json = await res.json()
      this.textbooks = (json && json.code === 200) ? (json.data || []) : []
    },
    openBindMaterials(row) {
      this.selectedCourseId = row.courseId
      this.selectedMaterials = Array.isArray(row.materials) ? row.materials.slice() : []
      this.bindVisible = true
    },
    async doBindMaterials() {
      const payload = { courseId: this.selectedCourseId, materialIds: this.selectedMaterials }
      const res = await fetch('/api/course/materials/bind', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
      const json = await res.json()
      if (json && json.code === 200) {
        this.$message.success('教材绑定成功')
        this.bindVisible = false
        this.loadCourses()
      } else {
        this.$message.error('绑定失败')
      }
    }
  }
}
</script>

<style scoped>
</style>