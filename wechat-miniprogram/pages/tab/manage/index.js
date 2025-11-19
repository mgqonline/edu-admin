const req = require('../../../utils/request')

Page({
  data: { role: 'student', studentId: '', course: '', type: '', score: '', submitting: false, homework: [], grades: [], avgScore: 0, pendingHomework: 0, submittedCount: 0 },
  onLoad() {
    const app = getApp();
    const role = wx.getStorageSync('ROLE') || (app.globalData && app.globalData.role) || 'student';
    this.setData({ role })
    if (role !== 'teacher') { this.loadHomework(); this.loadGrades(); }
  },
  onShow() {
    const app = getApp();
    const role = wx.getStorageSync('ROLE') || (app.globalData && app.globalData.role) || 'student';
    const tb = this.getTabBar && this.getTabBar();
    if (tb && typeof tb.setData === 'function') tb.setData({ selected: 1 });
    if (role !== 'teacher') { return }
  },
  onSubmit(e) {
    const { studentId, course, type, score } = e.detail.value || {}
    this.setData({ submitting: true })
    req.post('/wechat/api/teacher/grades/record', { studentId, course, type, score }, { loading: true }).then((data) => {
      wx.showToast({ title: '已登记', icon: 'success' })
      this.setData({ studentId: '', course: '', type: '', score: '' })
      this.setData({ submittedCount: this.data.submittedCount + 1 })
    }).finally(() => {
      this.setData({ submitting: false })
    })
  },
  loadHomework() {
    req.get('/wechat/api/student/homework/list', null, { transform: (d) => Array.isArray(d) ? d : null, defaultValue: [{ id: 1, title: '数学作业：练习册第3章', dueDate: '2025-11-15' }] }).then((list) => {
      this.setData({ homework: list, pendingHomework: list.length })
    })
  },
  loadGrades() {
    req.get('/wechat/api/student/grades/list', null, { transform: (d) => Array.isArray(d) ? d : null, defaultValue: [{ id: 1, course: '英语', type: '随堂测试', score: 88 }] }).then((list) => {
      const avg = list.length ? Math.round(list.reduce((s, i) => s + (Number(i.score) || 0), 0) / list.length) : 0
      this.setData({ grades: list, avgScore: avg })
    })
  },
  uploadPhoto(e) {
    const hwId = e.currentTarget.dataset.id
    wx.chooseImage({ count: 1, success: (r) => {
      const filePath = r.tempFilePaths[0]
      req.upload('/wechat/api/student/homework/upload', filePath, 'file', { homeworkId: hwId }, { loading: true }).then(() => wx.showToast({ title: '已上传', icon: 'success' }))
    }})
  }
});