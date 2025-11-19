<template>
  <div class="invoice-apply">
    <a-page-header title="开票申请" sub-title="学员提交抬头、税号、金额并关联缴费记录" />
    <a-card bordered title="申请信息" style="margin-bottom:12px">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 12 }">
        <a-form-item label="学员">
          <a-select v-model="form.studentId" placeholder="选择学员" style="width:100%" allowClear>
            <a-select-option v-for="s in studentOptions" :key="s.id" :value="s.id">{{ s.name }}（ID: {{ s.id }}）</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="关联缴费记录">
          <a-select v-model="form.settlementId" placeholder="选择缴费记录" style="width:100%" allowClear>
            <a-select-option v-for="s in settlements" :key="s.id" :value="s.id">{{ s.id }} | 学员 {{ s.studentId }} | 金额 ¥{{ s.totalFee }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="发票抬头"><a-input v-model="form.title" placeholder="填写发票抬头" /></a-form-item>
        <a-form-item label="税号"><a-input v-model="form.taxNo" placeholder="填写税号" /></a-form-item>
        <a-form-item label="开票金额"><a-input-number v-model="form.amount" :min="0" style="width:100%" /></a-form-item>
        <a-form-item :wrapper-col="{ span: 12, offset: 6 }">
          <a-space>
            <a-button type="primary" v-perm="'finance:invoice:apply'" @click="submit">提交申请</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card bordered title="最近申请">
      <a-table :data-source="applications" :columns="columns" rowKey="id" :pagination="false">
        <template v-slot:status="slotProps">
          <a-tag color="orange" v-if="slotProps.text==='APPLIED'">已申请</a-tag>
          <a-tag color="green" v-else>已开票</a-tag>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'InvoiceApply',
  data() {
    return {
      form: { studentId: '', settlementId: null, title: '', taxNo: '', amount: 0 },
      studentOptions: [],
      settlements: [],
      applications: [],
      columns: [
        { title: '申请ID', dataIndex: 'id', key: 'id' },
        { title: '学员ID', dataIndex: 'studentId', key: 'studentId' },
        { title: '缴费记录ID', dataIndex: 'settlementId', key: 'settlementId' },
        { title: '抬头', dataIndex: 'title', key: 'title' },
        { title: '税号', dataIndex: 'taxNo', key: 'taxNo' },
        { title: '金额', dataIndex: 'amount', key: 'amount' },
        { title: '状态', dataIndex: 'status', key: 'status', scopedSlots: { customRender: 'status' } }
      ]
    }
  },
  methods: {
    async loadStudentOptions() {
      try { const res = await fetch('/api/student/list'); const json = await res.json(); const arr = Array.isArray(json.data) ? json.data : []; this.studentOptions = arr.map(it => ({ id: it.id, name: it.name })) } catch(e) { this.studentOptions = [] }
    },
    async loadSettlements() {
      try { const res = await fetch('/api/finance/settlement/list'); const json = await res.json(); this.settlements = Array.isArray(json.data) ? json.data : [] } catch(e) { this.settlements = [] }
    },
    async loadApplications() {
      try { const res = await fetch('/api/finance/invoice/applications'); const json = await res.json(); this.applications = Array.isArray(json.data) ? json.data : [] } catch(e) { this.applications = [] }
    },
    async submit() {
      const payload = { studentId: this.form.studentId, settlementId: this.form.settlementId, title: this.form.title, taxNo: this.form.taxNo, amount: this.form.amount }
      const res = await fetch('/api/finance/invoice/apply', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
      const json = await res.json()
      if (json && json.code === 0) { this.$message && this.$message.success('提交成功'); this.loadApplications(); this.reset() } else { this.$message && this.$message.error('提交失败：'+(json.msg||'未知错误')) }
    },
    reset() { this.form = { studentId: '', settlementId: null, title: '', taxNo: '', amount: 0 } }
  },
  mounted() { this.loadStudentOptions(); this.loadSettlements(); this.loadApplications() }
}
</script>

<style scoped>
.invoice-apply { }
</style>