Page({
  data: { homework: [], grades: [] },
  onLoad() {
    this.loadHomework();
    this.loadGrades();
  },
  loadHomework() {
    wx.request({
      url: 'http://localhost:8090/wechat/api/student/homework/list',
      success: (res) => this.setData({ homework: res.data?.data || [] }),
      fail: () => this.setData({ homework: [{ id: 1, title: '数学作业：练习册第3章', dueDate: '2025-11-15' }] })
    })
  },
  loadGrades() {
    wx.request({
      url: 'http://localhost:8090/wechat/api/student/grades/list',
      success: (res) => this.setData({ grades: res.data?.data || [] }),
      fail: () => this.setData({ grades: [{ id: 1, course: '英语', type: '随堂测试', score: 88 }] })
    })
  },
  uploadPhoto(e) {
    const hwId = e.currentTarget.dataset.id
    wx.chooseImage({ count: 1, success: (r) => {
      const filePath = r.tempFilePaths[0]
      wx.uploadFile({
        url: 'http://localhost:8090/wechat/api/student/homework/upload?studentId=1',
        filePath,
        name: 'file',
        formData: { homeworkId: hwId },
        success: () => wx.showToast({ title: '已上传', icon: 'success' }),
        fail: () => wx.showToast({ title: '上传失败', icon: 'error' })
      })
    }})
  }
})