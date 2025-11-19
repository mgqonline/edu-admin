const req = require('../../../utils/request')

Page({
  data: { signCode: '', expireMinutes: 0, history: [], loading: false, selectedSignCode: '', detailSigned: [], detailUnsigned: [], detailLoading: false },
  onLoad() {
    this.fetchHistory()
  },
  fetchHistory() {
    this.setData({ loading: true })
    req.get('/wechat/api/teacher/sign/history', null, { loading: true, transform: (d) => Array.isArray(d) ? d : null, defaultValue: [] }).then((list) => {
      this.setData({ history: list })
    }).finally(() => this.setData({ loading: false }))
  },
  onStartSign() {
    req.post('/wechat/api/teacher/sign', {}, { loading: true, header: { 'Content-Type': 'application/json' }, defaultValue: { signCode: 'LOCAL-123456', expireMinutes: 10 } }).then((d) => {
      const code = d && d.signCode ? d.signCode : ''
      const minutes = d && d.expireMinutes ? d.expireMinutes : 10
      this.setData({ signCode: code, expireMinutes: minutes })
      wx.showToast({ title: '已发起签到', icon: 'success' })
      this.fetchHistory()
      if (code) {
        this.fetchDetail(code)
      }
    })
  },
  onViewDetail(e) {
    const code = e.currentTarget.dataset.code || ''
    if (!code) return
    this.fetchDetail(code)
  },
  fetchDetail(signCode) {
    this.setData({ selectedSignCode: signCode, detailLoading: true })
    req.get('/wechat/api/teacher/sign/detail', { signCode }, { loading: true, showError: false, transform: (d) => {
      const signed = Array.isArray(d && d.signed) ? d.signed : []
      const unsigned = Array.isArray(d && d.unsigned) ? d.unsigned : []
      return { signed, unsigned }
    }, defaultValue: { signed: [], unsigned: [] } }).then((resp) => {
      this.setData({ detailSigned: resp.signed, detailUnsigned: resp.unsigned })
    }).finally(() => this.setData({ detailLoading: false }))
  }
})