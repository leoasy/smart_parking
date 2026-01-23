<template>
  <div class="dashboard">

    <!-- 顶部标题 -->
    <div class="dashboard-header">
      <h2>🚗 社区车位占用检测系统 · 智慧大屏</h2>
      <span class="time">{{ currentTime }}</span>
    </div>

    <!-- ① 核心指标 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6" v-for="item in stats" :key="item.title">
        <div class="stat-box">
          <div class="stat-title">{{ item.title }}</div>
          <div class="stat-value">{{ item.value }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- ② 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="8">
        <el-card>
          <div ref="slotChart" class="chart"></div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <div ref="alarmChart" class="chart"></div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <div ref="cameraChart" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ③ 实时事件 -->
    <el-card class="event-card">
      <div class="event-title">📡 AI 识别事件实时流</div>
      <el-table :data="events" height="260">
        <el-table-column prop="cameraName" label="摄像头" width="150" />
        <el-table-column prop="slotCode" label="车位" />
        <el-table-column prop="oldStatus" label="原状态" />
        <el-table-column prop="newStatus" label="新状态" />
        <el-table-column prop="eventTime" label="时间" />
      </el-table>
    </el-card>

  </div>
</template>

<script setup>
import { ref, onMounted } from "vue"
import * as echarts from "echarts"
import request from "@/utils/request"

// 当前时间
const currentTime = ref("")
setInterval(() => {
  currentTime.value = new Date().toLocaleString()
}, 1000)

// 指标数据
const stats = ref([
  { title: "总车位数", value: 0 },
  { title: "已占用", value: 0 },
  { title: "空闲车位", value: 0 },
  { title: "今日告警", value: 0 }
])

// 图表 DOM
const slotChart = ref(null)
const alarmChart = ref(null)
const cameraChart = ref(null)

// 事件列表
const events = ref([])

onMounted(async () => {
  loadDashboard()
})

// 请求后端数据
async function loadDashboard() {
  const res = await request.get("/biz/dashboard")

  const data = res.data

  stats.value[0].value = data.totalSlots
  stats.value[1].value = data.occupiedSlots
  stats.value[2].value = data.freeSlots
  stats.value[3].value = data.todayAlarms

  events.value = data.events

  initSlotChart(data)
  initAlarmChart(data)
  initCameraChart(data)
}

// ① 车位占用饼图
function initSlotChart(data) {
  const chart = echarts.init(slotChart.value)
  chart.setOption({
    title: { text: "车位占用情况", left: "center" },
    tooltip: { trigger: "item" },
    series: [
      {
        type: "pie",
        radius: "65%",
        data: [
          { value: data.occupiedSlots, name: "已占用" },
          { value: data.freeSlots, name: "空闲" }
        ]
      }
    ]
  })
}

// ② 告警趋势
function initAlarmChart(data) {
  const chart = echarts.init(alarmChart.value)
  chart.setOption({
    title: { text: "告警趋势（7天）" },
    xAxis: { type: "category", data: data.days },
    yAxis: { type: "value" },
    series: [
      { data: data.alarmCounts, type: "line", smooth: true }
    ]
  })
}

// ③ 摄像头状态
function initCameraChart(data) {
  const chart = echarts.init(cameraChart.value)
  chart.setOption({
    title: { text: "摄像头状态" },
    xAxis: { type: "category", data: ["在线", "离线"] },
    yAxis: { type: "value" },
    series: [
      {
        data: [data.cameraOnline, data.cameraOffline],
        type: "bar"
      }
    ]
  })
}
</script>

<style scoped>
.dashboard {
  padding: 20px;
  background: linear-gradient(135deg, #0f172a, #020617);
  color: #fff;
  min-height: 100vh;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.dashboard-header h2 {
  font-size: 22px;
  font-weight: bold;
}

.time {
  font-size: 14px;
  color: #94a3b8;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-box {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 18px;
  text-align: center;
  transition: 0.3s;
}

.stat-box:hover {
  transform: scale(1.05);
}

.stat-title {
  font-size: 14px;
  color: #cbd5e1;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  margin-top: 6px;
  color: #38bdf8;
}

.chart {
  height: 280px;
}

.event-card {
  margin-top: 20px;
}

.event-title {
  font-size: 16px;
  margin-bottom: 10px;
  color: #38bdf8;
}
</style>
