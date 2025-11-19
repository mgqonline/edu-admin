const req = require('../../../utils/request')

Page({
  data: { role: 'student', activeTab: 'consult', classes: [], classIndex: 0, classId: '', title: '', content: '', list: [], submitting: false, studentList: [], noticeCount: 0, parentMessages: [], parentVisible: [], consultFilter: 'all', replyMap: {}, unreadMsgCount: 0, replyImageMap: {}, surveyQuestion: '', surveyOptions: '', surveys: [], votingTopic: '', votingOptions: '', votes: [], directStudentId: '', directContent: '', directs: [] },
  onLoad() {
    const app = getApp();
    const role = wx.getStorageSync('ROLE') || (app.globalData && app.globalData.role) || 'student';
    this.setData({ role })
    if (role === 'teacher') { this.fetchClasses(); this.fetchList(); this.fetchParentMessages(); this.fetchSurveys(); this.fetchVotes(); this.fetchDirects(); } else { this.fetchStudentMessages(); }
  },
  onShow() {
    const app = getApp();
    const role = wx.getStorageSync('ROLE') || (app.globalData && app.globalData.role) || 'student';
    const tb = this.getTabBar && this.getTabBar();
    if (role === 'teacher' && tb && typeof tb.setData === 'function') {
      tb.setData({ selected: 2 })
    }
    if (role !== 'teacher') { return }
  },
  setTab(e) { const v = e.currentTarget.dataset.v || 'consult'; this.setData({ activeTab: v }) },
  onClassId(e) { this.setData({ classId: e.detail.value }) },
  onTitle(e) { this.setData({ title: e.detail.value }) },
  onContent(e) { this.setData({ content: e.detail.value }) },
  onSubmit() {
    const { classId, title, content } = this.data
    this.setData({ submitting: true })
    req.post('/wechat/api/teacher/notice/create', { classId, title, content }, { loading: true, header: { 'Content-Type': 'application/json' } }).then(() => {
      wx.showToast({ title: '已发布', icon: 'success' })
      this.setData({ title: '', content: '' })
      this.fetchList()
    }).finally(() => { this.setData({ submitting: false }) })
  },
  fetchList() {
    req.get('/wechat/api/teacher/notice/list', null, { loading: true }).then((data) => {
      let list = Array.isArray(data) ? data : []
      if (!list.length) list = [{ id: 'n-1', title: '示例通知：本周五班会' }]
      this.setData({ list, noticeCount: list.length })
    })
  },
  fetchClasses() {
    req.get('/wechat/api/teacher/classes/list', null, { loading: true, showError: false }).then((data) => {
      let classes = Array.isArray(data) ? data : []
      if (!classes.length) classes = [ { id: 'class-100', name: '高一1班' }, { id: 'class-101', name: '高一2班' } ]
      const firstId = classes[0] ? (classes[0].id || classes[0].classId || '') : ''
      this.setData({ classes, classIndex: 0, classId: firstId })
    })
  },
  onClassPick(e) {
    const idx = Number(e.detail.value)
    const { classes } = this.data
    const id = classes && classes[idx] ? (classes[idx].id || classes[idx].classId || '') : ''
    this.setData({ classIndex: idx, classId: id })
  },
  fetchParentMessages() {
    req.get('/wechat/api/teacher/message/list', null, { loading: true, transform: (d) => Array.isArray(d) ? d : null, defaultValue: [] }).then((list) => {
      const unread = list.filter(i => !i.reply).length
      this.setData({ parentMessages: list, unreadMsgCount: unread })
      this.applyConsultFilter()
    })
  },
  setConsultFilter(e) {
    const v = e.currentTarget.dataset.v || 'all'
    this.setData({ consultFilter: v })
    this.applyConsultFilter()
  },
  applyConsultFilter() {
    const { parentMessages, consultFilter } = this.data
    let v = parentMessages
    if (consultFilter === 'unread') v = parentMessages.filter(i => !i.reply)
    if (consultFilter === 'resolved') v = parentMessages.filter(i => i.status === 'resolved')
    this.setData({ parentVisible: v })
  },
  onReplyInput(e) {
    const id = e.currentTarget.dataset.id
    const v = e.detail.value
    this.setData({ replyMap: Object.assign({}, this.data.replyMap, { [id]: v }) })
  },
  onReplyImage(e) {
    const id = e.currentTarget.dataset.id
    wx.chooseImage({ count: 1, success: (r) => {
      const filePath = r.tempFilePaths[0]
      req.upload('/wechat/api/teacher/message/reply/image', filePath, 'file', { id }, { loading: true }).then((url) => {
        if (url) this.setData({ replyImageMap: Object.assign({}, this.data.replyImageMap, { [id]: url }) })
      })
    }})
  },
  onReply(e) {
    const id = e.currentTarget.dataset.id
    const reply = (this.data.replyMap[id] || '').trim()
    if (!reply) { wx.showToast({ title: '请输入回复内容', icon: 'none' }); return }
    const img = this.data.replyImageMap[id] || ''
    req.post('/wechat/api/teacher/message/reply', { id, reply, imageUrl: img }, { loading: true, header: { 'Content-Type': 'application/json' }, defaultValue: { ok: true } }).then(() => {
      wx.showToast({ title: '已回复', icon: 'success' })
      const map = Object.assign({}, this.data.replyMap); delete map[id]
      this.setData({ replyMap: map })
      this.fetchParentMessages()
    })
  },
  markStatus(e) {
    const id = e.currentTarget.dataset.id
    const status = e.currentTarget.dataset.status
    req.post('/wechat/api/teacher/message/mark', { id, status }, { loading: true, header: { 'Content-Type': 'application/json' }, defaultValue: { ok: true } }).then(() => {
      wx.showToast({ title: '已更新', icon: 'success' })
      this.fetchParentMessages()
    })
  },
  convertToNotice(e) {
    const id = e.currentTarget.dataset.id
    const msg = (this.data.parentMessages || []).find(m => m.id === id)
    if (!msg) return
    const classId = this.data.classId
    const title = '共性问题答复'
    const content = msg.content
    req.post('/wechat/api/teacher/notice/create', { classId, title, content }, { loading: true, header: { 'Content-Type': 'application/json' }, defaultValue: { ok: true } }).then(() => {
      wx.showToast({ title: '已转为通知', icon: 'success' })
      this.fetchList()
    })
  },
  // Survey
  onSurveyQuestion(e) { this.setData({ surveyQuestion: e.detail.value }) },
  onSurveyOptions(e) { this.setData({ surveyOptions: e.detail.value }) },
  createSurvey() {
    const { surveyQuestion, surveyOptions } = this.data
    const options = (surveyOptions || '').split(',').map(s => s.trim()).filter(Boolean)
    if (!surveyQuestion || options.length < 2) { wx.showToast({ title: '问题与至少两项选项', icon: 'none' }); return }
    req.post('/wechat/api/teacher/survey/create', { question: surveyQuestion, options }, { loading: true, header: { 'Content-Type': 'application/json' }, defaultValue: { id: 'local-survey-' + Date.now(), question: surveyQuestion, options, createdAt: new Date().toISOString() } }).then((created) => {
      const surveys = this.data.surveys.slice(); surveys.unshift(created)
      this.setData({ surveys, surveyQuestion: '', surveyOptions: '' })
      wx.showToast({ title: '已发布问卷', icon: 'success' })
    })
  },
  fetchSurveys() {
    req.get('/wechat/api/teacher/survey/list', null, { loading: true, showError: false, transform: (d) => Array.isArray(d) ? d : null, defaultValue: [] }).then((list) => this.setData({ surveys: list }))
  },
  // Vote
  onVotingTopic(e) { this.setData({ votingTopic: e.detail.value }) },
  onVotingOptions(e) { this.setData({ votingOptions: e.detail.value }) },
  createVote() {
    const { votingTopic, votingOptions } = this.data
    const options = (votingOptions || '').split(',').map(s => s.trim()).filter(Boolean)
    if (!votingTopic || options.length < 2) { wx.showToast({ title: '主题与至少两项选项', icon: 'none' }); return }
    req.post('/wechat/api/teacher/vote/create', { topic: votingTopic, options }, { loading: true, header: { 'Content-Type': 'application/json' }, defaultValue: { id: 'local-vote-' + Date.now(), topic: votingTopic, options, createdAt: new Date().toISOString() } }).then((created) => {
      const votes = this.data.votes.slice(); votes.unshift(created)
      this.setData({ votes, votingTopic: '', votingOptions: '' })
      wx.showToast({ title: '已发布投票', icon: 'success' })
    })
  },
  fetchVotes() {
    req.get('/wechat/api/teacher/vote/list', null, { loading: true, showError: false, transform: (d) => Array.isArray(d) ? d : null, defaultValue: [] }).then((list) => this.setData({ votes: list }))
  },
  // Direct communication
  onDirectStudentId(e) { this.setData({ directStudentId: e.detail.value }) },
  onDirectContent(e) { this.setData({ directContent: e.detail.value }) },
  createDirect() {
    const { directStudentId, directContent } = this.data
    if (!directStudentId || !directContent) { wx.showToast({ title: '填写学员ID与内容', icon: 'none' }); return }
    req.post('/wechat/api/teacher/direct/create', { studentId: directStudentId, content: directContent }, { loading: true, header: { 'Content-Type': 'application/json' }, defaultValue: { id: 'local-direct-' + Date.now(), studentId: directStudentId, content: directContent, createdAt: new Date().toISOString() } }).then((created) => {
      const directs = this.data.directs.slice(); directs.unshift(created)
      this.setData({ directs, directStudentId: '', directContent: '' })
      wx.showToast({ title: '已发起沟通', icon: 'success' })
    })
  },
  fetchDirects() {
    req.get('/wechat/api/teacher/direct/list', null, { loading: true, showError: false, transform: (d) => Array.isArray(d) ? d : null, defaultValue: [] }).then((list) => this.setData({ directs: list }))
  },
  fetchStudentMessages() {
    req.get('/wechat/api/student/messages/list', null, { loading: true, transform: (d) => Array.isArray(d) ? d : null, defaultValue: [] }).then((list) => this.setData({ studentList: list || [] }))
  }
});