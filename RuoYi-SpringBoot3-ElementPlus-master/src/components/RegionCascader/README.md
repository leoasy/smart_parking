# RegionCascader 区域级联组件

`RegionCascader` 是基于 Element Plus `el-cascader` 封装的省、市、区级联选择组件。组件已全局注册，可直接在页面中使用。

## 功能

- 支持省、市、区三级选择。
- 支持 `level` 控制选择层级：1 省、2 省市、3 省市区。
- 支持懒加载区域数据。
- 支持 `v-model` 双向绑定。
- 支持获取选中值、选中文本和完整选择信息。

## 基础用法

```vue
<template>
  <RegionCascader v-model="region" placeholder="请选择省市区" />
</template>

<script setup>
import { ref } from 'vue'

const region = ref([])
</script>
```

## 表单用法

```vue
<template>
  <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
    <el-form-item label="所在区域" prop="region">
      <RegionCascader
        v-model="form.region"
        placeholder="请选择省市区"
        @change-with-labels="handleRegionChange"
      />
    </el-form-item>
  </el-form>
</template>

<script setup>
import { reactive, ref } from 'vue'

const formRef = ref()
const form = reactive({
  region: [],
  regionText: ''
})

const rules = {
  region: [{ required: true, message: '请选择区域', trigger: 'change' }]
}

function handleRegionChange(data) {
  form.regionText = data.labels.join('/')
}
</script>
```

## 控制层级

```vue
<template>
  <RegionCascader v-model="province" :level="1" placeholder="请选择省份" />
  <RegionCascader v-model="city" :level="2" placeholder="请选择省市" />
  <RegionCascader v-model="district" :level="3" placeholder="请选择省市区" />
</template>
```

## Props

| 参数 | 说明 | 类型 | 默认值 |
| --- | --- | --- | --- |
| `modelValue` | 绑定值 | `Array` | `[]` |
| `placeholder` | 占位文本 | `String` | `请选择省市区` |
| `disabled` | 是否禁用 | `Boolean` | `false` |
| `size` | 尺寸 | `large \| default \| small` | `default` |
| `width` | 宽度 | `String` | `100%` |
| `showDistrict` | 兼容旧参数，是否显示区县 | `Boolean` | `true` |
| `level` | 显示层级 | `1 \| 2 \| 3` | `3` |

## Events

| 事件 | 说明 | 回调参数 |
| --- | --- | --- |
| `update:modelValue` | 绑定值变化 | `value: Array` |
| `change` | 选择变化 | `value: Array` |
| `change-with-labels` | 选择变化并返回文本 | `{ values: Array, labels: Array }` |

## Methods

通过 `ref` 获取组件实例：

| 方法 | 说明 |
| --- | --- |
| `reset()` | 清空选择 |
| `setValue(value)` | 设置选中值 |
| `getLabels()` | 获取选中文本数组 |
| `getSelectedInfo()` | 获取 `{ values, labels, text }` |

```vue
<template>
  <RegionCascader ref="regionRef" v-model="region" />
  <el-button @click="setBeijing">设置北京</el-button>
  <el-button @click="readInfo">读取信息</el-button>
</template>

<script setup>
import { ref } from 'vue'

const regionRef = ref()
const region = ref([])

function setBeijing() {
  regionRef.value.setValue([110000, 110100, 110101])
}

function readInfo() {
  console.log(regionRef.value.getSelectedInfo())
}
</script>
```

## 数据依赖

组件调用 `@/api/biz/Region` 中的：

- `listRegion({ level, parentId })`
- `getRegion(id)`

后端需要返回包含 `id`、`name`、`level`、`parentId` 的区域数据。
