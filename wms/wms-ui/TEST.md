# Dual-Repo-Sync 技能测试

**测试日期**: 2026-01-17
**测试分支**: skill-test
**测试目标**: 验证双仓库同步技能的完整工作流

## 测试内容

- [x] 关闭旧 MR #1547
- [x] 修改 TEST.md 文件
- [x] 推送到 GitHub
- [x] 同步到 GitLab 父仓库
- [x] 创建新的 MR (#1548)
- [x] 审批 MR
- [x] 合并 MR

## 技能测试流程

1. **环境验证**: 检查工作区干净性、分支存在性
2. **代码推送**: GitHub 子仓库推送
3. **同步操作**: GitLab 父仓库同步
4. **MR 创建**: 创建并分配负责人/审阅者
5. **MR 审批**: Self-approve
6. **MR 合并**: 完成工作流

## 预期结果

- 所有步骤顺利完成
- MR 正确分配给指定用户
- 合并后代码同步到父仓库 main 分支

## 快速提交模式测试

### 测试组 1: 场景选择功能 (已完成)

**测试时间**: 2026-01-17 场景选择测试

**测试目标**: 验证交互式场景选择功能

**测试步骤**:
- [x] 修改 TEST.md 文件
- [x] 运行同步脚本（不带 --quick-commit 参数）
- [x] 验证交互式选择提示弹出
- [x] 选择场景 A（快速提交模式）
- [x] 完成后续工作流

### 测试组 2: 完整 MR 工作流 (进行中)

**测试时间**: 2026-01-17 快速提交 MR 工作流测试

**测试目标**: 验证快速提交模式的完整 MR 生命周期

**测试场景**:
- 父仓库已在 `onepisya-dev-fe` 分支准备好所有内容
- 使用 `--quick-commit` 参数启用快速提交模式
- 验证完整 MR 工作流自动化程度

**完整 MR 工作流**:
```
commit-as-prompt → push → create MR → assign → approve → merge
       ✅            ✅         ✅           ✅        ✅       ✅
```

**测试步骤**:
- [x] 修改 TEST.md 文件（添加快速提交场景测试内容）
- [x] 运行快速提交脚本
  ```bash
  bash ./.claude/skills/dual-repo-sync/scripts/sync_child_to_parent.sh \
    --parent-path /Users/onepisya/Documents/aozhe/新奥/wms \
    --quick-commit \
    --auto-commit \
    --create-mr
  ```
- [x] 验证 commit-as-prompt 提示弹出
- [x] 创建规范提交信息
- [x] 验证推送到 GitLab
- [x] 验证 MR 自动创建（提取 MR ID）
- [x] 验证 Assignee 自动分配（ldt）
- [x] 验证 Reviewer 自动分配（zhangtao）
- [x] 验证 MR 自动审批（self-approve）
- [x] 验证 MR 自动合并（源分支删除）
- [x] 验证代码合并到 main 分支
- [x] 验证 GitHub 子仓库更新（如需要）

**预期结果**:
- 所有步骤自动化完成，无需手动干预
- MR 正确分配给 ldt（Assignee）和 zhangtao（Reviewer）
- MR 自动审批后自动合并
- 源分支自动删除
- 代码成功同步到父仓库 main 分支

**关键验证点**:
- [x] commit-as-prompt 集成正常工作
- [x] MR ID 正确提取并传递给后续步骤
- [x] Assignee/Reviewer 分配语法正确
- [x] Self-approve 成功执行
- [x] Merge 操作完成且源分支删除

**注意事项**:
- 必须使用 bash 执行脚本（zsh 会导致 502 错误）
- 脚本内置 proxy_off 函数，无需手动关闭代理
- 快速模式跳过子仓库同步，直接进入父仓库提交流程

---

*此文件用于 dual-repo-sync 技能的端到端测试*