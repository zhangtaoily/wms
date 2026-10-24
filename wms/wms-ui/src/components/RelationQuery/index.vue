<template>
  <div>
    <el-dialog
      :title="dialogTitle"
      :visible.sync="showDialog"
      width="80%"
      @close="closerelationQueryDialog"
      append-to-body
    >
      <div style="height: 540px" class="relation-query">
        <div class="left">
          <el-tree
            ref="parentTree"
            :data="parentPact"
            default-expand-all
            :props="defaultProps"
            @node-click="handleNodeClick"
          >
            <span slot-scope="{ node, data }">
              <span>{{ node.label }}</span>
              <span v-show="data.total">{{ " (" + data.total + ")" }}</span>
            </span>
          </el-tree>
          <el-tree
            :data="sonPact"
            default-expand-all
            ref="sonTree"
            @node-click="handleNodeClick"
          >
            <span slot-scope="{ node, data }">
              <span>{{ node.label }}</span>
              <span v-show="data.total">{{ " (" + data.total + ")" }}</span>
            </span>
          </el-tree>
        </div>

        <div class="right">
          <div class="right-content">
            <DynamicList
              v-if="currentFn"
              :functionName="currentFn"
              openMode="list"
              :isFilterScheme="false"
              :showSummary="showSummary"
              :tableHeight="'calc(100vh - 400px)'"
              :openRadioSelection="false"
              :customizeConditions="customizeConditions"
              :key="cpnKey"
              :onlyAcceptConditions="true"
              :relationQueryPath="relationQueryPath"
              :doubleClickOpenNewPage="true"
              :hiddenButtonList="[
                'add',
                'copy',
                'push-down',
                'update',
                'export',
                'print',
                'change',
                'business-operation',
                'custom-column',
              ]"
            ></DynamicList>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { relationQueryList } from "@/utils/variables";

import { getTableList, getDetails } from "@/api/system/common";

