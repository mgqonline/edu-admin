Page({
  data: { role: 'student', userId: '', baseUrl: '' },
  onLoad() {
    const app = getApp();
    const saved = wx.getStorageSync('BASE_URL');
    this.setData({ baseUrl: saved || (app.globalData && app.globalData.baseUrl) || 'http://127.0.0.1:8090' });
  },
  onRoleChange(e) {
    const role = e.detail.value;
    this.setData({ role });
  },
  onIdInput(e) {
    this.setData({ userId: e.detail.value });
  },
  onBaseUrlInput(e) {
    this.setData({ baseUrl: e.detail.value });
  },
  devLogin() {
    const role = this.data.role;
    const id = Number(this.data.userId);
    const baseUrl = this.data.baseUrl;
    if (!id) {
      wx.showToast({ title: '请输入用户ID', icon: 'none' });
      return;
    }
    const app = getApp();
    app.globalData.baseUrl = baseUrl;
    wx.setStorageSync('BASE_URL', baseUrl);
    const data = role === 'teacher' ? { role, teacherId: id } : { role, studentId: id };
    wx.showLoading({ title: '登录中' });
    const req = require('../../utils/request')
    req.post('/wechat/api/auth/dev-login', data, {
      loading: true,
      transform: (resp) => {
        if (!resp || typeof resp !== 'object') return null
        const token = resp.token || ''
        return token ? token : null
      },
      defaultValue: null
    }).then((token) => {
      if (!token) {
        token = 'Bearer wechat dev-openid-' + role + '-' + id
        wx.showToast({ title: '后端不可用，进入本地模拟', icon: 'none' })
      }
      wx.setStorageSync('AUTH_TOKEN', token);
      wx.setStorageSync('ROLE', role);
      app.globalData.role = role;
      wx.hideLoading();
      if (role === 'teacher') {
        wx.switchTab({ url: '/pages/tab/service/index' });
      } else {
        wx.switchTab({ url: '/pages/tab/schedule/index' });
      }
    }).catch(() => {
      wx.hideLoading();
      wx.showToast({ title: '登录异常', icon: 'error' })
    })
  }
});