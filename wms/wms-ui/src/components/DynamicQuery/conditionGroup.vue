<template>
  <div :class="{ marginClass: onlyOne }" v-if="reDraw">
    <div style="display: flex; justify-content: flex-start">
      <el-button
        v-if="onlyOne"
        type="primary"
        size="mini"
        style="margin-top: 10px; margin-bottom: 10px; cursor: pointer"
        @click="_addChild"
        >添加筛选条件</el-button
      >
    </div>
    <div class="condition-header" v-if="onlyOne">
      <div v-if="openFloor" class="group-button">
        <el-tooltip content="分组">
          <i
            class="el-icon-edit"
            icon-class="group"
            :style="{
              width: groupBtnSize + 'px',
              height: groupBtnSize + 'px',
              color: '#409e6f',
              cursor: 'pointer',
            }"
            @click.stop="_addGroup"
          ></i>
        </el-tooltip>
      </div>
    </div>
    <div
      v-for="(item, index) in conditionList"
      :key="index"
      :style="{ 'flex-direction': 'column' }"
    >
      <div
        :style="{
          display: 'flex',
          'flex-direction': 'row',
          'align-items': 'center',
        }"
        v-if="!item.groups"
      >
        <div
          :style="{
            display: 'flex',
            'flex-direction': 'row',
            'align-items': 'center',
          }"
        >
          <i
            class="el-icon-circle-plus-outline color-success font-title-large"
            style="cursor: pointer"
            @click="_addItem(item)"
          ></i>
          <i
            class="el-icon-circle-close color-danger font-title-large"
            style="cursor: pointer; margin-left: 5px"
            @click="_delItem(item)"
          ></i>

          <el-checkbox
            v-if="openFloor"
            style="padding: 0 10px 0 10px"
            v-model="item.checked"
          ></el-checkbox>
          <template v-if="floor > 1 && (!item.line || item.line.length == 0)">
            <div
              :style="{
                width: gradWidth + leftWidth * (floor - item.floor - 1) + 'px',
                height: '42px',
              }"
            ></div>
          </template>
          <template v-else>
            <div
              v-for="(n, li) in item.line"
              :key="n.l"
              :style="{
                width:
                  li == item.line.length - 1
                    ? gradWidth + leftWidth * (floor - item.floor) + 'px'
                    : leftWidth + 'px',
                height: '42px',
                background: getFloorColor(li + 1),
              }"
              :class="{
                'group-left': n.l == 2,
                'group-top-left': n.l == 4,
                'group-bottom-left': n.l == 5,
              }"
            >
              <el-tooltip :content="'点击取消所在分组'" v-if="n.l == 4">
                <i
                  class="el-icon-edit"
                  icon-class="group"
                  :style="{
                    width: groupBtnSize + 'px',
                    height: groupBtnSize + 'px',
                    color: '#409e6f',
                    cursor: 'pointer',
                  }"
                  @click="_delGroup(item, n.p)"
                ></i>
              </el-tooltip>
            </div>
          </template>
        </div>

        <!-- 且/或 -->
        <div style="position: relative">
          <div
            v-if="item.header"
            :style="{
              position: 'absolute',
              top: '-23px',
              left: '0px',
              display: 'flex',
              'flex-direction': 'row',
              width: '100%',
            }"
          >
            <div class="condition-header" style="margin-left: calc(50% - 15px)">
              且/或
            </div>
          </div>
          <el-select
            v-model="item.operate"
            style="width: 65px; padding: 5px 0 5px 1px"
            size="small"
          >
            <el-option
              v-for="ot in [
                { key: '且', val: 'AND' },
                { key: '或', val: 'OR' },
              ]"
              :key="ot.val"
              :label="ot.key"
              :value="ot.val"
            >
            </el-option>
          </el-select>
        </div>

        <!-- 字段模块 -->
        <div style="position: relative">
          <div
            v-if="item.header"
            :style="{
              position: 'absolute',
              top: '-23px',
              left: '0px',
              display: 'flex',
              'flex-direction': 'row',
              width: '100%',
            }"
          >
            <div class="condition-header" style="margin-left: calc(50% - 15px)">
              字段
            </div>
          </div>
          <el-select
            v-model="item.field"
            style="width: 100px; margin-left: 10px; padding: 5px 0 5px 0px"
            size="small"
            filterable
            clearable
            @change="fieldChangeHandler(item)"
          >
            <el-option
              v-for="ot in keyOptions"
              :key="ot.val"
              :label="ot.key"
              :value="ot.val"
            >
            </el-option>
          </el-select>
        </div>

        <!-- 运算符模块 -->
        <div style="position: relative">
          <div
            v-if="item.header"
            :style="{
              position: 'absolute',
              top: '-23px',
              left: '0px',
              display: 'flex',
              'flex-direction': 'row',
              width: '100%',
            }"
          >
            <div class="condition-header" style="margin-left: calc(50% - 15px)">
              运算符
            </div>
          </div>
          <el-select
            v-model="item.condition"
            style="width: 80px; margin-left: 10px; padding: 0px"
            size="small"
            @change="conditionChangeHandler(item)"
            filterable
            clearable
          >
            <el-option
              v-for="item in getOperate(getType(item.field))"
              :key="item.val"
              :label="item.key"
              :value="item.val"
            >
            </el-option>
          </el-select>
        </div>

        <!-- 值模块 -->
        <div style="position: relative">
          <div
            v-if="item.header"
            :style="{
              position: 'absolute',
              top: '-23px',
              left: '0px',
              display: 'flex',
              'flex-direction': 'row',
              width: '100%',
            }"
          >
            <div class="condition-header" style="margin-left: calc(10% - 15px)">
              值
            </div>
          </div>
          <el-select
            v-model="item.value"
            v-if="
              valList &&
              valList[item.field] &&
              valList[item.field].dom == 'select'
            "
            style="width: 200px; margin-left: 10px; padding: 5px 0 5px 0px"
            size="small"
          >
            <el-option
              v-for="ot in valList[item.field].data"
              :key="ot.val"
              :label="ot.key"
              :value="ot.val"
            >
            </el-option>
          </el-select>
          <el-date-picker
            v-else-if="isDateType(item)"
            :disabled="datePickerDisabled(item.condition)"
            size="small"
            v-model="item.value"
            type="datetime"
            clearable
            style="
              width: 200px;
              margin-left: 10px;
              cursor: pointer;
              padding: 5px 0 5px 0px;
            "
            :editable="false"
            format="yyyy-MM-dd HH:mm:ss"
            value-format="yyyy-MM-dd HH:mm:ss"
          >
          </el-date-picker>
          <el-input
            v-else
            v-model="item.value"
            style="width: 200px; margin-left: 10px; padding: 5px 0 5px 0px"
            placeholder="值"
            clearable
            size="small"
          />
        </div>
      </div>
      <conditionGroup
        :conditionList="item.groups"
        v-if="item.groups && item.groups.length > 0"
        :only-one="false"
        :parentData="parentData"
        :floor="floor"
        :borderColor="borderColor"
        :keyOptions="keyOptions"
        :val-list="valList"
        :operateMap="operateMap"
      ></conditionGroup>
    </div>
  </div>
