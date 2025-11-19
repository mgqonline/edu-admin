const req = require('../../../utils/request')

Page({
  data: { role: 'student', today: [], week: [], loading: false, loadingWeek: false, todayCount: 0, weekCount: 0 },
  onLoad() {
    const app = getApp();
    const role = wx.getStorageSync('ROLE') || (app.globalData && app.globalData.role) || 'student';
    this.setData({ role })
    if (role !== 'teacher') {
      this.loadToday();
      this.loadWeek();
    }
  },
  onShow() {
    const tb = this.getTabBar && this.getTabBar();
    if (tb && typeof tb.setData === 'function') {
      const sel = this.data.role !== 'teacher' ? 2 : 1
      tb.setData({ selected: sel })
    }
  },
  onPullDownRefresh() {
    if (this.data.role !== 'teacher') {
      this.loadToday(true)
      this.loadWeek(true)
    }
    wx.stopPullDownRefresh()
  },
  loadToday(force) {
    const cacheKey = 'CACHE_STUDENT_SCHEDULE_TODAY_TAB'
    if (!force) {
      const cached = req.getCache(cacheKey)
      if (cached) { this.setData({ today: cached }); return }
    }
    this.setData({ loading: true })
    req.get('/wechat/api/student/schedule/today', null, {
      loading: true,
      transform: (data) => Array.isArray(data) ? data : null,
      defaultValue: [
        { classId: 's-t-1', className: '语文', date: '2025-11-14', startTime: '08:00', endTime: '08:45', room: 'A101' }
      ]
    }).then((list) => {
      this.setData({ today: list, todayCount: list.length, loading: false })
      req.setCache(cacheKey, list, 10 * 60 * 1000)
    })
  },
  loadWeek(force) {
    const cacheKey = 'CACHE_STUDENT_SCHEDULE_WEEK_TAB'
    if (!force) {
      const cached = req.getCache(cacheKey)
      if (cached) { this.setData({ week: cached }); return }
    }
    this.setData({ loadingWeek: true })
    req.get('/wechat/api/student/schedule/week', null, {
      loading: true,
      transform: (data) => Array.isArray(data) ? data : null,
      defaultValue: [
        { classId: 's-w-1', className: '数学', date: '2025-11-15', startTime: '09:00', endTime: '09:45', room: 'B201' }
      ]
    }).then((list) => {
      this.setData({ week: list, weekCount: list.length, loadingWeek: false })
      req.setCache(cacheKey, list, 30 * 60 * 1000)
    })
  }
});