Page({
  data: { classes: [], classIndex: 0, classId: '', title: '', content: '', list: [], loading: false, count: 0 },
  onLoad() { this.fetchClasses(); this.fetchList() },
  onClassId(e) { this.setData({ classId: e.detail.value }) },
  onClassPick(e) {
    const idx = Number(e.detail.value)
    const { classes } = this.data
    const id = classes && classes[idx] ? (classes[idx].id || classes[idx].classId || '') : ''
    this.setData({ classIndex: idx, classId: id })
  },
  onTitle(e) { this.setData({ title: e.detail.value }) },
  onContent(e) { this.setData({ content: e.detail.value }) },
  onSubmit() {
    const { classId, title, content } = this.data
    if (!classId || !title || !content) { wx.showToast({ title: '请填写完整', icon: 'none' }); return }
    const req = require('../../../utils/request')
    req.post('/wechat/api/teacher/notice/create', { classId, title, content }, { loading: true, header: { 'Content-Type': 'application/json' }, defaultValue: { ok: true } }).then(() => {
      wx.showToast({ title: '已发布', icon: 'success' })
      this.fetchList()
    }).catch(() => {
      wx.showToast({ title: '发布失败', icon: 'none' })
    })
  },
  fetchList() {
    this.setData({ loading: true })
    wx.request({
      url: 'http://localhost:8090/wechat/api/teacher/notice/list',
      success: (res) => {
        const list = res.data?.data || []
        this.setData({ list, count: list.length })
      },
      fail: () => this.setData({ list: [], count: 0 }),
      complete: () => this.setData({ loading: false })
    })
  },
  fetchClasses() {
    const req = require('../../../utils/request')
    req.get('/wechat/api/teacher/classes/list', null, { loading: true, showError: false }).then((data) => {
      let classes = Array.isArray(data) ? data : []
      if (!classes.length) classes = [ { id: 'class-100', name: '高一1班' }, { id: 'class-101', name: '高一2班' } ]
      const firstId = classes[0] ? (classes[0].id || classes[0].classId || '') : ''
      this.setData({ classes, classIndex: 0, classId: firstId })
    })
  }
})