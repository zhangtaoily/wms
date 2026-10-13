/* ============ 模拟数据与生成器 ============ */
var WMS_DATA = (function () {
  "use strict";

  /* -------- 基础数据池(取自截图真实样本) -------- */
  var USERS = ["沈雪琴", "庄妍娟", "徐天宇", "王佳祺", "朱颖佳", "周丽燕", "吴玉君", "翟旺杰",
    "沈栋丽", "刘殿会", "吴玉霞", "霍环杰", "楚吉锋", "朱晓佳", "周嘉威", "吴金豆", "王德斌", "管理员"];
  var USER_IDS = ["003467", "009940", "004120", "004227", "007500", "010130", "008485", "003377"];

  var WAREHOUSES = [
    { no: "CK008", name: "立体仓库", erp: "立体仓库", type: "立库(自营)" },
    { no: "01.01", name: "原料仓", erp: "原料仓", type: "普通仓库" },
    { no: "01.02", name: "成品仓", erp: "成品仓", type: "普通仓库" },
    { no: "01.03", name: "备件仓", erp: "备件仓", type: "普通仓库" },
    { no: "01.04", name: "五金仓", erp: "五金仓", type: "普通仓库" },
    { no: "01.05", name: "销售备货仓", erp: "销售备货仓", type: "普通仓库" },
    { no: "02.01", name: "样品仓", erp: "样品仓", type: "普通仓库" },
    { no: "01.06", name: "暂存仓", erp: "暂存仓", type: "普通仓库" },
    { no: "01.07", name: "焊材库", erp: "焊材库", type: "普通仓库" },
    { no: "01.08", name: "辅料配件仓", erp: "辅料配件仓", type: "普通仓库" }
  ];

  var BATCH_PRE = ["QF", "P", "RL", "RJ", "RBL", "RLP", "RLE", "RB", "SL", "F", "JP", "R", "RJP"];
  var VAT_PRE = ["25", "26"];
  var MATERIALS = [
    { code: "5.1.2.002.017", name: "(CASHFEEL)膨体纱", yarn: "2/30 NM", comp: "70% CASHFEEL 30% WOOL" },
    { code: "5.1.1.002.020", name: "(CASHFEEL)精纺羊毛纱", yarn: "2/48 NM", comp: "100% CASHFEEL" },
    { code: "5.1.4.025.001", name: "(包芯纱)防缩羊毛纱", yarn: "2/60 NM", comp: "90% WOOL 10% PA" },
    { code: "3.1.2.004.1021", name: "本白阳离子染纱", yarn: "80S/2", comp: "100% TERYLENE" },
    { code: "5.1.2.002.039", name: "(巴素兰)羊毛纱", yarn: "2/48 NM", comp: "100% WOOL" },
    { code: "5.2.1.003.011", name: "丝光防缩毛纱", yarn: "2/72 NM", comp: "100% WOOL" },
    { code: "3.1.2.004.1008", name: "110s 精梳棉纱", yarn: "110S/1", comp: "100% COTTON" },
    { code: "5.1.3.002.039", name: "(CASHFEEL)染色纱", yarn: "2/48 NM", comp: "70% CASHFEEL" }
  ];
  var COLORS = [["001478", "浅灰兰"], ["006575", "浅兰"], ["A03098", "藏青"], ["B12045", "驼色"],
    ["001201", "元青"], ["C05032", "浅驼"], ["007789", "湖兰"], ["A45201", "深灰"]];
  var CUSTOMERS = ["江苏阳光集团", "山东如意科技", "江苏国泰国际", "宁波申洲针织", "浙江棒杰数码",
    "东莞德永佳", "常州旭荣针织", "佛山张槎针织", "绍兴新乐纺织", "无锡天然纺织"];
  var SALES = ["陈国良", "俞林华", "钟云良", "姚金飞", "沈剑峰"];
  var DEPARTS = ["信息管理部", "产品研发部", "仓储物流部", "纺纱车间", "织造车间", "染整车间", "销售部", "财务部"];
  var COMPANIES = ["浙江云梭纺织股份有限公司", "宁夏云梭羊绒有限公司", "浙江厚源纺织股份有限公司"];
  var QC_GROUPS = ["质检三组", "质检四组", "质检五组"];

  var IN_TYPES = ["采购入库", "产件入库", "调拨入库", "样品入库", "退料入库", "产存入库", "销售退货入库"];
  var OUT_TYPES = ["销售出库", "调拨出库", "领料出库", "直接拣货出库", "复磅检出库", "采购退料出库", "样品出库"];
  var DOC_TYPES = ["生产订单明细", "收料通知单", "调拨出库单", "发货通知单明细", "领料申请单", "采购退料单"];
  var WORK_MODES = ["条码作业", "批次作业"];
  var MOVE_TYPES = ["托盘挪库", "编码移库", "批次移库", "空托移库"];
  var PALLET_TYPES = ["件入条码组托", "传入条码组托", "手动组托"];
  var STATUS_SYNC = ["成功", "成功", "成功", "成功", "未同步"];
  var LOCATIONS = [];
  (function () {
    var zones = [1, 2, 3, 4, 6, 7];
    for (var zi = 0; zi < zones.length; zi++) {
      for (var a = 1; a <= 6; a++) {
        for (var r = 1; r <= 40; r += 7) {
          LOCATIONS.push(zones[zi] + "-" + a + "-" + r + "-1");
          LOCATIONS.push(zones[zi] + "-" + a + "-" + r + "-2");
        }
      }
    }
    for (var y = 60; y <= 240; y += 7) { LOCATIONS.push("Y-" + ("0" + y).slice(-3)); }
  })();

  /* -------- 伪随机(固定种子, 保证数据稳定) -------- */
  var _seed = 20261013;
  function rnd() { _seed = (_seed * 9301 + 49297) % 233280; return _seed / 233280; }
  function ri(min, max) { return min + Math.floor(rnd() * (max - min + 1)); }
  function pick(arr) { return arr[Math.floor(rnd() * arr.length)]; }
  function pad(n, l) { return ("000000000" + n).slice(-l); }
  function chance(p) { return rnd() < p; }
  function fixed(n, d) { return Number(n).toFixed(d || 3); }

  var BASE = new Date(2026, 9, 13); // 2026-10-13
  function dayStr(offsetDays, withTime) {
    var d = new Date(BASE.getTime() - (offsetDays || 0) * 86400000);
    var s = d.getFullYear() + "-" + pad(d.getMonth() + 1, 2) + "-" + pad(d.getDate(), 2);
    if (withTime) s += " " + pad(ri(8, 16), 2) + ":" + pad(ri(0, 59), 2);
    return s;
  }
  function ts(offsetDays) {
    var d = new Date(BASE.getTime() - (offsetDays || 0) * 86400000);
    return d.getFullYear() + "-" + pad(d.getMonth() + 1, 2) + "-" + pad(d.getDate(), 2) + " " +
      pad(ri(8, 16), 2) + ":" + pad(ri(0, 59), 2) + ":" + pad(ri(0, 59), 2);
  }
  function shortTs(offsetDays) {
    var d = new Date(BASE.getTime() - (offsetDays || 0) * 86400000);
    return pad(d.getFullYear() % 100, 2) + "-" + pad(d.getMonth() + 1, 2) + "-" + pad(d.getDate(), 2) +
      " " + pad(ri(8, 16), 2) + ":" + pad(ri(0, 59), 2);
  }

  function batchNo() {
    var pre = pick(BATCH_PRE);
    if (chance(0.3)) return pre + "26" + pad(ri(50, 99), 2) + ri(10000, 99999) + "-" + ri(1, 16);
    return pre + "26" + pad(ri(1, 12), 2) + ri(1000, 9999) + "-" + ri(1, 16) + (chance(0.15) ? "X" : "");
  }
  function vatNo() { return pick(VAT_PRE) + ri(10, 13) + ri(10, 12) + pad(ri(1, 999), 3) + pad(ri(1, 99), 2); }
  function locNo() { return pick(LOCATIONS); }
  function palletNo() { return "000" + ri(10000, 99999); }
  function boxCode() { return "26" + pad(ri(9, 12), 2) + ri(1000000, 9999999) + ri(10, 99); }
  function material() { return pick(MATERIALS); }
  function user() { return pick(USERS); }

  function docNo(prefix, seq, len) {
    return prefix + "26" + pad(10, 2) + pad(13, 2) + pad(seq, len || 5);
  }

  /* -------- 各模块数据生成 -------- */

  function genInbound() {
    var rows = [];
    for (var i = 1; i <= 64; i++) {
      var m = material(), wh = pick(WAREHOUSES);
      rows.push({
        seq: i, no: docNo("B", i, 6), date: dayStr(ri(0, 30)), status: chance(0.9) ? "生效" : "制作中",
        sync: pick(STATUS_SYNC), erpNo: pick(["FBDC772" + ri(10, 99), "CGSL050" + ri(10, 99), "PPBOM00" + ri(10, 99), "DBCK00" + ri(10, 99)]),
        docType: pick(DOC_TYPES), erpInNo: "SDL" + ri(100000, 999999), wh: wh.name,
        inType: pick(IN_TYPES), workMode: pick(WORK_MODES), batch: batchNo(), vat: vatNo(),
        weight: fixed(ri(80, 3200) / 10, 3), boxes: ri(1, 40),
        creator: user(), cTime: shortTs(ri(0, 30)), editor: user(), eTime: shortTs(ri(0, 30))
      });
    }
    return rows;
  }

  function genOutbound() {
    var rows = [];
    for (var i = 1; i <= 55; i++) {
      var wh = pick(WAREHOUSES);
      rows.push({
        seq: i, no: docNo("OS", i, 6), status: chance(0.85) ? "生效" : "制作中",
        erpOrder: pick(["FBDC772" + ri(10, 99), "XSXO0000" + ri(100, 999), "XSCK" + ri(100000, 999999)]),
        customer: pick(CUSTOMERS), sales: pick(SALES), sync: pick(STATUS_SYNC),
        erpOutNo: "XSO" + ri(100000, 999999), date: dayStr(ri(0, 30)), wh: wh.name,
        outType: pick(OUT_TYPES), workMode: pick(WORK_MODES), batch: batchNo(), vat: vatNo(),
        weight: fixed(ri(60, 2800) / 10, 3), boxes: ri(1, 36),
        creator: user(), cTime: shortTs(ri(0, 30)), handler: user(), hTime: shortTs(ri(0, 30))
      });
    }
    return rows;
  }

  function genOutNotice() {
    var rows = [];
    for (var i = 1; i <= 38; i++) {
      rows.push({
        seq: i, no: docNo("BC", i, 6), status: chance(0.9) ? "生效" : "制单中", date: dayStr(ri(0, 30)),
        erpNo: pick(["FBDC772" + ri(10, 99), "CGSL050" + ri(10, 99), "XSXO0000" + ri(100, 999)]),
        erpDate: dayStr(ri(0, 30)), sales: pick(SALES), salesDept: pick(["销售一部", "销售二部", "内销部", "外贸部"]),
        customer: pick(CUSTOMERS), supplier: pick(["云梭智能", "云梭纺织(越南)", "云梭纺织(银川)", ""]),
        qty: fixed(ri(10, 50000) / 10, 3),
        creator: user(), cTime: shortTs(ri(0, 30)), editor: user(), eTime: shortTs(ri(0, 30))
      });
    }
    return rows;
  }

  function genReceiving() {
    var rows = [];
    var sups = [["01.123", "VEN01344"], ["1.009", "VEN01419"], ["云梭智能", "VEN01344"], ["云梭纺织(越南)", "VEN01419"], ["云梭纺织(银川)", "VEN01344"]];
    for (var i = 1; i <= 26; i++) {
      var s = pick(sups);
      rows.push({
        seq: i, no: "RE25" + pad(ri(4, 9), 2) + ri(100000, 999999).toString().slice(0, 4) + pad(i, 4),
        outDate: dayStr(ri(30, 200)), supplier: s[0], supplierCode: s[1],
        outNo: (chance(0.5) ? "OS202" + ri(500000, 999999) : "XSXO0000" + ri(100, 999)),
        tranStatus: pick(["在途", "在途", "在途", "已到货", "已收货"]),
        cTime: shortTs(ri(30, 200)), eTime: shortTs(ri(0, 30))
      });
    }
    return rows;
  }

  function genPalletOrder() {
    var rows = [];
    for (var i = 1; i <= 46; i++) {
      rows.push({
        seq: i, no: docNo("UT", i, 6), status: "生效", date: ts(ri(0, 20)).replace(" ", " ").slice(0, 16),
        pType: pick(PALLET_TYPES), pallet: palletNo(), box: boxCode(),
        batch: batchNo(), vat: chance(0.5) ? vatNo() : "", remark: "",
        creator: user(), editor: user()
      });
    }
    return rows;
  }

  function genCodeCollect() {
    var rows = [];
    for (var i = 1; i <= 33; i++) {
      rows.push({
        seq: i, no: docNo("MD", i, 6), status: chance(0.9) ? "生效" : "制单中", date: dayStr(ri(0, 30)),
        erpNo: pick(["FBDC772" + ri(10, 99), "CGSL050" + ri(10, 99), ""]),
        bizType: pick(["调拨入库", "销售入库", "采购入库", "产件入库"]),
        batch: batchNo(), qty: fixed(ri(10, 5000) / 10, 3),
        creator: user(), cTime: shortTs(ri(0, 30)), editor: user(), eTime: shortTs(ri(0, 30)), remark: ""
      });
    }
    return rows;
  }

  function genWhTask() {
    var rows = [];
    for (var i = 1; i <= 42; i++) {
      var m = material();
      rows.push({
        seq: i, no: docNo("AT", i, 6), status: "生效", date: dayStr(ri(0, 15)),
        tType: pick(["条码出库", "条码出库", "条码入库", "空托出库"]),
        execStatus: pick(["待执行", "待执行", "执行中", "已完成"]),
        batch: batchNo(), material: m.code, taskBatch: batchNo(),
        outType: pick(OUT_TYPES), erpNo: "FBDC772" + ri(10, 99), erpType: pick(DOC_TYPES),
        stdBox: ri(10, 40), boxes: ri(1, 20)
      });
    }
    return rows;
  }

  function genOpTask() {
    var rows = [];
    var ports = ["出口自动", "月台1号回路", "月台2号回路", "5# 自动线"];
    for (var i = 1; i <= 60; i++) {
      rows.push({
        id: 1057700 + i, docId: 1057700 + i, status: "生效",
        pallet: palletNo(), fromLoc: locNo(), toLoc: chance(0.5) ? locNo() : pick(["出口自动", "拆托位"]),
        tType: pick(["条码出库", "条码出库", "条码入库", "托盘挪库"]),
        opType: pick(["出库", "入库", "移库"]), port: pick(ports),
        tTime: ts(ri(0, 6)).slice(0, 19), asrsTime: ts(ri(0, 6)).slice(0, 19),
        execStatus: pick(["执行中", "成功", "成功", "成功", "失败"]), remark: ""
      });
    }
    return rows;
  }

  function genOpLog() {
    var rows = [];
    for (var i = 1; i <= 80; i++) {
      var loc = locNo();
      rows.push({
        seq: i, status: "生效", analysis: pick(["待分析", "已分析", "已分析"]),
        wcsTime: ts(ri(0, 20)), procTime: chance(0.6) ? ts(ri(0, 20)) : "",
        opType: pick(["入库", "出库", "移库"]), result: pick(["正常", "正常", "正常", "异常"]),
        fromLoc: loc, fromName: "", toLoc: locNo(), toName: "",
        pallet: palletNo(), docDate: dayStr(ri(0, 20))
      });
    }
    return rows;
  }

  function genStockCount() {
    var rows = [];
    for (var i = 1; i <= 58; i++) {
      var pieces = ri(0, 31);
      rows.push({
        seq: i, no: docNo("CV", i, 5), status: "生效", date: dayStr(ri(0, 40)),
        workMode: "条码", wh: pick(["样品仓", "原料仓", "成品仓", "立体仓库"]),
        loc: pick(LOCATIONS.filter(function (l) { return l.indexOf("Y-") === 0; }).concat(["Y-066", "Y-107", "Y-075"])),
        batch: batchNo(), vat: vatNo(),
        realPieces: pieces, realWeight: fixed(pieces * ri(28, 52) / 100, 3),
        boxBarcode: boxCode(), boxPieces: pieces, boxWeight: fixed(pieces * ri(28, 52) / 100, 2),
        remark: "", creator: pick(["翟旺杰", "沈栋丽", "徐天宇"]), cTime: shortTs(ri(0, 40)),
        editor: pick(["翟旺杰", "沈栋丽"]), eTime: shortTs(ri(0, 40))
      });
    }
    return rows;
  }

  function genStockMove() {
    var rows = [];
    for (var i = 1; i <= 70; i++) {
      rows.push({
        seq: i, no: docNo("MD", i, 6), date: dayStr(ri(0, 30)), status: "生效",
        erpNo: chance(0.3) ? "DB" + ri(100000, 999999) : "",
        moveType: pick(MOVE_TYPES), batch: chance(0.4) ? batchNo() : "",
        box: chance(0.4) ? boxCode() : "",
        toWh: "立体仓库", toLoc: locNo(), fromWh: chance(0.3) ? "暂存仓" : "", fromLoc: chance(0.3) ? locNo() : "",
        creator: user(), cTime: shortTs(ri(0, 30)), editor: user(), eTime: shortTs(ri(0, 30)), remark: ""
      });
    }
    return rows;
  }

  function genBizBatch() {
    var rows = [];
    for (var i = 1; i <= 72; i++) {
      var m = material();
      rows.push({
        seq: i, no: (chance(0.2) ? "K" : "L") + "26" + pad(ri(1, 12), 2) + ri(10000, 99999) + "-" + ri(1, 16),
        material: m.code, materialName: m.name, yarn: m.yarn,
        date: dayStr(ri(0, 90)) + " " + pad(ri(8, 17), 2) + ":" + pad(ri(0, 59), 2)
      });
    }
    return rows;
  }

  function genInvBatch() {
    var rows = [];
    for (var i = 1; i <= 66; i++) {
      var m = material(), total = ri(0, 120);
      rows.push({
        seq: i, no: batchNo(), bizBatch: batchNo(), vat: vatNo(), comp: m.comp,
        totalBoxes: ri(0, 18), totalPieces: total, totalQty: fixed(total * ri(8, 25) / 10, 2),
        wh: pick(WAREHOUSES).name, loc: locNo(),
        locPieces: ri(0, total), locQty: fixed(ri(0, total) * 1.8, 2),
        boxPieces: ri(0, total), boxQty: fixed(ri(0, total) * 1.8, 2),
        barPieces: ri(0, total), barQty: fixed(ri(0, total) * 1.8, 2)
      });
    }
    return rows;
  }

  function genBoxInfo() {
    var rows = [];
    for (var i = 1; i <= 90; i++) {
      var tubes = chance(0.2) ? 0 : ri(8, 30);
      rows.push({
        seq: i, code: boxCode(), batch: batchNo(), vat: chance(0.6) ? vatNo() : "",
        status: pick(["待入", "在库", "在库", "在库", "已出库"]),
        wh: "立体仓库", loc: locNo(), pallet: palletNo(),
        inTime: chance(0.85) ? shortTs(ri(0, 10)) : "", outTime: chance(0.15) ? shortTs(ri(0, 5)) : "",
        tubes: tubes, stdWeight: fixed(tubes * 1.55, 2), tubeWeight: "1.80",
        grossWeight: fixed(tubes * 1.68, 2), netWeight: fixed(tubes * 1.55, 2),
        waitWeight: chance(0.3) ? fixed(ri(10, 30), 2) : "0.00",
        boxTotal: fixed(ri(80, 160) / 100, 2), unitQty: "0.05",
        realMR: fixed(ri(80, 160) / 10, 2), stdMR: "15.00",
        qcGroup: pick(QC_GROUPS), qcTime: shortTs(ri(0, 10))
      });
    }
    return rows;
  }

  function genPalletInfo() {
    var rows = [];
    for (var i = 1; i <= 48; i++) {
      var empty = chance(0.5), boxes = empty ? 0 : ri(2, 24);
      rows.push({
        seq: i, no: "000" + ri(10000, 31999), empty: empty ? "√" : "",
        loc: empty ? "库外" : locNo(), status: empty ? "闲" : "货",
        asrsStatus: empty ? "" : pick(["入库中", "出库中", ""]),
        emptyQty: empty ? 1 : 0, boxCount: boxes, batch: empty ? "" : batchNo(),
        totalBoxes: boxes, totalWeight: fixed(boxes * ri(15, 28), 3),
        creator: "徐天宇", cTime: "2026-10-08 09:49", editor: "徐天宇", eTime: "2026-10-08 09:49"
      });
    }
    return rows;
  }

  function genLocationInfo() {
    var names = ["包装仓库一", "包装仓库二", "包装仓库三", "五金仓", "电器五金仓", "药配一", "药配二", "药配三",
      "药配四", "药配五", "内配", "外配", "拆配", "原料仓A区", "原料仓B区", "成品仓A区", "立体库1巷道",
      "立体库2巷道", "立体库3巷道", "立体库4巷道", "样品仓货架", "暂存区", "复磅检(一)", "复磅检(二)"];
    var rows = [];
    for (var i = 1; i <= 120; i++) {
      var nm = pick(names), wh = pick(WAREHOUSES);
      var suffix = chance(0.5) ? "" : ri(1, 9) + "架" + ri(1, 4) + "层";
      rows.push({
        seq: i, erpLoc: "10" + ri(1000, 9999), locCode: "KW" + pad(ri(1, 9999), 4),
        locName: nm + suffix, wh: wh.name, erpNo: wh.no, erpName: wh.name,
        lastSync: "24-10-23 12:37", enabled: true,
        creator: "管理员", cTime: "22-01-16 12:23", editor: "管理员", eTime: "22-01-16 12:37"
      });
    }
    return rows;
  }

  function genWarehouseInfo() {
    var extra = ["研发仓", "试样仓", "委托原料仓", "市场仓库", "危险品仓"];
    var list = WAREHOUSES.map(function (w) { return { name: w.name, erp: w.no }; });
    extra.forEach(function (nm) { list.push({ name: nm, erp: "01." + pad(ri(10, 99), 2) }); });
    var rows = [];
    for (var i = 0; i < list.length; i++) {
      rows.push({
        seq: i + 1, name: list[i].name, erpNo: list[i].erp, erpName: list[i].name,
        lastSync: pick(["18-02-21 14:02", "18-10-24 12:27", "24-10-23 12:37", "25-06-18 09:12"]),
        whType: /立体/.test(list[i].name) ? "立库(自营)" : "普通仓库",
        enabled: true, creator: "管理员", cTime: "22-01-16 12:26",
        editor: pick(["周嘉威", "管理员"]), eTime: "26-10-04 10:53"
      });
    }
    return rows;
  }

  function genBizDict() {
    var items = [
      ["AddDays", "月末关账"], ["BHDATE", "扎染日期"], ["CancelPrice", "取消布板价"], ["ChangeBom", "配绒变更"],
      ["ChangeColor", "变更不同大货的提示色"], ["ChangePlan", "主计划变更"], ["ColorLight", "对色光源"],
      ["ColorRequire", "色牢度要求"], ["ColorSystem", "色系"], ["Component", "成分分类"],
      ["Currency", "币种（指定）"], ["CustLevel", "客户类别"], ["DeliveType", "送样形式"], ["DepartMent", "部门"],
      ["DetectionType", "检验类型"], ["EnvRequire", "环保要求"], ["ERPMovePurpose", "调拨用途"],
      ["ERPPurpose", "ERP单据用途"], ["FormulaType", "配方类型"], ["HSFactor", "打样员"],
      ["InType", "入库类型"], ["OutType", "出库类型"], ["PackType", "包装方式"], ["Unit", "单位"]
    ];
    return items.map(function (it, i) {
      return {
        seq: i + 1, code: it[0], name: it[1], creator: "管理员",
        cTime: "20" + ri(20, 24) + "-" + pad(ri(1, 12), 2) + "-" + pad(ri(1, 28), 2),
        editor: "管理员", eTime: "20" + ri(20, 26) + "-" + pad(ri(1, 12), 2) + "-" + pad(ri(1, 28), 2)
      };
    });
  }

  function genSysSetting() {
    var items = [
      ["ForeignLanguageMapping", "系统框架外文映射"], ["APPLanguageIn", "移动端语言包-入库"],
      ["APPLanguagePackage", "移动端语言包"], ["APPLanguageForCarry", "移动端语言包-组托"],
      ["APPLanguageForRecord", "移动端语言包-码单收录"], ["ASARDoorGate", "销售订单业务类型"],
      ["ERPType", "收工方式"], ["WeChatMsg", "企业微信消息"], ["BankAccount", "银企直连账户"],
      ["FlowReportUrlPrefix", "流程报表URL前缀"], ["FTPConfig", "FTP配置"], ["Unit", "单位"],
      ["ISNoticeBill", "采购检"], ["FactGoods", "生产过程检"], ["MacStringByConfig", "根据登录用户标识设置标识"],
      ["OAFlowDBConfig", "OA中间数据库连接信息"], ["PackType", "包装方式"], ["FactLevel", "单据等级"],
      ["HttpConfig", "HttpConfig"], ["WechatAccountInfo", "WechatAccountInfo"]
    ];
    return items.map(function (it, i) {
      return {
        seq: i + 1, code: it[0], name: it[1], creator: "管理员",
        cTime: "20" + ri(19, 24) + "-" + pad(ri(1, 12), 2) + "-" + pad(ri(1, 28), 2),
        editor: "管理员", eTime: "20" + ri(20, 24) + "-" + pad(ri(1, 12), 2) + "-" + pad(ri(1, 28), 2)
      };
    });
  }

  function genModuleGroup() {
    return [
      { seq: 1, code: "WMOperate", name: "仓储作业", icon: "118.gif", sort: 1, creator: "管理员", cTime: "22/01/15 18:09", editor: "管理员", eTime: "24/05/06 14:16" },
      { seq: 2, code: "ASRSOperate", name: "立库作业", icon: "Transfer_16x16.gif", sort: 2, creator: "管理员", cTime: "22/01/14 10:25", editor: "管理员", eTime: "22/01/14 10:25" },
      { seq: 3, code: "RMOperate", name: "库存管理", icon: "03144630.gif", sort: 3, creator: "管理员", cTime: "19/12/12 08:48", editor: "管理员", eTime: "24/05/09 14:04" },
      { seq: 4, code: "MDB", name: "基础数据", icon: "Send_16x16.png", sort: 998, creator: "管理员", cTime: "20/07/03 14:51", editor: "管理员", eTime: "24/05/06 14:22" },
      { seq: 5, code: "SystemSetting", name: "系统管理", icon: "config.png", sort: 999, creator: "管理员", cTime: "20/07/03 14:51", editor: "管理员", eTime: "24/05/06 14:12" }
    ];
  }

  function genFuncModule() {
    var items = [
      ["WMS_PNotice", "出库通知", "仓储作业"], ["WMS_SDLIn", "采购入库", "仓储作业"], ["WMS_Union", "组托单", "仓储作业"],
      ["WMS_Receive", "收货清单", "仓储作业"], ["WMS_In", "入库单", "仓储作业"], ["WMS_Out", "出库单", "仓储作业"],
      ["WMS_CodeCollect", "码单收录", "仓储作业"], ["WMS_ASRSTask", "仓储任务", "立库作业"],
      ["WMS_OpTask", "作业任务", "立库作业"], ["WMS_OpLog", "作业日志", "立库作业"], ["WMS_ScanTerm", "立库扫码终端", "立库作业"],
      ["WMS_StockCount", "盘存单", "库存管理"], ["WMS_StockMove", "移库单", "库存管理"],
      ["WMS_BizBatch", "业务批次", "基础数据"], ["WMS_InvBatch", "库存批次", "基础数据"],
      ["WMS_BoxInfo", "箱码信息", "基础数据"], ["WMS_Pallet", "托盘信息", "基础数据"],
      ["WMS_Location", "库位信息", "基础数据"], ["WMS_Warehouse", "仓库信息", "基础数据"],
      ["WMS_BizDict", "业务字典", "系统管理"], ["WMS_SysSetting", "系统设置", "系统管理"],
      ["WMS_ModuleGroup", "模块组别", "系统管理"], ["WMS_FuncModule", "功能模块", "系统管理"],
      ["WMS_User", "用户信息", "系统管理"], ["WMS_Role", "角色设置", "系统管理"],
      ["WMS_PrintTpl", "打印模板", "系统管理"], ["WMS_Remind", "提醒设置", "系统管理"]
    ];
    return items.map(function (it, i) {
      return {
        seq: i + 1, cls: "UVMMS." + it[0], orgId: 1, id: 1000 + i, group: it[2],
        code: it[0], name: it[1], type: "列表", mainTable: it[0].replace("WMS_", "T_"),
        statusField: "PartState", enabled: true, flow: chance(0.7), log: true,
        path: "UVMMS.WMS." + it[1], icon: "list_16x16.png", sort: i + 1,
        creator: "管理员", editor: "管理员",
        eTime: "20" + ri(22, 25) + "-" + pad(ri(1, 12), 2) + "-" + pad(ri(1, 28), 2)
      };
    });
  }

  function genUserInfo() {
    var names = ["王德斌", "徐天宇", "吴玉霞", "庄新娟", "蔡晓燕", "沈晓红", "李震杰", "杨利亚", "卫爱华",
      "翟跃杰", "夏明芬", "汤丽亚", "沈栋丽", "朱晓佳", "周嘉威", "沈富菊", "劳志佳", "刘晓会", "吴金豆", "周美燕"];
    var rows = [];
    for (var i = 0; i < names.length; i++) {
      rows.push({
        seq: i + 1, account: "0" + (10130 + i * 37 % 8000), userName: names[i],
        enabled: true, empId: "0" + (10130 + i * 37 % 8000), name: names[i],
        dept: pick(DEPARTS), company: pick(COMPANIES), remark: "",
        creator: "管理员", cTime: "2" + ri(1, 4) + "-" + pad(ri(1, 12), 2) + "-" + pad(ri(1, 28), 2),
        editor: "管理员", eTime: "2" + ri(5, 6) + "-" + pad(ri(1, 10), 2) + "-" + pad(ri(1, 28), 2)
      });
    }
    return rows;
  }

  function genRoleSetting() {
    return [
      { seq: 1, name: "成品库仓管员", remark: "", cDate: "2020-09-11", eDate: "2026-05-11" },
      { seq: 2, name: "仓库管理员", remark: "", cDate: "2022-05-19", eDate: "2025-09-03" }
    ];
  }

  function genPrintTemplate() {
    var items = ["托盘标签", "成品条码标签", "样品条码", "条码标签", "样品条码打印", "裁床工艺卡",
      "【大】双面粘贴_全英文", "【大】双面粘贴_二维码", "【小】单面粘贴_全英文", "托盘标签_立库",
      "组托明细标签", "出库单打印模板", "入库单打印模板", "盘存单打印模板", "移库单打印模板",
      "箱码标签_双联", "回单联模板", "码单收录模板", "发货清单模板", "库存批次标签"];
    var mods = ["组托单", "箱码信息", "入库单", "出库单", "盘存单", "移库单", "托盘信息"];
    return items.map(function (nm, i) {
      return {
        seq: i + 1, module: pick(mods), name: nm, enabled: chance(0.85),
        editor: chance(0.9) ? "管理员" : "杨立明",
        eTime: "20" + ri(20, 22) + "-" + pad(ri(1, 12), 2) + "-" + pad(ri(1, 28), 2),
        creator: chance(0.9) ? "管理员" : "杨立明",
        cTime: "20" + ri(20, 22) + "-" + pad(ri(1, 12), 2) + "-" + pad(ri(1, 28), 2)
      };
    });
  }

  function genRemindSetting() {
    var items = ["采购送货单 待完善", "采购入库单", "销售发货 待出库", "受托来料待入库", "委外存件待入库", "领料申请 待出库"];
    var mods = ["采购管理", "采购管理", "销售管理", "仓储作业", "仓储作业", "仓储作业"];
    return items.map(function (nm, i) {
      return {
        sort: i + 1, module: mods[i], name: nm, enabled: true, pk: "ID",
        isPublic: i === 0, allowUsers: pick(["536,582,583", "533,541,542,594", "533,541,542,586,594"]),
        creator: "管理员", cTime: "2020-10-" + pad(ri(10, 31), 2) + " 10:" + pad(ri(0, 59), 2),
        editor: "管理员", eTime: "2021-12-31 14:26"
      };
    });
  }

  /* -------- 单据编辑页明细数据 -------- */
  function genInDetail(n) {
    var rows = [];
    for (var i = 1; i <= (n || ri(3, 9)); i++) {
      var m = material(), tubes = ri(12, 40);
      rows.push({
        seq: i, batch: batchNo(), vat: vatNo(), loc: locNo(),
        colorCode: pick(COLORS)[0], colorName: pick(COLORS)[1], comp: m.comp,
        material: m.code, materialName: m.name, yarn: m.yarn,
        tubes: tubes, stdWeight: fixed(tubes * 1.05, 3), grossWeight: fixed(tubes * 1.18, 3), netWeight: fixed(tubes * 1.02, 3),
        sync: pick(STATUS_SYNC), barcode: boxCode()
      });
    }
    return rows;
  }
  function genPalletDetail(n) {
    var rows = [];
    for (var i = 1; i <= (n || ri(6, 14)); i++) {
      var tubes = ri(20, 26);
      rows.push({
        seq: i, pallet: palletNo(), tubes: tubes, stdWeight: fixed(tubes * 1.02, 3),
        netWeight: fixed(tubes * 0.98, 3), grossWeight: fixed(tubes * 1.12, 3),
        tubeWeight: "0.052", realTube: fixed(ri(40, 60) / 1000, 3), stdTube: "0.052"
      });
    }
    return rows;
  }
  function genAsrsLog(n) {
    var rows = [];
    for (var i = 1; i <= (n || ri(4, 8)); i++) {
      rows.push({
        seq: i, time: ts(ri(0, 3)), action: pick(["入库请求", "入库完成", "出库请求", "出库完成", "拣选确认"]),
        loc: locNo(), pallet: palletNo(), result: pick(["成功", "成功", "成功", "失败"]), msg: ""
      });
    }
    return rows;
  }
  function genOutDetail(n) {
    var rows = [];
    for (var i = 1; i <= (n || ri(2, 6)); i++) {
      var m = material(), tubes = ri(6, 30);
      rows.push({
        seq: i, erpNo: "PPBOM00" + ri(10, 99), erpDate: dayStr(ri(0, 5)), batch: batchNo(),
        batchInfo: m.name, vat: chance(0.4) ? vatNo() : "", boxes: ri(0, 6), tubes: tubes, pieces: tubes,
        stdWeight: fixed(tubes * 1.05, 3), grossWeight: fixed(tubes * 1.15, 3), netWeight: fixed(tubes * 1.0, 3),
        material: m.code, materialName: m.name, yarn: m.yarn, comp: m.comp,
        colorCode: pick(COLORS)[0]
      });
    }
    return rows;
  }
  function genNoticeDetail(n) {
    var rows = [];
    for (var i = 1; i <= (n || ri(3, 8)); i++) {
      var m = material();
      rows.push({
        seq: i, deliveType: pick(["汽运", "快递", "自提"]), batch: batchNo(), vat: vatNo(),
        material: m.code, materialName: m.name, spec: m.yarn, comp: m.comp,
        colorTop: pick(COLORS)[0], colorPiece: pick(COLORS)[0], colorComp: pick(COLORS)[0],
        colorCust: pick(COLORS)[0], color: pick(COLORS)[1],
        qty: fixed(ri(10, 600), 3), unit: "KG", wh: pick(WAREHOUSES).name, loc: locNo()
      });
    }
    return rows;
  }
  function genCodeDetail(n) {
    var rows = [];
    for (var i = 1; i <= (n || ri(6, 16)); i++) {
      var tubes = ri(5, 40);
      rows.push({
        seq: i, barcode: "0" + boxCode(), batch: batchNo(), invBatch: batchNo(),
        tubes: tubes, stdWeight: fixed(tubes * 1.0, 2), grossWeight: fixed(tubes * 1.12, 3),
        netWeight: fixed(tubes * 0.97, 3), tare: "0.000", stdBack: "0.000", realBack: chance(0.2) ? ri(10, 16) + ".000" : "0.000"
      });
    }
    return rows;
  }
  function genNoticeSource(n) {
    var rows = [];
    for (var i = 1; i <= (n || ri(2, 5)); i++) {
      rows.push({
        chk: false, bizNo: "FBDC772" + ri(10, 99), bizBatch: batchNo(), invBatch: batchNo(),
        tubes: ri(4, 40), stdWeight: fixed(ri(40, 400), 2)
      });
    }
    return rows;
  }
  function genCountDetail(n) {
    var rows = [];
    for (var i = 1; i <= (n || ri(1, 4)); i++) {
      var m = material(), pre = ri(5, 30), real = pre + ri(-1, 2);
      rows.push({
        loc: locNo(), locName: "", invBatch: batchNo(), vat: vatNo(),
        preTubes: pre, realTubes: real, diffTubes: real - pre,
        preStd: fixed(pre * 1.05, 3), realStd: fixed(real * 1.05, 3), diffStd: fixed((real - pre) * 1.05, 3),
        preGross: fixed(pre * 1.15, 3), realGross: fixed(real * 1.15, 3), diffGross: fixed((real - pre) * 1.15, 3),
        preNet: fixed(pre * 1.0, 3), realNet: fixed(real * 1.0, 3), diffNet: fixed((real - pre) * 1.0, 3),
        stdMR: "15.00", realMR: fixed(ri(100, 160) / 10, 2),
        colorCode: pick(COLORS)[0], colorName: pick(COLORS)[1],
        material: m.code, yarn: m.yarn, comp: m.comp
      });
    }
    return rows;
  }
  function genCountBoxes(n) {
    var rows = [];
    for (var i = 1; i <= (n || ri(1, 3)); i++) {
      rows.push({
        code: "0" + boxCode(), realStd: fixed(ri(5, 30), 3), realNet: fixed(ri(5, 30), 3),
        realGross: fixed(ri(5, 33), 3), tare: fixed(ri(20, 40) / 100, 3), tubeWeight: fixed(ri(30, 60) / 1000, 3)
      });
    }
    return rows;
  }
  function genMoveDetail(n) {
    var rows = [];
    for (var i = 1; i <= (n || ri(3, 10)); i++) {
      rows.push({
        seq: i, code: "0" + boxCode(), batch: batchNo(), vat: vatNo(),
        toWh: "样品仓", toLoc: "Y-" + ri(100, 300)
      });
    }
    return rows;
  }
  function genUnionDetail(n) {
    var rows = [];
    for (var i = 1; i <= (n || ri(5, 12)); i++) {
      rows.push({
        seq: i, box: boxCode(), markOut: "", boxStatus: "在途", wh: "立体仓库",
        locCode: "LK" + ri(1000, 9999), locName: locNo(), boxCount: 0,
        stdWeight: "0.000", batch: batchNo(), vat: vatNo()
      });
    }
    return rows;
  }
  function genInvBatchDetail(n) {
    var rows = [];
    for (var i = 1; i <= (n || ri(1, 4)); i++) {
      var pieces = ri(4, 30);
      rows.push({
        seq: i, sync: pick(STATUS_SYNC), loc: locNo(), invBatch: batchNo(), vat: vatNo(),
        pieces: pieces, boxes: ri(1, 6), tubes: pieces,
        stdWeight: fixed(pieces * 1.05, 3), grossWeight: fixed(pieces * 1.15, 3), netWeight: fixed(pieces * 0.98, 3),
        comp: pick(MATERIALS).comp, colorCode: pick(COLORS)[0], colorName: pick(COLORS)[1],
        allowQty: fixed(ri(100, 4000), 3)
      });
    }
    return rows;
  }
  function genTaskDetail(n) {
    var rows = [];
    for (var i = 1; i <= (n || ri(1, 3)); i++) {
      rows.push({
        seq: i, taskNo: docNo("AT", ri(1, 999), 6), status: pick(["执行中", "成功"]),
        fromLoc: locNo(), toLoc: pick(["出口自动", "拆托位"]), port: pick(["出口自动", "月台1号回路"]),
        tTime: ts(0).slice(0, 16), exec: pick(["执行中", "成功"])
      });
    }
    return rows;
  }
  function genWhLocations() {
    var rows = [];
    var letters = ["A", "B", "C", "D", "E"];
    var id = 10;
    for (var li = 0; li < letters.length; li++) {
      for (var n = 1; n <= 9; n++) { rows.push({ seq: rows.length + 1, name: letters[li] + n, erpLoc: String(id++), erpName: letters[li] + n, enabled: true, outer: "" }); }
    }
    return rows;
  }
  function genDictItems() {
    return [
      { seq: 1, disp: "1月", val: "1", more1: "6", more2: "", on: true },
      { seq: 2, disp: "2月", val: "2", more1: "6", more2: "", on: true },
      { seq: 3, disp: "3月", val: "3", more1: "6", more2: "", on: true },
      { seq: 4, disp: "4月", val: "4", more1: "6", more2: "", on: true },
      { seq: 5, disp: "5月", val: "5", more1: "6", more2: "", on: true },
      { seq: 6, disp: "6月", val: "6", more1: "6", more2: "", on: true },
      { seq: 7, disp: "7月", val: "7", more1: "6", more2: "", on: true },
      { seq: 8, disp: "8月", val: "8", more1: "6", more2: "", on: true },
      { seq: 9, disp: "9月", val: "9", more1: "6", more2: "", on: true },
      { seq: 10, disp: "10月", val: "10", more1: "6", more2: "", on: true },
      { seq: 11, disp: "11月", val: "11", more1: "6", more2: "", on: true },
      { seq: 12, disp: "12月", val: "12", more1: "7", more2: "", on: true }
    ];
  }
  function genRoleUsers() {
    var names = ["徐天宇", "吴玉霞", "庄新娟", "蔡晓燕", "沈晓红", "李震杰", "杨利亚", "卫爱华", "翟跃杰",
      "夏明芬", "汤丽亚", "沈栋丽", "朱晓佳", "周嘉威", "沈富菊", "劳志佳", "刘晓会"];
    return names.map(function (nm, i) { return { seq: i + 1, account: USER_IDS[i % USER_IDS.length] + (i > 7 ? i : ""), name: nm }; });
  }

  /* -------- 扫码终端任务数据 -------- */
  function genScanTasks() {
    var rows = [];
    for (var i = 1; i <= 7; i++) {
      var need = ri(0, 620), done = ri(0, 100) > 80 ? need : ri(0, need);
      rows.push({
        no: "AT" + ri(22, 26) + ri(10, 12) + ri(10, 30) + pad(ri(1, 999), 4),
        time: "26/" + pad(ri(8, 10), 2) + "/" + pad(ri(1, 28), 2) + " " + pad(ri(8, 16), 2) + ":" + pad(ri(0, 59), 2),
        batch: batchNo(), vat: vatNo(),
        need: fixed(need, 2) + "KG, " + ri(0, 288) + "筒",
        done: fixed(done, 2) + "KG, " + ri(0, 20) + "箱"
      });
    }
    return rows;
  }

  return {
    rnd: rnd, ri: ri, pick: pick, pad: pad, chance: chance, fixed: fixed,
    dayStr: dayStr, ts: ts, shortTs: shortTs, docNo: docNo,
    batchNo: batchNo, vatNo: vatNo, locNo: locNo, palletNo: palletNo, boxCode: boxCode,
    material: material, user: user,
    USERS: USERS, WAREHOUSES: WAREHOUSES, MATERIALS: MATERIALS, COLORS: COLORS, LOCATIONS: LOCATIONS,
    IN_TYPES: IN_TYPES, OUT_TYPES: OUT_TYPES, DOC_TYPES: DOC_TYPES, WORK_MODES: WORK_MODES,
    MOVE_TYPES: MOVE_TYPES, PALLET_TYPES: PALLET_TYPES, CUSTOMERS: CUSTOMERS, SALES: SALES,
    DEPARTS: DEPARTS, COMPANIES: COMPANIES,
    inbound: genInbound, outbound: genOutbound, outNotice: genOutNotice, receiving: genReceiving,
    palletOrder: genPalletOrder, codeCollect: genCodeCollect, whTask: genWhTask, opTask: genOpTask,
    opLog: genOpLog, stockCount: genStockCount, stockMove: genStockMove, bizBatch: genBizBatch,
    invBatch: genInvBatch, boxInfo: genBoxInfo, palletInfo: genPalletInfo, locationInfo: genLocationInfo,
    warehouseInfo: genWarehouseInfo, bizDict: genBizDict, sysSetting: genSysSetting,
    moduleGroup: genModuleGroup, funcModule: genFuncModule, userInfo: genUserInfo,
    roleSetting: genRoleSetting, printTemplate: genPrintTemplate, remindSetting: genRemindSetting,
    inDetail: genInDetail, palletDetail: genPalletDetail, asrsLog: genAsrsLog, outDetail: genOutDetail,
    noticeDetail: genNoticeDetail, codeDetail: genCodeDetail, noticeSource: genNoticeSource,
    countDetail: genCountDetail, countBoxes: genCountBoxes, moveDetail: genMoveDetail,
    unionDetail: genUnionDetail, invBatchDetail: genInvBatchDetail, taskDetail: genTaskDetail,
    whLocations: genWhLocations, dictItems: genDictItems, roleUsers: genRoleUsers,
    scanTasks: genScanTasks
  };
})();
