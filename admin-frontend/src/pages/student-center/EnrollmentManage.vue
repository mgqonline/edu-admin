<template>
  <div class="enroll-manage">
    <a-page-header title="报名管理" sub-title="报名、合同与审批" />

    <a-card bordered style="margin-bottom: 12px" title="提交报名">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-row :gutter="16">
          <a-col :span="6"><a-form-item label="学员ID"><a-input-number v-model="form.studentId" :min="1" style="width:100%" /></a-form-item></a-col>
          <a-col :span="6">
            <a-form-item label="班级">
              <template v-if="classes.length">
                <a-select v-model="form.classId" show-search allowClear placeholder="选择班级" style="width:100%" @change="onClassChange">
                  <a-select-option v-for="cl in classes" :key="cl.id" :value="cl.id">{{ cl.name || ('班级#'+cl.id) }}</a-select-option>
                </a-select>
              </template>
              <template v-else>
                <a-input-number v-model="form.classId" :min="1" style="width:100%" placeholder="班级ID" />
              </template>
            </a-form-item>
          </a-col>
          <a-col :span="6"><a-form-item label="报名信息"><a-input v-model="form.applyInfo" placeholder="备注或补充说明" /></a-form-item></a-col>
          <a-col :span="6"><a-form-item label="费用"><a-input-number v-model="form.fee" :min="0" :step="0.01" style="width:100%" @change="onFeeChange" /></a-form-item></a-col>
        </a-row>
        <a-form-item :wrapper-col="{ span: 16, offset: 6 }">
          <a-space>
            <a-checkbox v-model="form.approvalRequired">需审批</a-checkbox>
            <a-button v-if="selectedClass && selectedClass.fee" @click="applyClassFee">按班级费用</a-button>
            <a-button type="primary" @click="apply">提交报名</a-button>
            <a-button @click="loadList">加载列表</a-button>
            <a-button type="dashed" @click="cleanupInvalid">清理空记录</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card bordered title="报名列表">
      <a-table :data-source="list" :columns="columns" rowKey="id" :pagination="false">
        <template v-slot:status="slotProps">
          <a-tag color="orange" v-if="slotProps.text==='pending'">待处理</a-tag>
          <a-tag color="green" v-else-if="slotProps.text==='approved'">已通过</a-tag>
          <a-tag color="red" v-else-if="slotProps.text==='rejected'">已拒绝</a-tag>
          <a-tag v-else>未知</a-tag>
        </template>
        <template v-slot:contract="slotProps">
          <span v-if="slotProps && slotProps.record && slotProps.record.contractUrl">
            <a :href="slotProps.record.contractUrl" target="_blank">查看合同</a>
          </span>
          <span v-else>未生成</span>
        </template>
        <template v-slot:action="slotProps">
          <a-space v-if="slotProps && slotProps.record && slotProps.record.id">
            <a-button size="small" @click="generateContract(slotProps.record)">生成合同</a-button>
            <a-button size="small" @click="signContract(slotProps.record)" :disabled="!(slotProps && slotProps.record && slotProps.record.contractUrl) || (slotProps && slotProps.record && slotProps.record.signed)">电子签名</a-button>
            <a-button size="small" type="primary" v-if="slotProps && slotProps.record && slotProps.record.approvalRequired" @click="approve(slotProps.record, true)">审批通过</a-button>
            <a-button size="small" type="danger" v-if="slotProps && slotProps.record && slotProps.record.approvalRequired" @click="approve(slotProps.record, false)">审批拒绝</a-button>
          </a-space>
          <span v-else>—</span>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'EnrollmentManage',
  data() {
    return {
      form: { studentId: null, classId: null, applyInfo: '', fee: 0, approvalRequired: false },
      list: [],
      classes: [],
      selectedClass: null,
      columns: [
        { title: 'ID', dataIndex: 'id', key: 'id' },
        { title: '学员ID', dataIndex: 'studentId', key: 'studentId' },
        { title: '班级ID', dataIndex: 'classId', key: 'classId' },
        { title: '费用', dataIndex: 'fee', key: 'fee' },
        { title: '审批', key: 'approval', customRender: (text, record) => record.approvalRequired ? '需审批' : '免审批' },
        { title: '状态', dataIndex: 'status', key: 'status', scopedSlots: { customRender: 'status' } },
        { title: '合同', key: 'contract', scopedSlots: { customRender: 'contract' } },
        { title: '操作', key: 'action', scopedSlots: { customRender: 'action' } }
      ]
    };
  },
  methods: {
    isValidId(n) { return Number.isFinite(Number(n)) && Number(n) > 0; },
    isValidMoney(v) { const n = Number(v); return Number.isFinite(n) && n >= 0; },
    validateForm() {
      if (!this.isValidId(this.form.studentId)) { this.$message && this.$message.error('请填写有效的学员ID'); return false; }
      if (!this.isValidId(this.form.classId)) { this.$message && this.$message.error('请填写或选择有效的班级'); return false; }
      if (!this.isValidMoney(this.form.fee)) { this.$message && this.$message.error('请填写有效的费用'); return false; }
      return true;
    },
    async loadClassOptions() {
      try {
        const res = await fetch('/api/class/list');
        const json = await res.json();
        this.classes = Array.isArray(json.data) ? json.data : [];
      } catch(e) { this.classes = []; }
    },
    onClassChange(val) {
      this.form.classId = val;
      this.selectedClass = this.classes.find(c=>c.id===val) || null;
      if (this.selectedClass && this.selectedClass.fee != null) {
        this.form.fee = Number(this.selectedClass.fee) || 0;
      }
      this.autoApproval();
    },
    applyClassFee() {
      if (this.selectedClass && this.selectedClass.fee != null) {
        this.form.fee = Number(this.selectedClass.fee) || 0;
        this.$message && this.$message.success('已按班级费用填充');
        this.autoApproval();
      }
    },
    onFeeChange(val) { this.form.fee = Number(val) || 0; this.autoApproval(); },
    autoApproval() {
      // 设定一个阈值示例：费用≥2000则默认需审批，可手动关闭
      try {
        const need = (Number(this.form.fee)||0) >= 2000;
        this.form.approvalRequired = need;
      } catch(_) {}
    },
    prefillFromQuery() {
      const q = (this.$route && this.$route.query) ? this.$route.query : {};
      if (q.studentId) { this.form.studentId = Number(q.studentId) || null; }
      // 如果从试听而来，填充报名说明与默认审批
      const auditionId = q.auditionId ? String(q.auditionId) : '';
      const recommend = q.recommend ? String(q.recommend) : '';
      if (auditionId || recommend) {
        const note = `来自试听${auditionId ? '#' + auditionId : ''}${recommend ? '，推荐：' + recommend : ''}`;
        this.form.applyInfo = note;
        this.form.approvalRequired = true;
        this.$message && this.$message.info('已根据试听结果预填报名信息');
      }
    },
    async apply() {
      if (!this.validateForm()) return;
      const res = await fetch('/api/enroll/apply', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(this.form) });
      const json = await res.json();
      if (json.code === 200) { this.$message && this.$message.success('报名提交成功'); this.loadList(); } else { this.$message && this.$message.error('失败：'+(json.msg||'未知错误')); }
    },
    async loadList() {
      const res = await fetch('/api/enroll/list');
      const json = await res.json();
      this.list = json.data || [];
    },
    async generateContract(e) {
      if (!e || !e.id) { this.$message && this.$message.warning('无法生成合同：记录不存在'); return; }
      const res = await fetch('/api/enroll/contract/generate', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ enrollId: e.id }) });
      const json = await res.json();
      if (json.code === 200) { this.$message && this.$message.success('合同已生成'); this.loadList(); } else { this.$message && this.$message.error('失败：'+(json.msg||'未知错误')); }
    },
    async signContract(e) {
      if (!e || !e.id) { this.$message && this.$message.warning('无法签署：记录不存在'); return; }
      const res = await fetch('/api/enroll/contract/sign', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ enrollId: e.id }) });
      const json = await res.json();
      if (json.code === 200) { this.$message && this.$message.success('电子签名完成'); this.loadList(); } else { this.$message && this.$message.error('失败：'+(json.msg||'未知错误')); }
    },
    async approve(e, pass) {
      if (!e || !e.id) { this.$message && this.$message.warning('无法审批：记录不存在'); return; }
      const res = await fetch('/api/enroll/approve', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ enrollId: e.id, action: pass ? 'approve' : 'reject' }) });
      const json = await res.json();
      if (json.code === 200) { this.$message && this.$message.success(pass ? '审批通过' : '审批拒绝'); this.loadList(); } else { this.$message && this.$message.error('失败：'+(json.msg||'未知错误')); }
    },
    async cleanupInvalid() {
      try {
        if (!Array.isArray(this.list) || this.list.length === 0) { await this.loadList(); }
        const invalids = (this.list||[]).filter(x => !this.isValidId(x.studentId) || !this.isValidId(x.classId) || !this.isValidMoney(x.fee));
        if (invalids.length === 0) { this.$message && this.$message.info('没有需要清理的空记录'); return; }
        let ok = 0, fail = 0;
        for (const it of invalids) {
          try {
            const res = await fetch('/api/enroll/delete', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ id: it.id }) });
            const j = await res.json();
            if (j && j.code === 200) ok++; else fail++;
          } catch(_) { fail++; }
        }
        if (ok > 0) {
          this.$message && this.$message.success(`清理完成：成功 ${ok} 条，失败 ${fail} 条`);
          await this.loadList();
        } else {
          const before = this.list.length;
          this.list = (this.list||[]).filter(x => this.isValidId(x.studentId) && this.isValidId(x.classId) && this.isValidMoney(x.fee));
          const removed = before - this.list.length;
          this.$message && this.$message.info(`后端未提供删除接口，已从列表隐藏 ${removed} 条无效记录`);
        }
      } catch(e) {
        this.$message && this.$message.error('清理失败：网络或服务异常');
      }
    }
  },
  mounted() { this.loadList(); this.prefillFromQuery(); this.loadClassOptions(); }
}
</script>

<style scoped>
.enroll-manage { }
</style>