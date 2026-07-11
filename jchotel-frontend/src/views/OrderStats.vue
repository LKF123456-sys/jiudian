<template>
  <div class="page-container">
    <div class="page-header">
      <span class="page-title">营收统计</span>
      <div style="display: flex; gap: 10px; align-items: center;">
        <el-radio-group v-model="range" @change="onRangeChange">
          <el-radio-button label="week">近7天</el-radio-button>
          <el-radio-button label="month">近30天</el-radio-button>
          <el-radio-button label="custom">自定义</el-radio-button>
        </el-radio-group>
        <template v-if="range === 'custom'">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px;"
          ></el-date-picker>
          <el-button type="primary" @click="loadStats">查询</el-button>
        </template>
      </div>
    </div>

    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-label">总营收</div>
        <div class="stat-value">¥{{ formatMoney(totalAmount) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">订单数</div>
        <div class="stat-value">{{ totalCount }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">平均客单价</div>
        <div class="stat-value">¥{{ formatMoney(averageAmount) }}</div>
      </div>
    </div>

    <div class="chart-card">
      <div ref="chart" style="width: 100%; height: 400px;"></div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts/core'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { orderStats } from '@/api/order'
import { formatMoney } from '@/utils/format'

echarts.use([BarChart, GridComponent, TooltipComponent, CanvasRenderer])

export default {
  name: 'OrderStats',
  data() {
    return {
      range: 'week',
      dateRange: [],
      chart: null,
      dates: [],
      amounts: [],
      totalAmount: 0,
      totalCount: 0,
      averageAmount: 0
    }
  },
  created() {
    this.loadStats()
  },
  mounted() {
    this.initChart()
    window.addEventListener('resize', this.resizeChart)
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.resizeChart)
    if (this.chart) {
      this.chart.dispose()
      this.chart = null
    }
  },
  methods: {
    formatMoney,
    onRangeChange(val) {
      if (val === 'custom') {
        return
      }
      this.dateRange = []
      this.loadStats()
    },
    loadStats() {
      const params = {}
      if (this.range === 'custom') {
        if (!this.dateRange || this.dateRange.length !== 2) {
          return
        }
        params.startTime = this.dateRange[0]
        params.endTime = this.dateRange[1]
      } else {
        params.range = this.range
      }
      orderStats(params).then(data => {
        const stats = data || {}
        this.dates = stats.dates || []
        this.amounts = stats.amounts || []
        this.totalAmount = stats.totalAmount || 0
        this.totalCount = stats.totalCount || 0
        this.averageAmount = stats.averageAmount || 0
        this.updateChart()
      })
    },
    initChart() {
      this.chart = echarts.init(this.$refs.chart)
      this.updateChart()
    },
    updateChart() {
      if (!this.chart) return
      const dates = this.dates || []
      const amounts = this.amounts || []
      const option = {
        tooltip: {
          trigger: 'axis',
          formatter: function(params) {
            if (!params || !params.length) return ''
            const item = params[0]
            return (item.name || '') + '<br/>营收：¥' + (item.value != null ? item.value : 0)
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: dates,
          axisLine: { lineStyle: { color: '#ccc' } },
          axisLabel: { color: '#666' }
        },
        yAxis: {
          type: 'value',
          axisLine: { show: false },
          splitLine: { lineStyle: { color: '#eee' } },
          axisLabel: { color: '#666' }
        },
        series: [
          {
            name: '营收',
            type: 'bar',
            data: amounts,
            itemStyle: {
              color: {
                type: 'linear',
                x: 0, y: 0, x2: 0, y2: 1,
                colorStops: [
                  { offset: 0, color: '#1e3a5f' },
                  { offset: 1, color: '#4a7ebb' }
                ]
              },
              borderRadius: [4, 4, 0, 0]
            },
            barWidth: '50%'
          }
        ]
      }
      this.chart.setOption(option, true)
    },
    resizeChart() {
      if (this.chart) {
        this.chart.resize()
      }
    }
  }
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;
}
.page-title {
  font-size: 20px;
  font-weight: bold;
  color: #1e3a5f;
}
.stat-cards {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}
.stat-card {
  flex: 1;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  text-align: center;
}
.stat-label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 10px;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #1e3a5f;
}
.chart-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}
</style>
