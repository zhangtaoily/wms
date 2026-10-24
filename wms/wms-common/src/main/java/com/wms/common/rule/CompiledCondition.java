package com.wms.common.rule;

import com.wms.common.exception.ServiceException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 条件树编译产物：{@code ?} 占位 SQL 片段 + 顺序参数列表。
 * <p>片段不含任何用户值文本，配合 CustomizeMapper 参数化重载完成绑定（三层防线之三）。</p>
 *
 * @author wms
 */
public class CompiledCondition {

    private final String fragment;

    private final List<Object> params;

    public CompiledCondition(String fragment, List<Object> params) {
        this.fragment = fragment;
        this.params = params;
    }

    public String getFragment() {
        return fragment;
    }

    public List<Object> getParams() {
        return params;
    }

    /**
     * 将 {@code ?} 占位逐个替换为 MyBatis 绑定表达式 {@code #{paramKey.pN}}，
     * 配合 CustomizeMapper 的 @Param("paramKey") Map&lt;String,Object&gt; 使用。
     * <p>${sql} 先替换、SqlSourceBuilder 再解析 #{paramKey.pN} 完成绑定：
     * 纯 Map 键跳转（与 ${ew.customSqlSegment} 的 paramNameValuePairs 同构，机制可靠）；
     * 不用 #{paramKey[i]} 是因为 MapWrapper 对 List 下标不生效。</p>
     */
    public String toMybatisFragment(String paramKey) {
        StringBuilder sb = new StringBuilder(fragment.length() + params.size() * 16);
        int index = 0;
        for (int i = 0; i < fragment.length(); i++) {
            char c = fragment.charAt(i);
            if (c == '?') {
                sb.append("#{").append(paramKey).append(".p").append(index++).append('}');
            } else {
                sb.append(c);
            }
        }
        if (index != params.size()) {
            throw new ServiceException("条件树编译产物异常: 占位符数量 " + index + " 与参数数量 " + params.size() + " 不一致");
        }
        return sb.toString();
    }

    /**
     * 编译产物参数转 MyBatis 绑定 Map（p0 → v0, p1 → v1 ...），与 {@link #toMybatisFragment} 同一命名约定。
     */
    public Map<String, Object> toMybatisParams() {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < params.size(); i++) {
            map.put("p" + i, params.get(i));
        }
        return map;
    }
}
