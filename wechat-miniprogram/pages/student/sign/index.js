const req = require('../../../utils/request')

Page({
  data: { code: '', submitting: false },
  onInput(e) { this.setData({ code: e.detail.value }) },
  onScan() { wx.scanCode({ success: (r) => { this.setData({ code: r.result || '' }) } }) },
  onSubmit() {
    const { code } = this.data
    if (!code) { wx.showToast({ title: '请输入签到码', icon: 'none' }); return }
    this.setData({ submitting: true })
    req.post('/wechat/api/student/sign/submit', { signCode: code }, { loading: true, header: { 'Content-Type': 'application/json' }, defaultValue: { ok: true } }).then(() => {
      wx.showToast({ title: '已签到', icon: 'success' })
      this.setData({ code: '' })
    }).finally(() => this.setData({ submitting: false }))
  }
})