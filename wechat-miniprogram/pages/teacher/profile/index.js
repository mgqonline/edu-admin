const req = require('../../../utils/request')

Page({
  data: {
    summary: { monthTotal: 0 },
    history: [],
    pageNo: 1,
    pageSize: 10,
    hasMore: true,
    profile: { id: '', name: '', subjects: '未设定', years: 3, phone: '未绑定' }
  },
  onLoad() {
    this.initProfile()
    this.loadSummary()
    this.loadHistory(true)
  },
  onReachBottom() {
    if (this.data.hasMore) this.loadHistory()
  },
  initProfile() {
    const baseUrl = getApp().globalData.baseUrl
    const token = wx.getStorageSync('AUTH_TOKEN')
    req.get('/wechat/api/teacher/profile', null, { loading: false, header: token ? { Authorization: token } : {} }).then((data) => {
      if (data) this.setData({ profile: { id: data.teacherId, name: data.name || ('教师 #' + (data.teacherId || '')), subjects: data.subjects || '未设定', years: data.years || 3, phone: data.phone || '未绑定' } })
    })
  },
  loadSummary() {
    req.get('/wechat/api/teacher/fee/summary', null, { loading: true }).then((data) => {
      this.setData({ summary: data || { monthTotal: 0 } })
    })
  },
  loadHistory(reset) {
    let { pageNo, pageSize, history } = this.data
    if (reset) { pageNo = 1; history = [] }
    req.get('/wechat/api/teacher/fee/history', { pageNo, pageSize }, { loading: !reset }).then((data) => {
      const list = Array.isArray(data) ? data : []
      const merged = history.concat(list)
      const hasMore = list.length >= pageSize
      this.setData({ history: merged, pageNo: pageNo + 1, hasMore })
    })
  },
  toFeeHistory() { /* 当前页即显示历史 */ },
  goSchedule() { wx.switchTab({ url: '/pages/tab/service/index' }) },
  goGrades() { wx.switchTab({ url: '/pages/tab/manage/index' }) },
  goHomework() { wx.switchTab({ url: '/pages/tab/manage/index' }) },
  goAttendance() { wx.switchTab({ url: '/pages/tab/manage/index' }) }
})