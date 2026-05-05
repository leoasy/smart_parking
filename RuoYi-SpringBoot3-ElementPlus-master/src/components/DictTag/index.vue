<template>
  <div>
    <template v-for="(item, index) in options" :key="`${item.value}-${index}`">
      <template v-if="isValueMatch(item.value)">
        <span
          v-if="(item.elTagType === 'default' || item.elTagType === '') && (item.elTagClass === '' || item.elTagClass == null)"
          :class="item.elTagClass"
        >{{ item.label + " " }}</span>
        <el-tag
          v-else
          :disable-transitions="true"
          :type="item.elTagType"
          :class="item.elTagClass"
        >{{ item.label + " " }}</el-tag>
      </template>
    </template>
    <template v-if="unmatch && showValue">
      {{ handleArray(unmatchArray) }}
    </template>
  </div>
</template>

<script setup>
const unmatchArray = ref([])

const props = defineProps({
  options: {
    type: Array,
    default: () => []
  },
  value: [Number, String, Array],
  showValue: {
    type: Boolean,
    default: true
  },
  separator: {
    type: String,
    default: ","
  }
})

const values = computed(() => {
  if (props.value === null || typeof props.value === "undefined" || props.value === "") return []
  if (typeof props.value === "number" || typeof props.value === "boolean") return [props.value]
  return Array.isArray(props.value) ? props.value.map(item => `${item}`) : `${props.value}`.split(props.separator)
})

const unmatch = computed(() => {
  unmatchArray.value = []
  if (props.value === null || typeof props.value === "undefined" || props.value === "" || !Array.isArray(props.options) || props.options.length === 0) return false

  let hasUnmatch = false
  values.value.forEach(item => {
    if (!props.options.some(v => v.value == item)) {
      unmatchArray.value.push(item)
      hasUnmatch = true
    }
  })
  return hasUnmatch
})

function handleArray(array) {
  return array.length === 0 ? "" : array.join(" ")
}

function isValueMatch(itemValue) {
  return values.value.some(val => val == itemValue)
}
</script>

<style scoped>
.el-tag + .el-tag {
  margin-left: 10px;
}
</style>
