Page({
  data: {},
  openMenu() {
    const menu = this.selectComponent('#menu')
    if (menu) menu.open()
  }
})