</template>

<script>
import { allOperateList, operateMap,dateRangeOperateList } from "@/utils/variables.js";
const condition = {
  id: 1,
  index: 1,
  condition: "",
  operate: "AND",
  field: "",
  value: "",
  checked: false,
  header: true,
  pid: -1,
  floor: 1,
};

const gradWidth = 20;
const leftWidth = 20;
const groupBtnSize = 20;
const alpha = 0.2;

const initData = [Object.assign({}, condition)];

export default {
  name: "conditionGroup",
  components: {},
  props: {
    onlyOne: {
      type: Boolean,
      default: () => true,
    },
    floor: {
      type: Number,
      default: () => 1,
    },
    conditionList: {
      type: Array,
      default: () => initData,
    },
    keyOptions: {
      type: Array,
      default: () => [],
    },
    operateMap: {
      type: Object,
      default: () => {},
    },
    valList: {
      type: Object,
      default: () => {},
    },
    parentData: {
      type: Object,
      default: () => {},
    },
    gradWidth: {
      type: Number,
      default: () => gradWidth,
    },
    leftWidth: {
      type: Number,
      default: () => leftWidth,
    },
    groupBtnSize: {
      type: Number,
      default: () => groupBtnSize,
    },
    borderColor: {
      type: Array,
      default: () => ["rgba(64, 158, 111, " + alpha + ")"],
    },
    openFloor: {
      type: Boolean,
      default: true,
    },
  },
  mounted() {},
  data() {
    return {
      plotList: [],
      loading: false,
      reDraw: true,
      addGroupIndex: 0,
      // borderColor: ['#ffeb3b', '#4caf50', '#ffc107', '#ff9800', '#ff5722', '#2196f3', '#673ab7', '#795548', '#009688', '#009688', '#607d8b', '#9e9e9e', '#926012'],
      allOperateList: allOperateList,
    };
  },
  computed: {
    sidebar() {
      return this.$store.state.app.sidebar.opened;
    },
  },
  watch: {
    conditionList(val, oldVal) {
      this.$emit("input", val);

      while (this.borderColor.length < this.floor) {
        var _color = this.randomHexColor();
        while (this.borderColor.indexOf(_color) != -1) {
          _color = this.randomHexColor();
        }
        this.borderColor.push(_color);
      }

      this.reDraw = false;
      this.$nextTick(() => {
        this.reDraw = true;
      });
    },
    sidebar(val) {},
  },
  methods: {
    getType(field) {
      if (field) {
        return this.keyOptions.find((item) => item.val == field)?.type;
      } else {
        return field;
      }
    },
    // 字段发生改变
    fieldChangeHandler(item) {
      item.value = "";
      item.condition = "";
    },
    findChecked(list, arrParam) {
      var arr = arrParam || new Array();
      for (var i = 0; i < list.length; i++) {
        var o = list[i];
        if (o.groups && o.groups.length > 0) {
          this.findChecked(o.groups, arr);
        } else {
          if (o.checked) {
            arr.push(o);
          }
        }
      }
      return arr;
    },
    removeNode(list, targetList) {
      for (var i = 0; i < list.length; i++) {
        var o = list[i];
        for (var tid of targetList) {
          if (o.id == tid) {
            list.splice(i--, 1);
          }
        }
      }
    },
    findParentGroups(list, pid, retParam) {
      var ret = null || retParam;
      for (var i = 0; i < list.length; i++) {
        var o = list[i];
        if (o.groups && o.groups.length > 0) {
          if (o.id == pid) {
            ret = o;
          } else {
            ret = this.findParentGroups(o.groups, pid, ret);
          }
        }
      }
      return ret;
    },
    _addGroup() {
      this.addGroup(this.parentData.conditionList, this.parentData);
    },
    _delGroup(item, groupId) {
      this.delGroup(groupId, this.parentData.conditionList, this.parentData);
    },
    _addChild() {
      this.addChild(this.parentData.conditionList);
    },
    _delItem(item) {
      this.delItem(
        this.conditionList,
        item,
        this.parentData.conditionList,
        this.parentData
      );
    },
    _addItem(item) {
      this.addItem(
        this.conditionList,
        item.index,
        this.parentData.conditionList,
        this.parentData
      );
    },
    addItem(groups, index, conditionList, parentThis) {
      var newItem = Object.assign({}, condition, {
        id: new Date().getTime(),
        index: index + 1,
        floor: groups[0].floor,
        pid: groups[0].pid,
      });
      groups.splice(index, 0, newItem);

      parentThis.floor = this.refreshData(conditionList);
    },
    addChild(conditionList) {
      var newItem = Object.assign({}, condition, {
        id: new Date().getTime(),
        index: conditionList.length + 1,
        floor: 1,
        pid: -1,
      });
      newItem.header = false;
      conditionList.splice(conditionList.length, 0, newItem);
    },
    delItem(groups, item, conditionList, parentThis) {
      var sum = this.countItem(conditionList);
      if (sum <= 1) {
        return;
      }
      groups.splice(item.index - 1, 1);

      var currentGroups = this.findParentGroups(conditionList, groups[0].pid);
      if (currentGroups) {
        var parentGroups = this.findParentGroups(
          conditionList,
          currentGroups.pid
        );
        if (currentGroups.groups.length == 1) {
          var ag = JSON.parse(JSON.stringify(currentGroups.groups[0]));
          ag.index = currentGroups.index;
          ag.id = currentGroups.id;
          ag.pid = parentGroups ? parentGroups.id : -1;
          ag.floor = currentGroups.floor;
          if (ag.groups) {
            ag.groups.forEach((o, index) => {
              o.pid = ag.id;
              o.floor = ag.floor + 1;
              o.index = index + 1;
            });
          }
          if (parentGroups) {
            var _groups = this.findParentGroups(conditionList, parentGroups.id);
            _groups.groups.splice(currentGroups.index - 1, 1, ag);
          } else {
            conditionList.splice(currentGroups.index - 1, 1, ag);
          }
        }
      }
      if (conditionList.length == 1 && conditionList[0].groups) {
        var newList = JSON.parse(JSON.stringify(conditionList[0].groups));
        conditionList.splice(0, 1);
        for (var nl of newList) {
          nl.pid = -1;
          nl.floor = 1;
          conditionList.push(nl);
        }
      }
      parentThis.floor = this.refreshData(conditionList);
    },
    addGroup(conditionList, parentThis) {
      var checkedList = this.findChecked(conditionList);
      if (!checkedList || checkedList.length <= 1) {
        this.$message({
          message: "至少选择2个查询条目",
          type: "warning",
          duration: 1000,
        });
        return;
      }

      var checkNodes = [];
      for (var item of checkedList) {
        if (item.pid == -1) {
          this.uniquePush(checkNodes, item);
        } else {
          var pNode = this.getRealParent(conditionList, item, checkedList);
          if (pNode) {
            this.uniquePush(checkNodes, pNode);
          }
        }
      }

      var _tmpRoot = [];
      for (var ck of checkNodes) {
        var _tmp = this.findParentGroups(conditionList, ck.pid);
        if (_tmp) {
          this.uniquePush(_tmpRoot, _tmp);
        }
      }

      var allSelectCount = 0;
      var floorCount = [];
      for (var cn of checkNodes) {
        if (cn.groups) {
          allSelectCount += this.countItem(cn.groups);
        } else {
          allSelectCount++;
        }
        if (floorCount.indexOf(cn.floor) == -1) {
          floorCount.push(cn.floor);
        }
      }
      var rootGroup = this.findParentGroups(conditionList, checkNodes[0].pid);
      if (_tmpRoot.length > 1) {
        rootGroup = this.findParentGroups(conditionList, rootGroup.pid);

        allSelectCount = 0;
        for (var cn of _tmpRoot) {
          if (cn.groups) {
            allSelectCount += this.countItem(cn.groups);
          } else {
            allSelectCount++;
          }
        }
      }
      var rootArray = conditionList;
      if (rootGroup) {
        rootArray = rootGroup.groups;
      }
      var allCount = this.countItem(rootArray);

      var currentSelectCount = checkedList.length;

      if (allSelectCount != currentSelectCount || floorCount.length > 1) {
        this.$message({
          message: "不能交叉分组",
          type: "warning",
          duration: 1000,
        });
        return;
      }

      if (checkNodes.length == 1 || allCount == currentSelectCount) {
        this.$message({
          message: "无效分组",
          type: "warning",
          duration: 1000,
        });
        return;
      }

      var newCheckNode = JSON.parse(JSON.stringify(checkNodes));
      newCheckNode.sort(function (a, b) {
        return a.index - b.index;
      });
      var groupId = new Date().getTime();
      var newGroup = {
        groups: newCheckNode,
        id: groupId,
        index: newCheckNode[0].index,
        pid: newCheckNode[0].pid,
        floor: newCheckNode[0].floor,
      };

      var waitRemoveNode = [];
      for (var o of newCheckNode) {
        o.floor += 1;
        o.pid = groupId;
        if (!o.groups) {
          o.checked = false;
        }
        waitRemoveNode.push(o.id);
      }

      if (!rootGroup) {
        this.removeNode(conditionList, waitRemoveNode);
        conditionList.splice(newCheckNode[0].index - 1, 0, newGroup);
      } else {
        var _groups = this.findParentGroups(conditionList, rootGroup.id);
        this.removeNode(_groups.groups, waitRemoveNode);
        _groups.groups.splice(newCheckNode[0].index - 1, 0, newGroup);
      }
      parentThis.floor = this.refreshData(conditionList);
    },
    delGroup(groupId, conditionList, parentThis) {
      var parentGroups = this.findParentGroups(conditionList, groupId);
      var rootGroups = this.findParentGroups(conditionList, parentGroups.pid);

      var waitRemoveNode = [parentGroups.id];
      var newList = JSON.parse(JSON.stringify(parentGroups.groups));
      newList.forEach((o, index) => {
        o.pid = parentGroups.pid;
        o.floor = parentGroups.floor;
        o.checked = false;
      });

      if (!rootGroups) {
        this.removeNode(conditionList, waitRemoveNode);
        newList.forEach((o, index) => {
          conditionList.splice(parentGroups.index - 1 + index, 0, o);
        });
      } else {
        var _groups = this.findParentGroups(conditionList, rootGroups.id);
        this.removeNode(_groups.groups, waitRemoveNode);
        newList.forEach((o, index) => {
          _groups.groups.splice(parentGroups.index - 1 + index, 0, o);
        });
      }
      parentThis.floor = this.refreshData(conditionList);
    },
    getRealParent(allItems, item, checkedList) {
      var parentGroups = this.findParentGroups(allItems, item.pid);
      var ret = parentGroups;
      if (parentGroups) {
        var childCount = this.countItem(parentGroups.groups);
        var realChildCount = 0;
        for (var cl of checkedList) {
          if (cl.pid == parentGroups.id) {
            realChildCount++;
          } else {
            var pg = this.findParentGroups(allItems, cl.pid);
            if (pg) {
              if (pg.pid == parentGroups.id) {
                realChildCount++;
              } else {
                while (pg && pg.pid != parentGroups.id) {
                  pg = this.findParentGroups(allItems, pg.pid);
                  if (pg && pg.pid == parentGroups.id) {
                    realChildCount++;
                  }
                }
              }
            }
          }
        }
        if (childCount == realChildCount) {
          var _tmp = this.getRealParent(allItems, parentGroups, checkedList);
          if (_tmp) {
            ret = _tmp;
          }
        } else {
          ret = item;
        }
      }
      return ret;
    },
    reIndex(list, i, arr) {
      for (var index = 0; index < list.length; index++) {
        var o = list[index];
        if (arr.indexOf(i) == -1) {
          arr.push(i);
        }
        if (o.groups && o.groups.length > 0) {
          o.index = index + 1;
          o.floor = i;
          if (i == 1) {
            o.pid = -1;
          }
          this.reIndex(o.groups, i + 1, arr);
        } else {
          o.index = index + 1;
          o.floor = i;
          o.checked = false;
          if (i == 1) {
            o.pid = -1;
          }
        }
      }
    },
    drawLineGroup(list, currentFloor, retList) {
      for (var index = 0; index < list.length; index++) {
        var o = list[index];
        if (o.groups && o.groups.length > 0) {
          this.drawLineGroup(o.groups, currentFloor + 1, retList);
        } else {
          o.line = new Array(currentFloor - 1);
          if (retList.length == 0) {
            o.header = true;
          } else {
            o.header = false;
          }

          for (var _k = 0; _k < o.line.length; _k++) {
            o.line[_k] = { l: 2, p: -1 };
          }
          retList.push(o);
        }
      }
    },
    refreshData(list) {
      var floorCountArr = [];
      this.reIndex(list, 1, floorCountArr);
      var maxFloor = floorCountArr.length;

      var ret = new Array();
      this.drawLineGroup(list, 1, ret);

      for (var item of ret) {
        var parentGroup = this.findParentGroups(list, item.pid);
        if (item.pid != -1) {
          if (item.index == 1) {
            var node = { l: 4, p: parentGroup.id };
            item.line[item.line.length - 1] = node;
          } else if (item.index == parentGroup.groups.length) {
            var node = { l: 5, p: -1 };
            item.line[item.line.length - 1] = node;
          }
        }
        if (parentGroup) {
          var parentIndex = parentGroup.index;
          var parentLength = parentGroup.groups.length;
          var i = 2;
          var currentParentGroup = this.findParentGroups(list, parentGroup.pid);
          while (currentParentGroup) {
            if (i != 2) {
              parentGroup = JSON.parse(JSON.stringify(currentParentGroup));
              currentParentGroup = this.findParentGroups(list, parentGroup.pid);
            }
            if (currentParentGroup) {
              if (
                parentGroup.index == 1 &&
                item.index == 1 &&
                parentIndex == 1
              ) {
                var node = { l: 4, p: currentParentGroup.id };
                item.line[item.line.length - i] = node;
              } else if (
                parentGroup.index == currentParentGroup.groups.length &&
                item.index == parentLength
              ) {
                item.line[item.line.length - i] = { l: 5, p: -1 };
              } else {
                break;
              }
              i++;
            }
          }
        }
      }
      return maxFloor;
    },
    countItem(list, i) {
      var sum = i || 0;
      for (var index = 0; index < list.length; index++) {
        var o = list[index];
        if (o.groups && o.groups.length > 0) {
          sum += this.countItem(o.groups, i);
        } else {
          sum++;
        }
      }
      return sum;
    },
    uniquePush(arr, item) {
      var exist = false;
      for (var o of arr) {
        if (o.id == item.id) {
          exist = true;
        }
      }
      if (!exist) {
        arr.push(item);
      }
    },
    randomHexColor() {
      // return '#' + ('00000' + (Math.random() * 0x1000000 << 0).toString(16)).substr(-6);
      return this.randomColor(alpha);
    },
    randomColor(alpha) {
      alpha =
        alpha == undefined ? ((Math.random() * 10) / 10).toFixed(1) : alpha;
      alpha = Number(alpha);
      if (isNaN(alpha)) alpha = 1;
      var col = "rgba(";
      for (var i = 0; i < 3; i++) {
        col += parseInt(Math.random() * 256) + ",";
      }
      col += alpha + ")";
      return col;
    },
    getFloorColor(floor) {
      return this.borderColor[floor - 1];
    },
    // 获取操作符
    getOperate(type) {
      if (!type || (!type.startsWith("DATE") && !this.operateMap[type]))
        return this.allOperateList;
      return type.startsWith("DATE")
        ? this.operateMap.DATE
        : this.operateMap[type];
    },
    // 是否是时间类型
    isDateType(item) {
      // 获取类型
      const fieldType = this.getType(item.field);
      return fieldType && fieldType.startsWith("DATE");
    },
    // 时间选择框禁用
    datePickerDisabled(operate) {
      return dateRangeOperateList.includes(
        operate
      );
    },
    // 操作符发生改变
    conditionChangeHandler(item){
      if(this.datePickerDisabled(item.condition)){
        this.$set(item,'value',null)
      }
    }
  },
  created() {
    console.log("this.conditionList", this.conditionList);
    // if (
    //   typeof this.conditionList[0]?.field == "string" &&
    //   typeof this.conditionList[0]?.header == "undefined"
    // ) {
    //   this.conditionList[0].header = true;
    // }
    this.$nextTick(() => {});
  },
};
</script>

<style lang="scss" scoped>
:root {
  --borderWidth: 1px;
  --borderColor: rgba(158, 158, 158, 1);
}

table {
  border-collapse: collapse;
}

.marginClass {
  margin-bottom: 10px;
}

.condition-header {
  font-weight: 600;
  display: flex;
  flex-direction: row;
  height: 20px;
}

.group-button {
  margin-left: 47px;
  display: flex;
  flex-direction: row;
  align-items: center;
}

.group-left {
  border-left: var(--borderWidth) solid var(--borderColor);
}

.group-top-left {
  border-top: var(--borderWidth) solid var(--borderColor);
  border-left: var(--borderWidth) solid var(--borderColor);
}

.group-bottom-left {
  border-bottom: var(--borderWidth) solid var(--borderColor);
  border-left: var(--borderWidth) solid var(--borderColor);
}
::v-deep .el-input--suffix .el-input__inner {
  padding-right: 9px !important;
}
</style>
