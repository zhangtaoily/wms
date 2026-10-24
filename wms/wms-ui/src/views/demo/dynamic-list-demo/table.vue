<template>
  <div>
    <index ref="tableTemplete" @templete-del="tempDel" @templete-func="tempFunction" :options="options"
      :transfers="transfers" :headerButton="headerButton" :buttons="buttonList" :key="datekey"
      @templete-list="fetchData" @templete-submit="tempSubmit" />
  </div>
</template>

<script>
import index from './index'
import { listInfo, saveInfo, dels, selectDate } from "@/api/demo/dynamic-list/info.js";
import { listDeptApi, optionDeptApi, listDeptTreeApi, getDeptList } from "@/api/demo/dynamic-list/dept.js";
export default {
  name: 'DynamicListTable',
  components: {
    index
  },
  data() {
    return {
      tableList: [],
      total: 0,
      queryForm: [

      ],
      deptOptions: [],
      // 遮罩层
      loading: true,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 20,
      },
      datekey: Date.now(),
      //默认查询条件--需要修改
      transfers: [
        'orderCarDate'
      ],
      //列表字段--需要修改
      options: [
        {
          "createBy": "",
          "createTime": null,
          "updateBy": "",
          "updateTime": null,
          "id": "test",
          "moduleGroup": "DATA_DICT",
          "name": "D_INT_CONT_TYPE",
          "columnName": "code",
          "headerText": "编码",
          "sortIndex": 1,
          "columnWidth": 100,
          "sqlColumn": "code",
          "search": true,
          "dynamic": true
        },
        {
          "createBy": "",
          "createTime": null,
          "updateBy": "",
          "updateTime": null,
          "id": "test1",
          "moduleGroup": "DATA_DICT",
          "name": "D_INT_CONT_TYPE",
          "columnName": "id",
          "headerText": "id",
          "sortIndex": 2,
          "columnWidth": 100,
          "sqlColumn": "id",
          "search": false,
          "dynamic": true
        },
        {
          "createBy": "",
          "createTime": null,
          "updateBy": "",
          "updateTime": null,
          "id": "test3",
          "moduleGroup": "DATA_DICT",
          "name": "D_INT_CONT_TYPE",
          "columnName": "enName",
          "headerText": "中文名称",
          "sortIndex": 3,
          "columnWidth": 100,
          "sqlColumn": "select user_name from sys_user limit 1",
          "search": true,
          "dynamic": true
        }
      ],
      //按钮组件--需要修改
      //头部按钮
      headerButton: [
        {
          label: '打印模版1',
          title: '打印模版信息',
          value: 'print01',
          options: [],
          className: 'fun-export',
          hasPermi: ['dispatch:useCar:export'],
          url:"http://www.baidu.com"
        },
        {
          label: '打印模版2',
          title: '打印模版信息',
          value: 'print02',
          options: [],
          className: 'fun-export',
          hasPermi: ['dispatch:useCar:export'],
          url:"http://www.huya.com"
        },
      ],
      //列表按钮
      buttonList: [
        {
          label: '删除',
          title: '删除信息',
          value: 'del',
          options: [],
          hasPermi: ['trends:table:del'],

        },
        {
          label: '修改',
          title: '修改信息',
          value: 'update',
          options: [],
          hasPermi: ['trends:table:update'],

        }, {
          label: '审批',
          title: '审批信息',
          value: 'approve',
          hasPermi: ['trends:table:update'],
          options: [{
            id: 20,
            name: 'approve-type',
            label: '审批原因',
            type: 'select',
            param: [
              {
                value: 0,
                label: '同意',
              },
              {
                value: 1,
                label: '拒绝',
              },
            ],
            isAdd: true,
            isShow: true,
            isValidate: true,
          },
          {
            id: 21,
            name: 'approve-reason',
            label: '审批意见',
            type: 'textarea',
            param: '',
            isAdd: true,
            isShow: true,
            isValidate: false,
            exp: '',
          }],
        }
      ],
    }
  },
  watch: {},
  created() {
    this.$nextTick(() => {
      //获取部门树
      this.optionDeptApi();
      // 在此处执行你要执行的函数
      this.getList()
    });
  },
  mounted() {
    this.$nextTick(() => {
      //获取部门树
      this.optionDeptApi();
      // 在此处执行你要执行的函数
      this.getList()
    });
  },
  methods: {

    /** 查询部门列表 */
    optionDeptApi() {
      listDeptTreeApi({}).then(response => {
        this.deptOptions = response.data
        this.options[8].param = this.deptOptions
      })
    },
    //其他业务按钮--需要修改
    tempFunction(item, params) {
      if (item.value === 'export') {
        const ids = params.ids;
        const word = ids.length > 0 ? '是否确认导出选中的数据?' : '是否按查询条件导出用车记录?';
        this.queryParams.ids = ids
        this.$confirm(word, "提示", {
          confirmButtonText: "导出",
          cancelButtonText: "取消",
          closeOnClickModal: false,
          type: "info"
        }).then(function () {

        }).then(() => {
          this.download('dispatch/useCarRecord/export', {
            ...this.queryParams
          }, `用车记录.xlsx`)
          this.queryParams.ids = []
        }).catch(function () { });
      }
    },
    //删除
    tempDel(data) {
      this.$confirm('是否确认删除选中的数据项?', "系统提示", {
        closeOnClickModal: false,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(function () {
        return dels(data);
      }).then(() => {
        this.$message({
          message: '删除成功',
          type: 'success'
        });
        this.getList();
      }).catch(function () { });
    },
    //提交--需要修改
    tempSubmit(data, val) {
      console.log(data)
      if ('approve' === val) {
        this.$message({
          message: '审批完成',
          type: 'success'
        });
        // 调用子组件方法
        this.$refs.tableTemplete.submitClose();
      } else {
        if (data.id != undefined) {
          saveInfo(data).then(response => {
            if (response.code === 200) {
              this.$modal.msgSuccess("修改成功");
              this.getList();
            }
            // 调用子组件方法
            this.$refs.tableTemplete.submitClose();
          });
        } else {
          saveInfo(data).then(response => {
            if (response.code === 200) {
              this.$modal.msgSuccess("新增成功");
              this.getList();
              // 调用子组件方法
              this.$refs.tableTemplete.submitClose();
            }
          });
        }
      }

    },

    //组件封装需要修改处--需要修改
    fetchData(arr) {
      if (typeof arr !== 'undefined') {
        let res = JSON.parse(JSON.stringify(arr));
        this.queryParams = res
        if (typeof arr.orderCarDate === 'undefined' || arr.orderCarDate === null) {
          this.queryParams.beginTime = '';
          this.queryParams.endTime = '';
        } else {
          this.queryParams.beginTime = arr.orderCarDate[0];
          this.queryParams.endTime = arr.orderCarDate[1];
        }
        //orderCarDate复制为空字符串，否则为[]，传入后端会报错
        this.queryParams.orderCarDate = '';
        this.queryParams.createTime = '';
        this.queryParams.updateTime = '';
      }
      this.getList()
    },
    // 请求table数据
    getList() {
      this.loading = true;
      listInfo(this.queryParams).then(response => {
        if (response.rows.length == 0 && this.queryParams.pageNum > 1) {
          this.queryParams.pageNum = this.queryParams.pageNum - 1;
          this.total = response.total;
          this.getList();
        } else {
          this.tableList = response.rows;
          this.total = response.total;
          this.loading = false;

          // 调用子组件方法
          this.$refs.tableTemplete.refreshData(this.tableList, this.total);
        }
      }
      );
    },
  },
}
</script>
