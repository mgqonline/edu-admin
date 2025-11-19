const req = require('../../../utils/request')

Page({
  data: { classId: '', title: '', content: '', resourceUrl: '', submitting: false, titleLen: 0, contentLen: 0 },
  onClassId(e) { this.setData({ classId: e.detail.value }) },
  onTitle(e) { const v = e.detail.value || ''; this.setData({ title: v, titleLen: v.length }) },
  onContent(e) { const v = e.detail.value || ''; this.setData({ content: v, contentLen: v.length }) },
  onPickResource() {
    wx.chooseMessageFile({ count: 1, type: 'file', success: (r) => {
      const file = r.tempFiles && r.tempFiles[0]
      if (!file) return
      req.upload('/wechat/api/teacher/plan/upload', file.path, 'file', null, { loading: true }).then((url) => {
        if (url) this.setData({ resourceUrl: url })
      })
    }})
  },
  onSubmit() {
    const { classId, title, content, resourceUrl } = this.data
    if (!classId || !title || !content) { wx.showToast({ title: '请填写完整', icon: 'none' }); return }
    this.setData({ submitting: true })
    req.post('/wechat/api/teacher/plan/create', { classId, title, content, resourceUrl }, { loading: true, header: { 'Content-Type': 'application/json' }, defaultValue: { ok: true } }).then(() => {
      wx.showToast({ title: '已提交', icon: 'success' })
      this.setData({ classId: '', title: '', content: '', resourceUrl: '', titleLen: 0, contentLen: 0 })
    }).finally(() => this.setData({ submitting: false }))
  }
})