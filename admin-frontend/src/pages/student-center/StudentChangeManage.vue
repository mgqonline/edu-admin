<template>
  <div class="student-change">
    <a-page-header title="学员异动管理" sub-title="调班 / 休学 / 复学 / 退学" />

    <a-card bordered style="margin-bottom: 12px" title="调班申请与审批">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-row :gutter="16">
          <a-col :span="6"><a-form-item label="学员ID"><a-input-number v-model="transfer.studentId" :min="1" style="width:100%" /></a-form-item></a-col>
          <a-col :span="6"><a-form-item label="原班级ID"><a-input-number v-model="transfer.fromClassId" :min="1" style="width:100%" /></a-form-item></a-col>
          <a-col :span="6"><a-form-item label="新班级ID"><a-input-number v-model="transfer.toClassId" :min="1" style="width:100%" /></a-form-item></a-col>
          <a-col :span="6"><a-form-item label="备注"><a-input v-model="transfer.reason" /></a-form-item></a-col>
        </a-row>
        <a-form-item :wrapper-col="{ span: 16, offset: 6 }">
          <a-space>
            <a-button type="primary" @click="applyTransfer">发起申请</a-button>
            <a-button @click="loadTransferList">加载列表</a-button>
          </a-space>
        </a-form-item>
      </a-form>
      <a-divider />
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }" style="margin-top: 12px">
        <a-row :gutter="16">
          <a-col :span="6"><a-form-item label="审批记录ID"><a-input-number v-model="approve.id" :min="1" style="width:100%" /></a-form-item></a-col>
          <a-col :span="6"><a-form-item label="原班教师ID"><a-input-number v-model="approve.approverFrom" :min="1" style="width:100%" /></a-form-item></a-col>
          <a-col :span="6"><a-form-item label="新班教师ID"><a-input-number v-model="approve.approverTo" :min="1" style="width:100%" /></a-form-item></a-col>
        </a-row>
        <a-form-item :wrapper-col="{ span: 16, offset: 6 }">
          <a-space>
            <a-button type="primary" @click="approveTransfer">审批通过</a-button>
          </a-space>
        </a-form-item>
      </a-form>
      <a-table :data-source="transferList" :columns="transferColumns" rowKey="id" :pagination="false" />
    </a-card>

    <a-card bordered style="margin-bottom: 12px" title="休学 / 复学">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-row :gutter="16">
          <a-col :span="6"><a-form-item label="学员ID"><a-input-number v-model="suspend.studentId" :min="1" style="width:100%" /></a-form-item></a-col>
          <a-col :span="6"><a-form-item label="原因"><a-input v-model="suspend.reason" /></a-form-item></a-col>
            <a-col :span="6"><a-form-item label="预计复学日期"><CompatDatePicker v-model="suspend.resumeDate" style="width:100%" /></a-form-item></a-col>
        </a-row>
        <a-form-item :wrapper-col="{ span: 16, offset: 6 }">
          <a-space>
            <a-button type="primary" @click="applySuspend">登记休学</a-button>
            <a-button @click="applyResume">复学</a-button>
            <a-button @click="loadSuspendList">加载列表</a-button>
          </a-space>
        </a-form-item>
      </a-form>
      <a-table :data-source="suspendList" :columns="suspendColumns" rowKey="id" :pagination="false" />
    </a-card>

    <a-card bordered title="退学与退费核算">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-row :gutter="16">
          <a-col :span="6"><a-form-item label="学员ID"><a-input-number v-model="withdraw.studentId" :min="1" style="width:100%" /></a-form-item></a-col>
          <a-col :span="6"><a-form-item label="原因"><a-input v-model="withdraw.reason" /></a-form-item></a-col>
        </a-row>
        <a-form-item :wrapper-col="{ span: 16, offset: 6 }">
          <a-space>
            <a-button type="primary" @click="applyWithdraw">发起退学</a-button>
            <a-button @click="loadWithdrawList">加载列表</a-button>
          </a-space>
        </a-form-item>
      </a-form>
      <a-table :data-source="withdrawList" :columns="withdrawColumns" rowKey="id" :pagination="false" />
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'StudentChangeManage',
  components: { CompatDatePicker: () => import('../../components/CompatDatePicker.vue') },
  data() {
    return {
      routeSid: null,
      transfer: { studentId: null, fromClassId: null, toClassId: null, reason: '' },
      approve: { id: null, approverFrom: null, approverTo: null },
      transferList: [],
      suspend: { studentId: null, reason: '', resumeDate: null },
      suspendList: [],
      withdraw: { studentId: null, reason: '' },
      withdrawList: [],
      transferColumns: [
        { title: 'ID', dataIndex: 'id', key: 'id' },
        { title: '学员ID', dataIndex: 'studentId', key: 'studentId' },
        { title: '原班级', dataIndex: 'fromClassId', key: 'fromClassId' },
        { title: '新班级', dataIndex: 'toClassId', key: 'toClassId' },
        { title: '状态', dataIndex: 'status', key: 'status' },
        { title: '备注', dataIndex: 'remark', key: 'remark' }
      ],
      suspendColumns: [
        { title: 'ID', dataIndex: 'id', key: 'id' },
        { title: '学员ID', dataIndex: 'studentId', key: 'studentId' },
        { title: '类型', dataIndex: 'type', key: 'type' },
        { title: '原因', dataIndex: 'reason', key: 'reason' },
        { title: '预计复学', dataIndex: 'resumeDate', key: 'resumeDate' },
        { title: '状态', dataIndex: 'status', key: 'status' }
      ],
      withdrawColumns: [
        { title: 'ID', dataIndex: 'id', key: 'id' },
        { title: '学员ID', dataIndex: 'studentId', key: 'studentId' },
        { title: '退费金额', dataIndex: 'refundAmount', key: 'refundAmount' },
        { title: '状态', dataIndex: 'status', key: 'status' },
        { title: '原因', dataIndex: 'reason', key: 'reason' }
      ]
    }
  },
  methods: {
    async applyTransfer() {
      const res = await fetch('/api/student/change/transfer/apply', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(this.transfer) })
      const json = await res.json();
      if (json.code === 200) { this.$message.success('申请成功'); this.loadTransferList() } else { this.$message.error(json.msg||'失败') }
    },
    async approveTransfer() {
      const res = await fetch('/api/student/change/transfer/approve', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(this.approve) })
      const json = await res.json();
      if (json.code === 200) { this.$message.success('审批通过'); this.loadTransferList() } else { this.$message.error(json.msg||'失败') }
    },
    async loadTransferList() {
      const res = await fetch('/api/student/change/transfer/list')
      const json = await res.json();
      let arr = json.data || []
      if (this.routeSid) { arr = arr.filter(x => Number(x.studentId) === this.routeSid) }
      this.transferList = arr
    },
    async applySuspend() {
      const payload = { ...this.suspend, resumeDate: this.suspend.resumeDate ? this.suspend.resumeDate.format('YYYY-MM-DD') : null }
      const res = await fetch('/api/student/change/suspend/apply', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
      const json = await res.json();
      if (json.code === 200) { this.$message.success('休学登记成功'); this.loadSuspendList() } else { this.$message.error(json.msg||'失败') }
    },
    async applyResume() {
      const res = await fetch('/api/student/change/resume/apply', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ studentId: this.suspend.studentId }) })
      const json = await res.json();
      if (json.code === 200) { this.$message.success('复学成功'); this.loadSuspendList() } else { this.$message.error(json.msg||'失败') }
    },
    async loadSuspendList() {
      const res = await fetch('/api/student/change/suspend/list')
      const json = await res.json();
      let arr = json.data || []
      if (this.routeSid) { arr = arr.filter(x => Number(x.studentId) === this.routeSid) }
      this.suspendList = arr
    },
    async applyWithdraw() {
      const res = await fetch('/api/student/change/withdraw/apply', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(this.withdraw) })
      const json = await res.json();
      if (json.code === 200) { this.$message.success('退学发起成功'); this.loadWithdrawList() } else { this.$message.error(json.msg||'失败') }
    },
    async loadWithdrawList() {
      const res = await fetch('/api/student/change/withdraw/list')
      const json = await res.json();
      let arr = json.data || []
      if (this.routeSid) { arr = arr.filter(x => Number(x.studentId) === this.routeSid) }
      this.withdrawList = arr
    }
  },
  created() {
    try {
      const q = (this.$route && this.$route.query) ? this.$route.query : {}
      if (q && q.studentId) {
        const sid = Number(q.studentId) || null
        this.routeSid = sid
        this.transfer.studentId = sid
        this.suspend.studentId = sid
        this.withdraw.studentId = sid
      }
    } catch(e) { /* no-op */ }
    this.loadTransferList();
    this.loadSuspendList();
    this.loadWithdrawList();
  }
}
</script>

<style scoped>
.student-change { }
</style>