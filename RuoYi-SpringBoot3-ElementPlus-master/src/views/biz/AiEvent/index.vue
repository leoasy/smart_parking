<template>
  <div class="app-container">
    <el-form @submit.prevent :model="AiEventQueryParams" ref="AiEventQueryRef" :inline="true" v-show="AiEventShowSearch" label-width="auto">
      <el-form-item label="创建时间">
        <el-date-picker
            style="width: 230px;"
            v-model="datetimeRange"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="datetimerange"
            range-separator="-"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item label="事件发生时间" style="width: 308px">
        <el-date-picker
          v-model="AiEventDaterangeEventTime"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="datetimerange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="AiEventHandleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="AiEventResetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="AiEventHandleAdd"
          v-hasPermi="['biz:AiEvent:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="AiEventSingle"
          @click="AiEventHandleUpdate"
          v-hasPermi="['biz:AiEvent:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="AiEventMultiple"
          @click="AiEventHandleDelete"
          v-hasPermi="['biz:AiEvent:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="AiEventHandleExport"
          v-hasPermi="['biz:AiEvent:export']"
        >导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="info"
            plain
            icon="Download"
            @click="AiEventImportTemplate"
            v-hasPermi="['biz:AiEvent:import']"
        >下载模版</el-button>
      </el-col>
      <el-col :span="1.5">
      <file-upload
            v-model="importRes"
            v-hasPermi="['biz:AiEvent:import']"
            :limit="1"
            uploadBtnText="导入"
            :fileType="['xlsx']"
            accept=".xlsx"
            :action="'/biz/AiEvent/importData'"
            :isShowTip="false"
            :isShowFileList="false"
            isImport
          />
      </el-col>
      <right-toolbar v-model:showSearch="AiEventShowSearch" @queryTable="AiEventGetList"></right-toolbar>
    </el-row>

    <el-table v-loading="AiEventLoading" :data="AiEventList" @selection-change="AiEventHandleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="摄像头名称" align="center" prop="cameraName" />
      <el-table-column label="车位编号" align="center" prop="slotCode" />
      <el-table-column label="变更前状态" align="center" prop="oldStatus">
        <template #default="scope">
          <dict-tag :options="ai_event_type" :value="scope.row.oldStatus"/>
        </template>
      </el-table-column>
      <el-table-column label="变更后状态" align="center" prop="newStatus">
        <template #default="scope">
          <dict-tag :options="ai_event_type" :value="scope.row.newStatus"/>
        </template>
      </el-table-column>
      <el-table-column label="置信度" align="center" prop="confidence" />
      <el-table-column label="事件发生时间" align="center" prop="eventTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.eventTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column width="130" label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="AiEventHandleUpdate(scope.row)" v-hasPermi="['biz:AiEvent:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="AiEventHandleDelete(scope.row)" v-hasPermi="['biz:AiEvent:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="AiEventTotal>0"
      :total="AiEventTotal"
      v-model:page="AiEventQueryParams.pageNum"
      v-model:limit="AiEventQueryParams.pageSize"
      @pagination="AiEventGetList"
    />

    <!-- 添加或修改AI推理事件对话框 -->
    <el-dialog :title="AiEventTitle" v-model="AiEventOpen" width="500px" append-to-body destroy-on-close>
      <el-form @submit.prevent ref="AiEventRef" :model="AiEventForm" :rules="AiEventRules" label-width="auto">
        <el-form-item label="车位ID" prop="slotId">
          <el-input v-model="AiEventForm.slotId" placeholder="请输入车位ID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="AiEventSubmitForm">确 定</el-button>
          <el-button @click="AiEventCancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AiEvent">
import { listAiEvent, getAiEvent, delAiEvent, addAiEvent, updateAiEvent } from "@/api/biz/AiEvent";
import dayjs from "dayjs";
import xeUtils from "xe-utils";

const dateRange = ref([]);

const route = useRoute();
const action = ref(null);

onMounted(() => {
  AiEventHandleQuery();
});

const { proxy } = getCurrentInstance();
const { sys_common_status, ai_event_type } = proxy.useDict('sys_common_status', 'ai_event_type');

const importRes = ref(null);
watch(
  () => importRes,
  (val) => {
    if (val.value && val.value.code == 200) {
      AiEventHandleQuery();
    }
  },
  { deep: true, immediate: true }
);

const AiEventList = ref([]);
const AiEventOpen = ref(false);
const AiEventLoading = ref(true);
const AiEventShowSearch = ref(true);
const AiEventIds = ref([]);
const AiEventSingle = ref(true);
const AiEventMultiple = ref(true);
const AiEventTotal = ref(0);
const AiEventTitle = ref("");
const AiEventDaterangeEventTime = ref([]);

const AiEventData = reactive({
  AiEventForm: {},
  AiEventQueryParams: {
    pageNum: 1,
    pageSize: 10,
    cameraId: null,
    eventTime: null,
    framePath: null,
  },
  AiEventRules: {
    cameraId: [
      { required: true, message: "摄像头ID不能为空", trigger: "change" }
    ],
    eventTime: [
      { required: true, message: "事件发生时间不能为空", trigger: "blur" }
    ],
  }
});

const { AiEventQueryParams, AiEventForm, AiEventRules } = toRefs(AiEventData);

watch(
  route,
  (newRoute) => {
    action.value = newRoute.query && newRoute.query.action;
  },
  { immediate: true }
);

/** 查询AI推理事件列表 */
function AiEventGetList() {
  AiEventLoading.value = true;
  AiEventQueryParams.value.params = {};
  if (null != AiEventDaterangeEventTime.value && '' != AiEventDaterangeEventTime.value) {
    AiEventQueryParams.value.params["beginEventTime"] = AiEventDaterangeEventTime.value[0];
    AiEventQueryParams.value.params["endEventTime"] = AiEventDaterangeEventTime.value[1];
  }
  listAiEvent(proxy.addDateRange(AiEventQueryParams.value, dateRange.value, 'create_time')).then(response => {
    AiEventList.value = response.rows;
    AiEventTotal.value = response.total;
    AiEventLoading.value = false;
  });
}

// 取消按钮
function AiEventCancel() {
  AiEventOpen.value = false;
  AiEventReset();
}

// 表单重置
function AiEventReset() {
  AiEventForm.value = {
    eventId: null,
    cameraId: null,
    cameraName: null,
    slotId: null,
    slotCode: null,
    oldStatus: null,
    newStatus: null,
    confidence: null,
    eventTime: null,
    framePath: null,
    delFlag: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  };
  proxy.resetForm("AiEventRef");
}

/** 搜索按钮操作 */
function AiEventHandleQuery() {
  AiEventQueryParams.value.pageNum = 1;
  AiEventGetList();
}

/** 重置按钮操作 */
function AiEventResetQuery() {
  dateRange.value = [];
  AiEventDaterangeEventTime.value = [];
  proxy.resetForm("AiEventQueryRef");
  AiEventHandleQuery();
}

// 多选框选中数据
function AiEventHandleSelectionChange(selection) {
  AiEventIds.value = selection.map(item => item.eventId);
  AiEventSingle.value = selection.length != 1;
  AiEventMultiple.value = !selection.length;
}

/** 新增按钮操作 */
function AiEventHandleAdd() {
  AiEventReset();
  AiEventOpen.value = true;
  AiEventTitle.value = "添加";
}

/** 修改按钮操作 */
function AiEventHandleUpdate(row) {
  AiEventReset();
  const _eventId = row.eventId || AiEventIds.value
  getAiEvent(_eventId).then(response => {
    AiEventForm.value = response.data;
    AiEventOpen.value = true;
    AiEventTitle.value = "修改";
  });
}

/** 提交按钮 */
function AiEventSubmitForm() {
  proxy.$refs["AiEventRef"].validate(valid => {
    if (valid) {
        const submitForm = xeUtils.clone(AiEventForm.value, true);
      if (submitForm.eventId != null) {
        updateAiEvent(submitForm).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          AiEventOpen.value = false;
          AiEventGetList();
        });
      } else {
        addAiEvent(submitForm).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          AiEventOpen.value = false;
          AiEventGetList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function AiEventHandleDelete(row) {
  const _eventIds = row.eventId || AiEventIds.value;
  proxy.$modal.confirm('是否确认删除编号为"' + _eventIds + '"的数据项？').then(function() {
    return delAiEvent(_eventIds);
  }).then(() => {
    AiEventGetList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function AiEventHandleExport() {
  proxy.download('biz/AiEvent/export', {
    ...AiEventQueryParams.value
  }, `AI推理事件_${new Date().getTime()}.xlsx`)
}

/** 下载模板操作 */
function AiEventImportTemplate() {
  proxy.download(
      "biz/AiEvent/importTemplate",{},
      `AI推理事件_${new Date().getTime()}.xlsx`
  );
}
</script>
<style lang="scss" scoped>

</style>