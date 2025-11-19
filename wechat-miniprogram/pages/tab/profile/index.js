const req = require('../../../utils/request')

Page({
  data: { role: 'student', profile: { id: '', name: '', subjects: '未设定', years: 0, phone: '未绑定' }, summary: { monthTotal: 0 }, studentSchedule: [], scheduleCount: 0, studentMessages: [], unreadCount: 0 },
  onLoad() {
    const app = getApp();
    const role = wx.getStorageSync('ROLE') || (app.globalData && app.globalData.role) || 'student';
    this.setData({ role })
    if (role === 'teacher') { this.loadProfile(); this.loadSummary() } else { this.loadStudentSchedule(); this.loadStudentMessages() }
  },
  onShow() {
    const app = getApp();
    const role = wx.getStorageSync('ROLE') || (app.globalData && app.globalData.role) || 'student';
    const tb = this.getTabBar && this.getTabBar();
    if (tb && typeof tb.setData === 'function') {
      const sel = role === 'teacher' ? 3 : 3
      tb.setData({ selected: sel })
    }
    if (role !== 'teacher') { return }
  },
  loadProfile() {
    req.get('/wechat/api/teacher/profile', null, {
      loading: true,
      transform: (data) => {
        const d = data || {}
        const name = d.name || ('教师 #' + (d.teacherId || ''))
        const avatarUrl = d.avatarUrl || ('https://api.dicebear.com/7.x/avataaars/png?seed=' + encodeURIComponent(name))
        return { id: d.teacherId || '', name, subjects: d.subjects || '未设定', years: d.years || 0, phone: d.phone || '未绑定', avatarUrl }
      },
      defaultValue: { id: '', name: '教师', subjects: '未设定', years: 0, phone: '未绑定' }
    }).then((p) => { this.setData({ profile: p }) })
  },
  loadSummary() {
    req.get('/wechat/api/teacher/fee/summary', null, {
      loading: false,
      transform: (data) => {
        if (data && typeof data === 'object') return { monthTotal: typeof data.monthTotal === 'number' ? data.monthTotal : 0 }
        if (typeof data === 'number') return { monthTotal: data }
        return null
      },
      defaultValue: { monthTotal: 0 }
    }).then((summary) => { this.setData({ summary }) })
  },
  loadStudentSchedule() {
    req.get('/wechat/api/student/schedule/today', null, { loading: false, showError: false, transform: (d) => Array.isArray(d) ? d : null, defaultValue: [{ date: '2025-11-05', className: '数学提高班', startTime: '19:00', endTime: '21:00' }] }).then((list) => this.setData({ studentSchedule: list, scheduleCount: list.length }))
  },
  loadStudentMessages() {
    req.get('/wechat/api/student/messages/list', null, { transform: (d) => Array.isArray(d) ? d : null, defaultValue: [] }).then((list) => {
      const unread = list.filter(i => !i.reply).length
      this.setData({ studentMessages: list, unreadCount: unread })
    })
  },
  goStudentSign() { wx.navigateTo({ url: '/pages/student/sign/index' }) },
  logout() {
    try { wx.removeStorageSync('AUTH_TOKEN'); wx.removeStorageSync('ROLE') } catch (e) {}
    const app = getApp();
    if (app && app.globalData) app.globalData.role = 'student'
    wx.reLaunch({ url: '/pages/login/index' })
  }
});