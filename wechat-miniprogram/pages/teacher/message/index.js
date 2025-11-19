const req = require('../../../utils/request')

Page({
  data: { list: [], replyMap: {}, loading: false, unreadCount: 0 },
  onLoad() { this.fetchList() },
  onPullDownRefresh() { this.fetchList(() => wx.stopPullDownRefresh()) },
  fetchList(done) {
    this.setData({ loading: true })
    req.get('/wechat/api/teacher/message/list', null, { loading: true, transform: (d) => Array.isArray(d) ? d : null, defaultValue: [] }).then((list) => {
      const unread = list.filter(i => !i.reply).length
      this.setData({ list, unreadCount: unread })
    }).finally(() => { this.setData({ loading: false }); if (typeof done === 'function') done() })
  },
  onReplyInput(e) {
    const id = e.currentTarget.dataset.id
    const v = e.detail.value
    this.setData({ replyMap: Object.assign({}, this.data.replyMap, { [id]: v }) })
  },
  onReply(e) {
    const id = e.currentTarget.dataset.id
    const reply = (this.data.replyMap[id] || '').trim()
    if (!reply) { wx.showToast({ title: '请输入回复内容', icon: 'none' }); return }
    req.post('/wechat/api/teacher/message/reply', { id, reply }, { loading: true, header: { 'Content-Type': 'application/json' }, defaultValue: { ok: true } }).then(() => {
      wx.showToast({ title: '已回复', icon: 'success' })
      const map = Object.assign({}, this.data.replyMap); delete map[id]
      this.setData({ replyMap: map })
      this.fetchList()
    })
  }
})