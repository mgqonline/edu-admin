const req = require('../../../utils/request')

Page({
  data: { list: [], visibleList: [], unreadCount: 0, filter: 'all', composeContent: '', sending: false },
  onLoad() { this.fetchList() },
  onPullDownRefresh() { this.fetchList(() => wx.stopPullDownRefresh()) },
  setFilter(e) {
    const v = e.currentTarget.dataset.v || 'all'
    this.setData({ filter: v })
    this.applyFilter()
  },
  onCompose(e) { this.setData({ composeContent: e.detail.value }) },
  sendMessage() {
    const content = (this.data.composeContent || '').trim()
    if (!content) { wx.showToast({ title: '请输入留言内容', icon: 'none' }); return }
    this.setData({ sending: true })
    req.post('/wechat/api/student/messages/create', { content }, { loading: true, defaultValue: { ok: true } }).then(() => {
      wx.showToast({ title: '已发送', icon: 'success' })
      this.setData({ composeContent: '' })
      this.fetchList()
    }).finally(() => this.setData({ sending: false }))
  },
  fetchList(done) {
    Promise.all([
      req.get('/wechat/api/student/messages/list', null, { loading: true, showError: false, transform: (d) => Array.isArray(d) ? d : null, defaultValue: [] }),
      req.get('/wechat/api/teacher/notice/list', null, { loading: true, showError: false, transform: (d) => Array.isArray(d) ? d : null, defaultValue: [] })
    ]).then(([msgs, notices]) => {
      const msgList = (msgs || []).map(m => Object.assign({ type: 'message' }, m))
      const noticeList = (notices || []).map(n => ({ id: n.id || ('notice-' + Math.random()), type: 'notice', title: n.title || '', content: n.content || '', createdAt: n.createdAt || '' }))
      const combined = noticeList.concat(msgList)
      const unread = combined.filter(i => i.type === 'message' && !i.reply).length
      this.setData({ list: combined, unreadCount: unread })
      this.applyFilter()
    }).finally(() => { if (typeof done === 'function') done() })
  },
  applyFilter() {
    const { list, filter } = this.data
    let v = list
    if (filter === 'unread') v = list.filter(i => i.type === 'message' && !i.reply)
    this.setData({ visibleList: v })
  }
})