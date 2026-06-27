/*
 * CMDBuild has been developed and is managed by PAT srl
 * You can use, distribute, edit CMDBuild according to the license
 */
package org.cmdbuild.service.rest.v4.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.cmdbuild.menu.MenuItemType;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.cmdbuild.service.rest.common.serializationhelpers.MenuSerializationHelper.MENU_ITEM_TYPE_WS_MAP;
import static org.cmdbuild.utils.lang.CmPreconditions.checkNotBlank;
import static org.cmdbuild.utils.random.CmRandomUtils.randomId;

/**
 * @author ldare
 */
public class MenuNodeWsBean {

    public final MenuItemType menuType;
    public final String target, objectDescription, code;
    public final List<MenuNodeWsBean> children;

    public MenuNodeWsBean(
            @JsonProperty("menuType") String menuType,
            @JsonProperty("objectTypeName") String target,
            @JsonProperty("_id") String code,
            @JsonProperty("objectDescription") String objectDescription,
            @JsonProperty("children") List<MenuNodeWsBean> children) {
        this.menuType = checkNotNull(MENU_ITEM_TYPE_WS_MAP.inverse().get(checkNotBlank(menuType)), "unknown menu type = '%s'", menuType);
        this.target = target;
        this.objectDescription = objectDescription;
        this.code = firstNonNull(emptyToNull(code), randomId());
        this.children = firstNonNull(children, emptyList());
    }
}
