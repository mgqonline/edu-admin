const req = require('../../../utils/request')

Page({
  data: { dateOnly: '', startTime: '', endTime: '', capacity: '', reason: '', submitting: false },
  onPickDate(e) { this.setData({ dateOnly: e.detail.value }) },
  onPickStart(e) { this.setData({ startTime: e.detail.value }) },
  onPickEnd(e) { this.setData({ endTime: e.detail.value }) },
  onCapacity(e) { this.setData({ capacity: e.detail.value }) },
  onReason(e) { this.setData({ reason: e.detail.value }) },
  onSubmit() {
    const { dateOnly, startTime, endTime, capacity, reason } = this.data
    if (!dateOnly || !startTime || !endTime || !capacity || !reason) { wx.showToast({ title: '请填写完整', icon: 'none' }); return }
    const num = Number(capacity)
    if (!Number.isFinite(num) || num <= 0) { wx.showToast({ title: '人数需为正整数', icon: 'none' }); return }
    this.setData({ submitting: true })
    req.post('/wechat/api/teacher/room/apply', { dateOnly, startTime, endTime, capacity: num, reason }, {
      loading: true,
      header: { 'Content-Type': 'application/json' },
      defaultValue: { ok: true }
    }).then(() => {
      wx.showToast({ title: '已提交', icon: 'success' })
      this.setData({ dateOnly: '', startTime: '', endTime: '', capacity: '', reason: '' })
    }).finally(() => this.setData({ submitting: false }))
  }
})