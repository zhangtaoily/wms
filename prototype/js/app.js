/* ============ WMS 原型 运行时框架 ============ */
(function () {
  var D = WMS_DATA;

  /* ---------- 基础工具 ---------- */
  function $(id) { return document.getElementById(id); }
  function esc(v) {
    if (v === undefined || v === null) return "";
    return String(v).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
  }
  function p2(n) { return (n < 10 ? "0" : "") + n; }
  function fmtDT(d) {
    return d.getFullYear() + "-" + p2(d.getMonth() + 1) + "-" + p2(d.getDate()) + " " + p2(d.getHours()) + ":" + p2(d.getMinutes()) + ":" + p2(d.getSeconds());
  }
  /* "26-10-13 08:00" / "26/10/13" -> "2026-10-13" */
  function normDate(v) {
    var m = /(\d{2,4})[-\/](\d{1,2})[-\/](\d{1,2})/.exec(String(v || ""));
    if (!m) return "";
    var y = m[1].length === 2 ? "20" + m[1] : m[1];
    return y + "-" + D.pad(m[2], 2) + "-" + D.pad(m[3], 2);
  }
  function toISO(v) {
    var d = normDate(v);
    return d || "2026-10-13"; /* 原型基准日 */
  }

  /* ---------- 全局状态 ---------- */
  var tabs = [];          /* {key,title,closable} */
  var activeTab = null;
  var states = {};        /* key -> {rows,page,pageSize,selRow,q,from,to} */

  /* 底部日期过滤字段(模块 -> 行数据键) */
  var DATE_KEYS = {
    outNotice: "date", outboundOrder: "date", receiving: "outDate", palletOrder: "date",
    codeCollect: "date", inboundOrder: "date", whTask: "date", opTask: "tTime",
    opLog: "docDate", stockCount: "date", stockMove: "date", bizBatch: "date",
    invBatch: "date", boxInfo: "inTime", palletInfo: "cTime", locationInfo: "cTime",
    warehouseInfo: "cTime", bizDict: "cTime", sysSetting: "cTime", moduleGroup: "cTime",
    funcModule: "cTime", userInfo: "cTime", roleSetting: "cDate", printTemplate: "cTime",
    remindSetting: "cTime"
  };

  /* ============ 消息框 ============ */
  var MB_ICON = { warn: "!", info: "i", error: "✕", success: "✓" };
  function box(type, text, withCancel, onOk) {
    var layer = $("dialogLayer");
    var ov = document.createElement("div");
    ov.style.cssText = "position:absolute;inset:0;background:rgba(25,45,70,.22);pointer-events:auto";
    var boxEl = document.createElement("div");
    boxEl.className = "msgbox";
    boxEl.style.cssText = "left:50%;top:36%;transform:translate(-50%,-50%)";
    var html = '<div class="mb-body"><div class="mb-ico ' + type + '">' + MB_ICON[type] + '</div><div class="mb-text">' + esc(text) + "</div></div>";
    html += '<div class="mb-foot"><button class="btn primary" data-mb="ok">确 定</button>';
    if (withCancel) html += '<button class="btn" data-mb="cancel">取 消</button>';
    html += "</div>";
    boxEl.innerHTML = html;
    ov.appendChild(boxEl);
    layer.appendChild(ov);
    function close() { if (ov.parentNode) ov.parentNode.removeChild(ov); }
    boxEl.addEventListener("click", function (e) {
      var b = e.target.closest("button");
      if (!b) return;
      if (b.getAttribute("data-mb") === "ok") { close(); if (onOk) onOk(); }
      else close();
      e.stopPropagation();
    });
    boxEl.querySelector('button[data-mb="ok"]').focus();
  }
  function msgbox(type, text) { box(type, text, false, null); }
  function confirmBox(text, onOk) { box("warn", text, true, onOk); }

  /* ============ 表格构建 ============ */
  function gridHtml(columns, rows, selRow) {
    var s = '<table class="grid"><thead><tr>';
    columns.forEach(function (c) {
      var stl = "min-width:" + (c.w || 60) + "px", thCls = "";
      if (c.fixed) { stl += ";left:" + (c._left || 0) + "px"; thCls = ' class="fixedcol"'; }
      s += "<th" + thCls + ' style="' + stl + '">' + esc(c.t) + "</th>";
    });
    s += "</tr></thead><tbody>";
    rows.forEach(function (r, i) {
      s += "<tr" + (selRow && r === selRow ? ' class="selected"' : "") + ' data-i="' + i + '">';
      columns.forEach(function (c) {
        var raw = r[c.k], v;
        if (c.type === "check") v = raw ? "✓" : "";
        else v = (raw === undefined || raw === null) ? "" : raw;
        var cls = c.cls || "";
        if (c.cmap && c.cmap[raw] != null) cls += " " + c.cmap[raw];
        if (c.fixed) cls += " fixedcol";
        if (c.wrap) cls += " wrapcell";
        var stl = c.fixed ? ' style="left:' + (c._left || 0) + 'px"' : "";
        s += '<td class="' + cls.trim() + '"' + stl + ">" + esc(v) + "</td>";
      });
      s += "</tr>";
    });
    s += "</tbody></table>";
    return s;
  }
  function miniToolbar(list) {
    if (!list || !list.length) return "";
    var s = '<div class="mini-toolbar">';
    list.forEach(function (b) { s += '<button class="btn" data-btn="' + esc(b) + '">' + esc(b) + "</button>"; });
    return s + "</div>";
  }

  /* ============ 左侧菜单 ============ */
  function renderMenu() {
    var mt = $("menuTree");
    MENU_GROUPS.forEach(function (g) {
      var grp = document.createElement("div");
      grp.className = "menu-group open";
      var head = document.createElement("div");
      head.className = "grp";
      head.innerHTML = '<span class="arrow"></span><span class="folder">▤</span>' + esc(g.name);
      head.onclick = function () { grp.classList.toggle("open"); grp.classList.toggle("closed"); };
      var items = document.createElement("div");
      items.className = "items";
      g.items.forEach(function (it) {
        var title = it === "scanTerminal" ? "立库扫码终端" : it === "pdaWork" ? "PDA工作台" : MODULES[it].title;
        var d = document.createElement("div");
        d.className = "menu-item";
        d.setAttribute("data-key", it);
        d.innerHTML = '<span class="doc">▢</span>' + esc(title);
        d.onclick = function () { openTab(it); };
        items.appendChild(d);
      });
      grp.appendChild(head);
      grp.appendChild(items);
      mt.appendChild(grp);
    });
  }
  function setActiveMenu(key) {
    var all = $("menuTree").querySelectorAll(".menu-item");
    for (var i = 0; i < all.length; i++) {
      if (all[i].getAttribute("data-key") === key) all[i].classList.add("active");
      else all[i].classList.remove("active");
    }
  }

  /* ============ 选项卡管理 ============ */
  function findTab(key) {
    for (var i = 0; i < tabs.length; i++) if (tabs[i].key === key) return tabs[i];
    return null;
  }
  function renderTabBar() {
    var bar = $("tabBar");
    bar.innerHTML = "";
    tabs.forEach(function (t) {
      var d = document.createElement("div");
      d.className = "tab" + (t.key === activeTab ? " active" : "") + (t.key === "home" ? " home" : "");
      d.innerHTML = t.key === "home"
        ? '<span class="home-ico">⌂</span> ' + esc(t.title)
        : esc(t.title) + (t.closable ? ' <span class="x" title="关闭">✕</span>' : "");
      d.onclick = function () { activateTab(t.key); };
      var x = d.querySelector(".x");
      if (x) x.onclick = function (e) { e.stopPropagation(); closeTab(t.key); };
      bar.appendChild(d);
    });
  }
  function activateTab(key) {
    activeTab = key;
    var children = $("pageContainer").children;
    for (var i = 0; i < children.length; i++) children[i].classList.remove("active");
    var pg = $("page-" + key);
    if (pg) pg.classList.add("active");
    renderTabBar();
    setActiveMenu(MODULES[key] || key === "scanTerminal" ? key : key);
  }
  function openTab(key) {
    if (!findTab(key)) {
      if (key === "home") {
        tabs.push({ key: "home", title: "主页", closable: false });
        buildHome();
      } else if (key === "scanTerminal") {
        tabs.push({ key: "scanTerminal", title: "立库扫码终端", closable: true });
        buildScan();
      } else if (key === "pdaWork") {
        tabs.push({ key: "pdaWork", title: "PDA工作台", closable: true });
        buildPda();
      } else if (MODULES[key]) {
        tabs.push({ key: key, title: MODULES[key].title, closable: true });
        buildModule(key);
      }
    }
    activateTab(key);
  }
  function closeTab(key) {
    if (key === "home") return;
    for (var i = 0; i < tabs.length; i++) {
      if (tabs[i].key === key) { tabs.splice(i, 1); break; }
    }
    var pg = $("page-" + key);
    if (pg) pg.parentNode.removeChild(pg);
    if (activeTab === key) {
      var last = tabs.length ? tabs[tabs.length - 1].key : "home";
      activateTab(last);
    } else renderTabBar();
  }

  /* ============ 主页 ============ */
  function buildHome() {
    var p = document.createElement("div");
    p.className = "home-page";
    p.id = "page-home";
    var stats = [
      ["今日入库单", D.ri(6, 18), "较昨日 +2"],
      ["今日出库单", D.ri(8, 22), "较昨日 +5"],
      ["待执行任务", D.ri(12, 40), "含立库任务"],
      ["库存批次", D.ri(300, 500), "在库批次总数"],
      ["在库托盘", D.ri(900, 1500), "含空托回库"],
      ["箱码总量", D.ri(20000, 35000), "累计收录箱码"]
    ];
    var s = '<div class="home-inner">';
    s += '<div class="home-banner"><h1>智能仓储管理系统</h1><p>浙江云梭纺织股份有限公司 · 纱线纺织智能仓储数字化平台 · YUNSUO WMS V3.69</p></div>';
    s += '<div class="home-cards">';
    stats.forEach(function (it) {
      s += '<div class="home-card"><div class="hc-label">' + it[0] + '</div><div class="hc-value">' + it[1].toLocaleString() + '</div><div class="hc-extra">' + it[2] + "</div></div>";
    });
    s += "</div>";
    var links = [
      ["outNotice", "出库通知"], ["inboundOrder", "入库单"], ["outboundOrder", "出库单"],
      ["palletOrder", "组托单"], ["stockCount", "盘存单"], ["whTask", "仓储任务"],
      ["scanTerminal", "立库扫码终端"], ["warehouseInfo", "仓库信息"]
    ];
    s += '<div class="home-links"><h3>快捷入口</h3><div style="display:flex;flex-wrap:wrap;gap:8px">';
    links.forEach(function (lk) {
      s += '<span class="lk" data-open="' + lk[0] + '">' + lk[1] + "</span>";
    });
    s += "</div></div></div>";
    p.innerHTML = s;
    p.addEventListener("click", function (e) {
      var lk = e.target.closest("[data-open]");
      if (lk) openTab(lk.getAttribute("data-open"));
    });
    $("pageContainer").appendChild(p);
  }

  /* ============ 模块列表页 ============ */
  function buildModule(key) {
    var mod = MODULES[key];
    var page = document.createElement("div");
    page.className = "page";
    page.id = "page-" + key;
    /* 通用按钮补齐: 列设置(查询选项按钮移至查询框左侧) */
    var tbBtns = mod.toolbar.filter(function (b) { return b !== "查询选项"; });
    if (tbBtns.indexOf("列设置") < 0 && tbBtns.indexOf("显示设置") < 0) tbBtns.push("列设置");
    var s = '<div class="toolbar">';
    tbBtns.forEach(function (b) {
      s += '<button class="btn" data-btn="' + esc(b) + '">' + esc(b) + "</button>";
    });
    s += '<div class="tb-query"><button class="btn" data-btn="查询选项">查询选项</button><input type="text" id="q-' + key + '" placeholder="编号 / 关键字"><button class="btn primary" data-btn="查询">查询</button></div>';
    s += "</div>";
    s += '<div class="grid-wrap" id="gw-' + key + '"></div>';
    s += '<div class="grid-sum" id="gs-' + key + '" style="display:none"></div>';
    s += '<div class="page-foot" id="pf-' + key + '"></div>';
    page.innerHTML = s;
    $("pageContainer").appendChild(page);
    states[key] = {
      rows: mod.gen(), page: 1, pageSize: 50, selRow: null, q: "", from: "", to: "",
      /* 列配置(列设置/查询选项共用) */
      colCfg: mod.columns.map(function (c) {
        var cls = c.cls || "";
        return {
          t: c.t, k: c.k, origCls: cls, cmap: c.cmap || null, type: c.type || null,
          w: c.w || 60,
          align: /(^|\s)ctr(\s|$)/.test(cls) ? "居中" : /(^|\s)num(\s|$)/.test(cls) ? "靠右" : "默认",
          query: true, fixed: false, wrap: false, sort: "未设置", sum: false
        };
      })
    };
    bindModule(key, page);
    renderModule(key);
  }

  /* 由列配置生成渲染列(过滤隐藏列, 生成对齐/固定/折行标志) */
  function effColumns(st) {
    var leftOff = 0;
    return st.colCfg.filter(function (c) { return c.w > 0; }).map(function (c) {
      var cls = String(c.origCls || "").replace(/(^|\s)(num|ctr)(?=\s|$)/g, " ").replace(/\s+/g, " ").trim();
      if (c.align === "居中") cls = (cls + " ctr").trim();
      else if (c.align === "靠右") cls = (cls + " num").trim();
      var col = { t: c.t, k: c.k, w: c.w, cls: cls, cmap: c.cmap, type: c.type };
      if (c.fixed) { col.fixed = true; col._left = leftOff; }
      if (c.wrap) col.wrap = true;
      leftOff += c.w;
      return col;
    });
  }

  function filteredRows(key) {
    var st = states[key];
    var dk = DATE_KEYS[key];
    var q = (st.q || "").toLowerCase();
    var qCols = st.colCfg.filter(function (c) { return c.query; });
    var out = st.rows.filter(function (r) {
      if (q) {
        var hit = false;
        qCols.forEach(function (c) {
          var v = r[c.k];
          if (v !== undefined && v !== null && String(v).toLowerCase().indexOf(q) >= 0) hit = true;
        });
        if (!hit) return false;
      }
      if (dk && (st.from || st.to)) {
        var d = normDate(r[dk]);
        if (!d) return false;
        if (st.from && d < st.from) return false;
        if (st.to && d > st.to) return false;
      }
      return true;
    });
    /* 列设置中配置的排序 */
    var sortCol = null;
    for (var i = 0; i < st.colCfg.length; i++) {
      var sc = st.colCfg[i];
      if (sc.w > 0 && (sc.sort === "正序" || sc.sort === "倒序")) { sortCol = sc; break; }
    }
    if (sortCol) {
      var sk = sortCol.k, desc = sortCol.sort === "倒序";
      var numRe = /^-?\d+(\.\d+)?$/;
      out.sort(function (a, b) {
        var va = a[sk], vb = b[sk], r;
        if (numRe.test(String(va)) && numRe.test(String(vb))) r = parseFloat(va) - parseFloat(vb);
        else r = String(va == null ? "" : va).localeCompare(String(vb == null ? "" : vb), "zh-CN");
        return desc ? -r : r;
      });
    }
    return out;
  }

  function pgbtn(pg, txt, dis) {
    return '<span class="pgbtn" data-pg="' + pg + '"' + (dis ? " disabled" : "") + ">" + txt + "</span>";
  }

  function footHtml(key) {
    var st = states[key], mod = MODULES[key];
    var rows = filteredRows(key);
    var pages = Math.max(1, Math.ceil(rows.length / st.pageSize));
    if (st.page > pages) st.page = pages;
    var s = "<label>" + esc(mod.dateField || "日期") + ' 从 <input type="date" id="from-' + key + '" value="' + st.from + '"> 至 <input type="date" id="to-' + key + '" value="' + st.to + '"> <button class="btn" data-btn="查询" style="height:21px">查询</button></label>';
    s += '<span class="recinfo">记录数: ' + rows.length + "</span>";
    s += '<div class="pager"><span>第 ' + st.page + " 页 / 共 " + pages + " 页</span>";
    s += '<label>每页 <select id="ps-' + key + '">';
    [20, 50, 100].forEach(function (n) {
      s += '<option value="' + n + '"' + (st.pageSize === n ? " selected" : "") + ">" + n + "</option>";
    });
    s += "</select></label>";
    s += pgbtn("first", "|<", st.page <= 1) + pgbtn("prev", "<", st.page <= 1) +
         pgbtn("next", ">", st.page >= pages) + pgbtn("last", ">|", st.page >= pages);
    s += "</div>";
    return s;
  }

  function renderModule(key) {
    var st = states[key];
    var rows = filteredRows(key);
    var gw = $("gw-" + key), pf = $("pf-" + key);
    var cols = effColumns(st);
    gw.innerHTML = rows.length
      ? gridHtml(cols, rows, st.selRow)
      : '<div style="padding:40px;text-align:center;color:#8fa5ba">没有符合条件的记录</div>';
    pf.innerHTML = footHtml(key);
    /* 合计行 */
    var gs = $("gs-" + key);
    if (gs) {
      var sumCols = st.colCfg.filter(function (c) { return c.sum && c.w > 0; });
      if (sumCols.length && rows.length) {
        var parts = [];
        sumCols.forEach(function (c) {
          var total = 0;
          rows.forEach(function (r) {
            var n = parseFloat(r[c.k]);
            if (!isNaN(n)) total += n;
          });
          parts.push("<span>" + esc(c.t) + "合计: " + Number(total.toFixed(3)).toLocaleString() + "</span>");
        });
        gs.innerHTML = parts.join("");
        gs.style.display = "flex";
      } else { gs.innerHTML = ""; gs.style.display = "none"; }
    }
  }

  function bindModule(key, page) {
    /* 工具栏 / 底部按钮(委托) */
    page.addEventListener("click", function (e) {
      var b = e.target.closest("button.btn");
      if (b) { listBtn(key, b.getAttribute("data-btn") || b.textContent.trim()); return; }
      var pg = e.target.closest(".pgbtn");
      if (pg && !pg.hasAttribute("disabled")) {
        var st = states[key];
        var rows = filteredRows(key);
        var pages = Math.max(1, Math.ceil(rows.length / st.pageSize));
        var act = pg.getAttribute("data-pg");
        if (act === "first") st.page = 1;
        else if (act === "prev") st.page = Math.max(1, st.page - 1);
        else if (act === "next") st.page = Math.min(pages, st.page + 1);
        else if (act === "last") st.page = pages;
        renderModule(key);
      }
    });
    /* 底部筛选变化 */
    page.addEventListener("change", function (e) {
      var t = e.target, st = states[key];
      if (t.id === "ps-" + key) { st.pageSize = parseInt(t.value, 10) || 50; st.page = 1; renderModule(key); }
      else if (t.id === "from-" + key) { st.from = t.value; st.page = 1; renderModule(key); }
      else if (t.id === "to-" + key) { st.to = t.value; st.page = 1; renderModule(key); }
    });
    /* 表格行 */
    var gw = $("gw-" + key);
    gw.addEventListener("click", function (e) {
      var tr = e.target.closest("tr[data-i]");
      if (!tr) return;
      states[key].selRow = filteredRows(key)[parseInt(tr.getAttribute("data-i"), 10)];
      renderModule(key);
    });
    gw.addEventListener("dblclick", function (e) {
      var tr = e.target.closest("tr[data-i]");
      if (!tr) return;
      var st = states[key];
      st.selRow = filteredRows(key)[parseInt(tr.getAttribute("data-i"), 10)];
      openEdit(key, st.selRow, MODULES[key].viewOnly ? "view" : "edit");
    });
  }

  function listBtn(key, label) {
    var st = states[key], mod = MODULES[key];
    function needRow() {
      if (!st.selRow) { msgbox("warn", "请先选择一条记录！"); return false; }
      return true;
    }
    switch (label) {
      case "新建": case "新增":
        openEdit(key, null, "new"); break;
      case "编辑":
        if (needRow()) openEdit(key, st.selRow, "edit");
        break;
      case "查看":
        if (needRow()) openEdit(key, st.selRow, "view");
        break;
      case "复制":
        if (needRow()) openEdit(key, st.selRow, "copy");
        break;
      case "删除":
        if (needRow()) {
          var r = st.selRow;
          confirmBox("确定要删除记录 [" + (r.no || r.code || r.name || "") + "] 吗？", function () {
            var idx = st.rows.indexOf(r);
            if (idx >= 0) st.rows.splice(idx, 1);
            st.selRow = null;
            renderModule(key);
            msgbox("success", "删除成功！");
          });
        }
        break;
      case "查询":
        var qi = $("q-" + key);
        if (qi) st.q = qi.value.trim();
        st.page = 1;
        renderModule(key);
        msgbox("success", "查询完成，共 " + filteredRows(key).length + " 条记录。");
        break;
      case "刷新":
        st.q = ""; st.from = ""; st.to = ""; st.page = 1;
        var q2 = $("q-" + key); if (q2) q2.value = "";
        renderModule(key);
        break;
      case "查询选项":
        openQueryOptions(key); break;
      case "显示设置": case "列设置":
        openColumnSettings(key); break;
      default:
        msgbox("info", "原型演示：「" + label + "」操作未实现。");
    }
  }

  /* ============ 编辑弹窗 ============ */
  function fieldHtml(f, v, ro) {
    var ctl;
    var reqAttr = f.req ? ' data-req="1" data-l="' + esc(f.l) + '"' : "";
    if (f.t === "select") {
      var found = false;
      ctl = "<select" + (ro ? " disabled" : "") + reqAttr + ">";
      (f.opts || []).forEach(function (o) {
        var sel = String(o) === String(v);
        if (sel) found = true;
        ctl += "<option" + (sel ? " selected" : "") + ">" + esc(o) + "</option>";
      });
      if (v !== "" && v !== undefined && v !== null && !found) ctl += '<option selected>' + esc(v) + "</option>";
      ctl += "</select>";
    } else if (f.t === "check") {
      ctl = '<input type="checkbox"' + (v ? " checked" : "") + (ro ? " disabled" : "") + ">";
    } else if (f.t === "textarea") {
      ctl = "<textarea" + (ro ? " readonly" : "") + reqAttr + ' style="background:' + (ro ? "#f0f3f7" : "#fff") + '">' + esc(v) + "</textarea>";
    } else if (f.t === "date") {
      ctl = '<input type="date" value="' + toISO(v) + '"' + (ro ? " readonly" : "") + ">";
    } else if (f.t === "password") {
      ctl = '<input type="password" value="' + esc(v) + '"' + (ro ? " readonly" : "") + ">";
    } else {
      ctl = '<input type="text" value="' + esc(v) + '"' + (ro ? " readonly" : "") + reqAttr + ">";
    }
    return '<div class="f-item"><span class="f-label' + (f.req ? " req" : "") + '">' + esc(f.l) + '</span><span class="f-control">' + ctl + "</span></div>";
  }

  function permTreeHtml() {
    var s = '<div style="padding:10px 16px;line-height:2.1;font-size:12px">';
    MENU_GROUPS.forEach(function (g) {
      s += '<div><label style="cursor:pointer;font-weight:bold;color:#1f4e79"><input type="checkbox" checked> ' + esc(g.code + "  " + g.name) + "</label></div>";
      s += '<div style="padding-left:30px">';
      g.items.forEach(function (it) {
        var title = it === "scanTerminal" ? "立库扫码终端" : it === "pdaWork" ? "PDA工作台" : MODULES[it].title;
        s += '<div><label style="cursor:pointer;color:#33475c"><input type="checkbox" checked> ' + esc(title) + "</label></div>";
      });
      s += "</div>";
    });
    return s + "</div>";
  }

  function subPanelHtml(p) {
    var rows = p.gen ? D[p.gen]() : (p.rows || []);
    var s = '<div style="flex:1;min-width:0;display:flex;flex-direction:column;border:1px solid #c7d5e3;background:#fff;border-radius:3px;overflow:hidden">';
    s += '<div style="flex:none;padding:4px 10px;font-weight:bold;color:#1f4e79;background:linear-gradient(180deg,#fdfeff,#e2ecf7);border-bottom:1px solid #c2d2e0">' + esc(p.title) + "</div>";
    s += miniToolbar(p.toolbar);
    s += '<div class="grid-wrap" style="max-height:250px">' + gridHtml(p.columns, rows) + "</div></div>";
    return s;
  }

  function editTabBody(t, row) {
    if (t.remarkOnly) {
      return '<textarea class="remark-box" placeholder="请输入备注信息...">' + esc(row && row.remark || "") + "</textarea>";
    }
    if (t.permTree) return permTreeHtml();
    if (t.twoPanel) {
      return '<div style="display:flex;gap:8px;align-items:stretch">' + subPanelHtml(t.leftPanel) + subPanelHtml(t.rightPanel) + "</div>";
    }
    var rows = t.gen ? D[t.gen]() : (t.rows || []);
    var s = miniToolbar(t.toolbar);
    s += '<div class="grid-wrap">' + gridHtml(t.columns, rows) + "</div>";
    return s;
  }

  function tabGroupHtml(tabsArr, row) {
    var s = '<div class="tab-group"><div class="dtabs">';
    tabsArr.forEach(function (t, i) {
      s += '<div class="dtab' + (i === 0 ? " active" : "") + '" data-ti="' + i + '">' + esc(t.title) + "</div>";
    });
    s += "</div>";
    tabsArr.forEach(function (t, i) {
      s += '<div class="dtab-body' + (t.tall ? " tall" : "") + '" data-bi="' + i + '" style="display:' + (i === 0 ? "block" : "none") + ';margin-bottom:6px">' + editTabBody(t, row) + "</div>";
    });
    return s + "</div>";
  }

  function openEdit(key, row, mode) {
    var mod = MODULES[key];
    var ed = mod.edit || { toolbar: EDIT_TOOLBAR_DEFAULT, header: [] };
    var w = ed.width || 940, hgt = ed.height || 580;
    var left = Math.max(16, Math.round((window.innerWidth - w) / 2));
    var top = Math.max(16, Math.min(90, Math.round((window.innerHeight - hgt) / 2) - 30));
    var layer = $("dialogLayer");

    var ov = document.createElement("div");
    ov.style.cssText = "position:absolute;inset:0;background:rgba(25,45,70,.18);pointer-events:auto";
    layer.appendChild(ov);

    var win = document.createElement("div");
    win.className = "win";
    win.style.cssText = "width:" + w + "px;height:" + hgt + "px;left:" + left + "px;top:" + top + "px";
    win._ctx = { key: key, row: row, mode: mode, ov: ov };

    var ro = (mode === "view");
    var modeName = { new: "新建", edit: "编辑", view: "查看", copy: "复制" }[mode] || "编辑";
    var docId = row ? (row.no || row.code || "") : "";

    var s = '<div class="win-title"><span class="w-ico">▦</span><span class="w-text">' + esc(mod.title) + (docId ? " [" + esc(docId) + "]" : "") + " - " + modeName + '</span><span class="w-btn" data-w="close" title="关闭">✕</span></div>';
    s += '<div class="win-body">';
    s += '<div class="win-toolbar">';
    (ed.toolbar || EDIT_TOOLBAR_DEFAULT).forEach(function (b) {
      s += '<button class="btn" data-btn="' + esc(b) + '"' + (b === "保存" ? ' style="font-weight:bold"' : "") + ">" + esc(b) + "</button>";
    });
    s += "</div>";
    s += '<div class="win-content">';
    /* 表单头 */
    if (ed.header && ed.header.length) {
      s += '<div class="form-grid">';
      ed.header.forEach(function (fr) {
        s += '<div class="form-row">';
        fr.forEach(function (f) {
          var v = row ? row[f.k] : (f.t === "date" ? "26-10-13" : "");
          if (f.k.charAt(0) === "_") v = row ? row[f.k] : (f.opts ? f.opts[0] : "");
          s += fieldHtml(f, v, ro || f.ro);
        });
        s += "</div>";
      });
      s += "</div>";
    }
    /* 选项卡 */
    if (ed.tabs && ed.tabs.length) s += tabGroupHtml(ed.tabs, row);
    if (ed.lowerTabs && ed.lowerTabs.length) {
      s += '<div style="height:8px;flex:none"></div>';
      s += tabGroupHtml(ed.lowerTabs, row);
    }
    s += "</div></div>";
    /* 底部信息 */
    var foot = row
      ? "创建人: " + esc(row.creator || "-") + "　创建时间: " + esc(row.cTime || row.cDate || "-") +
        "　最后修改人: " + esc(row.editor || "-") + "　最后修改时间: " + esc(row.eTime || row.eDate || "-")
      : "创建人: -　创建时间: -　最后修改人: -　最后修改时间: -";
    s += '<div class="win-foot">' + foot + "</div>";
    win.innerHTML = s;
    layer.appendChild(win);

    /* 标题栏关闭 + 拖动 */
    win.querySelector('.w-btn[data-w="close"]').onclick = function () { closeWin(win); };
    enableDrag(win);
  }

  /* 窗口标题栏拖动 */
  function enableDrag(win) {
    var title = win.querySelector(".win-title");
    if (!title) return;
    title.addEventListener("mousedown", function (e) {
      if (e.target.classList.contains("w-btn")) return;
      var sx = e.clientX - win.offsetLeft, sy = e.clientY - win.offsetTop;
      function mv(ev) {
        win.style.left = (ev.clientX - sx) + "px";
        win.style.top = (ev.clientY - sy) + "px";
      }
      function up() {
        document.removeEventListener("mousemove", mv);
        document.removeEventListener("mouseup", up);
      }
      document.addEventListener("mousemove", mv);
      document.addEventListener("mouseup", up);
      e.preventDefault();
    });
  }

  function closeWin(win) {
    var layer = $("dialogLayer");
    if (win._ctx && win._ctx.ov && win._ctx.ov.parentNode) layer.removeChild(win._ctx.ov);
    if (win.parentNode) layer.removeChild(win);
  }

  function winBtn(win, label) {
    var ctx = win._ctx, key = ctx.key, row = ctx.row;
    if (ctx.custom && ctx.custom(label, win)) return;
    switch (label) {
      case "关闭":
        closeWin(win); break;
      case "保存":
        var empties = [];
        win.querySelectorAll('[data-req="1"]').forEach(function (el) {
          if (!el.value || !String(el.value).trim()) empties.push(el.getAttribute("data-l") || "");
        });
        if (empties.length) { msgbox("error", "「" + empties[0] + "」为必填项，请填写后再保存！"); break; }
        msgbox("success", "保存成功！");
        closeWin(win);
        break;
      case "新建":
        closeWin(win); openEdit(key, null, "new"); break;
      case "复制":
        if (row) { closeWin(win); openEdit(key, row, "copy"); }
        break;
      case "删除":
        if (row) {
          confirmBox("确定要删除当前记录吗？", function () {
            var st = states[key];
            if (st) {
              var idx = st.rows.indexOf(row);
              if (idx >= 0) st.rows.splice(idx, 1);
              st.selRow = null;
              renderModule(key);
            }
            msgbox("success", "删除成功！");
          });
          closeWin(win);
        }
        break;
      case "生效": case "提交": case "制单中": case "取消":
        if (row) row.status = (label === "生效" || label === "提交") ? "生效" : "制作中";
        msgbox("success", "操作成功！");
        closeWin(win);
        if (states[key]) renderModule(key);
        break;
      default:
        msgbox("info", "原型演示：「" + label + "」操作未实现。");
    }
  }

  /* ============ 查询选项弹窗(通用: 控制哪些列参与快捷查询) ============ */
  function openQueryOptions(key) {
    var st = states[key];
    var layer = $("dialogLayer");
    var ov = document.createElement("div");
    ov.style.cssText = "position:absolute;inset:0;background:rgba(25,45,70,.22);pointer-events:auto";
    layer.appendChild(ov);
    var W = 250;
    var win = document.createElement("div");
    win.className = "win";
    win.style.cssText = "width:" + W + "px;min-width:" + W + "px;height:auto;min-height:0;left:" +
      Math.max(16, Math.round((window.innerWidth - W) / 2)) + "px;top:170px";
    var allOn = st.colCfg.every(function (c) { return c.query; });
    win._ctx = {
      key: key, row: null, mode: "cfg", ov: ov,
      custom: function (label, w) {
        if (label === "确 定" || label === "确定") {
          w.querySelectorAll(".qa-item[data-i]").forEach(function (it) {
            st.colCfg[parseInt(it.getAttribute("data-i"), 10)].query = it.querySelector("input").checked;
          });
          closeWin(w);
          st.page = 1;
          renderModule(key);
          return true;
        }
        if (label === "取 消" || label === "取消") { closeWin(w); return true; }
        return false;
      }
    };
    var s = '<div class="win-title"><span class="w-ico">▦</span><span class="w-text">查询选项</span><span class="w-btn" data-w="close" title="关闭">✕</span></div>';
    s += '<div class="win-body"><div class="win-content" style="padding:0;display:flex;flex-direction:column">';
    s += '<div class="qa-list">';
    s += '<label class="qa-item all"><input type="checkbox" class="qa-all"' + (allOn ? " checked" : "") + "> (Select All)</label>";
    st.colCfg.forEach(function (c, i) {
      s += '<label class="qa-item" data-i="' + i + '"><input type="checkbox"' + (c.query ? " checked" : "") + "> " + esc(c.t) + "</label>";
    });
    s += "</div>";
    s += '<div class="win-foot" style="justify-content:center;gap:10px"><button class="btn primary" data-btn="确 定">确 定</button><button class="btn" data-btn="取 消">取 消</button></div>';
    s += "</div></div>";
    win.innerHTML = s;
    layer.appendChild(win);
    /* (Select All) 联动 */
    win.querySelector(".qa-all").addEventListener("change", function () {
      var on = this.checked;
      win.querySelectorAll(".qa-item[data-i] input").forEach(function (cb) { cb.checked = on; });
    });
    win.querySelector('.w-btn[data-w="close"]').onclick = function () { closeWin(win); };
    enableDrag(win);
  }

  /* ============ 列设置弹窗(通用: 列宽/对齐/固定/折行/排序/合计) ============ */
  var CS_ALIGNS = ["默认", "居中", "靠左", "靠右"];
  var CS_SORTS = ["未设置", "正序", "倒序"];

  function openColumnSettings(key) {
    var st = states[key], mod = MODULES[key];
    var work = st.colCfg.map(function (c) { return Object.assign({}, c); });
    var selIdx = -1;
    var layer = $("dialogLayer");
    var ov = document.createElement("div");
    ov.style.cssText = "position:absolute;inset:0;background:rgba(25,45,70,.22);pointer-events:auto";
    layer.appendChild(ov);
    var W = 900, H = 560;
    var win = document.createElement("div");
    win.className = "win";
    win.style.cssText = "width:" + W + "px;height:" + H + "px;left:" +
      Math.max(16, Math.round((window.innerWidth - W) / 2)) + "px;top:" +
      Math.max(16, Math.min(110, Math.round((window.innerHeight - H) / 2) - 10)) + "px";

    function selOpt(list, v) {
      return list.map(function (o) { return "<option" + (o === v ? " selected" : "") + ">" + o + "</option>"; }).join("");
    }
    function rowHtml(c, i) {
      var cls = (i === selIdx ? "cs-sel " : "") + (c.w <= 0 ? "cs-hidden" : "");
      var s = '<tr class="' + cls.trim() + '" data-cs="' + i + '">';
      s += '<td class="ctr">' + (i + 1) + "</td>";
      s += "<td>" + esc(c.t) + "</td>";
      s += "<td>" + esc(c.k) + "</td>";
      s += '<td class="ctr"><select data-f="align">' + selOpt(CS_ALIGNS, c.align) + "</select></td>";
      s += '<td class="ctr"><input type="text" data-f="w" value="' + c.w + '"></td>';
      s += '<td class="ctr">-</td>';
      s += '<td class="ctr"><input type="checkbox" data-f="query"' + (c.query ? " checked" : "") + "></td>";
      s += '<td class="ctr"><input type="checkbox" data-f="fixed"' + (c.fixed ? " checked" : "") + "></td>";
      s += '<td class="ctr"><input type="checkbox" data-f="wrap"' + (c.wrap ? " checked" : "") + "></td>";
      s += '<td class="ctr"><select data-f="sort">' + selOpt(CS_SORTS, c.sort) + "</select></td>";
      s += '<td class="ctr"><input type="checkbox" data-f="sum"' + (c.sum ? " checked" : "") + "></td>";
      s += "</tr>";
      return s;
    }
    function renderRows() {
      win.querySelector(".cs-grid tbody").innerHTML = work.map(rowHtml).join("");
    }
    function apply() {
      work.forEach(function (c) {
        var n = parseInt(c.w, 10);
        if (isNaN(n) || n < 0) c.w = 60;
      });
      st.colCfg = work;
      closeWin(win);
      st.page = 1;
      renderModule(key);
    }

    win._ctx = {
      key: key, row: null, mode: "cfg", ov: ov,
      custom: function (label, w) {
        if (label === "确 定" || label === "确定") { apply(); return true; }
        if (label === "取 消" || label === "取消") { closeWin(w); return true; }
        if (label === "新增") { msgbox("info", "原型演示：新增列需要配置数据源字段。"); return true; }
        if (label === "删除") {
          if (selIdx < 0) { msgbox("warn", "请先选中要删除的列！"); return true; }
          work[selIdx].w = 0;
          renderRows();
          return true;
        }
        if (label === "上移" || label === "下移") {
          if (selIdx < 0) { msgbox("warn", "请先选中要移动的列！"); return true; }
          var to = label === "上移" ? selIdx - 1 : selIdx + 1;
          if (to < 0 || to >= work.length) return true;
          var tmp = work[selIdx]; work[selIdx] = work[to]; work[to] = tmp;
          selIdx = to;
          renderRows();
          return true;
        }
        return false;
      }
    };

    var s = '<div class="win-title"><span class="w-ico">▦</span><span class="w-text">列设置 - ' + esc(mod.title) + '</span><span class="w-btn" data-w="close" title="关闭">✕</span></div>';
    s += '<div class="win-body"><div class="win-content" style="padding:8px;display:flex;flex-direction:column">';
    s += '<div class="grid-wrap cs-grid" style="flex:1;border:1px solid #c7d5e3">';
    s += '<table class="grid"><thead><tr>';
    ["序号", "列标题", "列名称", "靠齐", "列宽", "格式", "查询", "固定", "折行", "排序", "合计"].forEach(function (h) {
      s += "<th>" + esc(h) + "</th>";
    });
    s += "</tr></thead><tbody>" + work.map(rowHtml).join("") + "</tbody></table>";
    s += "</div></div></div>";
    s += '<div class="win-foot" style="gap:6px">';
    ["新增", "删除", "上移", "下移"].forEach(function (b) {
      s += '<button class="btn" data-btn="' + b + '">' + b + "</button>";
    });
    s += '<span style="flex:1"></span>';
    s += '<button class="btn primary" data-btn="确 定">确 定</button><button class="btn" data-btn="取 消">取 消</button>';
    s += "</div>";
    win.innerHTML = s;
    layer.appendChild(win);

    /* 行选中(不重建 DOM, 保证下拉框可用) */
    win.addEventListener("click", function (e) {
      var tr = e.target.closest("tr[data-cs]");
      if (!tr) return;
      var i = parseInt(tr.getAttribute("data-cs"), 10);
      if (selIdx !== i) {
        selIdx = i;
        win.querySelectorAll("tr.cs-sel").forEach(function (t) { t.classList.remove("cs-sel"); });
        tr.classList.add("cs-sel");
      }
    });
    /* 编辑实时回写 work */
    win.addEventListener("change", function (e) {
      var t = e.target, tr = t.closest("tr[data-cs]");
      if (!tr || !t.getAttribute("data-f")) return;
      var c = work[parseInt(tr.getAttribute("data-cs"), 10)];
      var f = t.getAttribute("data-f");
      if (f === "align") c.align = t.value;
      else if (f === "w") { var n = parseInt(t.value, 10); c.w = isNaN(n) ? 60 : Math.max(0, n); }
      else if (f === "sort") c.sort = t.value;
      else c[f] = t.checked;
    });
    win.querySelector('.w-btn[data-w="close"]').onclick = function () { closeWin(win); };
    enableDrag(win);
  }

  /* 弹窗层事件委托: 窗口工具栏 / 选项卡切换 / 弹窗内迷你工具栏 */
  $("dialogLayer").addEventListener("click", function (e) {
    var b = e.target.closest("button.btn");
    if (b && !e.target.closest(".msgbox")) {
      var win = e.target.closest(".win");
      if (win) { winBtn(win, b.getAttribute("data-btn") || b.textContent.trim()); return; }
    }
    var dtab = e.target.closest(".dtab");
    if (dtab) {
      var group = dtab.closest(".tab-group");
      if (!group) return;
      var idx = dtab.getAttribute("data-ti");
      group.querySelectorAll(".dtab").forEach(function (t) {
        t.classList.toggle("active", t.getAttribute("data-ti") === idx);
      });
      group.querySelectorAll(".dtab-body").forEach(function (bd) {
        bd.style.display = bd.getAttribute("data-bi") === idx ? "block" : "none";
      });
    }
  });

  /* ============ 立库扫码终端 ============ */
  var SCAN_BAYS = ["月台1号回路", "月台2号回路", "月台3号回路", "月台4号回路", "月台5号回路",
    "月台6号回路", "月台7号回路", "月台8号回路", "出口自动", "5#自动线", "自动组托组箱"];

  function taskCardHtml(t) {
    return '<div class="task-card"><div class="tc-no"><span>' + esc(t.no) + '</span><span class="tc-time">' + esc(t.time) + "</span></div>" +
      '<div class="tc-batch">' + esc(t.batch) + " / " + esc(t.vat) + "</div>" +
      '<div class="tc-qty"><span class="need">需出: ' + esc(t.need) + '</span><span class="done">已出: ' + esc(t.done) + "</span></div></div>";
  }
  function scanPanelHtml(title, tasks) {
    var kg = 0;
    tasks.forEach(function (t) { kg += parseFloat(t.need) || 0; });
    var s = '<div class="sw-panel"><div class="swp-title">' + title + '</div><div class="sw-list">';
    tasks.forEach(function (t) { s += taskCardHtml(t); });
    s += "</div>";
    s += '<div class="sw-sum">【' + title + '合计】 ' + tasks.length + " 单 · 需出 " + kg.toFixed(2) + "KG</div></div>";
    return s;
  }

  function buildScan() {
    var p = document.createElement("div");
    p.className = "page scan-page";
    p.id = "page-scanTerminal";
    var s = "";
    /* 头部 */
    s += '<div class="scan-head"><div class="sh-brand"><div class="brand-mark" style="width:36px;height:36px;font-size:19px">W</div>' +
      '<div><div class="sh-title">立库扫码终端</div><div style="font-size:10px;color:#5b7286;letter-spacing:.5px">YUNSUO WMS · ASRS SCAN TERMINAL V3.69</div></div></div>' +
      '<div class="scan-exit" data-act="exit">退出终端</div></div>';
    /* 主界面 */
    s += '<div class="scan-main" data-view="main"><div class="scan-btns">';
    s += '<div class="scan-btn" data-act="barcode"><div class="sb-ico">▥</div><div class="sb-text">条码出库</div></div>';
    s += '<div class="scan-btn" data-act="empty"><div class="sb-ico">▢</div><div class="sb-text">空托出库</div></div>';
    s += '<div class="scan-btn" data-act="test"><div class="sb-ico">▣</div><div class="sb-text">条码出库测试</div></div>';
    s += "</div></div>";
    /* 条码出库作业 */
    s += '<div class="scan-work" data-view="work">';
    s += '<div class="sw-top">';
    s += '<div class="sw-search"><input type="text" placeholder="扫描 / 输入 批次任务号"><button class="btn" data-act="search">检 索</button></div>';
    s += '<div class="bay-tabs">';
    SCAN_BAYS.forEach(function (b, i) {
      s += '<span class="bay-tab' + (i === 0 ? " active" : "") + '">' + b + "</span>";
    });
    s += "</div>";
    s += '<div class="sw-actions"><span class="btn-start" data-act="start">开始作业</span><span class="btn-cancel" data-act="back">取消返回</span></div>';
    s += "</div>";
    s += '<div class="sw-panels">';
    s += scanPanelHtml("业务批次任务", D.scanTasks());
    s += scanPanelHtml("托盘批次任务", D.scanTasks());
    s += "</div></div>";
    /* 底部 */
    s += '<div class="scan-foot"><span>操作员: admin 管理员</span><span class="sf-time">--:--:--</span></div>';
    p.innerHTML = s;

    p.addEventListener("click", function (e) {
      var act = e.target.closest("[data-act]");
      var bay = e.target.closest(".bay-tab");
      var card = e.target.closest(".task-card");
      if (act) {
        var a = act.getAttribute("data-act");
        var main = p.querySelector('[data-view="main"]');
        var work = p.querySelector('[data-view="work"]');
        if (a === "barcode") { main.style.display = "none"; work.classList.add("active"); }
        else if (a === "back") { work.classList.remove("active"); main.style.display = ""; }
        else if (a === "exit") { closeTab("scanTerminal"); }
        else if (a === "start") {
          if (!p.querySelector(".task-card.selected")) {
            msgbox("warn", "请选择有有效的批次任务，再进行选托下架作业！");
          } else {
            msgbox("success", "任务下发成功，输送线已启动选托下架作业！");
          }
        } else if (a === "empty" || a === "test") {
          msgbox("info", "原型演示：「" + act.querySelector(".sb-text").textContent + "」界面未实现。");
        } else if (a === "search") {
          msgbox("info", "原型演示：检索功能未实现。");
        }
        return;
      }
      if (bay) {
        p.querySelectorAll(".bay-tab").forEach(function (t) { t.classList.remove("active"); });
        bay.classList.add("active");
        return;
      }
      if (card) {
        var list = card.parentNode;
        var on = card.classList.contains("selected");
        list.querySelectorAll(".task-card").forEach(function (c) { c.classList.remove("selected"); });
        if (!on) card.classList.add("selected");
      }
    });
    $("pageContainer").appendChild(p);
  }

  /* ============ PDA 工作台(手持终端) ============ */
  var PDA_ICONS = {
    code: '<svg viewBox="0 0 48 48" fill="none"><path d="M7 14 L22 7 L37 14 L37 29 L22 36 L7 29 Z" fill="#4a4a4a" fill-opacity=".16" stroke="#4a4a4a" stroke-width="2.4" stroke-linejoin="round"/><path d="M7 14 L22 21 L37 14 M22 21 V36" stroke="#4a4a4a" stroke-width="2.4" stroke-linejoin="round"/><circle cx="33" cy="33" r="8" stroke="#00a0b0" stroke-width="3"/><path d="M39 39 L45 45" stroke="#00a0b0" stroke-width="3.4" stroke-linecap="round"/></svg>',
    pallet: '<svg viewBox="0 0 48 48" fill="none"><g stroke="#4a4a4a" stroke-width="2.4"><rect x="12" y="12" width="11" height="11" fill="#fff"/><rect x="25" y="12" width="11" height="11" fill="#fff"/><rect x="18" y="25" width="11" height="11" fill="#fff"/></g><path d="M7 42 H41" stroke="#4a4a4a" stroke-width="3.4" stroke-linecap="round"/><g stroke="#4a90d9" stroke-width="2.8" stroke-linecap="round"><path d="M40 4 V12 M36 8 H44"/></g></svg>',
    inbound: '<svg viewBox="0 0 48 48" fill="none" stroke="#4a90d9" stroke-width="2.6" stroke-linejoin="round"><path d="M6 21 L24 8 L42 21 V42 H6 Z"/><path d="M17 29 H31 M26 24 L31 29 L26 34" stroke-linecap="round"/></svg>',
    outbound: '<svg viewBox="0 0 48 48" fill="none" stroke="#4a90d9" stroke-width="2.6" stroke-linejoin="round"><path d="M6 21 L24 8 L42 21 V42 H6 Z"/><path d="M31 29 H17 M22 24 L17 29 L22 34" stroke-linecap="round"/></svg>',
    move: '<svg viewBox="0 0 48 48" fill="none" stroke="#4a4a4a" stroke-width="2.5" stroke-linecap="round"><rect x="4" y="17" width="13" height="13" rx="1.5"/><rect x="31" y="17" width="13" height="13" rx="1.5"/><path d="M20 23.5 H28 M24.5 20 L28 23.5 L24.5 27 M23.5 20 L20 23.5 L23.5 27"/></svg>',
    transfer: '<svg viewBox="0 0 48 48" fill="none" stroke="#4a4a4a" stroke-width="2.5" stroke-linejoin="round" stroke-linecap="round"><path d="M3 21 L13 13 V33 H3 Z"/><path d="M35 13 L45 21 V33 H35 Z"/><path d="M16 23 H32 M28 19 L32 23 L28 27"/></svg>',
    sync: '<svg viewBox="0 0 48 48" fill="none"><g stroke="#4a4a4a" stroke-width="2.4" stroke-linejoin="round"><path d="M11 5 H28 L36 13 V43 H11 Z"/><path d="M28 5 V13 H36"/><path d="M17 20 H30 M17 26 H30" stroke-linecap="round"/></g><g stroke="#4a90d9" stroke-width="2.6" stroke-linecap="round" stroke-linejoin="round"><path d="M22 33 V45 M18 37 L22 33 L26 37 M18 41 L22 45 L26 41"/></g></svg>',
    scanP: '<svg viewBox="0 0 48 48" fill="none"><path d="M6 15 V6 H15 M33 6 H42 V15 M42 33 V42 H33 M15 42 H6 V33" stroke="#4a4a4a" stroke-width="3" stroke-linecap="round"/><path d="M13 24 H35" stroke="#4a90d9" stroke-width="2.8" stroke-linecap="round"/></svg>',
    home: '<svg viewBox="0 0 26 26"><circle cx="13" cy="13" r="12" fill="#8d9aa8"/><path d="M6.5 13 L13 6.5 L19.5 13 M8.5 12 V19.5 H17.5 V12" stroke="#fff" stroke-width="1.9" fill="none" stroke-linejoin="round" stroke-linecap="round"/></svg>'
  };

  function buildPda() {
    var p = document.createElement("div");
    p.className = "page pda-page";
    p.id = "page-pdaWork";
    var funcs = [
      ["code", "码单收录"], ["pallet", "组托"],
      ["inbound", "入库"], ["outbound", "出库"],
      ["move", "移库"], ["transfer", "调拨"],
      ["sync", "同步状态查询"], ["scanP", "扫托查询"]
    ];
    var s = '<div class="pda-frame">';
    s += '<div class="pda-head"><span class="pda-home" data-act="home" title="主页">' + PDA_ICONS.home + '</span><span class="p-title">工作台</span></div>';
    s += '<div class="pda-grid">';
    funcs.forEach(function (f) {
      s += '<div class="pda-item" data-f="' + f[0] + '" data-name="' + f[1] + '">' + PDA_ICONS[f[0]] + '<span class="p-label">' + f[1] + "</span></div>";
    });
    s += "</div></div>";
    p.innerHTML = s;
    p.addEventListener("click", function (e) {
      if (e.target.closest("[data-act='home']")) { openTab("home"); return; }
      var it = e.target.closest(".pda-item");
      if (it) msgbox("info", "原型演示：「" + it.getAttribute("data-name") + "」作业界面未实现。");
    });
    $("pageContainer").appendChild(p);
  }

  /* ============ 状态栏时钟 / 全局 ============ */
  function clock() {
    var d = new Date();
    var t = fmtDT(d);
    $("serverTime").textContent = "服务器时间: " + t;
    var sf = document.querySelectorAll(".sf-time");
    for (var i = 0; i < sf.length; i++) sf[i].textContent = t.split(" ")[1];
  }

  window.exitSystem = function () {
    confirmBox("确定要退出系统吗？", function () {
      msgbox("info", "原型演示：退出系统操作。");
    });
  };

  /* ============ 启动 ============ */
  renderMenu();
  openTab("home");
  $("loginTime").textContent = "登录时间: " + fmtDT(new Date());
  clock();
  setInterval(clock, 1000);

  /* 标题栏链接 */
  var titleLinks = document.querySelectorAll(".title-links a");
  if (titleLinks[0]) titleLinks[0].onclick = function () { msgbox("info", "原型演示：修改密码窗口未实现。"); };
  if (titleLinks[1]) titleLinks[1].onclick = function () { msgbox("info", "原型演示：注销操作未实现。"); };
})();
