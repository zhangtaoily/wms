package com.wms.service.common.service;

import com.wms.common.utils.MapUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
public class TableNode {
    private Class<?> tableClass;
    private String tableName;
    private String parentTable;
    private Class<?> mapper;
    private String primaryKey;
    private String foreignKey;
    private String joinColumns;
    private String joinTables;
    private String orderBy;
    private Boolean isGoods = false;
    //复制的时候是否清空表
    private Boolean isCleanOnCopy = false;
    private Map<String, String> autoCodeJoinColumns = MapUtils.empty();
    private Map<String, String> unionColumns = MapUtils.empty();
    private List<TableNode> children = new CopyOnWriteArrayList<>();

    /**
     * 获取根节点
     *
     * @return 根节点TableNode
     * @throws IllegalStateException 如果找不到根节点
     */
    public TableNode fetchRoot() {
        return tree2List().stream()
            .filter(node -> node.getParentTable() == null)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No root node found"));
    }

    /**
     * 将树结构转换为列表
     *
     * @return 包含所有节点的列表
     */
    public List<TableNode> tree2List() {
        List<TableNode> result = new ArrayList<>();
        if (this.children != null) {
            traverseNode(this, result);
        }
        return result;
    }

    private void traverseNode(TableNode node, List<TableNode> result) {
        if (node == null) return;
        result.add(node);
        node.getChildren().forEach(child -> traverseNode(child, result));
    }

    public static List<TableNode> findChildNodes(TableNode root) {
        List<TableNode> childNodes = new ArrayList<>();
        findChildNodesRecursive(root, childNodes);
        return childNodes;
    }

    private static void findChildNodesRecursive(TableNode node, List<TableNode> childNodes) {
        for (TableNode child : node.getChildren()) {
            childNodes.add(child);
            findChildNodesRecursive(child, childNodes);
        }
    }

    /**
     * 获取明细子表节点
     *
     * @return 商品子表节点，如果没有则返回null
     */
    public TableNode getGoodsTableNode() {
        return this.children.stream()
            .filter(TableNode::getIsGoods)
            .findFirst()
            .orElse(null);
    }

    /**
     * 添加子节点
     *
     * @param child 要添加的子节点
     * @throws IllegalArgumentException 如果child为null
     */
    public void addChild(TableNode child) {
        if (child == null) {
            throw new IllegalArgumentException("Child node cannot be null");
        }
        this.children.add(child);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableNode tree = (TableNode) o;
        return Objects.equals(tableClass, tree.tableClass) && Objects.equals(parentTable, tree.parentTable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableClass, parentTable);
    }
}
