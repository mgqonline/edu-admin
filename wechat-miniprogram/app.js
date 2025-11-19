App({
  globalData: {
    // 可通过 wx.setStorageSync('BASE_URL', 'http://<LAN-IP>:8090') 覆盖
    baseUrl: ''
  },
  onLaunch() {
    console.log('Edu Wechat mini-program launched')
    try {
      const saved = wx.getStorageSync('BASE_URL')
      this.globalData.baseUrl = saved || 'http://127.0.0.1:8090'
    } catch (e) {
      this.globalData.baseUrl = 'http://127.0.0.1:8090'
    }
    try {
      const token = wx.getStorageSync('AUTH_TOKEN')
      const role = wx.getStorageSync('ROLE') || 'student'
      this.globalData.role = role
      if (!token) {
        wx.reLaunch({ url: '/pages/login/index' })
      } else if (role === 'teacher') {
        wx.switchTab({ url: '/pages/tab/service/index' })
      } else {
        wx.switchTab({ url: '/pages/tab/schedule/index' })
      }
    } catch (e) {
      // ignore startup redirect errors
    }
  }
})