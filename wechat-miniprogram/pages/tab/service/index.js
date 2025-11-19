const req = require('../../../utils/request')

Page({
  data: { role: 'student', list: [], loading: false, leaves: [], lessons: [], payments: [], reason: '', startDate: '', startTime: '', endDate: '', endTime: '', submitting: false, remainingTotal: 0, leaveCount: 0, scheduleCount: 0 },
  onLoad() {
    const app = getApp();
    const role = wx.getStorageSync('ROLE') || (app.globalData && app.globalData.role) || 'student';
    this.setData({ role })
    if (role === 'teacher') {
      this.loadSchedule()
    } else {
      this.loadLeaves(); this.loadLessons(); this.loadPayments();
    }
  },
  onPullDownRefresh() {
    if (this.data.role === 'teacher') {
      this.loadSchedule(() => wx.stopPullDownRefresh())
    } else {
      this.loadLeaves(); this.loadLessons(); this.loadPayments(); wx.stopPullDownRefresh()
    }
  },
  onShow() {
    const app = getApp();
    const role = wx.getStorageSync('ROLE') || (app.globalData && app.globalData.role) || 'student';
    const tb = this.getTabBar && this.getTabBar();
    if (tb && typeof tb.setData === 'function') {
      const sel = role === 'teacher' ? 0 : 0
      tb.setData({ selected: sel })
    }
    if (role !== 'teacher') { return }
  },
  loadSchedule(done) {
    this.setData({ loading: true })
    req.get('/wechat/api/teacher/schedule', null, { loading: true }).then((data) => {
      let list = Array.isArray(data) ? data : []
      if (!list.length) {
        list = [
          { id: 'm-1', className: '高一语文', date: '2025-11-14', startTime: '08:00', endTime: '08:45' },
          { id: 'm-2', className: '高一数学', date: '2025-11-14', startTime: '09:00', endTime: '09:45' },
          { id: 'm-3', className: '高一英语', date: '2025-11-14', startTime: '10:00', endTime: '10:45' }
        ]
      }
      this.setData({ list, scheduleCount: list.length })
    }).finally(() => {
      this.setData({ loading: false })
      if (typeof done === 'function') done()
    })
  }
  ,
  onReason(e) { this.setData({ reason: e.detail.value }) },
  onStartDate(e) { this.setData({ startDate: e.detail.value }) },
  onStartTime(e) { this.setData({ startTime: e.detail.value }) },
  onEndDate(e) { this.setData({ endDate: e.detail.value }) },
  onEndTime(e) { this.setData({ endTime: e.detail.value }) },
  submitLeave() {
    const { reason, startDate, startTime, endDate, endTime } = this.data
    if (!reason || !startDate || !startTime || !endDate || !endTime) { wx.showToast({ title: '请填写完整', icon: 'none' }); return }
    const start = `${startDate} ${startTime}`
    const end = `${endDate} ${endTime}`
    this.setData({ submitting: true })
    req.post('/wechat/api/student/leave/submit', { reason, start, end }, {
      loading: true,
      header: { 'Content-Type': 'application/json' },
      defaultValue: { id: 'local-' + Date.now(), reason, start, end, status: '审批中' }
    }).then((created) => {
      if (created && created.id) {
        const list = Array.isArray(this.data.leaves) ? this.data.leaves.slice() : []
        list.unshift(created)
        this.setData({ leaves: list, leaveCount: list.length })
      }
      wx.showToast({ title: '已提交', icon: 'success' })
      this.setData({ reason: '', startDate: '', startTime: '', endDate: '', endTime: '' })
    }).finally(() => this.setData({ submitting: false }))
  },
  loadLeaves() {
    req.get('/wechat/api/student/leave/list', null, { loading: false, showError: false, transform: (d) => Array.isArray(d) ? d : null, defaultValue: [{ id: 1, reason: '生病', start: '2025-11-15 18:00', end: '2025-11-15 20:00', status: '审批中' }] }).then((list) => this.setData({ leaves: list, leaveCount: list.length }))
  },
  loadLessons() {
    req.get('/wechat/api/student/lessons/remaining', null, { loading: false, showError: false, transform: (d) => Array.isArray(d) ? d : null, defaultValue: [{ courseId: 1, courseName: '英语口语', remaining: 6 }] }).then((list) => {
      const total = list.reduce((s, i) => s + (Number(i.remaining) || 0), 0)
      this.setData({ lessons: list, remainingTotal: total })
    })
  },
  loadPayments() {
    req.get('/wechat/api/student/payments/list', null, { loading: false, showError: false, transform: (d) => Array.isArray(d) ? d : null, defaultValue: [{ id: 1, courseName: '数学提高班', amount: 1200, date: '2025-10-01', type: '缴费' }, { id: 2, courseName: '数学提高班', amount: 200, date: '2025-10-15', type: '退费' }] }).then((list) => this.setData({ payments: list }))
  }
});