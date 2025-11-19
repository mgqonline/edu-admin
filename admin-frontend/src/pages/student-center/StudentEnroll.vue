<template>
  <div class="student-enroll">
    <a-page-header title="学员报名" sub-title="选择班级、费用与审批/合同签署" />

    <a-card bordered style="margin-bottom: 12px" title="报名信息">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-form-item label="学员ID">
              <a-input-number v-model="form.studentId" :min="1" style="width:100%" placeholder="学员ID" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="班级ID">
              <a-input-number v-model="form.classId" :min="1" style="width:100%" placeholder="班级ID" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="费用(元)">
              <a-input-number v-model="form.fee" :min="0" :step="100" style="width:100%" placeholder="学费" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="需审批">
              <a-switch v-model="form.approvalRequired" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="备注">
          <a-input v-model="form.applyInfo" placeholder="报名备注（将自动附加教材费说明）" />
        </a-form-item>
        <a-form-item :wrapper-col="{ span: 16, offset: 6 }">
          <a-space>
            <a-button type="primary" @click="apply">申请报名</a-button>
            <a-button @click="loadList">加载列表</a-button>
            <a-button type="dashed" @click="loadLast">获取最新报名</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card bordered title="报名记录">
      <a-table :data-source="list" :columns="columns" rowKey="id" :pagination="false">
        <template v-slot:status="slotProps">
          <a-tag color="orange" v-if="slotProps && slotProps.record && slotProps.record.status==='pending'">待审批</a-tag>
          <a-tag color="green" v-else-if="slotProps && slotProps.record && slotProps.record.status==='approved'">已通过</a-tag>
          <a-tag color="red" v-else-if="slotProps && slotProps.record && slotProps.record.status==='rejected'">已驳回</a-tag>
          <a-tag v-else>未知</a-tag>
        </template>
        <template v-slot:action="slotProps">
          <a-space v-if="slotProps && slotProps.record">
            <a-button size="small" @click="genContract(slotProps.record)">生成合同</a-button>
            <a-button size="small" @click="signContract(slotProps.record)" :disabled="!slotProps.record.contractUrl">签署合同</a-button>
            <a-button size="small" type="primary" @click="approve(slotProps.record, 'approve')" :disabled="slotProps.record.status!=='pending'">审批通过</a-button>
            <a-button size="small" type="danger" @click="approve(slotProps.record, 'reject')" :disabled="slotProps.record.status!=='pending'">驳回</a-button>
            <a-button size="small" type="dashed" @click="remove(slotProps.record)">删除</a-button>
          </a-space>
          <span v-else>—</span>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'StudentEnroll',
  data() {
    return {
      form: { studentId: null, classId: null, fee: 0, approvalRequired: false, applyInfo: '' },
      list: [],
      columns: [
        { title: 'ID', dataIndex: 'id', key: 'id' },
        { title: '学员ID', dataIndex: 'studentId', key: 'studentId' },
        { title: '班级ID', dataIndex: 'classId', key: 'classId' },
        { title: '费用', dataIndex: 'fee', key: 'fee' },
        { title: '教材费', dataIndex: 'materialsFee', key: 'materialsFee' },
        { title: '状态', dataIndex: 'status', key: 'status', scopedSlots: { customRender: 'status' } },
        { title: '合同', dataIndex: 'contractUrl', key: 'contractUrl' },
        { title: '签署', dataIndex: 'signed', key: 'signed' },
        { title: '操作', key: 'action', scopedSlots: { customRender: 'action' } }
      ]
    }
  },
  methods: {
    readQuery() {
      try {
        const q = (this.$route && this.$route.query) || {}
        if (q.studentId) this.form.studentId = Number(q.studentId)
        if (q.recommend) this.form.applyInfo = String(q.recommend)
      } catch(e) {}
    },
    async apply() {
      if (!this.form.studentId || !this.form.classId) {
        this.$message && this.$message.error('请填写学员ID与班级ID')
        return
      }
      try {
        const res = await fetch('/api/enroll/apply', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(this.form) })
        const json = await res.json()
        if (json.code === 200) { this.$message && this.$message.success('报名成功'); await this.loadList() } else { this.$message && this.$message.error('报名失败：'+(json.msg||'未知错误')) }
      } catch(e) { this.$message && this.$message.error('报名异常') }
    },
    async loadList() {
      const params = []
      if (this.form.studentId) params.push('studentId='+encodeURIComponent(this.form.studentId))
      const url = '/api/enroll/list' + (params.length?('?'+params.join('&')):'')
      try { const res = await fetch(url); const json = await res.json(); this.list = Array.isArray(json.data) ? json.data : [] } catch(e) { this.list = [] }
    },
    async loadLast() {
      if (!this.form.studentId) { this.$message && this.$message.warning('请先填写学员ID'); return }
      try { const res = await fetch(`/api/enroll/last?studentId=${encodeURIComponent(this.form.studentId)}`); const json = await res.json(); if (json.code===200) { this.$message && this.$message.success('已获取最新报名'); this.list = [json.data].concat(this.list) } else { this.$message && this.$message.error('未找到最新报名') } } catch(e) {}
    },
    async genContract(r) {
      try { const res = await fetch('/api/enroll/contract/generate', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ enrollId: r.id }) }); const json = await res.json(); if (json.code===200) { this.$message && this.$message.success('合同已生成'); await this.loadList() } else { this.$message && this.$message.error('生成失败') } } catch(e) {}
    },
    async approve(r, action) {
      try { const res = await fetch('/api/enroll/approve', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ enrollId: r.id, action }) }); const json = await res.json(); if (json.code===200) { this.$message && this.$message.success(action==='approve'?'已通过':'已驳回'); await this.loadList() } else { this.$message && this.$message.error(json.msg||'失败') } } catch(e) {}
    },
    async signContract(r) {
      try { const res = await fetch('/api/enroll/contract/sign', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ enrollId: r.id }) }); const json = await res.json(); if (json.code===200) { this.$message && this.$message.success('已签署'); await this.loadList() } else { this.$message && this.$message.error(json.msg||'失败') } } catch(e) {}
    },
    async remove(r) {
      try { const res = await fetch('/api/enroll/delete', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ id: r.id }) }); const json = await res.json(); if (json.code===200) { this.$message && this.$message.success('已删除'); await this.loadList() } else { this.$message && this.$message.error(json.msg||'失败') } } catch(e) {}
    }
  },
  async mounted() {
    this.readQuery()
    await this.loadList()
  }
}
</script>

<style scoped>
.student-enroll { }
</style>