<template>
  <a-config-provider :locale="zhCN">
  <a-layout style="min-height: 100vh">
      <a-layout-sider collapsible v-if="!isLoginPage" :collapsed="siderCollapsed" @collapse="onSiderCollapse" style="background:#f5f5f5;border-right:1px solid #eaecef;">
        <div class="logo"><i class="brand-logo"></i></div>
        <a-menu theme="light" mode="inline" :inline-collapsed="siderCollapsed" :style="{ background: '#f5f5f5' }">
          <a-menu-item key="home" v-if="!homeFromDB" @click="$router.push('/home')">
            <span class="menu-title"><i class="menu-icon home"></i><span v-if="!siderCollapsed">首页</span></span>
          </a-menu-item>
        <!-- 顶级菜单（动态）：如考勤管理等 -->
        <a-menu-item v-for="t in topMenus" :key="t.key" v-perm="t.perm" @click="$router.push(t.path)">
          <span class="menu-title"><i :class="['menu-icon', iconClassFor(t)]"></i><span v-if="!siderCollapsed">{{ t.label }}</span></span>
        </a-menu-item>
        <a-sub-menu key="base">
          <span slot="title" class="menu-title"><i class="menu-icon base"></i><span v-if="!siderCollapsed">基础平台</span></span>
          <!-- 动态渲染基础平台菜单，自动包含后续新增的功能点 -->
          <a-menu-item v-for="m in baseMenus" :key="m.key" v-perm="m.perm" @click="$router.push(m.path)">
            {{ m.label }}
          </a-menu-item>
        </a-sub-menu>
        <a-sub-menu key="student">
          <span slot="title" class="menu-title"><i class="menu-icon student"></i><span v-if="!siderCollapsed">学员中心</span></span>
          <a-menu-item key="student-profile" v-perm="'student:view'" @click="$router.push('/student/profile')">学员档案</a-menu-item>
          <a-menu-item key="student-audition" v-perm="'student:view'" @click="$router.push('/student/audition')">试听管理</a-menu-item>
          <a-menu-item key="student-enroll" v-perm="'student:view'" @click="$router.push('/student/enroll')">报名管理</a-menu-item>
          <a-menu-item key="student-guardian-perm" v-perm="'student:view'" @click="$router.push('/student/guardian-perm')">监护人权限</a-menu-item>
          <a-menu-item key="student-referral" v-perm="'student:view'" @click="$router.push('/student/referral')">转介绍管理</a-menu-item>
          <a-menu-item key="student-change" v-perm="'student:view'" @click="$router.push('/student/change')">学员异动管理</a-menu-item>
          <a-menu-item key="student-class-select" v-perm="'class:select'" @click="$router.push('/student/class-select')">选班申请</a-menu-item>
          <a-menu-item key="student-grade" v-perm="'menu:student:grade'" @click="$router.push('/student/grade')">成绩管理</a-menu-item>
        </a-sub-menu>
        <a-sub-menu key="finance">
          <span slot="title" class="menu-title"><i class="menu-icon finance"></i><span v-if="!siderCollapsed">财务管理</span></span>
          <!-- 动态渲染财务管理子菜单（例如收费管理） -->
          <a-menu-item v-for="m in financeMenus" :key="m.key" v-perm="m.perm" @click="$router.push(m.path)">
            {{ m.label }}
          </a-menu-item>
        </a-sub-menu>
        <a-sub-menu key="report" v-perm="'menu:report'">
          <span slot="title" class="menu-title"><i class="menu-icon report"></i><span v-if="!siderCollapsed">报表管理</span></span>
          <a-menu-item v-for="m in reportMenus" :key="m.key" v-perm="m.perm" @click="$router.push(m.path)">
            {{ m.label }}
          </a-menu-item>
        </a-sub-menu>
        
        <a-sub-menu key="course">
          <span slot="title" class="menu-title"><i class="menu-icon course"></i><span v-if="!siderCollapsed">课程与班级中心</span></span>
          <a-menu-item key="course-manage" v-perm="'course:view'" @click="$router.push('/course/manage')">课程管理</a-menu-item>
          <a-menu-item key="course-textbook" v-perm="'course:view'" @click="$router.push('/course/textbook')">教材与教具管理</a-menu-item>
          <a-menu-item key="course-schedule" v-perm="['course:schedule','menu:course:schedule']" @click="$router.push('/course/schedule')">排课管理</a-menu-item>
          <a-menu-item key="course-schedule-view" v-perm="['course:schedule','menu:course:schedule']" @click="$router.push('/course/schedule-view')">课表查询</a-menu-item>
        </a-sub-menu>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header v-if="!isLoginPage" style="background: #fff; display:flex; align-items:center; justify-content:space-between;">
        <span>教务管理后台</span>
        <div>
          <a-dropdown placement="bottomRight">
            <a-button>
              消息 📩<a-badge :count="unreadMsgCount" style="margin-left:6px" />
            </a-button>
            <a-menu slot="overlay">
              <a-menu-item v-for="m in headerMessages" :key="m.id" @click="onHeaderMessageClick(m)">
                <span :style="{ color: m.read ? '#999' : '#333' }">{{ m.text }}</span>
              </a-menu-item>
              <a-menu-divider />
              <a-menu-item @click="markAllHeaderRead">全部已读</a-menu-item>
            </a-menu>
          </a-dropdown>
          <a-divider type="vertical" />
          <!-- 合并：点击“张三”打开下拉层，竖排显示个人信息与操作项（使用标准菜单overlay，保证点击事件正常） -->
          <a-dropdown placement="bottomRight" :trigger="['click']" :visible="userDropdownVisible" @visibleChange="onUserDropdownVisibleChange">
            <a-button type="link" style="margin-right:8px">{{ (currentUser && (currentUser.name || currentUser.username)) || '张三' }}</a-button>
            <a-menu slot="overlay" :selectable="false" @click="onUserMenuClick">
              <a-menu-item disabled>
                <div style="padding:8px 12px; max-width:260px">
                  <a-descriptions size="small" :column="1" bordered>
                    <a-descriptions-item label="姓名">{{ (currentUser && currentUser.name) || '张三' }}</a-descriptions-item>
                    <a-descriptions-item label="用户名">{{ (currentUser && currentUser.username) || 'zhangsan' }}</a-descriptions-item>
                    <a-descriptions-item label="状态">{{ (currentUser && currentUser.status) || 'enabled' }}</a-descriptions-item>
                    <a-descriptions-item v-if="(currentUser && (currentUser.roleName || currentUser.roleId))" label="角色">
                      {{ currentUser.roleName || currentUser.roleId }}
                    </a-descriptions-item>
                  </a-descriptions>
                </div>
              </a-menu-item>
              <a-menu-divider />
              <a-menu-item key="changePwd" @click="openChangePwd">修改密码</a-menu-item>
              <a-menu-divider />
              <a-menu-item key="logout" @click="logout">退出登录</a-menu-item>
            </a-menu>
          </a-dropdown>
        </div>
      </a-layout-header>
      <a-layout-content style="margin:16px">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
  
  <!-- 修改密码弹窗（放在根组件内，避免多模板块） -->
  <a-modal
    ref="changePwdModal"
    title="修改密码"
    :visible="changePwd.visible"
    :confirmLoading="changePwd.loading"
    @ok="doChangePwd"
    @cancel="changePwd.visible = false"
    okText="保存"
    cancelText="取消"
    :getContainer="() => document.body"
    :forceRender="true"
    :width="520"
  >
    <a-form layout="horizontal" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
      <a-form-item label="用户名">
        <a-input v-model="changePwd.username" placeholder="例如：admin 或 员工用户名" />
      </a-form-item>
      <a-form-item label="旧密码">
        <a-input type="password" v-model="changePwd.oldPassword" />
      </a-form-item>
      <a-form-item label="新密码">
        <a-input type="password" v-model="changePwd.newPassword" />
      </a-form-item>
      <a-form-item label="确认新密码">
        <a-input type="password" v-model="changePwd.confirmPassword" />
      </a-form-item>
    </a-form>
  </a-modal>
  </a-config-provider>
