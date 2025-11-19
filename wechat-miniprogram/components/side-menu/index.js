Component({
  properties: {},
  data: { visible: false, role: 'student' },
  attached() {
    const app = getApp()
    const role = wx.getStorageSync('ROLE') || (app.globalData && app.globalData.role) || 'student'
    this.setData({ role })
  },
  methods: {
    open() { this.setData({ visible: true }) },
    close() { this.setData({ visible: false }) },
    noop() {},
    onRoleChange(e) {
      const role = e.detail.value
      wx.setStorageSync('ROLE', role)
      const app = getApp()
      app.globalData.role = role
      if (role === 'teacher') wx.reLaunch({ url: '/pages/teacher/index' })
      else wx.reLaunch({ url: '/pages/tab/schedule/index' })
    },
    go(e) {
      const url = e.currentTarget.dataset.url
      if (!url) return
      if (url.indexOf('/pages/tab/') === 0) wx.switchTab({ url })
      else wx.navigateTo({ url })
    },
    logout() {
      wx.removeStorageSync('AUTH_TOKEN')
      wx.reLaunch({ url: '/pages/login/index' })
    }
  }
})