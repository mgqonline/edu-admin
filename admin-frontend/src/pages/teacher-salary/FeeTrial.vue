<template>
  <div class="fee-trial">
    <a-page-header title="课时费试算" sub-title="支持优先级与特殊规则" />
    
    <a-card bordered>
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 12 }">
        <a-form-item label="校区选择">
          <a-select v-model="req.campusId" :allowClear="true" :loading="campusLoading" style="width: 100%" placeholder="先选择校区">
            <a-select-option v-for="c in campusOptions" :key="c.value" :value="c.value">{{ c.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="教师">
          <a-select v-model="req.teacherId" :disabled="!req.campusId" :loading="teacherLoading" style="width: 100%" placeholder="请选择教师">
            <a-select-option v-for="t in teacherOptions" :key="t.value" :value="t.value">{{ t.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="月份"><V2SimpleDate v-model="req.month" :type="'month'" format="YYYY-MM" placeholder="选择月份" /></a-form-item>
        <a-form-item label="教师等级">
          <a-select v-model="req.teacherLevel" placeholder="选择等级">
            <a-select-option value="初级">初级</a-select-option>
            <a-select-option value="中级">中级</a-select-option>
            <a-select-option value="高级">高级</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="月总课时"><a-input-number v-model="req.monthlyTotalLessons" :min="0" style="width: 100%" /></a-form-item>
      </a-form>

      <a-divider>课时列表</a-divider>
      <a-space style="margin-bottom: 8px"><a-button type="dashed" @click="addLesson">新增课时</a-button><a-button @click="fillDemo">填充示例</a-button></a-space>
      <a-table :data-source="req.lessons" :columns="columns" rowKey="id" :pagination="false">
        <span slot="courseType" slot-scope="text,record">
          <a-select v-model="record.courseType" style="width: 120px">
            <a-select-option value="班课">班课</a-select-option>
            <a-select-option value="一对一">一对一</a-select-option>
          </a-select>
        </span>
        <span slot="courseCategory" slot-scope="text,record">
          <a-select v-model="record.courseCategory" style="width: 140px">
            <a-select-option value="常规英语">常规英语</a-select-option>
            <a-select-option value="雅思">雅思</a-select-option>
          </a-select>
        </span>
        <span slot="classSize" slot-scope="text,record">
          <a-input-number v-model="record.classSize" :min="0" style="width: 100px" />
        </span>
        <span slot="isHoliday" slot-scope="text,record">
          <a-switch v-model="record.isHoliday" />
        </span>
        <span slot="isSubstitute" slot-scope="text,record">
          <a-switch v-model="record.isSubstitute" />
        </span>
        <span slot="adjustedFee" slot-scope="text,record">
          <a-input-number v-model="record.adjustedFee" :min="0" style="width: 120px" placeholder="手动调整优先" />
        </span>
        <span slot="action" slot-scope="text,record">
          <a-button type="link" @click="removeLesson(record.id)">删除</a-button>
        </span>
      </a-table>

      <div style="margin-top: 12px">
        <a-space>
          <a-button type="primary" :loading="calculating" @click="calculate">试算</a-button>
          <a-button @click="reset">重置</a-button>
        </a-space>
      </div>
    </a-card>

    <a-card bordered style="margin-top: 12px" v-if="result">
      <template slot="title">试算结果</template>
      <a-descriptions bordered :column="2">
        <a-descriptions-item label="教师ID">{{ result.teacherId }}</a-descriptions-item>
        <a-descriptions-item label="月份">{{ result.month }}</a-descriptions-item>
        <a-descriptions-item label="课时数">{{ result.totalLessons }}</a-descriptions-item>
        <a-descriptions-item label="基础金额">{{ result.baseAmount }}</a-descriptions-item>
        <a-descriptions-item label="节假日额外">{{ result.holidayExtra }}</a-descriptions-item>
        <a-descriptions-item label="代课加价">{{ result.substituteExtra }}</a-descriptions-item>
        <a-descriptions-item label="阶梯奖励">{{ result.tierBonus }}</a-descriptions-item>
        <a-descriptions-item label="最终金额">{{ result.finalAmount }}</a-descriptions-item>
      </a-descriptions>
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'FeeTrial',
  components: { V2SimpleDate: () => import('../../components/V2SimpleDate.vue') },
  data() {
    return {
      calculating: false,
      result: null,
      campusLoading: false,
      campusOptions: [],
      teacherLoading: false,
      teacherOptions: [],
      columns: [
        { title: '课程类型', dataIndex: 'courseType', key: 'courseType', scopedSlots: { customRender: 'courseType' } },
        { title: '课程类别', dataIndex: 'courseCategory', key: 'courseCategory', scopedSlots: { customRender: 'courseCategory' } },
        { title: '班级人数', dataIndex: 'classSize', key: 'classSize', scopedSlots: { customRender: 'classSize' } },
        { title: '节假日', dataIndex: 'isHoliday', key: 'isHoliday', scopedSlots: { customRender: 'isHoliday' } },
        { title: '代课', dataIndex: 'isSubstitute', key: 'isSubstitute', scopedSlots: { customRender: 'isSubstitute' } },
        { title: '手动调整', dataIndex: 'adjustedFee', key: 'adjustedFee', scopedSlots: { customRender: 'adjustedFee' } },
        { title: '操作', key: 'action', scopedSlots: { customRender: 'action' } }
      ],
      req: { teacherId: null, month: '', teacherLevel: '中级', campusId: null, monthlyTotalLessons: 0, lessons: [] },
      idSeq: 1
    }
  },
  mounted() {
    this.loadCampuses()
    this.$watch(() => this.req.campusId, (nv) => { this.onCampusChange(nv) })
  },
  methods: {
    addLesson() {
      const id = 'l' + (this.idSeq++)
      this.req.lessons.push({ id, courseType: '班课', courseCategory: '常规英语', classSize: 15, isHoliday: false, isSubstitute: false, adjustedFee: null })
    },
    removeLesson(id) { this.req.lessons = (this.req.lessons||[]).filter(x => x.id !== id) },
    reset() { this.req = { teacherId: null, month: '', teacherLevel: '中级', campusId: null, monthlyTotalLessons: 0, lessons: [] }; this.result = null },
    fillDemo() {
      this.req.month = '2025-11'
      this.req.lessons = [
        { id: 'l1', courseType: '班课', courseCategory: '常规英语', classSize: 22, isHoliday: false, isSubstitute: true, adjustedFee: null },
        { id: 'l2', courseType: '一对一', courseCategory: '常规英语', classSize: 1, isHoliday: true, isSubstitute: false, adjustedFee: null },
        { id: 'l3', courseType: '班课', courseCategory: '雅思', classSize: 18, isHoliday: false, isSubstitute: false, adjustedFee: 200 }
      ]
      this.req.monthlyTotalLessons = 86
    },
    async calculate() {
      if (!this.req.teacherId) { this.$message && this.$message.warning('请选择教师'); return }
      if (!this.req.month) { this.$message && this.$message.warning('请选择月份'); return }
      this.calculating = true
      try {
        const res = await fetch('/api/teacher/fee/calculate', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(this.req) })
        if (!res.ok) { throw new Error('服务异常') }
        const json = await res.json(); if (json && json.code === 200) { this.result = json.data } else { throw new Error(json && json.msg || '试算失败') }
      } catch(e) { this.$message && this.$message.error('试算失败：' + (e.message || '网络或服务异常')) } finally { this.calculating = false }
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
    ,async loadTeachers() {
      if (!this.req.campusId) { this.teacherOptions = []; return }
      this.teacherLoading = true
      try {
        const r = await fetch(`/api/teacher/list?campusId=${this.req.campusId}&status=enabled&page=1&size=1000`)
        const j = await r.json().catch(() => ({}))
        const items = j && j.data && Array.isArray(j.data.items) ? j.data.items : []
        this.teacherOptions = items.map(it => ({ value: it.id, label: `${it.name}${it.level?('（'+it.level+'）'):''}` }))
      } catch (e) {
        this.teacherOptions = []
      } finally {
        this.teacherLoading = false
      }
    }
    ,onCampusChange(nv) {
      this.req.teacherId = null
      this.loadTeachers()
    }
  }
}
</script>

<style scoped>
.fee-trial { }
</style>