<template>
  <div class="app-container">
    <el-form @submit.prevent :model="ParkingRoiQueryParams" ref="ParkingRoiQueryRef" :inline="true" v-show="ParkingRoiShowSearch" label-width="auto">
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
      <el-form-item label="车位编号" prop="slotCode">
        <el-input
          v-model="ParkingRoiQueryParams.slot_code"
          placeholder="请输入车位编号"
          clearable
          @keyup.enter="ParkingRoiHandleQuery"
        />
      </el-form-item>
      <el-form-item label="摄像头名称" prop="cameraName">
        <el-input
          v-model="ParkingRoiQueryParams.camera_name"
          placeholder="请输入摄像头名称"
          clearable
          @keyup.enter="ParkingRoiHandleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="ParkingRoiHandleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="ParkingRoiResetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="ParkingRoiHandleAdd"
          v-hasPermi="['biz:ParkingRoi:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="ParkingRoiSingle"
          @click="ParkingRoiHandleUpdate"
          v-hasPermi="['biz:ParkingRoi:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="ParkingRoiMultiple"
          @click="ParkingRoiHandleDelete"
          v-hasPermi="['biz:ParkingRoi:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="ParkingRoiHandleExport"
          v-hasPermi="['biz:ParkingRoi:export']"
        >导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="info"
            plain
            icon="Download"
            @click="ParkingRoiImportTemplate"
            v-hasPermi="['biz:ParkingRoi:import']"
        >下载模版</el-button>
      </el-col>
      <el-col :span="1.5">
      <file-upload
            v-model="importRes"
            v-hasPermi="['biz:ParkingRoi:import']"
            :limit="1"
            uploadBtnText="导入"
            :fileType="['xlsx']"
            accept=".xlsx"
            :action="'/biz/ParkingRoi/importData'"
            :isShowTip="false"
            :isShowFileList="false"
            isImport
          />
      </el-col>
      <right-toolbar v-model:showSearch="ParkingRoiShowSearch" @queryTable="ParkingRoiGetList"></right-toolbar>
    </el-row>

    <el-table v-loading="ParkingRoiLoading" :data="ParkingRoiList" @selection-change="ParkingRoiHandleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="车位编号" align="center" prop="slotCode" />Id" />
      <el-table-column label="摄像头名称" align="center" prop="cameraName" />
      <el-table-column label="图片宽度" align="center" prop="imageWidth" />
      <el-table-column label="图片高度" align="center" prop="imageHeight" />
      <el-table-column width="130" label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="ParkingRoiHandleUpdate(scope.row)" v-hasPermi="['biz:ParkingRoi:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="ParkingRoiHandleDelete(scope.row)" v-hasPermi="['biz:ParkingRoi:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="ParkingRoiTotal>0"
      :total="ParkingRoiTotal"
      v-model:page="ParkingRoiQueryParams.pageNum"
      v-model:limit="ParkingRoiQueryParams.pageSize"
      @pagination="ParkingRoiGetList"
    />

    <!-- 添加或修改车位ROI标注对话框 -->
    <el-dialog :title="ParkingRoiTitle" v-model="ParkingRoiOpen" width="500px" append-to-body destroy-on-close>
      <el-form @submit.prevent ref="ParkingRoiRef" :model="ParkingRoiForm" :rules="ParkingRoiRules" label-width="auto">
        <el-form-item label="车位编号" prop="slotCode">
          <el-input v-model="ParkingRoiForm.slotCode" placeholder="请输入车位编号" />
        </el-form-item>
        <el-form-item label="摄像头名称" prop="cameraName">
          <el-input v-model="ParkingRoiForm.cameraName" placeholder="请输入摄像头名称" />
        </el-form-item>
        <el-form-item label="图片宽度" prop="imageWidth">
          <el-input v-model="ParkingRoiForm.imageWidth" placeholder="请输入图片宽度" />
        </el-form-item>
        <el-form-item label="图片高度" prop="imageHeight">
          <el-input v-model="ParkingRoiForm.imageHeight" placeholder="请输入图片高度" />
        </el-form-item>
        <el-form-item label="ROI多边形坐标(JSON)" prop="roiPolygon">
          <el-input v-model="ParkingRoiForm.roiPolygon" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="ParkingRoiSubmitForm">确 定</el-button>
          <el-button @click="ParkingRoiCancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ParkingRoi">
import { listParkingRoi, getParkingRoi, delParkingRoi, addParkingRoi, updateParkingRoi } from "@/api/biz/ParkingRoi";
import dayjs from "dayjs";
import xeUtils from "xe-utils";

const dateRange = ref([]);

const route = useRoute();
const action = ref(null);

onMounted(() => {
  ParkingRoiHandleQuery();
});

const { proxy } = getCurrentInstance();
const { sys_common_status } = proxy.useDict('sys_common_status');

const importRes = ref(null);
watch(
  () => importRes,
  (val) => {
    if (val.value && val.value.code == 200) {
      ParkingRoiHandleQuery();
    }
  },
  { deep: true, immediate: true }
);

const ParkingRoiList = ref([]);
const ParkingRoiOpen = ref(false);
const ParkingRoiLoading = ref(true);
const ParkingRoiShowSearch = ref(true);
const ParkingRoiIds = ref([]);
const ParkingRoiSingle = ref(true);
const ParkingRoiMultiple = ref(true);
const ParkingRoiTotal = ref(0);
const ParkingRoiTitle = ref("");

const ParkingRoiData = reactive({
  ParkingRoiForm: {},
  ParkingRoiQueryParams: {
    pageNum: 1,
    pageSize: 10,
    slot_code: null,
    camera_name: null
  },
  ParkingRoiRules: {
  }
});

const { ParkingRoiQueryParams, ParkingRoiForm, ParkingRoiRules } = toRefs(ParkingRoiData);

watch(
  route,
  (newRoute) => {
    action.value = newRoute.query && newRoute.query.action;
  },
  { immediate: true }
);

/** 查询车位ROI标注列表 */
function ParkingRoiGetList() {
  ParkingRoiLoading.value = true;
  listParkingRoi(proxy.addDateRange(ParkingRoiQueryParams.value, dateRange.value, 'create_time')).then(response => {
    ParkingRoiList.value = response.rows;
    ParkingRoiTotal.value = response.total;
    ParkingRoiLoading.value = false;
  });
}

// 取消按钮
function ParkingRoiCancel() {
  ParkingRoiOpen.value = false;
  ParkingRoiReset();
}

// 表单重置
function ParkingRoiReset() {
  ParkingRoiForm.value = {
    parkingRoiId: null,
    slotId: null,
    slotCode: null,
    cameraName: null,
    imageWidth: null,
    imageHeight: null,
    roiPolygon: null,
    delFlag: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  };
  proxy.resetForm("ParkingRoiRef");
}

/** 搜索按钮操作 */
function ParkingRoiHandleQuery() {
  ParkingRoiQueryParams.value.pageNum = 1;
  ParkingRoiGetList();
}

/** 重置按钮操作 */
function ParkingRoiResetQuery() {
  dateRange.value = [];
  proxy.resetForm("ParkingRoiQueryRef");
  ParkingRoiHandleQuery();
}

// 多选框选中数据
function ParkingRoiHandleSelectionChange(selection) {
  ParkingRoiIds.value = selection.map(item => item.parkingRoiId);
  ParkingRoiSingle.value = selection.length != 1;
  ParkingRoiMultiple.value = !selection.length;
}

/** 新增按钮操作 */
function ParkingRoiHandleAdd() {
  ParkingRoiReset();
  ParkingRoiOpen.value = true;
  ParkingRoiTitle.value = "添加";
}

/** 修改按钮操作 */
function ParkingRoiHandleUpdate(row) {
  ParkingRoiReset();
  const _parkingRoiId = row.parkingRoiId || ParkingRoiIds.value
  getParkingRoi(_parkingRoiId).then(response => {
    ParkingRoiForm.value = response.data;
    ParkingRoiOpen.value = true;
    ParkingRoiTitle.value = "修改";
  });
}

/** 提交按钮 */
function ParkingRoiSubmitForm() {
  proxy.$refs["ParkingRoiRef"].validate(valid => {
    if (valid) {
        const submitForm = xeUtils.clone(ParkingRoiForm.value, true);
      if (submitForm.parkingRoiId != null) {
        updateParkingRoi(submitForm).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          ParkingRoiOpen.value = false;
          ParkingRoiGetList();
        });
      } else {
        addParkingRoi(submitForm).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          ParkingRoiOpen.value = false;
          ParkingRoiGetList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function ParkingRoiHandleDelete(row) {
  const _parkingRoiIds = row.parkingRoiId || ParkingRoiIds.value;
  proxy.$modal.confirm('是否确认删除编号为"' + _parkingRoiIds + '"的数据项？').then(function() {
    return delParkingRoi(_parkingRoiIds);
  }).then(() => {
    ParkingRoiGetList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function ParkingRoiHandleExport() {
  proxy.download('biz/ParkingRoi/export', {
    ...ParkingRoiQueryParams.value
  }, `车位ROI标注_${new Date().getTime()}.xlsx`)
}

/** 下载模板操作 */
function ParkingRoiImportTemplate() {
  proxy.download(
      "biz/ParkingRoi/importTemplate",{},
      `车位ROI标注_${new Date().getTime()}.xlsx`
  );
}
</script>
<style lang="scss" scoped>

</style>