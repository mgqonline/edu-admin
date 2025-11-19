Component({
  data: {
    selected: 0,
    role: 'student',
    list: [],
    submenuVisible: false,
    submenuItems: [],
    submenuUrls: []
  },
  pageLifetimes: {
    show() {
      // 页面展示时再根据当前路由更新选中态，避免组件attached过早触发拿不到当前页
      this.updateSelectedByRoute();
      if (this.data && this.data.role === 'teacher') {
        this.buildSubmenuByCurrent();
      }
    }
  },
  attached() {
    const app = getApp();
    const role = wx.getStorageSync('ROLE') || (app.globalData && app.globalData.role) || 'student';
    this.setData({ role });
    this.initTabs(role);
  },
  methods: {
    initTabs(role) {
      // 组件目录为 custom-tab-bar，与 assets 同级，使用相对路径 ../assets
      const greenService = 'data:image/svg+xml;utf8,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2224%22 height=%2224%22 viewBox=%220 0 24 24%22 fill=%22%2307c160%22><path d=%22M9.4 10.6a3 3 0 1 1 4.2 4.2l-1.1 1.1a3 3 0 1 1-4.2-4.2l1.1-1.1zM4 4h4l1 2-2 2-2-1V4zm12-1 4 4-2 2-3-3 1-3zm-14 11 3-3 2 2-3 3-2-2z%22/></svg>'
      const greenManage = 'data:image/svg+xml;utf8,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2224%22 height=%2224%22 viewBox=%220 0 24 24%22 fill=%22%2307c160%22><path d=%22M4 5h8a3 3 0 0 1 3 3v11H7a3 3 0 0 1-3-3V5zm10 0h6v14h-6V8a3 3 0 0 0-3-3z%22/></svg>'
      const greenStudy = 'data:image/svg+xml;utf8,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2224%22 height=%2224%22 viewBox=%220 0 24 24%22 fill=%22%2307c160%22><path d=%22M2 6l10-4 10 4-10 4L2 6zm2 5l8 3 8-3v7l-8 3-8-3v-7z%22/></svg>'
      const greenProfile = 'data:image/svg+xml;utf8,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2224%22 height=%2224%22 viewBox=%220 0 24 24%22 fill=%22%2307c160%22><path d=%22M12 12a5 5 0 1 0-0.001-10.001A5 5 0 0 0 12 12zm-9 9c0-4.418 4.582-8 9-8s9 3.582 9 8H3z%22/></svg>'
      const teacherTabs = [
        { pagePath: '/pages/tab/service/index', text: '教务事务', iconPath: '../assets/tab-service.png', selectedIconPath: greenService },
        { pagePath: '/pages/tab/manage/index', text: '教学管理', iconPath: '../assets/tab-schedule.png', selectedIconPath: greenManage },
        { pagePath: '/pages/tab/interaction/index', text: '教学互动', iconPath: '../assets/tab-study.png', selectedIconPath: greenStudy },
        { pagePath: '/pages/tab/profile/index', text: '个人信息', iconPath: '../assets/tab-profile.png', selectedIconPath: greenProfile }
      ];
      const studentTabs = [
        { pagePath: '/pages/tab/service/index', text: '家校服务', iconPath: '../assets/tab-service.png', selectedIconPath: greenService },
        { pagePath: '/pages/tab/manage/index', text: '学情报告', iconPath: '../assets/tab-study.png', selectedIconPath: greenStudy },
        { pagePath: '/pages/tab/schedule/index', text: '学习追踪', iconPath: '../assets/tab-schedule.png', selectedIconPath: greenManage },
        { pagePath: '/pages/tab/profile/index', text: '学员档案', iconPath: '../assets/tab-profile.png', selectedIconPath: greenProfile }
      ];
      this.setData({ list: role === 'teacher' ? teacherTabs : studentTabs });
      // 初始化不强制更新，避免attached阶段getCurrentPages为空导致报错
      this.updateSelectedByRoute();
    },
    buildSubmenu(path) {
      if (this.data.role !== 'teacher') {
        this.setData({ submenuVisible: false, submenuItems: [], submenuUrls: [] });
        return;
      }
      let items = [], urls = []
      if (path === '/pages/tab/manage/index') {
        items = ['成绩管理','作业管理','签到管理','课堂记录'];
        urls = ['/pages/teacher/grades/index','/pages/teacher/homework/index','/pages/teacher/attendance/index','/pages/teacher/record/index']
      } else if (path === '/pages/tab/interaction/index') {
        items = ['班级通知','家长留言']; urls = ['/pages/teacher/notice/index','/pages/teacher/message/index']
      } else if (path === '/pages/tab/service/index') {
        items = ['我的课表','调课申请','教室申请','教学计划'];
        urls = ['/pages/teacher/schedule/index','/pages/teacher/adjust/index','/pages/teacher/room/index','/pages/teacher/plan/index']
      } else if (path === '/pages/tab/profile/index') {
        items = ['个人信息']; urls = ['/pages/teacher/profile/index']
      }
      this.setData({ submenuVisible: items.length > 0, submenuItems: items, submenuUrls: urls })
    },
    hideSubmenu() { this.setData({ submenuVisible: false }) },
    onSubmenuTap(e) {
      const idx = e.currentTarget.dataset.idx
      const url = this.data.submenuUrls[idx]
      this.setData({ submenuVisible: false })
      if (url === '__logout__') {
        try { wx.removeStorageSync('AUTH_TOKEN'); wx.removeStorageSync('ROLE') } catch (e) {}
        const app = getApp();
        if (app && app.globalData) app.globalData.role = 'student'
        wx.reLaunch({ url: '/pages/login/index' })
        return
      }
      if (url) wx.navigateTo({ url })
    },
    noop() {},
    updateSelectedByRoute() {
      const pages = getCurrentPages();
      if (!pages || pages.length === 0) {
        // 无页面时不更新选中态
        return;
      }
      const current = pages[pages.length - 1];
      if (!current || !current.route) {
        return;
      }
      const route = '/' + current.route;
      const idx = this.data.list.findIndex(i => i.pagePath === route);
      if (idx >= 0) this.setData({ selected: idx });
    },
    buildSubmenuByCurrent() {
      const pages = getCurrentPages();
      if (!pages || pages.length === 0) return;
      const current = pages[pages.length - 1];
      if (!current || !current.route) return;
      const route = '/' + current.route;
      this.buildSubmenu(route);
    },
    switchTab(e) {
      const path = e.currentTarget.dataset.path;
      if (!path) return;
      const idx = this.data.list.findIndex(i => i.pagePath === path)
      if (idx >= 0) this.setData({ selected: idx })
      if (this.data.role === 'teacher') this.buildSubmenu(path)
      wx.switchTab({ url: path, success: () => {}, fail: () => {
        wx.showToast({ title: '切换失败', icon: 'none' })
      }});
    }
  }
});