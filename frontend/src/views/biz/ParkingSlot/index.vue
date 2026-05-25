<template>
  <!-- 应用容器 -->
  <div class="app-container">
    <!-- 搜索表单 -->
    <el-form @submit.prevent :model="ParkingSlotQueryParams" ref="ParkingSlotQueryRef" :inline="true"
      v-show="ParkingSlotShowSearch" label-width="auto">
      <!-- 创建时间选择器 -->
      <el-form-item label="创建时间">
        <el-date-picker style="width: 230px;" v-model="dateRange" value-format="YYYY-MM-DD" type="daterange"
          range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"></el-date-picker>
      </el-form-item>
      <!-- 停车区域ID输入框 -->
      <el-form-item label="停车区域ID" prop="areaId">
        <el-input v-model="ParkingSlotQueryParams.areaId" placeholder="请输入停车区域ID" clearable
          @keyup.enter="ParkingSlotHandleQuery" />
      </el-form-item>
      <!-- 车位编号输入框 -->
      <el-form-item label="车位编号" prop="slotCode">
        <el-input v-model="ParkingSlotQueryParams.slotCode" placeholder="请输入车位编号" clearable
          @keyup.enter="ParkingSlotHandleQuery" />
      </el-form-item>
      <!-- 车位状态下拉选择框 -->
      <el-form-item label="车位状态" prop="slotStatus">
        <el-select v-model="ParkingSlotQueryParams.slotStatus" placeholder="请选择车位状态" clearable>
          <el-option v-for="dict in slot_status" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <!-- 绑定摄像头下拉选择框 -->
      <el-form-item label="绑定摄像头">
        <el-select v-model="ParkingSlotQueryParams.cameraName" clearable>
          <el-option v-for="cam in cameraList" :key="cam.cameraId" :label="cam.cameraName" :value="cam.cameraName" />
        </el-select>
      </el-form-item>
      <!-- 搜索和重置按钮 -->
      <el-form-item>
        <el-button type="primary" icon="Search" @click="ParkingSlotHandleQuery()">搜索</el-button> <!-- 显式调用 -->
        <el-button icon="Refresh" @click="ParkingSlotResetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <!-- 操作按钮行 -->
    <el-row :gutter="10" class="mb8">
      <!-- 新增按钮 -->
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="ParkingSlotHandleAdd"
          v-hasPermi="['biz:ParkingSlot:add']">新增</el-button>
      </el-col>
      <!-- 修改按钮 -->
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="ParkingSlotSingle" @click="ParkingSlotHandleUpdate"
          v-hasPermi="['biz:ParkingSlot:edit']">修改</el-button>
      </el-col>
      <!-- 删除按钮 -->
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="ParkingSlotMultiple" @click="ParkingSlotHandleDelete"
          v-hasPermi="['biz:ParkingSlot:remove']">删除</el-button>
      </el-col>
      <!-- 导出按钮 -->
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="ParkingSlotHandleExport"
          v-hasPermi="['biz:ParkingSlot:export']">导出</el-button>
      </el-col>
      <!-- 下载模板按钮 -->
      <el-col :span="1.5">
        <el-button type="info" plain icon="Download" @click="ParkingSlotImportTemplate"
          v-hasPermi="['biz:ParkingSlot:import']">下载模版</el-button>
      </el-col>
      <!-- 导入按钮 -->
      <el-col :span="1.5">
        <file-upload v-model="importRes" v-hasPermi="['biz:ParkingSlot:import']" :limit="1" uploadBtnText="导入"
          :fileType="['xlsx']" accept=".xlsx" :action="'/biz/ParkingSlot/importData'" :isShowTip="false"
          :isShowFileList="false" isImport />
      </el-col>
      <!-- 右侧工具栏 -->
      <right-toolbar v-model:showSearch="ParkingSlotShowSearch" @queryTable="ParkingSlotGetList"></right-toolbar>
    </el-row>

    <!-- 车位信息表格 -->
    <el-table v-loading="ParkingSlotLoading" :data="ParkingSlotList"
      @selection-change="ParkingSlotHandleSelectionChange">
      <!-- 多选列 -->
      <el-table-column type="selection" width="55" align="center" />
      <!-- 停车区域ID列 -->
      <el-table-column label="停车区域ID" align="center" prop="areaId" />
      <!-- 车位编号列 -->
      <el-table-column label="车位编号" align="center" prop="slotCode" />
      <!-- 车位状态列 -->
      <el-table-column label="车位状态" align="center" prop="slotStatus">
        <template #default="scope">
          <dict-tag :options="slot_status" :value="scope.row.slotStatus" />
        </template>
      </el-table-column>
      <!-- 绑定摄像头列 -->
      <el-table-column label="绑定摄像头" prop="cameraName" />
      <!-- 操作列 -->
      <el-table-column width="130" label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <!-- 修改按钮 -->
          <el-button link type="primary" icon="Edit" @click="ParkingSlotHandleUpdate(scope.row)"
            v-hasPermi="['biz:ParkingSlot:edit']">修改</el-button>
          <!-- 删除按钮 -->
          <el-button link type="primary" icon="Delete" @click="ParkingSlotHandleDelete(scope.row)"
            v-hasPermi="['biz:ParkingSlot:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <pagination v-show="ParkingSlotTotal > 0" :total="ParkingSlotTotal" v-model:page="ParkingSlotQueryParams.pageNum"
      v-model:limit="ParkingSlotQueryParams.pageSize" @pagination="ParkingSlotHandleQuery(false)" />

    <!-- 添加或修改车位信息对话框 -->
    <el-dialog :title="ParkingSlotTitle" v-model="ParkingSlotOpen" width="500px" append-to-body destroy-on-close>
      <el-form @submit.prevent ref="ParkingSlotRef" :model="ParkingSlotForm" :rules="ParkingSlotRules"
        label-width="auto">
        <!-- 停车区域ID表单项 -->
        <el-form-item label="停车区域ID" prop="areaId">
          <el-input v-model="ParkingSlotForm.areaId" placeholder="请输入停车区域ID" />
        </el-form-item>
        <!-- 车位编号表单项 -->
        <el-form-item label="车位编号" prop="slotCode">
          <el-input v-model="ParkingSlotForm.slotCode" placeholder="请输入车位编号" />
        </el-form-item>
        <!-- 车位状态下拉选择框 -->
        <el-form-item label="车位状态" prop="slotStatus">
          <el-radio-group v-model="ParkingSlotForm.slotStatus">
            <el-radio v-for="dict in slot_status" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <!-- 绑定摄像头下拉选择框 -->
        <el-form-item label="绑定摄像头" prop="cameraName">
          <el-select v-model="ParkingSlotForm.cameraName" clearable>
            <el-option v-for="cam in cameraList" :key="cam.cameraId" :label="cam.cameraName" :value="cam.cameraName" />
          </el-select>
        </el-form-item>
        <!-- 备注表单项 -->
        <el-form-item label="备注" prop="remark">
          <el-input v-model="ParkingSlotForm.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <!-- 对话框底部按钮 -->
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="ParkingSlotSubmitForm">确 定</el-button>
          <el-button @click="ParkingSlotCancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ParkingSlot">
