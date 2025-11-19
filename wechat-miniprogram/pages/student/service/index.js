Page({
  data: { leaves: [], lessons: [], payments: [], reason: '', start: '', end: '' },
  onLoad() {
    this.loadLeaves();
    this.loadLessons();
    this.loadPayments();
  },
  onReason(e) { this.setData({ reason: e.detail.value }) },
  onStart(e) { this.setData({ start: e.detail.value }) },
  onEnd(e) { this.setData({ end: e.detail.value }) },
  submitLeave() {
    const { reason, start, end } = this.data
    if (!reason || !start || !end) { wx.showToast({ title: '请填写完整', icon: 'none' }); return }
    wx.request({
      url: 'http://localhost:8090/wechat/api/student/leave/submit',
      method: 'POST',
      data: { studentId: 1, reason, start, end },
      success: () => { wx.showToast({ title: '已提交', icon: 'success' }); this.loadLeaves() },
      fail: () => wx.showToast({ title: '提交失败', icon: 'error' })
    })
  },
  loadLeaves() {
    wx.request({
      url: 'http://localhost:8090/wechat/api/student/leave/list',
      data: { studentId: 1 },
      success: (res) => this.setData({ leaves: res.data?.data || [] }),
      fail: () => this.setData({ leaves: [{ id: 1, reason: '生病', start: '2025-11-15 18:00', end: '2025-11-15 20:00', status: '审批中' }] })
    })
  },
  loadLessons() {
    wx.request({
      url: 'http://localhost:8090/wechat/api/student/lessons/remaining',
      success: (res) => this.setData({ lessons: res.data?.data || [] }),
      fail: () => this.setData({ lessons: [{ courseId: 1, courseName: '英语口语', remaining: 6 }] })
    })
  },
  loadPayments() {
    wx.request({
      url: 'http://localhost:8090/wechat/api/student/payments/list',
      success: (res) => this.setData({ payments: res.data?.data || [] }),
      fail: () => this.setData({ payments: [{ id: 1, courseName: '数学提高班', amount: 1200, date: '2025-10-01', type: '缴费' }, { id: 2, courseName: '数学提高班', amount: 200, date: '2025-10-15', type: '退费' }] })
    })
  }
})