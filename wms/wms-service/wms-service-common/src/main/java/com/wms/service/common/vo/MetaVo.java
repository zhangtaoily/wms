package com.wms.service.common.vo;

import com.wms.common.utils.StringUtils;
import lombok.Data;

/**
 * 路由显示信息
 *
 * @author ruoyi
 */

@Data
public class MetaVo {

    /**
     * 设置该路由在侧边栏和面包屑中展示的名字
     */
    private String title;

    /**
     * 设置该路由的图标，对应路径src/assets/icons/svg
     */
    private String icon;

    /**
     * 设置为true，则不会被 <keep-alive>缓存
     */
    private boolean noCache;

    /**
     * 内链地址（http(s)://开头）
     */
    private String link;

    /**
     * 权限
     */
    private String perms;

    /**
     * 是否业务单据
     */
    private Integer isBusinessDoc;

    /**
     * 开启按钮权限
     */
    private Integer isOpenBtnPerms;

    public MetaVo(String title, String icon) {
        this.title = title;
        this.icon = icon;
    }

    public MetaVo(String title, String icon, boolean noCache) {
        this.title = title;
        this.icon = icon;
        this.noCache = noCache;
    }

    public MetaVo(String title, String icon, String link) {
        this.title = title;
        this.icon = icon;
        this.link = link;
    }

    public MetaVo(String title, String icon, boolean noCache, String link) {
        this.title = title;
        this.icon = icon;
        this.noCache = noCache;
        if (StringUtils.ishttp(link)) {
            this.link = link;
        }
    }

    public MetaVo(String title, String icon, boolean noCache, String link,String perms) {
        this.title = title;
        this.icon = icon;
        this.noCache = noCache;
        this.perms = perms;
        if (StringUtils.ishttp(link)) {
            this.link = link;
        }
    }

    public MetaVo(String title, String icon, boolean noCache, String link,String perms,Integer isBusinessDoc,Integer isOpenBtnPerms) {
        this.title = title;
        this.icon = icon;
        this.noCache = noCache;
        this.perms = perms;
        this.isBusinessDoc = isBusinessDoc;
        this.isOpenBtnPerms = isOpenBtnPerms;
        if (StringUtils.ishttp(link)) {
            this.link = link;
        }
    }

}
