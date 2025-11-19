<template>
  <a-date-picker
    :value="innerMoment"
    :format="displayFormat"
    :showTime="computedShowTime"
    :getCalendarContainer="safeGetContainer"
    :placeholder="placeholder"
    :disabled="disabled"
    :allowClear="allowClear"
    :class="$attrs && $attrs.class"
    :style="$attrs && $attrs.style"
    @openChange="onOpenChange"
    @panelChange="onPanelChange"
    @ok="onOk"
    @change="onAntChange"
  />
</template>

<script>
import moment from 'moment'

export default {
  name: 'CompatDatePicker',
  inheritAttrs: false,
  model: { prop: 'value', event: 'input' },
  props: {
    value: { type: [String, Number, Date, Object, null], default: null },
    // vue2-datepicker compatibility: 'date' | 'datetime' | 'month'（UI 仍用 date，按格式输出）
    type: { type: String, default: 'date' },
    // 旧项目常用：字符串格式
    valueFormat: { type: String, default: '' },
    // 新增兼容：'format' | 'date' | 'timestamp'
    valueType: { type: String, default: '' },
    // 常用显示与交互控制
    placeholder: { type: String, default: '' },
    disabled: { type: Boolean, default: false },
    allowClear: { type: Boolean, default: true },
  },
  data() {
    return { innerMoment: this.toMoment(this.value) }
  },
  computed: {
    resolvedValueType() {
      if (this.valueType) return this.valueType
      return this.valueFormat ? 'format' : 'date'
    },
    displayFormat() {
      if (this.valueFormat) return this.valueFormat
      if (this.type === 'datetime') return 'YYYY-MM-DD HH:mm:ss'
      if (this.type === 'month') return 'YYYY-MM'
      return 'YYYY-MM-DD'
    },
    computedShowTime() {
      if (this.type === 'datetime') return true
      const st = this.$attrs && this.$attrs.showTime
      return !!st
    },
    safeGetContainer() {
      const { getCalendarContainer } = this.$attrs || {}
      if (typeof getCalendarContainer === 'function') return getCalendarContainer
      return () => (typeof document !== 'undefined' ? document.body : undefined)
    },
  },
  watch: {
    value(val) {
      this.innerMoment = this.toMoment(val)
    },
  },
  methods: {
    onOpenChange() { /* no-op to stabilize chainedFunction */ },
    onPanelChange() { /* no-op */ },
    onOk() { /* no-op */ },
    toMoment(input) {
      if (input === null || input === undefined || input === '') return null
      if (moment.isMoment(input)) return input
      if (typeof input === 'number') {
        const ts = String(input).length === 10 ? input * 1000 : input
        return moment(ts)
      }
      if (input instanceof Date) return moment(input)
      if (typeof input === 'string') {
        const fmt = this.displayFormat
        const m = moment(input, fmt, true)
        return m.isValid() ? m : moment(input)
      }
      try {
        return moment(input)
      } catch (e) {
        return null
      }
    },
    outputValue(m) {
      if (!m || !moment.isMoment(m) || !m.isValid()) return null
      const vt = this.resolvedValueType
      if (vt === 'format') return m.format(this.displayFormat)
      if (vt === 'timestamp') return m.valueOf()
      // default: Date 对象
      return m.toDate()
    },
    onAntChange(m, str) {
      this.innerMoment = m
      const out = this.outputValue(m)
      this.$emit('input', out)
      this.$emit('change', out)
    },
  },
}
</script>

<style scoped>
</style>