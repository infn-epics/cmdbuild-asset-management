/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.cmdbuild.service.rest.v4.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.cmdbuild.navtree.NavTreeNode;
import org.cmdbuild.navtree.NavTreeNodeImpl;
import org.cmdbuild.navtree.NavTreeNodeSubclassViewMode;
import org.cmdbuild.utils.lang.CmStringUtils;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.cmdbuild.dao.beans.RelationDirection.RD_DIRECT;
import static org.cmdbuild.dao.beans.RelationDirection.RD_INVERSE;
import static org.cmdbuild.utils.json.CmJsonUtils.*;
import static org.cmdbuild.utils.lang.CmConvertUtils.*;
import static org.cmdbuild.utils.lang.CmInlineUtils.unflattenMap;
import static org.cmdbuild.utils.lang.CmMapUtils.map;
import static org.cmdbuild.utils.lang.CmPreconditions.firstNotBlank;
import static org.cmdbuild.utils.lang.CmStringUtils.toStringOrDefault;
import static org.cmdbuild.utils.lang.CmStringUtils.toStringOrNull;
import static org.cmdbuild.utils.random.CmRandomUtils.randomId;

/**
 *
 * @author schursin
 */
public class WsTreeNodeData {

    private final String id;
    private final String filter, targetClass, description, domain, direction;
    private final Boolean recursionEnabled, showOnlyOne, subclassViewShowIntermediateNodes;
    private final List<WsTreeNodeData> nodes;
    private final NavTreeNodeSubclassViewMode subclassViewMode;
    private final List<String> subclassFilter;
    private final Map<String, String> subclassDescriptions;

    @JsonCreator
    public WsTreeNodeData(Map<String, Object> values) {
        this.id = toStringOrNull(values.get("_id"));
        this.filter = toStringOrNull(values.get("filter"));
        this.targetClass = toStringOrNull(values.get("targetClass"));
        this.description = toStringOrNull(values.get("description"));
        this.domain = toStringOrNull(values.get("domain"));
        this.direction = toStringOrDefault(values.get("direction"), "_1");
        this.recursionEnabled = toBooleanOrNull(values.get("recursionEnabled"));
        this.showOnlyOne = toBooleanOrNull(values.get("showOnlyOne"));
        this.nodes = fromJson(toJson(values.get("nodes")), collectionType(WsTreeNodeData.class));
        this.subclassViewMode = parseEnumOrNull(toStringOrNull(values.get("subclassViewMode")), NavTreeNodeSubclassViewMode.class);
        this.subclassViewShowIntermediateNodes = toBooleanOrNull(values.get("subclassViewShowIntermediateNodes"));
        this.subclassFilter = toListOfStrings(toStringOrNull(values.get("subclassFilter")));
        this.subclassDescriptions = map(unflattenMap(values, "subclass")).mapValues(CmStringUtils::toStringOrNull).mapKeys(k -> k.replaceFirst("_description$", "")).immutable();
    }

    public NavTreeNode toTreeNode() {
        return NavTreeNodeImpl.builder()
                .withId(firstNotBlank(id, randomId()))
                .withTargetFilter(filter)
                .withTargetClassName(targetClass)
                .withTargetClassDescription(description)
                .withDomainName(domain)
                .withDirection(parseDirection(direction) ? RD_DIRECT : RD_INVERSE)
                .withEnableRecursion(recursionEnabled)
                .withShowOnlyOne(showOnlyOne)
                .withSubclassViewMode(subclassViewMode)
                .withSubclassViewShowIntermediateNodes(subclassViewShowIntermediateNodes)
                .withSubclassFilter(subclassFilter)
                .withSubclassDescriptions(subclassDescriptions)
                .withChildNodes(nodes.stream().map(WsTreeNodeData::toTreeNode).collect(toImmutableList()))
                .build();
    }

    private static boolean parseDirection(String direction) {
        return switch (nullToEmpty(direction)) {
            case "_1" -> true;
            case "_2" -> false;
            default -> throw new IllegalArgumentException("invalid direction = " + direction);
        };
    }
}
