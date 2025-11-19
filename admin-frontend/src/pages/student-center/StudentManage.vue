<template>
  <div class="student-manage">
    <a-page-header title="学员管理" sub-title="查看与联动报班/续费/退费/记录" />

    <a-card bordered title="学员列表">
      <a-table :data-source="list" :columns="columns" rowKey="id" :pagination="false">
        <span slot="gender" slot-scope="text, record">
          {{ formatGender(text) }}
        </span>
        <span slot="studentType" slot-scope="text, record">
          <a-tag v-if="text==='老生'" color="geekblue">老生</a-tag>
          <a-tag v-else color="blue">新生</a-tag>
        </span>
        <span slot="action" slot-scope="text, record">
          <a-space>
            <a-button size="small" @click="openEnrollData(record)" :disabled="!record">报班数据</a-button>
            <a-button size="small" @click="openChange(record)" :disabled="!record">调班</a-button>
            <a-button size="small" type="default"
                      v-if="record && record._studentType==='新生'"
                      @click="openEnrollPay(record)">缴费</a-button>
            <a-button size="small" type="primary"
                      v-else
                      @click="openRenew(record)"
                      :disabled="!record">续费</a-button>
            <a-button size="small"
                      v-if="record && record._studentType==='老生'"
                      @click="openRefund(record)">退费</a-button>
            <a-button size="small"
                      v-if="record && record._studentType==='老生'"
                      @click="openPayRecords(record)">缴费记录</a-button>
          </a-space>
        </span>
      </a-table>
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'StudentManage',
  data() {
    return {
      list: [],
      settlementIds: new Set(),
      columns: [
        { title: '姓名', dataIndex: 'name', key: 'name' },
        { title: '性别', dataIndex: 'gender', key: 'gender', scopedSlots: { customRender: 'gender' } },
        { title: '校区', dataIndex: 'campusName', key: 'campusName' },
        { title: '学生类型', dataIndex: 'studentType', key: 'studentType', scopedSlots: { customRender: 'studentType' } },
        { title: '操作', key: 'action', scopedSlots: { customRender: 'action' } }
      ]
    }
  },
  methods: {
    formatGender(g) { const map = { male: '男', female: '女' }; return map[g] || (g || '-'); },
    async loadStudents() {
      try {
        const res = await fetch('/api/student/list')
        const json = await res.json()
        const arr = Array.isArray(json.data) ? json.data : []
        this.list = arr.map(s => ({ ...s, studentType: null }))
      } catch(e) {
        this.list = []
      }
    },
    async loadSettlements() {
      try {
        const res = await fetch('/api/finance/settlement/list')
        const json = await res.json()
        const arr = Array.isArray(json.data) ? json.data : []
        const ids = new Set(arr.map(x => x.studentId).filter(x => x != null))
        this.settlementIds = ids
      } catch(e) { this.settlementIds = new Set() }
    },
    computeStudentType() {
      this.list = (this.list||[]).map(s => {
        const sid = s.id != null ? s.id : s.studentId
        const type = this.settlementIds.has(sid) ? '老生' : '新生'
        return { ...s, studentType: type, _studentType: type }
      })
    },
    openEnrollData(rec) { const sid = rec.id || rec.studentId; this.$router && this.$router.push({ path: '/student/enroll', query: { studentId: sid } }) },
    openChange(rec) { const sid = rec.id || rec.studentId; this.$router && this.$router.push({ path: '/student/change', query: { studentId: sid } }) },
    openEnrollPay(rec) { const sid = rec.id || rec.studentId; this.$router && this.$router.push({ path: '/finance/settlement/fee', query: { studentId: sid } }) },
    openRenew(rec) { const sid = rec.id || rec.studentId; this.$router && this.$router.push({ path: '/finance/settlement/renew', query: { studentId: sid } }) },
    openRefund(rec) { const sid = rec.id || rec.studentId; this.$router && this.$router.push({ path: '/finance/settlement/refund', query: { studentId: sid } }) },
    openPayRecords(rec) { const sid = rec.id || rec.studentId; this.$router && this.$router.push({ path: '/finance/settlement/renew', query: { studentId: sid } }) },
  },
  created() {
    this.loadStudents()
      .then(() => this.loadSettlements())
      .then(() => this.computeStudentType())
  }
}
</script>

<style scoped>
.student-manage { background: #fff; padding: 16px; }
</style>