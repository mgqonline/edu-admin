<template>
  <a-date-picker
    :value="momentValue"
    :format="format"
    :allowClear="allowClear"
    :disabled="disabled"
    :placeholder="placeholder"
    :getCalendarContainer="safeGetContainer"
    @change="handleChange"
  />
</template>

<script>
import moment from 'moment'

export default {
  name: 'V2DatePicker',
  model: { prop: 'value', event: 'input' },
  props: {
    value: { type: [String, Number, Date, Object, null], default: null },
    format: { type: String, default: 'YYYY-MM-DD' },
    allowClear: { type: Boolean, default: true },
    disabled: { type: Boolean, default: false },
    placeholder: { type: String, default: '' },
  },
  data() {
    return { momentValue: this.toMoment(this.value) }
  },
  watch: {
    value(val) { this.momentValue = this.toMoment(val) }
  },
  methods: {
    safeGetContainer() {
      return typeof document !== 'undefined' ? document.body : undefined
    },
    toMoment(input) {
      if (input === null || input === undefined || input === '') return null
      if (moment.isMoment(input)) return input
      if (typeof input === 'number') {
        const ts = String(input).length === 10 ? input * 1000 : input
        return moment(ts)
      }
      if (input instanceof Date) return moment(input)
      if (typeof input === 'string') {
        const m = moment(input, this.format, true)
        return m.isValid() ? m : moment(input)
      }
      try { return moment(input) } catch (e) { return null }
    },
    handleChange(m, str) {
      this.momentValue = m
      const out = (str && typeof str === 'string' && str.length) ? str : null
      this.$emit('input', out)
      this.$emit('change', out)
    },
  },
}
</script>

<style scoped>
</style>