</template>

<script>
import zhCN from 'ant-design-vue/lib/locale-provider/zh_CN'
import { Modal } from 'ant-design-vue'
export default {
  name: 'App'
  , methods: {
    openChangePwd() {
      // 自动填充当前用户用户名（若存在）
      if (this.currentUser && this.currentUser.username && !this.changePwd.username) {
        this.changePwd.username = this.currentUser.username
      }
      // 使用全局确认弹窗机制渲染修改密码表单，绕过局部 <a-modal> 注入问题
      try {
        const h = this.$createElement
        const form = h('a-form', { props: { layout: 'horizontal', labelCol: { span: 6 }, wrapperCol: { span: 16 } } }, [
          h('a-form-item', { props: { label: '用户名' } }, [
            h('a-input', {
              domProps: { value: this.changePwd.username },
              on: { input: e => { this.changePwd.username = e && e.target ? e.target.value : e } }
            })
          ]),
          h('a-form-item', { props: { label: '旧密码' } }, [
            h('a-input', {
              props: { type: 'password' },
              domProps: { value: this.changePwd.oldPassword },
              on: { input: e => { this.changePwd.oldPassword = e && e.target ? e.target.value : e } }
            })
          ]),
          h('a-form-item', { props: { label: '新密码' } }, [
            h('a-input', {
              props: { type: 'password' },
              domProps: { value: this.changePwd.newPassword },
              on: { input: e => { this.changePwd.newPassword = e && e.target ? e.target.value : e } }
            })
          ]),
          h('a-form-item', { props: { label: '确认新密码' } }, [
            h('a-input', {
              props: { type: 'password' },
              domProps: { value: this.changePwd.confirmPassword },
              on: { input: e => { this.changePwd.confirmPassword = e && e.target ? e.target.value : e } }
            })
          ])
        ])
        Modal.confirm({
          title: '修改密码',
          content: form,
          width: 520,
          okText: '保存',
          cancelText: '取消',
          maskClosable: false,
          onOk: () => this.doChangePwd(),
          onCancel: () => {
            // no-op; 保留输入内容以便下次继续
          }
        })
      } catch (e) {
        console.error('[openChangePwd] fallback confirm render failed', e)
        this.$message && this.$message.error('无法打开弹窗，请稍后重试')
      }
    },
    onUserDropdownVisibleChange(v) {
      this.userDropdownVisible = v
      // 当下拉层关闭且存在待打开标志时，再显示弹窗，避免动画/遮罩竞争
      if (!v && this.pendingOpenChangePwd) {
        this.pendingOpenChangePwd = false
        this.changePwd.visible = true
        this.$nextTick(() => { try { console.log('[openChangePwd] modal opened after dropdown close', this.$refs && this.$refs.changePwdModal) } catch (e) {} })
      }
    },
    onUserMenuClick(e) {
      const key = (e && e.key) || e
      const dom = e && e.domEvent
      if (dom && dom.stopPropagation) dom.stopPropagation()
      if (dom && dom.preventDefault) dom.preventDefault()
      if (key === 'changePwd') this.openChangePwd()
      if (key === 'logout') this.logout()
    },
    async doChangePwd() {
      const u = (this.changePwd.username || '').trim()
      const op = (this.changePwd.oldPassword || '').trim()
      const np = (this.changePwd.newPassword || '').trim()
      const cp = (this.changePwd.confirmPassword || '').trim()
      if (!u || !op || !np) { this.$message && this.$message.warning('请填写完整信息'); return }
      if (np !== cp) { this.$message && this.$message.error('两次输入的新密码不一致'); return }
      this.changePwd.loading = true
      try {
        const res = await fetch('/api/auth/password/change', {
          method: 'POST', headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ username: u, oldPassword: op, newPassword: np })
        })
        const json = await res.json().catch(() => ({}))
        if (json && (json.code === 200 || json.code === 'ok')) {
          this.$message && this.$message.success('密码修改成功')
          this.changePwd.visible = false
          this.changePwd.username = ''
          this.changePwd.oldPassword = ''
          this.changePwd.newPassword = ''
          this.changePwd.confirmPassword = ''
        } else {
          const msg = (json && (json.msg || json.message)) || '修改失败'
          this.$message && this.$message.error(msg)
        }
      } catch (e) {
        this.$message && this.$message.error('网络错误或服务器不可用')
      } finally { this.changePwd.loading = false }
    },
    logout() {
      // 触发后端登出（可选）
      fetch('/api/auth/logout', { method: 'POST' }).catch(() => {})
      // 清理本地登录信息
      localStorage.removeItem('AUTH_TOKEN')
      localStorage.removeItem('__USER__')
      localStorage.removeItem('__PERMS__')
      window.__PERMS__ = []
      this.$message && this.$message.success('已退出登录')
      this.$router.replace('/login')
    },
    async loadPermMenus() {
      try {
        const res = await fetch('/api/base/perm/catalog')
        const json = await res.json()
        if (json && json.code === 200) {
          const menus = (json.data && json.data.menus) || []
          // 顶级菜单：menu:<code> 例如 menu:attendance -> /attendance
          const tops = menus
            .filter(it => {
              const val = String(it.value || '')
              return val.startsWith('menu:') && !val.startsWith('menu:base') && !val.includes(':', 5) && val !== 'menu:finance'
            })
            .map(it => ({ key: it.value, label: it.label || it.value, path: this.routeByTop(it.value), perm: it.value }))
            .filter(it => !!it.path)
          this.topMenus = tops.length ? tops : this.fallbackTopMenus()
          this.homeFromDB = tops.some(it => it.path === '/home')
          const financeItems = menus
            .filter(it => String(it.value || '').startsWith('menu:finance:'))
            .map(it => ({ key: it.value, label: it.label || it.value, path: this.routeByFinance(it.value), perm: it.value }))
            .filter(it => !!it.path)
          this.financeMenus = this.mergeMenus(financeItems, this.fallbackFinanceMenus())
          const reportItems = menus
            .filter(it => String(it.value || '').startsWith('menu:report:'))
            .map(it => ({ key: it.value, label: it.label || it.value, path: this.routeByReport(it.value), perm: it.value }))
            .filter(it => !!it.path)
          this.reportMenus = reportItems.length ? reportItems : this.fallbackReportMenus()
          const items = menus
            .filter(it => {
              const val = String(it.value || '')
              return val.startsWith('menu:base:') && val !== 'menu:base:staff'
            })
            .map(it => ({ key: it.value, label: it.label || it.value, path: this.routeByMenu(it.value), perm: [it.value, 'menu:base'] }))
            .filter(it => !!it.path)
          this.baseMenus = this.mergeMenus(items, this.fallbackBaseMenus())
        } else {
          this.topMenus = this.fallbackTopMenus()
          this.financeMenus = this.fallbackFinanceMenus()
          this.reportMenus = this.fallbackReportMenus()
          this.baseMenus = this.fallbackBaseMenus()
          this.homeFromDB = false
        }
      } catch (e) {
        this.topMenus = this.fallbackTopMenus()
        this.financeMenus = this.fallbackFinanceMenus()
        this.reportMenus = this.fallbackReportMenus()
        this.baseMenus = this.fallbackBaseMenus()
        this.homeFromDB = false
      }
    },
    mergeMenus(primary, fallback) {
      // 将接口返回的菜单与兜底菜单合并，避免缺项（如教师管理）未出现在接口时消失
      const map = new Map()
      ;(primary || []).forEach(m => { if (m && m.key) map.set(m.key, m) })
      ;(fallback || []).forEach(m => { if (m && m.key && !map.has(m.key)) map.set(m.key, m) })
      return Array.from(map.values())
    },
    routeByTop(val) {
      // 通用规则：menu:<code> -> /<code>，例如 menu:attendance -> /attendance
      const prefix = 'menu:'
      const basePrefix = 'menu:base'
      const s = String(val)
      if (s.startsWith(prefix) && !s.startsWith(basePrefix)) {
        const code = s.slice(prefix.length)
        const path = code === 'dashboard' ? '/home' : `/${code}`
        const matched = this.$router && this.$router.match ? this.$router.match(path) : null
        if (matched && matched.matched && matched.matched.length) return path
        return null
      }
      return null
    },
    routeByMenu(val) {
      // 通用规则：menu:base:<code> -> /base/<code>
      const prefix = 'menu:base:'
      if (String(val).startsWith(prefix)) {
        const code = String(val).slice(prefix.length)
        const path = `/base/${code}`
        // 仅返回已注册的有效路由，避免跳转报错
        const matched = this.$router && this.$router.match ? this.$router.match(path) : null
        if (matched && matched.matched && matched.matched.length) return path
        return null
      }
      return null
    },
    routeByFinance(val) {
      // 通用规则：menu:finance:<code[:sub]...> -> /finance/<code[/sub]...>
      const prefix = 'menu:finance:'
      if (String(val).startsWith(prefix)) {
        const code = String(val).slice(prefix.length).replace(/:/g, '/')
        const path = `/finance/${code}`
        const matched = this.$router && this.$router.match ? this.$router.match(path) : null
        if (matched && matched.matched && matched.matched.length) return path
        return null
      }
      return null
    },
    routeByReport(val) {
      // 通用规则：menu:report:<code> -> /report/<code>
      const prefix = 'menu:report:'
      if (String(val).startsWith(prefix)) {
        const code = String(val).slice(prefix.length).replace(/:/g, '/')
        const path = `/report/${code}`
        const matched = this.$router && this.$router.match ? this.$router.match(path) : null
        if (matched && matched.matched && matched.matched.length) return path
        return null
      }
      return null
    },
    fallbackTopMenus() {
      return [
        { key: 'menu:attendance', label: '签到统计', path: '/attendance', perm: 'menu:attendance' }
      ]
    },
    fallbackFinanceMenus() {
      return [
        { key: 'menu:finance:settlement:fee', label: '收费管理', path: '/finance/settlement/fee', perm: 'menu:finance:settlement:fee' },
        { key: 'menu:finance:settlement:renew', label: '续费管理', path: '/finance/settlement/renew', perm: 'menu:finance:settlement:renew' },
        { key: 'menu:finance:settlement:refund', label: '退费管理', path: '/finance/settlement/refund', perm: 'menu:finance:settlement:refund' },
        { key: 'menu:finance:settlement:approver-config', label: '审批人配置', path: '/finance/settlement/approver-config', perm: 'menu:finance:settlement:approver-config' },
        // 发票管理子菜单
        { key: 'menu:finance:invoice:apply', label: '开票申请', path: '/finance/invoice/apply', perm: 'menu:finance:invoice:apply' },
        { key: 'menu:finance:invoice:manage', label: '发票管理', path: '/finance/invoice/manage', perm: 'menu:finance:invoice:manage' },
        { key: 'menu:finance:invoice:stat', label: '开票统计', path: '/finance/invoice/stat', perm: 'menu:finance:invoice:stat' },
        { key: 'menu:finance:teacher-fee:rule', label: '课时费规则', path: '/finance/teacher-fee/rule', perm: 'menu:finance:teacher-fee:rule' },
        { key: 'menu:finance:teacher-fee:trial', label: '课时费试算', path: '/finance/teacher-fee/trial', perm: 'menu:finance:teacher-fee:trial' }
      ]
    },
    fallbackReportMenus() {
      return [
        { key: 'menu:report:enroll', label: '招生报表', path: '/report/enroll', perm: 'menu:report:enroll' },
        { key: 'menu:report:teaching', label: '教学报表', path: '/report/teaching', perm: 'menu:report:teaching' },
        { key: 'menu:report:finance', label: '财务报表', path: '/report/finance', perm: 'menu:report:finance' },
        { key: 'menu:report:audition', label: '试听转化', path: '/report/audition', perm: 'menu:report:audition' }
      ]
    },
    fallbackBaseMenus() {
      return [
        { key: 'menu:base:campus', label: '校区管理', path: '/base/campus', perm: ['menu:base:campus','menu:base'] },
        { key: 'menu:base:dept', label: '部门管理', path: '/base/dept', perm: ['menu:base:dept','menu:base'] },
        { key: 'menu:base:role', label: '角色与权限', path: '/base/role', perm: ['menu:base:role','menu:base'] },
        { key: 'menu:base:user', label: '用户管理', path: '/base/user', perm: ['menu:base:user','menu:base'] },
        { key: 'menu:base:perm', label: '权限目录管理', path: '/base/perm', perm: ['menu:base:perm','menu:base'] },
        { key: 'menu:base:teacher', label: '教师管理', path: '/base/teacher', perm: ['menu:base:teacher','menu:base'] },
        { key: 'menu:base:classroom', label: '教室管理', path: '/base/classroom', perm: ['menu:base:classroom','menu:base'] },
        { key: 'menu:base:holiday', label: '节假日管理', path: '/base/holiday', perm: ['menu:base:holiday','menu:base'] },
        { key: 'menu:base:subject-dict', label: '科目维护', path: '/base/subject-dict', perm: ['menu:base:subject-dict','menu:base'] },
          { key: 'menu:base:class-dict', label: '班级管理', path: '/base/class-dict', perm: ['menu:base:class-dict','menu:base'] },
        { key: 'menu:base:grade-dict', label: '年级维护', path: '/base/grade-dict', perm: ['menu:base:grade-dict','menu:base'] }
      ]
    },
    async markAllHeaderRead() {
      try {
        const username = (this.currentUser && this.currentUser.username) ? this.currentUser.username : 'admin'
        const res = await fetch('/api/notify/message/read-all', {
          method: 'POST', headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ username })
        })
        const json = await res.json()
        if (json && json.code === 200) {
          this.headerMessages = (this.headerMessages || []).map(m => ({ ...m, read: true }))
          this.$message && this.$message.success('消息已全部标记为已读')
        } else {
          this.$message && this.$message.error((json && json.msg) || '标记已读失败')
        }
      } catch(e) {
        this.$message && this.$message.error(e.message || '网络错误，标记已读失败')
      }
    }
    , async onHeaderMessageClick(m) {
      if (!m) return
      const text = m.text || '消息通知'
      try {
        const res = await fetch('/api/notify/message/read', {
          method: 'POST', headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ id: m.id })
        })
        const json = await res.json()
        if (json && json.code === 200) {
          m.read = true
          if (this.$notification && this.$notification.open) {
            this.$notification.open({ message: '消息通知', description: text, duration: 3 })
          } else if (this.$message && this.$message.info) {
            this.$message.info(text)
          }
        } else {
          this.$message && this.$message.error((json && json.msg) || '标记已读失败')
        }
      } catch(e) {
        this.$message && this.$message.error(e.message || '网络错误，标记已读失败')
      }
    }
    , async loadHeaderMessages() {
      try {
        const username = (this.currentUser && this.currentUser.username) ? this.currentUser.username : 'admin'
        const res = await fetch(`/api/notify/message/list?username=${encodeURIComponent(username)}`)
        const json = await res.json()
        if (json && json.code === 200) {
          const arr = Array.isArray(json.data) ? json.data : []
          this.headerMessages = arr.map(it => ({ id: it.id, text: it.text, read: !!it.read }))
        }
      } catch(e) {
        // 保持原本静态数据不变
      }
    }
    , onSiderCollapse(v) {
      this.siderCollapsed = !!v
    }
    , iconClassFor(t) {
      const k = String(t.key || t.perm || '').toLowerCase()
      if (k.includes('attendance')) return 'attendance'
      if (k.includes('teacher-fee')) return 'teacher-fee'
      if (k.includes('report')) return 'report'
      if (k.includes('finance')) return 'finance'
      if (k.includes('base')) return 'base'
      if (k.includes('student')) return 'student'
      if (k.includes('course')) return 'course'
      return 'default'
    }
  },
  watch: {
    'changePwd.visible'(v) {
      try {
        console.log('[changePwd.visible]', v)
        if (v) {
          this.$nextTick(() => {
            try {
              const wrap = document.querySelector('.ant-modal-wrap')
              const mask = document.querySelector('.ant-modal-mask')
              console.log('[diagnose] wrap:', !!wrap, wrap)
              console.log('[diagnose] mask:', !!mask, mask)
              console.log('[diagnose] body class contains ant-modal-open:', document.body.classList.contains('ant-modal-open'))
            } catch(e) {}
          })
        }
      } catch (e) {}
    }
  },
  data() {
    return { topMenus: [], financeMenus: [], reportMenus: [], baseMenus: [], zhCN, userDropdownVisible: false, headerMessages: [] , changePwd: { visible: false, username: '', oldPassword: '', newPassword: '', confirmPassword: '', loading: false }, homeFromDB: false, siderCollapsed: false }
  },
  created() {
    this.loadPermMenus()
    if (this.currentUser && this.currentUser.username) {
      this.changePwd.username = this.currentUser.username
    }
    this.loadHeaderMessages()
    },
  computed: {
    isLoginPage() {
      return this.$route && this.$route.path === '/login'
    }
    , unreadMsgCount() {
      return this.headerMessages.filter(m => !m.read).length
    }
    , currentUser() {
      try {
        const s = localStorage.getItem('__USER__')
        const u = s ? JSON.parse(s) : null
        return u || { name: '张三', username: 'zhangsan', status: 'enabled' }
      } catch (e) {
        return { name: '张三', username: 'zhangsan', status: 'enabled' }
      }
    }
  }
}
</script>

