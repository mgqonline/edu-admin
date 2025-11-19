const req = require('../../../utils/request')

Page({
  data: { today: [], week: [], loading: false, loadingWeek: false, filter: 'today' },
  onLoad() {
    this.loadToday();
    this.loadWeek();
  },
  onPullDownRefresh() {
    this.loadToday(true)
    this.loadWeek(true)
    wx.stopPullDownRefresh()
  },
  loadToday(force) {
    const cacheKey = 'CACHE_STUDENT_SCHEDULE_TODAY'
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
      this.setData({ today: list, loading: false })
      req.setCache(cacheKey, list, 10 * 60 * 1000)
    })
  },
  loadWeek(force) {
    const cacheKey = 'CACHE_STUDENT_SCHEDULE_WEEK'
    if (!force) {
      const cached = req.getCache(cacheKey)
      if (cached) { this.setData({ week: cached }); return }
    }
    this.setData({ loadingWeek: true })
    req.get('/wechat/api/student/schedule/week', null, {
      loading: true,
      transform: (data) => Array.isArray(data) ? data : null,
      defaultValue: [
        { classId: 's-w-1', className: '数学', date: '2025-11-15', startTime: '09:00', endTime: '09:45', room: 'B201' },
        { classId: 's-w-2', className: '英语', date: '2025-11-16', startTime: '10:00', endTime: '10:45', room: 'C301' }
      ]
    }).then((list) => {
      this.setData({ week: list, loadingWeek: false })
      req.setCache(cacheKey, list, 30 * 60 * 1000)
    })
  },
  setFilter(e) {
    const v = e.currentTarget.dataset.v
    this.setData({ filter: v })
  },
  scanSign(e) {
    wx.scanCode({
      success: (r) => {
        const payload = { code: r.result, classId: e.currentTarget.dataset.id }
        req.post('/wechat/api/student/sign/scan', payload, { loading: true }).then((ok) => {
          if (ok) wx.showToast({ title: '已签到', icon: 'success' })
        })
      },
      fail: () => wx.showToast({ title: '未识别二维码', icon: 'error' })
    })
  }
})