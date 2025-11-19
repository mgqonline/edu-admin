const req = require('../../../utils/request')

Page({
  data: { approvals: [], count: 0, submitting: false, course: '', originalDate: '', originalTime: '', newDate: '', newTime: '', reason: '' },
  onLoad() {
    req.get('/wechat/api/teacher/adjust/list', null, { transform: (d) => Array.isArray(d) ? d : null, defaultValue: [] }).then((list) => {
      this.setData({ approvals: list, count: list.length })
    })
  },
  onCourse(e) { this.setData({ course: e.detail.value }) },
  onReason(e) { this.setData({ reason: e.detail.value }) },
  onPickOriginalDate(e) { this.setData({ originalDate: e.detail.value }) },
  onPickOriginalTime(e) { this.setData({ originalTime: e.detail.value }) },
  onPickNewDate(e) { this.setData({ newDate: e.detail.value }) },
  onPickNewTime(e) { this.setData({ newTime: e.detail.value }) },
  onSubmit() {
    const { course, originalDate, originalTime, newDate, newTime, reason } = this.data
    if (!course || !originalDate || !originalTime || !newDate || !newTime || !reason) { wx.showToast({ title: '请填写完整', icon: 'none' }); return }
    const original = `${originalDate} ${originalTime}`
    const updated = `${newDate} ${newTime}`
    this.setData({ submitting: true })
    req.post('/wechat/api/teacher/adjust/apply', { course, originalTime: original, newTime: updated, reason }, { loading: true, header: { 'Content-Type': 'application/json' }, defaultValue: { id: 'local-' + Date.now(), course, originalTime: original, newTime: updated, status: '审批中' } }).then((created) => {
      const list = this.data.approvals.slice()
      if (created && created.id) list.unshift(created)
      this.setData({ approvals: list, count: list.length, course: '', originalDate: '', originalTime: '', newDate: '', newTime: '', reason: '' })
      wx.showToast({ title: '已提交', icon: 'success' })
    }).finally(() => this.setData({ submitting: false }))
  }
})