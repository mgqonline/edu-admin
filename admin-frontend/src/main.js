import Vue from 'vue'
import App from './App.vue'
import router from './router'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/antd.css'
import { installPerm } from './perm'
import moment from 'moment'
import 'moment/locale/zh-cn'

Vue.use(Antd)

// 设置中文 locale（影响日期组件月份/星期中文显示）
try { if (moment && typeof moment.locale === 'function') moment.locale('zh-cn') } catch(e) { /* ignore */ }

// 加固 Vue.nextTick：当传入非函数时，避免把 undefined 推入回调队列
(function hardenNextTick(VueCtor){
  const origNextTick = VueCtor.nextTick
  if (typeof origNextTick === 'function') {
    VueCtor.nextTick = function(cb, ctx) {
      if (typeof cb !== 'function') {
        try {
          console.warn('[nextTick] non-function callback intercepted', { type: typeof cb, ctx })
        } catch (e) {}
        return origNextTick.call(this)
      }
      return origNextTick.call(this, cb, ctx)
    }
    VueCtor.prototype.$nextTick = function(cb) {
      if (typeof cb !== 'function') {
        try {
          console.warn('[instance.$nextTick] non-function callback intercepted', { type: typeof cb, name: (this && this.$options && this.$options.name) })
        } catch (e) {}
        return origNextTick.call(this)
      }
      return origNextTick.call(this, cb, this)
    }
  }
})(Vue)

// 全局错误/警告处理：辅助定位 nextTick 异常来源
Vue.config.errorHandler = function(err, vm, info) {
  try {
    const name = vm && vm.$options && vm.$options.name
    console.error('[GlobalError]', err && err.message, 'info:', info, 'component:', name, err)
  } catch (e) {
    console.error('[GlobalError]', err)
  }
}
Vue.config.warnHandler = function(msg, vm, trace) {
  try {
    const name = vm && vm.$options && vm.$options.name
    console.warn('[GlobalWarn]', msg, 'component:', name, trace)
  } catch (e) {
    console.warn('[GlobalWarn]', msg)
  }
}

// 全局 fetch 包装器：自动附加 Authorization 与 X-Perms
;(function setupFetchAuth() {
  const originalFetch = window.fetch
  if (!originalFetch) return
  window.fetch = async function(input, init = {}) {
    const token = localStorage.getItem('AUTH_TOKEN') || ''
    const perms = window.__PERMS__ || JSON.parse(localStorage.getItem('__PERMS__') || '[]')
    // 规范化 headers
    const headers = new Headers(init && init.headers ? init.headers : undefined)
    if (token && !headers.has('Authorization')) {
      headers.set('Authorization', 'Bearer ' + token)
    }
    if (Array.isArray(perms) && perms.length && !headers.has('X-Perms')) {
      headers.set('X-Perms', perms.join(','))
    }
    const nextInit = Object.assign({}, init, { headers })
    return originalFetch(input, nextInit)
  }
})()

// 安装权限指令与路由守卫
installPerm(Vue, router)

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')