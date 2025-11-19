<template>
  <div class="fee-rule-config">
    <a-page-header title="课时费规则配置" sub-title="基础与特殊规则" />
    
    <a-card bordered>
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 12 }">
        <a-form-item label="适用校区">
          <a-select v-model="form.campusId" :allowClear="true" :loading="campusLoading" style="width: 100%" placeholder="留空为全局">
            <a-select-option v-for="c in campusOptions" :key="c.value" :value="c.value">{{ c.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="教师等级">
          <a-select v-model="form.teacherLevel" placeholder="选择等级">
            <a-select-option value="初级">初级</a-select-option>
            <a-select-option value="中级">中级</a-select-option>
            <a-select-option value="高级">高级</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="基础课时费">
          <a-input-number v-model="form.baseFeePerLesson" :min="0" style="width: 100%" />
        </a-form-item>
        <a-divider>课程类型系数</a-divider>
        <a-form-item label="一对一系数">
          <a-input-number v-model="form.oneToOneFactor" :min="0" step="0.1" style="width: 100%" />
        </a-form-item>
        <a-form-item label="雅思系数">
          <a-input-number v-model="form.ieltsFactor" :min="0" step="0.1" style="width: 100%" />
        </a-form-item>
        <a-divider>班级规模</a-divider>
        <a-form-item label="人数阈值">
          <a-input-number v-model="form.largeClassThreshold" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="上浮系数">
          <a-input-number v-model="form.largeClassFactor" :min="0" step="0.1" style="width: 100%" />
        </a-form-item>
        <a-divider>特殊规则</a-divider>
        <a-form-item label="节假日系数">
          <a-input-number v-model="form.holidayFactor" :min="0" step="0.1" style="width: 100%" />
        </a-form-item>
        <a-form-item label="代课加价">
          <a-input-number v-model="form.substituteExtra" :min="0" style="width: 100%" />
        </a-form-item>
        <a-divider>阶梯奖励</a-divider>
        <a-form-item label="月课时阈值">
          <a-input-number v-model="form.tierMonthlyThreshold" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="超额系数">
          <a-input-number v-model="form.tierFactor" :min="0" step="0.1" style="width: 100%" />
        </a-form-item>
        <a-form-item :wrapper-col="{ span: 12, offset: 6 }">
          <a-space>
            <a-button type="primary" :loading="saving" @click="save">保存规则</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
    <a-divider orientation="left">各校区课时费规则</a-divider>
    <a-alert type="warning" show-icon message="提示" description="调整后的规则下月才生效" style="margin-bottom: 8px" />
    <div class="rule-table-wrapper">
      <a-table
        :columns="ruleColumns"
        :data-source="rules"
        :pagination="rulePagination"
        rowKey="id"
        size="middle"
        bordered
        :scroll="{ x: 'max-content' }"
        @change="onRuleTableChange"
      >
        <span slot="action" slot-scope="text,record">
          <a-button type="link" @click="openEdit(record)">编辑</a-button>
        </span>
      </a-table>
    </div>
    <a-modal :visible="editVisible" title="编辑课时费规则" @ok="saveEdit" @cancel="closeEdit" :confirmLoading="editSaving">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 14 }">
        <a-form-item label="适用校区">
          <a-select v-model="editForm.campusId" :allowClear="true" :loading="campusLoading" style="width: 100%" placeholder="留空为全局">
            <a-select-option v-for="c in campusOptions" :key="c.value" :value="c.value">{{ c.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="教师等级">
          <a-select v-model="editForm.teacherLevel" placeholder="选择等级">
            <a-select-option value="初级">初级</a-select-option>
            <a-select-option value="中级">中级</a-select-option>
            <a-select-option value="高级">高级</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="基础课时费"><a-input-number v-model="editForm.baseFeePerLesson" :min="0" style="width: 100%" /></a-form-item>
        <a-form-item label="一对一系数"><a-input-number v-model="editForm.oneToOneFactor" :min="0" step="0.1" style="width: 100%" /></a-form-item>
        <a-form-item label="雅思系数"><a-input-number v-model="editForm.ieltsFactor" :min="0" step="0.1" style="width: 100%" /></a-form-item>
        <a-form-item label="大班阈值"><a-input-number v-model="editForm.largeClassThreshold" :min="0" style="width: 100%" /></a-form-item>
        <a-form-item label="大班系数"><a-input-number v-model="editForm.largeClassFactor" :min="0" step="0.1" style="width: 100%" /></a-form-item>
        <a-form-item label="节假日系数"><a-input-number v-model="editForm.holidayFactor" :min="0" step="0.1" style="width: 100%" /></a-form-item>
        <a-form-item label="代课补贴"><a-input-number v-model="editForm.substituteExtra" :min="0" style="width: 100%" /></a-form-item>
        <a-form-item label="阶梯阈值"><a-input-number v-model="editForm.tierMonthlyThreshold" :min="0" style="width: 100%" /></a-form-item>
        <a-form-item label="阶梯系数"><a-input-number v-model="editForm.tierFactor" :min="0" step="0.1" style="width: 100%" /></a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
export default {
  name: 'FeeRuleConfig',
  data() {
    return {
      saving: false,
      campusLoading: false,
      campusOptions: [],
      rules: [],
      ruleColumns: [
        { title: '校区', dataIndex: 'campusName', ellipsis: true },
        { title: '等级', dataIndex: 'teacherLevel', ellipsis: true },
        { title: '基础课时费', dataIndex: 'baseFeePerLesson', align: 'right' },
        { title: '一对一系数', dataIndex: 'oneToOneFactor', align: 'right' },
        { title: '雅思系数', dataIndex: 'ieltsFactor', align: 'right' },
        { title: '大班阈值', dataIndex: 'largeClassThreshold', align: 'right' },
        { title: '大班系数', dataIndex: 'largeClassFactor', align: 'right' },
        { title: '节假日系数', dataIndex: 'holidayFactor', align: 'right' },
        { title: '代课补贴', dataIndex: 'substituteExtra', align: 'right' },
        { title: '阶梯阈值', dataIndex: 'tierMonthlyThreshold', align: 'right' },
        { title: '阶梯系数', dataIndex: 'tierFactor', align: 'right' },
        { title: '操作', key: 'action', scopedSlots: { customRender: 'action' } }
      ],
      rulePagination: { current: 1, pageSize: 10, total: 0, showSizeChanger: true, pageSizeOptions: ['10','20','50'] },
      form: {
        campusId: null,
        teacherLevel: '中级',
        baseFeePerLesson: 120,
        oneToOneFactor: 1.5,
        ieltsFactor: 1.2,
        largeClassThreshold: 20,
        largeClassFactor: 1.1,
        holidayFactor: 2.0,
        substituteExtra: 30,
        tierMonthlyThreshold: 80,
        tierFactor: 1.2
      },
      editVisible: false,
      editSaving: false,
      editForm: {
        id: null,
        campusId: null,
        teacherLevel: '中级',
        baseFeePerLesson: 0,
        oneToOneFactor: 1.5,
        ieltsFactor: 1.2,
        largeClassThreshold: 0,
        largeClassFactor: 1.1,
        holidayFactor: 1.0,
        substituteExtra: 0,
        tierMonthlyThreshold: 0,
        tierFactor: 1.0
      }
    }
  },
  mounted() {
    this.loadCampuses()
    this.loadRules(1, 10)
  },
  methods: {
    async save() {
      this.saving = true
      try {
        const res = await fetch('/api/teacher/fee/rule/save', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(this.form) })
        const json = await res.json()
        if (json && json.code === 200) {
          this.$message && this.$message.success('规则已保存')
          this.loadRules(1, this.rulePagination.pageSize)
        } else {
          throw new Error('保存失败')
        }
      } catch (e) {
        this.$message && this.$message.error('保存失败：网络或服务异常')
      } finally {
        this.saving = false
      }
    },
    reset() {
      this.form = { campusId: null, teacherLevel: '中级', baseFeePerLesson: 120, oneToOneFactor: 1.5, ieltsFactor: 1.2, largeClassThreshold: 20, largeClassFactor: 1.1, holidayFactor: 2.0, substituteExtra: 30, tierMonthlyThreshold: 80, tierFactor: 1.2 }
    },
    async loadCampuses() {
      this.campusLoading = true
      try {
        const r = await fetch('/api/base/campus/list')
        const j = await r.json().catch(() => ({}))
        const list = Array.isArray(j && j.data) ? j.data : []
        const enabled = list.filter(it => String(it.status || '').toLowerCase() === 'enabled')
        this.campusOptions = enabled.map(it => ({ value: it.id, label: it.name || ('#' + it.id) }))
      } catch (e) {
        this.campusOptions = []
      } finally {
        this.campusLoading = false
      }
    }
    ,async loadRules(page, size) {
      const params = new URLSearchParams()
      params.set('page', String(page||1))
      params.set('size', String(size||10))
      if (this.form.campusId) params.set('campusId', String(this.form.campusId))
      try {
        const r = await fetch('/api/teacher/fee/rule/list?' + params.toString())
        const j = await r.json().catch(() => ({}))
        const data = j && j.data ? j.data : { items: [], total: 0, page: 1, size: 10 }
        this.rules = Array.isArray(data.items) ? data.items : []
        this.rulePagination = Object.assign({}, this.rulePagination, { current: Number(data.page||1), pageSize: Number(data.size||10), total: Number(data.total||0) })
      } catch (e) {
        this.rules = []
        this.rulePagination = Object.assign({}, this.rulePagination, { total: 0 })
      }
    }
    ,onRuleTableChange(pagination) {
      const p = pagination || {}
      const current = Number(p.current || this.rulePagination.current || 1)
      const size = Number(p.pageSize || this.rulePagination.pageSize || 10)
      this.loadRules(current, size)
    }
    ,openEdit(record) {
      const r = record || {}
      this.editForm = {
        id: r.id,
        campusId: r.campusId || null,
        teacherLevel: r.teacherLevel || '中级',
        baseFeePerLesson: Number(r.baseFeePerLesson || 0),
        oneToOneFactor: Number(r.oneToOneFactor || 1.5),
        ieltsFactor: Number(r.ieltsFactor || 1.2),
        largeClassThreshold: Number(r.largeClassThreshold || 0),
        largeClassFactor: Number(r.largeClassFactor || 1.1),
        holidayFactor: Number(r.holidayFactor || 1.0),
        substituteExtra: Number(r.substituteExtra || 0),
        tierMonthlyThreshold: Number(r.tierMonthlyThreshold || 0),
        tierFactor: Number(r.tierFactor || 1.0)
      }
      this.editVisible = true
    }
    ,closeEdit() { this.editVisible = false }
    ,async saveEdit() {
      if (!this.editForm || !this.editForm.id) { this.$message && this.$message.error('无效记录'); return }
      this.editSaving = true
      try {
        const r = await fetch(`/api/teacher/fee/rule/update/${this.editForm.id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(this.editForm) })
        const j = await r.json(); if (j && j.code === 200) { this.$message && this.$message.success('已更新，次月生效'); this.editVisible = false; this.loadRules(this.rulePagination.current, this.rulePagination.pageSize) } else { throw new Error('更新失败') }
      } catch (e) { this.$message && this.$message.error('更新失败：网络或服务异常') } finally { this.editSaving = false }
    }
  }
}
</script>

<style scoped>
.fee-rule-config { }
.rule-table-wrapper { width: 100%; overflow-x: auto; }
</style>