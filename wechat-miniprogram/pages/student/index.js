const req = require('../../utils/request')

Page({
  data: { schedule: [], scheduleCount: 0 },
  onLoad() { this.fetchSchedule() },
  onPullDownRefresh() { this.fetchSchedule(() => wx.stopPullDownRefresh()) },
  fetchSchedule(done) {
    req.get('/wechat/api/student/schedule/today', null, { transform: (d) => Array.isArray(d) ? d : null, defaultValue: [{ date: '2025-11-05', className: '数学提高班', startTime: '19:00', endTime: '21:00' }] }).then((list) => {
      this.setData({ schedule: list, scheduleCount: list.length })
    }).finally(() => { if (typeof done === 'function') done() })
  },
  onScan() { wx.scanCode({ success: () => {} }) }
})