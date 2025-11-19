const req = require('../../../utils/request')

Page({
  data: { teacherId: '', parentName: '', content: '', contentLen: 0, submitting: false },
  onTeacherId(e) { this.setData({ teacherId: e.detail.value }) },
  onParentName(e) { this.setData({ parentName: e.detail.value }) },
  onContent(e) { const v = e.detail.value || ''; this.setData({ content: v, contentLen: v.length }) },
  onSubmit() {
    const { teacherId, parentName, content } = this.data
    if (!teacherId || !parentName || !content) { wx.showToast({ title: '请完整填写信息', icon: 'none' }); return }
    this.setData({ submitting: true })
    req.post('/wechat/api/student/contact/send', { teacherId, parentName, content }, { loading: true, header: { 'Content-Type': 'application/json' }, defaultValue: { ok: true } }).then(() => {
      wx.showToast({ title: '已发送', icon: 'success' })
      this.setData({ teacherId: '', parentName: '', content: '', contentLen: 0 })
    }).finally(() => this.setData({ submitting: false }))
  }
})