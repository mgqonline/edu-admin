<template>
  <div class="guardian-perm">
    <a-page-header title="监护人权限设置" sub-title="查看与操作权限" />

    <a-card bordered style="margin-bottom: 12px" title="配置监护人权限">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-row :gutter="16">
          <a-col :span="6"><a-form-item label="学员ID"><a-input-number v-model="form.studentId" :min="1" style="width:100%" /></a-form-item></a-col>
          <a-col :span="6"><a-form-item label="姓名"><a-input v-model="form.guardianName" /></a-form-item></a-col>
          <a-col :span="6"><a-form-item label="手机号"><a-input v-model="form.guardianPhone" /></a-form-item></a-col>
          <a-col :span="6"><a-form-item label="关系"><a-input v-model="form.relation" placeholder="父亲/母亲/亲属" /></a-form-item></a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-card size="small" title="可查看信息" bordered>
              <a-space direction="vertical">
                <a-checkbox v-model="viewPerms.schedule">课表</a-checkbox>
                <a-checkbox v-model="viewPerms.grades">成绩</a-checkbox>
                <a-checkbox v-model="viewPerms.payments">缴费</a-checkbox>
              </a-space>
            </a-card>
          </a-col>
          <a-col :span="12">
            <a-card size="small" title="可操作功能" bordered>
              <a-space direction="vertical">
                <a-checkbox v-model="actionPerms.leave">请假</a-checkbox>
                <a-checkbox v-model="actionPerms.renew">续费</a-checkbox>
              </a-space>
            </a-card>
          </a-col>
        </a-row>
        <a-form-item :wrapper-col="{ span: 16, offset: 6 }">
          <a-space>
            <a-button type="primary" @click="save">保存设置</a-button>
            <a-button @click="loadList">加载列表</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card bordered title="监护人权限列表">
      <a-table :data-source="list" :columns="columns" rowKey="id" :pagination="pagination" @change="onTableChange">
        <template v-slot:viewPerms="slotProps">
          {{ formatPerms(slotProps && slotProps.record && slotProps.record.viewPerms) }}
        </template>
        <template v-slot:actionPerms="slotProps">
          {{ formatPerms(slotProps && slotProps.record && slotProps.record.actionPerms) }}
        </template>
      </a-table>
    </a-card>
  </div>
  </template>

<script>
export default {
  name: 'GuardianPerm',
  data() {
    return {
      form: { studentId: null, guardianName: '', guardianPhone: '', relation: '' },
      viewPerms: { schedule: true, grades: true, payments: true },
      actionPerms: { leave: false, renew: false },
      list: [],
      pagination: { current: 1, pageSize: 10, total: 0, showSizeChanger: true, pageSizeOptions: ['10','20','50'] },
      columns: [
        { title: 'ID', dataIndex: 'id', key: 'id' },
        { title: '学员ID', dataIndex: 'studentId', key: 'studentId' },
        { title: '监护人', dataIndex: 'guardianName', key: 'guardianName' },
        { title: '手机号', dataIndex: 'guardianPhone', key: 'guardianPhone' },
        { title: '关系', dataIndex: 'relation', key: 'relation' },
        { title: '可查看', key: 'viewPerms', scopedSlots: { customRender: 'viewPerms' } },
        { title: '可操作', key: 'actionPerms', scopedSlots: { customRender: 'actionPerms' } }
      ]
    }
  },
  methods: {
    formatPerms(obj) {
      if (!obj) return '-'
      const map = { schedule: '课表', grades: '成绩', payments: '缴费', leave: '请假', renew: '续费' }
      const keys = Object.keys(obj).filter(k => obj[k])
      return keys.length ? keys.map(k => map[k] || k).join('、') : '-'
    },
    async save() {
      if (!this.form.studentId || !this.form.guardianPhone) {
        this.$message && this.$message.error('请填写学员ID与手机号');
        return
      }
      const payload = { ...this.form, viewPerms: this.viewPerms, actionPerms: this.actionPerms }
      const res = await fetch('/api/student/guardian/perm/set', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
      const json = await res.json()
      if (json.code === 200) {
        this.$message && this.$message.success('保存成功')
        this.loadList()
      } else {
        this.$message && this.$message.error('失败：' + (json.msg || '未知错误'))
      }
    },
    async loadList() {
      if (!this.form.studentId) { this.$message && this.$message.info('请先填写学员ID'); return }
      const params = new URLSearchParams()
      params.append('studentId', String(this.form.studentId))
      params.append('page', String(this.pagination.current || 1))
      params.append('size', String(this.pagination.pageSize || 10))
      const res = await fetch(`/api/student/guardian/perm/list?${params.toString()}`)
      const json = await res.json()
      const payload = json.data || {}
      this.list = Array.isArray(payload) ? payload : (payload.items || [])
      this.pagination.total = Number(payload.total || this.list.length)
    },
    onTableChange(pager) {
      this.pagination.current = pager.current
      this.pagination.pageSize = pager.pageSize
      this.loadList()
    }
  }
}
</script>

<style scoped>
.guardian-perm { }
</style>