// @/api/system/organize/dept.js

const listDeptApi = () => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 200,
        data: [
          {
            ancestors: "0",
            code: "99",
            id: "99",
            name: "厂",
            parentId: "0",
            children: [
              {
                ancestors: "0,99",
                code: "test",
                id: "1720269456426344449",
                name: "测试部门",
                parentId: "99",
              },
              {
                ancestors: "0,99",
                code: "102",
                id: "1560428307109957633",
                name: "生产技术室",
                parentId: "99",
              },
              {
                ancestors: "0,99",
                code: "104",
                id: "1560428634399887362",
                name: "设备室",
                parentId: "99",
              },
            ],
          },
        ],
      });
    }, 1000);
  });
};

const optionDeptApi = () => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 200,
        data: [
          {
            ancestors: "0",
            code: "99",
            id: "99",
            name: "厂",
            parentId: "0",
            children: [
              {
                ancestors: "0,99",
                code: "test",
                id: "1720269456426344449",
                name: "测试部门",
                parentId: "99",
              },
              {
                ancestors: "0,99",
                code: "102",
                id: "1560428307109957633",
                name: "生产技术室",
                parentId: "99",
              },
              {
                ancestors: "0,99",
                code: "104",
                id: "1560428634399887362",
                name: "设备室",
                parentId: "99",
              },
            ],
          },
        ],
      });
    }, 1000);
  });
};

const listDeptTreeApi = () => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 200,
        data: [
          {
            ancestors: "0",
            code: "99",
            id: "99",
            name: "厂",
            parentId: "0",
            children: [
              {
                ancestors: "0,99",
                code: "test",
                id: "1720269456426344449",
                name: "测试部门",
                parentId: "99",
              },
              {
                ancestors: "0,99",
                code: "102",
                id: "1560428307109957633",
                name: "生产技术室",
                parentId: "99",
              },
              {
                ancestors: "0,99",
                code: "104",
                id: "1560428634399887362",
                name: "设备室",
                parentId: "99",
              },
            ],
          },
        ],
      });
    }, 1000);
  });
};

const getDeptList = () => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 200,
        data: [
          {
            ancestors: "0",
            code: "99",
            id: "99",
            name: "厂",
            parentId: "0",
            children: [
              {
                ancestors: "0,99",
                code: "test",
                id: "1720269456426344449",
                name: "测试部门",
                parentId: "99",
              },
              {
                ancestors: "0,99",
                code: "102",
                id: "1560428307109957633",
                name: "生产技术室",
                parentId: "99",
              },
              {
                ancestors: "0,99",
                code: "104",
                id: "1560428634399887362",
                name: "设备室",
                parentId: "99",
              },
            ],
          },
        ],
      });
    }, 1000);
  });
};

export { listDeptApi, optionDeptApi, listDeptTreeApi, getDeptList };
