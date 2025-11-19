Page({
  data: { submitting: false, submittedCount: 0 },
  onSubmit(e) {
    const { studentId, course, type, score } = e.detail.value
    this.setData({ submitting: true })
    wx.request({
      url: 'http://localhost:8090/wechat/api/teacher/grades/record',
      method: 'POST',
      data: { studentId, course, type, score },
      success: () => { wx.showToast({ title: '已登记', icon: 'success' }); this.setData({ submittedCount: this.data.submittedCount + 1 }) },
      fail: () => wx.showToast({ title: '登记失败', icon: 'none' }),
      complete: () => this.setData({ submitting: false })
    })
  }
})