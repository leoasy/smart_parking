<template>
  <div class="app-container">
    <el-form @submit.prevent :model="DevCameraQueryParams" ref="DevCameraQueryRef" :inline="true" v-show="DevCameraShowSearch" label-width="auto">
      <el-form-item label="创建时间">
        <el-date-picker
            style="width: 230px;"
            v-model="dateRange"
            value-format="YYYY-MM-DD"
            type="daterange"
            range-separator="-"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item label="摄像头名称" prop="cameraName">
        <el-input
          v-model="DevCameraQueryParams.cameraName"
          placeholder="请输入摄像头名称"
          clearable
          @keyup.enter="DevCameraHandleQuery"
        />
      </el-form-item>
      <el-form-item label="设备状态" prop="cameraStatus">
        <el-select v-model="DevCameraQueryParams.cameraStatus" placeholder="请选择设备状态" clearable>
          <el-option
            v-for="dict in device_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="安装位置" prop="location">
        <el-input
          v-model="DevCameraQueryParams.location"
          placeholder="请输入安装位置"
          clearable
          @keyup.enter="DevCameraHandleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="DevCameraHandleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="DevCameraResetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="DevCameraHandleAdd"
          v-hasPermi="['biz:DevCamera:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="DevCameraSingle"
          @click="DevCameraHandleUpdate"
          v-hasPermi="['biz:DevCamera:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="DevCameraMultiple"
          @click="DevCameraHandleDelete"
          v-hasPermi="['biz:DevCamera:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="DevCameraHandleExport"
          v-hasPermi="['biz:DevCamera:export']"
        >导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="info"
            plain
            icon="Download"
            @click="DevCameraImportTemplate"
            v-hasPermi="['biz:DevCamera:import']"
        >下载模版</el-button>
      </el-col>
      <el-col :span="1.5">
      <file-upload
            v-model="importRes"
            v-hasPermi="['biz:DevCamera:import']"
            :limit="1"
            uploadBtnText="导入"
            :fileType="['xlsx']"
            accept=".xlsx"
            :action="'/biz/DevCamera/importData'"
            :isShowTip="false"
            :isShowFileList="false"
            isImport
          />
      </el-col>
      <right-toolbar v-model:showSearch="DevCameraShowSearch" @queryTable="DevCameraGetList"></right-toolbar>
    </el-row>

    <el-table v-loading="DevCameraLoading" :data="DevCameraList" @selection-change="DevCameraHandleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="摄像头名称" align="center" prop="cameraName" />
      <el-table-column label="设备状态" align="center" prop="cameraStatus">
        <template #default="scope">
          <dict-tag :options="device_status" :value="scope.row.cameraStatus"/>
        </template>
      </el-table-column>
      <el-table-column label="最后心跳时间" align="center" prop="lastHeartbeat" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.lastHeartbeat, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="安装位置" align="center" prop="location" />
      <el-table-column width="130" label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="DevCameraHandleUpdate(scope.row)" v-hasPermi="['biz:DevCamera:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="DevCameraHandleDelete(scope.row)" v-hasPermi="['biz:DevCamera:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="DevCameraTotal>0"
      :total="DevCameraTotal"
      v-model:page="DevCameraQueryParams.pageNum"
      v-model:limit="DevCameraQueryParams.pageSize"
      @pagination="DevCameraGetList"
    />

    <!-- 添加或修改摄像头设备对话框 -->
    <el-dialog :title="DevCameraTitle" v-model="DevCameraOpen" width="500px" append-to-body destroy-on-close>
      <el-form @submit.prevent ref="DevCameraRef" :model="DevCameraForm" :rules="DevCameraRules" label-width="auto">
        <el-form-item label="摄像头名称" prop="cameraName">
          <el-input v-model="DevCameraForm.cameraName" placeholder="请输入摄像头名称" />
        </el-form-item>
        <el-form-item label="RTSP地址" prop="rtspUrl">
          <el-input v-model="DevCameraForm.rtspUrl" placeholder="请输入RTSP地址" />
        </el-form-item>
        <el-form-item label="设备状态" prop="cameraStatus">
          <el-select v-model="DevCameraForm.cameraStatus" placeholder="请选择设备状态">
            <el-option
              v-for="dict in device_status"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="安装位置" prop="location">
          <el-input v-model="DevCameraForm.location" placeholder="请输入安装位置" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="DevCameraForm.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="DevCameraSubmitForm">确 定</el-button>
          <el-button @click="DevCameraCancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DevCamera">
import { listDevCamera, getDevCamera, delDevCamera, addDevCamera, updateDevCamera } from "@/api/biz/DevCamera";
import dayjs from "dayjs";
import xeUtils from "xe-utils";

const dateRange = ref([]);

const route = useRoute();
const action = ref(null);

onMounted(() => {
  DevCameraHandleQuery();
});

const { proxy } = getCurrentInstance();
const { device_status, sys_common_status } = proxy.useDict('device_status', 'sys_common_status');

const importRes = ref(null);
watch(
  () => importRes,
  (val) => {
    if (val.value && val.value.code == 200) {
      DevCameraHandleQuery();
    }
  },
  { deep: true, immediate: true }
);

const DevCameraList = ref([]);
const DevCameraOpen = ref(false);
const DevCameraLoading = ref(true);
const DevCameraShowSearch = ref(true);
const DevCameraIds = ref([]);
const DevCameraSingle = ref(true);
const DevCameraMultiple = ref(true);
const DevCameraTotal = ref(0);
const DevCameraTitle = ref("");

const DevCameraData = reactive({
  DevCameraForm: {},
  DevCameraQueryParams: {
    pageNum: 1,
    pageSize: 10,
    cameraName: null,
    cameraStatus: null,
    location: null,
  },
  DevCameraRules: {
    cameraName: [
      { required: true, message: "摄像头名称不能为空", trigger: "blur" }
    ],
    rtspUrl: [
      { required: true, message: "RTSP地址不能为空", trigger: "blur" }
    ],
    cameraStatus: [
      { required: true, message: "设备状态(ONLINE/OFFLINE)不能为空", trigger: "change" }
    ],
  }
});

const { DevCameraQueryParams, DevCameraForm, DevCameraRules } = toRefs(DevCameraData);

watch(
  route,
  (newRoute) => {
    action.value = newRoute.query && newRoute.query.action;
  },
  { immediate: true }
);

/** 查询摄像头设备列表 */
function DevCameraGetList() {
  DevCameraLoading.value = true;
  listDevCamera(proxy.addDateRange(DevCameraQueryParams.value, dateRange.value, 'create_time')).then(response => {
    DevCameraList.value = response.rows;
    DevCameraTotal.value = response.total;
    DevCameraLoading.value = false;
  });
}

// 取消按钮
function DevCameraCancel() {
  DevCameraOpen.value = false;
  DevCameraReset();
}

// 表单重置
function DevCameraReset() {
  DevCameraForm.value = {
    cameraId: null,
    cameraName: null,
    rtspUrl: null,
    cameraStatus: null,
    lastHeartbeat: null,
    location: null,
    delFlag: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  };
  proxy.resetForm("DevCameraRef");
}

/** 搜索按钮操作 */
function DevCameraHandleQuery() {
  DevCameraQueryParams.value.pageNum = 1;
  DevCameraGetList();
}

/** 重置按钮操作 */
function DevCameraResetQuery() {
  dateRange.value = [];
  proxy.resetForm("DevCameraQueryRef");
  DevCameraHandleQuery();
}

// 多选框选中数据
function DevCameraHandleSelectionChange(selection) {
  DevCameraIds.value = selection.map(item => item.cameraId);
  DevCameraSingle.value = selection.length != 1;
  DevCameraMultiple.value = !selection.length;
}

/** 新增按钮操作 */
function DevCameraHandleAdd() {
  DevCameraReset();
  DevCameraOpen.value = true;
  DevCameraTitle.value = "添加";
}

/** 修改按钮操作 */
function DevCameraHandleUpdate(row) {
  DevCameraReset();
  const _cameraId = row.cameraId || DevCameraIds.value
  getDevCamera(_cameraId).then(response => {
    DevCameraForm.value = response.data;
    DevCameraOpen.value = true;
    DevCameraTitle.value = "修改";
  });
}

/** 提交按钮 */
function DevCameraSubmitForm() {
  proxy.$refs["DevCameraRef"].validate(valid => {
    if (valid) {
        const submitForm = xeUtils.clone(DevCameraForm.value, true);
      if (submitForm.cameraId != null) {
        updateDevCamera(submitForm).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          DevCameraOpen.value = false;
          DevCameraGetList();
        });
      } else {
        addDevCamera(submitForm).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          DevCameraOpen.value = false;
          DevCameraGetList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function DevCameraHandleDelete(row) {
  const _cameraIds = row.cameraId || DevCameraIds.value;
  proxy.$modal.confirm('是否确认删除编号为"' + _cameraIds + '"的数据项？').then(function() {
    return delDevCamera(_cameraIds);
  }).then(() => {
    DevCameraGetList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function DevCameraHandleExport() {
  proxy.download('biz/DevCamera/export', {
    ...DevCameraQueryParams.value
  }, `摄像头设备_${new Date().getTime()}.xlsx`)
}

/** 下载模板操作 */
function DevCameraImportTemplate() {
  proxy.download(
      "biz/DevCamera/importTemplate",{},
      `摄像头设备_${new Date().getTime()}.xlsx`
  );
}
</script>
<style lang="scss" scoped>

</style>