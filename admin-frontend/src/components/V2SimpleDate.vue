<template>
  <date-picker
    v-model="inner"
    :format="format"
    :type="typeResolved"
    :lang="lang"
    :value-type="valueType"
    :clearable="allowClear"
    :disabled="disabled"
    :placeholder="placeholder"
  />
</template>

<script>
import DatePicker from 'vue2-datepicker'
import 'vue2-datepicker/index.css'
import zhCN from 'vue2-datepicker/locale/zh-cn'

export default {
  name: 'V2SimpleDate',
  components: { DatePicker },
  model: { prop: 'value', event: 'input' },
  props: {
    value: { type: [String, Number, Date, null], default: null },
    format: { type: String, default: 'YYYY-MM-DD' },
    type: { type: String, default: null },
    // 与 AntDesign 的 showTime 语义对齐：true 时使用 datetime
    showTime: { type: Boolean, default: false },
    // 默认中文语言包，月份与星期显示为中文
    lang: { type: Object, default: () => zhCN },
    // 与旧 CompatDatePicker 对齐：'format' | 'date' | 'timestamp'
    valueType: { type: String, default: 'format' },
    allowClear: { type: Boolean, default: true },
    disabled: { type: Boolean, default: false },
    placeholder: { type: String, default: '' },
  },
  computed: {
    typeResolved(){
      if (this.showTime) return 'datetime'
      return this.type || null
    }
  },
  data() {
    return { inner: this.value }
  },
  watch: {
    value(val) { this.inner = val },
    inner(val) { this.$emit('input', val); this.$emit('change', val) }
  },
}
</script>

<style scoped>
</style>