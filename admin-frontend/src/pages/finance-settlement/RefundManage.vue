<template>
  <div class="refund-manage">
    <a-page-header title="退费管理" sub-title="退费申请、核算、审批与执行" />

    <!-- 退费申请 -->
    <a-card bordered title="退费申请" style="margin-bottom:12px">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 12 }">
        <a-form-item label="学员">
          <a-select v-model="apply.studentId" placeholder="选择学员" style="width:100%" allowClear>
            <a-select-option v-for="s in studentOptions" :key="s.id" :value="s.id">{{ s.name }}（ID: {{ s.id }}）</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="班级"><a-select v-model="apply.classId" placeholder="选择班级" style="width:100%">
          <a-select-option v-for="c in classOptions" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
        </a-select></a-form-item>
        <a-form-item label="剩余课时"><a-input-number v-model="apply.remainingHours" :min="0" style="width:100%" /></a-form-item>
        <a-form-item label="单价"><a-input-number v-model="apply.unitPrice" :min="0" style="width:100%" /></a-form-item>
        <a-form-item label="手续费"><a-input-number v-model="apply.serviceFee" :min="0" style="width:100%" /></a-form-item>
        <a-form-item label="退费原因"><a-textarea v-model="apply.reason" :rows="3" placeholder="填写退费原因" /></a-form-item>
        <a-form-item :wrapper-col="{ span: 12, offset: 6 }">
          <a-space>
            <a-button type="primary" v-perm="'finance:refund:apply'" @click="submitApply">提交申请</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 退费记录列表与操作 -->
    <a-card bordered title="退费记录">
      <a-table :data-source="list" :columns="columns" rowKey="id" :pagination="false">
        <span slot="status" slot-scope="text, record">
          <a-tag v-if="text==='PENDING_L1'" color="orange">待一审</a-tag>
          <a-tag v-else-if="text==='PENDING_L2'" color="gold">待二审</a-tag>
          <a-tag v-else-if="text==='PENDING_FINANCE'" color="blue">待财务</a-tag>
          <a-tag v-else-if="text==='APPROVED'" color="green">已审批</a-tag>
          <a-tag v-else-if="text==='REFUNDED'" color="green">已退款</a-tag>
          <a-tag v-else-if="text==='REJECTED'" color="red">已驳回</a-tag>
          <a-tag v-else>{{ text }}</a-tag>
        </span>
        <span slot="amount" slot-scope="text, record">
          自动：{{ toFixed(record.autoAmount) }} / 实际：<strong>{{ toFixed(record.finalAmount) }}</strong>
        </span>
        <span slot="refundMethod" slot-scope="text, record">
          <span v-if="record.status==='REFUNDED'">{{ formatRefundMethod(text) }}</span>
          <span v-else>—</span>
        </span>
        <span slot="refundAt" slot-scope="text, record">
          <span v-if="record.status==='REFUNDED' && text">{{ formatTime(text) }}</span>
          <span v-else>—</span>
        </span>
        <span slot="action" slot-scope="text, record">
          <a-space>
            <a-button size="small" v-perm="'finance:refund:adjust'" @click="openAdjust(record)" :disabled="record.status==='REFUNDED'">调整金额</a-button>
            <a-dropdown v-perm="'finance:refund:approve'">
              <a-button size="small" :disabled="record.status==='REFUNDED'">审批</a-button>
              <a-menu slot="overlay">
                <a-menu-item @click="openApprove(record, '1', true)">一审通过</a-menu-item>
                <a-menu-item @click="openApprove(record, '2', true)">二审通过</a-menu-item>
                <a-menu-item @click="openApprove(record, 'finance', true)">财务通过</a-menu-item>
                <a-menu-divider />
                <a-menu-item @click="approve(record, '1', false)">一审驳回</a-menu-item>
              </a-menu>
            </a-dropdown>
            <a-button size="small" type="primary" v-perm="'finance:refund:execute'" @click="openExecute(record)" :disabled="record.status!=='APPROVED'">执行退款</a-button>
          </a-space>
        </span>
      </a-table>
    </a-card>

    <!-- 调整弹窗 -->
    <a-modal v-model="adjust.visible" title="调整退款金额" @ok="doAdjust" ok-text="保存" cancel-text="取消">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 12 }">
        <a-form-item label="自动金额"><a-input :value="toFixed(adjust.autoAmount)" disabled /></a-form-item>
        <a-form-item label="实际金额"><a-input-number v-model="adjust.finalAmount" :min="0" style="width:100%" /></a-form-item>
        <a-form-item label="调整说明"><a-textarea v-model="adjust.note" :rows="3" /></a-form-item>
      </a-form>
    </a-modal>

    <!-- 执行退款弹窗 -->
    <a-modal v-model="exec.visible" title="执行退款" @ok="doExecute" ok-text="执行" cancel-text="取消">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 12 }">
        <a-form-item label="退款方式">
          <a-select v-model="exec.method" placeholder="选择退款方式">
            <a-select-option value="cash">现金</a-select-option>
            <a-select-option value="wechat">微信</a-select-option>
            <a-select-option value="alipay">支付宝</a-select-option>
            <a-select-option value="bankcard">银行卡</a-select-option>
            <a-select-option value="banktransfer">银行转账</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="交易/收据号"><a-input v-model="exec.txnId" placeholder="填写交易或收据编号" /></a-form-item>
      </a-form>
    </a-modal>

    <!-- 审批弹窗：选择审批人 -->
    <a-modal v-model="approveDlg.visible" title="选择审批人" @ok="doApprove" ok-text="提交" cancel-text="取消">
      <div v-if="approveDlg.level==='1'">
        <a-alert type="info" message="为一审选择审批人" show-icon style="margin-bottom:8px" />
        <a-select v-model="approveDlg.approverId" placeholder="请选择一审审批人" style="width:100%">
          <a-select-option v-for="u in approverConfig.level1ApproverIds" :key="u" :value="u">审批人ID：{{ u }}</a-select-option>
        </a-select>
        <div v-if="approverConfig.level1ApproverIds.length===0" style="margin-top:8px;color:#999">未配置一审审批人，无法通过，请在“审批人配置”页面设置</div>
      </div>
      <div v-else-if="approveDlg.level==='2'">
        <a-alert type="info" message="为二审选择审批人" show-icon style="margin-bottom:8px" />
        <a-select v-model="approveDlg.approverId" placeholder="请选择二审审批人" style="width:100%">
          <a-select-option v-for="u in approverConfig.level2ApproverIds" :key="u" :value="u">审批人ID：{{ u }}</a-select-option>
        </a-select>
        <div v-if="approverConfig.level2ApproverIds.length===0" style="margin-top:8px;color:#999">未配置二审审批人，无法通过，请在“审批人配置”页面设置</div>
      </div>
      <div v-else>
        <a-alert type="info" message="为财务终审选择审批人" show-icon style="margin-bottom:8px" />
        <a-select v-model="approveDlg.approverId" placeholder="请选择财务审批人" style="width:100%">
          <a-select-option v-for="u in approverConfig.financeApproverIds" :key="u" :value="u">审批人ID：{{ u }}</a-select-option>
        </a-select>
        <div v-if="approverConfig.financeApproverIds.length===0" style="margin-top:8px;color:#999">未配置财务审批人，无法通过，请在“审批人配置”页面设置</div>
      </div>
    </a-modal>
  </div>
