const req = require('../../../utils/request')

Page({
  data: { content: '', performance: '', contentLen: 0, performanceLen: 0, mood: '良好', submitting: false },
  onContent(e) { const v = e.detail.value || ''; this.setData({ content: v, contentLen: v.length }) },
  onPerformance(e) { const v = e.detail.value || ''; this.setData({ performance: v, performanceLen: v.length }) },
  setMood(e) { const v = e.currentTarget.dataset.v; this.setData({ mood: v }) },
  onSubmit() {
    const { content, performance, mood } = this.data
    if (!content || !performance) { wx.showToast({ title: '请填写内容与表现', icon: 'none' }); return }
    this.setData({ submitting: true })
    req.post('/wechat/api/teacher/classrecord/create', { content, performance, mood }, { loading: true, header: { 'Content-Type': 'application/json' }, defaultValue: { ok: true } }).then(() => {
      wx.showToast({ title: '已提交', icon: 'success' })
      this.setData({ content: '', performance: '', contentLen: 0, performanceLen: 0, mood: '良好' })
    }).finally(() => this.setData({ submitting: false }))
  }
})