<style>
.logo { text-align:center; padding: 16px; }
.brand-logo { display:inline-block; width:32px; height:32px; background-size:contain; background-repeat:no-repeat; background-image: url('data:image/svg+xml;utf8,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2232%22 height=%2232%22 viewBox=%220 0 24 24%22 fill=%22none%22 stroke=%22%232f3b4c%22 stroke-width=%222%22 stroke-linecap=%22round%22 stroke-linejoin=%22round%22><rect x=%223%22 y=%223%22 width=%2218%22 height=%2218%22 rx=%223%22 fill=%22%23f5f5f5%22/><path d=%22M12 5l7 3-7 3-7-3 7-3z%22 stroke=%22%2307c160%22/><path d=%22M5 12l7 3 7-3%22/><path d=%22M7 16v2a2 2 0 0 0 2 2h6a2 2 0 0 0 2-2v-2%22/></svg>'); }
.ant-menu-light.ant-menu-inline, .ant-menu-light.ant-menu-vertical { background: #f5f5f5; }
.ant-menu-light .ant-menu-submenu-title { background: transparent; }
.ant-layout-sider { background: #f5f5f5 !important; }
.ant-layout-sider-children { background: #f5f5f5 !important; }
.ant-layout-sider-trigger { background: #f5f5f5 !important; border-top: 1px solid #eaecef; color: #6b7c93; }
.ant-layout-sider-trigger .anticon { color: #6b7c93; }
.ant-menu-light .ant-menu-item-selected, .ant-menu-light .ant-menu-submenu-selected > .ant-menu-submenu-title { background: #eaeaea; }
.menu-title { display: inline-flex; align-items: center; gap: 8px; }
.menu-icon { display:inline-block; width:18px; height:18px; background-size:contain; background-repeat:no-repeat; }
.menu-icon.home { background-image: url('data:image/svg+xml;utf8,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2218%22 height=%2218%22 viewBox=%220 0 24 24%22 fill=%22none%22 stroke=%22%236b7c93%22 stroke-width=%222%22 stroke-linecap=%22round%22 stroke-linejoin=%22round%22><path d=%22M3 9l9-7 9 7%22/><path d=%22M9 22V12h6v10%22/></svg>'); }
.menu-icon.finance { background-image: url('data:image/svg+xml;utf8,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2218%22 height=%2218%22 viewBox=%220 0 24 24%22 fill=%22none%22 stroke=%22%2307c160%22 stroke-width=%222%22 stroke-linecap=%22round%22 stroke-linejoin=%22round%22><path d=%22M3 3h18v4H3z%22/><path d=%22M3 10h18v11H3z%22/><path d=%22M7 14h3v5H7z%22/><path d=%22M14 12h3v7h-3z%22/></svg>'); }
.menu-icon.report { background-image: url('data:image/svg+xml;utf8,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2218%22 height=%2218%22 viewBox=%220 0 24 24%22 fill=%22none%22 stroke=%22%236b7c93%22 stroke-width=%222%22 stroke-linecap=%22round%22 stroke-linejoin=%22round%22><rect x=%223%22 y=%223%22 width=%2218%22 height=%2218%22 rx=%222%22/><path d=%22M7 9h10M7 13h10M7 17h6%22/></svg>'); }
.menu-icon.base { background-image: url('data:image/svg+xml;utf8,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2218%22 height=%2218%22 viewBox=%220 0 24 24%22 fill=%22none%22 stroke=%22%236b7c93%22 stroke-width=%222%22 stroke-linecap=%22round%22 stroke-linejoin=%22round%22><circle cx=%229%22 cy=%229%22 r=%223%22/><circle cx=%2219%22 cy=%225%22 r=%223%22/><circle cx=%2219%22 cy=%2219%22 r=%223%22/><path d=%22M11.5 10.5L16 8M11.5 13.5L16 16%22/></svg>'); }
.menu-icon.student { background-image: url('data:image/svg+xml;utf8,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2218%22 height=%2218%22 viewBox=%220 0 24 24%22 fill=%22none%22 stroke=%22%236b7c93%22 stroke-width=%222%22 stroke-linecap=%22round%22 stroke-linejoin=%22round%22><path d=%22M12 2l9 4-9 4-9-4 9-4z%22/><path d=%22M4 14l8 4 8-4%22/><path d=%22M4 10v4%22/><path d=%22M20 10v4%22/></svg>'); }
.menu-icon.course { background-image: url('data:image/svg+xml;utf8,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2218%22 height=%2218%22 viewBox=%220 0 24 24%22 fill=%22none%22 stroke=%22%236b7c93%22 stroke-width=%222%22 stroke-linecap=%22round%22 stroke-linejoin=%22round%22><path d=%22M4 4h16v16H4z%22/><path d=%22M8 8h8v8H8z%22/></svg>'); }
.menu-icon.attendance { background-image: url('data:image/svg+xml;utf8,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2218%22 height=%2218%22 viewBox=%220 0 24 24%22 fill=%22none%22 stroke=%22%2307c160%22 stroke-width=%222%22 stroke-linecap=%22round%22 stroke-linejoin=%22round%22><path d=%22M9 11l3 3L22 4%22/><path d=%22M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11%22/></svg>'); }
.menu-icon[class*='teacher-fee'] { background-image: url('data:image/svg+xml;utf8,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2218%22 height=%2218%22 viewBox=%220 0 24 24%22 fill=%22none%22 stroke=%22%2307c160%22 stroke-width=%222%22 stroke-linecap=%22round%22 stroke-linejoin=%22round%22><circle cx=%2212%22 cy=%2212%22 r=%2210%22/><path d=%22M12 8v8M8 12h8%22/></svg>'); }
.menu-icon.default { background-image: url('data:image/svg+xml;utf8,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2218%22 height=%2218%22 viewBox=%220 0 24 24%22 fill=%22none%22 stroke=%22%236b7c93%22 stroke-width=%222%22 stroke-linecap=%22round%22 stroke-linejoin=%22round%22><path d=%22M12 2l7 7-7 7-7-7 7-7z%22/></svg>'); }
</style>