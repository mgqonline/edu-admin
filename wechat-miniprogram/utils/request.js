const app = getApp ? getApp() : null

function baseUrl() {
  if (app && app.globalData && app.globalData.baseUrl) return app.globalData.baseUrl
  const saved = wx.getStorageSync('BASE_URL')
  return saved || 'http://127.0.0.1:8090'
}

function authHeader() {
  const token = wx.getStorageSync('AUTH_TOKEN')
  return token ? { Authorization: token } : {}
}

function handleResponse(res, showError) {
  const body = res && res.data ? res.data : null
  if (!body) {
    if (showError) wx.showToast({ title: '网络错误', icon: 'error' })
    return null
  }
  if (typeof body.code === 'number' && body.code !== 200) {
    if (body.code === 401 || body.code === 403) {
      wx.reLaunch({ url: '/pages/login/index' })
    }
    if (showError) wx.showToast({ title: body.msg || '请求失败', icon: 'none' })
    return null
  }
  return body.data !== undefined ? body.data : body
}

function request(method, url, data, opts) {
  const options = opts || {}
  const showLoading = !!options.loading
  if (showLoading) wx.showLoading({ title: '加载中' })
  return new Promise((resolve) => {
    wx.request({
      url: baseUrl() + url,
      method,
      data: data || {},
      header: Object.assign({}, authHeader(), options.header || {}),
      success: (res) => {
        const parsed = handleResponse(res, options.showError !== false)
        let result = parsed
        if (typeof options.transform === 'function') {
          try { result = options.transform(parsed) } catch (e) { result = null }
        }
        resolve(result !== undefined && result !== null ? result : (options.defaultValue !== undefined ? options.defaultValue : null))
      },
      fail: () => {
        if (options.showError !== false) wx.showToast({ title: '网络错误', icon: 'error' })
        resolve(options.defaultValue !== undefined ? options.defaultValue : null)
      },
      complete: () => { if (showLoading) wx.hideLoading() }
    })
  })
}

function get(url, params, opts) { return request('GET', url, params, opts) }
function post(url, data, opts) { return request('POST', url, data, opts) }

function upload(url, filePath, name, formData, opts) {
  const options = opts || {}
  const showLoading = !!options.loading
  if (showLoading) wx.showLoading({ title: '上传中' })
  return new Promise((resolve) => {
    wx.uploadFile({
      url: baseUrl() + url,
      filePath,
      name: name || 'file',
      formData: formData || {},
      header: Object.assign({}, authHeader(), options.header || {}),
      success: (res) => {
        let data = null
        try { data = JSON.parse(res.data) } catch (e) { data = null }
        const parsed = handleResponse({ data }, options.showError !== false)
        let result = parsed
        if (typeof options.transform === 'function') {
          try { result = options.transform(parsed) } catch (e) { result = null }
        }
        resolve(result !== undefined && result !== null ? result : (options.defaultValue !== undefined ? options.defaultValue : null))
      },
      fail: () => {
        if (options.showError !== false) wx.showToast({ title: '上传失败', icon: 'error' })
        resolve(options.defaultValue !== undefined ? options.defaultValue : null)
      },
      complete: () => { if (showLoading) wx.hideLoading() }
    })
  })
}

function setCache(key, value, ttlMs) {
  const now = Date.now()
  wx.setStorageSync(key, { value, ts: now, ttl: ttlMs || 0 })
}

function getCache(key) {
  const item = wx.getStorageSync(key)
  if (!item || typeof item !== 'object') return null
  const { value, ts, ttl } = item
  if (!ts || !ttl) return value
  if (Date.now() - ts > ttl) return null
  return value
}

module.exports = { get, post, upload, setCache, getCache }