// 导入API模块
import { listParkingSlot, getParkingSlot, delParkingSlot, addParkingSlot, updateParkingSlot } from "@/api/biz/ParkingSlot";
import { listDevCamera } from "@/api/biz/DevCamera";
import dayjs from "dayjs";
import xeUtils from "xe-utils";
import { onActivated } from 'vue';


// 摄像头列表数据
const cameraList = ref([]);
// 日期范围选择器数据
const dateRange = ref([]);
const route = useRoute();
const action = ref(null);

onMounted(async () => {
  ParkingSlotLoading.value = true;
  try {
    const cameraRes = await listDevCamera();
    cameraList.value = cameraRes.rows || [];
    await ParkingSlotHandleQuery(true); // 确保 await，保证先加载数据
  } catch (error) {
    console.error('车位页面初始化失败:', error);
  } finally {
    ParkingSlotLoading.value = false; // 无论成功失败都关闭 loading
  }
});

onActivated(() => {
  ParkingSlotHandleQuery(true); // 路由切回此页时重新加载
});


const { proxy } = getCurrentInstance();
const { sys_common_status, slot_status } = proxy.useDict('sys_common_status', 'slot_status');

const importRes = ref(null);
watch(
  () => importRes,
  (val) => {
    if (val.value && val.value.code == 200) {
      ParkingSlotHandleQuery();
    }
  },
  { deep: true, immediate: true }
);

