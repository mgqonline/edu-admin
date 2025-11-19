<template>
  <div class="login-page">
    <div class="bg-decor">
      <canvas ref="bgCanvas" class="bg-stars"></canvas>
      <div class="meteors">
        <div class="meteor" v-for="m in meteors" :key="m.id" :style="m.style"></div>
      </div>
    </div>
    <div class="login-shell">
      <div class="hero-pane">
        <div class="hero-badge">EA</div>
        <div class="hero-title">Edu Admin 教务系统</div>
        <div class="hero-subtitle">高效、稳定的教务管理平台</div>
        <div class="hero-features">
          <div class="feat">一体化教务流程</div>
          <div class="feat">数据可视化与统计</div>
          <div class="feat">安全权限与审计</div>
        </div>
      </div>
      <div class="form-pane">
        <a-card :bordered="false" class="login-card">
          <div class="brand">
            <div class="brand-logo">EA</div>
            <div class="brand-title">Edu Admin</div>
            <div class="brand-subtitle">教务管理后台</div>
          </div>
          <a-form layout="vertical">
            <a-form-item label="用户名">
              <a-input v-model="form.username" placeholder="请输入用户名" @keyup.enter.native="onLogin" />
            </a-form-item>
            <a-form-item label="密码">
              <a-input-password v-model="form.password" placeholder="请输入密码" @keyup.enter.native="onLogin" />
            </a-form-item>
            <div class="form-extras">
              <a-checkbox v-model="remember">记住我</a-checkbox>
              <a href="javascript:void(0)" class="link" @click="openForgot">忘记密码</a>
            </div>
            <a-button type="primary" block :loading="submitting" @click="onLogin">登录</a-button>
          </a-form>
        </a-card>
      </div>
    </div>
    <a-modal v-model="forgotVisible" title="忘记密码 / 修改密码" :confirm-loading="changing" @ok="onSubmitForgot" @cancel="()=>{forgotVisible=false}">
      <a-form layout="vertical">
        <a-radio-group v-model="forgotMode" style="margin-bottom:12px;">
          <a-radio value="reset">找回密码（验证码）</a-radio>
          <a-radio value="change">修改密码（知道旧密码）</a-radio>
        </a-radio-group>
        <a-form-item label="用户名"><a-input v-model="forgot.username" placeholder="请输入用户名" /></a-form-item>
        <template v-if="forgotMode==='reset'">
          <a-form-item label="验证码">
            <a-input v-model="forgot.code" placeholder="请输入验证码" />
          </a-form-item>
          <a-button size="small" @click="sendCode" :disabled="sendingCode" style="margin-bottom:8px;">{{ sendingCode ? '发送中...' : '发送验证码' }}</a-button>
          <div v-if="lastCode" style="margin-top:4px;color:#888;">演示环境验证码：{{ lastCode }}（10分钟内有效）</div>
        </template>
        <template v-else>
          <a-form-item label="旧密码"><a-input-password v-model="forgot.oldPassword" placeholder="请输入旧密码" /></a-form-item>
        </template>
        <a-form-item label="新密码"><a-input-password v-model="forgot.newPassword" placeholder="请输入新密码" /></a-form-item>
        <div style="margin-top:8px;color:#888;">
          无法找回旧密码？可使用“找回密码（验证码）”方式；生产环境验证码将通过短信/邮件发送。
        </div>
      </a-form>
    </a-modal>
    <div class="footer-info">
      <span>{{ icpRecord }}</span>
      <span class="dot">·</span>
      <span>{{ companyName }}</span>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LoginPage',
  data() {
    return {
      form: { username: '', password: '' },
      submitting: false,
      remember: true,
      forgotVisible: false,
      forgot: { username: '', oldPassword: '', newPassword: '', code: '' },
      forgotMode: 'reset',
      changing: false,
      sendingCode: false,
      lastCode: '',
      icpRecord: 'ICP备案号：ICP10000891',
      companyName: '开发公司：示例科技有限公司',
      meteors: [],
      starField: [],
      starRAF: 0
    }
  },
  mounted() {
    this.initMeteors()
    this.initStars()
    window.addEventListener('resize', this.resizeStars)
  },
  beforeDestroy() {
    if (this.starRAF) cancelAnimationFrame(this.starRAF)
    window.removeEventListener('resize', this.resizeStars)
  },
  created() {
    try {
      const remembered = localStorage.getItem('__REMEMBER__') === '1'
      this.remember = remembered !== false
      const u = localStorage.getItem('__REMEMBER_USER__') || ''
      const pEnc = localStorage.getItem('__REMEMBER_PSW__') || ''
      let p = ''
      if (pEnc) {
        try { p = atob(pEnc) } catch(e) { p = '' }
      }
      if (remembered && u && p) {
        this.form.username = u
        this.form.password = p
      }
    } catch(e) {}
  },
  methods: {
    initMeteors() {
      const arr = []
      const count = 12
      for (let i = 0; i < count; i++) {
        const top = Math.round(Math.random() * 90 + 2)
        const delay = (Math.random() * 8).toFixed(2) + 's'
        const dur = (3 + Math.random() * 4).toFixed(2) + 's'
        const opacity = (0.6 + Math.random() * 0.4).toFixed(2)
        arr.push({ id: 'm' + i, style: { top: top + 'vh', left: '-20vw', opacity, animation: 'shoot ' + dur + ' linear ' + delay + ' infinite' } })
      }
      this.meteors = arr
    },
    initStars() {
      const c = this.$refs.bgCanvas
      if (!c) return
      const dpr = window.devicePixelRatio || 1
      c.width = Math.floor(window.innerWidth * dpr)
      c.height = Math.floor(window.innerHeight * dpr)
      const ctx = c.getContext('2d')
      ctx.scale(dpr, dpr)
      const stars = []
      const n = Math.floor((window.innerWidth * window.innerHeight) / 8000)
      for (let i = 0; i < n; i++) {
        const x = Math.random() * window.innerWidth
        const y = Math.random() * window.innerHeight
        const r = Math.random() * 1.5 + 0.2
        const tw = Math.random() * 1
        stars.push({ x, y, r, t: tw })
      }
      this.starField = stars
      const loop = () => {
        ctx.clearRect(0, 0, window.innerWidth, window.innerHeight)
        for (let i = 0; i < this.starField.length; i++) {
          const s = this.starField[i]
          s.t += 0.02
          const a = 0.6 + Math.sin(s.t) * 0.4
          ctx.globalAlpha = a
          ctx.fillStyle = '#ffffff'
          ctx.beginPath()
          ctx.arc(s.x, s.y, s.r, 0, Math.PI * 2)
          ctx.fill()
        }
        ctx.globalAlpha = 1
        this.starRAF = requestAnimationFrame(loop)
      }
      loop()
    },
    resizeStars() {
      if (!this.$refs.bgCanvas) return
      this.initStars()
    },
    openForgot() {
      this.forgotVisible = true
      this.forgot.username = this.form.username || ''
    },
    async sendCode() {
      if (!this.forgot.username) return this.$message && this.$message.warning('请填写用户名')
      this.sendingCode = true
      try {
        const res = await fetch('/api/auth/password/reset/request', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ username: this.forgot.username }) })
        const json = await res.json()
        if (json && json.code === 200) {
          const data = json.data || {}
          this.lastCode = data.code || ''
          this.$message && this.$message.success('验证码已发送（演示环境直接显示）')
        } else {
          this.$message && this.$message.error(json.message || '发送失败')
        }
      } catch(e) { this.$message && this.$message.error('发送失败') }
      this.sendingCode = false
    },
    async onSubmitForgot() {
      if (this.forgotMode === 'reset') {
        if (!this.forgot.username || !this.forgot.code || !this.forgot.newPassword) {
          return this.$message && this.$message.warning('请填写用户名、验证码与新密码')
        }
        this.changing = true
        try {
          const res = await fetch('/api/auth/password/reset/confirm', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ username: this.forgot.username, code: this.forgot.code, newPassword: this.forgot.newPassword }) })
          const json = await res.json()
          if (json && json.code === 200) {
            this.$message && this.$message.success('密码已重置，请使用新密码登录')
            this.forgotVisible = false
            this.form.username = this.forgot.username
          } else { this.$message && this.$message.error(json.message || '重置失败') }
        } catch(e) { this.$message && this.$message.error('重置失败') }
        this.changing = false
      } else {
        if (!this.forgot.username || !this.forgot.oldPassword || !this.forgot.newPassword) {
          return this.$message && this.$message.warning('请填写用户名、旧密码与新密码')
        }
        this.changing = true
        try {
          const res = await fetch('/api/auth/password/change', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ username: this.forgot.username, oldPassword: this.forgot.oldPassword, newPassword: this.forgot.newPassword }) })
          const json = await res.json()
          if (json && json.code === 200) {
            this.$message && this.$message.success('密码已更新，请使用新密码登录')
            this.forgotVisible = false
            this.form.username = this.forgot.username
          } else { this.$message && this.$message.error(json.message || '修改失败') }
        } catch(e) { this.$message && this.$message.error('网络错误，修改失败') }
        this.changing = false
      }
    },
    async onLogin() {
      if (!this.form.username || !this.form.password) {
        return this.$message && this.$message.warning('请输入用户名和密码')
      }
      this.submitting = true
      try {
        const res = await fetch('/api/auth/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(this.form)
        })
        const json = await res.json()
        if (json && json.code === 200) {
          const data = json.data || {}
          localStorage.setItem('AUTH_TOKEN', data.token || '')
          if (data.user) localStorage.setItem('__USER__', JSON.stringify(data.user))
          const perms = data.perms || []
          window.__PERMS__ = perms
          localStorage.setItem('__PERMS__', JSON.stringify(perms))
          if (this.remember) localStorage.setItem('__REMEMBER__', '1')
          if (this.remember) {
            try {
              localStorage.setItem('__REMEMBER_USER__', String(this.form.username||''))
              localStorage.setItem('__REMEMBER_PSW__', btoa(String(this.form.password||'')))
            } catch(e) {}
          }
          this.$message && this.$message.success('登录成功')
          this.$router.replace('/attendance')
        } else {
          this.$message && this.$message.error(json.message || '登录失败')
        }
      } catch (e) {
        this.$message && this.$message.error('登录失败')
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style>
.login-page {
  position: relative;
  height: 100vh;
  padding: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #eef3ff 0%, #f8fbff 50%, #f3f6ff 100%);
  box-sizing: border-box;
  overflow: hidden;
}
.bg-decor {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}
.bg-stars { position: absolute; inset: 0; width: 100%; height: 100%; }
.meteors { position: absolute; inset: 0; overflow: hidden; }
.meteor { position: absolute; width: 22vw; height: 2px; background: linear-gradient(90deg, rgba(255,255,255,0) 0%, rgba(255,255,255,0.9) 40%, rgba(255,255,255,0) 100%); filter: blur(0.4px); transform: rotate(-18deg); will-change: transform, opacity; }
@keyframes shoot { 0% { transform: translateX(0) translateY(0) rotate(-18deg); opacity: 0 } 5% { opacity: 1 } 100% { transform: translateX(130vw) translateY(28vh) rotate(-18deg); opacity: 0 } }
.bg-decor::before, .bg-decor::after {
  content: '';
  position: absolute;
  width: 320px;
  height: 320px;
  filter: blur(60px);
  border-radius: 50%;
  opacity: 0.45;
  animation: float 10s ease-in-out infinite;
}
.bg-decor::before {
  left: -80px;
  top: -40px;
  background: radial-gradient(circle at 30% 30%, #7da2ff 0%, transparent 60%);
}
.bg-decor::after {
  right: -60px;
  bottom: -40px;
  background: radial-gradient(circle at 70% 70%, #96e6a1 0%, transparent 60%);
  animation-delay: 2s;
}
@keyframes float {
  0%, 100% { transform: translateY(0) translateX(0); }
  50% { transform: translateY(10px) translateX(8px); }
}
.brand {
  text-align: center;
  margin-bottom: 8px;
}
.brand-logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 16px;
  color: #fff;
  font-weight: 700;
  letter-spacing: 1px;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #5b8cff 0%, #7da2ff 70%, #96e6a1 100%);
  box-shadow: 0 6px 20px rgba(91, 140, 255, 0.35);
}
.brand-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2d3d;
}
.brand-subtitle {
  font-size: 12px;
  color: #6b7c93;
}
</style>
<style>
.login-shell { width: 100%; max-width: 100%; display: grid; grid-template-columns: 1.2fr 0.8fr; gap: clamp(16px, 2vw, 32px); align-items: stretch; }
.hero-pane { position: relative; background: rgba(255,255,255,0.7); border-radius: 16px; padding: clamp(20px, 3vw, 32px); box-shadow: 0 10px 30px rgba(91,140,255,0.12); }
.hero-badge { display: inline-flex; align-items: center; justify-content: center; width: 64px; height: 64px; border-radius: 18px; color: #fff; font-weight: 700; background: linear-gradient(135deg, #5b8cff 0%, #7da2ff 70%, #96e6a1 100%); box-shadow: 0 8px 24px rgba(91,140,255,0.35); }
.hero-title { margin-top: 16px; font-size: 28px; font-weight: 700; color: #1f2d3d; }
.hero-subtitle { margin-top: 6px; font-size: 14px; color: #6b7c93; }
.hero-features { display: grid; grid-template-columns: repeat(auto-fit, minmax(160px, 1fr)); gap: clamp(8px, 1.5vw, 16px); margin-top: 20px; }
.feat { padding: 12px 14px; border-radius: 12px; background: #fff; color: #2f3b4c; box-shadow: 0 6px 18px rgba(0,0,0,0.06); }
.login-card { width: 100%; max-width: 520px; margin: 0 auto; box-shadow: 0 10px 30px rgba(0,0,0,0.06); border-radius: 16px; }
.form-extras { display:flex; justify-content: space-between; align-items: center; margin: 8px 0 12px; }
.link { color: #5b8cff; text-decoration: none; }
@media (max-width: 960px) { .login-shell { width: 100%; grid-template-columns: 1fr; } .login-card { width: 100%; max-width: 100%; } }
.footer-info { position: absolute; left: 0; right: 0; bottom: calc(env(safe-area-inset-bottom) + 8px); text-align: center; font-size: 12px; color: #9aa4b5; }
.footer-info .dot { margin: 0 8px; }
</style>