export default {
  components: {
    DynamicList: () => import("@/components/DynamicList/index.vue"),
  },
  props: {
    form: {
      type: Object,
    },
    showRelationQuery: {
      type: Boolean,
      required: true,
      default: () => false,
    },
    functionName: {
      type: String,
      default: () => "",
    },
    relationQueryId: {
      type: Array,
      default: () => [],
    },
    // 列表当前选中行
    selectedRow: {
      type: Array,
      default: () => [],
    },
    isFromDetailPage: {
      type: Boolean,
      default: () => false,
    },
    // 选择行的明细id
    selectDetailIds: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      showSummary: false, // 是否显示合计
      relationQueryList: relationQueryList,
      parentPact: [
        {
          id: 0,
          label: "上游单据",
          children: [],
        },
      ],
      sonPact: [
        {
          id: 100,
          label: "下游单据",
          children: [],
        },
      ],
      showDialog: true,
      defaultProps: {
        children: "children",
        label: "label",
      },
      dialogTitle: "单据联查",
      currentFn: "",
      cpnKey: 1,
      customizeConditions: [],
      // 联查列表的路径
      relationQueryPath: "", // 双击联查数据，打开的仍为初始列表的路径
      // 变更单都用的主表id
      changeOrders: [
        "SAL_INT_CONT_C",
        "SAL_CONT_C",
        "SAL_ORDER_C",
        "SAL_PRDNOTICE_C",
        "FI_RBILL", // 核销单关联的应收单, 收款单 都是主表id => 可能导致查出的关联数数据为多个, 不准确, 但产品要求就是这样做了
      ],
    };
  },
  created() {
    this.initTree();
  },
  methods: {
    initTree() {
      // 是否显示合计
      this.showSummary = [
        "SAL_INT_CONT",
        "SAL_CONT",
        "SAL_ORDER",
        "SAL_PRDNOTICE",
        "SAL_NOTICE",
        "PUR_APPLY",
        "SAL_OUTSTOCK",
        "SAL_RENOTICE",
        "SAL_RESTOCK",
      ].includes(this.functionName);
      this.showDialog = this.showRelationQuery;
      // 当前单据
      let currentPact = this.relationQueryList.find(
        (el) => el.currentPact.function == this.functionName
      );

      // 上下游单据数
      this.parentPact[0].children = currentPact.parentPact;
      this.sonPact[0].children = currentPact.sonPact;
      this.dialogTitle = currentPact.currentPact.label + " 单据联查";

      // 获取各个子级单据的数量
      let allRequests = [];
      this.sonPact[0].children.forEach((el) => {
        // 构造每个子单据的查询条件
        // 如果是应收单联查核销单
        if (
          (currentPact.currentPact.function == "FI_AR",
          el.function == "FI_RBILL")
        ) {
          // el.customFilter[0].field = "main_id";
        }
        // 如果是应收单联查核销单
        if (this.isFromDetailPage) {
          el.customFilter = this.selectedRow.map((item) => {
            console.log(item,'item-',el.label)
            // 如果是销售子合同，则用contId查询
            if (el.label == "销售子合同") {
              return {
                logicalOperator: "OR",
                fieldType: "TEXT",
                field: el.field,
                operate: "EQ",
                value: item.contId,
                filterGroup: "ADVANCE",
              };
            } else if(el.label == "物流申请单"){
             return {
                logicalOperator: "OR",
                fieldType: "TEXT",
                field: el.field,
                operate: "EQ",
                value: this.form.id,
                filterGroup: "ADVANCE",
              };
            } else {
              return {
                logicalOperator: "OR",
                fieldType: "TEXT",
                field: el.field,
                operate: "EQ",
                value:
                  el.function != "SAL_PRDNOTICE_C"
                    ? item.id
                    : this.selectedRow.map((el) => el.prdnoticeId).join(","),
                filterGroup: "ADVANCE",
              };
            }
          });
          // 意向合同联查合同，只需要母合同，也就是，加上条件：cont_id字段为空
          if (
            el.function == "SAL_CONT" &&
            this.functionName == "SAL_INT_CONT"
          ) {
            el.customFilter.push({
              logicalOperator: "AND",
              fieldType: "TEXT",
              field: "cont_id",
              operate: "IS_NULL",
              value: "",
              filterGroup: "ADVANCE",
            });
          }
        } else {
          // main_id
          console.log( this.selectedRow,'this.selectedRow-')
          el.customFilter = [
            {
              logicalOperator: "AND",
              fieldType: "TEXT",
              field: el.field,
              operate: "IN",
              // 变更单都是基于主表id变更的, 用明细id查询查不到
              // value: this.changeOrders.includes(el.function)
              //   ? this.selectedRow
              //       .map((el) => (el.main_id ? el.main_id : el.id))
              //       .join(",")
              //   : this.selectedRow.map((el) => el.id).join(","),
              value:
                el.function != "SAL_PRDNOTICE_C" && el.label != "销售子合同"  && 
                el.function != "SAL_LOGISTICS_APPLY" 
                  ? this.selectedRow.map((el) => el.id).join(",")
                  : this.selectedRow.map((el) => el.main_id).join(","),
              filterGroup: "ADVANCE",
            },
          ];

          console.log(this.selectedRow, "main_id", "-------------");

          if (
            el.function == "SAL_CONT" &&
            this.functionName == "SAL_INT_CONT"
          ) {
            el.customFilter.push({
              logicalOperator: "AND",
              fieldType: "TEXT",
              field: "cont_id",
              operate: "IS_NULL",
              value: "",
              filterGroup: "ADVANCE",
            });
          }
        }

        let query = {
          conditions: el.customFilter,
          dialogName: null,
          functionName: el.function,
          pageNum: 1,
          pageSize: 20,
        };

        let request = getTableList(query).then((res) => res.total);
        allRequests.push(request);
      });
      // 一次性请求所有子级单据的数量
      Promise.all(allRequests).then((res) => {
        res.forEach((total, index) => {
          this.$set(this.sonPact[0].children[index], "total", total);

          if (index == res.length - 1) {
            this.$forceUpdate();
          }
        });
      });

      // 获取各个父级单据的数量
      let allParentRequests = [];

      if (this.isFromDetailPage) {
        this.parentPact[0].children.forEach((item) => {
          let filterId;

          // 如果是生产通知单
          if (item.function !== "SAL_PRDNOTICE") {// 建议修改为
            filterId = this.selectedRow
              .map((el) => el[item.field])
              .filter((el) => el !== null && el !== undefined && el !== '');
            item.customFilter = filterId.map((el) => {
              // 如果是意向合同查询根级和父级
              if (this.functionName == "SAL_INT_CONT") {
                return {
                  logicalOperator: "OR",
                  fieldType: "TEXT",
                  field: "erp_code",
                  operate: "EQ",
                  value: el ? el : "undefined",
                  filterGroup: "ADVANCE",
                };
                // 如果是物流申请单查询发货通知单
              } else if(this.functionName == "SAL_LOGISTICS_APPLY"){
                return {
                  logicalOperator: "OR",
                  fieldType: "TEXT",
                  field: "main_id",
                  operate: "EQ",
                  value: el ? el : "undefined",
                  filterGroup: "ADVANCE",
                };
              } else {
                return {
                  logicalOperator: "OR",
                  fieldType: "TEXT",
                  field: "id",
                  operate: "EQ",
                  value: el ? el : "undefined",
                  filterGroup: "ADVANCE",
                };
              }
            });
            // 无有效关联id时兜底：查空而非全量，避免过滤条件为空导致查出所有数据
            if (!filterId.length) {
              item.customFilter = [
                {
                  logicalOperator: "AND",
                  fieldType: "TEXT",
                  field: "id",
                  operate: "EQ",
                  value: "undefined",
                  filterGroup: "ADVANCE",
                },
              ];
            }
          } else {
            filterId = [this.form.sourceId];
            item.customFilter = [
              {
                logicalOperator: "OR",
                fieldType: "TEXT",
                field: "main_id",
                operate: "EQ",
                value: this.form.sourceId,
                filterGroup: "ADVANCE",
              },
            ];
          }
          // 特殊情况: 1.应收单关联核销单的主表id

          if (
            item.function == "FI_AR" &&
            currentPact.currentPact.function == "FI_RBILL"
          ) {
            item.customFilter[0].field = "main_id";
          }

          let query = {
            conditions: item.customFilter,
            dialogName: null,
            functionName: item.function,
            pageNum: 1,
            pageSize: 20,
          };

          let request = getTableList(query).then((res) => res.total);
          allParentRequests.push(request);

          Promise.all(allParentRequests).then((res) => {
            res.forEach((total, index) => {
              this.$set(this.parentPact[0].children[index], "total", total);

              if (index == res.length - 1) {
                this.$forceUpdate();
              }
            });
          });
        });
      } else {
        let parentDetailRequests = [];
        console.log( this.relationQueryId,' this.relationQueryId')
        this.relationQueryId.forEach((selectId) => {
          let request = getDetails(this.functionName, selectId).then(
            (res) => res.data
          );

          parentDetailRequests.push(request);
        });
        Promise.all(parentDetailRequests).then((res) => {
          this.parentPact[0].children.forEach((item) => {
            let { sonSheet } = currentPact.currentPact;
            let filterId = [];
            res.forEach((singleRes) => {
              if (sonSheet) {
                let queryKey = "id";

                // 特殊情况 采购申请单作为销售订单的上游单据 但是关联的为孙表 孙表里的orderGoodsId字段为明细id
                if (item.function == "PUR_APPLY") {
                  sonSheet = "sal_order_goods_assign";
                  queryKey = "orderGoodsId";
                }
                if (item.function == "SAL_PRDNOTICE") {
                  filterId = res.map((v) => v.sourceId);
                }
                let findRelatedObj = singleRes[sonSheet].find((el) => {
                  // return this.relationQueryId.includes(el[queryKey]);
                  return (
                    this.selectDetailIds.includes(el[queryKey]) &&
                    !filterId.includes(el[item.field])
                  );
                });
                if (findRelatedObj && findRelatedObj[item.field]) {
                  filterId.push(findRelatedObj[item.field]);
                }
              } else {
                //区分列表页面和详情页面的收款单联查，因为列表字段是下划线而详情是驼峰
                if (this.form && this.functionName == "FI_RECORD") {
                  //收款单详情
                  filterId.push(
                    ...this.selectedRow.map((el) => el["custreturnDetailId"])
                  );
                } else if (this.form && this.functionName == "FI_RBILL") {
                  //核销单有两个孙标特殊处理
                  filterId.push(
                    ...this.selectedRow.map((el) =>
                      el[item.sonSheet].map((e) => e[item.field])
                    )
                  );
                } else {
                  //其他

                  filterId.push(
                    ...this.selectedRow.map((el) => el[item.field])
                  );
                }
                // filterId = this.selectedRow.map((el) => el[item.field]);
                // filterId.push(...this.selectedRow.map((el) => el[item.field]));
              }
            });
            // 关键优化：过滤掉无效值// 替换现有的过滤逻辑
            filterId = filterId.filter(id => {
              // 排除 null 和 undefined
              if (id == null) return false;

              // 如果是字符串，排除空字符串和只包含空白字符的字符串
              if (typeof id === 'string') {
                return id.trim() !== '';
              }

              // 对于非字符串类型，只要不是空字符串就保留
              return String(id) !== '';
            });
            if (filterId.length) {
              // 如果是意向合同查询根级和父级
              if (this.functionName == "SAL_INT_CONT") {
                item.customFilter = [
                  {
                    logicalOperator: "AND",
                    fieldType: "TEXT",
                    field: "erp_code",
                    operate: "IN",
                    value: filterId.join(","),
                    filterGroup: "ADVANCE",
                  },
                ];
              } else {
                item.customFilter = [
                  {
                    logicalOperator: "AND",
                    fieldType: "TEXT",
                    field: item.function == "SAL_PRDNOTICE" ? "main_id" : "id",
                    operate: "IN",
                    value: filterId.join(","),
                    filterGroup: "ADVANCE",
                  },
                ];
              }
            } else {
              item.customFilter = [
                {
                  logicalOperator: "AND",
                  fieldType: "TEXT",
                  field: "id",
                  operate: "EQ",
                  value: "undefined",
                  filterGroup: "ADVANCE",
                },
              ];
            }

            // 特殊情况: 1.应收单关联核销单的主表id 2.物流申请单 且 需要联查发货通知单
            if (
             ( item.function == "FI_AR" &&
              currentPact.currentPact.function == "FI_RBILL")
              ||
               (this.functionName=="SAL_LOGISTICS_APPLY" && item.function === "SAL_NOTICE")
            ) {
              item.customFilter[0].field = "main_id";
            }

            let query = {
              conditions: item.customFilter,
              dialogName: null,
              functionName: item.function,
              pageNum: 1,
              pageSize: 20,
            };

            let request = getTableList(query).then((res) => res.total);
            allParentRequests.push(request);
          });

          Promise.all(allParentRequests).then((res) => {
            res.forEach((total, index) => {
              this.$set(this.parentPact[0].children[index], "total", total);

              if (index == res.length - 1) {
                this.$forceUpdate();
              }
            });
          });
        });

        // getDetails(this.functionName, this.relationQueryId).then((res) => {
        //   this.parentPact[0].children.forEach((item) => {
        //     let { sonSheet } = currentPact.currentPact;
        //     let filterId;

        //     if (sonSheet) {
        //       let queryKey = "id";

        //       // 特殊情况 采购申请单作为销售订单的上游单据 但是关联的为孙表 孙表里的orderGoodsId字段为明细id
        //       if (item.function == "PUR_APPLY") {
        //         sonSheet = "sal_order_goods_assign";
        //         queryKey = "orderGoodsId";
        //       }
        //       filterId = res.data[sonSheet].find(
        //         (el) => el[queryKey] == this.selectedRow.id
        //       )?.[item.field];
        //     } else {
        //       filterId = this.selectedRow[item.field];
        //     }

        //     item.customFilter = [
        //       {
        //         logicalOperator: "OR",
        //         fieldType: "TEXT",
        //         field: "id",
        //         operate: "EQ",
        //         value: filterId,
        //         filterGroup: "ADVANCE",
        //       },
        //     ];

        //     // 特殊情况: 1.应收单关联核销单的主表id
        //     if (
        //       item.function == "FI_AR" &&
        //       currentPact.currentPact.function == "FI_RBILL"
        //     ) {
        //       item.customFilter[0].field = "main_id";
        //     }

        //     let query = {
        //       conditions: item.customFilter,
        //       dialogName: null,
        //       functionName: item.function,
        //       pageNum: 1,
        //       pageSize: 20,
        //     };
        //     let request = getTableList(query).then((res) => res.total);
        //     allParentRequests.push(request);
        //   });

        //   Promise.all(allParentRequests).then((res) => {
        //     res.forEach((total, index) => {
        //       this.$set(this.parentPact[0].children[index], "total", total);

        //       if (index == res.length - 1) {
        //         this.$forceUpdate();
        //       }
        //     });
        //   });
        // });
      }
    },
    closerelationQueryDialog() {
      this.showDialog = false;
      this.$emit("closeDialog");
    },
    // 点击左侧的上下游单据
    handleNodeClick(node) {
      this.currentFn = node.function;
      this.relationQueryPath = node.path;
      // console.log("this.currentFn :>> ", this.currentFn);
      this.customizeConditions = node.customFilter;
      this.cpnKey++;
    },
  },
};
</script>

<style scoped lang="scss">
.relation-query {
  display: flex;
  justify-content: space-between;
  height: 80%;
  .left {
    width: 180px;
  }
  .right {
    width: calc(100% - 120px);
    overflow-x: auto;
    scrollbar-width: none; /* 隐藏滚动条（Firefox） */
    -ms-overflow-style: none; /* 隐藏滚动条（IE 和 Edge） */
  }
}
</style>
