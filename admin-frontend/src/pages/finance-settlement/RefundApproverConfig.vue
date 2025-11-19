<template>
  <div class="refund-approver-config">
    <a-page-header title="审批人配置" sub-title="配置退费审批各级审批人ID" />
    <a-card bordered>
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 12 }">
        <a-form-item label="一审审批人ID列表">
          <a-input v-model="form.level1" placeholder="如：101,102" />
        </a-form-item>
        <a-form-item label="二审审批人ID列表">
          <a-input v-model="form.level2" placeholder="如：201,202" />
        </a-form-item>
        <a-form-item label="财务审批人ID列表">
          <a-input v-model="form.finance" placeholder="如：301,302" />
        </a-form-item>
        <a-form-item :wrapper-col="{ span: 12, offset: 6 }">
          <a-space>
            <a-button type="primary" @click="save">保存</a-button>
            <a-button @click="load">刷新</a-button>
          </a-space>
        </a-form-item>
      </a-form>
      <a-alert type="info" show-icon style="margin-top:12px" message="说明" description="填写逗号分隔的用户ID列表。审批时必须从配置列表中选择审批人，若未配置将无法通过相应级别的审批。" />
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'RefundApproverConfig',
  data() {
    return { form: { level1: '', level2: '', finance: '' } }
  },
  methods: {
    toCsv(arr) { return Array.isArray(arr) ? arr.join(',') : '' },
    toArr(csv) { return (csv || '').split(',').map(s => Number(s.trim())).filter(v => Number.isFinite(v)) },
    async load() {
      try { const res = await fetch('/api/finance/refund/approver/config'); const json = await res.json(); const d = json && json.data; if (d) { this.form.level1 = this.toCsv(d.level1ApproverIds || []); this.form.level2 = this.toCsv(d.level2ApproverIds || []); this.form.finance = this.toCsv(d.financeApproverIds || []); } }
      catch(e) { this.$message && this.$message.error('加载失败') }
    },
    async save() {
      const payload = { level1ApproverIds: this.toArr(this.form.level1), level2ApproverIds: this.toArr(this.form.level2), financeApproverIds: this.toArr(this.form.finance) }
      try { const res = await fetch('/api/finance/refund/approver/config', { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) }); const json = await res.json(); if (json && json.code === 'ok') { this.$message && this.$message.success('保存成功') } else { throw new Error('失败') } }
      catch(e) { this.$message && this.$message.error('保存失败') }
    }
  },
  created() { this.load() }
}
</script>

<style scoped>
.refund-approver-config { background: #fff; padding: 16px; }
</style>