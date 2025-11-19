<template>
  <div class="invoice-stat">
    <a-page-header title="开票统计" sub-title="按月份/校区统计开票金额，导出明细" />
    <a-card bordered title="统计查询" style="margin-bottom:12px">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 12 }">
        <a-form-item label="月份"><V2SimpleDate v-model="query.month" :type="'month'" format="YYYY-MM" placeholder="选择月份" style="width:100%" /></a-form-item>
        <a-form-item label="学校"><a-select v-model="query.campusId" placeholder="选择学校(可选)" allowClear style="width:100%">
          <a-select-option v-for="c in campusOptions" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
        </a-select></a-form-item>
        <a-form-item :wrapper-col="{ span: 12, offset: 6 }">
          <a-space>
            <a-button type="primary" v-perm="'finance:invoice:stat'" @click="loadSummary">统计</a-button>
            <a-button v-perm="'finance:invoice:export'" @click="exportDetails">导出明细</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card bordered title="统计结果">
      <a-descriptions :column="1" bordered>
        <a-descriptions-item label="月份">{{ summary.month || '-' }}</a-descriptions-item>
        <a-descriptions-item label="总金额">{{ summary.totalAmount || 0 }}</a-descriptions-item>
      </a-descriptions>
      <a-divider>按校区金额</a-divider>
      <a-table :data-source="campusList" :columns="campusColumns" rowKey="campusId" :pagination="false" />
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'InvoiceStat',
  data() {
    return {
      query: { month: '', campusId: null },
      campusOptions: [],
      summary: { month: '', totalAmount: 0, byCampus: {} },
      campusColumns: [
        { title: '校区ID', dataIndex: 'campusId', key: 'campusId' },
        { title: '金额', dataIndex: 'amount', key: 'amount' }
      ]
    }
  },
  components: { V2SimpleDate: () => import('../../components/V2SimpleDate.vue') },
  computed: {
    campusList() {
      const m = this.summary && this.summary.byCampus ? this.summary.byCampus : {}
      return Object.keys(m).map(k => ({ campusId: Number(k), amount: m[k] }))
    }
  },
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
    async loadSummary() {
      if (!this.query.month) { this.$message && this.$message.warning('请填写月份'); return }
      const params = ['month='+encodeURIComponent(this.query.month)]
      if (this.query.campusId != null && this.query.campusId !== '') params.push('campusId='+encodeURIComponent(this.query.campusId))
      const url = '/api/finance/invoice/stat/summary?'+params.join('&')
      const res = await fetch(url)
      const json = await res.json()
      if (json && json.code === 0) { this.summary = json.data || { month: '', totalAmount: 0, byCampus: {} } } else { this.$message && this.$message.error('统计失败：'+(json.msg||'未知错误')) }
    },
    exportDetails() {
      if (!this.query.month) { this.$message && this.$message.warning('请填写月份'); return }
      const params = ['month='+encodeURIComponent(this.query.month)]
      if (this.query.campusId != null && this.query.campusId !== '') params.push('campusId='+encodeURIComponent(this.query.campusId))
      const url = '/api/finance/invoice/export/details?'+params.join('&')
      // 直接打开导出链接触发浏览器下载
      window.open(url, '_blank')
    }
  }
,
  mounted() { this.loadCampusOptions() }
}
</script>

<style scoped>
.invoice-stat { }
</style>