const ParkingSlotList = ref([]);
const ParkingSlotOpen = ref(false);
const ParkingSlotLoading = ref(true);
const ParkingSlotShowSearch = ref(true);
const ParkingSlotIds = ref([]);
const ParkingSlotSingle = ref(true);
const ParkingSlotMultiple = ref(true);
const ParkingSlotTotal = ref(0);
const ParkingSlotTitle = ref("");

const ParkingSlotData = reactive({
  ParkingSlotForm: {},
  ParkingSlotQueryParams: {
    pageNum: 1,
    pageSize: 10,
    areaId: null,
    slotCode: null,
    slotStatus: null,
    cameraName: null
  },
  ParkingSlotRules: {
    areaId: [
      { required: true, message: "停车区域ID不能为空", trigger: "blur" }
    ],
    slotCode: [
      { required: true, message: "车位编号不能为空", trigger: "blur" }
    ],
  }
});

const { ParkingSlotQueryParams, ParkingSlotForm, ParkingSlotRules } = toRefs(ParkingSlotData);

watch(
  route,
  (newRoute) => {
    action.value = newRoute.query && newRoute.query.action;
  },
  { immediate: true }
);

/** 查询车位信息列表 */
function ParkingSlotGetList() {
  ParkingSlotLoading.value = true;
  listParkingSlot(proxy.addDateRange(ParkingSlotQueryParams.value, dateRange.value, 'create_time')).then(response => {
    const cameraMap = new Map((cameraList.value || []).map(cam => [String(cam.cameraId), cam.cameraName]));
    const enhancedParkingSlotList = response.rows.map(slot => ({
      ...slot,
      cameraName: cameraMap.get(String(slot.cameraId)) || '未绑定' // 查找时也转换为字符串
    }));
    // --- 结束映射 ---

    // 将增强后的列表赋值给前端显示
    ParkingSlotList.value = enhancedParkingSlotList;
    ParkingSlotTotal.value = response.total;
    ParkingSlotLoading.value = false;
  }).catch(error => { // 添加错误处理
    console.error('获取车位列表失败:', error);
    ParkingSlotList.value = []; // 清空列表
    ParkingSlotTotal.value = 0;
    ParkingSlotLoading.value = false;
  });
}

// 取消按钮
function ParkingSlotCancel() {
  ParkingSlotOpen.value = false;
  ParkingSlotReset();
}

// 表单重置
function ParkingSlotReset() {
  ParkingSlotForm.value = {
    parkingSlotId: null,
    areaId: null,
    slotCode: null,
    slotStatus: null,
    cameraId: null,
    delFlag: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  };
  proxy.resetForm("ParkingSlotRef");
}

function ParkingSlotHandleQuery(resetPage = true) {
  console.log('[DEBUG] ParkingSlotHandleQuery called, resetPage:', resetPage);
  ParkingSlotLoading.value = true;

  const selectedCamera = cameraList.value.find(cam => cam.cameraName === ParkingSlotQueryParams.value.cameraName);
  const queryParamsForRequest = { ...ParkingSlotQueryParams.value };

  if (selectedCamera) {
    queryParamsForRequest.cameraId = selectedCamera.cameraId;
    delete queryParamsForRequest.cameraName;
  } else {
    delete queryParamsForRequest.cameraId;
  }

  if (resetPage) queryParamsForRequest.pageNum = 1;

  listParkingSlot(proxy.addDateRange(queryParamsForRequest, dateRange.value, 'create_time'))
    .then(response => {
      const cameraMap = new Map((cameraList.value || []).map(cam => [String(cam.cameraId), cam.cameraName]));
      ParkingSlotList.value = response.rows.map(slot => ({
        ...slot,
        cameraName: cameraMap.get(String(slot.cameraId)) || '未绑定'
      }));
      ParkingSlotTotal.value = response.total;
    })
    .catch(error => {
      console.error('获取车位列表失败:', error);
      ParkingSlotList.value = [];
      ParkingSlotTotal.value = 0;
    })
    .finally(() => {
      ParkingSlotLoading.value = false; 
    });
}


