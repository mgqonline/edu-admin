<template>
  <div class="referral-manage">
    <a-page-header title="转介绍管理" sub-title="记录关系与奖励规则" />

    <a-card bordered style="margin-bottom: 12px" title="建立转介绍关系">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-row :gutter="16">
          <a-col :span="6"><a-form-item label="推荐人姓名"><a-input v-model="form.referrerName" placeholder="输入姓名" /></a-form-item></a-col>
          <a-col :span="8">
            <a-form-item label="新学员">
              <a-select v-model="form.newStudentId" showSearch :filterOption="false" @search="onStudentSearch" style="width:100%" allowClear placeholder="选择学员">
                <a-select-option v-for="s in studentOptions" :key="s.id" :value="s.id">{{ s.name }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="奖励类型">
              <a-select v-model="reward.type" style="width:100%" allowClear>
                <a-select-option value="coupon">代金券</a-select-option>
                <a-select-option value="cash">现金</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="6"><a-form-item label="奖励金额"><a-input-number v-model="reward.amount" :min="0" :step="0.01" style="width:100%" /></a-form-item></a-col>
        </a-row>
        <a-form-item :wrapper-col="{ span: 16, offset: 6 }">
          <a-space>
            <a-button type="primary" :loading="submitting" :disabled="!canSubmit" @click="relate">建立关系</a-button>
            <a-button :loading="loadingList" @click="loadList">加载列表</a-button>
            <a-button @click="resetFilter">重置筛选</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card bordered title="转介绍记录">
      <a-table :data-source="list" :columns="columns" rowKey="id" :pagination="false" :loading="loadingList">
        <template v-slot:rewardRule="slotProps">
          {{ formatReward(slotProps && slotProps.record && slotProps.record.rewardRule) }}
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'ReferralManage',
  data() {
    return {
      form: { referrerName: '', newStudentId: null },
      reward: { type: 'coupon', amount: 100 },
      list: [],
      loadingList: false,
      loadingStudents: false,
      submitting: false,
      studentOptions: [],
      columns: [
        { title: 'ID', dataIndex: 'id', key: 'id' },
        { title: '推荐人ID', dataIndex: 'referrerId', key: 'referrerId' },
        { title: '新学员ID', dataIndex: 'newStudentId', key: 'newStudentId' },
        { title: '奖励规则', key: 'rewardRule', scopedSlots: { customRender: 'rewardRule' } },
        { title: '时间', dataIndex: 'time', key: 'time' }
      ]
    }
  },
  computed: {
    canSubmit() {
      return !!(this.form.referrerName && this.form.referrerName.trim()) && !!this.form.newStudentId && !this.submitting
    }
  },
  methods: {
    formatReward(obj) {
      if (!obj) return '-'
      const typeMap = { coupon: '代金券', cash: '现金' }
      const t = typeMap[obj.type] || obj.type
      const a = obj.amount != null ? Number(obj.amount) : '-'
      return `${t} ${a}`
    },
    async relate() {
      if (!(this.form.referrerName && this.form.referrerName.trim()) || !this.form.newStudentId) {
        this.$message && this.$message.error('请填写推荐人姓名与选择新学员');
        return
      }
      const rid = await this.resolveReferrerIdByName(this.form.referrerName)
      if (!rid) { this.$message && this.$message.error('未找到匹配的推荐人'); return }
      const payload = { referrerId: Number(rid), newStudentId: Number(this.form.newStudentId), rewardRule: { type: this.reward.type, amount: Number(this.reward.amount) } }
      this.submitting = true
      try {
        const res = await fetch('/api/student/referral/relate', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
        const json = await res.json()
        if (json.code === 200) {
          const created = json && json.data
          const id = created && created.id
          this.$message && this.$message.success(`关联成功，记录ID：${id}`)
          const match = Number(rid) === Number(created.referrerId) && (!this.form.newStudentId || Number(this.form.newStudentId) === Number(created.newStudentId))
          if (match) {
            this.list = [created, ...(this.list || [])]
          } else {
            this.loadList()
          }
        } else if (json.code === 409) {
          this.$message && this.$message.warning(json.msg || '该转介绍关系已存在')
        } else {
          this.$message && this.$message.error('失败：' + (json.msg || '未知错误'))
        }
      } catch (e) {
        this.$message && this.$message.error('网络错误或服务器不可用')
      } finally {
        this.submitting = false
      }
    },
    async loadList() {
      this.loadingList = true
      try {
        const params = new URLSearchParams()
        if (this.form.referrerName && this.form.referrerName.trim()) {
          const rid = await this.resolveReferrerIdByName(this.form.referrerName)
          if (rid) params.append('referrerId', rid)
        }
        if (this.form.newStudentId) params.append('newStudentId', this.form.newStudentId)
        const res = await fetch(`/api/student/referral/list?${params.toString()}`)
        const json = await res.json()
        this.list = json.data || []
        if (this.$message) {
          const c = this.list.length
          this.$message.success(`已加载 ${c} 条记录`)
        }
      } catch (e) {
        this.$message && this.$message.error('加载失败，请稍后重试')
      } finally {
        this.loadingList = false
      }
    },
    resetFilter() {
      this.form.referrerName = ''
      this.form.newStudentId = null
      this.loadList()
    },
    async resolveReferrerIdByName(name) {
      try {
        const url = `/api/student/search?name=${encodeURIComponent(name)}&limit=20`
        const res = await fetch(url)
        const json = await res.json()
        const arr = Array.isArray(json.data) ? json.data : []
        const exact = arr.find(x => String(x.name).trim() === String(name).trim())
        const pick = exact || arr[0]
        return pick && pick.id ? Number(pick.id) : null
      } catch (e) { return null }
    },
    async onStudentSearch(val) {
      try {
        this.loadingStudents = true
        const url = `/api/student/search?name=${encodeURIComponent(val || '')}&limit=20`
        const res = await fetch(url)
        const json = await res.json()
        const arr = Array.isArray(json.data) ? json.data : []
        this.studentOptions = arr.map(it => ({ id: it.id, name: it.name }))
      } catch (e) {
        this.studentOptions = []
      } finally {
        this.loadingStudents = false
      }
    }
  }
}
</script>

<style scoped>
.referral-manage { }
</style>