<template>
  <div class="student-profile">
    <a-page-header title="学员档案管理" sub-title="基础信息、监护人、标签与历史记录" />

    <!-- 学员档案弹窗：新增/编辑 -->
    <a-modal :visible="studentModal.visible" :title="studentModal.mode==='edit' ? '编辑学员' : '新增学员'" @ok="submitStudent" @cancel="closeStudentModal" :width="900">
      <!-- 档案表单（弹窗内） -->
      <a-card bordered style="margin-bottom: 12px">
        <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item label="姓名">
                <a-input v-model="form.name" placeholder="请输入姓名" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="性别">
                <a-select v-model="form.gender" placeholder="选择性别">
                  <a-select-option value="male">男</a-select-option>
                  <a-select-option value="female">女</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="出生日期">
                <V2SimpleDate v-model="form.birthDate" :type="'date'" format="YYYY-MM-DD" placeholder="选择出生日期" style="width:100%" />
              </a-form-item>
            </a-col>
          </a-row>
          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item label="证件类型">
                <a-select v-model="form.idType" placeholder="选择证件类型" allowClear>
                  <a-select-option value="身份证">身份证</a-select-option>
                  <a-select-option value="护照">护照</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="证件号码">
                <a-input v-model="form.idNumber" placeholder="请输入证件号码" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="照片URL">
                <a-input v-model="form.photoUrl" placeholder="可输入图片链接" />
              </a-form-item>
            </a-col>
          </a-row>
          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item label="校区">
                <a-select v-model="form.campusId" placeholder="选择校区" allowClear>
                  <a-select-option v-for="c in campusList" :key="c.id" :value="c.id">{{ c.title || c.name || ('#'+c.id) }}</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </a-card>

      <!-- 监护人与标签（弹窗内） -->
      <a-row :gutter="16" style="margin-bottom: 12px">
        <a-col :span="12">
          <a-card title="监护人信息" bordered>
            <a-space direction="vertical" style="width:100%">
              <div v-for="(g, idx) in guardians" :key="idx">
                <a-row :gutter="8" align="middle">
                  <a-col :span="6"><a-input v-model="g.name" placeholder="姓名" /></a-col>
                  <a-col :span="6"><a-input v-model="g.relation" placeholder="关系" /></a-col>
                  <a-col :span="6"><a-input v-model="g.phone" placeholder="联系电话" /></a-col>
                  <a-col :span="4"><a-checkbox v-model="g.emergency">紧急联系人</a-checkbox></a-col>
                  <a-col :span="2"><a-button size="small" type="danger" @click="removeGuardian(idx)">删除</a-button></a-col>
                </a-row>
              </div>
              <a-button type="dashed" icon="plus" @click="addGuardian">添加监护人</a-button>
            </a-space>
          </a-card>
        </a-col>
        <a-col :span="12">
          <a-card title="标签管理" bordered>
            <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
              <a-form-item label="来源渠道">
                <a-select v-model="originChannels" mode="tags" style="width:100%" :token-separators="[',','，',';','；',' ']" :options="channelOptions" placeholder="输入或选择：地推/转介绍/线上" />
              </a-form-item>
              <a-form-item label="兴趣特长">
                <a-select v-model="interests" mode="tags" style="width:100%" :token-separators="[',','，',';','；',' ']" placeholder="如：美术,钢琴,篮球" />
              </a-form-item>
              <a-form-item label="学情标签">
                <a-select v-model="studyTags" mode="tags" style="width:100%" :token-separators="[',','，',';','；',' ']" placeholder="如：基础一般,注意力需提升" />
              </a-form-item>
            </a-form>
          </a-card>
        </a-col>
      </a-row>
    </a-modal>

    <!-- 操作按钮 -->
    <a-card bordered style="margin-bottom: 12px">
      <a-space>
        <a-button type="primary" @click="openAddStudent">新增学员</a-button>
        <a-button @click="exportStudents">导出</a-button>
        <a-button @click="openImportModal">导入</a-button>
        <a-button @click="generateDemo">生成50条数据</a-button>
      </a-space>
    </a-card>

    <!-- 查询条件 -->
    <a-card bordered style="margin-bottom: 12px">
      <a-form :layout="'inline'">
        <a-form-item label="姓名">
          <a-input v-model="filters.name" placeholder="学员姓名" allowClear />
        </a-form-item>
        <a-form-item label="手机">
          <a-input v-model="filters.phone" placeholder="手机号码" allowClear />
        </a-form-item>
        <a-form-item label="来源渠道">
          <a-select v-model="filters.channel" :options="channelOptions" allowClear style="width:160px" placeholder="选择渠道" />
        </a-form-item>
        <a-form-item label="校区">
          <a-select v-model="filters.campusId" allowClear style="width:160px" placeholder="选择校区">
            <a-select-option v-for="c in campusList" :key="c.id" :value="c.id">{{ c.title || c.name || ('#'+c.id) }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="searchStudents">查询</a-button>
            <a-button @click="resetFilters">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 学员列表 -->
    <a-card title="学员列表" bordered>
      <a-table :data-source="list" :columns="columns" rowKey="id" :pagination="pagination" @change="onTableChange">
         <template slot="campus" slot-scope="text, record, index">
          <span>{{ (record && record.campusName) || '-' }}</span>
        </template>
        <template slot="channels" slot-scope="text, record, index">
          <span>{{ ((record && record.originChannels) || []).join('、') }}</span>
        </template>
        <template slot="action" slot-scope="text, record, index">
          <a-space>
            <a-button size="small" @click="edit(record)">编辑</a-button>
            <a-button size="small" @click="openHistory(record)">历史记录</a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <a-modal :visible="importModal.visible" title="导入学员数据" @ok="submitImport" @cancel="closeImportModal" :width="720">
      <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
        <a-form-item label="JSON数组">
          <a-input type="textarea" v-model="importText" :rows="10" placeholder='[{"name":"张三","gender":"male","birthDate":"2012-06-01","idType":"身份证","idNumber":"ID000001","photoUrl":"","campusId":1,"guardians":[{"name":"父亲","relation":"父亲","phone":"13800000000","emergency":true}],"originChannels":["地推"],"interests":["篮球"],"studyTags":["积极主动"]}]' />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 历史记录弹窗 -->
    <a-modal :visible="historyVisible" title="历史记录" @ok="closeHistory" @cancel="closeHistory" :width="720">
      <div style="margin-bottom:12px">当前学员ID：{{ currentStudentId }}</div>
      <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="类型">
          <a-select v-model="historyForm.type" placeholder="报班/缴费/请假/调班" allowClear>
            <a-select-option value="报班">报班</a-select-option>
            <a-select-option value="缴费">缴费</a-select-option>
            <a-select-option value="请假">请假</a-select-option>
            <a-select-option value="调班">调班</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="详情">
          <a-input v-model="historyForm.detail" placeholder="请输入详情" />
        </a-form-item>
        <a-form-item label="时间">
          <V2SimpleDate
            v-model="historyForm.time"
            :showTime="true"
            format="YYYY-MM-DD HH:mm:ss"
            style="width:100%"
            @change="onHistoryTimeChange"
          />
        </a-form-item>
        <a-form-item :wrapper-col="{ span: 18, offset: 5 }">
          <a-space>
            <a-button type="primary" @click="addHistory">添加历史记录</a-button>
            <a-button @click="loadHistory">刷新历史列表</a-button>
          </a-space>
        </a-form-item>
      </a-form>
      <a-list :data-source="historyList" bordered :renderItem="renderHistoryItem" />
    </a-modal>

    

  </div>
</template>

<script>
export default {
  name: 'StudentProfile',
  components: { V2SimpleDate: () => import('../../components/V2SimpleDate.vue') },
  data() {
    return {
      form: { id: null, name: '', gender: 'male', birthDate: '', idType: '', idNumber: '', photoUrl: '', campusId: null },
      guardians: [],
      originChannels: [],
      interests: [],
      studyTags: [],
      list: [],
      pagination: { current: 1, pageSize: 10, total: 0, showSizeChanger: true, pageSizeOptions: ['10','20','50'], showTotal: total => `共 ${total} 条` },
      filters: { name: '', phone: '', channel: '', campusId: null },
      columns: [
        { title: 'ID', dataIndex: 'id', key: 'id' },
        { title: '姓名', dataIndex: 'name', key: 'name' },
        { title: '性别', dataIndex: 'gender', key: 'gender' },
        { title: '出生日期', dataIndex: 'birthDate', key: 'birthDate' },
        { title: '校区', key: 'campus', scopedSlots: { customRender: 'campus' } },
        { title: '渠道', key: 'channels', scopedSlots: { customRender: 'channels' } },
        { title: '操作', key: 'action', scopedSlots: { customRender: 'action' } }
      ],
      historyVisible: false,
      currentStudentId: null,
      historyForm: { type: '', detail: '', time: '' },
      historyList: [],
      campusList: [],
      campusMap: {},
      channelOptions: [
        { value: '地推', label: '地推' },
        { value: '转介绍', label: '转介绍' },
        { value: '线上', label: '线上' }
      ],
      studentModal: { visible: false, mode: 'add' },
      importModal: { visible: false },
      importText: ''
    }
  },
  methods: {
    onBirthDateChange(dateString) { this.form.birthDate = dateString || '' },
    onHistoryTimeChange(dateString) { this.historyForm.time = dateString || '' },
    addGuardian() { this.guardians.push({ name: '', relation: '', phone: '', emergency: false }) },
    removeGuardian(idx) { this.guardians.splice(idx, 1) },
    async loadCampusList() {
      try {
        const res = await fetch('/api/base/campus/list')
        const json = await res.json()
        this.campusList = json.data || []
        const map = {}
        this.campusList.forEach(c => { map[c.id] = c.title || c.name || ('#'+c.id) })
        this.campusMap = map
      } catch (e) {
        this.$message && this.$message.error('加载校区失败')
      }
    },
    openAddStudent(){ this.resetForm(); this.studentModal.visible = true; this.studentModal.mode = 'add' },
    closeStudentModal(){ this.studentModal.visible = false },
    async submitStudent(){
      const payload = {
        ...this.form,
        guardians: this.guardians,
        originChannels: this.originChannels,
        interests: this.interests,
        studyTags: this.studyTags
      }
      const isEdit = this.studentModal && this.studentModal.mode === 'edit'
      const url = isEdit ? '/api/student/update' : '/api/student/save'
      try {
        const res = await fetch(url, { method: 'POST', headers: { 'Content-Type':'application/json' }, body: JSON.stringify(payload) })
        const json = await res.json()
        if (json.code === 200) {
          this.$message && this.$message.success(isEdit ? '更新成功' : '新增成功')
          this.closeStudentModal()
          await this.loadList()
          this.resetForm()
        } else {
          this.$message && this.$message.error((isEdit ? '更新失败：' : '新增失败：') + (json.msg || '未知错误'))
        }
      } catch (e) {
        this.$message && this.$message.error((isEdit ? '更新异常：' : '新增异常：') + (e && e.message ? e.message : '网络异常'))
      }
    },
    resetForm(){
      this.form = { id: null, name: '', gender: 'male', birthDate: '', idType: '', idNumber: '', photoUrl: '', campusId: null }
      this.guardians = []
      this.originChannels = []
      this.interests = []
      this.studyTags = []
    },
    searchStudents(){ this.pagination.current = 1; this.loadList() },
    resetFilters(){ this.filters = { name: '', phone: '', channel: '', campusId: null }; this.pagination.current = 1; this.loadList() },
    async loadList() {
      const { current, pageSize } = this.pagination
      const res = await fetch(`/api/student/list?page=${current}&size=${pageSize}`)
      const json = await res.json()
      const data = json.data
      if (Array.isArray(data)) {
        const enriched = data.map(x => {
          const id = x.id != null ? x.id : x.studentId
          const campusId = x.campusId != null ? x.campusId : (x.campus != null ? x.campus : null)
          const campusName = x.campusName || x.campusTitle || (campusId != null && this.campusMap ? this.campusMap[campusId] : undefined)
          const originChannels = Array.isArray(x.originChannels) ? x.originChannels : (Array.isArray(x.channels) ? x.channels : [])
          return { ...x, id, campusId, campusName, originChannels }
        })
        const f = this.filters || {}
        const name = (f.name || '').trim()
        const phone = (f.phone || '').trim()
        const channel = (f.channel || '').trim()
        const campusIdFilter = f.campusId
        const filtered = enriched.filter(s => {
          if (!s || s.id === undefined || s.id === null) return false
          let ok = true
          if (name) ok = ok && String(s.name || '').includes(name)
          if (campusIdFilter !== null && campusIdFilter !== undefined && campusIdFilter !== '') ok = ok && String(s.campusId || '') === String(campusIdFilter)
          if (channel) {
            const chs = Array.isArray(s.originChannels) ? s.originChannels : []
            ok = ok && chs.some(ch => String(ch || '').includes(channel))
          }
          if (phone) {
            const gs = Array.isArray(s.guardians) ? s.guardians : []
            const match = gs.some(g => String(g && g.phone ? g.phone : '').includes(phone))
            ok = ok && match
          }
          return ok
        })
        this.list = filtered
        this.pagination.total = enriched.length
      } else if (data && Array.isArray(data.items)) {
        const enriched = data.items.map(x => {
          const id = x.id != null ? x.id : x.studentId
          const campusId = x.campusId != null ? x.campusId : (x.campus != null ? x.campus : null)
          const campusName = x.campusName || x.campusTitle || (campusId != null && this.campusMap ? this.campusMap[campusId] : undefined)
          const originChannels = Array.isArray(x.originChannels) ? x.originChannels : (Array.isArray(x.channels) ? x.channels : [])
          return { ...x, id, campusId, campusName, originChannels }
        })
        // 对于后端分页数据，直接展示当前页数据，并使用后端 total
        this.list = enriched
        this.pagination.total = data.total || enriched.length
      } else {
        this.list = []
        this.pagination.total = 0
      }
    },
    onTableChange(pagination) {
      this.pagination.current = pagination.current
      this.pagination.pageSize = pagination.pageSize
      this.loadList()
    },
    async generateDemo() {
      const res = await fetch('/api/student/import/demo', { method:'POST' })
      const json = await res.json()
      if (json.code === 200) {
        this.$message && this.$message.success(`已生成 ${json.data.count} 条示例数据`)
        this.pagination.current = 1
        await this.loadList()
      } else {
        this.$message && this.$message.error(`生成失败：${json.msg || '未知错误'}`)
      }
    },
    openImportModal(){ this.importModal.visible = true },
    closeImportModal(){ this.importModal.visible = false },
    async submitImport(){
      let payload = null
      try {
        const arr = JSON.parse(this.importText || '[]')
        if (!Array.isArray(arr) || arr.length === 0) {
          this.$message && this.$message.warning('请输入有效的JSON数组')
          return
        }
        payload = arr.map(x => ({
          id: x.id || null,
          name: x.name || '',
          gender: x.gender || 'male',
          birthDate: x.birthDate || '',
          idType: x.idType || '',
          idNumber: x.idNumber || '',
          photoUrl: x.photoUrl || '',
          campusId: x.campusId == null ? null : x.campusId,
          guardians: Array.isArray(x.guardians) ? x.guardians : [],
          originChannels: Array.isArray(x.originChannels) ? x.originChannels : [],
          interests: Array.isArray(x.interests) ? x.interests : [],
          studyTags: Array.isArray(x.studyTags) ? x.studyTags : []
        }))
      } catch (e) {
        this.$message && this.$message.error('JSON解析失败')
        return
      }
      try {
        const res = await fetch('/api/student/import', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(payload) })
        const json = await res.json()
        if (json.code === 200) {
          this.$message && this.$message.success(`导入成功，共 ${json.data.count} 条`)
          this.importText = ''
          this.closeImportModal()
          this.pagination.current = 1
          await this.loadList()
        } else {
          this.$message && this.$message.error(`导入失败：${json.msg || '未知错误'}`)
        }
      } catch (e) {
        this.$message && this.$message.error('导入请求异常')
      }
    },
    getRecordById(id) {
      if (!id && id !== 0) return null
      const arr = Array.isArray(this.list) ? this.list : []
      const sid = String(id)
      return arr.find(x => x && String(x.id) === sid) || null
    },
    editAt(index) {
      const arr = Array.isArray(this.list) ? this.list : []
      const s = (typeof index === 'number' && index >= 0) ? arr[index] : null
      if (!s) { this.$message && this.$message.warning('未找到该行数据'); return }
      this.edit(s)
    },
    editById(id) {
      const s = this.getRecordById(id)
      if (!s) { this.$message && this.$message.warning('未找到该行数据'); return }
      this.edit(s)
    },
    edit(s) {
      console.log(s)
      if (!s) { this.$message && this.$message.warning('行数据缺失，无法编辑'); return }
      // 尝试根据 id/studentId 获取列表中的最新数据，防止索引或快照不一致
      const sid = (s.id !== undefined && s.id !== null) ? s.id : s.studentId
      const latest = (sid !== undefined && sid !== null) ? (this.getRecordById(sid) || s) : s
      // 使用逐字段赋值，保持表单双向绑定的响应性
      this.form.id = latest.id != null ? latest.id : sid
      this.form.name = latest.name
      this.form.gender = latest.gender
      this.form.birthDate = latest.birthDate
      this.form.idType = latest.idType
      this.form.idNumber = latest.idNumber
      this.form.photoUrl = latest.photoUrl
      this.form.campusId = latest.campusId || null
      this.guardians = Array.isArray(latest.guardians) ? latest.guardians.map(x => ({ ...x })) : []
      this.originChannels = Array.isArray(latest.originChannels) ? latest.originChannels.slice() : (Array.isArray(latest.channels) ? latest.channels.slice() : [])
      this.interests = Array.isArray(latest.interests) ? latest.interests.slice() : []
      this.studyTags = Array.isArray(latest.studyTags) ? latest.studyTags.slice() : []
      // 打开编辑弹窗
      this.studentModal.visible = true
      this.studentModal.mode = 'edit'
    },
    openHistoryById(id) {
      const s = this.getRecordById(id)
      if (!s) { this.$message && this.$message.warning('未找到该行数据'); return }
      this.openHistory(s)
    },
    openHistoryAt(index) {
      const arr = Array.isArray(this.list) ? this.list : []
      const s = (typeof index === 'number' && index >= 0) ? arr[index] : null
      if (!s) { this.$message && this.$message.warning('未找到该行数据'); return }
      this.openHistory(s)
    },
    async openHistory(s) {
      if (!s) { this.$message && this.$message.warning('行数据缺失，无法打开历史'); return }
      const sid = (s.id !== undefined && s.id !== null) ? s.id : s.studentId
      if (sid === undefined || sid === null || sid === '') { this.$message && this.$message.warning('缺少学员ID'); return }
      this.historyVisible = true
      this.currentStudentId = sid
      await this.loadHistory()
    },
    async loadHistory() {
      const res = await fetch(`/api/student/history/list?studentId=${this.currentStudentId}`)
      const json = await res.json()
      this.historyList = json.data || []
    },
    async addHistory() {
      const payload = { ...this.historyForm, studentId: this.currentStudentId }
      const res = await fetch('/api/student/history/add', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(payload) })
      const json = await res.json()
      if (json.code === 200) {
        await this.loadHistory(); this.$message && this.$message.success('已添加历史记录')
      } else {
        this.$message && this.$message.error('添加失败：' + (json.msg || '未知错误'))
      }
    },
    async exportStudents() {
      const res = await fetch('/api/student/export')
      const json = await res.json()
      this.$message && this.$message.success(`导出成功，共 ${json.data.length} 条`)
    },
    renderHistoryItem(item) {
      return this.$createElement('a-list-item', [
        this.$createElement('span', `${item.time} [${item.type}] - ${item.detail}`)
      ])
    },
    closeHistory() { this.historyVisible = false }
  },
  async mounted() {
    await this.loadCampusList()
    await this.loadList()
  }
}
</script>

<style scoped>
.student-profile { }
</style>