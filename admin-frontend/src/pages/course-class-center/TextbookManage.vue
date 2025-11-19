<template>
  <div style="background:#fff; padding:16px">
    <h3>教材与教具管理</h3>
    <a-form layout="inline" style="margin-bottom:12px">
      <a-form-item label="名称">
        <a-input v-model="form.name" placeholder="教材名称" style="width:200px" />
      </a-form-item>
      <a-form-item label="出版社">
        <a-input v-model="form.publisher" placeholder="出版社" style="width:200px" />
      </a-form-item>
      <a-form-item label="单价">
        <a-input-number v-model="form.unitPrice" :min="0" :step="1" style="width:160px" />
      </a-form-item>
      <a-button type="primary" @click="saveTextbook">保存</a-button>
      <a-button style="margin-left:8px" @click="resetForm">重置</a-button>
    </a-form>

    <a-space style="margin-bottom:8px">
      <a-input-search v-model="keyword" placeholder="搜索教材或出版社" @search="loadTextbooks" style="width:300px" />
      <a-button @click="loadTextbooks">刷新</a-button>
      <a-button type="dashed" @click="loadLowStock">低库存预警</a-button>
    </a-space>

    <a-table :columns="columns" :data-source="data" rowKey="textbookId" :pagination="false">
      <span slot="actions" slot-scope="t, record">
        <a-space>
          <a @click="inventory(record, 'in')">入库</a>
          <a @click="inventory(record, 'out')">出库</a>
          <a @click="viewRecords(record)">记录</a>
        </a-space>
      </span>
    </a-table>

    <a-modal :visible="recordVisible" title="库存记录" @cancel="recordVisible=false" footer="null">
      <a-table :columns="recordColumns" :data-source="records" rowKey="rkey" :pagination="false" />
    </a-modal>
  </div>
</template>

<script>
export default {
  name: 'TextbookManage',
  data() {
    return {
      form: { name: '', publisher: '', unitPrice: 0 },
      keyword: '',
      data: [],
      recordVisible: false,
      records: []
    }
  },
  computed: {
    columns() {
      return [
        { title: '名称', dataIndex: 'name' },
        { title: '出版社', dataIndex: 'publisher' },
        { title: '单价', dataIndex: 'unitPrice' },
        { title: '库存', dataIndex: 'stock' },
        { title: '操作', key: 'actions', scopedSlots: { customRender: 'actions' } }
      ]
    },
    recordColumns() {
      return [
        { title: '类型', dataIndex: 'type' },
        { title: '数量', dataIndex: 'qty' },
        { title: '时间', dataIndex: 'time' }
      ]
    }
  },
  created() {
    this.loadTextbooks()
  },
  methods: {
    async saveTextbook() {
      const res = await fetch('/api/course/textbook/save', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(this.form) })
      const json = await res.json()
      if (json && json.code === 200) {
        this.$message.success('保存成功')
        this.loadTextbooks()
      } else {
        this.$message.error('保存失败')
      }
    },
    resetForm() {
      this.form = { name: '', publisher: '', unitPrice: 0 }
    },
    async loadTextbooks() {
      const url = '/api/course/textbook/list' + (this.keyword ? ('?keyword=' + encodeURIComponent(this.keyword)) : '')
      const res = await fetch(url)
      const json = await res.json()
      this.data = (json && json.code === 200) ? (json.data || []) : []
    },
    async inventory(row, type) {
      const qty = await this.promptQty(type)
      if (!qty) return
      const url = type === 'in' ? `/api/course/textbook/inventory/in?textbookId=${row.textbookId}&qty=${qty}` : `/api/course/textbook/inventory/out?textbookId=${row.textbookId}&qty=${qty}`
      const res = await fetch(url, { method: 'POST' })
      const json = await res.json()
      if (json && json.code === 200) {
        this.$message.success((type === 'in' ? '入库' : '出库') + '成功')
        this.loadTextbooks()
      }
    },
    promptQty(type) {
      return new Promise(resolve => {
        const val = window.prompt('请输入数量', '1')
        const num = Number(val)
        resolve(Number.isFinite(num) && num > 0 ? Math.floor(num) : 0)
      })
    },
    async viewRecords(row) {
      const res = await fetch(`/api/course/textbook/inventory/records/${row.textbookId}`)
      const json = await res.json()
      const data = (json && json.code === 200) ? (json.data || []) : []
      this.records = data.map((it, idx) => Object.assign({ rkey: `${row.textbookId}-${idx}` }, it))
      this.recordVisible = true
    },
    async loadLowStock() {
      const res = await fetch('/api/course/textbook/low-stock?threshold=5')
      const json = await res.json()
      const list = (json && json.code === 200) ? (json.data || []) : []
      if (!list.length) {
        this.$message.info('暂无低库存教材')
      } else {
        const names = list.map(it => `${it.name}(库存:${it.stock})`).join('，')
        this.$message.warn('低库存：' + names)
      }
    }
  }
}
</script>

<style scoped>
</style>