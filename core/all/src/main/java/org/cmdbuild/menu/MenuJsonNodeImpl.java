/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cmdbuild.menu;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import jakarta.annotation.Nullable;
import static java.util.Collections.emptyList;
import java.util.List;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.cmdbuild.menu.MenuItemType.CLASS;
import static org.cmdbuild.menu.MenuItemType.CUSTOM_PAGE;
import static org.cmdbuild.menu.MenuItemType.DASHBOARD;
import static org.cmdbuild.menu.MenuItemType.FOLDER;
import static org.cmdbuild.menu.MenuItemType.GEOATTRIBUTE;
import static org.cmdbuild.menu.MenuItemType.NAVTREE;
import static org.cmdbuild.menu.MenuItemType.PROCESS;
import static org.cmdbuild.menu.MenuItemType.REPORT_CSV;
import static org.cmdbuild.menu.MenuItemType.REPORT_ODT;
import static org.cmdbuild.menu.MenuItemType.REPORT_PDF;
import static org.cmdbuild.menu.MenuItemType.REPORT_RTF;
import static org.cmdbuild.menu.MenuItemType.REPORT_XML;
import static org.cmdbuild.menu.MenuItemType.SYSTEM_FOLDER;
import static org.cmdbuild.menu.MenuItemType.VIEW;
import static org.cmdbuild.utils.lang.CmConvertUtils.serializeEnum;
import static org.cmdbuild.utils.lang.CmExceptionUtils.unsupported;
import static org.cmdbuild.utils.lang.CmMapUtils.map;
import static org.cmdbuild.utils.lang.CmNullableUtils.firstNotNull;
import static org.cmdbuild.utils.lang.CmPreconditions.checkNotBlank;
import static org.cmdbuild.utils.lang.CmPreconditions.checkNotNull;

public class MenuJsonNodeImpl implements MenuJsonNode {

    public final static BiMap<MenuItemType, String> MENU_ITEM_TYPE_SERIALIZATION_MAP = HashBiMap.create(map(
            CLASS, "class",
            GEOATTRIBUTE, "geoattribute",
            DASHBOARD, "dashboard",
            PROCESS, "processclass",
            FOLDER, "folder",
            SYSTEM_FOLDER, "system_folder",
            REPORT_CSV, "reportcsv",
            REPORT_PDF, "reportpdf",
            REPORT_ODT, "reportodt",
            REPORT_RTF, "reportrtf",
            REPORT_XML, "reportxml",
            VIEW, "view",
            CUSTOM_PAGE, "custompage",
            NAVTREE, serializeEnum(NAVTREE)));

    private final MenuItemType menuType;
    private final String target, description, code;
    private final List<MenuJsonNode> children;

    @JsonCreator
    public MenuJsonNodeImpl(@JsonProperty("type") String menuType,
            @JsonProperty("target") String target,
            @JsonProperty("code") String code,
            @JsonProperty("description") String description,
            @JsonProperty("children") List<MenuJsonNodeImpl> children) {
        this(checkNotNull(MENU_ITEM_TYPE_SERIALIZATION_MAP.inverse().get(menuType), "invalid menu type = '%s'", menuType), target, description, code, (List) children);
    }

    public MenuJsonNodeImpl(MenuItemType menuType, String target, String description, String code, List<MenuJsonNode> children) {
        this.menuType = checkNotNull(menuType);
        switch (menuType) {
            case CLASS, PROCESS, REPORT_CSV, REPORT_PDF, REPORT_ODT, REPORT_RTF, REPORT_XML, CUSTOM_PAGE, DASHBOARD, VIEW, NAVTREE, GEOATTRIBUTE ->
                this.target = checkNotBlank(target, "must set valid target for menu item node of type = %s", menuType);
            case FOLDER, ROOT, SYSTEM_FOLDER ->
                this.target = null;
            default ->
                throw unsupported("unsupported menu item type = %s", menuType);
        }
        this.description = trimToNull(description);
        this.code = checkNotBlank(code);
        this.children = firstNotNull(children, emptyList()).stream().map(MenuJsonNode.class::cast).collect(toUnmodifiableList());
    }

    @JsonProperty("type")
    public String getMenuTypeAsJson() {
        return checkNotNull(MENU_ITEM_TYPE_SERIALIZATION_MAP.get(menuType));
    }

    @Override
    @JsonIgnore
    public MenuItemType getMenuType() {
        return menuType;
    }

    @Override
    @Nullable
    @JsonProperty("target")
    public String getTarget() {
        return target;
    }

    @Override
    @Nullable
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @Override
    @JsonProperty("children")
    public List<MenuJsonNode> getChildren() {
        return children;
    }

    @Override
    @JsonProperty("code")
    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "MenuJsonNodeImpl{" + "menuType=" + menuType + ", objectType=" + target + ", objectDescription=" + description + ", code=" + code + '}';
    }

}
