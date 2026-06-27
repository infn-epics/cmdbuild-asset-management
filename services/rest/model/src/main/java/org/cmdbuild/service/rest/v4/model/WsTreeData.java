/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.cmdbuild.service.rest.v4.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.cmdbuild.navtree.NavTreeImpl;
import org.cmdbuild.navtree.NavTreeType;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Iterables.getOnlyElement;
import static org.cmdbuild.navtree.NavTreeType.NT_DEFAULT;
import static org.cmdbuild.utils.lang.CmConvertUtils.parseEnumOrDefault;
import static org.cmdbuild.utils.lang.CmPreconditions.checkNotBlank;

/**
 *
 * @author schursin
 */
public class WsTreeData {

    private final String name, description;
    private final WsTreeNodeData data;
    private final boolean active;
    private final NavTreeType type;

    public WsTreeData(@JsonProperty("name") String name,
                      @JsonProperty("description") String description,
                      @JsonProperty("nodes") List<WsTreeNodeData> nodes,
                      @JsonProperty("active") boolean active,
                      @JsonProperty("type") String type) {
        this.name = checkNotBlank(name, "nav tree name cannot be null");
        this.description = nullToEmpty(description);
        this.data = checkNotNull(getOnlyElement(nodes, null), "a nav tree must have a root node");
        this.active = active;
        this.type = parseEnumOrDefault(type, NT_DEFAULT);
    }

    public NavTreeImpl.NavTreeDataImplBuilder toTreeNode() {
        return NavTreeImpl.builder()
                .withName(name)
                .withDescription(description)
                .withData(data.toTreeNode())
                .withActive(active)
                .withType(type);
    }
}
