const req = require('../../../utils/request')

Page({
  data: { title: '', content: '', titleLen: 0, contentLen: 0, submitting: false, list: [], loading: false },
  onLoad() { this.fetchHistory() },
  onPullDownRefresh() { this.fetchHistory(() => wx.stopPullDownRefresh()) },
  onTitle(e) { const v = e.detail.value || ''; this.setData({ title: v, titleLen: v.length }) },
  onContent(e) { const v = e.detail.value || ''; this.setData({ content: v, contentLen: v.length }) },
  fetchHistory(done) {
    this.setData({ loading: true })
    req.get('/wechat/api/student/feedback/list', null, { loading: true, showError: false, transform: (d) => Array.isArray(d) ? d : null, defaultValue: [] }).then((list) => {
      this.setData({ list })
    }).finally(() => { this.setData({ loading: false }); if (typeof done === 'function') done() })
  },
  onSubmit() {
    const { title, content } = this.data
    if (!title || !content) { wx.showToast({ title: '请填写标题与内容', icon: 'none' }); return }
    this.setData({ submitting: true })
    req.post('/wechat/api/student/feedback/create', { title, content }, { loading: true, header: { 'Content-Type': 'application/json' }, defaultValue: { id: 'local-' + Date.now(), title, content, createdAt: new Date().toISOString() } }).then((created) => {
      wx.showToast({ title: '提交成功', icon: 'success' })
      const list = this.data.list.slice(); if (created && created.id) list.unshift(created)
      this.setData({ title: '', content: '', titleLen: 0, contentLen: 0, list })
    }).finally(() => this.setData({ submitting: false }))
  }
})