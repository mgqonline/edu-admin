<template>
  <div class="invoice-manage">
    <a-page-header title="发票管理" sub-title="记录发票号码、开票日期、寄送状态" />

    <a-card bordered title="申请列表" style="margin-bottom:12px">
      <a-table :data-source="applications" :columns="appColumns" rowKey="id" :pagination="false">
        <template v-slot:status="slotProps">
          <a-tag color="orange" v-if="slotProps.text==='APPLIED'">已申请</a-tag>
          <a-tag color="green" v-else>已开票</a-tag>
        </template>
        <template v-slot:action="slotProps">
          <a-space v-if="slotProps && slotProps.record">
            <a-button size="small" type="primary" v-perm="'finance:invoice:manage'" @click="openIssue(slotProps.record)" :disabled="String(slotProps.record.status)==='INVOICED'">开票</a-button>
          </a-space>
          <span v-else>—</span>
        </template>
      </a-table>
    </a-card>

    <a-card bordered title="发票记录">
      <a-space style="margin-bottom: 12px">
        <V2SimpleDate v-model="filter.month" :type="'month'" format="YYYY-MM" placeholder="选择月份" style="width:160px" />
        <a-select v-model="filter.campusId" placeholder="选择学校(可选)" allowClear style="width:180px">
          <a-select-option v-for="c in campusOptions" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
        </a-select>
        <a-button @click="loadRecords">查询</a-button>
      </a-space>
      <a-table :data-source="records" :columns="recColumns" rowKey="id" :pagination="false" />
    </a-card>

    <a-modal v-model="issueVisible" title="开具发票" :footer="null">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 12 }">
        <a-form-item label="申请ID"><a-input v-model="issue.applicationId" disabled /></a-form-item>
        <a-form-item label="学员ID"><a-input v-model="issue.studentId" /></a-form-item>
        <a-form-item label="缴费记录ID"><a-input v-model="issue.settlementId" /></a-form-item>
        <a-form-item label="校区ID"><a-input-number v-model="issue.campusId" :min="0" style="width:100%" /></a-form-item>
        <a-form-item label="发票号"><a-input v-model="issue.invoiceNo" placeholder="不填自动生成" /></a-form-item>
        <a-form-item label="开票日期"><V2SimpleDate v-model="issue.invoiceDate" :type="'date'" format="YYYY-MM-DD" style="width:100%" /></a-form-item>
        <a-form-item label="寄送状态">
          <a-select v-model="issue.deliveryStatus" style="width:100%">
            <a-select-option value="pending">待寄送</a-select-option>
            <a-select-option value="sent">已寄送</a-select-option>
            <a-select-option value="received">已签收</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="金额"><a-input-number v-model="issue.amount" :min="0" style="width:100%" /></a-form-item>
        <a-form-item :wrapper-col="{ span: 12, offset: 6 }">
          <a-space>
            <a-button type="primary" v-perm="'finance:invoice:manage'" @click="doIssue">确认开票</a-button>
            <a-button @click="issueVisible=false">取消</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
export default {
  name: 'InvoiceManage',
  data() {
    return {
      applications: [],
      records: [],
      filter: { month: '', campusId: null },
      campusOptions: [],
      issueVisible: false,
      issue: { applicationId: '', settlementId: '', studentId: '', campusId: null, invoiceNo: '', invoiceDate: '', deliveryStatus: 'pending', amount: 0 },
      appColumns: [
        { title: '申请ID', dataIndex: 'id', key: 'id' },
        { title: '学员ID', dataIndex: 'studentId', key: 'studentId' },
        { title: '金额', dataIndex: 'amount', key: 'amount' },
        { title: '状态', dataIndex: 'status', key: 'status', scopedSlots: { customRender: 'status' } },
        { title: '操作', key: 'action', scopedSlots: { customRender: 'action' } }
      ],
      recColumns: [
        { title: '记录ID', dataIndex: 'id', key: 'id' },
        { title: '发票号', dataIndex: 'invoiceNo', key: 'invoiceNo' },
        { title: '开票日期', dataIndex: 'invoiceDate', key: 'invoiceDate' },
        { title: '校区ID', dataIndex: 'campusId', key: 'campusId' },
        { title: '学员ID', dataIndex: 'studentId', key: 'studentId' },
        { title: '金额', dataIndex: 'amount', key: 'amount' },
        { title: '状态', dataIndex: 'deliveryStatus', key: 'deliveryStatus' }
      ]
    }
  },
  components: { V2SimpleDate: () => import('../../components/V2SimpleDate.vue') },
  methods: {
    async loadCampusOptions() {
      try {
        const res = await fetch('/api/base/campus/list')
        const json = await res.json()
        if (json && json.code === 200) {
          this.campusOptions = Array.isArray(json.data) ? json.data : []
        } else {
          this.campusOptions = []
        }
      } catch (e) { this.campusOptions = [] }
    },
    async loadApplications() {
      try { const res = await fetch('/api/finance/invoice/applications'); const json = await res.json(); this.applications = Array.isArray(json.data) ? json.data : [] } catch(e) { this.applications = [] }
    },
    async loadRecords() {
      const params = []
      if (this.filter.month) params.push('month='+encodeURIComponent(this.filter.month))
      if (this.filter.campusId != null && this.filter.campusId !== '') params.push('campusId='+encodeURIComponent(this.filter.campusId))
      const url = '/api/finance/invoice/records' + (params.length ? ('?'+params.join('&')) : '')
      try { const res = await fetch(url); const json = await res.json(); this.records = Array.isArray(json.data) ? json.data : [] } catch(e) { this.records = [] }
    },
    openIssue(app) {
      this.issueVisible = true
      this.issue = { applicationId: app.id, settlementId: app.settlementId, studentId: app.studentId, campusId: null, invoiceNo: '', invoiceDate: '', deliveryStatus: 'pending', amount: app.amount }
    },
    async doIssue() {
      const payload = { ...this.issue }
      // 适配日期为字符串 yyyy-MM-dd
      if (payload.invoiceDate && typeof payload.invoiceDate !== 'string') {
        try { const d = payload.invoiceDate; const y = d.year(); const m = (d.month()+1).toString().padStart(2,'0'); const day = d.date().toString().padStart(2,'0'); payload.invoiceDate = `${y}-${m}-${day}` } catch(e) {}
      }
      const res = await fetch('/api/finance/invoice/issue', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
      const json = await res.json()
      if (json && json.code === 0) { this.$message && this.$message.success('开票成功'); this.issueVisible=false; this.loadApplications(); this.loadRecords() } else { this.$message && this.$message.error('开票失败：'+(json.msg||'未知错误')) }
    }
  },
  mounted() { this.loadCampusOptions(); this.loadApplications(); this.loadRecords() }
}
</script>

<style scoped>
.invoice-manage { }
</style>