/** 重置按钮操作 */
function ParkingSlotResetQuery() {
  dateRange.value = [];
  // 重置表单，这通常会清空所有绑定到表单项的 model 属性
  proxy.resetForm("ParkingSlotQueryRef");

  // --- 手动清空 cameraName 参数 ---
  // 确保查询参数中的 cameraName 被清空，以防 resetForm 未能正确处理 select 组件
  ParkingSlotQueryParams.value.cameraName = null; // 或者 undefined
  // --- 结束手动清空 ---

  // 重置后也要重新查询
  // 里面的 HandleQuery 会处理参数转换，包括处理刚刚手动清空的 cameraName
  ParkingSlotHandleQuery();
}
// 多选框选中数据
function ParkingSlotHandleSelectionChange(selection) {
  ParkingSlotIds.value = selection.map(item => item.parkingSlotId);
  ParkingSlotSingle.value = selection.length != 1;
  ParkingSlotMultiple.value = !selection.length;
}

/** 新增按钮操作 */
function ParkingSlotHandleAdd() {
  ParkingSlotReset();
  ParkingSlotOpen.value = true;
  ParkingSlotTitle.value = "添加";
}

/** 修改按钮操作 */
function ParkingSlotHandleUpdate(row) {
  ParkingSlotReset();
  const _parkingSlotId = row.parkingSlotId || ParkingSlotIds.value
  getParkingSlot(_parkingSlotId).then(response => {
    ParkingSlotForm.value = response.data;
    ParkingSlotOpen.value = true;
    ParkingSlotTitle.value = "修改";
  });
}

/** 提交按钮 */
function ParkingSlotSubmitForm() {
  proxy.$refs["ParkingSlotRef"].validate(valid => {
    if (valid) {
      const submitForm = xeUtils.clone(ParkingSlotForm.value, true);

      // --- 开始处理提交参数 ---
      // 将选中的 cameraName 转换为 cameraId
      if (submitForm.cameraName) {
        const selectedCamera = cameraList.value.find(cam => cam.cameraName === submitForm.cameraName);
        if (selectedCamera) {
          submitForm.cameraId = selectedCamera.cameraId; // 设置 cameraId
        }
        // 如果没找到，可能意味着前端数据不一致，需要处理，这里简单删除 cameraName
        delete submitForm.cameraName; // 后端不需要 cameraName，只存 cameraId
      }
      // --- 结束处理提交参数 ---

      if (submitForm.parkingSlotId != null) {
        updateParkingSlot(submitForm).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          ParkingSlotOpen.value = false;
          ParkingSlotGetList();
        });
      } else {
        addParkingSlot(submitForm).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          ParkingSlotOpen.value = false;
          ParkingSlotGetList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function ParkingSlotHandleDelete(row) {
  const _parkingSlotIds = row.parkingSlotId || ParkingSlotIds.value;
  proxy.$modal.confirm('是否确认删除编号为"' + _parkingSlotIds + '"的数据项？').then(function () {
    return delParkingSlot(_parkingSlotIds);
  }).then(() => {
    ParkingSlotGetList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => { });
}

/** 导出按钮操作 */
function ParkingSlotHandleExport() {
  proxy.download('biz/ParkingSlot/export', {
    ...ParkingSlotQueryParams.value
  }, `车位信息_${new Date().getTime()}.xlsx`)
}

/** 下载模板操作 */
function ParkingSlotImportTemplate() {
  proxy.download(
    "biz/ParkingSlot/importTemplate", {},
    `车位信息_${new Date().getTime()}.xlsx`
  );
}
</script>
<style lang="scss" scoped></style>