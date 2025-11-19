Page({
  data: { list: [], loading: false, count: 0 },
  onLoad() {
    this.setData({ loading: true })
    wx.request({
      url: 'http://localhost:8090/wechat/api/teacher/schedule',
      method: 'GET',
      success: (res) => {
        const list = res.data?.data || []
        this.setData({ list, count: list.length })
      },
      fail: () => {
        const list = [
          { id: 't-1', className: '语文', date: '2025-11-14', startTime: '08:00', endTime: '08:45' }
        ]
        this.setData({ list, count: list.length })
      },
      complete: () => this.setData({ loading: false })
    })
  }
})