</template>

<script>
export default {
  name: 'RefundManage',
  data() {
    return {
      classOptions: [],
      studentOptions: [],
      apply: { studentId: null, classId: null, remainingHours: 0, unitPrice: 0, serviceFee: 0, reason: '' },
      list: [],
      columns: [
        { title: '学员ID', dataIndex: 'studentId', key: 'studentId' },
        { title: '班级ID', dataIndex: 'classId', key: 'classId' },
        { title: '金额', key: 'amount', scopedSlots: { customRender: 'amount' } },
        { title: '状态', dataIndex: 'status', key: 'status', scopedSlots: { customRender: 'status' } },
        { title: '退款方式', dataIndex: 'refundMethod', key: 'refundMethod', scopedSlots: { customRender: 'refundMethod' } },
        { title: '到账时间', dataIndex: 'refundAt', key: 'refundAt', scopedSlots: { customRender: 'refundAt' } },
        { title: '操作', key: 'action', scopedSlots: { customRender: 'action' } }
      ],
      adjust: { visible: false, id: null, autoAmount: 0, finalAmount: 0, note: '' },
      exec: { visible: false, id: null, method: '', txnId: '' },
      approveDlg: { visible: false, id: null, level: '1', pass: true, approverId: null },
      approverConfig: { level1ApproverIds: [], level2ApproverIds: [], financeApproverIds: [] }
    }
  },
  methods: {
    toFixed(n) { const x = Number(n||0); return Number.isFinite(x) ? x.toFixed(2) : '0.00' },
    async loadStudentOptions() { try { const res = await fetch('/api/student/list'); const json = await res.json(); const arr = Array.isArray(json.data) ? json.data : []; this.studentOptions = arr.map(it => ({ id: it.id, name: it.name })) } catch(e) { this.studentOptions = [] } },
    async loadClassOptions() { try { const res = await fetch('/api/class/list'); const json = await res.json(); this.classOptions = Array.isArray(json.data) ? json.data : [] } catch(e) { this.classOptions = [] } },
    async loadList() {
      try {
        const res = await fetch('/api/finance/refund/list');
        const json = await res.json();
        let arr = Array.isArray(json.data) ? json.data : []
        // 路由参数过滤学员
        const q = (this.$route && this.$route.query) ? this.$route.query : {}
        const sid = q && q.studentId ? Number(q.studentId) : null
        if (sid) { arr = arr.filter(r => Number(r.studentId) === sid) }
        this.list = arr
      } catch(e) { this.list = [] }
    },
    async loadApproverConfig() {
      try { const res = await fetch('/api/finance/refund/approver/config'); const json = await res.json(); const d = json && json.data; this.approverConfig = d ? d : { level1ApproverIds: [], level2ApproverIds: [], financeApproverIds: [] } } catch(e) { this.approverConfig = { level1ApproverIds: [], level2ApproverIds: [], financeApproverIds: [] } }
    },
    async submitApply() {
      const p = { studentId: Number(this.apply.studentId), classId: Number(this.apply.classId), remainingHours: Number(this.apply.remainingHours||0), unitPrice: Number(this.apply.unitPrice||0), serviceFee: Number(this.apply.serviceFee||0), reason: this.apply.reason||'' }
      try { const res = await fetch('/api/finance/refund/apply', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(p) }); const json = await res.json(); if (json && json.code === 'ok') { this.$message && this.$message.success('申请已提交'); this.loadList() } else { throw new Error('失败') } } catch(e) { this.$message && this.$message.error('申请失败') }
    },
    openAdjust(rec) { this.adjust = { visible: true, id: rec.id, autoAmount: rec.autoAmount, finalAmount: Number(rec.finalAmount||0), note: '' } },
    async doAdjust() {
      try { const res = await fetch(`/api/finance/refund/adjust/${this.adjust.id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ finalAmount: Number(this.adjust.finalAmount||0), note: this.adjust.note||'' }) }); const json = await res.json(); if (json && json.code === 'ok') { this.$message && this.$message.success('调整已保存'); this.adjust.visible = false; this.loadList() } else { throw new Error('失败') } } catch(e) { this.$message && this.$message.error('调整失败') }
    },
    openApprove(rec, level, pass) {
      this.approveDlg = { visible: true, id: rec.id, level, pass, approverId: null }
      // 如果只有一个审批人，默认选中以减少点击
      const map = { '1': this.approverConfig.level1ApproverIds, '2': this.approverConfig.level2ApproverIds, 'finance': this.approverConfig.financeApproverIds }
      const arr = map[level] || []
      if (arr.length === 1) this.approveDlg.approverId = arr[0]
    },
    async doApprove() {
      if (!this.approveDlg.approverId) { this.$message && this.$message.warning('请选择审批人'); return }
      try {
        const res = await fetch(`/api/finance/refund/approve/${this.approveDlg.id}`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ level: this.approveDlg.level, pass: this.approveDlg.pass, approverId: this.approveDlg.approverId }) })
        const json = await res.json()
        if (json && json.code === 'ok') { this.$message && this.$message.success('已处理审批'); this.approveDlg.visible = false; this.loadList() } else { const msg = (json && json.data) || '失败'; this.$message && this.$message.error('审批失败：' + msg) }
      } catch(e) { this.$message && this.$message.error('审批失败') }
    },
    openExecute(rec) { this.exec = { visible: true, id: rec.id, method: '', txnId: '' } },
    async doExecute() {
      try { const res = await fetch(`/api/finance/refund/execute/${this.exec.id}`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ refundMethod: this.exec.method || '', refundTxnId: this.exec.txnId || '' }) }); const json = await res.json(); if (json && json.code === 'ok') { this.$message && this.$message.success('退款已执行'); this.exec.visible = false; this.loadList() } else { throw new Error('失败') } } catch(e) { this.$message && this.$message.error('执行失败') }
    }
    ,
    formatRefundMethod(m) {
      if (!m) return '-'
      const map = { cash: '现金', wechat: '微信', alipay: '支付宝', bankcard: '银行卡', bank: '银行转账', banktransfer: '银行转账', '银行转账': '银行转账', '现金': '现金', '微信': '微信', '支付宝': '支付宝', '银行卡': '银行卡' }
      return map[m] || m
    },
    formatTime(t) { try { const d = new Date(t); if (isNaN(d.getTime())) return String(t); return d.toLocaleString(); } catch(e) { return String(t) } }
  },
  created() {
    this.loadStudentOptions();
    this.loadClassOptions();
    // 路由参数预选学员
    try {
      const q = (this.$route && this.$route.query) ? this.$route.query : {}
      if (q && q.studentId) { this.apply.studentId = Number(q.studentId) || null }
    } catch(e) { /* no-op */ }
    this.loadList();
    this.loadApproverConfig();
  }
}
</script>

<style scoped>
.refund-manage { background: #fff; padding: 16px; }
</style>