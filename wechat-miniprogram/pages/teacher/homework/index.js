const req = require('../../../utils/request')

Page({
  data: { title: '', content: '', titleLen: 0, contentLen: 0, answerUrl: '', answerName: '', submitting: false },
  onTitle(e) { const v = e.detail.value || ''; this.setData({ title: v, titleLen: v.length }) },
  onContent(e) { const v = e.detail.value || ''; this.setData({ content: v, contentLen: v.length }) },
  onPublish() {
    const { title, content, answerUrl } = this.data
    if (!title || !content) { wx.showToast({ title: '请填写标题与内容', icon: 'none' }); return }
    this.setData({ submitting: true })
    req.post('/wechat/api/teacher/homework/publish', { title, content, answerUrl }, { loading: true, header: { 'Content-Type': 'application/json' }, defaultValue: { ok: true } }).then(() => {
      wx.showToast({ title: '已发布', icon: 'success' })
      this.setData({ title: '', content: '', titleLen: 0, contentLen: 0 })
    }).finally(() => this.setData({ submitting: false }))
  },
  onUpload() {
    wx.chooseMessageFile({ count: 1, type: 'file', success: (res) => {
      const file = res.tempFiles && res.tempFiles[0]
      if (!file) return
      req.upload('/wechat/api/teacher/homework/answer/upload', file.path, 'file', { title: file.name }, { loading: true }).then((url) => {
        if (url) this.setData({ answerUrl: url, answerName: file.name })
        wx.showToast({ title: '已上传', icon: 'success' })
      })
    }})
  }
})