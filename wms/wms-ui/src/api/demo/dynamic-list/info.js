// "@/api/demo/dynamic-list/info.js"

const listInfo = (params) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 200,
        msg: "success",
        rows: [
          {
            id: 1,
            orderCarDate: "2023-10-01",
            telephone: "1234567890",
            carType: "1",
            remark: "Test Remark",
            carNum: "5",
            carTeam: "1",
            startDate: "2023-10-01",
            endDate: "2023-10-05",
            jobDept: "99",
            jobProject: "1",
            carUsed: "John Doe",
            useCarExplain: "Test Explanation",
            createTime: "2023-10-01 10:00:00",
            updateTime: "2023-10-01 12:00:00",
            createBy: "admin",
            updateBy: "admin",
            enableFlag: 1,
            testTime: "10:00-12:00",
          },
          {
            id: 2,
            orderCarDate: "2023-10-02",
            telephone: "0987654321",
            carType: "2",
            remark: "Another Test Remark",
            carNum: "3",
            carTeam: "2",
            startDate: "2023-10-02",
            endDate: "2023-10-06",
            jobDept: "99",
            jobProject: "2",
            carUsed: "Jane Doe",
            useCarExplain: "Another Test Explanation",
            createTime: "2023-10-02 10:00:00",
            updateTime: "2023-10-02 12:00:00",
            createBy: "admin",
            updateBy: "admin",
            enableFlag: 0,
            testTime: "14:00-16:00",
          },
        ],
        total: 2,
      });
    }, 1000);
  });
};

const saveInfo = (data) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 200,
        msg: "success",
      });
    }, 1000);
  });
};

const dels = (ids) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 200,
        msg: "success",
      });
    }, 1000);
  });
};

const selectDate = () => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        code: 200,
        msg: "success",
        data: [
          { value: "2023-10-01", label: "2023-10-01" },
          { value: "2023-10-02", label: "2023-10-02" },
          { value: "2023-10-03", label: "2023-10-03" },
          { value: "2023-10-04", label: "2023-10-04" },
          { value: "2023-10-05", label: "2023-10-05" },
          { value: "2023-10-06", label: "2023-10-06" },
        ],
      });
    }, 1000);
  });
};

export { listInfo, saveInfo, dels, selectDate };
