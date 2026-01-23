<template>
  <div class="app-container">
    <el-form @submit.prevent :model="AlarmQueryParams" ref="AlarmQueryRef" :inline="true" v-show="AlarmShowSearch" label-width="auto">
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
      <el-form-item label="关联事件ID" prop="eventIdLike">
        <el-input
          v-model="AlarmQueryParams.eventIdLike"
          placeholder="请输入关联事件ID"
          clearable
          @keyup.enter="AlarmHandleQuery"
        />
      </el-form-item>
      <el-form-item label="告警等级" prop="alarmLevel">
        <el-select v-model="AlarmQueryParams.alarmLevel" placeholder="请选择告警等级" clearable>
          <el-option
            v-for="dict in alarm_level"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="告警状态" prop="alarmStatus">
        <el-select v-model="AlarmQueryParams.alarmStatus" placeholder="请选择告警状态" clearable>
          <el-option
            v-for="dict in alarm_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="触发时间" style="width: 308px">
        <el-date-picker
          v-model="AlarmDaterangeTriggerTime"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="AlarmHandleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="AlarmResetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="AlarmHandleAdd"
          v-hasPermi="['biz:Alarm:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="AlarmSingle"
          @click="AlarmHandleUpdate"
          v-hasPermi="['biz:Alarm:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="AlarmMultiple"
          @click="AlarmHandleDelete"
          v-hasPermi="['biz:Alarm:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="AlarmHandleExport"
          v-hasPermi="['biz:Alarm:export']"
        >导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="info"
            plain
            icon="Download"
            @click="AlarmImportTemplate"
            v-hasPermi="['biz:Alarm:import']"
        >下载模版</el-button>
      </el-col>
      <el-col :span="1.5">
      <file-upload
            v-model="importRes"
            v-hasPermi="['biz:Alarm:import']"
            :limit="1"
            uploadBtnText="导入"
            :fileType="['xlsx']"
            accept=".xlsx"
            :action="'/biz/Alarm/importData'"
            :isShowTip="false"
            :isShowFileList="false"
            isImport
          />
      </el-col>
      <right-toolbar v-model:showSearch="AlarmShowSearch" @queryTable="AlarmGetList"></right-toolbar>
    </el-row>

    <el-table v-loading="AlarmLoading" :data="AlarmList" @selection-change="AlarmHandleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="车位编号" align="center" prop="slotCode" />
      <el-table-column label="告警等级" align="center" prop="alarmLevel">
        <template #default="scope">
          <dict-tag :options="alarm_level" :value="scope.row.alarmLevel"/>
        </template>
      </el-table-column>
      <el-table-column label="告警类型" align="center" prop="alarmType" >
        <template #default="scope">
          <dict-tag :options="alarm_type" :value="scope.row.alarmType" />
        </template>
      </el-table-column>
      <el-table-column label="告警状态" align="center" prop="alarmStatus">
        <template #default="scope">
          <dict-tag :options="alarm_status" :value="scope.row.alarmStatus"/>
        </template>
      </el-table-column>
      <el-table-column label="触发时间" align="center" prop="triggerTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.triggerTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column width="130" label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="AlarmHandleUpdate(scope.row)" v-hasPermi="['biz:Alarm:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="AlarmHandleDelete(scope.row)" v-hasPermi="['biz:Alarm:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="AlarmTotal>0"
      :total="AlarmTotal"
      v-model:page="AlarmQueryParams.pageNum"
      v-model:limit="AlarmQueryParams.pageSize"
      @pagination="AlarmGetList"
    />

    <!-- 添加或修改告警记录对话框 -->
    <el-dialog :title="AlarmTitle" v-model="AlarmOpen" width="500px" append-to-body destroy-on-close>
      <el-form @submit.prevent ref="AlarmRef" :model="AlarmForm" :rules="AlarmRules" label-width="auto">
        <el-form-item label="关联事件ID" prop="eventId">
          <el-input v-model="AlarmForm.eventId" placeholder="请输入关联事件ID" />
        </el-form-item>
        <el-form-item label="告警等级" prop="alarmLevel">
          <el-select v-model="AlarmForm.alarmLevel" placeholder="请选择告警等级">
            <el-option
              v-for="dict in alarm_level"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="告警类型" prop="alarmType">
          <el-input v-model="AlarmForm.alarmType" placeholder="请输入告警类型" />
        </el-form-item>
        <el-form-item label="告警状态" prop="alarmStatus">
          <el-select v-model="AlarmForm.alarmStatus" placeholder="请选择告警状态">
            <el-option
              v-for="dict in alarm_status"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="触发时间" prop="triggerTime">
          <el-date-picker clearable style="width: 100%;"
            v-model="AlarmForm.triggerTime"
            type="date"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择触发时间">
          </el-date-picker>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="AlarmSubmitForm">确 定</el-button>
          <el-button @click="AlarmCancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Alarm">
import { listAlarm, getAlarm, delAlarm, addAlarm, updateAlarm } from "@/api/biz/Alarm";
import dayjs from "dayjs";
import xeUtils from "xe-utils";

const dateRange = ref([]);

const route = useRoute();
const action = ref(null);

onMounted(() => {
  AlarmHandleQuery();
});

const { proxy } = getCurrentInstance();
const { sys_common_status, alarm_level, alarm_type, alarm_status } = proxy.useDict('sys_common_status', 'alarm_level', 'alarm_type', 'alarm_status');

const importRes = ref(null);
watch(
  () => importRes,
  (val) => {
    if (val.value && val.value.code == 200) {
      AlarmHandleQuery();
    }
  },
  { deep: true, immediate: true }
);

const AlarmList = ref([]);
const AlarmOpen = ref(false);
const AlarmLoading = ref(true);
const AlarmShowSearch = ref(true);
const AlarmIds = ref([]);
const AlarmSingle = ref(true);
const AlarmMultiple = ref(true);
const AlarmTotal = ref(0);
const AlarmTitle = ref("");
const AlarmDaterangeTriggerTime = ref([]);

const AlarmData = reactive({
  AlarmForm: {},
  AlarmQueryParams: {
    pageNum: 1,
    pageSize: 10,
    eventId: null,
    alarmLevel: null,
    alarmStatus: null,
    triggerTime: null,
  },
  AlarmRules: {
    alarmLevel: [
      { required: true, message: "告警等级不能为空", trigger: "change" }
    ],
    alarmStatus: [
      { required: true, message: "告警状态不能为空", trigger: "change" }
    ],
  }
});

const { AlarmQueryParams, AlarmForm, AlarmRules } = toRefs(AlarmData);

watch(
  route,
  (newRoute) => {
    action.value = newRoute.query && newRoute.query.action;
  },
  { immediate: true }
);

/** 查询告警记录列表 */
function AlarmGetList() {
  AlarmLoading.value = true;
  AlarmQueryParams.value.params = {};
  if (null != AlarmDaterangeTriggerTime.value && '' != AlarmDaterangeTriggerTime.value) {
    AlarmQueryParams.value.params["beginTriggerTime"] = AlarmDaterangeTriggerTime.value[0];
    AlarmQueryParams.value.params["endTriggerTime"] = AlarmDaterangeTriggerTime.value[1];
  }
  listAlarm(proxy.addDateRange(AlarmQueryParams.value, dateRange.value, 'create_time')).then(response => {
    AlarmList.value = response.rows;
    AlarmTotal.value = response.total;
    AlarmLoading.value = false;
  });
}

// 取消按钮
function AlarmCancel() {
  AlarmOpen.value = false;
  AlarmReset();
}

// 表单重置
function AlarmReset() {
  AlarmForm.value = {
    alarmId: null,
    eventId: null,
    cameraId: null,
    slotId: null,
    alarmLevel: null,
    alarmType: null,
    alarmStatus: null,
    triggerTime: null,
    delFlag: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  };
  proxy.resetForm("AlarmRef");
}

/** 搜索按钮操作 */
function AlarmHandleQuery() {
  AlarmQueryParams.value.pageNum = 1;
  AlarmGetList();
}

/** 重置按钮操作 */
function AlarmResetQuery() {
  dateRange.value = [];
  AlarmDaterangeTriggerTime.value = [];
  proxy.resetForm("AlarmQueryRef");
  AlarmHandleQuery();
}

// 多选框选中数据
function AlarmHandleSelectionChange(selection) {
  AlarmIds.value = selection.map(item => item.alarmId);
  AlarmSingle.value = selection.length != 1;
  AlarmMultiple.value = !selection.length;
}

/** 新增按钮操作 */
function AlarmHandleAdd() {
  AlarmReset();
  AlarmOpen.value = true;
  AlarmTitle.value = "添加";
}

/** 修改按钮操作 */
function AlarmHandleUpdate(row) {
  AlarmReset();
  const _alarmId = row.alarmId || AlarmIds.value
  getAlarm(_alarmId).then(response => {
    AlarmForm.value = response.data;
    AlarmOpen.value = true;
    AlarmTitle.value = "修改";
  });
}

/** 提交按钮 */
function AlarmSubmitForm() {
  proxy.$refs["AlarmRef"].validate(valid => {
    if (valid) {
        const submitForm = xeUtils.clone(AlarmForm.value, true);
      if (submitForm.alarmId != null) {
        updateAlarm(submitForm).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          AlarmOpen.value = false;
          AlarmGetList();
        });
      } else {
        addAlarm(submitForm).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          AlarmOpen.value = false;
          AlarmGetList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function AlarmHandleDelete(row) {
  const _alarmIds = row.alarmId || AlarmIds.value;
  proxy.$modal.confirm('是否确认删除编号为"' + _alarmIds + '"的数据项？').then(function() {
    return delAlarm(_alarmIds);
  }).then(() => {
    AlarmGetList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function AlarmHandleExport() {
  proxy.download('biz/Alarm/export', {
    ...AlarmQueryParams.value
  }, `告警记录_${new Date().getTime()}.xlsx`)
}

/** 下载模板操作 */
function AlarmImportTemplate() {
  proxy.download(
      "biz/Alarm/importTemplate",{},
      `告警记录_${new Date().getTime()}.xlsx`
  );
}
</script>
<style lang="scss" scoped>

</style>