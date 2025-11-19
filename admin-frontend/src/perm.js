// 简易权限工具：全局指令 v-perm 和路由守卫
export function installPerm(Vue, router) {
  // 读取当前权限集
  function getPerms() {
    const cached = window.__PERMS__ || JSON.parse(localStorage.getItem('__PERMS__') || '[]')
    return Array.isArray(cached) ? cached : []
  }
  // 支持字符串或数组：数组表示满足任意一个即可
  function hasPerm(required) {
    const perms = getPerms()
    if (!required) return true
    // 当权限集为空时，默认放行（开发环境或权限尚未拉取），避免页面无法操作
    if (!perms || perms.length === 0) return true
    if (Array.isArray(required)) {
      if (perms.includes('*')) return true
      return required.some(r => perms.includes(r))
    }
    return perms.includes('*') || perms.includes(required)
  }
  function isAuthed() {
    const token = localStorage.getItem('AUTH_TOKEN')
    return !!token
  }
  // v-perm：根据权限隐藏元素（支持数组）
  Vue.directive('perm', {
    inserted(el, binding) {
      const required = binding.value
      if (required === undefined) return
      if (!hasPerm(required)) {
        el.style.display = 'none'
      }
    }
  })
  // 路由守卫：基于 route.meta.requiredPerm 控制访问（支持数组）
  if (router && router.beforeEach) {
    router.beforeEach((to, from, next) => {
      const required = to.meta && to.meta.requiredPerm
      // 登录页直接放行
      if (to.path === '/login') return next()
      // 若需要权限且未登录，跳转登录页
      if (required && !isAuthed()) {
        console.warn('[perm] 请先登录')
        if (to.path === '/login') return next()
        return next({ path: '/login', replace: true })
      }
      // 已登录则校验具体权限
      if (!required) return next()
      if (hasPerm(required)) return next()
      console.warn('[perm] 无访问权限，跳转至可访问页面')
      const fallback = '/attendance'
      if (to.path === fallback) return next()
      next({ path: fallback, replace: true })
    })
  }
}

export default { installPerm }