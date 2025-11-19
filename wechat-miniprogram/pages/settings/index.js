Page({
  data: { settings: { homework: true, attendance: true, payment: true } },
  onLoad() {
    const saved = wx.getStorageSync('settings')
    if (saved && typeof saved === 'object') this.setData({ settings: saved })
  },
  toggle(e) {
    const key = e.currentTarget.dataset.key
    const val = e.detail.value
    const s = Object.assign({}, this.data.settings)
    s[key] = val
    this.setData({ settings: s })
    wx.setStorageSync('settings', s)
  },
  clearCache() {
    const base = wx.getStorageSync('BASE_URL')
    const role = wx.getStorageSync('ROLE')
    const token = wx.getStorageSync('AUTH_TOKEN')
    wx.clearStorage()
    if (base) wx.setStorageSync('BASE_URL', base)
    if (role) wx.setStorageSync('ROLE', role)
    if (token) wx.setStorageSync('AUTH_TOKEN', token)
    wx.showToast({ title: '已清理', icon: 'success' })
  }
})